package com.bismarck.voleimanager.app.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val CLOUD_GROUPS_COLLECTION = "cloudGroups"
private const val LIVE_STATE_COLLECTION = "liveState"
private const val LIVE_STATE_DOC_ID = "current"
private const val HISTORY_COLLECTION = "history"
private const val ELO_LOGS_COLLECTION = "eloLogs"
private const val FIELD_VISIBILITY = "visibility"
private const val FIELD_SHARE_HISTORY = "shareHistoryWithObservers"
private const val FIELD_SHOW_ELO = "showEloToObservers"
private const val REMOTE_LIST_LIMIT = 100L

/** Jogador "enxuto" sincronizado em `liveState` — usa [publicId] (estável entre dispositivos) em
 *  vez do id local autoGenerate do Room, que não tem significado fora do aparelho de origem. */
data class RemotePlayerSnapshot(
    val publicId: String = "",
    val name: String = "",
    val elo: Double = 1200.0,
    val isPriority: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "publicId" to publicId,
        "name" to name,
        "elo" to elo,
        "isPriority" to isPriority
    )

    companion object {
        fun fromMap(map: Map<*, *>): RemotePlayerSnapshot = RemotePlayerSnapshot(
            publicId = map["publicId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            elo = (map["elo"] as? Number)?.toDouble() ?: 1200.0,
            isPriority = map["isPriority"] as? Boolean ?: false
        )
    }
}

/** Espelha o jogo em andamento de um grupo premium sincronizado: times em quadra, fila de espera
 *  e placar — é isto que um(a) Espectador(a) (ou um Auxiliar que entrou via código, antes de
 *  `role-permission-matrix` liberar escrita remota) vê em tempo real na tela "Ao vivo". */
data class LiveGameState(
    val groupName: String = "",
    val teamA: List<RemotePlayerSnapshot> = emptyList(),
    val teamB: List<RemotePlayerSnapshot> = emptyList(),
    val waitingList: List<RemotePlayerSnapshot> = emptyList(),
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val currentStreak: Int = 0,
    val streakOwner: String? = null,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "groupName" to groupName,
        "teamA" to teamA.map { it.toMap() },
        "teamB" to teamB.map { it.toMap() },
        "waitingList" to waitingList.map { it.toMap() },
        "scoreA" to scoreA,
        "scoreB" to scoreB,
        "currentStreak" to currentStreak,
        "streakOwner" to streakOwner,
        "updatedAt" to updatedAt
    )
}

/** Entrada de histórico "enxuta" (equivalente a um subconjunto de [com.bismarck.voleimanager.app.data.model.MatchHistory])
 *  publicada em `cloudGroups/{id}/history` — só visível a observadores quando
 *  `visibility.shareHistoryWithObservers` estiver ligado. */
data class RemoteHistoryEntry(
    val id: String = "",
    val date: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val winner: String = "",
    val teamAScore: Int? = null,
    val teamBScore: Int? = null,
    val endTimestamp: Long? = null
)

/** Entrada de Elo "enxuta" publicada em `cloudGroups/{id}/eloLogs` — só visível a observadores
 *  quando `visibility.shareHistoryWithObservers` E `visibility.showEloToObservers` estiverem
 *  ligados (ver firestore.rules). */
data class RemoteEloLogEntry(
    val playerNameSnapshot: String = "",
    val date: String = "",
    val elo: Double = 1200.0,
    val won: Boolean = false
)

/** Toggles de visibilidade do grupo para observadores, espelhados de `cloudGroups/{id}.visibility`. */
data class GroupVisibility(
    val shareHistoryWithObservers: Boolean = false,
    val showEloToObservers: Boolean = false
)

/**
 * Motor de sincronização Firestore <-> Room. Ponte entre o estado local do app (Room, via
 * `VoleiViewModel`) e o documento em nuvem de um grupo premium sincronizado
 * (`cloudGroups/{cloudGroupId}`), usado por auxiliares/espectadores para ver o jogo em tempo real.
 *
 * Segue o mesmo padrão defensivo do [AuthManager]/[CloudFunctionsManager]: sem
 * `google-services.json` configurado (ou em testes de unidade, ver [isRunningInUnitTest]), todas
 * as chamadas abaixo falham de forma silenciosa (nunca derrubam o app) em vez de travar
 * esperando uma rede que não existe.
 *
 * Escopo desta primeira versão (`firestore-sync-engine`): o dispositivo do organizador (dono do
 * grupo local sincronizado, [com.bismarck.voleimanager.app.data.model.GroupConfig.remoteRole] nulo)
 * é quem publica o estado (fonte da verdade continua sendo o Room local dele); dispositivos que
 * entraram via código de Auxiliar/Espectador ([com.bismarck.voleimanager.app.data.model.GroupConfig.remoteRole]
 * não nulo) apenas observam. Permitir que um Auxiliar remoto também escreva de volta (edição
 * multi-dispositivo de verdade) fica para quando `role-permission-matrix` definir por completo a
 * matriz de permissões de escrita remota.
 */
object CloudSyncManager {
    private const val TAG = "CloudSyncManager"

    private fun firestoreOrNull(): FirebaseFirestore? {
        if (isRunningInUnitTest) return null
        return try {
            Firebase.firestore
        } catch (e: Exception) {
            Log.d(TAG, "Firestore indisponível: ${e.message}")
            null
        }
    }

    private fun groupDoc(firestore: FirebaseFirestore, cloudGroupId: String) =
        firestore.collection(CLOUD_GROUPS_COLLECTION).document(cloudGroupId)

    // ---------------------------------------------------------------------------------------
    // Live state (times em quadra, fila de espera, placar)
    // ---------------------------------------------------------------------------------------

    /** Publica o estado do jogo em andamento (melhor esforço, nunca lança/bloqueia por muito
     *  tempo). Chamado com alta frequência (a cada ponto/troca de time), por isso não é
     *  suspend nem aguarda a confirmação do servidor — só loga falhas. */
    fun pushLiveState(cloudGroupId: String, state: LiveGameState) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .collection(LIVE_STATE_COLLECTION).document(LIVE_STATE_DOC_ID)
            .set(state.toMap())
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao publicar liveState (best-effort): ${e.message}") }
    }

    /** Observa em tempo real o estado do jogo em andamento de [cloudGroupId]. Emite `null`
     *  enquanto não houver nenhum estado publicado ainda, ou se o Firestore estiver indisponível. */
    fun observeLiveState(cloudGroupId: String): Flow<LiveGameState?> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId)
            .collection(LIVE_STATE_COLLECTION).document(LIVE_STATE_DOC_ID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Falha ao observar liveState: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                val data = snapshot?.data
                if (data == null) {
                    trySend(null)
                } else {
                    trySend(
                        LiveGameState(
                            groupName = data["groupName"] as? String ?: "",
                            teamA = (data["teamA"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty(),
                            teamB = (data["teamB"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty(),
                            waitingList = (data["waitingList"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty(),
                            scoreA = (data["scoreA"] as? Number)?.toInt() ?: 0,
                            scoreB = (data["scoreB"] as? Number)?.toInt() ?: 0,
                            currentStreak = (data["currentStreak"] as? Number)?.toInt() ?: 0,
                            streakOwner = data["streakOwner"] as? String,
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    // ---------------------------------------------------------------------------------------
    // Histórico e Elo (visibilidade controlada por `observer-visibility-controls`)
    // ---------------------------------------------------------------------------------------

    /** Publica uma partida finalizada (melhor esforço). Chamado uma vez por partida — pode
     *  esperar a confirmação sem custo perceptível. */
    suspend fun pushHistoryEntry(cloudGroupId: String, entry: RemoteHistoryEntry) {
        val firestore = firestoreOrNull() ?: return
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION).add(
                    mapOf(
                        "date" to entry.date,
                        "teamA" to entry.teamA,
                        "teamB" to entry.teamB,
                        "winner" to entry.winner,
                        "teamAScore" to entry.teamAScore,
                        "teamBScore" to entry.teamBScore,
                        "endTimestamp" to entry.endTimestamp
                    )
                ).addOnCompleteListener { cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao publicar histórico (best-effort): ${e.message}")
        }
    }

    /** Publica uma entrada de Elo (melhor esforço), ver [pushHistoryEntry]. */
    suspend fun pushEloLogEntry(cloudGroupId: String, entry: RemoteEloLogEntry) {
        val firestore = firestoreOrNull() ?: return
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION).add(
                    mapOf(
                        "playerNameSnapshot" to entry.playerNameSnapshot,
                        "date" to entry.date,
                        "elo" to entry.elo,
                        "won" to entry.won
                    )
                ).addOnCompleteListener { cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao publicar log de elo (best-effort): ${e.message}")
        }
    }

    /** Observa as últimas partidas publicadas de [cloudGroupId] (mais recentes primeiro). Chamar
     *  apenas quando `visibility.shareHistoryWithObservers` estiver ligado — as security rules já
     *  bloqueiam a leitura do lado do servidor, mas evitamos a assinatura no client também. */
    fun observeHistory(cloudGroupId: String): Flow<List<RemoteHistoryEntry>> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION)
            .orderBy("endTimestamp", Query.Direction.DESCENDING)
            .limit(REMOTE_LIST_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Falha ao observar histórico remoto: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snapshot?.documents.orEmpty().map { doc ->
                        RemoteHistoryEntry(
                            id = doc.id,
                            date = doc.getString("date") ?: "",
                            teamA = doc.getString("teamA") ?: "",
                            teamB = doc.getString("teamB") ?: "",
                            winner = doc.getString("winner") ?: "",
                            teamAScore = (doc.get("teamAScore") as? Number)?.toInt(),
                            teamBScore = (doc.get("teamBScore") as? Number)?.toInt(),
                            endTimestamp = (doc.get("endTimestamp") as? Number)?.toLong()
                        )
                    }
                )
            }
        awaitClose { registration.remove() }
    }

    /** Observa os últimos registros de Elo publicados de [cloudGroupId]. Ver [observeHistory]. */
    fun observeEloLogs(cloudGroupId: String): Flow<List<RemoteEloLogEntry>> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION)
            .limit(REMOTE_LIST_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Falha ao observar elo remoto: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snapshot?.documents.orEmpty().map { doc ->
                        RemoteEloLogEntry(
                            playerNameSnapshot = doc.getString("playerNameSnapshot") ?: "",
                            date = doc.getString("date") ?: "",
                            elo = doc.getDouble("elo") ?: 1200.0,
                            won = doc.getBoolean("won") ?: false
                        )
                    }
                )
            }
        awaitClose { registration.remove() }
    }

    // ---------------------------------------------------------------------------------------
    // Visibilidade (observer-visibility-controls)
    // ---------------------------------------------------------------------------------------

    /** Observa em tempo real os toggles de visibilidade de [cloudGroupId], para refletir em todos
     *  os dispositivos (organizador/auxiliar/espectador) assim que qualquer um deles mudar. */
    fun observeGroupVisibility(cloudGroupId: String): Flow<GroupVisibility?> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Falha ao observar visibilidade: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                val visibility = snapshot?.get(FIELD_VISIBILITY) as? Map<*, *>
                if (visibility == null) {
                    trySend(null)
                } else {
                    trySend(
                        GroupVisibility(
                            shareHistoryWithObservers = visibility[FIELD_SHARE_HISTORY] as? Boolean ?: false,
                            showEloToObservers = visibility[FIELD_SHOW_ELO] as? Boolean ?: false
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    /** Atualiza os toggles de visibilidade de [cloudGroupId] (permitido a organizador/auxiliar,
     *  ver `onlyTouchesClientEditableFields()` em firestore.rules — `visibility` não é um campo
     *  sensível). Retorna uma mensagem de erro amigável em caso de falha, ou `null` em sucesso. */
    suspend fun setGroupVisibility(cloudGroupId: String, shareHistory: Boolean, showElo: Boolean): String? {
        val firestore = firestoreOrNull() ?: return null
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                groupDoc(firestore, cloudGroupId)
                    .set(
                        mapOf(FIELD_VISIBILITY to mapOf(FIELD_SHARE_HISTORY to shareHistory, FIELD_SHOW_ELO to showElo)),
                        SetOptions.merge()
                    )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(Unit))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao salvar visibilidade")))
                        }
                    }
            }.getOrThrow()
            null
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao salvar visibilidade: ${e.message}")
            e.message ?: "Não foi possível salvar a visibilidade agora."
        }
    }
}

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
private const val FIELD_SHARE_ONLY_TODAY = "shareOnlyTodayHistory"
private const val FIELD_GROUP_TYPE = "groupType"
private const val FIELD_BALANCING_MODE = "balancingMode"
private const val FIELD_TEAM_SIZE = "teamSize"
private const val FIELD_IS_ACTIVE = "isActive"
private const val FIELD_TEAM_A_COLOR = "teamAColorName"
private const val FIELD_TEAM_B_COLOR = "teamBColorName"
private const val FIELD_SPECTATOR_CODE = "spectatorCode"
private const val FIELD_ACTIVE_ADMIN_DEVICE_ID = "activeAdminDeviceId"
private const val FIELD_ACTIVE_ADMIN_SINCE = "activeAdminSince"
/** `history-remote-list-limit`: antes fixado em 100, o que truncava silenciosamente grupos com
 *  mais de 100 partidas publicadas (ex.: um grupo com meses de jogo facilmente passa de 300-400
 *  partidas e milhares de logs de Elo) — Espectador/Auxiliar só enxergavam as ~100 partidas mais
 *  recentes por `endTimestamp`, dando a falsa impressão de que a sincronização do histórico antigo
 *  tinha "parado no meio". `eloLogs` cresce ~teamSize vezes mais rápido que `history` (um log por
 *  jogador por partida), daí o limite bem maior. Ainda são limites, não leitura infinita, para
 *  colocar algum teto no custo de uma única assinatura do Firestore.
 */
private const val REMOTE_HISTORY_LIST_LIMIT = 3000L
/** O Firestore rejeita a consulta inteira (`INVALID_ARGUMENT: Limit value ... over the maximum
 *  value of 10000`) se `.limit()` passar de 10 mil — não é só "sem efeito", a query inteira falha
 *  e o listener nunca entrega nada (nem erro visível na UI), zerando silenciosamente jogos/
 *  vitórias/aproveitamento de todo mundo. Fica no teto real do Firestore, nunca acima dele. */
private const val REMOTE_ELO_LOGS_LIST_LIMIT = 10000L

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
object CloudSyncManager : com.bismarck.voleimanager.app.platform.CloudSyncService {
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
    override fun pushLiveState(cloudGroupId: String, state: LiveGameState) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .collection(LIVE_STATE_COLLECTION).document(LIVE_STATE_DOC_ID)
            .set(state.toMap())
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao publicar liveState (best-effort): ${e.message}") }
    }

    /** Observa em tempo real o estado do jogo em andamento de [cloudGroupId]. Emite `null`
     *  enquanto não houver nenhum estado publicado ainda, ou se o Firestore estiver indisponível. */
    override fun observeLiveState(cloudGroupId: String): Flow<LiveGameState?> = callbackFlow {
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
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L,
                            pendingFinishWinner = data["pendingFinishWinner"] as? String,
                            pendingFinishRequestId = data["pendingFinishRequestId"] as? String,
                            presentPlayers = (data["presentPlayers"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty(),
                            allPlayers = (data["allPlayers"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty(),
                            matchStartTimestamp = (data["matchStartTimestamp"] as? Number)?.toLong(),
                            lastScoringTeam = data["lastScoringTeam"] as? String,
                            rotationRequiredForTeam = data["rotationRequiredForTeam"] as? String,
                            pendingPresenceTogglePublicId = data["pendingPresenceTogglePublicId"] as? String,
                            pendingPresenceToggleRequestId = data["pendingPresenceToggleRequestId"] as? String,
                            writerSessionId = data["writerSessionId"] as? String,
                            gamesPlayedToday = (data["gamesPlayedToday"] as? Map<*, *>)?.entries
                                ?.mapNotNull { (k, v) -> (k as? String)?.let { key -> key to ((v as? Number)?.toInt() ?: 0) } }
                                ?.toMap().orEmpty(),
                            assignedPositions = (data["assignedPositions"] as? Map<*, *>)?.entries
                                ?.mapNotNull { (k, v) -> (k as? String)?.let { key -> key to (v as? String ?: return@let null) } }
                                ?.toMap().orEmpty(),
                            assignedSlotIndices = (data["assignedSlotIndices"] as? Map<*, *>)?.entries
                                ?.mapNotNull { (k, v) -> (k as? String)?.let { key -> key to ((v as? Number)?.toInt() ?: return@let null) } }
                                ?.toMap().orEmpty(),
                            compositionIncomplete = data["compositionIncomplete"] as? Boolean ?: false,
                            hasPreviousMatch = data["hasPreviousMatch"] as? Boolean ?: false,
                            lastWinners = (data["lastWinners"] as? List<*>)?.mapNotNull { (it as? Map<*, *>)?.let(RemotePlayerSnapshot::fromMap) }.orEmpty()
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
     *  esperar a confirmação sem custo perceptível. Retorna o id do documento criado (nulo se a
     *  publicação falhar), guardado temporariamente para permitir apagar o registro remoto caso
     *  a partida seja desfeita em seguida (`undo-last-match`). */
    override suspend fun pushHistoryEntry(cloudGroupId: String, entry: RemoteHistoryEntry): String? {
        val firestore = firestoreOrNull() ?: return null
        return try {
            suspendCancellableCoroutine { cont ->
                groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION).add(
                    mapOf(
                        "date" to entry.date,
                        "teamA" to entry.teamA,
                        "teamB" to entry.teamB,
                        "winner" to entry.winner,
                        "teamAScore" to entry.teamAScore,
                        "teamBScore" to entry.teamBScore,
                        "endTimestamp" to entry.endTimestamp,
                        "startTimestamp" to entry.startTimestamp,
                        "eloPoints" to entry.eloPoints,
                        "teamAAverageElo" to entry.teamAAverageElo,
                        "teamBAverageElo" to entry.teamBAverageElo
                    )
                ).addOnCompleteListener { task ->
                    cont.resume(if (task.isSuccessful) task.result?.id else null)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao publicar histórico (best-effort): ${e.message}")
            null
        }
    }

    /** Apaga uma entrada de histórico publicada anteriormente (melhor esforço;
     *  `undo-last-match`). */
    override suspend fun deleteHistoryEntry(cloudGroupId: String, docId: String) {
        val firestore = firestoreOrNull() ?: return
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION).document(docId)
                    .delete()
                    .addOnCompleteListener { cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao apagar histórico remoto (best-effort): ${e.message}")
        }
    }

    /** Publica uma entrada de Elo (melhor esforço), ver [pushHistoryEntry]. Retorna o id do
     *  documento criado (nulo se a publicação falhar). */
    override suspend fun pushEloLogEntry(cloudGroupId: String, entry: RemoteEloLogEntry): String? {
        val firestore = firestoreOrNull() ?: return null
        return try {
            suspendCancellableCoroutine { cont ->
                groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION).add(
                    mapOf(
                        "playerNameSnapshot" to entry.playerNameSnapshot,
                        "date" to entry.date,
                        "elo" to entry.elo,
                        "won" to entry.won,
                        "endTimestamp" to entry.endTimestamp
                    )
                ).addOnCompleteListener { task ->
                    cont.resume(if (task.isSuccessful) task.result?.id else null)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao publicar log de elo (best-effort): ${e.message}")
            null
        }
    }

    /** Apaga, em lote, logs de elo publicados anteriormente (melhor esforço;
     *  `undo-last-match`). */
    override suspend fun deleteEloLogEntries(cloudGroupId: String, docIds: List<String>) {
        if (docIds.isEmpty()) return
        val firestore = firestoreOrNull() ?: return
        val collection = groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION)
        try {
            docIds.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                suspendCancellableCoroutine<Unit> { cont ->
                    val batch = firestore.batch()
                    chunk.forEach { docId -> batch.delete(collection.document(docId)) }
                    batch.commit().addOnCompleteListener { cont.resume(Unit) }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao apagar logs de elo remotos (best-effort): ${e.message}")
        }
    }

    /** Máximo de operações por `WriteBatch` do Firestore (limite real é 500; deixamos folga). */
    private const val BATCH_CHUNK_SIZE = 450

    /**
     * `backfill-batched-writes`: publica todo o histórico pré-existente de uma vez só (em lotes de
     * até [BATCH_CHUNK_SIZE] partidas por `WriteBatch.commit()`), em vez de uma escrita
     * `.add()` sequencial por partida. Um grupo com algumas centenas/milhares de partidas levava
     * minutos inteiros no modo sequencial antigo (um round-trip de rede por documento) — tempo
     * suficiente para o processo ser encerrado pelo Android ou o usuário sair da tela antes do
     * fim, deixando o histórico "pela metade" sem nenhum aviso (a mesma falha silenciosa que
     * [history-backfill] tenta evitar). Cada lote é uma chamada de rede só, então algumas
     * centenas de partidas viram poucas chamadas em vez de centenas. Retorna `true` só se todos
     * os lotes confirmarem com sucesso — se algum falhar, quem chama sabe que o backfill ficou
     * incompleto e não deve marcar [com.bismarck.voleimanager.app.data.model.GroupConfig.historyBackfilledAt].
     */
    override suspend fun pushHistoryEntriesBatched(cloudGroupId: String, entries: List<RemoteHistoryEntry>): Boolean {
        if (entries.isEmpty()) return true
        val firestore = firestoreOrNull() ?: return false
        val collection = groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION)
        return entries.chunked(BATCH_CHUNK_SIZE).all { chunk ->
            try {
                suspendCancellableCoroutine<Boolean> { cont ->
                    val batch = firestore.batch()
                    chunk.forEach { entry ->
                        batch.set(
                            collection.document(),
                            mapOf(
                                "date" to entry.date,
                                "teamA" to entry.teamA,
                                "teamB" to entry.teamB,
                                "winner" to entry.winner,
                                "teamAScore" to entry.teamAScore,
                                "teamBScore" to entry.teamBScore,
                                "endTimestamp" to entry.endTimestamp,
                                "startTimestamp" to entry.startTimestamp,
                                "eloPoints" to entry.eloPoints,
                                "teamAAverageElo" to entry.teamAAverageElo,
                                "teamBAverageElo" to entry.teamBAverageElo
                            )
                        )
                    }
                    batch.commit()
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "Falha ao publicar lote de histórico (backfill): ${e.message}")
                            cont.resume(false)
                        }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Falha ao publicar lote de histórico (backfill): ${e.message}")
                false
            }
        }
    }

    /** Publica todos os logs de Elo pré-existentes em lotes — ver [pushHistoryEntriesBatched]. */
    override suspend fun pushEloLogEntriesBatched(cloudGroupId: String, entries: List<RemoteEloLogEntry>): Boolean {
        if (entries.isEmpty()) return true
        val firestore = firestoreOrNull() ?: return false
        val collection = groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION)
        return entries.chunked(BATCH_CHUNK_SIZE).all { chunk ->
            try {
                suspendCancellableCoroutine<Boolean> { cont ->
                    val batch = firestore.batch()
                    chunk.forEach { entry ->
                        batch.set(
                            collection.document(),
                            mapOf(
                                "playerNameSnapshot" to entry.playerNameSnapshot,
                                "date" to entry.date,
                                "elo" to entry.elo,
                                "won" to entry.won,
                                "endTimestamp" to entry.endTimestamp
                            )
                        )
                    }
                    batch.commit()
                        .addOnSuccessListener { cont.resume(true) }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "Falha ao publicar lote de elo (backfill): ${e.message}")
                            cont.resume(false)
                        }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Falha ao publicar lote de elo (backfill): ${e.message}")
                false
            }
        }
    }

    /** Observa as últimas partidas publicadas de [cloudGroupId] (mais recentes primeiro). Chamar
     *  apenas quando `visibility.shareHistoryWithObservers` estiver ligado — as security rules já
     *  bloqueiam a leitura do lado do servidor, mas evitamos a assinatura no client também. */
    override fun observeHistory(cloudGroupId: String): Flow<List<RemoteHistoryEntry>> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId).collection(HISTORY_COLLECTION)
            .orderBy("endTimestamp", Query.Direction.DESCENDING)
            .limit(REMOTE_HISTORY_LIST_LIMIT)
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
                            endTimestamp = (doc.get("endTimestamp") as? Number)?.toLong(),
                            startTimestamp = (doc.get("startTimestamp") as? Number)?.toLong(),
                            eloPoints = (doc.get("eloPoints") as? Number)?.toDouble() ?: 0.0,
                            teamAAverageElo = (doc.get("teamAAverageElo") as? Number)?.toDouble(),
                            teamBAverageElo = (doc.get("teamBAverageElo") as? Number)?.toDouble()
                        )
                    }
                )
            }
        awaitClose { registration.remove() }
    }

    /** Observa os últimos registros de Elo publicados de [cloudGroupId]. Ver [observeHistory]. */
    override fun observeEloLogs(cloudGroupId: String): Flow<List<RemoteEloLogEntry>> = callbackFlow {
        val firestore = firestoreOrNull()
        if (firestore == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = groupDoc(firestore, cloudGroupId).collection(ELO_LOGS_COLLECTION)
            .orderBy("endTimestamp", Query.Direction.DESCENDING)
            .limit(REMOTE_ELO_LOGS_LIST_LIMIT)
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
                            won = doc.getBoolean("won") ?: false,
                            endTimestamp = (doc.get("endTimestamp") as? Number)?.toLong()
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
    override fun observeGroupVisibility(cloudGroupId: String): Flow<GroupVisibility?> = callbackFlow {
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
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        // Só acontece quando o documento de membro deste usuário foi apagado
                        // (organizador regenerou o código de Espectador) — ver [accessRevoked].
                        trySend(GroupVisibility(isActive = false, accessRevoked = true))
                    } else {
                        trySend(null)
                    }
                    return@addSnapshotListener
                }
                val visibility = snapshot?.get(FIELD_VISIBILITY) as? Map<*, *>
                val groupType = snapshot?.getString(FIELD_GROUP_TYPE)
                val balancingMode = snapshot?.getString(FIELD_BALANCING_MODE)
                val teamSize = (snapshot?.get(FIELD_TEAM_SIZE) as? Number)?.toInt()
                val teamAColorName = snapshot?.getString(FIELD_TEAM_A_COLOR)
                val teamBColorName = snapshot?.getString(FIELD_TEAM_B_COLOR)
                val isActive = snapshot?.get(FIELD_IS_ACTIVE) as? Boolean
                val spectatorCode = snapshot?.getString(FIELD_SPECTATOR_CODE)
                val activeAdminDeviceId = snapshot?.getString(FIELD_ACTIVE_ADMIN_DEVICE_ID)
                val activeAdminSince = (snapshot?.get(FIELD_ACTIVE_ADMIN_SINCE) as? Number)?.toLong()
                if (visibility == null && groupType == null && balancingMode == null && teamSize == null &&
                    teamAColorName == null && teamBColorName == null && isActive == null && spectatorCode == null &&
                    activeAdminDeviceId == null
                ) {
                    trySend(null)
                } else {
                    trySend(
                        GroupVisibility(
                            shareHistoryWithObservers = visibility?.get(FIELD_SHARE_HISTORY) as? Boolean ?: false,
                            showEloToObservers = visibility?.get(FIELD_SHOW_ELO) as? Boolean ?: false,
                            shareOnlyTodayHistory = visibility?.get(FIELD_SHARE_ONLY_TODAY) as? Boolean ?: false,
                            groupType = groupType,
                            balancingMode = balancingMode,
                            teamSize = teamSize,
                            teamAColorName = teamAColorName,
                            teamBColorName = teamBColorName,
                            isActive = isActive ?: true,
                            spectatorCode = spectatorCode,
                            activeAdminDeviceId = activeAdminDeviceId,
                            activeAdminSince = activeAdminSince
                        )
                    )
                }
            }
        awaitClose { registration.remove() }
    }

    /** Atualiza os toggles de visibilidade de [cloudGroupId] (permitido a organizador/auxiliar,
     *  ver `onlyTouchesClientEditableFields()` em firestore.rules — `visibility` não é um campo
     *  sensível). Retorna uma mensagem de erro amigável em caso de falha, ou `null` em sucesso. */
    override suspend fun setGroupVisibility(
        cloudGroupId: String,
        shareHistory: Boolean,
        showElo: Boolean,
        shareOnlyToday: Boolean
    ): String? {
        val firestore = firestoreOrNull() ?: return null
        return try {
            suspendCancellableCoroutine<Result<Unit>> { cont ->
                groupDoc(firestore, cloudGroupId)
                    .set(
                        mapOf(
                            FIELD_VISIBILITY to mapOf(
                                FIELD_SHARE_HISTORY to shareHistory,
                                FIELD_SHOW_ELO to showElo,
                                FIELD_SHARE_ONLY_TODAY to shareOnlyToday
                            )
                        ),
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

    /** Liga/desliga a flag `isActive` do documento raiz (melhor esforço), refletindo
     *  [com.bismarck.voleimanager.app.data.model.GroupConfig.isCloudSynced] do organizador —
     *  ver [GroupVisibility.isActive]. Nunca apaga o documento nem o código de convite, então a
     *  sincronização volta a funcionar normalmente assim que o organizador reativar. */
    override fun setGroupActiveState(cloudGroupId: String, isActive: Boolean) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .set(mapOf(FIELD_IS_ACTIVE to isActive), SetOptions.merge())
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao publicar isActive (best-effort): ${e.message}") }
    }

    /** Reivindica/transfere a sessão de administrador de [cloudGroupId] para [deviceId] (melhor
     *  esforço) — ver `admin-session-transfer`. Chamado tanto na primeira ativação da
     *  sincronização de um grupo (auto-reivindicação, sem sobrescrever se outro aparelho já for o
     *  dono) quanto na transferência explícita pelo usuário (sempre sobrescreve, ver
     *  [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.transferAdminSession]). */
    override fun setActiveAdminDevice(cloudGroupId: String, deviceId: String, since: Long) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .set(
                mapOf(FIELD_ACTIVE_ADMIN_DEVICE_ID to deviceId, FIELD_ACTIVE_ADMIN_SINCE to since),
                SetOptions.merge()
            )
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao reivindicar sessão de administrador (best-effort): ${e.message}") }
    }

    /** Publica os metadados de cabeçalho do grupo (tipo, modo de balanceamento, tamanho de time)
     *  de [cloudGroupId] (melhor esforço, permitido a organizador/auxiliar — nenhum destes campos
     *  é sensível em `onlyTouchesClientEditableFields()` em firestore.rules). Usado para manter os
     *  ícones do cabeçalho sincronizados entre organizador/auxiliar/espectador. */
    override fun setGroupMeta(cloudGroupId: String, groupType: String, balancingMode: String, teamSize: Int) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .set(
                mapOf(
                    FIELD_GROUP_TYPE to groupType,
                    FIELD_BALANCING_MODE to balancingMode,
                    FIELD_TEAM_SIZE to teamSize
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao publicar metadados do grupo (best-effort): ${e.message}") }
    }

    /** Publica as cores oficiais de Time A/Time B de [cloudGroupId] (melhor esforço), definidas
     *  pelo organizador via [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.setGroupTeamColors]
     *  — usado para que Auxiliar/Espectador vejam a mesma personalização de cores do organizador. */
    override fun setGroupTeamColors(cloudGroupId: String, teamAColorName: String, teamBColorName: String) {
        val firestore = firestoreOrNull() ?: return
        groupDoc(firestore, cloudGroupId)
            .set(
                mapOf(
                    FIELD_TEAM_A_COLOR to teamAColorName,
                    FIELD_TEAM_B_COLOR to teamBColorName
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { e -> Log.d(TAG, "Falha ao publicar cores do grupo (best-effort): ${e.message}") }
    }
}

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

/** Jogador "enxuto" sincronizado em `liveState` — usa [publicId] (estável entre dispositivos) em
 *  vez do id local autoGenerate do Room, que não tem significado fora do aparelho de origem.
 *  Inclui os campos exibidos nos cards da tela "Jogo" (fora tolerância/posição do dia, que não
 *  fazem sentido fora do aparelho que calcula presença localmente). */
data class RemotePlayerSnapshot(
    val publicId: String = "",
    val name: String = "",
    val elo: Double = 1200.0,
    val isPriority: Boolean = false,
    val matchesPlayed: Int = 0,
    val victories: Int = 0,
    val preferredPosition: String? = null,
    val secondaryPosition: String? = null,
    /** Tolerância de atraso (pedágio) e a data em que foi calculada, já resolvidas pelo
     *  organizador — só o valor final é replicado (o cálculo em si continua acontecendo apenas
     *  no aparelho que controla a presença localmente), para que Auxiliar/Espectador também
     *  vejam o mesmo badge de pedágio que o organizador. */
    val dailyToll: Int = 0,
    val tollDate: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "publicId" to publicId,
        "name" to name,
        "elo" to elo,
        "isPriority" to isPriority,
        "matchesPlayed" to matchesPlayed,
        "victories" to victories,
        "preferredPosition" to preferredPosition,
        "secondaryPosition" to secondaryPosition,
        "dailyToll" to dailyToll,
        "tollDate" to tollDate
    )

    companion object {
        fun fromMap(map: Map<*, *>): RemotePlayerSnapshot = RemotePlayerSnapshot(
            publicId = map["publicId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            elo = (map["elo"] as? Number)?.toDouble() ?: 1200.0,
            isPriority = map["isPriority"] as? Boolean ?: false,
            matchesPlayed = (map["matchesPlayed"] as? Number)?.toInt() ?: 0,
            victories = (map["victories"] as? Number)?.toInt() ?: 0,
            preferredPosition = map["preferredPosition"] as? String,
            secondaryPosition = map["secondaryPosition"] as? String,
            dailyToll = (map["dailyToll"] as? Number)?.toInt() ?: 0,
            tollDate = map["tollDate"] as? String ?: ""
        )
    }
}

/** Espelha o jogo em andamento de um grupo premium sincronizado: times em quadra, fila de espera
 *  e placar — é isto que um(a) Espectador(a) (ou Auxiliar) vê em tempo real na tela "Jogo (Ao
 *  vivo)". Também carrega o canal de comando usado por um(a) Auxiliar para pedir que o
 *  organizador finalize a partida em andamento (só o dispositivo organizador tem os registros
 *  locais de jogador no Room para calcular Elo/histórico de verdade, ver
 *  [pendingFinishWinner]/[pendingFinishRequestId] e `role-permission-matrix`). */
data class LiveGameState(
    val groupName: String = "",
    val teamA: List<RemotePlayerSnapshot> = emptyList(),
    val teamB: List<RemotePlayerSnapshot> = emptyList(),
    val waitingList: List<RemotePlayerSnapshot> = emptyList(),
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val currentStreak: Int = 0,
    val streakOwner: String? = null,
    val updatedAt: Long = 0L,
    /** Time ("A"/"B") que um Auxiliar pediu para vencer a partida em andamento; só o
     *  organizador processa este campo (chamando sua lógica real de fim de partida) e volta a
     *  publicá-lo como `null` assim que atender o pedido. */
    val pendingFinishWinner: String? = null,
    /** Identificador único do pedido acima, usado pelo organizador para não processar o mesmo
     *  pedido duas vezes caso receba a mesma atualização mais de uma vez. */
    val pendingFinishRequestId: String? = null,
    /** Jogadores marcados como presentes/selecionados no aparelho do organizador *antes* de uma
     *  partida começar (ainda sem times formados). Publicado só pelo organizador (único
     *  dispositivo com o roster real do grupo no Room); Auxiliar apenas ecoa o último valor
     *  recebido ao publicar suas próprias atualizações, para não apagar essa lista sem querer.
     *  Usado pelo Espectador para ver "quem está presente agora" antes do jogo começar. */
    val presentPlayers: List<RemotePlayerSnapshot> = emptyList(),
    /** Lista completa do elenco do grupo (sem filtro de presença), publicada só pelo organizador
     *  (único com o roster real no Room) e ecoada por um Auxiliar remoto — usada para o Auxiliar
     *  enxergar a lista de jogadores completa, sem as restrições de edição do Espectador. */
    val allPlayers: List<RemotePlayerSnapshot> = emptyList(),
    /** Instante (epoch millis) em que a partida em andamento começou, usado para calcular tempo de
     *  partida/tempo médio de duração em todos os papéis. */
    val matchStartTimestamp: Long? = null,
    /** Time ("A"/"B") que fez o ponto mais recente, para o indicador visual no placar. */
    val lastScoringTeam: String? = null,
    /** Time ("A"/"B") que deve fazer rodízio, para o indicador visual no placar. */
    val rotationRequiredForTeam: String? = null,
    /** publicId do jogador cuja presença um Auxiliar remoto pediu para alternar; só o organizador
     *  processa este campo (aplicando sua própria lógica local de presença/fila) e volta a
     *  publicá-lo como `null` assim que atender o pedido — mesmo padrão de
     *  [pendingFinishWinner]/[pendingFinishRequestId]. */
    val pendingPresenceTogglePublicId: String? = null,
    /** Identificador único do pedido acima, ver [pendingFinishRequestId]. */
    val pendingPresenceToggleRequestId: String? = null,
    /** Identificador único de sessão do dispositivo que publicou esta atualização (gerado uma vez
     *  por processo/instalação da ViewModel). Usado para detectar auto-eco de forma confiável —
     *  ao contrário de comparar [updatedAt] (relógio do aparelho, sujeito a variação entre
     *  dispositivos), cada dispositivo só ignora atualizações que ele mesmo publicou, nunca as de
     *  outro aparelho, mesmo que os relógios estejam dessincronizados (ver `fix-admin-aux-sync-races`). */
    val writerSessionId: String? = null,
    /** Quantidade de partidas já disputadas hoje por cada jogador (chave = publicId), calculada
     *  pelo organizador a partir dos registros locais de Elo/histórico — replicada para que
     *  Auxiliar/Espectador vejam o mesmo indicador "jogos hoje"/ordem de fila que o organizador. */
    val gamesPlayedToday: Map<String, Int> = emptyMap(),
    /** Posição atribuída a cada jogador na formação em quadra (chave = publicId, valor = nome do
     *  enum [com.bismarck.voleimanager.app.data.model.PlayerPosition]) — só relevante para grupos
     *  `FIXED_POSITIONS`. Calculada só pelo organizador (dono do algoritmo [util.PositionAssigner])
     *  e replicada para Auxiliar/Espectador exibirem os mesmos selos de posição. */
    val assignedPositions: Map<String, String> = emptyMap(),
    /** Índice do slot de composição atribuído a cada jogador (chave = publicId), ver [assignedPositions]. */
    val assignedSlotIndices: Map<String, Int> = emptyMap(),
    /** Se a composição do time está incompleta (algum slot preenchido abaixo do nível
     *  secundário) — replicado para o aviso "composição incompleta" também aparecer para
     *  Auxiliar/Espectador. */
    val compositionIncomplete: Boolean = false,
    /** Se a última partida deste grupo já terminou (times zerados aguardando a próxima rodada) —
     *  replicado para que o banner de "time vencedor" (`EmptyStateCard`) apareça também para
     *  Auxiliar/Espectador, que não têm essa informação calculada localmente. */
    val hasPreviousMatch: Boolean = false,
    /** Jogadores do time vencedor da última partida encerrada, ver [hasPreviousMatch]. */
    val lastWinners: List<RemotePlayerSnapshot> = emptyList()
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
        "updatedAt" to updatedAt,
        "pendingFinishWinner" to pendingFinishWinner,
        "pendingFinishRequestId" to pendingFinishRequestId,
        "presentPlayers" to presentPlayers.map { it.toMap() },
        "allPlayers" to allPlayers.map { it.toMap() },
        "matchStartTimestamp" to matchStartTimestamp,
        "lastScoringTeam" to lastScoringTeam,
        "rotationRequiredForTeam" to rotationRequiredForTeam,
        "pendingPresenceTogglePublicId" to pendingPresenceTogglePublicId,
        "pendingPresenceToggleRequestId" to pendingPresenceToggleRequestId,
        "writerSessionId" to writerSessionId,
        "gamesPlayedToday" to gamesPlayedToday,
        "assignedPositions" to assignedPositions,
        "assignedSlotIndices" to assignedSlotIndices,
        "compositionIncomplete" to compositionIncomplete,
        "hasPreviousMatch" to hasPreviousMatch,
        "lastWinners" to lastWinners.map { it.toMap() }
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
    val endTimestamp: Long? = null,
    val startTimestamp: Long? = null,
    val eloPoints: Double = 0.0,
    val teamAAverageElo: Double? = null,
    val teamBAverageElo: Double? = null
)

/** Entrada de Elo "enxuta" publicada em `cloudGroups/{id}/eloLogs` — só visível a observadores
 *  quando `visibility.shareHistoryWithObservers` E `visibility.showEloToObservers` estiverem
 *  ligados (ver firestore.rules). */
data class RemoteEloLogEntry(
    val playerNameSnapshot: String = "",
    val date: String = "",
    val elo: Double = 1200.0,
    val won: Boolean = false,
    /** Timestamp de fim da partida que gerou este registro — usado só para ordenar/descobrir o
     *  Elo "mais recente" de cada jogador ao reconstruir o histórico no aparelho remoto, já que a
     *  consulta do Firestore não garante ordem de inserção. */
    val endTimestamp: Long? = null
)

/** Toggles de visibilidade do grupo para observadores, espelhados de `cloudGroups/{id}.visibility`. */
data class GroupVisibility(
    val shareHistoryWithObservers: Boolean = false,
    val showEloToObservers: Boolean = false,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.shareOnlyTodayHistory]. */
    val shareOnlyTodayHistory: Boolean = false,
    /** Metadados do grupo espelhados no documento raiz (fora do mapa `visibility`) para que
     *  ícones de tipo/balanceamento/tamanho de time no cabeçalho fiquem sincronizados entre
     *  organizador, auxiliar e espectador — ver `GroupConfig.groupType`/`balancingMode`/`teamSize`. */
    val groupType: String? = null,
    val balancingMode: String? = null,
    val teamSize: Int? = null,
    /** Cores oficiais do Time A/Time B definidas pelo organizador ([TeamAccentColor.name]),
     *  espelhadas aqui para que Espectadores enxerguem a mesma escolha de cores do organizador
     *  (ver [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.setGroupTeamColors]).
     *  `null` enquanto o organizador nunca personalizou as cores (usa o padrão azul/amarelo). */
    val teamAColorName: String? = null,
    val teamBColorName: String? = null,
    /** Espelha se o organizador ainda mantém a sincronização em nuvem deste grupo ligada
     *  ([com.bismarck.voleimanager.app.data.model.GroupConfig.isCloudSynced]). Quando o
     *  organizador desliga a sincronização, o código de convite continua válido (o documento
     *  `cloudGroups/{id}` não é apagado), mas `isActive=false` faz Auxiliar/Espectador ocultarem
     *  os dados imediatamente até o grupo voltar a ser sincronizado. Padrão `true` para grupos
     *  antigos que nunca tinham esse campo (nunca foram desativados). */
    val isActive: Boolean = true,
    /** Código permanente de convite de Espectador (`spectatorCode`), sempre visível na tela
     *  Premium para o organizador/auxiliar compartilhar (texto simples/QR) — ver
     *  `spectator-code-client`. `null` até o backend gerá-lo na primeira ativação do grupo
     *  ([ensureSpectatorCode] em `volei_manager_backend`). */
    val spectatorCode: String? = null,
    /** `true` só quando o listener deste documento falhou com PERMISSION_DENIED — ou seja, este
     *  dispositivo (Espectador) teve seu acesso revogado (organizador tocou em "Atualizar
     *  código", ver `regenerateSpectatorCode`). Distinto de `isActive=false`: aqui o grupo pode
     *  continuar ativo, só este dispositivo específico que perdeu o acesso e precisa reentrar com
     *  o novo código. */
    val accessRevoked: Boolean = false,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.activeAdminDeviceId] —
     *  `null` = nenhum aparelho reivindicou a sessão ainda (nenhuma restrição). */
    val activeAdminDeviceId: String? = null,
    /** Ver [com.bismarck.voleimanager.app.data.model.GroupConfig.activeAdminSince]. */
    val activeAdminSince: Long? = null
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
                        "endTimestamp" to entry.endTimestamp,
                        "startTimestamp" to entry.startTimestamp,
                        "eloPoints" to entry.eloPoints,
                        "teamAAverageElo" to entry.teamAAverageElo,
                        "teamBAverageElo" to entry.teamBAverageElo
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
                        "won" to entry.won,
                        "endTimestamp" to entry.endTimestamp
                    )
                ).addOnCompleteListener { cont.resume(Unit) }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao publicar log de elo (best-effort): ${e.message}")
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
    suspend fun pushHistoryEntriesBatched(cloudGroupId: String, entries: List<RemoteHistoryEntry>): Boolean {
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
    suspend fun pushEloLogEntriesBatched(cloudGroupId: String, entries: List<RemoteEloLogEntry>): Boolean {
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
    fun observeHistory(cloudGroupId: String): Flow<List<RemoteHistoryEntry>> = callbackFlow {
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
    fun observeEloLogs(cloudGroupId: String): Flow<List<RemoteEloLogEntry>> = callbackFlow {
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
    suspend fun setGroupVisibility(
        cloudGroupId: String,
        shareHistory: Boolean,
        showElo: Boolean,
        shareOnlyToday: Boolean = false
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
    fun setGroupActiveState(cloudGroupId: String, isActive: Boolean) {
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
    fun setActiveAdminDevice(cloudGroupId: String, deviceId: String, since: Long = System.currentTimeMillis()) {
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
    fun setGroupMeta(cloudGroupId: String, groupType: String, balancingMode: String, teamSize: Int) {
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
    fun setGroupTeamColors(cloudGroupId: String, teamAColorName: String, teamBColorName: String) {
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

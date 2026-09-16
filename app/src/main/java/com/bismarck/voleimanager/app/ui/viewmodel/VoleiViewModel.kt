package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.viewModelScope
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.GroupLog
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_BALANCING_MODE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_NAME
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_TYPE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_MIN_PLAYERS
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_TEAM_SIZE
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.TournamentMatch
import com.bismarck.voleimanager.app.data.model.TournamentTeam
import com.bismarck.voleimanager.app.data.model.TournamentTeamMember
import com.bismarck.voleimanager.app.util.AppAuthUser
import com.bismarck.voleimanager.app.util.AuthManager
import com.bismarck.voleimanager.app.util.BillingManager
import com.bismarck.voleimanager.app.util.BillingProductIds
import com.bismarck.voleimanager.app.util.CloudFunctionsManager
import com.bismarck.voleimanager.app.util.CloudSyncManager
import com.bismarck.voleimanager.app.util.EloCalculator
import com.bismarck.voleimanager.app.util.GeneratedJoinCode
import com.bismarck.voleimanager.app.util.GoogleSignInHelper
import com.bismarck.voleimanager.app.util.GroupVisibility
import com.bismarck.voleimanager.app.util.JoinRole
import com.bismarck.voleimanager.app.util.LiveGameState
import com.bismarck.voleimanager.app.util.PositionAssigner
import com.bismarck.voleimanager.app.util.RemoteEloLogEntry
import com.bismarck.voleimanager.app.util.RemoteHistoryEntry
import com.bismarck.voleimanager.app.util.RemotePlayerSnapshot
import com.bismarck.voleimanager.app.util.SubscriptionOffer
import com.bismarck.voleimanager.app.util.TeamBalancer
import com.bismarck.voleimanager.app.util.TelemetryManager
import com.bismarck.voleimanager.app.util.TollCalculator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.Normalizer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.xmlpull.v1.XmlPullParser
import com.bismarck.voleimanager.app.data.model.BalancingMode

const val DEFAULT_GROUP_NAME = "Geral"
const val MAX_GROUP_NAME_LENGTH = 20
const val MAX_PLAYER_NAME_LENGTH = 24
private const val AUTO_CLEAR_GAME_AFTER_LAST_MATCH_MS = 12L * 60L * 60L * 1000L
private val REVIEW_REQUEST_MILESTONES = listOf(3, 10, 25)
// Gatilho de fallback do pedido de avaliação (ver registerCompletedMatchForReviewFallback):
// cobre quem nunca aciona os marcos de "limpeza válida" acima.
private const val REVIEW_FALLBACK_MIN_DISTINCT_DAYS = 2
private const val REVIEW_FALLBACK_MIN_MATCHES_FINISHED = 7
private const val KEY_MATCHES_FINISHED_COUNT = "matches_finished_count"
private const val KEY_LAST_MATCH_FINISHED_DATE = "last_match_finished_date"
private const val KEY_DISTINCT_MATCH_DAYS_COUNT = "distinct_match_days_count"
private const val KEY_REVIEW_FALLBACK_DONE = "review_fallback_done"
/** Intervalo mínimo entre trocas de qual(is) grupo(s) é(são) o(s) grupo(s) premium sincronizado(s)
 *  (ver [VoleiViewModel.setGroupCloudSynced]) — bloqueio otimista da UI; a regra de verdade é
 *  sempre revalidada no backend. */
private const val PREMIUM_GROUP_SWITCH_COOLDOWN_MILLIS = 15L * 24L * 60L * 60L * 1000L

/**
 * Cabeçalho do CSV de jogadores — fonte única usada tanto pela exportação real
 * ([VoleiViewModel.exportData]) quanto pelo modelo baixável ([VoleiViewModel.exportPlayersTemplate]),
 * para evitar desalinhamento se o formato mudar no futuro.
 */
private const val PLAYERS_CSV_HEADER =
    "ID,Nome,Elo,Partidas,Vitorias,Grupo,Prioridade,PedagioDiario,DataPedagio,PosicaoPreferida,PosicaoSecundaria"

private const val XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

enum class Screen { GAME, HISTORY, CLOUD_SYNC, FAQ, ABOUT }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CsvType { JOGADORES, HISTORICO, ELO_LOGS, BACKUP_COMPLETO }
/**
 * Cor de destaque disponível para um time (Time A ou Time B). Usuários premium podem escolher
 * qualquer uma das 5 para cada time, desde que Time A e Time B usem cores diferentes. Sem
 * premium, o app sempre usa o padrão [BLUE] (Time A) / [YELLOW] (Time B). O mapeamento para
 * valores reais de [androidx.compose.ui.graphics.Color] fica na camada de UI (ui/theme), não
 * aqui, para manter o ViewModel livre de tipos do Compose.
 */
enum class TeamAccentColor { BLUE, YELLOW, RED, GREEN, PURPLE }

/** Faz o parse seguro de um nome salvo de [TeamAccentColor] (ex.: vindo do banco), retornando
 *  `null` em vez de lançar exceção se o valor for desconhecido/corrompido. */
fun parseTeamAccentColorOrNull(name: String): TeamAccentColor? = try {
    TeamAccentColor.valueOf(name)
} catch (e: IllegalArgumentException) {
    null
}

/**
 * Perfil do usuário no app, perguntado uma única vez, antes de qualquer outra etapa do
 * onboarding (inclusive antes do onboarding de grupo). Organizador e Auxiliar são direcionados
 * para o cadastro/login gratuito na tela de Nuvem; Espectador segue sem conta obrigatória.
 */
enum class UserProfileType { ORGANIZADOR, AUXILIAR, ESPECTADOR }

/**
 * Passos intermediários mostrados uma única vez, logo após [VoleiViewModel.setUserProfileType],
 * antes do onboarding normal de criação de grupo. Organizador/Auxiliar são obrigados a criar
 * conta/entrar ([AUTH_REQUIRED]) antes de prosseguir (não precisam confirmar o e-mail ainda,
 * só ter feito login/cadastro). Espectador vê uma sugestão pulável de login
 * ([SPECTATOR_AUTH_SUGGESTION]) seguida de uma sugestão pulável de código de grupo
 * ([SPECTATOR_JOIN_SUGGESTION]). É um estado transitório, não persistido: se o app for encerrado
 * no meio do fluxo, o usuário simplesmente cai direto no onboarding normal de grupo na próxima
 * abertura (a pergunta de perfil em si já não seria mostrada de novo).
 */
enum class PostProfileOnboardingStage { NONE, AUTH_REQUIRED, SPECTATOR_AUTH_SUGGESTION, SPECTATOR_JOIN_SUGGESTION }

/**
 * Pacote de assinatura premium da sincronização em nuvem: [NONE] (sem assinatura), [SINGLE]
 * (1 grupo sincronizado) ou [MULTI] (até 5 grupos sincronizados). O preço de cada pacote é
 * definido inteiramente no Play Console (com preço por região, ex.: R$ 4,90/R$ 9,90 no Brasil) —
 * ver [com.bismarck.voleimanager.app.util.BillingManager]. A confirmação definitiva do pacote
 * ativo vem do backend (ver `billing-integration`/`purchase-validation-function`); até lá,
 * [VoleiViewModel.effectivePremiumPlanTier] usa o estado local do Play Billing
 * ([VoleiViewModel.realBillingPremiumPlanTier]) ou, só em build de debug sem uma compra real,
 * [VoleiViewModel.debugPremiumPlanTier].
 */
enum class CloudPlanTier(val maxSyncedGroups: Int) { NONE(0), SINGLE(1), MULTI(5) }

data class BackupData(
    val version: Int = 1,
    val date: String,
    val players: List<Player>,
    val history: List<MatchHistory>,
    val logs: List<PlayerEloLog>,
    /** Configuração do grupo exportado (inclui o tipo de grupo). Nulo em backups antigos. */
    val groupConfig: GroupConfig? = null,
    val tournamentTeams: List<TournamentTeam>? = null,
    val tournamentTeamMembers: List<TournamentTeamMember>? = null,
    val tournamentMatches: List<TournamentMatch>? = null,
    val groupLogs: List<GroupLog>? = null
)

data class PendingMergeImportData(
    val players: List<Player>,
    val history: List<MatchHistory>,
    val logs: List<PlayerEloLog>,
    val overlappingGroups: List<String>,
    val duplicatePlayerNames: List<String> = emptyList(),
    val duplicatePlayerGroups: Map<String, Int> = emptyMap(),
    /** Configuração do grupo do backup importado (só presente em importações de .vlz), usada para
     *  salvar a config de um grupo novo e para trocar automaticamente para ele após a importação. */
    val groupConfig: GroupConfig? = null
)

data class GameStateSnapshot(
    val groupName: String,
    val teamA: List<Player>,
    val teamB: List<Player>,
    val waitingList: List<Player>,
    val presentPlayerIds: List<Int>,
    val scoreA: Int,
    val scoreB: Int,
    val currentStreak: Int,
    val streakOwner: String?,
    val hasPreviousMatch: Boolean,
    val lastWinners: List<Player>,
    val lastLosers: List<Player>,
    val currentMatchStartTimestamp: Long? = null,
    val roundCounter: Int = 0,
    val restingPlayers: Map<Int, Int> = emptyMap(),
    val rebalancedPlayerIds: List<Int> = emptyList(),
    val autoSelectedLoserPlayerIds: List<Int> = emptyList(),
    val guaranteedNextMatchPlayerIds: List<Int> = emptyList(),
    val lastScoringTeam: String? = null,
    val rotationRequiredForTeam: String? = null,
    /** Posição ocupada por cada jogador na partida (Modo Posições Fixas). playerId -> nome do enum. */
    val assignedPositions: Map<Int, String> = emptyMap(),
    /** Índice da vaga ocupada por cada jogador dentro do próprio time (0 = topo do card base). */
    val assignedSlotIndices: Map<Int, Int> = emptyMap(),
    val compositionIncomplete: Boolean = false
)

data class ManualStreakAdjustmentLog(
    val timestamp: Long,
    val groupName: String,
    val team: String,
    val oldOwner: String?,
    val oldStreak: Int,
    val newOwner: String?,
    val newStreak: Int
)

data class ManualSubstitutionLog(
    val timestamp: Long,
    val groupName: String,
    val playerOutName: String,
    val playerInName: String,
    val targetTeam: String,
    val incomingSource: String
)

internal data class RestingMarkResult(
    val restingPlayers: Map<Int, Int>,
    val waitingList: List<Player>
)

internal data class ReturningPlayersResolution(
    val returningIds: Set<Int>,
    val restingPlayers: Map<Int, Int>,
    val waitingList: List<Player>
)

internal data class TeamSnapshotWithIds(
    val names: String,
    val ids: String
)

/** Converte para o formato "enxuto" publicado em `liveState` (ver [CloudSyncManager]) — usa
 *  [Player.publicId] (estável entre dispositivos) em vez do [Player.id] local. */
internal fun Player.toRemoteSnapshot() = RemotePlayerSnapshot(
    publicId = publicId,
    name = name,
    elo = elo,
    isPriority = isPriority,
    matchesPlayed = matchesPlayed,
    victories = victories,
    preferredPosition = preferredPosition,
    secondaryPosition = secondaryPosition
)

/** Reconstrói um [Player] "sintético" a partir de um snapshot remoto, para os dispositivos que
 *  entraram via código (Auxiliar/Espectador) reaproveitarem a mesma UI local de [Player]
 *  ([GameScreenContent][com.bismarck.voleimanager.app.ui.game.GameScreenContent]) sem terem
 *  nenhuma linha real na tabela `players` do Room. [Player.id] é derivado de forma estável a
 *  partir do [RemotePlayerSnapshot.publicId] (nunca colide com ids reais do Room porque esses
 *  dispositivos nunca têm jogadores reais salvos para o grupo remoto — a tabela local fica vazia
 *  para ele). Tolerância (`dailyToll`) e posição do dia não são sincronizadas: cada aparelho as
 *  calcularia de um jeito diferente sem sentido fora de quem organiza a presença localmente.
 */
internal fun RemotePlayerSnapshot.toSyntheticPlayer(groupName: String) = Player(
    id = publicId.hashCode(),
    name = name,
    elo = elo,
    matchesPlayed = matchesPlayed,
    victories = victories,
    isPriority = isPriority,
    groupName = groupName,
    preferredPosition = preferredPosition,
    secondaryPosition = secondaryPosition,
    publicId = publicId
)

private data class TeamSnapshotEntry(
    val name: String,
    val id: Int?
)

internal fun normalizeTeamSnapshotWithIds(
    rawNames: String,
    rawIds: String,
    normalizeName: (String) -> String
): TeamSnapshotWithIds {
    val idsByIndex = if (rawIds.isBlank()) emptyList() else rawIds.split(",").map { it.trim().toIntOrNull() }
    val entries = rawNames
        .take(255)
        .split(",")
        .mapIndexedNotNull { index, rawName ->
            val normalizedName = normalizeName(rawName)
            if (normalizedName.isBlank()) return@mapIndexedNotNull null
            TeamSnapshotEntry(name = normalizedName, id = idsByIndex.getOrNull(index))
        }
        .sortedBy { it.name.lowercase(Locale.ROOT) }

    val names = entries.joinToString(", ") { it.name }
    val ids = if (entries.none { it.id != null }) {
        ""
    } else {
        entries.joinToString(",") { it.id?.toString() ?: "" }
    }
    return TeamSnapshotWithIds(names = names, ids = ids)
}

/**
 * `Player.id` e `Player.publicId` são únicos globalmente (todos os grupos), mas um backup
 * (.vlz) preserva o `id`/`publicId` originais do dispositivo em que foi exportado. Reimportar
 * esse backup — seja para restaurar o mesmo grupo após um import parcial anterior, seja para
 * um teste em outro grupo do mesmo dispositivo — pode colidir com jogadores já existentes que
 * usam por acaso o mesmo id/publicId (ex.: ids sequenciais baixos de outro grupo já criado).
 * Como `insertPlayers` usa [androidx.room.OnConflictStrategy.IGNORE], essa colisão faz a linha
 * ser descartada silenciosamente, sem nenhum aviso — o jogador simplesmente não aparece.
 *
 * Esta função detecta essas colisões antes do insert e atribui uma nova identidade (id e/ou
 * publicId) apenas aos jogadores que colidem, propagando o novo id para as referências em
 * [MatchHistory.teamAIds]/[teamBIds] e [PlayerEloLog.playerId] do mesmo backup, para que o
 * histórico e o Elo continuem apontando para o jogador correto após o remapeamento.
 */
internal fun remapCollidingPlayerIdentities(
    players: List<Player>,
    history: List<MatchHistory>,
    logs: List<PlayerEloLog>,
    existingIds: Set<Int>,
    existingPublicIds: Set<String>
): Triple<List<Player>, List<MatchHistory>, List<PlayerEloLog>> {
    val usedIds = existingIds.toMutableSet()
    val usedPublicIds = existingPublicIds.toMutableSet()
    players.forEach { p ->
        if (p.id > 0) usedIds.add(p.id)
        usedPublicIds.add(p.publicId)
    }

    val idRemap = mutableMapOf<Int, Int>()
    var nextCandidateId = (usedIds.maxOrNull() ?: 0) + 1

    val remappedPlayers = players.map { p ->
        val needsNewId = p.id > 0 && existingIds.contains(p.id) && idRemap[p.id] == null
        val newId = when {
            !needsNewId -> p.id
            else -> {
                while (usedIds.contains(nextCandidateId)) nextCandidateId++
                usedIds.add(nextCandidateId)
                idRemap[p.id] = nextCandidateId
                nextCandidateId
            }
        }
        val needsNewPublicId = existingPublicIds.contains(p.publicId)
        val newPublicId = if (needsNewPublicId) {
            var candidate = java.util.UUID.randomUUID().toString()
            while (usedPublicIds.contains(candidate)) candidate = java.util.UUID.randomUUID().toString()
            usedPublicIds.add(candidate)
            candidate
        } else {
            p.publicId
        }
        if (newId == p.id && newPublicId == p.publicId) p else p.copy(id = newId, publicId = newPublicId)
    }

    if (idRemap.isEmpty()) return Triple(remappedPlayers, history, logs)

    fun remapIdCsv(csv: String): String {
        if (csv.isBlank()) return csv
        return csv.split(",").joinToString(",") { token ->
            val trimmed = token.trim()
            val originalId = trimmed.toIntOrNull()
            if (originalId != null) (idRemap[originalId] ?: originalId).toString() else token
        }
    }

    val remappedHistory = history.map { h ->
        if (h.teamAIds.isBlank() && h.teamBIds.isBlank()) h
        else h.copy(teamAIds = remapIdCsv(h.teamAIds), teamBIds = remapIdCsv(h.teamBIds))
    }
    val remappedLogs = logs.map { l ->
        idRemap[l.playerId]?.let { l.copy(playerId = it) } ?: l
    }

    return Triple(remappedPlayers, remappedHistory, remappedLogs)
}

internal fun canonicalizePersonNameCompat(name: String): String {
    val normalized = name.trim().replace(Regex("\\s+"), " ")
    val noAccents = Normalizer.normalize(normalized, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return noAccents.lowercase(Locale.ROOT)
}

internal fun collectDuplicatePlayerNames(players: List<Player>): List<String> {
    val duplicates = linkedSetOf<String>()
    players.groupBy { it.groupName }.forEach { (_, groupPlayers) ->
        val seen = mutableSetOf<String>()
        groupPlayers.forEach { player ->
            val key = canonicalizePersonNameCompat(player.name)
            if (key.isBlank()) return@forEach
            if (key in seen) {
                duplicates.add(player.name.trim())
            } else {
                seen.add(key)
            }
        }
    }
    return duplicates.toList()
}

internal fun resolveImportedPlayersForInsert(
    players: List<Player>,
    existingNamesByGroup: Map<String, Set<String>> = emptyMap()
): Pair<List<Player>, List<String>> {
    val accepted = mutableListOf<Player>()
    val skipped = linkedSetOf<String>()
    players.groupBy { it.groupName }.forEach { (groupName, groupPlayers) ->
        val seenInPayload = mutableSetOf<String>()
        val existingNames = existingNamesByGroup[groupName].orEmpty()
        groupPlayers.forEach { player ->
            val canonicalName = canonicalizePersonNameCompat(player.name)
            if (canonicalName.isBlank()) return@forEach
            if (canonicalName in existingNames || canonicalName in seenInPayload) {
                skipped.add("${player.name.trim()} [$groupName]")
                return@forEach
            }
            seenInPayload.add(canonicalName)
            accepted.add(player)
        }
    }
    return accepted to skipped.toList()
}

internal fun resolveImportedPlayersWithAutoRename(
    players: List<Player>,
    existingNamesByGroup: Map<String, Set<String>> = emptyMap()
): Pair<List<Player>, List<String>> {
    val accepted = mutableListOf<Player>()
    val renamed = linkedSetOf<String>()
    players.groupBy { it.groupName }.forEach { (groupName, groupPlayers) ->
        val seenInPayload = mutableSetOf<String>()
        val existingNames = existingNamesByGroup[groupName].orEmpty().toMutableSet()
        groupPlayers.forEach { player ->
            val canonicalName = canonicalizePersonNameCompat(player.name)
            if (canonicalName.isBlank()) return@forEach
        val baseName = player.name.trim().replace(Regex("\\s+"), " ").ifBlank { "Desconhecido" }
            if (canonicalName in existingNames || canonicalName in seenInPayload) {
                var nextIndex = 2
                var candidate = baseName
                while (true) {
                    val candidateCanonical = canonicalizePersonNameCompat(candidate)
                    if (candidateCanonical !in existingNames && candidateCanonical !in seenInPayload) {
                        break
                    }
                    candidate = "${baseName} $nextIndex"
                    nextIndex++
                }
                val renamedPlayer = player.copy(name = candidate)
                renamed.add("${player.name.trim()} -> ${renamedPlayer.name} [$groupName]")
                accepted.add(renamedPlayer)
                seenInPayload.add(canonicalizePersonNameCompat(candidate))
                existingNames.add(canonicalizePersonNameCompat(candidate))
                return@forEach
            }
            seenInPayload.add(canonicalName)
            existingNames.add(canonicalName)
            accepted.add(player)
        }
    }
    return accepted to renamed.toList()
}

internal fun applyRestingMark(
    currentResting: Map<Int, Int>,
    currentWaiting: List<Player>,
    playersToRest: List<Player>,
    returnRound: Int
): RestingMarkResult {
    if (playersToRest.isEmpty()) return RestingMarkResult(currentResting, currentWaiting)
    val idsToRest = playersToRest.map { it.id }.toSet()
    val nextResting = currentResting.toMutableMap()
    playersToRest.forEach { nextResting[it.id] = returnRound }
    val waitingWithoutResting = currentWaiting.filterNot { idsToRest.contains(it.id) }
    val nextWaiting = (waitingWithoutResting + playersToRest).distinctBy { it.id }
    return RestingMarkResult(nextResting, nextWaiting)
}

internal fun resolveReturningPlayers(
    currentResting: Map<Int, Int>,
    currentWaiting: List<Player>,
    roundCounter: Int
): ReturningPlayersResolution {
    val returningIds = currentResting.filterValues { it <= roundCounter }.keys
    if (returningIds.isEmpty()) {
        return ReturningPlayersResolution(
            returningIds = emptySet(),
            restingPlayers = currentResting,
            waitingList = currentWaiting
        )
    }
    val nextResting = currentResting.filterKeys { !returningIds.contains(it) }
    val nextWaiting = currentWaiting.filterNot { returningIds.contains(it.id) }
    return ReturningPlayersResolution(returningIds, nextResting, nextWaiting)
}

@OptIn(ExperimentalCoroutinesApi::class)
class VoleiViewModel(application: Application, private val repository: VoleiRepository) :
    AndroidViewModel(application) {
    private val screenDataSharing = SharingStarted.Eagerly

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _pendingMergeImport = MutableStateFlow<PendingMergeImportData?>(null)
    val pendingMergeImport: StateFlow<PendingMergeImportData?> = _pendingMergeImport.asStateFlow()

    /**
     * Uri de um backup (.vlz/.json) aberto fora do app (gerenciador de arquivos, e-mail, etc.),
     * aguardando confirmação do usuário antes de importar. Ver [onExternalFileOpened].
     */
    private val _pendingExternalImportUri = MutableStateFlow<Uri?>(null)
    val pendingExternalImportUri: StateFlow<Uri?> = _pendingExternalImportUri.asStateFlow()

    /** Chamado pela MainActivity quando o app é aberto via ACTION_VIEW (ex.: toque em um .vlz). */
    fun onExternalFileOpened(uri: Uri) {
        _pendingExternalImportUri.value = uri
    }

    fun cancelExternalImport() {
        _pendingExternalImportUri.value = null
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    /** Exibe uma mensagem simples de UI (snackbar), ex.: aviso de recurso bloqueado. */
    fun showMessage(message: String) {
        _uiMessage.value = message
    }
    private val _currentScreen = MutableStateFlow(Screen.GAME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    private val _isGroupDataLoading = MutableStateFlow(true)
    val isGroupDataLoading: StateFlow<Boolean> = _isGroupDataLoading.asStateFlow()
    private var groupLoadToken = 0

    /** Timestamp da última [LiveGameState] que este próprio dispositivo publicou — usado para
     *  descartar o "eco" de estados remotos recebidos de volta pelo listener de
     *  [CloudSyncManager.observeLiveState] logo após publicarmos algo (ver `aux-bidirectional-sync`). */
    private var lastPushedUpdatedAt: Long = 0L

    /** ID do último pedido de encerramento de partida ([LiveGameState.pendingFinishRequestId]) já
     *  processado pelo organizador — evita chamar [finishGame] duas vezes para o mesmo pedido de
     *  um Auxiliar remoto. */
    private var lastProcessedFinishRequestId: String? = null

    /** Último [LiveGameState.presentPlayers] recebido/observado — usado por um Auxiliar remoto
     *  para "ecoar" essa lista ao publicar suas próprias atualizações (placar, times), já que só
     *  o organizador tem o roster real no Room para recalculá-la (ver [observeAndPushCloudLiveState]). */
    private var lastKnownRemotePresentPlayers: List<RemotePlayerSnapshot> = emptyList()

    /** Último [LiveGameState.allPlayers] recebido/observado — mesmo papel de
     *  [lastKnownRemotePresentPlayers], mas para o elenco completo do grupo (usado pelo Auxiliar
     *  para ver a lista de jogadores completa, sem restrição de edição). */
    private var lastKnownRemoteAllPlayers: List<RemotePlayerSnapshot> = emptyList()

    /** Último id de pedido de alternância de presença ([LiveGameState.pendingPresenceToggleRequestId])
     *  já processado pelo organizador, para não aplicar o mesmo pedido de um Auxiliar duas vezes
     *  (mesmo padrão de [lastProcessedFinishRequestId]). */
    private var lastProcessedPresenceToggleRequestId: String? = null

    private val _currentGroupConfig = MutableStateFlow(
        GroupConfig(groupName = "", onboardingStep = ONBOARDING_STEP_GROUP_NAME)
    )
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

    /**
     * `true` quando o grupo atualmente selecionado foi sincronizado via código de convite de
     * Espectador ([GroupConfig.remoteRole] == `"ESPECTADOR"`) — ou seja, este dispositivo não tem
     * permissão de edição sobre o grupo, só visualização em tempo real (ver
     * `spectator-game-screen-restrictions`). Grupos próprios (`remoteRole == null`) ou entrados
     * como Auxiliar sempre retornam `false`.
     */
    val isSpectatorOfCurrentGroup: StateFlow<Boolean> = _currentGroupConfig
        .map { it.remoteRole == UserProfileType.ESPECTADOR.name }
        .stateIn(viewModelScope, screenDataSharing, false)

    /** `true` quando o grupo ativo foi acessado via código de Auxiliar
     *  ([GroupConfig.remoteRole] == `"AUXILIAR"`) — este dispositivo não tem roster real no Room
     *  para o grupo (não é o dono), mas tem permissão de edição total, só que espelhada via
     *  [LiveGameState] (ver `aux-full-roster-sync`). */
    val isAuxiliarOfCurrentGroup: StateFlow<Boolean> = _currentGroupConfig
        .map { it.remoteRole == UserProfileType.AUXILIAR.name }
        .stateIn(viewModelScope, screenDataSharing, false)

    val players = repository.allPlayers.stateIn(
        viewModelScope,
        screenDataSharing,
        emptyList()
    )
    private val _allHistory = repository.history.stateIn(
        viewModelScope,
        screenDataSharing,
        emptyList()
    )
    private val _allGroupConfigs = repository.allGroupConfigs.stateIn(
        viewModelScope,
        screenDataSharing,
        emptyList()
    )
    private val _allEloLogs = repository.eloLogs.stateIn(
        viewModelScope,
        screenDataSharing,
        emptyList()
    )

    private val currentGroupName = _currentGroupConfig
        .map { it.groupName }
        .distinctUntilChanged()

    val currentGroupPlayers = currentGroupName
        .flatMapLatest { repository.playersByGroup(it) }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    val currentGroupHistory = currentGroupName
        .flatMapLatest { repository.historyByGroup(it) }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    val currentGroupEloLogs = currentGroupName
        .flatMapLatest { repository.eloLogsByGroup(it) }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    val groupsSortedByRecentHistory = combine(
        _allHistory,
        players,
        _allEloLogs,
        _allGroupConfigs
    ) { history, allPlayers, allEloLogs, allConfigs ->
        val groupsWithDates = mutableMapOf<String, Long>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        history.forEach { match ->
            try {
                val matchTime = sdf.parse(match.date)?.time ?: 0L
                val currentMax = groupsWithDates[match.groupName] ?: 0L
                if (matchTime > currentMax) {
                    groupsWithDates[match.groupName] = matchTime
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }

        val orderedByHistory = groupsWithDates.toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val allGroups = linkedSetOf<String>()
        allGroups.addAll(allConfigs.map { it.groupName })
        allGroups.addAll(allPlayers.map { it.groupName })
        allGroups.addAll(history.map { it.groupName })
        allGroups.addAll(allEloLogs.map { it.groupName })

        val groupsWithoutHistory = allGroups
            .filterNot { groupsWithDates.containsKey(it) }
            .sortedBy { it.lowercase(Locale.getDefault()) }

        orderedByHistory + groupsWithoutHistory
    }.stateIn(viewModelScope, screenDataSharing, emptyList())

    private val _historyDateFilter = MutableStateFlow<String?>(null)
    val historyDateFilter = _historyDateFilter.asStateFlow()

    val availableHistoryDates = currentGroupHistory.map { list ->
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        list.map { it.date.split(" ")[0] }.distinct().sortedWith { d1, d2 ->
            try {
                sdf.parse(d1)?.compareTo(sdf.parse(d2)) ?: 0
            } catch (e: Exception) {
                0
            }
        }.reversed()
    }.stateIn(viewModelScope, screenDataSharing, emptyList())

    val targetDate = combine(currentGroupEloLogs, availableHistoryDates) { logs, _dates ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hasToday = logs.any { it.date == today }
        if (hasToday) today else logs.map { it.date }.maxOrNull() ?: today
    }.stateIn(
        viewModelScope,
        screenDataSharing,
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    val gamesPlayedTodayMap = combine(currentGroupEloLogs, targetDate) { logs, tDate ->
        logs.filter { it.date == tDate }.groupingBy { it.playerId }.eachCount()
    }.stateIn(viewModelScope, screenDataSharing, emptyMap())

    // Usado na tela de jogo em andamento: conta apenas os jogos do dia real (sem cair
    // para o último dia com histórico), zerando para o primeiro jogo do dia de cada jogador.
    val gamesPlayedStrictTodayMap = currentGroupEloLogs.map { logs ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        logs.filter { it.date == today }.groupingBy { it.playerId }.eachCount()
    }.stateIn(viewModelScope, screenDataSharing, emptyMap())

    val sortedPlayersForPresence =
        combine(currentGroupPlayers, gamesPlayedTodayMap) { pList, gamesMap ->
            pList.sortedWith { p1, p2 ->
                val g1 = gamesMap[p1.id] ?: 0
                val g2 = gamesMap[p2.id] ?: 0
                when {
                    g1 > 0 || g2 > 0 -> {
                        // Para jogadores que já jogaram hoje, ordena por jogos em ordem decrescente e depois por Elo decrescente
                        if (g1 != g2) g2.compareTo(g1) else p2.elo.compareTo(p1.elo)
                    }
                    else -> {
                        // Para jogadores sem jogos no dia, ordena alfabeticamente pelo nome
                        p1.name.compareTo(p2.name)
                    }
                }
            }
        }.stateIn(viewModelScope, screenDataSharing, emptyList())

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _showElo = MutableStateFlow(false)
    val showElo: StateFlow<Boolean> = _showElo.asStateFlow()

    private val _showToll = MutableStateFlow(false)
    val showToll: StateFlow<Boolean> = _showToll.asStateFlow()

    private val _isSupporter = MutableStateFlow(false)
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _telemetryEnabled = MutableStateFlow(false)
    val telemetryEnabled: StateFlow<Boolean> = _telemetryEnabled.asStateFlow()

    /** True apenas antes do usuário responder ao diálogo de consentimento de telemetria pela
     *  primeira vez (nunca mais volta a ser true depois disso, mesmo que ele desative depois). */
    private val _showTelemetryConsentPrompt = MutableStateFlow(false)
    val showTelemetryConsentPrompt: StateFlow<Boolean> = _showTelemetryConsentPrompt.asStateFlow()

    private val _userProfileType = MutableStateFlow<UserProfileType?>(null)
    val userProfileType: StateFlow<UserProfileType?> = _userProfileType.asStateFlow()

    /** True apenas antes do usuário responder à pergunta de perfil pela primeira vez (perguntada
     *  uma única vez, antes de qualquer outra etapa do onboarding, inclusive o de grupo). */
    private val _showUserProfileOnboarding = MutableStateFlow(false)
    val showUserProfileOnboarding: StateFlow<Boolean> = _showUserProfileOnboarding.asStateFlow()

    private val _postProfileOnboardingStage = MutableStateFlow(PostProfileOnboardingStage.NONE)
    val postProfileOnboardingStage: StateFlow<PostProfileOnboardingStage> = _postProfileOnboardingStage.asStateFlow()

    /**
     * Sobreposição pessoal (só neste dispositivo/usuário) das cores de time, disponível apenas
     * para quem tem acesso premium — mesmo estando num grupo cujo organizador definiu outras
     * cores. Quando desabilitada (padrão), o usuário vê as cores oficiais do grupo
     * ([GroupConfig.teamAColorName]/[GroupConfig.teamBColorName], definidas pelo organizador ou
     * auxiliar via [setGroupTeamColors]) — inclusive se ele próprio não for premium.
     */
    private val _personalTeamColorOverrideEnabled = MutableStateFlow(false)
    private val _personalTeamAColor = MutableStateFlow(TeamAccentColor.BLUE)
    private val _personalTeamBColor = MutableStateFlow(TeamAccentColor.YELLOW)

    /**
     * Estado local e otimista da assinatura, refletindo a última compra conhecida pelo Play
     * Billing neste aparelho ([BillingManager.activeProductIds]) — `true` assim que uma das duas
     * assinaturas ([BillingProductIds.SINGLE_GROUP]/[BillingProductIds.MULTI_GROUP]) é comprada e
     * reconhecida (`acknowledge`), mesmo antes do backend confirmar via Real-time Developer
     * Notifications (`purchase-validation-function`, ainda não implementada em
     * `volei_manager_backend`). Quando essa fase existir, isso deve ser substituído/combinado com
     * `users/{uid}.activeEntitlement` do Firestore, que é a fonte de verdade definitiva (única
     * capaz de refletir cancelamento, reembolso ou expiração sem o app precisar estar aberto).
     */
    private val _realPremiumEntitlement: StateFlow<Boolean> = BillingManager.activeProductIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, screenDataSharing, false)

    /**
     * Interruptor **apenas de debug** para simular uma assinatura premium ativa sem precisar de
     * uma compra real na Play Store, permitindo testar recursos premium (ex.: cores de time)
     * durante o desenvolvimento. Nunca tem efeito em build de release: [setDebugPremiumOverride]
     * ignora a chamada e [hasPremiumAccess] nunca considera este flag fora de [BuildConfig.DEBUG].
     */
    private val _debugPremiumOverride = MutableStateFlow(false)
    val debugPremiumOverride: StateFlow<Boolean> = _debugPremiumOverride.asStateFlow()

    val hasPremiumAccess: StateFlow<Boolean> =
        combine(_realPremiumEntitlement, _debugPremiumOverride) { real, debugOverride ->
            real || (BuildConfig.DEBUG && debugOverride)
        }.stateIn(viewModelScope, screenDataSharing, false)

    /**
     * Cores efetivas do Time A/B a serem exibidas: por padrão, as cores oficiais do grupo atual
     * (definidas pelo organizador/auxiliar premium, visíveis a todos, inclusive observadores sem
     * premium). Se o próprio usuário tiver acesso premium e tiver ativado uma sobreposição
     * pessoal ([setPersonalTeamColorOverride]), essa sobreposição prevalece só para ele.
     */
    val effectiveTeamColors: StateFlow<Pair<TeamAccentColor, TeamAccentColor>> = combine(
        _currentGroupConfig,
        hasPremiumAccess,
        _personalTeamColorOverrideEnabled,
        _personalTeamAColor,
        _personalTeamBColor
    ) { config, premium, overrideEnabled, personalA, personalB ->
        if (premium && overrideEnabled) {
            personalA to personalB
        } else {
            val groupA = config.teamAColorName?.let(::parseTeamAccentColorOrNull) ?: TeamAccentColor.BLUE
            val groupB = config.teamBColorName?.let(::parseTeamAccentColorOrNull) ?: TeamAccentColor.YELLOW
            groupA to groupB
        }
    }.stateIn(viewModelScope, screenDataSharing, TeamAccentColor.BLUE to TeamAccentColor.YELLOW)

    val effectiveTeamAColor: StateFlow<TeamAccentColor> = effectiveTeamColors
        .map { it.first }
        .stateIn(viewModelScope, screenDataSharing, TeamAccentColor.BLUE)
    val effectiveTeamBColor: StateFlow<TeamAccentColor> = effectiveTeamColors
        .map { it.second }
        .stateIn(viewModelScope, screenDataSharing, TeamAccentColor.YELLOW)

    val personalTeamColorOverrideEnabled: StateFlow<Boolean> =
        _personalTeamColorOverrideEnabled.asStateFlow()
    val personalTeamAColor: StateFlow<TeamAccentColor> = _personalTeamAColor.asStateFlow()
    val personalTeamBColor: StateFlow<TeamAccentColor> = _personalTeamBColor.asStateFlow()

    /** Cores oficiais configuradas no grupo atual (editáveis via [setGroupTeamColors]),
     *  ignorando qualquer sobreposição pessoal — usado para preencher o seletor de edição. */
    val groupTeamAColor: StateFlow<TeamAccentColor> = _currentGroupConfig
        .map { it.teamAColorName?.let(::parseTeamAccentColorOrNull) ?: TeamAccentColor.BLUE }
        .stateIn(viewModelScope, screenDataSharing, TeamAccentColor.BLUE)
    val groupTeamBColor: StateFlow<TeamAccentColor> = _currentGroupConfig
        .map { it.teamBColorName?.let(::parseTeamAccentColorOrNull) ?: TeamAccentColor.YELLOW }
        .stateIn(viewModelScope, screenDataSharing, TeamAccentColor.YELLOW)

    // ---------------------------------------------------------------------------------------
    // Sincronização em nuvem (tela "Nuvem") — modelo local/premium. A engine de fato (Firestore),
    // o cadastro/login (Firebase Auth) e a validação de compra ficam para as fases seguintes
    // (`auth-account-flow`, `firestore-sync-engine`, `billing-integration`); por ora esta seção
    // cobre o que já dá para testar localmente: escolha de qual(is) grupo(s) é(são) o(s) grupo(s)
    // premium sincronizado(s), respeitando o limite do pacote e o intervalo mínimo de troca.
    // ---------------------------------------------------------------------------------------

    /** Todos os grupos locais, para a tela de Nuvem listar candidatos à sincronização. */
    val allGroupConfigs: StateFlow<List<GroupConfig>> = _allGroupConfigs

    /** Nomes dos grupos marcados como sincronizados em nuvem neste dispositivo. */
    val cloudSyncedGroupNames: StateFlow<List<String>> = _allGroupConfigs
        .map { list -> list.filter { it.isCloudSynced }.map { it.groupName } }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    /**
     * Pacote real derivado das assinaturas ativas conhecidas pelo Play Billing neste aparelho
     * ([BillingManager.activeProductIds]) — [CloudPlanTier.MULTI] tem prioridade sobre
     * [CloudPlanTier.SINGLE] no caso (não esperado) de ambas aparecerem ativas ao mesmo tempo.
     * Ver a ressalva de [_realPremiumEntitlement] sobre isso ainda não ser validado pelo backend.
     */
    private val _realPremiumPlanTier: StateFlow<CloudPlanTier> = BillingManager.activeProductIds
        .map { productIds ->
            when {
                BillingProductIds.MULTI_GROUP in productIds -> CloudPlanTier.MULTI
                BillingProductIds.SINGLE_GROUP in productIds -> CloudPlanTier.SINGLE
                else -> CloudPlanTier.NONE
            }
        }
        .stateIn(viewModelScope, screenDataSharing, CloudPlanTier.NONE)

    /** Pacote simulado **apenas em build de debug**, para testar o limite de grupos sincronizados
     *  sem precisar de uma compra real (ver [setDebugPremiumPlanTier]). */
    private val _debugPremiumPlanTier = MutableStateFlow(CloudPlanTier.SINGLE)
    val debugPremiumPlanTier: StateFlow<CloudPlanTier> = _debugPremiumPlanTier.asStateFlow()

    /** Pacote efetivamente em vigor: [CloudPlanTier.NONE] sem acesso premium; caso contrário, o
     *  pacote real quando existir, senão (só em debug) o pacote simulado. */
    val effectivePremiumPlanTier: StateFlow<CloudPlanTier> = combine(
        hasPremiumAccess,
        _realPremiumEntitlement,
        _realPremiumPlanTier,
        _debugPremiumPlanTier
    ) { hasAccess, realEntitlement, realTier, debugTier ->
        when {
            !hasAccess -> CloudPlanTier.NONE
            realEntitlement -> realTier
            BuildConfig.DEBUG -> debugTier
            else -> CloudPlanTier.NONE
        }
    }.stateIn(viewModelScope, screenDataSharing, CloudPlanTier.NONE)

    /** Só tem efeito em build de debug — troca o pacote simulado (1 ou até 5 grupos) usado por
     *  [effectivePremiumPlanTier] enquanto não existe integração real de pagamento. */
    fun setDebugPremiumPlanTier(tier: CloudPlanTier) {
        if (!BuildConfig.DEBUG || tier == CloudPlanTier.NONE) return
        _debugPremiumPlanTier.value = tier
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putString("debug_premium_plan_tier", tier.name).apply()
    }

    /** Ofertas de assinatura disponíveis na Play Store (preço já formatado/localizado), vindas do
     *  [BillingManager] — vazio até os produtos existirem no Play Console e o billing conectar. */
    val subscriptionOffers: StateFlow<List<SubscriptionOffer>> = BillingManager.offers

    /** Melhor oferta disponível para [tier] (mensal ou anual, conforme [annual]), ou `null` se as
     *  ofertas ainda não carregaram (ex.: produto ainda não cadastrado no Play Console, ou sem
     *  conexão) — quem chama deve tratar esse caso com uma mensagem amigável. */
    fun findSubscriptionOffer(tier: CloudPlanTier, annual: Boolean): SubscriptionOffer? {
        val productId = when (tier) {
            CloudPlanTier.SINGLE -> BillingProductIds.SINGLE_GROUP
            CloudPlanTier.MULTI -> BillingProductIds.MULTI_GROUP
            CloudPlanTier.NONE -> return null
        }
        val basePlanId = if (annual) BillingProductIds.BASE_PLAN_ANNUAL else BillingProductIds.BASE_PLAN_MONTHLY
        val offers = subscriptionOffers.value.filter { it.productId == productId }
        return offers.firstOrNull { it.basePlanId == basePlanId } ?: offers.firstOrNull()
    }

    /**
     * Lança o fluxo de compra nativo da Play Store para o pacote [tier] (mensal ou anual). Requer
     * que [subscriptionOffers] já tenha a oferta correspondente (senão mostra uma mensagem
     * amigável) — a confirmação chega de forma assíncrona pelo `PurchasesUpdatedListener` do
     * [BillingManager], refletida automaticamente em [hasPremiumAccess]/[effectivePremiumPlanTier]
     * assim que a compra for reconhecida.
     */
    fun purchasePremiumPlan(activity: Activity, tier: CloudPlanTier, annual: Boolean) {
        val offer = findSubscriptionOffer(tier, annual)
        if (offer == null) {
            showMessage(getApplication<Application>().getString(R.string.cloud_sync_plan_offer_unavailable))
            return
        }
        val launched = BillingManager.launchPurchaseFlow(activity, offer)
        if (!launched) {
            showMessage(getApplication<Application>().getString(R.string.cloud_sync_plan_offer_unavailable))
        }
    }

    /** Reconsulta as assinaturas ativas conhecidas pela Play Store (ex.: ao o usuário voltar ao
     *  app depois de concluir uma compra na tela nativa da Play Store) — ver [BillingManager.refreshPurchases]. */
    fun refreshPurchases() = BillingManager.refreshPurchases()

    /**
     * Ativa ou desativa a sincronização em nuvem de [groupName]. Exige acesso premium; ao ativar,
     * respeita o limite de grupos simultâneos do pacote vigente ([effectivePremiumPlanTier]) e o
     * intervalo mínimo de 15 dias entre trocas (ver [PREMIUM_GROUP_SWITCH_COOLDOWN_MILLIS]) — a
     * regra "de verdade" será sempre revalidada no backend quando a Cloud Function existir; aqui
     * é só o bloqueio otimista da UI. Desativar nunca é bloqueado (libera uma vaga).
     */
    fun setGroupCloudSynced(groupName: String, synced: Boolean) = viewModelScope.launch {
        if (!hasPremiumAccess.value) return@launch
        val configs = repository.getAllGroupConfigs()
        val target = configs.firstOrNull { it.groupName == groupName } ?: return@launch
        if (target.isCloudSynced == synced) return@launch

        if (!synced) {
            repository.saveGroupConfig(target.copy(isCloudSynced = false, cloudGroupId = null))
            if (_currentGroupConfig.value.groupName == groupName) {
                _currentGroupConfig.value = _currentGroupConfig.value.copy(
                    isCloudSynced = false,
                    cloudGroupId = null
                )
            }
            return@launch
        }

        val currentlySynced = configs.filter { it.isCloudSynced }
        val maxAllowed = effectivePremiumPlanTier.value.maxSyncedGroups
        if (currentlySynced.size >= maxAllowed) {
            showMessage(getApplication<Application>().getString(R.string.cloud_sync_limit_reached))
            return@launch
        }
        // O intervalo de 15 dias só existe para impedir trocar QUAL grupo ocupa a vaga única do
        // pacote SINGLE (o pedido original do usuário fala em "mudar o grupo premium" no
        // singular). No pacote MULTI, preencher vagas ainda livres (até 5) é uma simples adição
        // de capacidade, não uma troca — não faz sentido travar isso por 15 dias, senão quem
        // acabou de assinar o MULTI teria que esperar 15 dias entre cada um dos 5 grupos iniciais.
        if (maxAllowed == CloudPlanTier.SINGLE.maxSyncedGroups) {
            // Usa `configs` (todos os grupos), não `currentlySynced`: desativar o grupo anterior
            // já zera seu `isCloudSynced` sem apagar `lastPremiumSwitchAt`, então o cooldown
            // precisa olhar a última troca registrada em qualquer grupo, não só nos ainda ativos.
            val lastSwitchAt = configs.mapNotNull { it.lastPremiumSwitchAt }.maxOrNull()
            if (lastSwitchAt != null && System.currentTimeMillis() - lastSwitchAt < PREMIUM_GROUP_SWITCH_COOLDOWN_MILLIS) {
                showMessage(getApplication<Application>().getString(R.string.cloud_sync_switch_cooldown))
                return@launch
            }
        }
        val now = System.currentTimeMillis()

        val updated = target.copy(
            isCloudSynced = true,
            cloudGroupId = target.publicId,
            lastPremiumSwitchAt = now
        )
        repository.saveGroupConfig(updated)
        if (_currentGroupConfig.value.groupName == groupName) {
            _currentGroupConfig.value = _currentGroupConfig.value.copy(
                isCloudSynced = true,
                cloudGroupId = target.publicId,
                lastPremiumSwitchAt = now
            )
        }
        TelemetryManager.logGroupCloudSynced(
            getApplication(),
            target.groupType,
            effectivePremiumPlanTier.value.name
        )

        // Melhor esforço: também registra no backend (cria/atualiza `cloudGroups/{cloudGroupId}`),
        // seguindo o mesmo padrão local-first do AuthManager — a UI já foi liberada localmente
        // acima, então uma falha aqui (ex.: sem assinatura real ainda, `failed-precondition`) só
        // é logada, nunca bloqueia quem está testando com a simulação de premium em debug.
        val backendError = CloudFunctionsManager.switchPremiumGroup(target.publicId, groupName)
        if (backendError != null) {
            Log.d("VoleiViewModel", "switchPremiumGroup (best-effort) falhou: $backendError")
        }
    }

    /**
     * Gera um código de convite (via a Cloud Function real `createJoinCode`) para [groupName],
     * concedendo o papel [role] a quem resgatar o código. Exige que o grupo já esteja
     * sincronizado em nuvem (tenha um `cloudGroupId`) — se ainda não tiver acontecido a criação
     * real do documento no backend (ex.: sem entitlement premium ainda), a Cloud Function retorna
     * um erro amigável, repassado como está a [onResult].
     */
    fun generateJoinCode(groupName: String, role: JoinRole, onResult: (GeneratedJoinCode?, String?) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            val target = repository.getGroupConfig(groupName)
            val cloudGroupId = target?.cloudGroupId
            if (cloudGroupId == null) {
                onResult(null, getApplication<Application>().getString(R.string.generate_join_code_group_not_synced))
                return@launch
            }
            val result = CloudFunctionsManager.createJoinCode(cloudGroupId, role)
            onResult(result.getOrNull(), result.exceptionOrNull()?.message)
        }

    // ---------------------------------------------------------------------------------------
    // Conta (Firebase Auth) — cadastro/login gratuito por e-mail/senha, exigido de
    // Organizador/Auxiliar antes de assinar um pacote premium. Login com Google fica para uma
    // fase seguinte (`auth-account-flow`, item de login social).
    // ---------------------------------------------------------------------------------------

    /** Usuário logado no momento (ou `null`), refletido no cabeçalho do menu lateral. */
    val currentUser: StateFlow<AppAuthUser?> = AuthManager.currentUser
        .stateIn(viewModelScope, screenDataSharing, null)

    private val _authInProgress = MutableStateFlow(false)
    val authInProgress: StateFlow<Boolean> = _authInProgress.asStateFlow()

    /** Cria uma conta gratuita e já efetua o login. [fullName]/[birthDate] ficam guardados para
     *  uma futura verificação de elegibilidade de compra premium; [nickname] é o nome público
     *  exibido no topo do app. [onResult] recebe `null` em caso de sucesso, ou uma mensagem de
     *  erro amigável para exibir no diálogo. */
    fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        nickname: String,
        birthDate: String,
        onResult: (String?) -> Unit
    ) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.signUp(email.trim(), password, fullName.trim(), nickname.trim(), birthDate.trim())
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Efetua login com e-mail/senha. [onResult] recebe `null` em caso de sucesso, ou uma
     *  mensagem de erro amigável para exibir no diálogo. */
    fun signInWithEmail(email: String, password: String, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.signIn(email.trim(), password)
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Login (ou cadastro automático, no primeiro acesso) via conta Google — ver
     *  [com.bismarck.voleimanager.app.util.GoogleSignInHelper] e
     *  [AuthManager.signInWithGoogleIdToken]. [context] precisa ser um contexto de Activity (ex.:
     *  `LocalContext.current` na Composable que abre o diálogo), pois o seletor de contas do
     *  Google é uma UI do sistema exibida sobre a Activity atual. */
    fun signInWithGoogle(context: Context, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val webClientId = context.getString(R.string.google_web_client_id)
            val error = GoogleSignInHelper.getIdToken(context, webClientId).fold(
                onSuccess = { idToken -> AuthManager.signInWithGoogleIdToken(idToken) },
                onFailure = { e -> e.message ?: getApplication<Application>().getString(R.string.google_sign_in_error) }
            )
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Reenvia o e-mail de confirmação da conta logada (ver [AuthManager.resendVerificationEmail]).
     *  [onResult] recebe `null` em caso de sucesso, ou uma mensagem amigável (erro, ou aviso caso
     *  o e-mail já esteja confirmado). */
    fun resendVerificationEmail(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            onResult(AuthManager.resendVerificationEmail())
        }
    }

    /** Recarrega o usuário logado (ver [AuthManager.refreshCurrentUser]) para que
     *  [currentUser].emailVerified reflita uma confirmação feita fora do app (link recebido por
     *  e-mail) assim que o app volta ao primeiro plano — faz o botão "Reenviar e-mail de
     *  confirmação" sumir automaticamente sem precisar de nenhuma ação do usuário. */
    fun refreshCurrentUser() {
        viewModelScope.launch { AuthManager.refreshCurrentUser() }
    }

    /** Inicia a troca de e-mail da conta logada (confirma a senha atual, depois envia um link de
     *  confirmação para o novo e-mail — ver [AuthManager.changeEmail]). [onResult] recebe `null`
     *  em caso de sucesso, ou uma mensagem de erro amigável. */
    fun changeEmail(currentPassword: String, newEmail: String, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.changeEmail(currentPassword, newEmail.trim())
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Altera a senha da conta logada, exigindo a senha atual (ver [AuthManager.changePassword]).
     *  [onResult] recebe `null` em caso de sucesso, ou uma mensagem de erro amigável. */
    fun changePassword(currentPassword: String, newPassword: String, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.changePassword(currentPassword, newPassword)
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Envia o e-mail de "esqueci minha senha" (ver [AuthManager.sendPasswordResetEmail]), usado
     *  a partir da tela de login sem precisar estar logado. [onResult] recebe `null` em caso de
     *  sucesso, ou uma mensagem de erro amigável. */
    fun sendPasswordResetEmail(email: String, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.sendPasswordResetEmail(email.trim())
            _authInProgress.value = false
            onResult(error)
        }
    }

    fun signOut() {
        AuthManager.signOut()
    }

    /** Atualiza o apelido público, nome completo e data de nascimento do usuário logado. */
    fun updateUserProfile(nickname: String, fullName: String, birthDate: String, onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.updateProfile(nickname.trim(), fullName.trim(), birthDate.trim())
            _authInProgress.value = false
            onResult(error)
        }
    }

    /** Define (ou remove, se [base64] for `null`) a foto de perfil do usuário logado. */
    fun updateProfilePhoto(base64: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            onResult(AuthManager.updateProfilePhoto(base64))
        }
    }

    /** Apaga a conta do usuário logado (Firebase Auth + perfil no Firestore). [onResult] recebe
     *  `null` em caso de sucesso, ou uma mensagem de erro amigável (por exemplo, pedindo para
     *  entrar novamente antes de apagar a conta, exigência de segurança do Firebase). */
    fun deleteAccount(onResult: (String?) -> Unit) {
        _authInProgress.value = true
        viewModelScope.launch {
            val error = AuthManager.deleteAccount()
            _authInProgress.value = false
            onResult(error)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Grupos remotos (entrados via código de Auxiliar/Espectador de outra pessoa) e
    // transferência de posse de um grupo premium. Sem o backend de sincronização
    // (`firestore-sync-engine`) ainda não existe validação/decodificação real de código — por
    // ora, o código apenas define o papel (prefixo "AUX"/"ESP") e cria um grupo local marcado
    // como remoto, para já poder testar a UI (ícone de streaming, sem editar/apagar, botão
    // "sair"). Quando o backend existir, isso passa a resolver o grupo/õs dados reais.
    // ---------------------------------------------------------------------------------------

    /**
     * Tenta "entrar" num grupo de outra pessoa a partir de um código de convite, chamando a
     * Cloud Function real `redeemJoinCode` (`volei_manager_backend`). Retorna uma mensagem de
     * erro amigável, ou `null` em caso de sucesso (e já seleciona o grupo criado).
     *
     * Como um código só é resgatável de verdade depois que o organizador tiver uma assinatura
     * premium ativa validada no servidor (`switchPremiumGroup` precisa rodar com sucesso antes,
     * criando `cloudGroups/{cloudGroupId}`), e ainda não existe cobrança real, essa chamada
     * tende a falhar em qualquer ambiente sem entitlement — por isso, em build de debug, caímos
     * de volta na simulação local antiga (papel pelo prefixo "AUX"/"ESP") só para continuar
     * testando a UI enquanto o backend de cobrança não existe.
     */
    fun joinGroupWithCode(code: String, onResult: (String?) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            onResult(getApplication<Application>().getString(R.string.join_group_invalid_code))
            return@launch
        }

        val remoteResult = CloudFunctionsManager.redeemJoinCode(trimmed)
        val redeemed = remoteResult.getOrNull()
        if (redeemed != null) {
            val remoteRole = when (redeemed.role) {
                JoinRole.AUXILIAR -> UserProfileType.AUXILIAR
                JoinRole.ESPECTADOR -> UserProfileType.ESPECTADOR
            }
            joinRemoteGroup(
                cloudGroupId = redeemed.cloudGroupId,
                role = remoteRole,
                displayCode = trimmed,
                remoteGroupName = redeemed.groupName,
                onResult = onResult
            )
            return@launch
        }

        if (BuildConfig.DEBUG) {
            val upper = trimmed.uppercase(Locale.getDefault())
            val debugRole = when {
                upper.startsWith("AUX") -> UserProfileType.AUXILIAR
                upper.startsWith("ESP") -> UserProfileType.ESPECTADOR
                else -> null
            }
            if (debugRole != null) {
                joinRemoteGroup(cloudGroupId = upper, role = debugRole, displayCode = upper, onResult = onResult)
                return@launch
            }
        }

        onResult(remoteResult.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.join_group_invalid_code))
    }

    private suspend fun joinRemoteGroup(
        cloudGroupId: String,
        role: UserProfileType,
        displayCode: String,
        remoteGroupName: String? = null,
        onResult: (String?) -> Unit
    ) {
        val baseName = normalizeGroupName(
            remoteGroupName?.takeIf { it.isNotBlank() }
                ?: getApplication<Application>().getString(R.string.join_group_remote_group_name, displayCode)
        )
        // Se já existir um grupo local com o mesmo nome (ex.: dois grupos remotos com nomes
        // iguais, ou colisão com um grupo próprio), acrescenta um sufixo numérico até achar um
        // nome livre, em vez de bloquear a entrada ou sobrescrever o grupo existente.
        var groupName = baseName
        var suffix = 2
        while (repository.getGroupConfig(groupName) != null) {
            val candidate = "$baseName ($suffix)"
            groupName = normalizeGroupName(candidate)
            suffix++
            if (suffix > 50) {
                onResult(getApplication<Application>().getString(R.string.join_group_already_joined))
                return
            }
        }
        val cfg = GroupConfig(
            groupName = groupName,
            onboardingStep = ONBOARDING_STEP_COMPLETE,
            isCloudSynced = true,
            cloudGroupId = cloudGroupId,
            remoteRole = role.name
        )
        repository.saveGroupConfig(cfg)
        loadGroupConfig(groupName)
        TelemetryManager.logMemberJoinedViaCode(getApplication(), role.name)
        onResult(null)
    }

    /** Desconecta este dispositivo de um grupo remoto (Auxiliar/Espectador), sem afetar o grupo
     *  para os demais membros — equivalente a [deleteGroup], mas usado apenas pela UI de grupos
     *  de outra pessoa (botão vermelho "Sair", no lugar de "Apagar grupo"). */
    fun leaveRemoteGroup(groupName: String) = deleteGroup(groupName)

    /**
     * Organizador solicita transferir a posse de um grupo premium sincronizado para um(a)
     * Auxiliar (identificado por e-mail), que precisa também ser premium. Sem o backend de
     * sincronização, isso fica registrado localmente como uma intenção pendente
     * ([GroupConfig.pendingOwnershipTransferTo]) — a efetivação real (trocar quem é o
     * organizador oficial nos demais dispositivos) exige a Cloud Function correspondente.
     */
    fun requestGroupOwnershipTransfer(groupName: String, targetEmail: String) = viewModelScope.launch(Dispatchers.IO) {
        val target = repository.getGroupConfig(groupName) ?: return@launch
        if (target.remoteRole != null || !target.isCloudSynced) {
            showMessage(getApplication<Application>().getString(R.string.ownership_transfer_requires_own_premium_group))
            return@launch
        }
        val normalizedEmail = targetEmail.trim()
        if (normalizedEmail.isBlank()) return@launch
        repository.saveGroupConfig(target.copy(pendingOwnershipTransferTo = normalizedEmail))
        if (_currentGroupConfig.value.groupName == groupName) {
            _currentGroupConfig.value = _currentGroupConfig.value.copy(pendingOwnershipTransferTo = normalizedEmail)
        }
        showMessage(getApplication<Application>().getString(R.string.ownership_transfer_requested))
    }

    fun cancelGroupOwnershipTransfer(groupName: String) = viewModelScope.launch(Dispatchers.IO) {
        val target = repository.getGroupConfig(groupName) ?: return@launch
        repository.saveGroupConfig(target.copy(pendingOwnershipTransferTo = null))
        if (_currentGroupConfig.value.groupName == groupName) {
            _currentGroupConfig.value = _currentGroupConfig.value.copy(pendingOwnershipTransferTo = null)
        }
    }

    private val _teamsSwapped = MutableStateFlow(false)
    val teamsSwapped: StateFlow<Boolean> = _teamsSwapped.asStateFlow()
    fun toggleTeamsSwapped() { _teamsSwapped.value = !_teamsSwapped.value }

    /** Sinaliza para a UI que é um bom momento para solicitar o fluxo de review do Play. */
    private val _shouldRequestReview = MutableStateFlow(false)
    val shouldRequestReview: StateFlow<Boolean> = _shouldRequestReview.asStateFlow()

    // --- Controle de descanso e rodadas ---
    private val _roundCounter = MutableStateFlow(0)
    val roundCounter: StateFlow<Int> = _roundCounter.asStateFlow()

    // Mapa: playerId -> rodada de retorno (inclusive)
    private val _restingPlayers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val restingPlayers = _restingPlayers.asStateFlow()

    private fun markPlayersResting(players: List<Player>, rounds: Int = 1) {
        if (players.isEmpty()) return
        val returnRound = _roundCounter.value + rounds
        val result = applyRestingMark(
            currentResting = _restingPlayers.value,
            currentWaiting = _waitingList.value,
            playersToRest = players.map { applyTollIfNecessary(it) },
            returnRound = returnRound
        )
        _restingPlayers.value = result.restingPlayers
        _waitingList.value = result.waitingList
    }

    private fun collectAndClearReturningPlayers(): List<Player> {
        val result = resolveReturningPlayers(
            currentResting = _restingPlayers.value,
            currentWaiting = _waitingList.value,
            roundCounter = _roundCounter.value
        )
        if (result.returningIds.isEmpty()) return emptyList()
        val returning = result.returningIds
            .mapNotNull { id ->
                currentGroupPlayers.value.find { it.id == id }
                    ?.takeIf { _presentPlayerIds.value.contains(it.id) }
            }
        _restingPlayers.value = result.restingPlayers
        _waitingList.value = result.waitingList
        return returning
    }

    /**
     * Se houver um time completo voltando do descanso, monta uma partida imediata
     * contra o time que estava reinando na rodada anterior (_lastWinners).
     * Retorna true quando a partida já foi formada e não precisa seguir a lógica padrão.
     */
    private fun tryScheduleReturningTeamMatchIfAny(conf: GroupConfig): Boolean {
        val returningPlayers = collectAndClearReturningPlayers()
        if (returningPlayers.isEmpty()) return false

        val teamSize = conf.teamSize
        val returningTeamPlayers = returningPlayers.take(teamSize).map { applyTollIfNecessary(it) }.toMutableList()
        if (returningTeamPlayers.size < teamSize) {
            val missing = teamSize - returningTeamPlayers.size
            val substitutePlayers = _waitingList.value
                .asSequence()
                .filterNot { _restingPlayers.value.containsKey(it.id) }
                .take(missing)
                .toList()
            if (substitutePlayers.isNotEmpty()) {
                val substituteIds = substitutePlayers.map { it.id }.toSet()
                returningTeamPlayers.addAll(substitutePlayers.map { applyTollIfNecessary(it) })
                _waitingList.value = _waitingList.value.filterNot { substituteIds.contains(it.id) }
            }
        }
        if (returningTeamPlayers.size < teamSize) {
            _waitingList.value = (returningTeamPlayers + _waitingList.value).distinctBy { it.id }
            return false
        }

        // O time adversário é o time reinante da rodada em que eles descansaram,
        // guardado em _lastWinners e filtrado apenas pelos jogadores ainda presentes.
        val reigningWinnerPlayers = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val opposingTeamPlayers = mutableListOf<Player>()
        opposingTeamPlayers.addAll(reigningWinnerPlayers.take(teamSize))
        var replacedMissingWinnerWithWaitingPlayer = false

        // Se o time reinante não completar a quadra, complementa com a waitingList.
        if (opposingTeamPlayers.size < teamSize) {
            val needed = teamSize - opposingTeamPlayers.size
            val picks = _waitingList.value.take(needed)
            opposingTeamPlayers.addAll(picks)
            _waitingList.value = _waitingList.value.drop(needed)
            if (picks.isNotEmpty()) replacedMissingWinnerWithWaitingPlayer = true
        }

        // Se ainda faltarem jogadores, devolve o time retornante para a fila de espera.
        if (opposingTeamPlayers.size < teamSize) {
            // Mantém o time retornante descansando por mais uma rodada.
            markPlayersResting(returningTeamPlayers, rounds = 1)
            return false
        }

        // Preserva os perdedores da rodada anterior na fila antes de agendar a partida imediata.
        val previousLoserIds = lastLosers
            .filter { _presentPlayerIds.value.contains(it.id) }
            .map { it.id }
            .toSet()
        val returningTeamIds = returningTeamPlayers.map { it.id }.toSet()
        val opposingTeamIds = opposingTeamPlayers.map { it.id }.toSet()
        val previousLosers = lastLosers
            .filter { _presentPlayerIds.value.contains(it.id) }
            .filterNot { returningTeamIds.contains(it.id) || opposingTeamIds.contains(it.id) }
            .map { applyTollIfNecessary(it) }
        _waitingList.value = (
            _waitingList.value.filterNot { returningTeamIds.contains(it.id) || opposingTeamIds.contains(it.id) } + previousLosers
        ).distinctBy { it.id }

        // O time que voltou do descanso ocupa o lado oposto ao time reinante.
        // Se o reinante está no lado A, o retornante entra como B; se está no B, entra como A.
        val returningTeamPlaysAsA = when (_streakOwner.value) {
            "A" -> false
            "B" -> true
            else -> true
        }

        if (returningTeamPlaysAsA) {
            _teamA.value = sortTeamPlayers(returningTeamPlayers)
            _teamB.value = sortTeamPlayers(opposingTeamPlayers)
        } else {
            _teamA.value = sortTeamPlayers(opposingTeamPlayers)
            _teamB.value = sortTeamPlayers(returningTeamPlayers)
        }
        _autoSelectedLoserPlayerIds.value = (_teamA.value + _teamB.value)
            .map { it.id }
            .filter { previousLoserIds.contains(it) }
            .toSet()
        _hasPreviousMatch.value = false
        resetScoresAndPointIndicator()
        if (replacedMissingWinnerWithWaitingPlayer) {
            _currentStreak.value = 0
            _streakOwner.value = null
        }
        _currentMatchStartTimestamp.value = System.currentTimeMillis()

        refreshPositionAssignments()
        return true
    }

    private val _teamA = MutableStateFlow<List<Player>>(emptyList())
    val teamA = _teamA.asStateFlow()
    private val _teamB = MutableStateFlow<List<Player>>(emptyList())
    val teamB = _teamB.asStateFlow()
    private val _waitingList = MutableStateFlow<List<Player>>(emptyList())
    val waitingList = _waitingList.asStateFlow()
    private val _presentPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val presentPlayerIds = _presentPlayerIds.asStateFlow()

    /** Jogadores marcados como presentes/selecionados, sincronizados via [LiveGameState.presentPlayers]
     *  para dispositivos que entraram como Auxiliar/Espectador (sem roster real no Room). Alimenta a
     *  tela "Jogo (Ao vivo)" antes de uma partida começar (ver `spectator-player-list-visible`). */
    private val _remoteSelectedPlayers = MutableStateFlow<List<Player>>(emptyList())
    val remoteSelectedPlayers = _remoteSelectedPlayers.asStateFlow()

    /** Elenco completo (sem filtro de presença) do grupo remoto, sincronizado via
     *  [LiveGameState.allPlayers] — usado pelo Auxiliar para ver a lista de jogadores completa do
     *  grupo, sem as restrições de edição do Espectador (ver `aux-full-roster-sync`). */
    private val _remoteAllPlayers = MutableStateFlow<List<Player>>(emptyList())
    val remoteAllPlayers = _remoteAllPlayers.asStateFlow()

    /** Posição ocupada por cada jogador na partida atual. Vazio fora do Modo Posições Fixas. */
    private val _assignedPositions = MutableStateFlow<Map<Int, PlayerPosition>>(emptyMap())
    val assignedPositions = _assignedPositions.asStateFlow()
    private val _assignedSlotIndices = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val assignedSlotIndices = _assignedSlotIndices.asStateFlow()

    /** `true` quando algum time em quadra não cumpre a composição mínima de posições. */
    private val _compositionIncomplete = MutableStateFlow(false)
    val compositionIncomplete = _compositionIncomplete.asStateFlow()

    private val _scoreA = MutableStateFlow(0)
    val scoreA = _scoreA.asStateFlow()
    private val _scoreB = MutableStateFlow(0)
    val scoreB = _scoreB.asStateFlow()
    private val _lastScoringTeam = MutableStateFlow<String?>(null)
    val lastScoringTeam = _lastScoringTeam.asStateFlow()
    private val _rotationRequiredForTeam = MutableStateFlow<String?>(null)
    val rotationRequiredForTeam = _rotationRequiredForTeam.asStateFlow()

    private val _hasPreviousMatch = MutableStateFlow(false)
    val hasPreviousMatch = _hasPreviousMatch.asStateFlow()
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()
    private val _streakOwner = MutableStateFlow<String?>(null)
    val streakOwner = _streakOwner.asStateFlow()
    private val _lastWinners = MutableStateFlow<List<Player>>(emptyList())
    val lastWinners = _lastWinners.asStateFlow()
    private val _rebalancedPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val rebalancedPlayerIds = _rebalancedPlayerIds.asStateFlow()
    private val _autoSelectedLoserPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val autoSelectedLoserPlayerIds = _autoSelectedLoserPlayerIds.asStateFlow()
    private val _guaranteedNextMatchPlayerIds = MutableStateFlow<List<Int>>(emptyList())
    val guaranteedNextMatchPlayerIds = _guaranteedNextMatchPlayerIds.asStateFlow()
    private val _manualStreakAdjustments = MutableStateFlow<List<ManualStreakAdjustmentLog>>(emptyList())
    val manualStreakAdjustments = _manualStreakAdjustments.asStateFlow()
    private val _manualSubstitutions = MutableStateFlow<List<ManualSubstitutionLog>>(emptyList())
    val manualSubstitutions = _manualSubstitutions.asStateFlow()
    private var lastLosers: List<Player> = emptyList()
    private val _currentMatchStartTimestamp = MutableStateFlow<Long?>(null)

    // Controla quando a persistência do estado de jogo fica ativa (após a primeira carga do grupo)
    private var persistenceReady = false
    private val maxManualStreakLogsInMemory = 50
    private val maxManualSubstitutionLogsInMemory = 50

    init {
        loadPreferences()
        viewModelScope.launch {
            val existingGroups = repository.getAllGroupNames()
            val initialGroup = existingGroups.firstOrNull()
            if (initialGroup != null) {
                loadGroupConfig(initialGroup)
            } else {
                // Instalação nova: não persiste nenhum grupo até o usuário confirmar um nome válido.
                startFreshGroupOnboarding()
            }
        }
        observeAndPersistGameState()
        observeAndPushCloudLiveState()
        observeAndMirrorRemoteLiveState()
        observeRemoteGroupVisibility()
        viewModelScope.launch {
            availableHistoryDates.collect { dates ->
                if (_historyDateFilter.value == null && dates.isNotEmpty()) _historyDateFilter.value =
                    dates.first()
            }
        }
    }

    // Observa todos os fluxos do estado de jogo e persiste a cada alteração
    private fun observeAndPersistGameState() {
        viewModelScope.launch {
            combine(
                _teamA, _teamB, _waitingList, _presentPlayerIds, _scoreA
            ) { _, _, _, _, _ -> }.collect { if (persistenceReady) saveGameState() }
        }
        viewModelScope.launch {
            combine(
                _scoreB, _currentStreak, _hasPreviousMatch, _lastWinners, _streakOwner
            ) { _, _, _, _, _ -> }.collect { if (persistenceReady) saveGameState() }
        }
        viewModelScope.launch {
            combine(_lastScoringTeam, _rotationRequiredForTeam) { _, _ -> }
                .collect { if (persistenceReady) saveGameState() }
        }
        // Persiste também os jogadores em descanso e o contador de rodadas
        viewModelScope.launch {
            combine(
                _restingPlayers,
                _roundCounter,
                _rebalancedPlayerIds,
                _autoSelectedLoserPlayerIds,
                _guaranteedNextMatchPlayerIds
            ) { _, _, _, _, _ -> }.collect { if (persistenceReady) saveGameState() }
        }
        viewModelScope.launch {
            combine(_assignedPositions, _assignedSlotIndices, _compositionIncomplete) { _, _, _ -> }
                .collect { if (persistenceReady) saveGameState() }
        }

        // Sempre que os times mudarem, remove esses jogadores do mapa de descanso e garante que a waitingList não tenha duplicados nem jogadores em quadra
        viewModelScope.launch {
            combine(_teamA, _teamB) { a, b ->
                val ids = (a.map { it.id } + b.map { it.id }).toSet()
                ids
            }.collect { ids ->
                if (ids.isNotEmpty()) {
                    // Limpa as marcações de descanso dos jogadores que estão em quadra agora
                    val reduced = _restingPlayers.value.filterKeys { !ids.contains(it) }
                    if (reduced.size != _restingPlayers.value.size) _restingPlayers.value = reduced
                    // Remove da waitingList os jogadores que estão nos times e elimina duplicados
                    val dedup = _waitingList.value.filterNot { ids.contains(it.id) }.distinctBy { it.id }
                    if (dedup.size != _waitingList.value.size) _waitingList.value = dedup
                } else {
                    // Mantém a waitingList sem duplicados mesmo quando não há partida em andamento
                    val dedup = _waitingList.value.distinctBy { it.id }
                    if (dedup.size != _waitingList.value.size) _waitingList.value = dedup
                }
            }
        }
    }

    /**
     * Motor de sincronização (`firestore-sync-engine`): sempre que o grupo local ativo for o
     * grupo **próprio** deste dispositivo ([GroupConfig.remoteRole] nulo) e estiver premium
     * sincronizado ([GroupConfig.isCloudSynced] com [GroupConfig.cloudGroupId] não nulo), publica
     * o estado do jogo em andamento (times, fila, placar, sequência) em
     * `cloudGroups/{cloudGroupId}/liveState/current`, para que auxiliares/espectadores que
     * entraram via código vejam em tempo real (ver [CloudSyncManager.pushLiveState]). Usa
     * [kotlinx.coroutines.flow.debounce] para não disparar uma escrita por tecla em sequências
     * rápidas de toques no placar.
     */
    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    private fun observeAndPushCloudLiveState() {
        data class LiveGameStatePartial(
            val teamA: List<Player>,
            val teamB: List<Player>,
            val waitingList: List<Player>,
            val scoreA: Int,
            val scoreB: Int,
            val presentPlayerIds: Set<Int>
        )
        val partialFlow = combine(
            combine(_teamA, _teamB, _waitingList) { teamA, teamB, waiting -> Triple(teamA, teamB, waiting) },
            _scoreA, _scoreB, _presentPlayerIds
        ) { teams, scoreA, scoreB, presentIds ->
            LiveGameStatePartial(teams.first, teams.second, teams.third, scoreA, scoreB, presentIds)
        }
        // Campos extras que também dependem de um Player.id real (elenco completo, marcador de
        // ponto/rodízio, início da partida) são agrupados aqui para não estourar o limite de 5
        // flows tipadas do combine() abaixo.
        val extraFlow = combine(_currentMatchStartTimestamp, _lastScoringTeam, _rotationRequiredForTeam) { ts, lastTeam, rotation ->
            Triple(ts, lastTeam, rotation)
        }
        val groupPlayersAndExtra = combine(currentGroupPlayers, extraFlow) { players, extra -> players to extra }
        viewModelScope.launch {
            combine(_currentGroupConfig, partialFlow, _currentStreak, _streakOwner, groupPlayersAndExtra) { config, partial, streak, owner, playersAndExtra ->
                val groupPlayers = playersAndExtra.first
                val (matchStartTimestamp, lastScoringTeam, rotationRequiredForTeam) = playersAndExtra.second
                // Organizador (grupo próprio) e Auxiliar (`remoteRole == "AUXILIAR"`) publicam o
                // estado ao vivo; Espectador nunca escreve (fica só na ponta de leitura abaixo).
                val canPush = config.isCloudSynced && config.cloudGroupId != null &&
                    (config.remoteRole == null || config.remoteRole == UserProfileType.AUXILIAR.name)
                if (canPush) {
                    // Presença/seleção pré-partida e elenco completo: só o organizador tem o
                    // roster real no Room para calculá-los; o Auxiliar ecoa o último valor
                    // observado, para não apagar essas listas com um valor vazio ao publicar sua
                    // própria edição de placar/times.
                    val presentSnapshots: List<RemotePlayerSnapshot>
                    val allPlayerSnapshots: List<RemotePlayerSnapshot>
                    if (config.remoteRole == null) {
                        presentSnapshots = partial.presentPlayerIds.mapNotNull { id -> groupPlayers.find { it.id == id } }
                            .map { it.toRemoteSnapshot() }
                        allPlayerSnapshots = groupPlayers.map { it.toRemoteSnapshot() }
                    } else {
                        presentSnapshots = lastKnownRemotePresentPlayers
                        allPlayerSnapshots = lastKnownRemoteAllPlayers
                    }
                    config.cloudGroupId to LiveGameState(
                        groupName = config.groupName,
                        teamA = partial.teamA.map { it.toRemoteSnapshot() },
                        teamB = partial.teamB.map { it.toRemoteSnapshot() },
                        waitingList = partial.waitingList.map { it.toRemoteSnapshot() },
                        scoreA = partial.scoreA,
                        scoreB = partial.scoreB,
                        currentStreak = streak,
                        streakOwner = owner,
                        updatedAt = System.currentTimeMillis(),
                        presentPlayers = presentSnapshots,
                        allPlayers = allPlayerSnapshots,
                        matchStartTimestamp = matchStartTimestamp,
                        lastScoringTeam = lastScoringTeam,
                        rotationRequiredForTeam = rotationRequiredForTeam
                    )
                } else null
            }.debounce(400).collect { pushable ->
                if (pushable != null) {
                    lastPushedUpdatedAt = pushable.second.updatedAt
                    lastKnownRemotePresentPlayers = pushable.second.presentPlayers
                    lastKnownRemoteAllPlayers = pushable.second.allPlayers
                    CloudSyncManager.pushLiveState(pushable.first, pushable.second)
                }
            }
        }
    }

    /**
     * Espelha o [LiveGameState] em nuvem de volta para as flows locais de jogo (`_teamA`,
     * `_teamB`, `_waitingList`, `_scoreA`, `_scoreB`, `_currentStreak`, `_streakOwner`) que
     * alimentam a mesma [com.bismarck.voleimanager.app.ui.game.GameScreenContent] usada
     * localmente — é isso que permite ao Auxiliar/Espectador reaproveitarem toda a UI rica do
     * jogo (ver `aux-bidirectional-sync`), e ao organizador enxergar em tempo real uma edição
     * feita por um Auxiliar em outro aparelho.
     *
     * Descarta qualquer estado remoto que seja só o eco da última publicação deste próprio
     * dispositivo (comparando `updatedAt` com [lastPushedUpdatedAt]), já que não há um id de
     * dispositivo separado nesta v1 (ver decisão de "echo-suppression" do plano).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAndMirrorRemoteLiveState() {
        viewModelScope.launch {
            _currentGroupConfig
                .map { it.cloudGroupId }
                .distinctUntilChanged()
                .flatMapLatest { cloudGroupId ->
                    if (cloudGroupId == null) flowOf(null) else CloudSyncManager.observeLiveState(cloudGroupId)
                }
                .collect { state ->
                    if (state == null) return@collect
                    if (state.updatedAt <= lastPushedUpdatedAt) return@collect
                    val config = _currentGroupConfig.value
                    if (config.cloudGroupId == null) return@collect

                    lastKnownRemotePresentPlayers = state.presentPlayers
                    lastKnownRemoteAllPlayers = state.allPlayers
                    _remoteAllPlayers.value = state.allPlayers.map { it.toSyntheticPlayer(config.groupName) }
                    // Sincronizado para todos os papéis (organizador, auxiliar, espectador): quem
                    // publicou o valor mais recente já é o único responsável por "ser dono" dele
                    // (organizador para o início da partida, quem tocou no placar por último para
                    // marcador de ponto/rodízio), então basta espelhar sem checagem extra de papel.
                    _currentMatchStartTimestamp.value = state.matchStartTimestamp
                    _lastScoringTeam.value = state.lastScoringTeam
                    _rotationRequiredForTeam.value = state.rotationRequiredForTeam

                    if (config.remoteRole == null) {
                        // Organizador: remapeia os snapshots remotos para os Players reais deste
                        // aparelho via publicId (só aceita se todos os jogadores citados existirem
                        // localmente, para não "sumir" gente da tela por uma corrida de sincronismo).
                        val localByPublicId = currentGroupPlayers.value.associateBy { it.publicId }
                        fun mapBack(list: List<RemotePlayerSnapshot>): List<Player>? =
                            list.map { localByPublicId[it.publicId] ?: return null }
                        val mappedA = mapBack(state.teamA)
                        val mappedB = mapBack(state.teamB)
                        val mappedWait = mapBack(state.waitingList)
                        if (mappedA != null && mappedB != null && mappedWait != null) {
                            _teamA.value = mappedA
                            _teamB.value = mappedB
                            _waitingList.value = mappedWait
                            _scoreA.value = state.scoreA
                            _scoreB.value = state.scoreB
                            _currentStreak.value = state.currentStreak
                            _streakOwner.value = state.streakOwner
                        }
                        val requestId = state.pendingFinishRequestId
                        val requestWinner = state.pendingFinishWinner
                        if (requestId != null && requestId != lastProcessedFinishRequestId && requestWinner != null) {
                            lastProcessedFinishRequestId = requestId
                            finishGame(requestWinner)
                        }
                        val presenceRequestId = state.pendingPresenceToggleRequestId
                        val presencePublicId = state.pendingPresenceTogglePublicId
                        if (presenceRequestId != null && presenceRequestId != lastProcessedPresenceToggleRequestId && presencePublicId != null) {
                            lastProcessedPresenceToggleRequestId = presenceRequestId
                            currentGroupPlayers.value.find { it.publicId == presencePublicId }?.let { togglePlayerPresence(it) }
                        }
                    } else {
                        // Auxiliar/Espectador: não existem Players reais no Room local deste
                        // aparelho para o grupo remoto, então reconstruímos objetos sintéticos.
                        _teamA.value = state.teamA.map { it.toSyntheticPlayer(config.groupName) }
                        _teamB.value = state.teamB.map { it.toSyntheticPlayer(config.groupName) }
                        _waitingList.value = state.waitingList.map { it.toSyntheticPlayer(config.groupName) }
                        _scoreA.value = state.scoreA
                        _scoreB.value = state.scoreB
                        _currentStreak.value = state.currentStreak
                        _streakOwner.value = state.streakOwner
                    }
                    _remoteSelectedPlayers.value = state.presentPlayers.map { it.toSyntheticPlayer(config.groupName) }
                }
        }
    }

    /**
     * Mantém o espelho local de visibilidade ([GroupConfig.shareHistoryWithObservers]/
     * [GroupConfig.showEloToObservers]) em dia com o documento em nuvem do grupo ativo, para que
     * uma mudança feita em outro dispositivo (ex.: organizador ligando o toggle pelo celular
     * enquanto o auxiliar está com o app aberto no tablet) reflita aqui sem precisar reabrir o
     * app.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRemoteGroupVisibility() {
        viewModelScope.launch {
            _currentGroupConfig
                .map { it.cloudGroupId }
                .distinctUntilChanged()
                .flatMapLatest { cloudGroupId ->
                    if (cloudGroupId == null) flowOf(null) else CloudSyncManager.observeGroupVisibility(cloudGroupId)
                }
                .collect { visibility ->
                    if (visibility == null) return@collect
                    val current = _currentGroupConfig.value
                    if (current.cloudGroupId == null) return@collect
                    val newGroupType = visibility.groupType ?: current.groupType
                    val newBalancingMode = visibility.balancingMode ?: current.balancingMode
                    val newTeamSize = visibility.teamSize ?: current.teamSize
                    if (current.shareHistoryWithObservers != visibility.shareHistoryWithObservers ||
                        current.showEloToObservers != visibility.showEloToObservers ||
                        current.groupType != newGroupType ||
                        current.balancingMode != newBalancingMode ||
                        current.teamSize != newTeamSize
                    ) {
                        val updated = current.copy(
                            shareHistoryWithObservers = visibility.shareHistoryWithObservers,
                            showEloToObservers = visibility.showEloToObservers,
                            groupType = newGroupType,
                            balancingMode = newBalancingMode,
                            teamSize = newTeamSize
                        )
                        _currentGroupConfig.value = updated
                        repository.saveGroupConfig(updated)
                    }
                }
        }
    }

    /** Grupo remoto observado ao vivo (times, placar, fila) quando este dispositivo entrou via
     *  código de Auxiliar/Espectador ([GroupConfig.remoteRole] não nulo) — usado pela tela "Ao
     *  vivo" no lugar da engine local de jogo, que não roda para grupos de outra pessoa. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val remoteLiveGameState: StateFlow<LiveGameState?> = _currentGroupConfig
        .map { config -> config.takeIf { it.remoteRole != null }?.cloudGroupId }
        .distinctUntilChanged()
        .flatMapLatest { cloudGroupId ->
            if (cloudGroupId == null) flowOf(null) else CloudSyncManager.observeLiveState(cloudGroupId)
        }
        .stateIn(viewModelScope, screenDataSharing, null)

    /** Histórico de partidas do grupo remoto ativo. Auxiliar sempre vê tudo (regras do Firestore já
     *  concedem acesso total a `canManageGroupContent`); Espectador só vê quando o organizador/
     *  auxiliar ligou [GroupConfig.shareHistoryWithObservers]. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val remoteHistory: StateFlow<List<RemoteHistoryEntry>> = _currentGroupConfig
        .flatMapLatest { config ->
            val cloudGroupId = config.cloudGroupId
            val allowed = config.remoteRole == UserProfileType.AUXILIAR.name || config.shareHistoryWithObservers
            if (config.remoteRole != null && cloudGroupId != null && allowed) {
                CloudSyncManager.observeHistory(cloudGroupId)
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    /** Ranking de Elo do grupo remoto ativo. Auxiliar sempre vê tudo; Espectador só quando o
     *  organizador/auxiliar ligou tanto [GroupConfig.shareHistoryWithObservers] quanto
     *  [GroupConfig.showEloToObservers]. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val remoteEloLogs: StateFlow<List<RemoteEloLogEntry>> = _currentGroupConfig
        .flatMapLatest { config ->
            val cloudGroupId = config.cloudGroupId
            val allowed = config.remoteRole == UserProfileType.AUXILIAR.name ||
                (config.shareHistoryWithObservers && config.showEloToObservers)
            if (config.remoteRole != null && cloudGroupId != null && allowed) {
                CloudSyncManager.observeEloLogs(cloudGroupId)
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, screenDataSharing, emptyList())

    /**
     * Liga/desliga, para o grupo premium sincronizado [groupName], a visibilidade de histórico
     * ([shareHistory]) e de ranking de Elo ([showElo]) para espectadores (`observer-visibility-controls`).
     * Permitido a organizador (grupo próprio) e auxiliar (grupo remoto, `remoteRole == "AUXILIAR"`)
     * — nunca a espectador. Grava localmente de imediato e envia ao Firestore em segundo plano
     * (best-effort, ver [CloudSyncManager.setGroupVisibility]).
     */
    fun setGroupVisibility(groupName: String, shareHistory: Boolean, showElo: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val target = repository.getGroupConfig(groupName) ?: return@launch
        if (target.remoteRole == UserProfileType.ESPECTADOR.name) return@launch
        val cloudGroupId = target.cloudGroupId ?: return@launch
        val effectiveShowElo = showElo && shareHistory
        val updated = target.copy(shareHistoryWithObservers = shareHistory, showEloToObservers = effectiveShowElo)
        repository.saveGroupConfig(updated)
        if (_currentGroupConfig.value.groupName == groupName) {
            _currentGroupConfig.value = _currentGroupConfig.value.copy(
                shareHistoryWithObservers = shareHistory,
                showEloToObservers = effectiveShowElo
            )
        }
        val error = CloudSyncManager.setGroupVisibility(cloudGroupId, shareHistory, effectiveShowElo)
        if (error != null) showMessage(error)
    }

    private fun saveGameState() {
        val snapshot = GameStateSnapshot(
            groupName = _currentGroupConfig.value.groupName,
            teamA = _teamA.value,
            teamB = _teamB.value,
            waitingList = _waitingList.value,
            presentPlayerIds = _presentPlayerIds.value.toList(),
            scoreA = _scoreA.value,
            scoreB = _scoreB.value,
            currentStreak = _currentStreak.value,
            streakOwner = _streakOwner.value,
            hasPreviousMatch = _hasPreviousMatch.value,
            lastWinners = _lastWinners.value,
            lastLosers = lastLosers,
            currentMatchStartTimestamp = _currentMatchStartTimestamp.value,
            roundCounter = _roundCounter.value,
            restingPlayers = _restingPlayers.value,
            rebalancedPlayerIds = _rebalancedPlayerIds.value.toList(),
            autoSelectedLoserPlayerIds = _autoSelectedLoserPlayerIds.value.toList(),
            guaranteedNextMatchPlayerIds = _guaranteedNextMatchPlayerIds.value,
            lastScoringTeam = _lastScoringTeam.value,
            rotationRequiredForTeam = _rotationRequiredForTeam.value,
            assignedPositions = _assignedPositions.value.mapValues { it.value.name },
            assignedSlotIndices = _assignedSlotIndices.value,
            compositionIncomplete = _compositionIncomplete.value
        )
        // If nothing meaningful is happening, clear instead of saving
        if (!snapshot.hasPreviousMatch && snapshot.teamA.isEmpty() && snapshot.teamB.isEmpty()) {
            clearSavedGameState(snapshot.groupName)
            return
        }
        val json = Gson().toJson(snapshot)
        getApplication<Application>()
            .getSharedPreferences("volei", Context.MODE_PRIVATE)
            .edit().putString("game_state_${snapshot.groupName}", json).apply()
    }

    private fun clearSavedGameState(groupName: String = _currentGroupConfig.value.groupName) {
        getApplication<Application>()
            .getSharedPreferences("volei", Context.MODE_PRIVATE)
            .edit().remove("game_state_$groupName").apply()
    }

    private suspend fun shouldAutoClearCurrentGameByInactivity(groupName: String): Boolean {
        val latestMatchTimestamp = repository.getHistoryByGroupSync(groupName)
            .asSequence()
            .mapNotNull { match ->
                match.endTimestamp ?: match.startTimestamp ?: parseLegacyMatchDate(match.date)
            }
            .maxOrNull()
            ?: return false
        return (System.currentTimeMillis() - latestMatchTimestamp) >= AUTO_CLEAR_GAME_AFTER_LAST_MATCH_MS
    }

    private fun parseLegacyMatchDate(rawDate: String): Long? {
        val text = rawDate.trim()
        if (text.isBlank()) return null
        val patterns = listOf(
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val parsed = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                    isLenient = false
                }.parse(text)
                if (parsed != null) return parsed.time
            } catch (_: ParseException) {
                // Ignore and try the next legacy format.
            }
        }
        return null
    }

    private fun tryRestoreGameState(groupName: String): Boolean {
        val json = getApplication<Application>()
            .getSharedPreferences("volei", Context.MODE_PRIVATE)
            .getString("game_state_$groupName", null) ?: return false
        return try {
            val snapshot = Gson().fromJson(json, GameStateSnapshot::class.java)
            if (snapshot == null || snapshot.groupName != groupName) return false
            _teamA.value = snapshot.teamA
            _teamB.value = snapshot.teamB
            _waitingList.value = snapshot.waitingList
            _presentPlayerIds.value = snapshot.presentPlayerIds.toSet()
            _scoreA.value = snapshot.scoreA
            _scoreB.value = snapshot.scoreB
            _currentStreak.value = snapshot.currentStreak
            _streakOwner.value = snapshot.streakOwner
            _hasPreviousMatch.value = snapshot.hasPreviousMatch
            _lastWinners.value = snapshot.lastWinners
            lastLosers = snapshot.lastLosers
            _currentMatchStartTimestamp.value = snapshot.currentMatchStartTimestamp
            _roundCounter.value = snapshot.roundCounter
            _restingPlayers.value = snapshot.restingPlayers
            _rebalancedPlayerIds.value = snapshot.rebalancedPlayerIds.toSet()
            _autoSelectedLoserPlayerIds.value = snapshot.autoSelectedLoserPlayerIds.toSet()
            _guaranteedNextMatchPlayerIds.value = snapshot.guaranteedNextMatchPlayerIds
            _lastScoringTeam.value = snapshot.lastScoringTeam
            _rotationRequiredForTeam.value = snapshot.rotationRequiredForTeam
            _assignedPositions.value = (snapshot.assignedPositions ?: emptyMap())
                .mapNotNull { (id, name) ->
                    PlayerPosition.fromStoredValue(name)?.let { id to it }
                }
                .toMap()
            _assignedSlotIndices.value = snapshot.assignedSlotIndices ?: emptyMap()
            _compositionIncomplete.value = snapshot.compositionIncomplete
            Log.d("GameState", "Estado do jogo restaurado para grupo '$groupName'")
            true
        } catch (e: Exception) {
            Log.e("GameState", "Erro ao restaurar estado do jogo: ${e.message}")
            false
        }
    }

    fun setHistoryDateFilter(d: String?) {
        _historyDateFilter.value = d
    }

    fun setThemeMode(m: ThemeMode) {
        _themeMode.value = m
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putString("theme", m.name).apply()
    }

    fun setShowElo(show: Boolean) {
        _showElo.value = show
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("show_elo", show).apply()
    }

    fun setShowToll(show: Boolean) {
        _showToll.value = show
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("show_toll", show).apply()
    }

    fun setSupporter(isSupporter: Boolean) {
        _isSupporter.value = isSupporter
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("is_supporter", isSupporter).apply()
    }

    /**
     * Registra a escolha de consentimento de telemetria (opt-in/opt-out), liga/desliga a coleta
     * no [TelemetryManager] e marca que o diálogo já foi respondido (não é mostrado de novo
     * automaticamente, mas pode ser reaberto pelo menu para revisão).
     */
    fun setTelemetryEnabled(enabled: Boolean) {
        _telemetryEnabled.value = enabled
        _showTelemetryConsentPrompt.value = false
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean(TelemetryManager.PREF_KEY_TELEMETRY_ENABLED, enabled).apply()
        TelemetryManager.applyConsent(getApplication(), enabled)
    }

    /**
     * Define as cores **oficiais** do grupo atual (Time A/B), definidas pelo organizador ou
     * auxiliar premium — passam a valer para todos que visualizam este grupo, inclusive
     * observadores sem premium (a menos que eles próprios tenham premium e uma sobreposição
     * pessoal ativa, ver [setPersonalTeamColorOverride]). Só tem efeito se o usuário tiver acesso
     * premium ([hasPremiumAccess]) e as duas cores forem diferentes entre si.
     */
    fun setGroupTeamColors(teamA: TeamAccentColor, teamB: TeamAccentColor) {
        if (!hasPremiumAccess.value || teamA == teamB) return
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            teamAColorName = teamA.name,
            teamBColorName = teamB.name
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    /**
     * Ativa uma sobreposição pessoal (só neste dispositivo) das cores de time, prevalecendo sobre
     * as cores oficiais do grupo atual — só para quem já as vê. Útil para um observador/auxiliar
     * premium que prefere outras cores sem alterar o que os demais membros do grupo enxergam. Só
     * tem efeito se o usuário tiver acesso premium e as duas cores forem diferentes entre si.
     */
    fun setPersonalTeamColorOverride(teamA: TeamAccentColor, teamB: TeamAccentColor) {
        if (!hasPremiumAccess.value || teamA == teamB) return
        _personalTeamColorOverrideEnabled.value = true
        _personalTeamAColor.value = teamA
        _personalTeamBColor.value = teamB
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("personal_team_color_override_enabled", true)
            .putString("personal_team_color_a", teamA.name)
            .putString("personal_team_color_b", teamB.name)
            .apply()
    }

    /** Desativa a sobreposição pessoal, voltando a exibir as cores oficiais do grupo atual. */
    fun clearPersonalTeamColorOverride() {
        _personalTeamColorOverrideEnabled.value = false
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("personal_team_color_override_enabled", false).apply()
    }

    /**
     * Simula (ou desliga a simulação de) uma assinatura premium ativa, **apenas em build de
     * debug** — em release essa chamada não faz nada, mesmo que a UI que a expõe não seja
     * renderizada (defesa em profundidade). Ao desligar, a sobreposição pessoal é desativada
     * (volta a valer a cor oficial do grupo) — as cores oficiais do grupo em si não são
     * apagadas, do mesmo jeito que uma assinatura real expirando não deveria descustomizar o
     * grupo para os demais membros sem uma ação explícita.
     */
    fun setDebugPremiumOverride(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        _debugPremiumOverride.value = enabled
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("debug_premium_override", enabled).apply()
        if (!enabled && !_realPremiumEntitlement.value) {
            clearPersonalTeamColorOverride()
        }
    }

    /**
     * Registra o perfil do usuário (Organizador/Auxiliar/Espectador), respondido uma única vez
     * na primeira etapa do onboarding. Organizador e Auxiliar devem ser direcionados, na UI, ao
     * fluxo de cadastro/login gratuito antes de prosseguir; Espectador segue sem essa exigência.
     */
    fun setUserProfileType(type: UserProfileType) {
        _userProfileType.value = type
        _showUserProfileOnboarding.value = false
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putString("user_profile_type", type.name).apply()
        _postProfileOnboardingStage.value = when (type) {
            UserProfileType.ORGANIZADOR, UserProfileType.AUXILIAR ->
                if (currentUser.value == null) PostProfileOnboardingStage.AUTH_REQUIRED else PostProfileOnboardingStage.NONE
            UserProfileType.ESPECTADOR -> PostProfileOnboardingStage.SPECTATOR_AUTH_SUGGESTION
        }
    }

    /** Chamado quando o usuário aperta "voltar" numa das etapas intermediárias de roteamento por
     *  perfil (gate de conta obrigatória, ou sugestões puláveis de login/código para o
     *  Espectador), caso se arrependa da escolha de perfil feita na primeira tela — volta para lá
     *  para que ele possa escolher de novo (a persistência em SharedPreferences só é
     *  sobrescrita quando [setUserProfileType] roda de novo). */
    fun returnToProfileSelection() {
        _postProfileOnboardingStage.value = PostProfileOnboardingStage.NONE
        _showUserProfileOnboarding.value = true
    }

    /** Chamado assim que o gate obrigatório de conta (Organizador/Auxiliar) é atendido — login ou
     *  cadastro concluído —, liberando o onboarding normal de grupo. */
    fun onAuthGatePassed() {
        if (_postProfileOnboardingStage.value == PostProfileOnboardingStage.AUTH_REQUIRED) {
            _postProfileOnboardingStage.value = PostProfileOnboardingStage.NONE
        }
    }

    /** Chamado quando o Espectador pula (ou conclui com sucesso) a sugestão de login/cadastro,
     *  avançando para a sugestão de código de grupo. */
    fun onSpectatorAuthStepDone() {
        if (_postProfileOnboardingStage.value == PostProfileOnboardingStage.SPECTATOR_AUTH_SUGGESTION) {
            _postProfileOnboardingStage.value = PostProfileOnboardingStage.SPECTATOR_JOIN_SUGGESTION
        }
    }

    /** Chamado quando o Espectador pula (ou conclui com sucesso) a sugestão de código de grupo,
     *  encerrando o roteamento por perfil e liberando o onboarding normal de criação de grupo. */
    fun onSpectatorJoinStepDone() {
        if (_postProfileOnboardingStage.value == PostProfileOnboardingStage.SPECTATOR_JOIN_SUGGESTION) {
            _postProfileOnboardingStage.value = PostProfileOnboardingStage.NONE
        }
    }

    /** Usado pela dica de rolagem do cabeçalho (rotação/duplo toque), exibida uma vez por grupo. */
    fun hasSeenHeaderScrollTooltip(groupName: String): Boolean {
        return getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE)
            .getBoolean("seen_header_scroll_tooltip_$groupName", false)
    }

    fun markHeaderScrollTooltipSeen(groupName: String) {
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putBoolean("seen_header_scroll_tooltip_$groupName", true).apply()
    }

    /**
     * Chamado após uma "Limpeza válida" (jogo real limpo via "Limpar jogo atual"), gatilho
     * orgânico para sugerir a avaliação do app na Play Store. Cada marco (3ª, 10ª, 25ª
     * limpeza válida) só dispara o pedido de review uma única vez; a Play Store decide
     * internamente, com sua própria cota, se o diálogo será de fato exibido ao usuário.
     */
    private fun registerQualifyingGameClear() {
        val prefs = getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE)
        val newCount = prefs.getInt("clear_match_valid_count", 0) + 1
        prefs.edit().putInt("clear_match_valid_count", newCount).apply()

        val milestone = REVIEW_REQUEST_MILESTONES.firstOrNull { it == newCount } ?: return
        val milestoneKey = "review_milestone_${milestone}_done"
        if (prefs.getBoolean(milestoneKey, false)) return
        prefs.edit().putBoolean(milestoneKey, true).apply()
        _shouldRequestReview.value = true
    }

    /**
     * Gatilho de fallback do pedido de avaliação, para cobrir quem nunca usa "Limpar jogo
     * atual" e por isso nunca aciona [registerQualifyingGameClear]. Conta partidas finalizadas
     * (cada chamada de [finishGame]) e em quantos dias diferentes (calendário) isso aconteceu.
     * A partir do 2º dia diferente com partida finalizada - ou seja, checado logo após o fim da
     * primeira partida desse 2º dia, e continuando a cada partida seguinte caso ainda não tenha
     * disparado -, sugere a avaliação uma única vez assim que o total acumulado de partidas
     * finalizadas atingir [REVIEW_FALLBACK_MIN_MATCHES_FINISHED], desde que nenhum marco de
     * limpeza válida já tenha disparado o pedido (nesse caso o fluxo de marcos já cuida disso).
     */
    private fun registerCompletedMatchForReviewFallback() {
        val prefs = getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastMatchDay = prefs.getString(KEY_LAST_MATCH_FINISHED_DATE, null)
        var distinctDaysCount = prefs.getInt(KEY_DISTINCT_MATCH_DAYS_COUNT, 0)
        if (lastMatchDay != today) {
            distinctDaysCount += 1
            prefs.edit()
                .putString(KEY_LAST_MATCH_FINISHED_DATE, today)
                .putInt(KEY_DISTINCT_MATCH_DAYS_COUNT, distinctDaysCount)
                .apply()
        }

        val newMatchCount = prefs.getInt(KEY_MATCHES_FINISHED_COUNT, 0) + 1
        prefs.edit().putInt(KEY_MATCHES_FINISHED_COUNT, newMatchCount).apply()

        if (prefs.getBoolean(KEY_REVIEW_FALLBACK_DONE, false)) return
        if (distinctDaysCount < REVIEW_FALLBACK_MIN_DISTINCT_DAYS) return
        if (newMatchCount < REVIEW_FALLBACK_MIN_MATCHES_FINISHED) return
        val anyClearMilestoneReached = REVIEW_REQUEST_MILESTONES.any {
            prefs.getBoolean("review_milestone_${it}_done", false)
        }
        if (anyClearMilestoneReached) return

        prefs.edit().putBoolean(KEY_REVIEW_FALLBACK_DONE, true).apply()
        _shouldRequestReview.value = true
    }

    /** Chamado pela UI depois de tratar (ou tentar tratar) o pedido de review. */
    fun onReviewRequestHandled() {
        _shouldRequestReview.value = false
    }

    fun incrementScoreA() {
        if (_scoreA.value < 99) {
            _scoreA.value++
            registerPointForTeam("A")
        }
    }

    fun decrementScoreA() {
        if (_scoreA.value > 0) {
            _scoreA.value--
            clearPointIndicator()
        }
    }

    fun incrementScoreB() {
        if (_scoreB.value < 99) {
            _scoreB.value++
            registerPointForTeam("B")
        }
    }

    fun decrementScoreB() {
        if (_scoreB.value > 0) {
            _scoreB.value--
            clearPointIndicator()
        }
    }

    private fun registerPointForTeam(teamId: String) {
        val previousScoringTeam = _lastScoringTeam.value
        _lastScoringTeam.value = teamId
        _rotationRequiredForTeam.value = if (previousScoringTeam != null && previousScoringTeam != teamId) {
            teamId
        } else {
            null
        }
    }

    private fun clearPointIndicator() {
        _lastScoringTeam.value = null
        _rotationRequiredForTeam.value = null
    }

    private fun resetScoresAndPointIndicator() {
        _scoreA.value = 0
        _scoreB.value = 0
        clearPointIndicator()
    }

    fun setStreakForTeam(team: String, streakValue: Int): ManualStreakAdjustmentLog? {
        if (team != "A" && team != "B") return null
        val normalized = streakValue.coerceAtLeast(0)
        val oldOwner = _streakOwner.value
        val oldStreak = _currentStreak.value

        val newOwner: String?
        val newStreak: Int

        if (normalized == 0) {
            if (_streakOwner.value == team) {
                _streakOwner.value = null
                _currentStreak.value = 0
                newOwner = null
                newStreak = 0
            } else {
                return null
            }
        } else {
            _streakOwner.value = team
            _currentStreak.value = normalized
            newOwner = team
            newStreak = normalized
        }

        if (oldOwner == newOwner && oldStreak == newStreak) return null

        val log = ManualStreakAdjustmentLog(
            timestamp = System.currentTimeMillis(),
            groupName = _currentGroupConfig.value.groupName,
            team = team,
            oldOwner = oldOwner,
            oldStreak = oldStreak,
            newOwner = newOwner,
            newStreak = newStreak
        )
        appendManualStreakLog(log)
        return log
    }

    fun undoLastManualStreakAdjustment(): Boolean {
        val last = _manualStreakAdjustments.value.lastOrNull() ?: return false
        if (
            _currentGroupConfig.value.groupName != last.groupName ||
            _streakOwner.value != last.newOwner ||
            _currentStreak.value != last.newStreak
        ) {
            return false
        }
        _streakOwner.value = last.oldOwner
        _currentStreak.value = last.oldStreak
        _manualStreakAdjustments.value = _manualStreakAdjustments.value.dropLast(1)
        return true
    }

    private fun appendManualStreakLog(log: ManualStreakAdjustmentLog) {
        val trimmed = _manualStreakAdjustments.value.takeLast(maxManualStreakLogsInMemory - 1)
        _manualStreakAdjustments.value = trimmed + log
    }

    private fun appendManualSubstitutionLog(log: ManualSubstitutionLog) {
        val trimmed = _manualSubstitutions.value.takeLast(maxManualSubstitutionLogsInMemory - 1)
        _manualSubstitutions.value = trimmed + log
    }

    private fun clearManualSubstitutionLogs() {
        _manualSubstitutions.value = emptyList()
    }

    private fun clearManualStreakLogs() {
        _manualStreakAdjustments.value = emptyList()
    }

    private fun clearAllActivityLogs() {
        clearManualSubstitutionLogs()
        clearManualStreakLogs()
        _rebalancedPlayerIds.value = emptySet()
        _autoSelectedLoserPlayerIds.value = emptySet()
    }

    private fun loadPreferences() {
        val prefs =
            getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE)
        _themeMode.value = try {
            ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        _showElo.value = prefs.getBoolean("show_elo", false)
        _showToll.value = prefs.getBoolean("show_toll", false)
        _isSupporter.value = prefs.getBoolean("is_supporter", false)
        _telemetryEnabled.value = prefs.getBoolean(TelemetryManager.PREF_KEY_TELEMETRY_ENABLED, false)
        _showTelemetryConsentPrompt.value = !prefs.contains(TelemetryManager.PREF_KEY_TELEMETRY_ENABLED)
        TelemetryManager.init(getApplication(), _telemetryEnabled.value)
        AuthManager.init(getApplication())
        BillingManager.init(getApplication())
        _debugPremiumOverride.value =
            BuildConfig.DEBUG && prefs.getBoolean("debug_premium_override", false)
        _debugPremiumPlanTier.value = prefs.getString("debug_premium_plan_tier", null)?.let {
            try {
                CloudPlanTier.valueOf(it).takeIf { tier -> tier != CloudPlanTier.NONE }
            } catch (e: Exception) {
                null
            }
        } ?: CloudPlanTier.SINGLE
        _personalTeamColorOverrideEnabled.value =
            prefs.getBoolean("personal_team_color_override_enabled", false)
        _personalTeamAColor.value =
            prefs.getString("personal_team_color_a", null)?.let(::parseTeamAccentColorOrNull)
                ?: TeamAccentColor.BLUE
        _personalTeamBColor.value =
            prefs.getString("personal_team_color_b", null)?.let(::parseTeamAccentColorOrNull)
                ?: TeamAccentColor.YELLOW
        _userProfileType.value = prefs.getString("user_profile_type", null)?.let {
            try {
                UserProfileType.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
        _showUserProfileOnboarding.value = !prefs.contains("user_profile_type")
    }

    fun isGameInProgress(): Boolean = _teamA.value.isNotEmpty() || _teamB.value.isNotEmpty()

    fun loadGroupConfig(name: String, balancingMode: String? = null) {
        if (name.isBlank()) {
            startFreshGroupOnboarding()
            return
        }
        val same = _currentGroupConfig.value.groupName == name
        val loadToken = ++groupLoadToken
        if (!same || !persistenceReady) {
            _isGroupDataLoading.value = true
        }
        viewModelScope.launch {
            val loaded = repository.getGroupConfig(name)
            val knownGroups = repository.getAllGroupNames()
            val existingGroupWithoutConfig = loaded == null && knownGroups.contains(name)
            val normalized = loaded?.let {
                val loadedType = GroupType.fromStoredValue(it.groupType)
                it.copy(
                    victoryLimit = it.victoryLimit.coerceIn(2, loadedType.maxTeamSize),
                    balancingMode = BalancingMode.fromStoredValue(it.balancingMode).name,
                    groupType = loadedType.name,
                    teamSize = loadedType.coerceTeamSize(it.teamSize)
                )
            } ?: GroupConfig(
                groupName = name,
                balancingMode = BalancingMode.fromStoredValue(balancingMode).name,
                onboardingStep = if (existingGroupWithoutConfig) {
                    ONBOARDING_STEP_COMPLETE
                } else {
                    ONBOARDING_STEP_GROUP_NAME
                }
            )
            if (loaded == null || normalized != loaded) {
                repository.saveGroupConfig(normalized)
            }
            _currentGroupConfig.value = normalized
            if (shouldAutoClearCurrentGameByInactivity(name)) {
                resetGameState()
                clearSavedGameState(name)
            } else if (!same) {
                // Switching groups: reset current state, then try to restore saved state for new group
                resetGameState()
                tryRestoreGameState(name)
            } else if (!isGameInProgress()) {
                // Same group, no active game: try to restore (covers process-death scenario)
                tryRestoreGameState(name)
            }
            persistenceReady = true
            if (loadToken == groupLoadToken) {
                _isGroupDataLoading.value = false
            }
        }
    }

    /**
     * Reseta o estado do grupo atual para "sem grupo ainda", sem persistir nada no banco.
     * Usado quando não há nenhum grupo existente (instalação nova ou último grupo apagado):
     * evita recriar um grupo com nome hardcoded antes do usuário confirmar um nome válido.
     */
    private fun startFreshGroupOnboarding() {
        _currentGroupConfig.value = GroupConfig(groupName = "", onboardingStep = ONBOARDING_STEP_GROUP_NAME)
        resetGameState()
        persistenceReady = true
        _isGroupDataLoading.value = false
    }

    private fun resetGameState() {
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _presentPlayerIds.value = emptySet(); _currentStreak.value = 0; _streakOwner.value =
            null; _hasPreviousMatch.value = false
        _historyDateFilter.value = null
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = null
        _roundCounter.value = 0
        _restingPlayers.value = emptyMap()
        _guaranteedNextMatchPlayerIds.value = emptyList()
        clearPositionAssignments()
        clearAllActivityLogs()
        lastLosers = emptyList()
    }

    // --- Modo Posições Fixas: atribuição de posições ---

    private fun usesPositions(): Boolean = _currentGroupConfig.value.type.usesPositions

    private fun clearPositionAssignments() {
        _assignedPositions.value = emptyMap()
        _assignedSlotIndices.value = emptyMap()
        _compositionIncomplete.value = false
    }

    /**
     * Recalcula o mapa de posições a partir dos times em quadra. Só faz efeito no Modo Posições
     * Fixas; nos demais tipos limpa o estado para não deixar resíduo de uma conversão de tipo.
     */
    private fun refreshPositionAssignments() {
        if (!usesPositions()) {
            clearPositionAssignments()
            return
        }
        val conf = _currentGroupConfig.value
        val teamSize = conf.teamSize
        val a = PositionAssigner.assignPositionsToExistingTeam(_teamA.value, teamSize, conf.guaranteeSetter)
        val b = PositionAssigner.assignPositionsToExistingTeam(_teamB.value, teamSize, conf.guaranteeSetter)
        _assignedPositions.value = a.positions + b.positions
        _assignedSlotIndices.value = buildMap {
            a.slots.forEachIndexed { index, slot -> slot.player?.let { put(it.id, index) } }
            b.slots.forEachIndexed { index, slot -> slot.player?.let { put(it.id, index) } }
        }
        _compositionIncomplete.value = !a.isComplete || !b.isComplete
    }

    fun updateConfig(
        s: Int,
        l: Int,
        priorityP: Boolean,
        scoreEnabled: Boolean = true,
        balancingMode: String = _currentGroupConfig.value.balancingMode,
        groupType: String = _currentGroupConfig.value.groupType,
        guaranteeSetter: Boolean = _currentGroupConfig.value.guaranteeSetter
    ) {
        val current = _currentGroupConfig.value
        val requestedType = GroupType.fromStoredValue(groupType)
        val newType = if (current.type.canConvertTo(requestedType)) requestedType else current.type
        val typeChanged = newType != current.type
        val safeTeamSize = newType.coerceTeamSize(s)
        val safeVictoryLimit = l.coerceIn(2, newType.maxTeamSize)
        if (current.teamSize != safeTeamSize) {
            _currentStreak.value = 0
            _streakOwner.value = null
            trimGuaranteedNextMatchToCapacity(safeTeamSize * 2)
        }
        _currentGroupConfig.value = current.copy(
            groupType = newType.name,
            teamSize = safeTeamSize,
            victoryLimit = safeVictoryLimit,
            priorityEnabled = priorityP && newType.supportsPriority,
            scoreEnabled = scoreEnabled,
            balancingMode = BalancingMode.fromStoredValue(balancingMode).name,
            guaranteeSetter = guaranteeSetter
        )
        if (typeChanged && isGameInProgress()) {
            // A partida em andamento não sobrevive à troca de tipo: as regras de composição mudam.
            cancelGame()
        }
        refreshPositionAssignments()
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
        val updatedConfig = _currentGroupConfig.value
        val cloudGroupId = updatedConfig.cloudGroupId
        if (cloudGroupId != null && updatedConfig.remoteRole != UserProfileType.ESPECTADOR.name) {
            // Repassa tipo/balanceamento/tamanho de time ao Firestore (organizador e Auxiliar
            // podem editar) para que os ícones do cabeçalho fiquem sincronizados em todos os
            // aparelhos do grupo (ver `header-meta-sync`).
            viewModelScope.launch(Dispatchers.IO) {
                CloudSyncManager.setGroupMeta(
                    cloudGroupId,
                    updatedConfig.groupType,
                    updatedConfig.balancingMode,
                    updatedConfig.teamSize
                )
            }
        }
    }

    private fun trimGuaranteedNextMatchToCapacity(maxPlayersInCourt: Int) {
        if (maxPlayersInCourt <= 0) {
            _guaranteedNextMatchPlayerIds.value = emptyList()
            return
        }
        val current = _guaranteedNextMatchPlayerIds.value
        if (current.size > maxPlayersInCourt) {
            _guaranteedNextMatchPlayerIds.value = current.take(maxPlayersInCourt)
        }
    }

    fun continueCurrentGroupOnboardingWithTeamSize(teamSize: Int) {
        val clampedTeamSize = _currentGroupConfig.value.type.coerceTeamSize(teamSize)
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            teamSize = clampedTeamSize,
            onboardingStep = ONBOARDING_STEP_MIN_PLAYERS
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun returnCurrentGroupOnboardingToTeamSizeStep() {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            onboardingStep = ONBOARDING_STEP_TEAM_SIZE
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun continueCurrentGroupOnboardingWithGroupName(newName: String) = viewModelScope.launch(Dispatchers.IO) {
        val normalizedName = normalizeGroupName(newName)
        if (normalizedName.isBlank()) return@launch

        val current = _currentGroupConfig.value
        val oldName = current.groupName
        if (oldName.isNotBlank() && oldName != normalizedName) {
            repository.renameGroup(oldName, normalizedName)
        }
        _currentGroupConfig.value = current.copy(
            groupName = normalizedName,
            onboardingStep = ONBOARDING_STEP_GROUP_TYPE
        )
        repository.saveGroupConfig(_currentGroupConfig.value)
    }

    fun continueCurrentGroupOnboardingWithGroupType(groupType: String) {
        val type = GroupType.fromStoredValue(groupType)
        val current = _currentGroupConfig.value
        _currentGroupConfig.value = current.copy(
            groupType = type.name,
            teamSize = type.coerceTeamSize(current.teamSize),
            balancingMode = if (type.supportsBalancingMode) current.balancingMode else BalancingMode.REBALANCE.name,
            onboardingStep = if (type.supportsBalancingMode) {
                ONBOARDING_STEP_BALANCING_MODE
            } else {
                ONBOARDING_STEP_TEAM_SIZE
            }
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun returnCurrentGroupOnboardingToGroupTypeStep() {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            onboardingStep = ONBOARDING_STEP_GROUP_TYPE
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun continueCurrentGroupOnboardingWithBalancingMode(balancingMode: String) {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            balancingMode = BalancingMode.fromStoredValue(balancingMode).name,
            onboardingStep = ONBOARDING_STEP_TEAM_SIZE
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun returnCurrentGroupOnboardingToGroupNameStep() {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            onboardingStep = ONBOARDING_STEP_GROUP_NAME
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun returnCurrentGroupOnboardingToBalancingModeStep() {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            onboardingStep = ONBOARDING_STEP_BALANCING_MODE
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun completeCurrentGroupOnboarding() {
        if (_currentGroupConfig.value.onboardingStep >= ONBOARDING_STEP_COMPLETE) return
        _currentGroupConfig.value = _currentGroupConfig.value.copy(onboardingStep = ONBOARDING_STEP_COMPLETE)
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    suspend fun renameGroup(old: String, new: String) {
        val normalizedNew = normalizeGroupName(new)
        if (normalizedNew.isBlank() || normalizedNew == old) return
        repository.renameGroup(old, normalizedNew)
        if (_currentGroupConfig.value.groupName == old) {
            _currentGroupConfig.value = _currentGroupConfig.value.copy(groupName = normalizedNew)
        }
    }

    fun deleteGroup(name: String) = viewModelScope.launch {
        repository.deleteGroup(name)
        if (_currentGroupConfig.value.groupName == name) {
            val fallbackGroup = repository.getAllGroupNames().firstOrNull()
            if (fallbackGroup != null) {
                loadGroupConfig(fallbackGroup)
            } else {
                startFreshGroupOnboarding()
            }
        }
    }

    fun createGroup(
        name: String,
        balancingMode: String = BalancingMode.REBALANCE.name,
        groupType: String = GroupType.RECREATIONAL.name
    ) = viewModelScope.launch(Dispatchers.IO) {
        val normalizedName = normalizeGroupName(name)
        if (normalizedName.isBlank()) return@launch

        val existingConfig = repository.getGroupConfig(normalizedName)
        if (existingConfig != null) {
            loadGroupConfig(normalizedName)
            return@launch
        }
        val type = GroupType.fromStoredValue(groupType)
        val cfg = GroupConfig(
            groupName = normalizedName,
            balancingMode = if (type.supportsBalancingMode) {
                BalancingMode.fromStoredValue(balancingMode).name
            } else {
                BalancingMode.REBALANCE.name
            },
            onboardingStep = if (type.supportsBalancingMode) {
                ONBOARDING_STEP_BALANCING_MODE
            } else {
                ONBOARDING_STEP_TEAM_SIZE
            },
            groupType = type.name
        )
        repository.saveGroupConfig(cfg)
        // Garantir que o load use o cfg salvo (faz reset e tentativa de restauração)
        loadGroupConfig(normalizedName)
        TelemetryManager.logGroupCreated(getApplication(), cfg.groupType, cfg.balancingMode)
    }

    private fun getUsageCountMap(date: String): Map<Int, Int> {
        return currentGroupEloLogs.value
            .filter { it.date == date }
            .groupingBy { it.playerId }
            .eachCount()
    }

    private fun sortTeamPlayers(team: List<Player>): List<Player> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        return team.shuffled().sortedBy { p ->
            TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)
        }
    }

    private fun teamSnapshotFromPlayers(players: List<Player>): TeamSnapshotWithIds {
        val entries = players
            .mapNotNull { player ->
                val normalizedName = normalizePersonName(player.name)
                if (normalizedName.isBlank()) return@mapNotNull null
                TeamSnapshotEntry(name = normalizedName, id = player.id)
            }
            .sortedBy { it.name.lowercase(Locale.ROOT) }
        return TeamSnapshotWithIds(
            names = entries.joinToString(", ") { it.name },
            ids = entries.joinToString(",") { it.id?.toString() ?: "" }
        )
    }

    private fun calculateTollForNewPlayer(excludePlayerId: Int? = null): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        val presentPlayers = _presentPlayerIds.value.mapNotNull { id ->
            currentGroupPlayers.value.find { it.id == id }
        }
        return TollCalculator.calculateToll(presentPlayers, usageMap, today, excludePlayerId)
    }

    private fun applyTollIfNecessary(player: Player): Player {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        val presentPlayers = _presentPlayerIds.value.mapNotNull { id ->
            currentGroupPlayers.value.find { it.id == id }
        }
        val updatedP = TollCalculator.applyToll(player, presentPlayers, usageMap, today)
        if (updatedP !== player) {
            viewModelScope.launch(Dispatchers.IO) { repository.updatePlayer(updatedP) }
        }
        return updatedP
    }

    fun addPlayer(
        n: String,
        e: Double,
        g: String,
        isPriority: Boolean,
        preferredPosition: String? = null,
        secondaryPosition: String? = null
    ) = viewModelScope.launch {
        if (g.isBlank()) return@launch
        val normalizedName = normalizePersonName(n)
        if (normalizedName.isBlank()) return@launch

        val nameAlreadyExists = currentGroupPlayers.value.any {
            areSameCanonicalName(it.name, normalizedName)
        }

        if (nameAlreadyExists) {
            _uiMessage.value = getApplication<Application>().getString(R.string.players_name_already_exists)
            // 2. Usamos return@launch para sair da corrotina sem quebrar o viewModelScope
            return@launch
        }

        val pToInsert = Player(
            name = normalizedName,
            elo = e,
            groupName = g,
            isPriority = isPriority,
            dailyToll = 0,
            tollDate = "",
            preferredPosition = PlayerPosition.fromStoredValue(preferredPosition)?.name,
            secondaryPosition = PlayerPosition.fromStoredValue(secondaryPosition)?.name
        )
        val newId = repository.insertPlayer(pToInsert)
        val newPlayer = pToInsert.copy(id = newId.toInt())

        _presentPlayerIds.update { it + newPlayer.id }

        if (isGameInProgress()) {
            val updatedP = applyTollIfNecessary(newPlayer)
            val gamesPlayed = gamesPlayedTodayMap.value[updatedP.id] ?: 0
            if (gamesPlayed > 0) {
                _waitingList.update { it + updatedP }
            } else {
                _waitingList.update { listOf(updatedP) + it }
            }
        }
    }

    fun deletePlayer(p: Player) = viewModelScope.launch {
        repository.deletePlayer(p)
        _guaranteedNextMatchPlayerIds.value =
            _guaranteedNextMatchPlayerIds.value.filter { it != p.id }
        if (_presentPlayerIds.value.contains(p.id)) togglePlayerPresence(p)
    }

    fun editPlayer(
        p: Player,
        n: String,
        isPriority: Boolean,
        preferredPosition: String? = p.preferredPosition,
        secondaryPosition: String? = p.secondaryPosition
    ) = viewModelScope.launch(Dispatchers.IO) {
        val oldName = p.name
        val normalizedName = normalizePersonName(n)
        if (normalizedName.isBlank()) return@launch

        if (!areSameCanonicalName(oldName, normalizedName)) {
            val nameAlreadyExists = currentGroupPlayers.value.any {
                it.id != p.id && areSameCanonicalName(it.name, normalizedName)
            }
            if (nameAlreadyExists) {
                _uiMessage.value = getApplication<Application>().getString(R.string.players_name_already_exists)
                return@launch
            }
        }
        val up = p.copy(
            name = normalizedName,
            isPriority = isPriority,
            preferredPosition = PlayerPosition.fromStoredValue(preferredPosition)?.name,
            secondaryPosition = PlayerPosition.fromStoredValue(secondaryPosition)?.name
        )
        repository.updatePlayer(up)
        if (oldName != normalizedName) {
            repository.renamePlayerCascade(p.id, oldName, normalizedName, p.groupName)
        }
        _teamA.value = sortTeamPlayers(_teamA.value.map { if (it.id == p.id) up else it })
        _teamB.value = sortTeamPlayers(_teamB.value.map { if (it.id == p.id) up else it })
        _waitingList.value = _waitingList.value.map { if (it.id == p.id) up else it }
        _lastWinners.value = _lastWinners.value.map { if (it.id == p.id) up else it }
        lastLosers = lastLosers.map { if (it.id == p.id) up else it }
    }

    fun togglePlayerPresence(p: Player) {
        if (_currentGroupConfig.value.remoteRole == UserProfileType.AUXILIAR.name) {
            requestRemotePresenceToggle(p.publicId)
            return
        }
        val ids = _presentPlayerIds.value.toMutableSet()
        if (ids.contains(p.id)) {
            ids.remove(p.id)
            _waitingList.value = _waitingList.value.filter { it.id != p.id }
            _presentPlayerIds.value = ids
            _guaranteedNextMatchPlayerIds.value =
                _guaranteedNextMatchPlayerIds.value.filter { it != p.id }
        } else {
            ids.add(p.id)
            _presentPlayerIds.value = ids

            val isWinnerWaiting =
                _hasPreviousMatch.value && _lastWinners.value.any { it.id == p.id }
            if (!_teamA.value.any { it.id == p.id } && !_teamB.value.any { it.id == p.id } && !_waitingList.value.any { it.id == p.id } && !isWinnerWaiting) {
                if (isGameInProgress()) {
                    val updatedP = applyTollIfNecessary(p)
                    val gamesPlayed = gamesPlayedTodayMap.value[p.id] ?: 0
                    if (gamesPlayed > 0) {
                        _waitingList.value = _waitingList.value + updatedP
                    } else {
                        _waitingList.value = listOf(updatedP) + _waitingList.value
                    }
                }
            }
        }
    }

    fun toggleGuaranteedNextMatchPlayer(player: Player) {
        val maxPlayersInCourt = _currentGroupConfig.value.teamSize * 2
        if (maxPlayersInCourt <= 0) return

        val current = _guaranteedNextMatchPlayerIds.value
        if (current.contains(player.id)) {
            _guaranteedNextMatchPlayerIds.value = current.filter { it != player.id }
            return
        }

        if (current.size >= maxPlayersInCourt) {
            _uiMessage.value = getApplication<Application>().getString(
                R.string.max_guaranteed_next_match_players,
                maxPlayersInCourt
            )
            return
        }

        if (!_presentPlayerIds.value.contains(player.id)) {
            togglePlayerPresence(player)
        }
        _guaranteedNextMatchPlayerIds.value = _guaranteedNextMatchPlayerIds.value + player.id
    }

    fun removePlayerFromWaitingList(p: Player) {
        val ids = _presentPlayerIds.value.toMutableSet()
        if (ids.contains(p.id)) {
            ids.remove(p.id)
            _presentPlayerIds.value = ids
            _waitingList.value = _waitingList.value.filter { it.id != p.id }
        }
    }

    fun movePlayerToBeginning(p: Player) {
        val updatedPlayer = if (_presentPlayerIds.value.contains(p.id)) {
            p
        } else {
            applyTollIfNecessary(p)
        }

        _presentPlayerIds.update { it + updatedPlayer.id }
        _waitingList.update { list ->
            buildList {
                add(updatedPlayer)
                addAll(list.filterNot { it.id == updatedPlayer.id })
            }
        }
    }

    fun movePlayerToEnd(p: Player) {
        val updatedPlayer = if (_presentPlayerIds.value.contains(p.id)) {
            p
        } else {
            applyTollIfNecessary(p)
        }

        _presentPlayerIds.update { it + updatedPlayer.id }
        _waitingList.update { list ->
            buildList {
                addAll(list.filterNot { it.id == updatedPlayer.id })
                add(updatedPlayer)
            }
        }
    }

    fun reorderWaitingList(from: Int, to: Int) {
        if (from < 0 || to < 0) return
        val newList = _waitingList.value.toMutableList()
        if (from >= newList.size || to > newList.size) return
        val item = newList.removeAt(from)
        newList.add(to, item)
        _waitingList.value = newList
    }

    fun moveWaitingPlayerToIndex(player: Player, targetIndex: Int) {
        _waitingList.update { list ->
            val withoutPlayer = list.filterNot { it.id == player.id }
            val safeIndex = targetIndex.coerceIn(0, withoutPlayer.size)
            buildList {
                addAll(withoutPlayer.take(safeIndex))
                add(player)
                addAll(withoutPlayer.drop(safeIndex))
            }
        }
    }

    fun insertPlayerIntoWaitingList(player: Player, targetIndex: Int) {
        val updatedPlayer = if (_presentPlayerIds.value.contains(player.id)) {
            player
        } else {
            applyTollIfNecessary(player)
        }

        _presentPlayerIds.update { it + updatedPlayer.id }
        _waitingList.update { list ->
            val withoutPlayer = list.filterNot { it.id == updatedPlayer.id }
            val safeIndex = targetIndex.coerceIn(0, withoutPlayer.size)
            buildList {
                addAll(withoutPlayer.take(safeIndex))
                add(updatedPlayer)
                addAll(withoutPlayer.drop(safeIndex))
            }
        }
    }

    fun setAllPlayersPresence(list: List<Player>, present: Boolean) {
        // Auxiliar não tem `_presentPlayerIds` local válido (não é dono do Room do grupo) — usa a
        // lista de presentes espelhada do organizador para saber quem já está presente antes de
        // decidir se precisa alternar (ver `aux-full-roster-sync`/[togglePlayerPresence]).
        val currentlyPresentIds = if (_currentGroupConfig.value.remoteRole == UserProfileType.AUXILIAR.name) {
            _remoteSelectedPlayers.value.map { it.id }.toSet()
        } else {
            _presentPlayerIds.value
        }
        list.forEach { player ->
            val isCurrentlyPresent = currentlyPresentIds.contains(player.id)
            val shouldToggle =
                (present && !isCurrentlyPresent) || (!present && isCurrentlyPresent)
            if (shouldToggle) {
                togglePlayerPresence(player)
            }
        }
    }

    fun startNewAutomaticGame(all: List<Player>, size: Int) {
        clearAllActivityLogs()
        val available = all.filter { _presentPlayerIds.value.contains(it.id) }
        if (available.size < size * 2) return

        val availableWithTollApplied = available.map { applyTollIfNecessary(it) }

        val config = _currentGroupConfig.value
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)

        fun getEffectiveGames(p: Player): Int =
            TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)

        val selectedPlayers = mutableListOf<Player>()
        val pool =
            TeamBalancer.groupAndInterleave(availableWithTollApplied) { getEffectiveGames(it) }.toMutableList()
        val usesPositions = config.type.usesPositions
        val guaranteedIdsSet = _guaranteedNextMatchPlayerIds.value.toSet()
        if (guaranteedIdsSet.isNotEmpty()) {
            val guaranteedPlayers = pool.filter { guaranteedIdsSet.contains(it.id) }.take(size * 2)
            selectedPlayers.addAll(guaranteedPlayers)
            val guaranteedSelectedIds = guaranteedPlayers.map { it.id }.toSet()
            pool.removeAll { guaranteedSelectedIds.contains(it.id) }
        } else if (!usesPositions) {
            val shouldApplyPriorityRuleInSelection = shouldApplyPriorityRule(config.priorityEnabled, pool)
            if (shouldApplyPriorityRuleInSelection) {
                val priorities = pool.filter { it.isPriority }
                val prioritiesToSelect = priorities.take(2)
                selectedPlayers.addAll(prioritiesToSelect)
                pool.removeAll(prioritiesToSelect)
            }
        }

        val remainingSlots = (size * 2) - selectedPlayers.size
        if (remainingSlots > 0) {
            val others = if (usesPositions) {
                // No Modo Posições Fixas a escolha de quem entra também cobre as vagas faltantes.
                val (picked, _) = PositionAssigner.pickToCoverComposition(
                    base = selectedPlayers,
                    pool = pool,
                    count = remainingSlots,
                    teamSize = size,
                    guaranteeSetter = config.guaranteeSetter
                )
                picked
            } else {
                pool.take(remainingSlots)
            }
            selectedPlayers.addAll(others)
            pool.removeAll(others)
        }

        val (finalA, finalB) = if (usesPositions) {
            PositionAssigner.buildBalancedTeams(selectedPlayers, size, config.guaranteeSetter).let {
                it.teamA to it.teamB
            }
        } else {
            clearPositionAssignments()
            balanceTeamsWithPriority(
                players = selectedPlayers,
                teamSize = size,
                usePriorityRule = shouldApplyPriorityRule(config.priorityEnabled, selectedPlayers)
            )
        }
        _teamA.value = sortTeamPlayers(finalA); _teamB.value =
            sortTeamPlayers(finalB); _waitingList.value = pool
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
        _guaranteedNextMatchPlayerIds.value = emptyList()
        refreshPositionAssignments()
    }

    private fun shouldApplyPriorityRule(priorityEnabled: Boolean, players: List<Player>): Boolean {
        return priorityEnabled && players.count { it.isPriority } >= 2
    }

    private fun balanceTeamsWithPriority(
        players: List<Player>,
        teamSize: Int,
        usePriorityRule: Boolean
    ): Pair<List<Player>, List<Player>> {
        val priorities = if (usePriorityRule) {
            players.filter { it.isPriority }.sortedByDescending { it.elo }
        } else {
            emptyList()
        }
        val nonPriorities = if (usePriorityRule) {
            players.filter { !it.isPriority }.sortedByDescending { it.elo }
        } else {
            players.sortedByDescending { it.elo }
        }
        val tA = mutableListOf<Player>()
        val tB = mutableListOf<Player>()

        priorities.forEachIndexed { i, p ->
            if (tA.size < teamSize && tB.size < teamSize) {
                if (i % 2 == 0) tA.add(p) else tB.add(p)
            } else if (tA.size < teamSize) tA.add(p) else tB.add(p)
        }
        nonPriorities.forEach { p ->
            if (tA.size < teamSize && tB.size < teamSize) {
                if (tA.sumOf { it.elo } <= tB.sumOf { it.elo }) tA.add(p) else tB.add(p)
            } else if (tA.size < teamSize) {
                tA.add(p)
            } else if (tB.size < teamSize) {
                tB.add(p)
            }
        }
        return tA to tB
    }

    private fun splitPlayersEvenlyForRebalance(
        players: List<Player>,
        priorityEnabled: Boolean
    ): Pair<MutableList<Player>, MutableList<Player>> {
        if (players.isEmpty()) return mutableListOf<Player>() to mutableListOf<Player>()
        val targetA = (players.size + 1) / 2
        val targetB = players.size / 2
        val remaining = players.sortedByDescending { it.elo }.toMutableList()
        val teamA = mutableListOf<Player>()
        val teamB = mutableListOf<Player>()

        if (shouldApplyPriorityRule(priorityEnabled, players)) {
            val orderedPriorities = TeamBalancer.interleaveByElo(remaining.filter { it.isPriority })
            val firstPriority = orderedPriorities.getOrNull(0)
            val secondPriority = orderedPriorities.getOrNull(1)
            if (firstPriority != null && teamA.size < targetA) {
                teamA.add(firstPriority)
                remaining.remove(firstPriority)
            }
            if (secondPriority != null && teamB.size < targetB) {
                teamB.add(secondPriority)
                remaining.remove(secondPriority)
            }
        }

        remaining.forEach { player ->
            val canAddA = teamA.size < targetA
            val canAddB = teamB.size < targetB
            when {
                canAddA && canAddB -> {
                    if (teamA.sumOf { it.elo } <= teamB.sumOf { it.elo }) teamA.add(player) else teamB.add(player)
                }
                canAddA -> teamA.add(player)
                canAddB -> teamB.add(player)
            }
        }
        return teamA to teamB
    }

    fun startManualGame(tA: List<Player>, tB: List<Player>, rem: List<Player>) {
        clearAllActivityLogs()
        val tAWithToll = tA.map { applyTollIfNecessary(it) }
        val tBWithToll = tB.map { applyTollIfNecessary(it) }
        val remWithToll = rem.map { applyTollIfNecessary(it) }

        _teamA.value = sortTeamPlayers(tAWithToll); _teamB.value =
            sortTeamPlayers(tBWithToll); _waitingList.value = remWithToll
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
        _guaranteedNextMatchPlayerIds.value = emptyList()
        refreshPositionAssignments()
    }

    fun cancelGame() {
        clearAllActivityLogs()
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _currentStreak.value = 0; _streakOwner.value = null; _hasPreviousMatch.value = false
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = null
        clearPositionAssignments()
    }

    fun substitutePlayer(out: Player, `in`: Player) {
        val wait = _waitingList.value.toMutableList()
        val nA = _teamA.value.toMutableList()
        val nB = _teamB.value.toMutableList()
        val idxOutA = nA.indexOfFirst { it.id == out.id }
        val idxOutB = nB.indexOfFirst { it.id == out.id }

        val inWithToll = applyTollIfNecessary(`in`)

        val idxInA = nA.indexOfFirst { it.id == inWithToll.id }
        val idxInB = nB.indexOfFirst { it.id == inWithToll.id }
        val idxInWait = wait.indexOfFirst { it.id == inWithToll.id }

        var resetStreak = false
        var substitutionLog: ManualSubstitutionLog? = null

        fun swapAssignedSlotsWithinSameTeam(targetTeam: String) {
            val positions = _assignedPositions.value.toMutableMap()
            val slots = _assignedSlotIndices.value.toMutableMap()
            val outPosition = positions[out.id]
            val inPosition = positions[inWithToll.id]
            val outSlot = slots[out.id]
            val inSlot = slots[inWithToll.id]
            if (outPosition != null && inPosition != null) {
                positions[out.id] = inPosition
                positions[inWithToll.id] = outPosition
                _assignedPositions.value = positions
            }
            if (outSlot != null && inSlot != null) {
                slots[out.id] = inSlot
                slots[inWithToll.id] = outSlot
                _assignedSlotIndices.value = slots
            }
            substitutionLog = ManualSubstitutionLog(
                timestamp = System.currentTimeMillis(),
                groupName = _currentGroupConfig.value.groupName,
                playerOutName = out.name,
                playerInName = inWithToll.name,
                targetTeam = targetTeam,
                incomingSource = "BENCH"
            )
        }

        if (idxOutA != -1) {
            if (_streakOwner.value == "A") resetStreak = true
            if (idxInA != -1) {
                swapAssignedSlotsWithinSameTeam("A")
            } else {
                nA[idxOutA] = inWithToll
            }
            if (idxInWait != -1) {
                wait[idxInWait] = out
                substitutionLog = ManualSubstitutionLog(
                    timestamp = System.currentTimeMillis(),
                    groupName = _currentGroupConfig.value.groupName,
                    playerOutName = out.name,
                    playerInName = inWithToll.name,
                    targetTeam = "A",
                    incomingSource = "WAIT"
                )
            } else if (idxInB != -1) {
                nB[idxInB] = out
                substitutionLog = ManualSubstitutionLog(
                    timestamp = System.currentTimeMillis(),
                    groupName = _currentGroupConfig.value.groupName,
                    playerOutName = out.name,
                    playerInName = inWithToll.name,
                    targetTeam = "A",
                    incomingSource = "B"
                )
            }
        } else if (idxOutB != -1) {
            if (_streakOwner.value == "B") resetStreak = true
            if (idxInB != -1) {
                swapAssignedSlotsWithinSameTeam("B")
            } else {
                nB[idxOutB] = inWithToll
            }
            if (idxInWait != -1) {
                wait[idxInWait] = out
                substitutionLog = ManualSubstitutionLog(
                    timestamp = System.currentTimeMillis(),
                    groupName = _currentGroupConfig.value.groupName,
                    playerOutName = out.name,
                    playerInName = inWithToll.name,
                    targetTeam = "B",
                    incomingSource = "WAIT"
                )
            } else if (idxInA != -1) {
                nA[idxInA] = out
                substitutionLog = ManualSubstitutionLog(
                    timestamp = System.currentTimeMillis(),
                    groupName = _currentGroupConfig.value.groupName,
                    playerOutName = out.name,
                    playerInName = inWithToll.name,
                    targetTeam = "B",
                    incomingSource = "A"
                )
            }
        }

        if (resetStreak) {
            _currentStreak.value = 0
            _streakOwner.value = null
        }

        substitutionLog?.let(::appendManualSubstitutionLog)

        _teamA.value = sortTeamPlayers(nA); _teamB.value = sortTeamPlayers(nB); _waitingList.value =
            wait
        if (idxInA != -1 && idxOutA != -1 || idxInB != -1 && idxOutB != -1) return
        refreshPositionAssignments()
    }

    fun finishGame(winner: String) {
        val conf = _currentGroupConfig.value
        if (conf.remoteRole == UserProfileType.AUXILIAR.name) {
            requestRemoteFinish(winner)
            return
        }
        clearAllActivityLogs()
        val cA = _teamA.value
        val cB = _teamB.value
        val sA = _scoreA.value
        val sB = _scoreB.value
        if (cA.isEmpty() || cB.isEmpty()) return

        registerCompletedMatchForReviewFallback()

        if (_streakOwner.value == winner) _currentStreak.value++ else {
            _streakOwner.value = winner; _currentStreak.value = 1
        }
        val (winners, losers) = if (winner == "A") cA to cB else cB to cA
        _lastWinners.value = winners; lastLosers = losers; _hasPreviousMatch.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val conf = _currentGroupConfig.value
            val cloudGroupId = conf.cloudGroupId.takeIf { conf.remoteRole == null && conf.isCloudSynced }
            val avgA = cA.map { it.elo }.average()
            val avgB = cB.map { it.elo }.average()
            val delta =
                if (winner == "A") EloCalculator.calculateEloChange(avgA, avgB)
                else EloCalculator.calculateEloChange(avgB, avgA)
            val dateLog = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val endTimestamp = System.currentTimeMillis()
            val dateDisplay =
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(endTimestamp))
            val startTimestamp = _currentMatchStartTimestamp.value

            val updatedPlayers = mutableListOf<Player>()
            val newWinners = mutableListOf<Player>()
            val newLosers = mutableListOf<Player>()

            suspend fun process(list: List<Player>, won: Boolean, opponentAvgElo: Double) {
                val deltas = EloCalculator.calculateNormalizedDeltas(list, opponentAvgElo, won, delta)
                list.forEachIndexed { i, p ->
                    val newElo = p.elo + deltas[i]
                    val u = p.copy(
                        elo = newElo,
                        matchesPlayed = p.matchesPlayed + 1,
                        victories = if (won) p.victories + 1 else p.victories
                    )
                    updatedPlayers.add(u); if (won) newWinners.add(u) else newLosers.add(u)
                    val nameSnapshot = normalizePersonName(u.name).ifBlank { "Desconhecido" }
                    repository.insertEloLog(
                        PlayerEloLog(
                            playerId = u.id,
                            playerNameSnapshot = nameSnapshot,
                            date = dateLog,
                            elo = newElo,
                            groupName = u.groupName,
                            won = won
                        )
                    )
                    if (cloudGroupId != null) {
                        CloudSyncManager.pushEloLogEntry(
                            cloudGroupId,
                            RemoteEloLogEntry(playerNameSnapshot = nameSnapshot, date = dateLog, elo = newElo, won = won, endTimestamp = endTimestamp)
                        )
                    }
                }
            }
            process(winners, true, if (winner == "A") avgB else avgA)
            process(losers, false, if (winner == "A") avgA else avgB)

            _lastWinners.value = newWinners; lastLosers = newLosers
            repository.updatePlayers(updatedPlayers)
            val teamASnapshot = teamSnapshotFromPlayers(cA)
            val teamBSnapshot = teamSnapshotFromPlayers(cB)
            repository.insertMatch(
                MatchHistory(
                    date = dateDisplay,
                    teamA = teamASnapshot.names,
                    teamB = teamBSnapshot.names,
                    teamAIds = teamASnapshot.ids,
                    teamBIds = teamBSnapshot.ids,
                    winner = winner,
                    eloPoints = delta,
                    groupName = cA.first().groupName,
                    teamAAverageElo = avgA,
                    teamBAverageElo = avgB,
                    teamAScore = sA,
                    teamBScore = sB,
                    startTimestamp = startTimestamp,
                    endTimestamp = endTimestamp
                )
            )
            if (cloudGroupId != null) {
                CloudSyncManager.pushHistoryEntry(
                    cloudGroupId,
                    RemoteHistoryEntry(
                        date = dateDisplay,
                        teamA = teamASnapshot.names,
                        teamB = teamBSnapshot.names,
                        winner = winner,
                        teamAScore = sA,
                        teamBScore = sB,
                        endTimestamp = endTimestamp,
                        startTimestamp = startTimestamp,
                        eloPoints = delta,
                        teamAAverageElo = avgA,
                        teamBAverageElo = avgB
                    )
                )
            }
            TelemetryManager.logMatchFinished(
                getApplication(),
                groupType = conf.groupType,
                teamSize = conf.teamSize,
                streakBroken = conf.victoryLimit > 0 && _currentStreak.value >= conf.victoryLimit
            )
            _teamA.value = emptyList(); _teamB.value = emptyList()
            resetScoresAndPointIndicator()
            _currentMatchStartTimestamp.value = null
        }
    }

    /**
     * Equivalente de [finishGame] para um Auxiliar remoto: como este aparelho não tem os
     * `Player` reais do grupo no Room (não é dono da tabela), ele não pode rodar o cálculo de
     * Elo/vitórias/histórico localmente. Em vez disso, publica um "pedido de encerramento"
     * (`pendingFinishWinner`/`pendingFinishRequestId`) em [LiveGameState]; o organizador (que tem
     * os `Player` reais) detecta o pedido em [observeAndMirrorRemoteLiveState] e roda o
     * [finishGame] de verdade, cujo push subsequente já limpa os campos pendentes e propaga o
     * placar/times zerados de volta para este Auxiliar. Localmente só atualizamos cosméticos
     * (sequência de vitórias e destaque de "última vitória") para dar feedback imediato.
     */
    private fun requestRemoteFinish(winner: String) {
        val conf = _currentGroupConfig.value
        val cloudGroupId = conf.cloudGroupId ?: return
        val cA = _teamA.value
        val cB = _teamB.value
        if (cA.isEmpty() || cB.isEmpty()) return

        if (_streakOwner.value == winner) _currentStreak.value++ else {
            _streakOwner.value = winner; _currentStreak.value = 1
        }
        val (winners, losers) = if (winner == "A") cA to cB else cB to cA
        _lastWinners.value = winners; lastLosers = losers; _hasPreviousMatch.value = true

        val requestId = java.util.UUID.randomUUID().toString()
        val pending = LiveGameState(
            groupName = conf.groupName,
            teamA = cA.map { it.toRemoteSnapshot() },
            teamB = cB.map { it.toRemoteSnapshot() },
            waitingList = _waitingList.value.map { it.toRemoteSnapshot() },
            scoreA = _scoreA.value,
            scoreB = _scoreB.value,
            currentStreak = _currentStreak.value,
            streakOwner = _streakOwner.value,
            updatedAt = System.currentTimeMillis(),
            pendingFinishWinner = winner,
            pendingFinishRequestId = requestId,
            presentPlayers = lastKnownRemotePresentPlayers,
            allPlayers = lastKnownRemoteAllPlayers,
            matchStartTimestamp = _currentMatchStartTimestamp.value,
            lastScoringTeam = _lastScoringTeam.value,
            rotationRequiredForTeam = _rotationRequiredForTeam.value
        )
        lastPushedUpdatedAt = pending.updatedAt
        viewModelScope.launch(Dispatchers.IO) {
            CloudSyncManager.pushLiveState(cloudGroupId, pending)
        }
    }

    /**
     * Pedido de um Auxiliar remoto para alternar a presença de um jogador (marcar/desmarcar antes
     * da partida começar). Como este aparelho não tem o roster real no Room, não pode recalcular
     * localmente os efeitos colaterais de presença (fila de espera, tolerância, etc.) — em vez
     * disso publica um pedido (`pendingPresenceTogglePublicId`/`pendingPresenceToggleRequestId`)
     * que o organizador detecta em [observeAndMirrorRemoteLiveState] e resolve chamando sua
     * própria lógica real de [togglePlayerPresence]; o push seguinte do organizador já limpa os
     * campos pendentes e propaga o novo estado de volta. Mesmo padrão de [requestRemoteFinish].
     */
    private fun requestRemotePresenceToggle(publicId: String) {
        val conf = _currentGroupConfig.value
        val cloudGroupId = conf.cloudGroupId ?: return
        val requestId = java.util.UUID.randomUUID().toString()
        val pending = LiveGameState(
            groupName = conf.groupName,
            teamA = _teamA.value.map { it.toRemoteSnapshot() },
            teamB = _teamB.value.map { it.toRemoteSnapshot() },
            waitingList = _waitingList.value.map { it.toRemoteSnapshot() },
            scoreA = _scoreA.value,
            scoreB = _scoreB.value,
            currentStreak = _currentStreak.value,
            streakOwner = _streakOwner.value,
            updatedAt = System.currentTimeMillis(),
            presentPlayers = lastKnownRemotePresentPlayers,
            allPlayers = lastKnownRemoteAllPlayers,
            matchStartTimestamp = _currentMatchStartTimestamp.value,
            lastScoringTeam = _lastScoringTeam.value,
            rotationRequiredForTeam = _rotationRequiredForTeam.value,
            pendingPresenceTogglePublicId = publicId,
            pendingPresenceToggleRequestId = requestId
        )
        lastPushedUpdatedAt = pending.updatedAt
        viewModelScope.launch(Dispatchers.IO) {
            CloudSyncManager.pushLiveState(cloudGroupId, pending)
        }
    }

    fun startNextRound() {
        try {
            val conf = _currentGroupConfig.value
            if (conf.teamSize <= 0) {
                startNextRoundRebalance(conf.copy(teamSize = 6))
                return
            }
            val mode = BalancingMode.fromStoredValue(conf.balancingMode)
            when (mode) {
                BalancingMode.REBALANCE -> startNextRoundRebalance(conf)
                BalancingMode.REST -> startNextRoundRest(conf)
            }
        } finally {
            _guaranteedNextMatchPlayerIds.value = emptyList()
        }
    }


    private fun startNextRoundRebalance(conf: GroupConfig) {
        if (conf.teamSize <= 0) return
        TelemetryManager.logTeamsRebalanced(getApplication(), conf.groupType, conf.balancingMode)
        _rebalancedPlayerIds.value = emptySet()
        _autoSelectedLoserPlayerIds.value = emptySet()
        val activeWinners = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val losers = lastLosers.filter { _presentPlayerIds.value.contains(it.id) }

        val activeWinnerIds = activeWinners.map { it.id }.toSet()
        val loserIds = losers.map { it.id }.toSet()
        val existingWaitlistIds = _waitingList.value.map { it.id }.toSet()

        val newPresentPlayerIds = _presentPlayerIds.value.filter { id ->
            !activeWinnerIds.contains(id) && !loserIds.contains(id) && !existingWaitlistIds.contains(id)
        }

        val newPlayersWithToll = newPresentPlayerIds.mapNotNull { id ->
            currentGroupPlayers.value.find { it.id == id }?.let { applyTollIfNecessary(it) }
        }

        val waitlist =
            _waitingList.value.filter { p -> !activeWinnerIds.contains(p.id) && !loserIds.contains(p.id) } + newPlayersWithToll

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        fun getEffectiveGames(p: Player): Int =
            TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)

        val sortedLosers = if (conf.type.usesPositions) {
            PositionAssigner.orderByIdealComposition(losers, conf.teamSize, conf.guaranteeSetter) { getEffectiveGames(it) }
        } else {
            TeamBalancer.groupAndInterleave(losers) { getEffectiveGames(it) }
        }

        if (_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null
            TelemetryManager.logStreakBreakRebalance(getApplication(), conf.groupType)

            val sortedWinners = TeamBalancer.groupAndInterleave(activeWinners) { getEffectiveGames(it) }
            val winnersToKeep = sortedWinners.take(conf.teamSize * 2)
            val winnersToDrop = sortedWinners.drop(conf.teamSize * 2)

            val fullPool = (winnersToDrop + waitlist + sortedLosers).toMutableList()

            if (conf.type.usesPositions) {
                // Divide o time vencedor em duas metades equilibradas por Elo e só depois
                // completa as vagas de posição faltantes com o topo da fila de espera.
                val kept = winnersToKeep.take(conf.teamSize * 2)
                val (halfA, halfB) = PositionAssigner.splitByElo(kept)
                var pool: List<Player> = fullPool

                fun completeTeam(base: List<Player>): List<Player> {
                    val missing = conf.teamSize - base.size
                    if (missing <= 0) return base
                    val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                        base = base,
                        pool = pool,
                        count = missing,
                        teamSize = conf.teamSize,
                        teams = 1,
                        guaranteeSetter = conf.guaranteeSetter,
                        losers = losers
                    )
                    pool = leftover
                    return base + picked
                }

                val finalA = completeTeam(halfA)
                val finalB = completeTeam(halfB)
                _teamA.value = sortTeamPlayers(finalA)
                _teamB.value = sortTeamPlayers(finalB)
                _waitingList.value = pool
                val keptIds = kept.map { it.id }.toSet()
                _rebalancedPlayerIds.value = (finalA + finalB)
                    .map { it.id }
                    .filter { keptIds.contains(it) }
                    .toSet()
                refreshPositionAssignments()
                finishRoundSetup(loserIds)
                return
            }

            val (cA, cB) = splitPlayersEvenlyForRebalance(
                players = winnersToKeep,
                priorityEnabled = conf.priorityEnabled
            )

            if (shouldApplyPriorityRule(conf.priorityEnabled, cA + cB + fullPool)) {
                if (cA.none { it.isPriority } && cA.size < conf.teamSize) {
                    val p = fullPool.firstOrNull { it.isPriority }
                    if (p != null) {
                        cA.add(p)
                        fullPool.remove(p)
                    }
                }
                if (cB.none { it.isPriority } && cB.size < conf.teamSize) {
                    val p = fullPool.firstOrNull { it.isPriority }
                    if (p != null) {
                        cB.add(p)
                        fullPool.remove(p)
                    }
                }
            }

            val totalNeeded = (conf.teamSize - cA.size) + (conf.teamSize - cB.size)
            if (totalNeeded > 0) {
                val playersToAdd = fullPool.take(totalNeeded).sortedByDescending { it.elo }
                if (fullPool.size >= totalNeeded) {
                    fullPool.subList(0, totalNeeded).clear()
                } else {
                    fullPool.clear()
                }

                playersToAdd.forEach { p ->
                    if (cA.size < conf.teamSize && cB.size < conf.teamSize) {
                        if (cA.sumOf { it.elo } <= cB.sumOf { it.elo }) cA.add(p) else cB.add(p)
                    } else if (cA.size < conf.teamSize) {
                        cA.add(p)
                    } else {
                        cB.add(p)
                    }
                }
            }

            _teamA.value = sortTeamPlayers(cA)
            _teamB.value = sortTeamPlayers(cB)
            _waitingList.value = fullPool
            val winnersToKeepIds = winnersToKeep.map { it.id }.toSet()
            _rebalancedPlayerIds.value =
                (cA + cB).map { it.id }.filter { winnersToKeepIds.contains(it) }.toSet()
        } else {
            val previousWinnerSide = _streakOwner.value
            var resetStreakAfterAutomaticReplacement = false
            var teamWin = activeWinners.toMutableList()
            var remainingPool = (waitlist + sortedLosers).toMutableList()
            val guaranteedIdsSet = _guaranteedNextMatchPlayerIds.value.toSet()
            val shuffledPool = remainingPool.shuffled()

            if (teamWin.size > conf.teamSize) {
                val sorted = TeamBalancer.groupAndInterleave(teamWin.toList()) { getEffectiveGames(it) }
                teamWin = sorted.take(conf.teamSize).toMutableList()
                val droppedWinners = TeamBalancer.interleaveByElo(sorted.drop(conf.teamSize))
                remainingPool.addAll(0, droppedWinners)
            } else if (teamWin.size < conf.teamSize) {
                val guaranteedOrdered = sortPlayersByUsageForSelection(
                    players = shuffledPool.filter { guaranteedIdsSet.contains(it.id) },
                    getEffectiveGames = ::getEffectiveGames
                )
                if (conf.type.usesPositions) {
                    // Garantidos primeiro; entre os demais, quem cobre as vagas ainda descobertas.
                    val orderedPool = orderGuaranteedFirst(remainingPool, guaranteedOrdered)
                    val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                        base = teamWin,
                        pool = orderedPool,
                        count = conf.teamSize - teamWin.size,
                        teamSize = conf.teamSize,
                        teams = 1,
                        guaranteeSetter = conf.guaranteeSetter,
                        losers = losers
                    )
                    if (picked.isNotEmpty()) {
                        teamWin.addAll(picked)
                        resetStreakAfterAutomaticReplacement = true
                    }
                    remainingPool = leftover.toMutableList()
                } else {
                    while (teamWin.size < conf.teamSize && remainingPool.isNotEmpty()) {
                        val shouldApplyPriorityRuleInWinner =
                            shouldApplyPriorityRule(conf.priorityEnabled, teamWin + remainingPool)
                        val needsPriorityInWinner = shouldApplyPriorityRuleInWinner && teamWin.none { it.isPriority }
                        val candidate = when {
                            needsPriorityInWinner -> {
                                remainingPool.firstOrNull { it.isPriority }
                                    ?: guaranteedOrdered.firstOrNull { guaranteed -> remainingPool.any { it.id == guaranteed.id } }
                                    ?: remainingPool.firstOrNull()
                            }
                            else -> guaranteedOrdered.firstOrNull { guaranteed ->
                                remainingPool.any { it.id == guaranteed.id }
                            } ?: remainingPool.firstOrNull()
                        }
                        if (candidate == null) {
                            break
                        }
                        teamWin.add(candidate)
                        remainingPool.remove(candidate)
                        resetStreakAfterAutomaticReplacement = true
                    }
                }
            }

            val teamChal = mutableListOf<Player>()
            if (conf.type.usesPositions) {
                val guaranteedOrdered = sortPlayersByUsageForSelection(
                    players = remainingPool.filter { guaranteedIdsSet.contains(it.id) },
                    getEffectiveGames = ::getEffectiveGames
                )
                val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                    base = emptyList(),
                    pool = orderGuaranteedFirst(remainingPool, guaranteedOrdered),
                    count = conf.teamSize,
                    teamSize = conf.teamSize,
                    teams = 1,
                    guaranteeSetter = conf.guaranteeSetter,
                    losers = losers
                )
                teamChal.addAll(picked)
                remainingPool = leftover.toMutableList()
            } else {
                val shouldApplyPriorityRuleInRound = shouldApplyPriorityRule(conf.priorityEnabled, teamWin + remainingPool)

                if (shouldApplyPriorityRuleInRound) {
                    val priorityPlayer = remainingPool.firstOrNull { it.isPriority }
                    if (priorityPlayer != null) {
                        teamChal.add(priorityPlayer); remainingPool.remove(priorityPlayer)
                    }
                }

                val slotsNeeded = conf.teamSize - teamChal.size
                if (slotsNeeded > 0) {
                    val guaranteedOrdered = sortPlayersByUsageForSelection(
                        players = remainingPool.filter { guaranteedIdsSet.contains(it.id) },
                        getEffectiveGames = ::getEffectiveGames
                    )
                    val guaranteedPicked = guaranteedOrdered.take(slotsNeeded)
                    teamChal.addAll(guaranteedPicked)
                    remainingPool.removeAll(guaranteedPicked)
                    val pendingSlots = conf.teamSize - teamChal.size
                    if (pendingSlots > 0) {
                        val queuePicked = remainingPool.take(pendingSlots)
                        teamChal.addAll(queuePicked)
                        remainingPool.removeAll(queuePicked)
                    }
                }
            }

            _waitingList.value = remainingPool
            if (previousWinnerSide == "B") {
                _teamB.value = sortTeamPlayers(teamWin); _teamA.value = sortTeamPlayers(teamChal)
            } else {
                _teamA.value = sortTeamPlayers(teamWin); _teamB.value = sortTeamPlayers(teamChal)
                if (!resetStreakAfterAutomaticReplacement) {
                    _streakOwner.value = "A"
                }
            }

            if (resetStreakAfterAutomaticReplacement) {
                _currentStreak.value = 0
                _streakOwner.value = null
            }
        }
        _autoSelectedLoserPlayerIds.value = (_teamA.value + _teamB.value)
            .map { it.id }
            .filter { loserIds.contains(it) }
            .toSet()

        _hasPreviousMatch.value = false
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
        refreshPositionAssignments()
    }

    /** Reordena o pool colocando os jogadores garantidos na frente, preservando a ordem original. */
    private fun orderGuaranteedFirst(pool: List<Player>, guaranteed: List<Player>): List<Player> {
        if (guaranteed.isEmpty()) return pool
        val guaranteedIds = guaranteed.map { it.id }.toSet()
        val head = guaranteed.filter { g -> pool.any { it.id == g.id } }
        return head + pool.filterNot { guaranteedIds.contains(it.id) }
    }

    /**
     * Ordenação estável para escolhas automáticas: prioriza menor uso do dia e,
     * em empate, menor histórico total de partidas.
     */
    private fun sortPlayersByUsageForSelection(
        players: List<Player>,
        getEffectiveGames: (Player) -> Int
    ): List<Player> {
        return players.sortedWith(
            compareBy<Player> { getEffectiveGames(it) }
                .thenBy { it.matchesPlayed }
                .thenBy { it.id }
        )
    }

    /** Encerramento comum de uma rodada: destaca perdedores reaproveitados e reinicia o placar. */
    private fun finishRoundSetup(loserIds: Set<Int>) {
        _autoSelectedLoserPlayerIds.value = (_teamA.value + _teamB.value)
            .map { it.id }
            .filter { loserIds.contains(it) }
            .toSet()
        _hasPreviousMatch.value = false
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    private fun startNextRoundRest(conf: GroupConfig) {
        if (conf.teamSize <= 0) return
        _rebalancedPlayerIds.value = emptySet()
        _autoSelectedLoserPlayerIds.value = emptySet()
        _roundCounter.value += 1
        if (tryScheduleReturningTeamMatchIfAny(conf)) return

        val activeWinners = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val losers = lastLosers.filter { _presentPlayerIds.value.contains(it.id) }

        val activeWinnerIds = activeWinners.map { it.id }.toSet()
        val loserIds = losers.map { it.id }.toSet()
        val existingWaitlistIds = _waitingList.value.map { it.id }.toSet()

        val newPresentPlayerIds = _presentPlayerIds.value.filter { id ->
            !activeWinnerIds.contains(id) && !loserIds.contains(id) && !existingWaitlistIds.contains(id)
        }

        val newPlayersWithToll = newPresentPlayerIds.mapNotNull { id ->
            currentGroupPlayers.value.find { it.id == id }?.let { applyTollIfNecessary(it) }
        }

        val waitlist = (
            _waitingList.value.filter { p -> !activeWinnerIds.contains(p.id) && !loserIds.contains(p.id) } +
                newPlayersWithToll
            ).distinctBy { it.id }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        fun getEffectiveGames(p: Player): Int =
            TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)

        val sortedLosers = if (conf.type.usesPositions) {
            PositionAssigner.orderByIdealComposition(losers, conf.teamSize, conf.guaranteeSetter) { getEffectiveGames(it) }
        } else {
            TeamBalancer.groupAndInterleave(losers) { getEffectiveGames(it) }
        }
        val fullTeamsInWait = waitlist.size / conf.teamSize
        val winnerSide = _streakOwner.value

        fun buildChallengerTeam(
            basePlayers: List<Player>,
            fillPool: List<Player>
        ): Pair<List<Player>, List<Player>> {
            val team = basePlayers.take(conf.teamSize).toMutableList()
            val remaining = fillPool.toMutableList()
            if (team.size < conf.teamSize) {
                if (conf.type.usesPositions) {
                    val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                        base = team,
                        pool = remaining,
                        count = conf.teamSize - team.size,
                        teamSize = conf.teamSize,
                        teams = 1,
                        guaranteeSetter = conf.guaranteeSetter,
                        losers = fillPool
                    )
                    team.addAll(picked)
                    return team to leftover
                }
                if (shouldApplyPriorityRule(conf.priorityEnabled, team + remaining)) {
                    val priorityPlayer = remaining.firstOrNull { it.isPriority }
                    if (priorityPlayer != null) {
                        team.add(priorityPlayer)
                        remaining.remove(priorityPlayer)
                    }
                }
                while (team.size < conf.teamSize && remaining.isNotEmpty()) {
                    team.add(remaining.removeAt(0))
                }
            }
            return team to remaining
        }

        if (_currentStreak.value < conf.victoryLimit) {
            startNextRoundRebalance(conf)
            return
        }

        _currentStreak.value = 0
        _streakOwner.value = null

        when {
            fullTeamsInWait >= 2 -> {
                val team1: List<Player>
                val team2: List<Player>
                if (conf.type.usesPositions) {
                    val assignment = PositionAssigner.buildBalancedTeams(
                        waitlist.take(conf.teamSize * 2),
                        conf.teamSize,
                        conf.guaranteeSetter
                    )
                    team1 = assignment.teamA
                    team2 = assignment.teamB
                } else {
                    team1 = waitlist.take(conf.teamSize)
                    team2 = waitlist.drop(conf.teamSize).take(conf.teamSize)
                }
                val remainingWait = waitlist.drop(conf.teamSize * 2)
                val restedWinners = activeWinners.map { applyTollIfNecessary(it) }

                _teamA.value = sortTeamPlayers(team1)
                _teamB.value = sortTeamPlayers(team2)

                val returnRound = _roundCounter.value + 1
                val restMap = _restingPlayers.value.toMutableMap()
                restedWinners.forEach { restMap[it.id] = returnRound }
                _restingPlayers.value = restMap
                _waitingList.value = (restedWinners + remainingWait + sortedLosers).distinctBy { it.id }
            }

            else -> {
                val reigningTeam = activeWinners.toMutableList()
                val queueForNextTeams = waitlist.toMutableList()
                if (reigningTeam.size < conf.teamSize) {
                    val needed = conf.teamSize - reigningTeam.size
                    val picks = if (conf.type.usesPositions) {
                        val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                            base = reigningTeam,
                            pool = queueForNextTeams,
                            count = needed,
                            teamSize = conf.teamSize,
                            teams = 1,
                            guaranteeSetter = conf.guaranteeSetter
                        )
                        reigningTeam.addAll(picked)
                        queueForNextTeams.clear()
                        queueForNextTeams.addAll(leftover)
                        emptyList()
                    } else {
                        queueForNextTeams.take(needed)
                    }
                    reigningTeam.addAll(picks)
                    if (picks.isNotEmpty()) queueForNextTeams.subList(0, picks.size).clear()
                }

                if (reigningTeam.size < conf.teamSize) {
                    startNextRoundRebalance(conf)
                    return
                }

                if (queueForNextTeams.size >= conf.teamSize) {
                    val teamFromWait: List<Player>
                    val remainingAfterTeam: List<Player>
                    if (conf.type.usesPositions) {
                        val (picked, leftover) = PositionAssigner.pickToCoverComposition(
                            base = emptyList(),
                            pool = queueForNextTeams,
                            count = conf.teamSize,
                            teamSize = conf.teamSize,
                            teams = 1,
                            guaranteeSetter = conf.guaranteeSetter
                        )
                        teamFromWait = picked
                        remainingAfterTeam = leftover
                    } else {
                        teamFromWait = queueForNextTeams.take(conf.teamSize)
                        remainingAfterTeam = queueForNextTeams.drop(conf.teamSize)
                    }
                    if (winnerSide == "B") {
                        _teamA.value = sortTeamPlayers(teamFromWait)
                        _teamB.value = sortTeamPlayers(reigningTeam)
                    } else {
                        _teamA.value = sortTeamPlayers(reigningTeam)
                        _teamB.value = sortTeamPlayers(teamFromWait)
                    }

                    _waitingList.value = (remainingAfterTeam + sortedLosers).distinctBy { it.id }
                } else {
                    val (challengerTeam, remainingWait) = buildChallengerTeam(queueForNextTeams, sortedLosers)

                    if (winnerSide == "B") {
                        _teamA.value = sortTeamPlayers(challengerTeam)
                        _teamB.value = sortTeamPlayers(reigningTeam)
                    } else {
                        _teamA.value = sortTeamPlayers(reigningTeam)
                        _teamB.value = sortTeamPlayers(challengerTeam)
                    }

                    _waitingList.value = remainingWait.distinctBy { it.id }
                }
            }
        }
        _autoSelectedLoserPlayerIds.value = (_teamA.value + _teamB.value)
            .map { it.id }
            .filter { loserIds.contains(it) }
            .toSet()

        _hasPreviousMatch.value = false
        resetScoresAndPointIndicator()
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
        refreshPositionAssignments()
    }


    private fun formatElo(elo: Double): String = String.format(Locale.US, "%.2f", elo)

    private fun normalizePersonName(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ").take(MAX_PLAYER_NAME_LENGTH)
    }

    private fun normalizeGroupName(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ").take(MAX_GROUP_NAME_LENGTH)
    }

    private fun canonicalPersonName(name: String): String {
        return canonicalizePersonNameCompat(name)
    }

    private fun areSameCanonicalName(a: String, b: String): Boolean {
        return canonicalPersonName(a) == canonicalPersonName(b)
    }

    private fun normalizeTeamNamesSnapshot(raw: String): String {
        return normalizeTeamSnapshotWithIds(
            rawNames = raw,
            rawIds = "",
            normalizeName = ::normalizePersonName
        ).names
    }

    fun importData(uri: Uri, type: CsvType, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if (type == CsvType.BACKUP_COMPLETO) {
                TelemetryManager.logBackupImported(context)
            } else {
                TelemetryManager.logCsvImported(context, type.name)
            }
            try {
                val contentResolver = context.contentResolver
                if (type == CsvType.BACKUP_COMPLETO) {
                    val json =
                        BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).use { it.readText() }
                    val backup = Gson().fromJson(json, BackupData::class.java)

                    if (backup != null) {
                        val rawSafePlayers = backup.players.map { p ->
                            p.copy(
                                id = if (p.id > 0) p.id else 0,
                                name = normalizePersonName(p.name).ifBlank { "Desconhecido" },
                                // Must match the group's own name length limit (MAX_GROUP_NAME_LENGTH):
                                // older backups could carry a longer groupName than the app currently
                                // allows when renaming, which made the rename dialog silently reject
                                // every keystroke until the name was saved once and got truncated.
                                groupName = normalizeGroupName(p.groupName),
                                // Backups antigos (anteriores ao publicId) não têm esse campo no
                                // JSON: o Gson ignora o valor padrão do Kotlin e deixa null, o que
                                // quebra a constraint NOT NULL da coluna ao inserir. Gera um novo.
                                publicId = (p.publicId ?: "").ifBlank { java.util.UUID.randomUUID().toString() }
                            )
                        }
                        val safeGroupConfig = backup.groupConfig?.let { gc ->
                            gc.copy(
                                groupName = normalizeGroupName(gc.groupName),
                                publicId = (gc.publicId ?: "").ifBlank { java.util.UUID.randomUUID().toString() }
                            )
                        }

                        val rawSafeHistory = backup.history.map { h ->
                            val teamASnapshot = normalizeTeamSnapshotWithIds(
                                rawNames = h.teamA,
                                rawIds = h.teamAIds,
                                normalizeName = ::normalizePersonName
                            )
                            val teamBSnapshot = normalizeTeamSnapshotWithIds(
                                rawNames = h.teamB,
                                rawIds = h.teamBIds,
                                normalizeName = ::normalizePersonName
                            )
                            h.copy(
                                id = 0,
                                date = h.date.take(20),
                                teamA = teamASnapshot.names,
                                teamB = teamBSnapshot.names,
                                teamAIds = teamASnapshot.ids,
                                teamBIds = teamBSnapshot.ids,
                                winner = h.winner.take(50),
                                groupName = normalizeGroupName(h.groupName)
                            )
                        }

                        val rawSafeLogs = backup.logs.map { l ->
                            l.copy(
                                id = 0,
                                playerNameSnapshot = normalizePersonName(l.playerNameSnapshot)
                                    .ifBlank { "Desconhecido" },
                                date = l.date.take(20),
                                groupName = normalizeGroupName(l.groupName)
                            )
                        }


                        // Ids/publicIds do backup podem colidir com jogadores já existentes no
                        // dispositivo (de outro grupo ou de um import parcial anterior). Como o
                        // insert usa IGNORE em conflito, isso descartaria o jogador em silêncio.
                        val existingPlayerIds = repository.getAllPlayerIds().toSet()
                        val existingPlayerPublicIds = repository.getAllPlayerPublicIds().toSet()
                        val (safePlayers, safeHistory, safeLogs) = remapCollidingPlayerIdentities(
                            players = rawSafePlayers,
                            history = rawSafeHistory,
                            logs = rawSafeLogs,
                            existingIds = existingPlayerIds,
                            existingPublicIds = existingPlayerPublicIds
                        )

                        val importedGroups = (safePlayers.map { it.groupName } +
                            safeHistory.map { it.groupName } +
                            safeLogs.map { it.groupName }).toSet()
                        val existingGroups = repository.getAllGroupNames().toSet()
                        val overlapping = importedGroups.intersect(existingGroups).toList()
                        val duplicatePlayerNames = collectDuplicatePlayerNames(safePlayers)
                        val duplicatePlayerGroups = safePlayers.groupBy { it.groupName }
                            .mapValues { (_, players) ->
                                players.groupBy { canonicalizePersonNameCompat(it.name) }
                                    .count { it.value.size > 1 }
                            }
                            .filterValues { it > 0 }

                        if (overlapping.isNotEmpty() || duplicatePlayerNames.isNotEmpty()) {
                            _pendingMergeImport.value = PendingMergeImportData(
                                players = safePlayers,
                                history = safeHistory,
                                logs = safeLogs,
                                overlappingGroups = overlapping,
                                duplicatePlayerNames = duplicatePlayerNames,
                                duplicatePlayerGroups = duplicatePlayerGroups,
                                groupConfig = safeGroupConfig
                            )
                        } else {
                            performImportWithDedup(safePlayers, safeHistory, safeLogs, emptySet())
                            finalizeGroupImport(
                                groupConfig = safeGroupConfig,
                                groupNameFallback = safePlayers.firstOrNull()?.groupName
                                    ?: safeHistory.firstOrNull()?.groupName
                                    ?: safeLogs.firstOrNull()?.groupName
                            )
                        }
                    } else {
                        Log.e("Import", context.getString(R.string.invalid_backup_format))
                    }
                } else {
                    val rows = readTabularRows(context, uri)
                    if (rows.isEmpty()) {
                        _uiMessage.value = context.getString(R.string.import_error_empty_or_unreadable)
                        return@launch
                    }
                    val dataLines = rows.drop(1)

                    when (type) {
                        CsvType.JOGADORES -> {
                            val list = dataLines.mapNotNull { cols ->
                                try {
                                    if (cols.size >= 6) {
                                        Player(
                                            id = 0,
                                            name = cols[1].takeIf { it.isNotBlank() }
                                                ?.let { normalizePersonName(it) }
                                                ?.ifBlank { "Desconhecido" }
                                                ?: "Desconhecido",
                                            elo = cols[2].toDoubleOrNull() ?: 1200.0,
                                            matchesPlayed = cols[3].toIntOrNull() ?: 0,
                                            victories = cols[4].toIntOrNull() ?: 0,
                                            groupName = cols[5].takeIf { it.isNotBlank() }
                                                ?.let(::normalizeGroupName)
                                                ?: DEFAULT_GROUP_NAME,
                                            isPriority = cols.getOrElse(6) { "false" }
                                                .toBooleanStrictOrNull() ?: false,
                                            dailyToll = cols.getOrElse(7) { "0" }.toIntOrNull()
                                                ?: 0,
                                            tollDate = cols.getOrElse(8) { "" }.take(20),
                                            preferredPosition = PlayerPosition
                                                .fromStoredValue(cols.getOrElse(9) { "" })?.name,
                                            secondaryPosition = PlayerPosition
                                                .fromStoredValue(cols.getOrElse(10) { "" })?.name
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (list.isNotEmpty()) {
                                val importedGroups = list.map { it.groupName }.toSet()
                                val existingGroups = repository.getAllGroupNames().toSet()
                                val overlapping = importedGroups.intersect(existingGroups)
                                val duplicatePlayerNames = collectDuplicatePlayerNames(list)
                                val duplicatePlayerGroups = list.groupBy { it.groupName }
                                    .mapValues { (_, players) ->
                                        players.groupBy { canonicalizePersonNameCompat(it.name) }
                                            .count { it.value.size > 1 }
                                    }
                                    .filterValues { it > 0 }
                                if (overlapping.isNotEmpty() || duplicatePlayerNames.isNotEmpty()) {
                                    _pendingMergeImport.value = PendingMergeImportData(
                                        players = list, history = emptyList(), logs = emptyList(),
                                        overlappingGroups = overlapping.toList(),
                                        duplicatePlayerNames = duplicatePlayerNames,
                                        duplicatePlayerGroups = duplicatePlayerGroups
                                    )
                                } else {
                                    performImportWithDedup(list, emptyList(), emptyList(), emptySet())
                                    finalizeGroupImport(
                                        groupConfig = null,
                                        groupNameFallback = list.firstOrNull()?.groupName
                                    )
                                }
                            } else {
                                _uiMessage.value = context.getString(R.string.import_error_no_valid_rows)
                            }
                        }

                        CsvType.HISTORICO -> {
                            val list = dataLines.mapNotNull { cols ->
                                try {
                                    if (cols.size >= 6) {
                                        MatchHistory(
                                            id = 0,
                                            date = cols[0].takeIf { it.isNotBlank() }?.take(20)
                                                ?: SimpleDateFormat(
                                                    "dd/MM/yyyy HH:mm",
                                                    Locale.getDefault()
                                                ).format(Date()),
                                            teamA = normalizeTeamNamesSnapshot(cols[1]),
                                            teamB = normalizeTeamNamesSnapshot(cols[2]),
                                            winner = cols[3].take(50),
                                            eloPoints = cols[4].toDoubleOrNull() ?: 0.0,
                                            groupName = cols[5].takeIf { it.isNotBlank() }
                                                ?.let(::normalizeGroupName)
                                                ?: DEFAULT_GROUP_NAME,
                                            teamAAverageElo = cols.getOrElse(6) { "" }
                                                .toDoubleOrNull(),
                                            teamBAverageElo = cols.getOrElse(7) { "" }
                                                .toDoubleOrNull(),
                                            teamAScore = cols.getOrElse(8) { "" }.toIntOrNull(),
                                            teamBScore = cols.getOrElse(9) { "" }.toIntOrNull(),
                                            startTimestamp = cols.getOrElse(10) { "" }
                                                .toLongOrNull(),
                                            endTimestamp = cols.getOrElse(11) { "" }
                                                .toLongOrNull()
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (list.isNotEmpty()) {
                                val importedGroups = list.map { it.groupName }.toSet()
                                val existingGroups = repository.getAllGroupNames().toSet()
                                val overlapping = importedGroups.intersect(existingGroups)
                                if (overlapping.isNotEmpty()) {
                                    _pendingMergeImport.value = PendingMergeImportData(
                                        players = emptyList(), history = list, logs = emptyList(),
                                        overlappingGroups = overlapping.toList()
                                    )
                                } else {
                                    performImportWithDedup(emptyList(), list, emptyList(), emptySet())
                                    finalizeGroupImport(
                                        groupConfig = null,
                                        groupNameFallback = list.firstOrNull()?.groupName
                                    )
                                }
                            } else {
                                _uiMessage.value = context.getString(R.string.import_error_no_valid_rows)
                            }
                        }

                        CsvType.ELO_LOGS -> {
                            val list = dataLines.mapNotNull { cols ->
                                try {
                                    if (cols.size >= 6) {
                                        PlayerEloLog(
                                            id = 0,
                                            playerId = cols[1].toIntOrNull() ?: 0,
                                            playerNameSnapshot = normalizePersonName(cols[2])
                                                .ifBlank { "Desconhecido" },
                                            date = cols[3].takeIf { it.isNotBlank() }?.take(20)
                                                ?: SimpleDateFormat(
                                                    "yyyy-MM-dd",
                                                    Locale.getDefault()
                                                ).format(Date()),
                                            elo = cols[4].toDoubleOrNull() ?: 1200.0,
                                            groupName = cols[5].takeIf { it.isNotBlank() }
                                                ?.let(::normalizeGroupName)
                                                ?: DEFAULT_GROUP_NAME,
                                            won = cols.getOrElse(6) { "" }.toBooleanStrictOrNull()
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (list.isNotEmpty()) {
                                val importedGroups = list.map { it.groupName }.toSet()
                                val existingGroups = repository.getAllGroupNames().toSet()
                                val overlapping = importedGroups.intersect(existingGroups)
                                if (overlapping.isNotEmpty()) {
                                    _pendingMergeImport.value = PendingMergeImportData(
                                        players = emptyList(), history = emptyList(), logs = list,
                                        overlappingGroups = overlapping.toList()
                                    )
                                } else {
                                    performImportWithDedup(emptyList(), emptyList(), list, emptySet())
                                    finalizeGroupImport(
                                        groupConfig = null,
                                        groupNameFallback = list.firstOrNull()?.groupName
                                    )
                                }
                            } else {
                                _uiMessage.value = context.getString(R.string.import_error_no_valid_rows)
                            }
                        }

                        else -> {}
                    }
                }
            } catch (e: UnsupportedXlsException) {
                _uiMessage.value = context.getString(R.string.import_error_xls_unsupported)
            } catch (e: Exception) {
                Log.e("Import", "Erro: ${e.message}")
                _uiMessage.value = context.getString(R.string.import_error_generic)
                TelemetryManager.recordException(e)
            }
        }
    }

    fun confirmMergeImport(renameDuplicates: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = _pendingMergeImport.value ?: return@launch
            try {
                val resolvedPlayers = if (renameDuplicates) {
                    val existingNamesByGroup = data.overlappingGroups.associateWith { groupName ->
                        repository.getPlayersByGroupSync(groupName).map { canonicalPersonName(it.name) }.toSet()
                    }
                    resolveImportedPlayersWithAutoRename(data.players, existingNamesByGroup).first
                } else {
                    val existingNamesByGroup = data.overlappingGroups.associateWith { groupName ->
                        repository.getPlayersByGroupSync(groupName).map { canonicalPersonName(it.name) }.toSet()
                    }
                    resolveImportedPlayersForInsert(data.players, existingNamesByGroup).first
                }

                val groupedByGroup = resolvedPlayers.groupBy { it.groupName }
                groupedByGroup.forEach { (_, playersInGroup) ->
                    if (playersInGroup.isNotEmpty()) repository.insertPlayers(playersInGroup)
                }

                val historyByGroup = data.history.groupBy { it.groupName }
                for ((groupName, groupHistory) in historyByGroup) {
                    val existingKeys = repository.getHistoryByGroupSync(groupName)
                        .map { Triple(it.date, it.teamA, it.teamB) }.toSet()
                    val toInsert = groupHistory.filter { Triple(it.date, it.teamA, it.teamB) !in existingKeys }
                    if (toInsert.isNotEmpty()) repository.insertHistoryList(toInsert)
                }

                val logsByGroup = data.logs.groupBy { it.groupName }
                for ((groupName, groupLogs) in logsByGroup) {
                    val existingKeys = repository.getEloLogsByGroupSync(groupName)
                        .map { Pair(it.playerNameSnapshot, it.date) }.toSet()
                    val toInsert = groupLogs.filter { Pair(it.playerNameSnapshot, it.date) !in existingKeys }
                    if (toInsert.isNotEmpty()) repository.insertEloLogs(toInsert)
                }
                finalizeGroupImport(
                    groupConfig = data.groupConfig,
                    groupNameFallback = data.players.firstOrNull()?.groupName
                        ?: data.history.firstOrNull()?.groupName
                        ?: data.logs.firstOrNull()?.groupName
                )
            } catch (e: Exception) {
                Log.e("Import", "Erro ao mesclar importação: ${e.message}")
                _uiMessage.value = getApplication<Application>().getString(R.string.import_error_generic)
                TelemetryManager.recordException(e)
            }
            _pendingMergeImport.value = null
        }
    }

    fun cancelMergeImport() {
        _pendingMergeImport.value = null
    }

    /**
     * Após uma importação de backup (.vlz), salva a config do grupo importado (se for um grupo
     * novo, sem sobrescrever a config de um grupo já existente) e troca o grupo ativo para ele,
     * para que as telas exibam os dados recém-importados sem precisar de troca manual pelo usuário.
     */
    private suspend fun finalizeGroupImport(groupConfig: GroupConfig?, groupNameFallback: String?) {
        val targetGroupName = (groupConfig?.groupName?.takeIf { it.isNotBlank() } ?: groupNameFallback)
            ?.takeIf { it.isNotBlank() } ?: return
        if (groupConfig != null && repository.getGroupConfig(targetGroupName) == null) {
            // Um grupo importado sempre já está "pronto para uso": força onboarding concluído
            // independente do valor vindo do backup, para não reabrir o assistente de configuração.
            repository.saveGroupConfig(
                groupConfig.copy(groupName = targetGroupName, onboardingStep = ONBOARDING_STEP_COMPLETE)
            )
        }
        loadGroupConfig(targetGroupName)
    }

    private suspend fun performImportWithDedup(
        players: List<Player>,
        history: List<MatchHistory>,
        logs: List<PlayerEloLog>,
        overlappingGroups: Set<String>
    ) {
        val existingNamesByGroup = overlappingGroups.associateWith { groupName ->
            repository.getPlayersByGroupSync(groupName).map { canonicalPersonName(it.name) }.toSet()
        }
        val (playersToInsert, _) = resolveImportedPlayersForInsert(players, existingNamesByGroup)
        val playersByGroup = playersToInsert.groupBy { it.groupName }
        for ((groupName, groupPlayers) in playersByGroup) {
            if (groupPlayers.isNotEmpty()) repository.insertPlayers(groupPlayers)
        }

        // History: dedup by date+teamA+teamB within same group
        val historyByGroup = history.groupBy { it.groupName }
        for ((groupName, groupHistory) in historyByGroup) {
            val toInsert = if (groupName in overlappingGroups) {
                val existingKeys = repository.getHistoryByGroupSync(groupName)
                    .map { Triple(it.date, it.teamA, it.teamB) }.toSet()
                groupHistory.filter { Triple(it.date, it.teamA, it.teamB) !in existingKeys }
            } else {
                groupHistory
            }
            if (toInsert.isNotEmpty()) repository.insertHistoryList(toInsert)
        }

        // EloLogs: dedup by playerNameSnapshot+date within same group
        val logsByGroup = logs.groupBy { it.groupName }
        for ((groupName, groupLogs) in logsByGroup) {
            val toInsert = if (groupName in overlappingGroups) {
                val existingKeys = repository.getEloLogsByGroupSync(groupName)
                    .map { Pair(it.playerNameSnapshot, it.date) }.toSet()
                groupLogs.filter { Pair(it.playerNameSnapshot, it.date) !in existingKeys }
            } else {
                groupLogs
            }
            if (toInsert.isNotEmpty()) repository.insertEloLogs(toInsert)
        }
    }

    fun exportData(context: Context, type: CsvType, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (type == CsvType.BACKUP_COMPLETO) {
                TelemetryManager.logBackupExported(context)
            } else {
                TelemetryManager.logCsvExported(context, type.name)
            }
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "")
            val extension = if (type == CsvType.BACKUP_COMPLETO) "vlz" else "xlsx"
            val finalName =
                if (safeFileName.endsWith(".$extension")) safeFileName else "$safeFileName.$extension"

            if (type == CsvType.BACKUP_COMPLETO) {
                val groupName = _currentGroupConfig.value.groupName
                val backup = BackupData(
                    date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    players = currentGroupPlayers.value,
                    history = currentGroupHistory.value,
                    logs = currentGroupEloLogs.value,
                    groupConfig = _currentGroupConfig.value,
                    tournamentTeams = repository.getTournamentTeamsByGroupSync(groupName).takeIf { it.isNotEmpty() },
                    tournamentTeamMembers = repository.getTournamentTeamMembersByGroupSync(groupName).takeIf { it.isNotEmpty() },
                    tournamentMatches = repository.getTournamentMatchesByGroupSync(groupName).takeIf { it.isNotEmpty() },
                    groupLogs = repository.getGroupLogsByGroupSync(groupName).takeIf { it.isNotEmpty() }
                )
                val json = Gson().toJson(backup)
                shareFile(context, finalName, json, "application/octet-stream")
            } else {
                val (headers, rows) = when (type) {
                    CsvType.JOGADORES -> {
                        val headers = PLAYERS_CSV_HEADER.split(",")
                        val rows = currentGroupPlayers.value.map {
                            listOf(
                                it.id.toString(),
                                it.name,
                                formatElo(it.elo),
                                it.matchesPlayed.toString(),
                                it.victories.toString(),
                                it.groupName,
                                it.isPriority.toString(),
                                it.dailyToll.toString(),
                                it.tollDate,
                                it.preferredPosition.orEmpty(),
                                it.secondaryPosition.orEmpty()
                            )
                        }
                        headers to rows
                    }

                    CsvType.HISTORICO -> {
                        val headers = listOf(
                            "Data", "TimeA", "TimeB", "Vencedor", "EloGanho", "Grupo",
                            "MediaEloTimeA", "MediaEloTimeB", "PlacarTimeA", "PlacarTimeB",
                            "InicioPartida", "FimPartida"
                        )
                        val rows = currentGroupHistory.value.map {
                            listOf(
                                it.date,
                                it.teamA,
                                it.teamB,
                                it.winner,
                                formatElo(it.eloPoints),
                                it.groupName,
                                it.teamAAverageElo?.let { e -> formatElo(e) } ?: "",
                                it.teamBAverageElo?.let { e -> formatElo(e) } ?: "",
                                it.teamAScore?.toString() ?: "",
                                it.teamBScore?.toString() ?: "",
                                it.startTimestamp?.toString() ?: "",
                                it.endTimestamp?.toString() ?: ""
                            )
                        }
                        headers to rows
                    }

                    CsvType.ELO_LOGS -> {
                        val headers = listOf("ID", "PlayerID", "Nome", "Data", "Elo", "Grupo", "Vitoria")
                        val rows = currentGroupEloLogs.value.map {
                            listOf(
                                it.id.toString(),
                                it.playerId.toString(),
                                it.playerNameSnapshot,
                                it.date,
                                formatElo(it.elo),
                                it.groupName,
                                it.won?.toString() ?: ""
                            )
                        }
                        headers to rows
                    }

                    else -> emptyList<String>() to emptyList<List<String>>()
                }
                shareBytes(context, finalName, buildXlsxBytes(headers, rows), XLSX_MIME_TYPE)
            }
        }
    }

    /**
     * Gera e compartilha um modelo .xlsx de jogadores, com o cabeçalho real (mesma fonte usada em
     * [exportData]) e uma linha de exemplo claramente marcada, para o usuário preencher em lote
     * no Excel/Google Sheets/celular e importar depois via "Importar CSV > Jogadores" (que já lê
     * .xlsx normalmente, ver [readTabularRows]).
     */
    fun exportPlayersTemplate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val exampleGroup = _currentGroupConfig.value.groupName.takeIf { it.isNotBlank() }
                ?: DEFAULT_GROUP_NAME
            val headers = PLAYERS_CSV_HEADER.split(",")
            val exampleRow = listOf(
                "0",
                context.getString(R.string.template_example_name),
                "1200.00",
                "0",
                "0",
                exampleGroup,
                "false",
                "0",
                "",
                context.getString(R.string.template_position_hint),
                context.getString(R.string.template_secondary_position_hint)
            )
            val bytes = buildXlsxBytes(headers, listOf(exampleRow))
            shareBytes(context, "modelo_jogadores.xlsx", bytes, XLSX_MIME_TYPE)
        }
    }

    private fun shareFile(context: Context, name: String, content: String, mimeType: String) {
        shareBytes(context, name, content.toByteArray(), mimeType)
    }

    private fun shareBytes(context: Context, name: String, bytes: ByteArray, mimeType: String) {
        try {
            val file = File(context.cacheDir, name)
            FileOutputStream(file).use { it.write(bytes) }
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, context.getString(R.string.save_file, name))
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("Export", context.getString(R.string.error, e.message))
            TelemetryManager.recordException(e)
        }
    }

    /**
     * Gera os bytes de um arquivo .xlsx mínimo e válido (zip + XML escrito manualmente, sem
     * biblioteca externa), com uma única planilha contendo [headers] na primeira linha seguida de
     * [rows]. Usa células do tipo `inlineStr` para texto, evitando a necessidade de um
     * `sharedStrings.xml` separado. Espelha o parsing já existente em [readXlsxRows]/[parseSheetRows].
     */
    private fun buildXlsxBytes(headers: List<String>, rows: List<List<String>>): ByteArray {
        fun xmlEscape(s: String) = s.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")

        fun columnName(index: Int): String {
            var i = index
            val sb = StringBuilder()
            do {
                sb.insert(0, ('A' + (i % 26)))
                i = i / 26 - 1
            } while (i >= 0)
            return sb.toString()
        }

        val allRows = listOf(headers) + rows
        val sheetXml = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
            append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
            append("<sheetData>")
            allRows.forEachIndexed { rowIndex, row ->
                append("<row r=\"${rowIndex + 1}\">")
                row.forEachIndexed { colIndex, cell ->
                    val ref = "${columnName(colIndex)}${rowIndex + 1}"
                    val numeric = cell.toDoubleOrNull()
                    if (numeric != null && cell.isNotBlank()) {
                        append("<c r=\"$ref\"><v>${xmlEscape(cell)}</v></c>")
                    } else {
                        append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${xmlEscape(cell)}</t></is></c>")
                    }
                }
                append("</row>")
            }
            append("</sheetData>")
            append("</worksheet>")
        }

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            </Types>""".trimIndent()

        val rootRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
            </Relationships>""".trimIndent()

        val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets><sheet name="Jogadores" sheetId="1" r:id="rId1"/></sheets>
            </workbook>""".trimIndent()

        val workbookRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
            </Relationships>""".trimIndent()

        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zip ->
            fun writeEntry(name: String, data: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(data.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            writeEntry("[Content_Types].xml", contentTypesXml)
            writeEntry("_rels/.rels", rootRelsXml)
            writeEntry("xl/workbook.xml", workbookXml)
            writeEntry("xl/_rels/workbook.xml.rels", workbookRelsXml)
            writeEntry("xl/worksheets/sheet1.xml", sheetXml)
        }
        return outputStream.toByteArray()
    }

    fun shareBitmap(context: Context, bitmap: android.graphics.Bitmap, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val safeDate = date.replace(Regex("[^a-zA-Z0-9]"), "_")
                val fileName = "history_$safeDate.png"
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(intent, "Share history").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                Log.e("Share", "Erro ao compartilhar imagem: ${e.message}")
            }
        }
    }

    private fun smartSplit(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(current.toString().trim()); current.clear()
                }

                else -> current.append(c)
            }
        }
        result.add(current.toString().trim())
        return result.map { it.replace("\"", "").trim() }
    }

    /**
     * Lê um arquivo tabular (CSV/texto ou XLSX) apontado por [uri] e devolve uma lista de linhas,
     * cada uma já dividida em colunas. Isso permite que o restante do fluxo de importação
     * (JOGADORES/HISTORICO/ELO_LOGS) trabalhe sempre com `List<String>` por linha, seja a origem
     * um .csv puro ou uma planilha .xlsx exportada/editada no Excel, Google Sheets etc.
     * Arquivos .xls (formato binário antigo do Excel) são detectados e sinalizados via
     * [UnsupportedXlsException], pois exigiriam um parser binário próprio (fora de escopo).
     */
    private fun readTabularRows(context: Context, uri: Uri): List<List<String>> {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return emptyList()
        return when {
            isZipFormat(bytes) -> readXlsxRows(bytes)
            isLegacyXlsFormat(bytes) -> throw UnsupportedXlsException()
            else -> {
                String(bytes, Charsets.UTF_8).lineSequence()
                    .filter { it.isNotBlank() }
                    .map { smartSplit(it) }
                    .toList()
            }
        }
    }

    private fun isZipFormat(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()

    private fun isLegacyXlsFormat(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0xD0.toByte() && bytes[1] == 0xCF.toByte() &&
            bytes[2] == 0x11.toByte() && bytes[3] == 0xE0.toByte()

    /** Extrai as linhas/colunas da primeira planilha de um arquivo .xlsx (formato zip + XML). */
    private fun readXlsxRows(bytes: ByteArray): List<List<String>> {
        var sharedStrings: List<String> = emptyList()
        val sheetEntries = mutableMapOf<String, ByteArray>()

        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "xl/sharedStrings.xml" -> sharedStrings = parseSharedStrings(zis.readBytes())
                    entry.name.matches(Regex("xl/worksheets/sheet\\d+\\.xml")) ->
                        sheetEntries[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val firstSheetName = sheetEntries.keys.minByOrNull {
            Regex("\\d+").find(it)?.value?.toIntOrNull() ?: Int.MAX_VALUE
        } ?: return emptyList()

        return parseSheetRows(sheetEntries.getValue(firstSheetName), sharedStrings)
    }

    private fun parseSharedStrings(data: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        val parser = android.util.Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        var eventType = parser.eventType
        var currentSi: StringBuilder? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> currentSi = StringBuilder()
                    "t" -> currentSi?.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> if (parser.name == "si") {
                    strings.add(currentSi?.toString() ?: "")
                    currentSi = null
                }
            }
            eventType = try { parser.next() } catch (e: Exception) { XmlPullParser.END_DOCUMENT }
        }
        return strings
    }

    private fun parseSheetRows(data: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = android.util.Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(data), "UTF-8")
        var eventType = parser.eventType
        var currentRow: MutableList<String>? = null
        var currentColIndex = -1
        var currentCellType: String? = null
        var currentValue: StringBuilder? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        currentCellType = parser.getAttributeValue(null, "t")
                        val ref = parser.getAttributeValue(null, "r")
                        currentColIndex = ref?.let { columnLetterToIndex(it) } ?: (currentColIndex + 1)
                        currentValue = StringBuilder()
                    }
                    "v" -> currentValue?.append(parser.nextText())
                    "t" -> if (currentCellType == "inlineStr") currentValue?.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "c" -> {
                        val row = currentRow
                        if (row != null && currentColIndex >= 0) {
                            while (row.size <= currentColIndex) row.add("")
                            val raw = currentValue?.toString() ?: ""
                            row[currentColIndex] = when (currentCellType) {
                                "s" -> raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                                "b" -> if (raw == "1") "true" else "false"
                                else -> raw
                            }
                        }
                        currentCellType = null
                        currentValue = null
                    }
                    "row" -> {
                        currentRow?.let { rows.add(it) }
                        currentRow = null
                        currentColIndex = -1
                    }
                }
            }
            eventType = try { parser.next() } catch (e: Exception) { XmlPullParser.END_DOCUMENT }
        }
        return rows
    }

    /** Converte uma referência de célula do Excel (ex.: "C7") no índice de coluna 0-based (2). */
    private fun columnLetterToIndex(cellRef: String): Int {
        var idx = 0
        for (c in cellRef) {
            if (c.isLetter()) {
                idx = idx * 26 + (c.uppercaseChar() - 'A' + 1)
            } else break
        }
        return idx - 1
    }

    private class UnsupportedXlsException : Exception("Formato .xls (binário antigo) não suportado")

    fun standardizeJsonBackupData(jsonString: String): String {
        val gson = Gson()
        val data = gson.fromJson(jsonString, com.google.gson.JsonObject::class.java)
        data.addProperty("version", 1)
        val historyArray = data.getAsJsonArray("history")
        if (historyArray != null) {
            for (element in historyArray) {
                val match = element.asJsonObject
                if (!match.has("teamAIds")) match.addProperty("teamAIds", "")
                if (!match.has("teamBIds")) match.addProperty("teamBIds", "")
                val teamASnapshot = normalizeTeamSnapshotWithIds(
                    rawNames = match.get("teamA")?.asString ?: "",
                    rawIds = match.get("teamAIds")?.asString ?: "",
                    normalizeName = ::normalizePersonName
                )
                val teamBSnapshot = normalizeTeamSnapshotWithIds(
                    rawNames = match.get("teamB")?.asString ?: "",
                    rawIds = match.get("teamBIds")?.asString ?: "",
                    normalizeName = ::normalizePersonName
                )
                match.addProperty("teamA", teamASnapshot.names)
                match.addProperty("teamB", teamBSnapshot.names)
                match.addProperty("teamAIds", teamASnapshot.ids)
                match.addProperty("teamBIds", teamBSnapshot.ids)
                if (!match.has("teamAScore")) match.addProperty("teamAScore", 0)
                if (!match.has("teamBScore")) match.addProperty("teamBScore", 0)
                if (!match.has("teamAAverageElo")) match.addProperty("teamAAverageElo", 0.0)
                if (!match.has("teamBAverageElo")) match.addProperty("teamBAverageElo", 0.0)
                if (!match.has("startTimestamp")) match.addProperty("startTimestamp", 0L)
                if (!match.has("endTimestamp")) match.addProperty("endTimestamp", 0L)
            }
        }
        val playersArray = data.getAsJsonArray("players")
        if (playersArray != null) {
            var nextId = (playersArray.mapNotNull { it.asJsonObject.get("id")?.asInt }.maxOrNull() ?: 0) + 1
            for (element in playersArray) {
                val p = element.asJsonObject
                if (!p.has("id") || p.get("id").asInt <= 0) p.addProperty("id", nextId++)
                if (p.has("name")) {
                    val normalized = normalizePersonName(p.get("name").asString).ifBlank { "Desconhecido" }
                    p.addProperty("name", normalized)
                }
            }
        }
        val logsArray = data.getAsJsonArray("logs")
        if (logsArray != null) {
            for (element in logsArray) {
                val log = element.asJsonObject
                if (log.has("playerNameSnapshot")) {
                    val normalized = normalizePersonName(log.get("playerNameSnapshot").asString)
                        .ifBlank { "Desconhecido" }
                    log.addProperty("playerNameSnapshot", normalized)
                }
            }
        }
        return gson.toJson(data)
    }

    fun captureHistoryScreenAsImage(
        context: Context,
        view: android.view.View,
        matches: List<MatchHistory>?,
        matchSortMode: com.bismarck.voleimanager.app.ui.MatchSortMode?,
        players: List<com.bismarck.voleimanager.app.ui.HistoryPlayerInfo>?,
        playerSortMode: com.bismarck.voleimanager.app.ui.PlayerSortMode?,
        date: String,
        isDarkTheme: Boolean,
        showElo: Boolean,
        showScore: Boolean,
        matchDurationsMinutes: Map<Int, Int>? = null,
        averagePlayersEloText: String? = null,
        averageMatchDurationText: String? = null
    ) {
        val groupName = _currentGroupConfig.value.groupName
        val usesPositions = _currentGroupConfig.value.type.usesPositions
        val teamAColorFamily = com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily(effectiveTeamAColor.value, isDarkTheme)
        val teamBColorFamily = com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily(effectiveTeamBColor.value, isDarkTheme)
        val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
            setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
            setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
            setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

            setContent {
                com.bismarck.voleimanager.app.ui.theme.AppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                    androidx.compose.material3.Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                        com.bismarck.voleimanager.app.ui.ExportableImageContent(
                            matches = matches,
                            matchSortMode = matchSortMode,
                            players = players,
                            playerSortMode = playerSortMode,
                            groupName = groupName,
                            date = date,
                            isDarkTheme = isDarkTheme,
                            showElo = showElo,
                            showScore = showScore,
                            matchDurationsMinutes = matchDurationsMinutes,
                            averagePlayersEloText = averagePlayersEloText,
                            averageMatchDurationText = averageMatchDurationText,
                            usesPositions = usesPositions,
                            teamAColorFamily = teamAColorFamily,
                            teamBColorFamily = teamBColorFamily
                        )
                    }
                }
            }
        }
        val scrollView = android.widget.ScrollView(context).apply {
            addView(composeView)
            alpha = 0f
            isVerticalScrollBarEnabled = false
        }
        val root = view.rootView as? android.view.ViewGroup
        if (root != null) {
            root.addView(scrollView, android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT))
            scrollView.postDelayed({
                try {
                    composeView.measure(
                        android.view.View.MeasureSpec.makeMeasureSpec(1440, android.view.View.MeasureSpec.EXACTLY),
                        android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                    )
                    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
                    if (composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
                        val bitmap = android.graphics.Bitmap.createBitmap(composeView.measuredWidth, composeView.measuredHeight, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        composeView.draw(canvas)
                        shareBitmap(context, bitmap, date)
                    }
                } catch (e: Exception) { e.printStackTrace() } finally { root.removeView(scrollView) }
            }, 500)
        }
    }

    fun clearRecentGameData() {
        val wasQualifyingClear = isGameInProgress() || _hasPreviousMatch.value
        resetGameState()
        clearSavedGameState()
        if (wasQualifyingClear) {
            registerQualifyingGameClear()
        }
    }
}

class VoleiViewModelFactory(
    private val application: Application,
    private val repository: VoleiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(VoleiViewModel::class.java)) {
            return VoleiViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

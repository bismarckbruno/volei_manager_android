package com.bismarck.voleimanager.app.ui.viewmodel

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
import com.bismarck.voleimanager.app.util.EloCalculator
import com.bismarck.voleimanager.app.util.PositionAssigner
import com.bismarck.voleimanager.app.util.TeamBalancer
import com.bismarck.voleimanager.app.util.TollCalculator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.Normalizer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bismarck.voleimanager.app.data.model.BalancingMode

const val DEFAULT_GROUP_NAME = "Geral"
const val MAX_GROUP_NAME_LENGTH = 20
const val MAX_PLAYER_NAME_LENGTH = 24
private const val AUTO_CLEAR_GAME_AFTER_LAST_MATCH_MS = 12L * 60L * 60L * 1000L

enum class Screen { GAME, HISTORY, FAQ, ABOUT }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CsvType { JOGADORES, HISTORICO, ELO_LOGS, BACKUP_COMPLETO }
enum class TeamColorTheme { DEFAULT, RED_GREEN, PURPLE_ORANGE }

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
    val duplicatePlayerGroups: Map<String, Int> = emptyMap()
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

    fun clearUiMessage() {
        _uiMessage.value = null
    }
    private val _currentScreen = MutableStateFlow(Screen.GAME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    private val _isGroupDataLoading = MutableStateFlow(true)
    val isGroupDataLoading: StateFlow<Boolean> = _isGroupDataLoading.asStateFlow()
    private var groupLoadToken = 0

    private val _currentGroupConfig = MutableStateFlow(
        GroupConfig(groupName = "", onboardingStep = ONBOARDING_STEP_GROUP_NAME)
    )
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

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

    private val _teamColorTheme = MutableStateFlow(TeamColorTheme.DEFAULT)
    val teamColorTheme: StateFlow<TeamColorTheme> = _teamColorTheme.asStateFlow()

    private val _teamsSwapped = MutableStateFlow(false)
    val teamsSwapped: StateFlow<Boolean> = _teamsSwapped.asStateFlow()
    fun toggleTeamsSwapped() { _teamsSwapped.value = !_teamsSwapped.value }

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
        if (!isSupporter) setTeamColorTheme(TeamColorTheme.DEFAULT)
    }

    fun setTeamColorTheme(theme: TeamColorTheme) {
        _teamColorTheme.value = theme
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit()
            .putString("team_color", theme.name).apply()
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
        _teamColorTheme.value = try {
            TeamColorTheme.valueOf(prefs.getString("team_color", "DEFAULT")!!)
        } catch (e: Exception) {
            TeamColorTheme.DEFAULT
        }
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
        list.forEach { player ->
            val isCurrentlyPresent = _presentPlayerIds.value.contains(player.id)
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
        clearAllActivityLogs()
        val cA = _teamA.value
        val cB = _teamB.value
        val sA = _scoreA.value
        val sB = _scoreB.value
        if (cA.isEmpty() || cB.isEmpty()) return

        if (_streakOwner.value == winner) _currentStreak.value++ else {
            _streakOwner.value = winner; _currentStreak.value = 1
        }
        val (winners, losers) = if (winner == "A") cA to cB else cB to cA
        _lastWinners.value = winners; lastLosers = losers; _hasPreviousMatch.value = true

        viewModelScope.launch(Dispatchers.IO) {
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
                    repository.insertEloLog(
                        PlayerEloLog(
                            playerId = u.id,
                            playerNameSnapshot = normalizePersonName(u.name)
                                .ifBlank { "Desconhecido" },
                            date = dateLog,
                            elo = newElo,
                            groupName = u.groupName,
                            won = won
                        )
                    )
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
            _teamA.value = emptyList(); _teamB.value = emptyList()
            resetScoresAndPointIndicator()
            _currentMatchStartTimestamp.value = null
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
                val guaranteedOrdered = shuffledPool
                    .filter { guaranteedIdsSet.contains(it.id) }
                    .sortedBy { getEffectiveGames(it) }
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
                val guaranteedOrdered = remainingPool
                    .shuffled()
                    .filter { guaranteedIdsSet.contains(it.id) }
                    .sortedBy { getEffectiveGames(it) }
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
                    val guaranteedOrdered = remainingPool
                        .shuffled()
                        .filter { guaranteedIdsSet.contains(it.id) }
                        .sortedBy { getEffectiveGames(it) }
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
            try {
                val contentResolver = context.contentResolver
                if (type == CsvType.BACKUP_COMPLETO) {
                    val json =
                        BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).use { it.readText() }
                    val backup = Gson().fromJson(json, BackupData::class.java)

                    if (backup != null) {
                        val safePlayers = backup.players.map { p ->
                            p.copy(
                                id = if (p.id > 0) p.id else 0,
                                name = normalizePersonName(p.name).ifBlank { "Desconhecido" },
                                groupName = p.groupName.take(50)
                            )
                        }

                        val safeHistory = backup.history.map { h ->
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
                                groupName = h.groupName.take(50)
                            )
                        }

                        val safeLogs = backup.logs.map { l ->
                            l.copy(
                                id = 0,
                                playerNameSnapshot = normalizePersonName(l.playerNameSnapshot)
                                    .ifBlank { "Desconhecido" },
                                date = l.date.take(20),
                                groupName = l.groupName.take(50)
                            )
                        }

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
                                duplicatePlayerGroups = duplicatePlayerGroups
                            )
                        } else {
                            performImportWithDedup(safePlayers, safeHistory, safeLogs, emptySet())
                        }
                    } else {
                        Log.e("Import", context.getString(R.string.invalid_backup_format))
                    }
                } else {
                    val lines =
                        BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).readLines()
                    if (lines.isEmpty()) return@launch
                    val dataLines = lines.drop(1)

                    when (type) {
                        CsvType.JOGADORES -> {
                            val list = dataLines.mapNotNull { line ->
                                try {
                                    val cols = smartSplit(line)
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
                                            groupName = cols[5].takeIf { it.isNotBlank() }?.take(50)
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
                                }
                            }
                        }

                        CsvType.HISTORICO -> {
                            val list = dataLines.mapNotNull { line ->
                                try {
                                    val cols = smartSplit(line)
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
                                            groupName = cols[5].takeIf { it.isNotBlank() }?.take(50)
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
                                }
                            }
                        }

                        CsvType.ELO_LOGS -> {
                            val list = dataLines.mapNotNull { line ->
                                try {
                                    val cols = smartSplit(line)
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
                                            groupName = cols[5].takeIf { it.isNotBlank() }?.take(50)
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
                                }
                            }
                        }

                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("Import", "Erro: ${e.message}")
            }
        }
    }

    fun confirmMergeImport(renameDuplicates: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = _pendingMergeImport.value ?: return@launch
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
            _pendingMergeImport.value = null
        }
    }

    fun cancelMergeImport() {
        _pendingMergeImport.value = null
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
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "")
            val finalName =
                if (safeFileName.endsWith(if (type == CsvType.BACKUP_COMPLETO) ".json" else ".csv")) safeFileName else "$safeFileName.${if (type == CsvType.BACKUP_COMPLETO) "json" else "csv"}"
            val content = StringBuilder()

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
                shareFile(context, finalName, json, "application/json")
            } else {
                when (type) {
                    CsvType.JOGADORES -> {
                        content.append("ID,Nome,Elo,Partidas,Vitorias,Grupo,Prioridade,PedagioDiario,DataPedagio,PosicaoPreferida,PosicaoSecundaria\n")
                        currentGroupPlayers.value.forEach {
                            content.append(
                                "${it.id},\"${
                                    it.name.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",${formatElo(it.elo)},${it.matchesPlayed},${it.victories},\"${
                                    it.groupName.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",\"${it.isPriority}\",${it.dailyToll},\"${it.tollDate}\",\"${it.preferredPosition.orEmpty()}\",\"${it.secondaryPosition.orEmpty()}\"\n"
                            )
                        }
                    }

                    CsvType.HISTORICO -> {
                        content.append("Data,TimeA,TimeB,Vencedor,EloGanho,Grupo,MediaEloTimeA,MediaEloTimeB,PlacarTimeA,PlacarTimeB,InicioPartida,FimPartida\n")
                        currentGroupHistory.value.forEach {
                            content.append(
                                "\"${it.date}\",\"${
                                    it.teamA.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",\"${
                                    it.teamB.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",\"${it.winner}\",${formatElo(it.eloPoints)},\"${
                                    it.groupName.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",${it.teamAAverageElo?.let { e -> formatElo(e) } ?: ""},${
                                    it.teamBAverageElo?.let { e ->
                                        formatElo(
                                            e
                                        )
                                    } ?: ""
                                },${it.teamAScore ?: ""},${it.teamBScore ?: ""},${it.startTimestamp ?: ""},${it.endTimestamp ?: ""}\n")
                        }
                    }

                    CsvType.ELO_LOGS -> {
                        content.append("ID,PlayerID,Nome,Data,Elo,Grupo,Vitoria\n")
                        currentGroupEloLogs.value.forEach {
                            content.append(
                                "${it.id},${it.playerId},\"${
                                    it.playerNameSnapshot.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",\"${it.date}\",${formatElo(it.elo)},\"${
                                    it.groupName.replace(
                                        "\"",
                                        "\"\""
                                    )
                                }\",${it.won ?: ""}\n"
                            )
                        }
                    }

                    else -> {}
                }
                shareFile(context, finalName, content.toString(), "text/csv")
            }
        }
    }

    private fun shareFile(context: Context, name: String, content: String, mimeType: String) {
        try {
            val file = File(context.cacheDir, name)
            FileOutputStream(file).use { it.write(content.toByteArray()) }
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
        }
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
                            usesPositions = usesPositions
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
        resetGameState()
        clearSavedGameState()
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

package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.res.stringResource
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
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import com.bismarck.voleimanager.app.util.EloCalculator
import com.bismarck.voleimanager.app.util.TeamBalancer
import com.bismarck.voleimanager.app.util.TollCalculator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bismarck.voleimanager.app.data.model.BalancingMode

const val DEFAULT_GROUP_NAME = "Geral"

enum class Screen { GAME, HISTORY, FAQ, ABOUT }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CsvType { JOGADORES, HISTORICO, ELO_LOGS, BACKUP_COMPLETO }
enum class TeamColorTheme { DEFAULT, RED_GREEN, PURPLE_ORANGE }

data class BackupData(
    val version: Int = 1,
    val date: String,
    val players: List<Player>,
    val history: List<MatchHistory>,
    val logs: List<PlayerEloLog>
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
    val restingPlayers: Map<Int, Int> = emptyMap()
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

internal data class RestingMarkResult(
    val restingPlayers: Map<Int, Int>,
    val waitingList: List<Player>
)

internal data class ReturningPlayersResolution(
    val returningIds: Set<Int>,
    val restingPlayers: Map<Int, Int>,
    val waitingList: List<Player>
)

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

class VoleiViewModel(application: Application, private val repository: VoleiRepository) :
    AndroidViewModel(application) {

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }
    private val _currentScreen = MutableStateFlow(Screen.GAME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    private val _currentGroupConfig = MutableStateFlow(GroupConfig(DEFAULT_GROUP_NAME))
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

    val players = repository.allPlayers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _allHistory = repository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _allEloLogs = repository.eloLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val currentGroupPlayers = combine(players, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupHistory = combine(_allHistory, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupEloLogs = combine(_allEloLogs, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val targetDate = combine(currentGroupEloLogs, availableHistoryDates) { logs, dates ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hasToday = logs.any { it.date == today }
        if (hasToday) today else logs.map { it.date }.maxOrNull() ?: today
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )

    val gamesPlayedTodayMap = combine(currentGroupEloLogs, targetDate) { logs, tDate ->
        logs.filter { it.date == tDate }.groupingBy { it.playerId }.eachCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            .mapNotNull { id -> currentGroupPlayers.value.find { it.id == id } }
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
        // O time retornante precisa ter pelo menos teamSize jogadores para voltar como time completo.
        if (returningPlayers.size < teamSize) {
            // Se ainda não fecha um time completo, recoloca os jogadores como descansando.
            markPlayersResting(returningPlayers, rounds = 0) // Reinsere sem avançar rodada (mantém visibilidade)
            return false
        }

        val returningTeamPlayers = returningPlayers.take(teamSize).map { applyTollIfNecessary(it) }

        // O time adversário é o time reinante da rodada em que eles descansaram,
        // guardado em _lastWinners e filtrado apenas pelos jogadores ainda presentes.
        val reigningWinnerPlayers = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val opposingTeamPlayers = mutableListOf<Player>()
        opposingTeamPlayers.addAll(reigningWinnerPlayers.take(teamSize))

        // Se o time reinante não completar a quadra, complementa com a waitingList.
        if (opposingTeamPlayers.size < teamSize) {
            val needed = teamSize - opposingTeamPlayers.size
            val picks = _waitingList.value.take(needed)
            opposingTeamPlayers.addAll(picks)
            _waitingList.value = _waitingList.value.drop(needed)
        }

        // Se ainda faltarem jogadores, devolve o time retornante para a fila de espera.
        if (opposingTeamPlayers.size < teamSize) {
            // Mantém o time retornante descansando por mais uma rodada.
            markPlayersResting(returningTeamPlayers, rounds = 1)
            return false
        }

        // Preserva os perdedores da rodada anterior na fila antes de agendar a partida imediata.
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
        _hasPreviousMatch.value = false
        _scoreA.value = 0; _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()

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

    private val _scoreA = MutableStateFlow(0)
    val scoreA = _scoreA.asStateFlow()
    private val _scoreB = MutableStateFlow(0)
    val scoreB = _scoreB.asStateFlow()

    private val _hasPreviousMatch = MutableStateFlow(false)
    val hasPreviousMatch = _hasPreviousMatch.asStateFlow()
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()
    private val _streakOwner = MutableStateFlow<String?>(null)
    val streakOwner = _streakOwner.asStateFlow()
    private val _lastWinners = MutableStateFlow<List<Player>>(emptyList())
    val lastWinners = _lastWinners.asStateFlow()
    private val _manualStreakAdjustments = MutableStateFlow<List<ManualStreakAdjustmentLog>>(emptyList())
    val manualStreakAdjustments = _manualStreakAdjustments.asStateFlow()
    private var lastLosers: List<Player> = emptyList()
    private val _currentMatchStartTimestamp = MutableStateFlow<Long?>(null)

    // Controla quando a persistência do estado de jogo fica ativa (após a primeira carga do grupo)
    private var persistenceReady = false
    private val maxManualStreakLogsInMemory = 50

    init {
        loadPreferences()
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
        // Persiste também os jogadores em descanso e o contador de rodadas
        viewModelScope.launch {
            combine(_restingPlayers, _roundCounter) { _, _ -> }.collect { if (persistenceReady) saveGameState() }
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
            restingPlayers = _restingPlayers.value
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

    fun incrementScoreA() {
        _scoreA.value++
    }

    fun decrementScoreA() {
        if (_scoreA.value > 0) _scoreA.value--
    }

    fun incrementScoreB() {
        _scoreB.value++
    }

    fun decrementScoreB() {
        if (_scoreB.value > 0) _scoreB.value--
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
        val same = _currentGroupConfig.value.groupName == name
        viewModelScope.launch {
            _currentGroupConfig.value = repository.getGroupConfig(name)
                ?: GroupConfig(
                    groupName = name,
                    balancingMode = balancingMode ?: BalancingMode.REBALANCE.name
                ).also { repository.saveGroupConfig(it) }
            if (!same) {
                // Switching groups: reset current state, then try to restore saved state for new group
                resetGameState()
                tryRestoreGameState(name)
            } else if (!isGameInProgress()) {
                // Same group, no active game: try to restore (covers process-death scenario)
                tryRestoreGameState(name)
            }
            persistenceReady = true
        }
    }

    private fun resetGameState() {
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _presentPlayerIds.value = emptySet(); _currentStreak.value = 0; _streakOwner.value =
            null; _hasPreviousMatch.value = false
        _historyDateFilter.value = null
        _scoreA.value = 0; _scoreB.value = 0
        _currentMatchStartTimestamp.value = null
        _roundCounter.value = 0
        _restingPlayers.value = emptyMap()
        lastLosers = emptyList()
    }

    fun updateConfig(s: Int, l: Int, priorityP: Boolean, scoreEnabled: Boolean = true, balancingMode: String = _currentGroupConfig.value.balancingMode) {
        if (_currentGroupConfig.value.teamSize != s) {
            _currentStreak.value = 0
            _streakOwner.value = null
        }
        _currentGroupConfig.value = _currentGroupConfig.value.copy(
            teamSize = s,
            victoryLimit = l,
            priorityEnabled = priorityP,
            scoreEnabled = scoreEnabled,
            balancingMode = balancingMode
        )
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }

    fun renameGroup(old: String, new: String) = viewModelScope.launch {
        repository.renameGroup(
            old,
            new
        ); if (_currentGroupConfig.value.groupName == old) loadGroupConfig(new)
    }

    fun deleteGroup(name: String) = viewModelScope.launch {
        repository.deleteGroup(name); if (_currentGroupConfig.value.groupName == name) loadGroupConfig(
        DEFAULT_GROUP_NAME
    )
    }

    fun createGroup(name: String, balancingMode: String = BalancingMode.REBALANCE.name) = viewModelScope.launch(Dispatchers.IO) {
        val cfg = GroupConfig(groupName = name, balancingMode = balancingMode)
        repository.saveGroupConfig(cfg)
        // Garantir que o load use o cfg salvo (faz reset e tentativa de restauração)
        loadGroupConfig(name)
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

    fun addPlayer(n: String, e: Double, g: String, isPriority: Boolean) = viewModelScope.launch {
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
            tollDate = ""
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
        repository.deletePlayer(p); if (_presentPlayerIds.value.contains(p.id)) togglePlayerPresence(
        p
    )
    }

    fun editPlayer(p: Player, n: String, isPriority: Boolean) = viewModelScope.launch(Dispatchers.IO) {
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
        val up = p.copy(name = normalizedName, isPriority = isPriority)
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
        if (present) {
            val newIds = mutableSetOf<Int>()
            val currentWait = _waitingList.value.toMutableList()

            list.forEach { p ->
                newIds.add(p.id)
                val playing =
                    _teamA.value.any { it.id == p.id } || _teamB.value.any { it.id == p.id }
                if (!playing && !currentWait.any { it.id == p.id }) {
                    if (isGameInProgress()) {
                        val updatedP = applyTollIfNecessary(p)
                        val gamesPlayed = gamesPlayedTodayMap.value[updatedP.id] ?: 0
                        if (gamesPlayed > 0) {
                            currentWait.add(updatedP)
                        } else {
                            currentWait.add(0, updatedP)
                        }
                    } else {
                        currentWait.add(p)
                    }
                }
            }

            _presentPlayerIds.value = newIds
            if (isGameInProgress()) {
                _waitingList.value = currentWait
            }
        } else {
            _presentPlayerIds.value = emptySet(); _waitingList.value = emptyList()
        }
    }

    fun startNewAutomaticGame(all: List<Player>, size: Int) {
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

        if (config.priorityEnabled) {
            val priorities = pool.filter { it.isPriority }
            val prioritiesToSelect = priorities.take(2)
            selectedPlayers.addAll(prioritiesToSelect)
            pool.removeAll(prioritiesToSelect)
        }

        val remainingSlots = (size * 2) - selectedPlayers.size
        if (remainingSlots > 0) {
            val others = pool.take(remainingSlots)
            selectedPlayers.addAll(others)
            pool.removeAll(others)
        }

        val (finalA, finalB) = balanceTeamsWithPriority(selectedPlayers, size)
        _teamA.value = sortTeamPlayers(finalA); _teamB.value =
            sortTeamPlayers(finalB); _waitingList.value = pool
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
        _scoreA.value = 0; _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    private fun balanceTeamsWithPriority(
        players: List<Player>,
        teamSize: Int
    ): Pair<List<Player>, List<Player>> {
        val priorities = players.filter { it.isPriority }.sortedByDescending { it.elo }
        val nonPriorities = players.filter { !it.isPriority }.sortedByDescending { it.elo }
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

    fun startManualGame(tA: List<Player>, tB: List<Player>, rem: List<Player>) {
        val tAWithToll = tA.map { applyTollIfNecessary(it) }
        val tBWithToll = tB.map { applyTollIfNecessary(it) }
        val remWithToll = rem.map { applyTollIfNecessary(it) }

        _teamA.value = sortTeamPlayers(tAWithToll); _teamB.value =
            sortTeamPlayers(tBWithToll); _waitingList.value = remWithToll
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
        _scoreA.value = 0; _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    fun cancelGame() {
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _currentStreak.value = 0; _streakOwner.value = null; _hasPreviousMatch.value = false
        _scoreA.value = 0; _scoreB.value = 0
        _currentMatchStartTimestamp.value = null
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

        if (idxOutA != -1) {
            nA[idxOutA] = inWithToll
            if (_streakOwner.value == "A") resetStreak = true
            if (idxInWait != -1) wait[idxInWait] = out else if (idxInB != -1) nB[idxInB] = out
        } else if (idxOutB != -1) {
            nB[idxOutB] = inWithToll
            if (_streakOwner.value == "B") resetStreak = true
            if (idxInWait != -1) wait[idxInWait] = out else if (idxInA != -1) nA[idxInA] = out
        }

        if (resetStreak) {
            _currentStreak.value = 0
            _streakOwner.value = null
        }

        _teamA.value = sortTeamPlayers(nA); _teamB.value = sortTeamPlayers(nB); _waitingList.value =
            wait
    }

    fun finishGame(winner: String) {
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
            repository.insertMatch(
                MatchHistory(
                    date = dateDisplay,
                    teamA = cA.map { normalizePersonName(it.name) }
                        .filter { it.isNotBlank() }
                        .sortedBy { it.lowercase() }
                        .joinToString(", "),
                    teamB = cB.map { normalizePersonName(it.name) }
                        .filter { it.isNotBlank() }
                        .sortedBy { it.lowercase() }
                        .joinToString(", "),
                    teamAIds = cA.sortedBy { it.name.lowercase() }.joinToString(",") { it.id.toString() },
                    teamBIds = cB.sortedBy { it.name.lowercase() }.joinToString(",") { it.id.toString() },
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
            _scoreA.value = 0; _scoreB.value = 0
            _currentMatchStartTimestamp.value = null
        }
    }

    fun startNextRound() {
        val conf = _currentGroupConfig.value
        val mode = try { BalancingMode.valueOf(conf.balancingMode) } catch (e: Exception) { BalancingMode.REBALANCE }
        when (mode) {
            BalancingMode.REBALANCE -> startNextRoundRebalance(conf)
            BalancingMode.WINNER_RESTS -> startNextRoundWinnerRests(conf)
            BalancingMode.BOTH_REST -> startNextRoundBothRest(conf)
        }
    }


    private fun startNextRoundRebalance(conf: GroupConfig) {
        val conf = _currentGroupConfig.value
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

        val sortedLosers = TeamBalancer.groupAndInterleave(losers) { getEffectiveGames(it) }

        if (_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null

            val sortedWinners = TeamBalancer.groupAndInterleave(activeWinners) { getEffectiveGames(it) }
            val winnersToKeep = sortedWinners.take(conf.teamSize * 2)
            val winnersToDrop = sortedWinners.drop(conf.teamSize * 2)

            val fullPool = (winnersToDrop + waitlist + sortedLosers).toMutableList()

            val cA = mutableListOf<Player>()
            val cB = mutableListOf<Player>()

            if (conf.priorityEnabled) {
                val priorityWinners = winnersToKeep.filter { it.isPriority }.toMutableList()
                val nonPriorityWinners = winnersToKeep.filter { !it.isPriority }.toMutableList()

                if (priorityWinners.isNotEmpty()) {
                    cA.add(priorityWinners.removeAt(0))
                } else {
                    val p = fullPool.firstOrNull { it.isPriority }
                    if (p != null) {
                        cA.add(p); fullPool.remove(p)
                    }
                }

                if (priorityWinners.isNotEmpty()) {
                    cB.add(priorityWinners.removeAt(0))
                } else {
                    val p = fullPool.firstOrNull { it.isPriority }
                    if (p != null) {
                        cB.add(p); fullPool.remove(p)
                    }
                }

                nonPriorityWinners.addAll(priorityWinners)
                nonPriorityWinners.sortedByDescending { it.elo }.forEach { p ->
                    if (cA.size < conf.teamSize && cB.size < conf.teamSize) {
                        if (cA.sumOf { it.elo } <= cB.sumOf { it.elo }) cA.add(p) else cB.add(p)
                    } else if (cA.size < conf.teamSize) {
                        cA.add(p)
                    } else if (cB.size < conf.teamSize) {
                        cB.add(p)
                    }
                }
            } else {
                winnersToKeep.sortedByDescending { it.elo }.forEach { p ->
                    if (cA.size < conf.teamSize && cB.size < conf.teamSize) {
                        if (cA.sumOf { it.elo } <= cB.sumOf { it.elo }) cA.add(p) else cB.add(p)
                    } else if (cA.size < conf.teamSize) {
                        cA.add(p)
                    } else if (cB.size < conf.teamSize) {
                        cB.add(p)
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

        } else {
            var teamWin = activeWinners.toMutableList()
            var remainingPool = (waitlist + sortedLosers).toMutableList()

            if (teamWin.size > conf.teamSize) {
                val sorted = TeamBalancer.groupAndInterleave(teamWin.toList()) { getEffectiveGames(it) }
                teamWin = sorted.take(conf.teamSize).toMutableList()
                val droppedWinners = TeamBalancer.interleaveByElo(sorted.drop(conf.teamSize))
                remainingPool.addAll(0, droppedWinners)
            } else if (teamWin.size < conf.teamSize) {
                val needed = conf.teamSize - teamWin.size
                if (remainingPool.size >= needed) {
                    val picked = remainingPool.take(needed)
                    teamWin.addAll(picked)
                    remainingPool.removeAll(picked)
                } else {
                    teamWin.addAll(remainingPool)
                    remainingPool.clear()
                }
            }

            val teamChal = mutableListOf<Player>()

            if (conf.priorityEnabled) {
                val priorityPlayer = remainingPool.firstOrNull { it.isPriority }
                if (priorityPlayer != null) {
                    teamChal.add(priorityPlayer); remainingPool.remove(priorityPlayer)
                }
            }

            val slotsNeeded = conf.teamSize - teamChal.size
            if (slotsNeeded > 0) {
                val picked = remainingPool.take(slotsNeeded)
                teamChal.addAll(picked)
                remainingPool.removeAll(picked)
            }

            _waitingList.value = remainingPool
            if (_streakOwner.value == "B") {
                _teamB.value = sortTeamPlayers(teamWin); _teamA.value = sortTeamPlayers(teamChal)
            } else {
                _teamA.value = sortTeamPlayers(teamWin); _teamB.value =
                    sortTeamPlayers(teamChal); _streakOwner.value = "A"
            }
        }
        _hasPreviousMatch.value = false
        _scoreA.value = 0
        _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    /**
     * Descanso Simples:
     * - Quando o vencedor atinge victoryLimit, apenas o time vencedor sai para descansar.
     * - Mesmo que existam dois times completos na fila, não fazemos "descanso duplo"; apenas substituímos o vencedor pelo time completo do topo (se houver).
     * - Se não houver time completo na fila, o vencedor continua jogando.
     */
    private fun startNextRoundWinnerRests(conf: GroupConfig) {
        // Avança a rodada e tenta reativar quem terminou o descanso
        _roundCounter.value += 1
        // Se houver um time retornante e for possível montar a partida, agenda e encerra aqui
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

        val waitlist = _waitingList.value.filter { p -> !activeWinnerIds.contains(p.id) && !loserIds.contains(p.id) } + newPlayersWithToll

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        fun getEffectiveGames(p: Player): Int = TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)

        val sortedLosers = TeamBalancer.groupAndInterleave(losers) { getEffectiveGames(it) }

        // Se venceu e atingiu o limite, aplica a regra de descanso simples
        if (_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null

            // Seleciona os vencedores que permaneceram (em ordem de interleave)
            val sortedWinners = TeamBalancer.groupAndInterleave(activeWinners) { getEffectiveGames(it) }
            val winnersToKeep = sortedWinners.take(conf.teamSize * 2)
            val winnersToDrop = sortedWinners.drop(conf.teamSize * 2)

            // Quantidade de times completos na fila
            val fullTeamsInWait = (waitlist.size) / conf.teamSize

            if (fullTeamsInWait >= 1) {
                // Apenas 1 time completo do topo entra para substituir (descanso simples)
                val teamFromWait = waitlist.take(conf.teamSize)
                val newWait = waitlist.drop(conf.teamSize).toMutableList()
                // Os perdedores formam o time adversário (completa com newWait se necessário)
                val teamLosers = sortedLosers.take(conf.teamSize).toMutableList()
                if (teamLosers.size < conf.teamSize) {
                    val needed = conf.teamSize - teamLosers.size
                    val picked = newWait.take(needed)
                    teamLosers.addAll(picked)
                    newWait.removeAll(picked.toSet())
                }
                // Os vencedores vão descansar (winnersToDrop + winnersToKeep ficam fora desta partida)
                // Monta os times: teamFromWait vs teamLosers
                if (teamFromWait.size == conf.teamSize && teamLosers.size == conf.teamSize) {
                    _teamA.value = sortTeamPlayers(teamFromWait)
                    _teamB.value = sortTeamPlayers(teamLosers)
                    // Marca os vencedores como descansando e adiciona à espera (com asterisco)
                    val allResting = winnersToKeep + winnersToDrop
                    val returnRound = _roundCounter.value + 1
                    val restMap = _restingPlayers.value.toMutableMap()
                    allResting.forEach { restMap[it.id] = returnRound }
                    _restingPlayers.value = restMap
                    // A waitingList deve conter: jogadores descansando + newWait
                    val restingWithToll = allResting.map { applyTollIfNecessary(it) }.distinctBy { it.id }
                    val dedupWait = (restingWithToll + newWait).distinctBy { it.id }
                    _waitingList.value = dedupWait
                } else {
                    // Fallback: se não há jogadores suficientes, mantém a lógica clássica
                    startNextRoundRebalance(conf)
                }
            } else {
                // Menos que 1 time completo: o vencedor continua jogando
                val currentWinners = winnersToKeep
                if (currentWinners.size >= conf.teamSize * 2) {
                    val (a, b) = balanceTeamsWithPriority(currentWinners, conf.teamSize)
                    _teamA.value = sortTeamPlayers(a); _teamB.value = sortTeamPlayers(b)
                    _waitingList.value = waitlist + sortedLosers
                } else {
                    startNextRoundRebalance(conf)
                }
            }
        } else {
            // Ainda não atingiu o limite: usa o comportamento padrão (rebalanceamento) para a rotatividade
            startNextRoundRebalance(conf)
        }

        _hasPreviousMatch.value = false
        _scoreA.value = 0
        _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    /**
     * Descanso Duplo:
     * - Quando o vencedor atingir victoryLimit:
     *   - Se houver 2 (ou mais) times completos na fila: os dois times completos se enfrentam (e o perdedor da última partida vai para o fim da fila),
     *     e os vencedores da partida anterior ficam "descansando" (fora).
     *   - Se houver apenas 1 time completo: comporta-se como Descanso Simples.
     *   - Se não houver times completos suficientes: vencedor continua.
     */
     private fun startNextRoundBothRest(conf: GroupConfig) {
        // Avança a rodada e tenta reativar quem terminou o descanso
         _roundCounter.value += 1
        // Se houver um time retornante e for possível montar a partida, agenda e encerra aqui
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

        val waitlist = _waitingList.value.filter { p -> !activeWinnerIds.contains(p.id) && !loserIds.contains(p.id) } + newPlayersWithToll

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val usageMap = getUsageCountMap(today)
        fun getEffectiveGames(p: Player): Int = TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)

        val sortedLosers = TeamBalancer.groupAndInterleave(losers) { getEffectiveGames(it) }

        if (_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null

            val sortedWinners = TeamBalancer.groupAndInterleave(activeWinners) { getEffectiveGames(it) }
            val winnersToKeep = sortedWinners.take(conf.teamSize * 2)
            val winnersToDrop = sortedWinners.drop(conf.teamSize * 2)

            // Quantos times completos existem no waitlist?
            val fullTeamsInWait = waitlist.size / conf.teamSize

            if (fullTeamsInWait >= 2) {
                // As duas equipes completas do topo da fila se enfrentam
                val team1 = waitlist.take(conf.teamSize)
                val team2 = waitlist.drop(conf.teamSize).take(conf.teamSize)
                val newWait = waitlist.drop(conf.teamSize * 2).toMutableList()
                // O perdedor da última rodada (losers) vai para o final da fila:
                // (mantém a ordem: adiciona os perdedores ao final de newWait)
                newWait.addAll(sortedLosers)
                // Os vencedores anteriores entram em descanso (mantidos fora do jogo)
                // Monta a partida entre team1 x team2
                _teamA.value = sortTeamPlayers(team1)
                _teamB.value = sortTeamPlayers(team2)
                // Marca os vencedores (winnersToKeep + winnersToDrop) como descansando por 1 rodada
                val allResting = winnersToKeep + winnersToDrop
                val returnRound = _roundCounter.value + 1
                val restMap = _restingPlayers.value.toMutableMap()
                allResting.forEach { restMap[it.id] = returnRound }
                _restingPlayers.value = restMap
                // A waitingList deve conter: jogadores em descanso + newWait
                val restingWithToll = allResting.map { applyTollIfNecessary(it) }.distinctBy { it.id }
                val dedupWait = (restingWithToll + newWait).distinctBy { it.id }
                _waitingList.value = dedupWait
             } else if (fullTeamsInWait == 1) {
                  // Apenas 1 time completo na fila -> comporta-se como descanso simples
                 val teamFromWait = waitlist.take(conf.teamSize)
                 val remainingAfterTeam = waitlist.drop(conf.teamSize).toMutableList()
                      val teamLosers = sortedLosers.take(conf.teamSize).toMutableList()
                 if (teamLosers.size < conf.teamSize) {
                     val needed = conf.teamSize - teamLosers.size
                     val picked = remainingAfterTeam.take(needed)
                     teamLosers.addAll(picked)
                     remainingAfterTeam.removeAll(picked)
                 }
                 if (teamFromWait.size == conf.teamSize && teamLosers.size == conf.teamSize) {
                     _teamA.value = sortTeamPlayers(teamFromWait)
                     _teamB.value = sortTeamPlayers(teamLosers)
                     val allResting = winnersToKeep + winnersToDrop
                     val returnRound = _roundCounter.value + 1
                     val restMap = _restingPlayers.value.toMutableMap()
                     allResting.forEach { restMap[it.id] = returnRound }
                     _restingPlayers.value = restMap
                     val restingWithToll = allResting.map { applyTollIfNecessary(it) }.distinctBy { it.id }
                      val dedupWait = (restingWithToll + remainingAfterTeam).distinctBy { it.id }
                     _waitingList.value = dedupWait
                 } else {
                     startNextRoundRebalance(conf)
                     return
                 }
            } else {
                // Menos que 1 time completo na fila -> o vencedor continua
                val currentWinners = winnersToKeep
                if (currentWinners.size >= conf.teamSize * 2) {
                    val (a, b) = balanceTeamsWithPriority(currentWinners, conf.teamSize)
                    _teamA.value = sortTeamPlayers(a); _teamB.value = sortTeamPlayers(b)
                    _waitingList.value = waitlist + sortedLosers
                } else {
                    startNextRoundRebalance(conf)
                    return
                }
            }
        } else {
            // Ainda não atingiu o limite: usa o comportamento padrão (rebalanceamento)
            startNextRoundRebalance(conf)
            return
        }

        _hasPreviousMatch.value = false
        _scoreA.value = 0
        _scoreB.value = 0
        _currentMatchStartTimestamp.value = System.currentTimeMillis()
    }

    private fun formatElo(elo: Double): String = String.format(Locale.US, "%.2f", elo)

    private fun normalizePersonName(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ").take(50)
    }

    private fun canonicalPersonName(name: String): String {
        val normalized = normalizePersonName(name)
        val noAccents = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return noAccents.lowercase(Locale.ROOT)
    }

    private fun areSameCanonicalName(a: String, b: String): Boolean {
        return canonicalPersonName(a) == canonicalPersonName(b)
    }

    private fun normalizeTeamNamesSnapshot(raw: String): String {
        return raw
            .take(255)
            .split(",")
            .map { normalizePersonName(it) }
            .filter { it.isNotBlank() }
            .sortedBy { it.lowercase() }
            .joinToString(", ")
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
                                name = normalizePersonName(p.name).ifBlank { "Desconhecido" },
                                groupName = p.groupName.take(50)
                            )
                        }

                        val safeHistory = backup.history.map { h ->
                            h.copy(
                                date = h.date.take(20),
                                teamA = normalizeTeamNamesSnapshot(h.teamA),
                                teamB = normalizeTeamNamesSnapshot(h.teamB),
                                winner = h.winner.take(50),
                                groupName = h.groupName.take(50)
                            )
                        }

                        val safeLogs = backup.logs.map { l ->
                            l.copy(
                                playerNameSnapshot = normalizePersonName(l.playerNameSnapshot)
                                    .ifBlank { "Desconhecido" },
                                date = l.date.take(20),
                                groupName = l.groupName.take(50)
                            )
                        }

                        repository.insertPlayers(safePlayers)
                        repository.insertHistoryList(safeHistory)
                        safeLogs.forEach { repository.insertEloLog(it) }

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
                                            id = cols[0].toIntOrNull() ?: 0,
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
                                            tollDate = cols.getOrElse(8) { "" }.take(20)
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (list.isNotEmpty()) repository.insertPlayers(list)
                        }

                        CsvType.HISTORICO -> {
                            val list = dataLines.mapNotNull { line ->
                                try {
                                    val cols = smartSplit(line)
                                    if (cols.size >= 6) {
                                        MatchHistory(
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
                            if (list.isNotEmpty()) repository.insertHistoryList(list)
                        }

                        CsvType.ELO_LOGS -> {
                            val list = dataLines.mapNotNull { line ->
                                try {
                                    val cols = smartSplit(line)
                                    if (cols.size >= 6) {
                                        PlayerEloLog(
                                            id = cols[0].toIntOrNull() ?: 0,
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
                            list.forEach { repository.insertEloLog(it) }
                        }

                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("Import", "Erro: ${e.message}")
            }
        }
    }

    fun exportData(context: Context, type: CsvType, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "")
            val finalName =
                if (safeFileName.endsWith(if (type == CsvType.BACKUP_COMPLETO) ".json" else ".csv")) safeFileName else "$safeFileName.${if (type == CsvType.BACKUP_COMPLETO) "json" else "csv"}"
            val content = StringBuilder()

            if (type == CsvType.BACKUP_COMPLETO) {
                val backup = BackupData(
                    date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    players = currentGroupPlayers.value,
                    history = currentGroupHistory.value,
                    logs = currentGroupEloLogs.value
                )
                val json = Gson().toJson(backup)
                shareFile(context, finalName, json, "application/json")
            } else {
                when (type) {
                    CsvType.JOGADORES -> {
                        content.append("ID,Nome,Elo,Partidas,Vitorias,Grupo,Prioridade,PedagioDiario,DataPedagio\n")
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
                                }\",\"${it.isPriority}\",${it.dailyToll},\"${it.tollDate}\"\n"
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
                if (match.has("teamA")) {
                    match.addProperty("teamA", normalizeTeamNamesSnapshot(match.get("teamA").asString))
                }
                if (match.has("teamB")) {
                    match.addProperty("teamB", normalizeTeamNamesSnapshot(match.get("teamB").asString))
                }
                if (!match.has("teamAScore")) match.addProperty("teamAScore", 0)
                if (!match.has("teamBScore")) match.addProperty("teamBScore", 0)
                if (!match.has("teamAIds")) match.addProperty("teamAIds", "")
                if (!match.has("teamBIds")) match.addProperty("teamBIds", "")
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
        val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
            setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
            setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
            setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

            setContent {
                com.bismarck.voleimanager.app.ui.theme.AppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                    androidx.compose.material3.Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                        com.bismarck.voleimanager.app.ui.ExportableImageContent(matches, matchSortMode, players, playerSortMode, date, isDarkTheme, showElo, showScore, matchDurationsMinutes, averagePlayersEloText, averageMatchDurationText)
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

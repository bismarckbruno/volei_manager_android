package com.example.voleimanager.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voleimanager.data.VoleiRepository
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog
import com.example.voleimanager.util.EloCalculator
import com.example.voleimanager.util.TeamBalancer
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

enum class Screen { GAME, RANKING, HISTORY, CHARTS }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CsvType { JOGADORES, HISTORICO, ELO_LOGS, BACKUP_COMPLETO }

data class BackupData(
    val version: Int = 1,
    val date: String,
    val players: List<Player>,
    val history: List<MatchHistory>,
    val logs: List<PlayerEloLog>
)

class VoleiViewModel(application: Application, private val repository: VoleiRepository) : AndroidViewModel(application) {

    // --- NAVEGAÇÃO ---
    private val _currentScreen = MutableStateFlow(Screen.GAME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    // --- CONFIGURAÇÃO DO GRUPO ATUAL ---
    private val _currentGroupConfig = MutableStateFlow(GroupConfig("Geral"))
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

    // --- DADOS BRUTOS ---
    val players = repository.allPlayers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _allHistory = repository.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _allEloLogs = repository.eloLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DADOS FILTRADOS ---
    val currentGroupPlayers = combine(players, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupHistory = combine(_allHistory, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupEloLogs = combine(_allEloLogs, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FILTROS DE DATA (HISTÓRICO E RANKING) ---
    private val _rankingDateFilter = MutableStateFlow<String?>(null)
    val rankingDateFilter = _rankingDateFilter.asStateFlow()

    private val _historyDateFilter = MutableStateFlow<String?>(null)
    val historyDateFilter = _historyDateFilter.asStateFlow()

    val availableRankingDates = currentGroupEloLogs.map { list ->
        list.map { it.date }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableHistoryDates = currentGroupHistory.map { list ->
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        list.map { it.date.split(" ")[0] }.distinct().sortedWith { d1, d2 ->
            try { sdf.parse(d1)?.compareTo(sdf.parse(d2)) ?: 0 } catch (e: Exception) { 0 }
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // --- MAPA DE PARTIDAS (DATA MAIS RECENTE) ---
    val gamesPlayedTodayMap = combine(currentGroupEloLogs, availableHistoryDates) { logs, dates ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hasToday = logs.any { it.date == today }
        val targetDate = if (hasToday) today else logs.map { it.date }.maxOrNull()
        if (targetDate != null) logs.filter { it.date == targetDate }.groupingBy { it.playerId }.eachCount() else emptyMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- LISTA ORDENADA PARA TELA INICIAL ---
    val sortedPlayersForPresence = combine(currentGroupPlayers, gamesPlayedTodayMap) { pList, gamesMap ->
        pList.sortedWith(
            compareByDescending<Player> { gamesMap[it.id] ?: 0 }
                .thenByDescending { it.elo }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FILTROS DO GRÁFICO ---
    private val _chartSelectedPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val chartSelectedPlayerIds = _chartSelectedPlayerIds.asStateFlow()
    
    private val _chartDateRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)
    val chartDateRange = _chartDateRange.asStateFlow()

    fun setChartDateRange(start: Long?, end: Long?) { _chartDateRange.value = start to end }

    fun toggleChartPlayer(id: Int) {
        val s = _chartSelectedPlayerIds.value.toMutableSet()
        if(s.contains(id)) s.remove(id) else s.add(id)
        _chartSelectedPlayerIds.value = s
    }

    // --- TEMA ---
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // --- ESTADOS DO JOGO ---
    private val _teamA = MutableStateFlow<List<Player>>(emptyList()); val teamA = _teamA.asStateFlow()
    private val _teamB = MutableStateFlow<List<Player>>(emptyList()); val teamB = _teamB.asStateFlow()
    private val _waitingList = MutableStateFlow<List<Player>>(emptyList()); val waitingList = _waitingList.asStateFlow()
    private val _presentPlayerIds = MutableStateFlow<Set<Int>>(emptySet()); val presentPlayerIds = _presentPlayerIds.asStateFlow()

    private val _hasPreviousMatch = MutableStateFlow(false); val hasPreviousMatch = _hasPreviousMatch.asStateFlow()
    private val _currentStreak = MutableStateFlow(0); val currentStreak = _currentStreak.asStateFlow()
    private val _streakOwner = MutableStateFlow<String?>(null); val streakOwner = _streakOwner.asStateFlow()
    private val _lastWinners = MutableStateFlow<List<Player>>(emptyList()); val lastWinners = _lastWinners.asStateFlow()
    private var lastLosers: List<Player> = emptyList()

    init { 
        loadThemePreference() 
        viewModelScope.launch {
            availableHistoryDates.collect { dates -> if (_historyDateFilter.value == null && dates.isNotEmpty()) _historyDateFilter.value = dates.first() }
        }
    }

    // --- SETTERS E CONFIGS ---
    fun setRankingDateFilter(d: String?) { _rankingDateFilter.value = d }
    fun setHistoryDateFilter(d: String?) { _historyDateFilter.value = d }
    fun setThemeMode(m: ThemeMode) { _themeMode.value = m; getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit().putString("theme", m.name).apply() }
    private fun loadThemePreference() { _themeMode.value = try { ThemeMode.valueOf(getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).getString("theme", "SYSTEM")!!) } catch (e: Exception) { ThemeMode.SYSTEM } }

    fun getRankingListForDate(date: String?): List<Player> {
        if (date == null) return currentGroupPlayers.value.sortedByDescending { it.elo }
        val logs = currentGroupEloLogs.value.filter { it.date == date }
        return logs.map { Player(id = it.playerId, name = it.playerNameSnapshot, elo = it.elo, groupName = it.groupName) }.sortedByDescending { it.elo }
    }

    fun isGameInProgress(): Boolean = _teamA.value.isNotEmpty() || _teamB.value.isNotEmpty() || _hasPreviousMatch.value

    fun loadGroupConfig(name: String) {
        val same = _currentGroupConfig.value.groupName == name
        viewModelScope.launch {
            _currentGroupConfig.value = repository.getGroupConfig(name) ?: GroupConfig(name).also { repository.saveGroupConfig(it) }
            if(!same) resetGameState()
        }
    }

    private fun resetGameState() {
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _presentPlayerIds.value = emptySet(); _currentStreak.value = 0; _streakOwner.value = null; _hasPreviousMatch.value = false
        _rankingDateFilter.value = null; _historyDateFilter.value = null; _chartSelectedPlayerIds.value = emptySet()
    }

    fun updateConfig(s: Int, l: Int, genderP: Boolean) {
        _currentGroupConfig.value = _currentGroupConfig.value.copy(teamSize = s, victoryLimit = l, genderPriorityEnabled = genderP)
        viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) }
    }
    
    fun renameGroup(old: String, new: String) = viewModelScope.launch { repository.renameGroup(old, new); if(_currentGroupConfig.value.groupName == old) loadGroupConfig(new) }
    fun deleteGroup(name: String) = viewModelScope.launch { repository.deleteGroup(name); if(_currentGroupConfig.value.groupName == name) loadGroupConfig("Geral") }

    // --- CRUD JOGADORES ---
    fun addPlayer(n: String, e: Double, g: String, sex: String) = viewModelScope.launch { repository.insertPlayer(Player(name = n, elo = e, groupName = g, sex = sex)) }
    fun deletePlayer(p: Player) = viewModelScope.launch { repository.deletePlayer(p); if(_presentPlayerIds.value.contains(p.id)) togglePlayerPresence(p) }
    fun renamePlayer(p: Player, n: String) = viewModelScope.launch {
        val up = p.copy(name = n); repository.updatePlayer(up)
        _teamA.value = _teamA.value.map { if(it.id == p.id) up else it }
        _teamB.value = _teamB.value.map { if(it.id == p.id) up else it }
        _waitingList.value = _waitingList.value.map { if(it.id == p.id) up else it }
    }

    fun togglePlayerPresence(p: Player) {
        val ids = _presentPlayerIds.value.toMutableSet()
        if(ids.contains(p.id)) {
            ids.remove(p.id)
            _waitingList.value = _waitingList.value.filter { it.id != p.id }
        } else {
            ids.add(p.id)
            val isWinnerWaiting = _hasPreviousMatch.value && _lastWinners.value.any { it.id == p.id }
            if(!_teamA.value.any{it.id==p.id} && !_teamB.value.any{it.id==p.id} && !_waitingList.value.any{it.id==p.id} && !isWinnerWaiting) {
                _waitingList.value = _waitingList.value + p
            }
        }
        _presentPlayerIds.value = ids
    }

    fun setAllPlayersPresence(list: List<Player>, present: Boolean) {
        if(present) {
            val currentWait = _waitingList.value.toMutableList()
            list.forEach { p ->
                val playing = _teamA.value.any { it.id == p.id } || _teamB.value.any { it.id == p.id }
                if(!playing && !currentWait.any { it.id == p.id }) currentWait.add(p)
            }
            _presentPlayerIds.value = list.map { it.id }.toSet()
            _waitingList.value = currentWait
        } else {
            _presentPlayerIds.value = emptySet(); _waitingList.value = emptyList()
        }
    }

    // --- LÓGICA DO JOGO ---
    fun startNewAutomaticGame(all: List<Player>, size: Int) {
        val available = all.filter { _presentPlayerIds.value.contains(it.id) }
        if(available.size < size * 2) return

        val pool = available.toMutableList()
        val selectedPlayers = mutableListOf<Player>()

        if (_currentGroupConfig.value.genderPriorityEnabled) {
            val women = pool.filter { it.sex == "F" }
            val womenToSelect = women.take(2)
            selectedPlayers.addAll(womenToSelect)
            pool.removeAll(womenToSelect)
        }
        
        pool.shuffle()
        val remainingSlots = (size * 2) - selectedPlayers.size
        if (remainingSlots > 0) {
            val others = pool.take(remainingSlots)
            selectedPlayers.addAll(others)
            pool.removeAll(others)
        }

        val (finalA, finalB) = balanceTeamsWithGender(selectedPlayers, size)
        _teamA.value = finalA; _teamB.value = finalB; _waitingList.value = pool
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
    }

    private fun balanceTeamsWithGender(players: List<Player>, teamSize: Int): Pair<List<Player>, List<Player>> {
        val women = players.filter { it.sex == "F" }.sortedByDescending { it.elo }
        val men = players.filter { it.sex != "F" }.sortedByDescending { it.elo }
        val tA = mutableListOf<Player>(); val tB = mutableListOf<Player>()

        women.forEachIndexed { i, p -> if (tA.size < teamSize && tB.size < teamSize) { if (i % 2 == 0) tA.add(p) else tB.add(p) } else if (tA.size < teamSize) tA.add(p) else tB.add(p) }
        men.forEach { p -> if (tA.size < teamSize && tB.size < teamSize) { if (tA.sumOf{it.elo} <= tB.sumOf{it.elo}) tA.add(p) else tB.add(p) } else if (tA.size < teamSize) tA.add(p) else tB.add(p) }
        return tA to tB
    }

    fun startManualGame(tA: List<Player>, tB: List<Player>, rem: List<Player>) {
        _teamA.value = tA; _teamB.value = tB; _waitingList.value = rem
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
    }

    fun cancelGame() {
        _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
        _currentStreak.value = 0; _streakOwner.value = null; _hasPreviousMatch.value = false
    }

    fun substitutePlayer(out: Player, `in`: Player) {
        val wait = _waitingList.value.toMutableList()
        val nA = _teamA.value.toMutableList()
        val nB = _teamB.value.toMutableList()
        val idxOutA = nA.indexOfFirst { it.id == out.id }; val idxOutB = nB.indexOfFirst { it.id == out.id }
        val idxInA = nA.indexOfFirst { it.id == `in`.id }; val idxInB = nB.indexOfFirst { it.id == `in`.id }; val idxInWait = wait.indexOfFirst { it.id == `in`.id }

        if (idxOutA != -1) {
            nA[idxOutA] = `in`
            if (idxInWait != -1) wait[idxInWait] = out else if (idxInB != -1) nB[idxInB] = out
        } else if (idxOutB != -1) {
            nB[idxOutB] = `in`
            if (idxInWait != -1) wait[idxInWait] = out else if (idxInA != -1) nA[idxInA] = out
        }
        _teamA.value = nA; _teamB.value = nB; _waitingList.value = wait; _currentStreak.value = 0
    }

    fun finishGame(winner: String) {
        val cA = _teamA.value; val cB = _teamB.value
        if(cA.isEmpty() || cB.isEmpty()) return

        if(_streakOwner.value == winner) _currentStreak.value++ else { _streakOwner.value = winner; _currentStreak.value = 1 }
        val (winners, losers) = if(winner == "A") cA to cB else cB to cA
        _lastWinners.value = winners; lastLosers = losers; _hasPreviousMatch.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val delta = if (winner == "A") EloCalculator.calculateEloChange(cA.map { it.elo }.average(), cB.map { it.elo }.average()) else EloCalculator.calculateEloChange(cB.map { it.elo }.average(), cA.map { it.elo }.average())
            val dateLog = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val dateDisplay = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val updatedPlayers = mutableListOf<Player>()
            val newWinners = mutableListOf<Player>(); val newLosers = mutableListOf<Player>()

            suspend fun process(list: List<Player>, won: Boolean) {
                list.forEach { p ->
                    val newElo = if(won) p.elo + delta else p.elo - delta
                    val u = p.copy(elo = newElo, matchesPlayed = p.matchesPlayed + 1, victories = if(won) p.victories + 1 else p.victories)
                    updatedPlayers.add(u); if(won) newWinners.add(u) else newLosers.add(u)
                    repository.insertEloLog(PlayerEloLog(playerId = u.id, playerNameSnapshot = u.name, date = dateLog, elo = newElo, groupName = u.groupName))
                }
            }
            process(if(winner == "A") cA else cB, true); process(if(winner == "A") cB else cA, false)

            _lastWinners.value = newWinners; lastLosers = newLosers
            repository.updatePlayers(updatedPlayers)
            repository.insertMatch(MatchHistory(date = dateDisplay, teamA = cA.joinToString(", "){it.name}, teamB = cB.joinToString(", "){it.name}, winner = "Time $winner", eloPoints = delta, groupName = cA.first().groupName))
            _teamA.value = emptyList(); _teamB.value = emptyList()
        }
    }

    fun startNextRound() {
        val conf = _currentGroupConfig.value
        val activeWinners = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val losers = lastLosers.filter { _presentPlayerIds.value.contains(it.id) }
        val waitlist = _waitingList.value.filter { p -> activeWinners.none { it.id == p.id } }
        var fullPool = waitlist + losers

        if(_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null
            val sortedWinners = activeWinners.sortedByDescending { it.elo }
            val winnersToKeep = sortedWinners.take((conf.teamSize * 2).coerceAtMost(sortedWinners.size))
            val winnersToDrop = sortedWinners.drop(winnersToKeep.size)
            fullPool = winnersToDrop + fullPool
            
            val cA = mutableListOf<Player>(); val cB = mutableListOf<Player>()
            winnersToKeep.forEachIndexed { i, p -> if (i % 2 == 0) cA.add(p) else cB.add(p) }
            completeTeams(cA, cB, fullPool, conf.teamSize)
        } else {
            var teamWin = activeWinners; var remainingPool = fullPool
            if(teamWin.size > conf.teamSize) { teamWin = teamWin.take(conf.teamSize); remainingPool = activeWinners.drop(conf.teamSize) + remainingPool }
            else if(teamWin.size < conf.teamSize) { val needed = conf.teamSize - teamWin.size; if(remainingPool.size >= needed) { teamWin = teamWin + remainingPool.take(needed); remainingPool = remainingPool.drop(needed) } }

            val teamChal = mutableListOf<Player>()
            val currentWait = waitlist.toMutableList(); val currentLosers = losers.toMutableList()
            val usedIds = teamWin.map { it.id }.toSet()
            currentWait.removeAll { usedIds.contains(it.id) }; currentLosers.removeAll { usedIds.contains(it.id) }

            if (conf.genderPriorityEnabled) {
                val woman = currentWait.firstOrNull { it.sex == "F" } ?: currentLosers.filter { it.sex == "F" }.maxByOrNull { it.elo }
                if (woman != null) { teamChal.add(woman); if (currentWait.remove(woman)) {} else currentLosers.remove(woman) }
            }
            
            val slotsNeeded = conf.teamSize - teamChal.size
            if (slotsNeeded > 0) {
                val fromWait = currentWait.take(slotsNeeded)
                teamChal.addAll(fromWait); currentWait.removeAll(fromWait)
                val neededLosers = slotsNeeded - fromWait.size
                if (neededLosers > 0 && currentLosers.isNotEmpty()) {
                     val sortedLosers = currentLosers.sortedBy { it.matchesPlayed } 
                     val picked = sortedLosers.take(neededLosers)
                     teamChal.addAll(picked); currentLosers.removeAll(picked)
                }
            }

            if (teamChal.size == conf.teamSize) {
                _waitingList.value = currentWait + currentLosers.shuffled()
                if(_streakOwner.value == "B") { _teamB.value = teamWin; _teamA.value = teamChal }
                else { _teamA.value = teamWin; _teamB.value = teamChal; _streakOwner.value = "A" }
            }
        }
        _hasPreviousMatch.value = false
    }

    private fun completeTeams(cA: MutableList<Player>, cB: MutableList<Player>, pool: List<Player>, size: Int) {
        val conf = _currentGroupConfig.value
        val av = pool.toMutableList()
        if (conf.genderPriorityEnabled) {
             if (cA.size < size && cA.none { it.sex == "F" }) { val w = av.firstOrNull { it.sex == "F" }; if (w != null) { cA.add(w); av.remove(w) } }
             if (cB.size < size && cB.none { it.sex == "F" }) { val w = av.firstOrNull { it.sex == "F" }; if (w != null) { cB.add(w); av.remove(w) } }
        }
        while ((cA.size < size || cB.size < size) && av.isNotEmpty()) {
            val p = av.removeAt(0)
            if (cA.size < size && cB.size < size) { if (cA.sumOf{it.elo} <= cB.sumOf{it.elo}) cA.add(p) else cB.add(p) }
            else if (cA.size < size) cA.add(p) else cB.add(p)
        }
        _teamA.value = cA; _teamB.value = cB; _waitingList.value = av
    }

    // --- CSV & BACKUP ---
    private fun formatElo(elo: Double): String = String.format(Locale.US, "%.2f", elo)

    fun importData(uri: Uri, type: CsvType, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                if (type == CsvType.BACKUP_COMPLETO) {
                    val json = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).use { it.readText() }
                    val backup = Gson().fromJson(json, BackupData::class.java)
                    repository.insertPlayers(backup.players)
                    repository.insertHistoryList(backup.history)
                    backup.logs.forEach { repository.insertEloLog(it) }
                } else {
                    val lines = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri))).readLines().drop(1)
                    // CORREÇÃO: Cast seguro com mapNotNull e when exhaustivo
                    when (type) {
                        CsvType.JOGADORES -> {
                             val list = lines.mapNotNull { line ->
                                 val cols = smartSplit(line)
                                 if(cols.size >= 6) Player(id=cols[0].toIntOrNull()?:0, name=cols[1], elo=cols[2].toDoubleOrNull()?:1200.0, matchesPlayed=cols[3].toIntOrNull()?:0, victories=cols[4].toIntOrNull()?:0, groupName=cols[5], sex=cols.getOrElse(6){"M"}) else null
                             }
                             repository.insertPlayers(list)
                        }
                        CsvType.HISTORICO -> {
                             val list = lines.mapNotNull { line ->
                                 val cols = smartSplit(line)
                                 if(cols.size >= 6) MatchHistory(date=cols[0], teamA=cols[1], teamB=cols[2], winner=cols[3], eloPoints=cols[4].toDoubleOrNull()?:0.0, groupName=cols[5]) else null
                             }
                             repository.insertHistoryList(list)
                        }
                        CsvType.ELO_LOGS -> {
                             val list = lines.mapNotNull { line ->
                                 val cols = smartSplit(line)
                                 if(cols.size >= 6) PlayerEloLog(id=cols[0].toIntOrNull()?:0, playerId=cols[1].toIntOrNull()?:0, playerNameSnapshot=cols[2], date=cols[3], elo=cols[4].toDoubleOrNull()?:1200.0, groupName=cols[5]) else null
                             }
                             list.forEach { repository.insertEloLog(it) }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) { Log.e("Import", "Erro: ${e.message}") }
        }
    }

    fun exportData(context: Context, type: CsvType, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalName = if (fileName.endsWith(if(type==CsvType.BACKUP_COMPLETO) ".json" else ".csv")) fileName else "$fileName.${if(type==CsvType.BACKUP_COMPLETO) "json" else "csv"}"
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
                when(type) {
                    CsvType.JOGADORES -> {
                        content.append("ID,Nome,Elo,Partidas,Vitorias,Grupo,Sexo\n")
                        currentGroupPlayers.value.forEach { content.append("${it.id},\"${it.name}\",${formatElo(it.elo)},${it.matchesPlayed},${it.victories},\"${it.groupName}\",\"${it.sex}\"\n") }
                    }
                    CsvType.HISTORICO -> {
                        content.append("Data,TimeA,TimeB,Vencedor,EloGanho,Grupo\n")
                        currentGroupHistory.value.forEach { content.append("\"${it.date}\",\"${it.teamA}\",\"${it.teamB}\",\"${it.winner}\",${formatElo(it.eloPoints)},\"${it.groupName}\"\n") }
                    }
                    CsvType.ELO_LOGS -> {
                        content.append("ID,PlayerID,Nome,Data,Elo,Grupo\n")
                        currentGroupEloLogs.value.forEach { content.append("${it.id},${it.playerId},\"${it.playerNameSnapshot}\",\"${it.date}\",${formatElo(it.elo)},\"${it.groupName}\"\n") }
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
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Salvar $name").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(chooser)
        } catch (e: Exception) { Log.e("Export", "Erro: ${e.message}") }
    }

    private fun smartSplit(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder(); var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { result.add(current.toString().trim()); current.clear() }
                else -> current.append(c)
            }
        }
        result.add(current.toString().trim())
        return result.map { it.replace("\"", "").trim() }
    }
    
    fun generateSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentGroup = _currentGroupConfig.value.groupName
            val names = listOf("Bruno", "Carlos", "Daniel", "Eduardo", "Fernanda", "Gabriela", "Hugo", "Igor", "Julia", "Karina", "Lucas", "Mariana", "Pedro", "Rafael", "Sofia", "Tiago")
            val newPlayers = names.map { name -> Player(name = name, elo = 1200.0, groupName = currentGroup, sex = if(name.endsWith("a")) "F" else "M") }
            repository.insertPlayers(newPlayers)
            kotlinx.coroutines.delay(500)
            val dbPlayers = players.value.filter { it.groupName == currentGroup }.toMutableList()
            if(dbPlayers.isEmpty()) return@launch

            val r = java.util.Random()
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
            
            repeat(25) {
                if (r.nextBoolean()) calendar.add(java.util.Calendar.HOUR_OF_DAY, 2)
                if (r.nextBoolean()) calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                if (calendar.time.after(Date())) return@repeat

                val matchPlayers = dbPlayers.shuffled().take(12)
                if (matchPlayers.size < 4) return@repeat
                val mid = matchPlayers.size / 2
                val teamA = matchPlayers.subList(0, mid); val teamB = matchPlayers.subList(mid, matchPlayers.size)
                
                val avgA = teamA.map { it.elo }.average(); val avgB = teamB.map { it.elo }.average()
                val probA = 1.0 / (1.0 + Math.pow(10.0, (avgB - avgA) / 400.0))
                val winnerA = r.nextDouble() < probA
                val delta = if (winnerA) EloCalculator.calculateEloChange(avgA, avgB) else EloCalculator.calculateEloChange(avgB, avgA)
                
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                
                teamA.forEachIndexed { i, p ->
                    val newElo = if (winnerA) p.elo + delta else p.elo - delta
                    val victory = if (winnerA) 1 else 0
                    val updated = p.copy(elo = newElo, matchesPlayed = p.matchesPlayed + 1, victories = p.victories + victory)
                    val idx = dbPlayers.indexOfFirst { it.id == p.id }
                    if (idx != -1) dbPlayers[idx] = updated
                    repository.insertEloLog(PlayerEloLog(playerId = updated.id, playerNameSnapshot = updated.name, date = dateStr, elo = newElo, groupName = currentGroup))
                }
                
                teamB.forEachIndexed { i, p ->
                    val newElo = if (!winnerA) p.elo + delta else p.elo - delta
                    val victory = if (!winnerA) 1 else 0
                    val updated = p.copy(elo = newElo, matchesPlayed = p.matchesPlayed + 1, victories = p.victories + victory)
                    val idx = dbPlayers.indexOfFirst { it.id == p.id }
                    if (idx != -1) dbPlayers[idx] = updated
                    repository.insertEloLog(PlayerEloLog(playerId = updated.id, playerNameSnapshot = updated.name, date = dateStr, elo = newElo, groupName = currentGroup))
                }
                
                val displayDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                repository.insertMatch(MatchHistory(date = displayDate, teamA = teamA.joinToString(", "){it.name}, teamB = teamB.joinToString(", "){it.name}, winner = if (winnerA) "Time A" else "Time B", eloPoints = delta, groupName = currentGroup))
            }
            repository.updatePlayers(dbPlayers)
        }
    }
    
    // RENOMEADO DE 'shareRankingText' PARA 'shareRanking'
    fun shareRanking(context: Context, playersList: List<Player>) {
        val group = _currentGroupConfig.value.groupName
        val date = _rankingDateFilter.value
        val titleDate = date?.let { try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!) } catch(e:Exception){it} } ?: "Atual"

        val sb = StringBuilder()
        sb.append("🏆 *Ranking Vôlei: $group* ($titleDate) 🏆\n\n")

        playersList.forEachIndexed { i, p ->
            val medal = when(i) { 0->"🥇"; 1->"🥈"; 2->"🥉"; else->"" }
            sb.append("${i+1}. ${p.name} - *${p.elo.toInt()}* $medal\n")
        }
        sb.append("\n📅 Gerado em: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(intent, "Compartilhar Ranking").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    }

    // CORREÇÃO: Patentes atualizadas
    fun getPatente(elo: Double): String = when {
        elo < 1000 -> "🐣 Iniciante"
        elo < 1100 -> "🏐 Aprendiz"
        elo < 1200 -> "🥉 Intermediário"
        elo < 1300 -> "🥈 Avançado"
        else -> "💎 Lenda"
    }
}

class VoleiViewModelFactory(private val application: Application, private val repository: VoleiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(VoleiViewModel::class.java)) {
            return VoleiViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

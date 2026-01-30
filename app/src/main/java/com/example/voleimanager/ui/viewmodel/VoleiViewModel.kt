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

enum class Screen { GAME, RANKING, HISTORY, CHARTS }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CsvType { JOGADORES, HISTORICO, ELO_LOGS }

class VoleiViewModel(application: Application, private val repository: VoleiRepository) : AndroidViewModel(application) {

    // --- NAVEGAÇÃO ---
    private val _currentScreen = MutableStateFlow(Screen.GAME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    fun navigateTo(screen: Screen) { _currentScreen.value = screen }

    // --- CONFIGURAÇÃO DO GRUPO ATUAL ---
    private val _currentGroupConfig = MutableStateFlow(GroupConfig("Geral"))
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

    // --- DADOS BRUTOS (Do Banco) ---
    // 'players' precisa ser público para a Sidebar listar os grupos únicos
    val players = repository.allPlayers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _allHistory = repository.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _allEloLogs = repository.eloLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DADOS FILTRADOS (Solução para isolamento de grupos) ---
    val currentGroupPlayers = combine(players, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupHistory = combine(_allHistory, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentGroupEloLogs = combine(_allEloLogs, _currentGroupConfig) { list, config ->
        list.filter { it.groupName == config.groupName }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FILTROS DE DATA ---
    private val _rankingDateFilter = MutableStateFlow<String?>(null)
    val rankingDateFilter = _rankingDateFilter.asStateFlow()

    private val _historyDateFilter = MutableStateFlow<String?>(null)
    val historyDateFilter = _historyDateFilter.asStateFlow()

    private val _chartSelectedPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val chartSelectedPlayerIds = _chartSelectedPlayerIds.asStateFlow()

    // Datas Disponíveis (Baseadas nos dados filtrados)
    val availableRankingDates = currentGroupEloLogs.map { list ->
        list.map { it.date }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableHistoryDates = currentGroupHistory.map { list ->
        list.map { it.date.split(" ")[0] }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- TEMA ---
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // --- ESTADOS DO JOGO ---
    private val _teamA = MutableStateFlow<List<Player>>(emptyList())
    val teamA = _teamA.asStateFlow()
    private val _teamB = MutableStateFlow<List<Player>>(emptyList())
    val teamB = _teamB.asStateFlow()
    private val _waitingList = MutableStateFlow<List<Player>>(emptyList())
    val waitingList = _waitingList.asStateFlow()
    private val _presentPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val presentPlayerIds = _presentPlayerIds.asStateFlow()

    private val _hasPreviousMatch = MutableStateFlow(false)
    val hasPreviousMatch = _hasPreviousMatch.asStateFlow()
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()
    private val _streakOwner = MutableStateFlow<String?>(null)
    val streakOwner = _streakOwner.asStateFlow()
    private val _lastWinners = MutableStateFlow<List<Player>>(emptyList())
    val lastWinners = _lastWinners.asStateFlow()
    private var lastLosers: List<Player> = emptyList()

    init { loadThemePreference() }

    // --- SETTERS ---
    fun setRankingDateFilter(d: String?) { _rankingDateFilter.value = d }
    fun setHistoryDateFilter(d: String?) { _historyDateFilter.value = d }

    fun toggleChartPlayer(id: Int) {
        val s = _chartSelectedPlayerIds.value.toMutableSet()
        if(s.contains(id)) s.remove(id) else s.add(id)
        _chartSelectedPlayerIds.value = s
    }

    fun setThemeMode(m: ThemeMode) {
        _themeMode.value = m
        getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE).edit().putString("theme", m.name).apply()
    }

    private fun loadThemePreference() {
        val p = getApplication<Application>().getSharedPreferences("volei", Context.MODE_PRIVATE)
        _themeMode.value = try { ThemeMode.valueOf(p.getString("theme", "SYSTEM")!!) } catch (e: Exception) { ThemeMode.SYSTEM }
    }

    // --- LÓGICA DE RANKING ---
    fun getRankingListForDate(date: String?): List<Player> {
        if (date == null) {
            return currentGroupPlayers.value.sortedByDescending { it.elo }
        } else {
            val logsDoDia = currentGroupEloLogs.value.filter { it.date == date }
            return logsDoDia.map { log ->
                Player(id = log.playerId, name = log.playerNameSnapshot, elo = log.elo, groupName = log.groupName)
            }.sortedByDescending { it.elo }
        }
    }

    // --- GRUPOS ---
    fun isGameInProgress(): Boolean {
        return _teamA.value.isNotEmpty() || _teamB.value.isNotEmpty() || _hasPreviousMatch.value
    }

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

    fun updateConfig(s: Int, l: Int) { _currentGroupConfig.value = _currentGroupConfig.value.copy(teamSize = s, victoryLimit = l); viewModelScope.launch { repository.saveGroupConfig(_currentGroupConfig.value) } }
    fun renameGroup(old: String, new: String) = viewModelScope.launch { repository.renameGroup(old, new); if(_currentGroupConfig.value.groupName == old) loadGroupConfig(new) }
    fun deleteGroup(name: String) = viewModelScope.launch { repository.deleteGroup(name); if(_currentGroupConfig.value.groupName == name) loadGroupConfig("Geral") }

    // --- JOGADORES ---
    fun addPlayer(n: String, e: Double, g: String) = viewModelScope.launch { repository.insertPlayer(Player(name = n, elo = e, groupName = g)) }
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
            // Lógica: Só entra na fila se não estiver jogando, nem na fila, e não for um vencedor esperando rodada
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
            _presentPlayerIds.value = emptySet()
            _waitingList.value = emptyList()
        }
    }

    // --- JOGO ---
    fun startNewAutomaticGame(all: List<Player>, size: Int) {
        val avail = all.filter { _presentPlayerIds.value.contains(it.id) }.shuffled()
        if(avail.size < size*2) return
        val res = TeamBalancer.createBalancedTeams(avail.take(size*2), size)
        _teamA.value = res.teamA; _teamB.value = res.teamB; _waitingList.value = avail.drop(size*2)
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
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
        val wait = _waitingList.value.toMutableList().apply { remove(`in`); add(out) }
        val nA = _teamA.value.toMutableList(); val idxA = nA.indexOfFirst { it.id == out.id }
        val nB = _teamB.value.toMutableList(); val idxB = nB.indexOfFirst { it.id == out.id }

        if(idxA != -1) {
            nA[idxA] = `in`
            _teamA.value = nA
            if(_streakOwner.value == "A") _currentStreak.value = 0
        } else if(idxB != -1) {
            nB[idxB] = `in`
            _teamB.value = nB
            if(_streakOwner.value == "B") _currentStreak.value = 0
        }
        _waitingList.value = wait
    }

    fun finishGame(winner: String) {
        val cA = _teamA.value
        val cB = _teamB.value
        if(cA.isEmpty() || cB.isEmpty()) return

        if(_streakOwner.value == winner) _currentStreak.value++ else { _streakOwner.value = winner; _currentStreak.value = 1 }

        val (winners, losers) = if(winner == "A") cA to cB else cB to cA
        _lastWinners.value = winners
        lastLosers = losers
        _hasPreviousMatch.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val avgA = cA.map { it.elo }.average()
            val avgB = cB.map { it.elo }.average()
            val delta = if (winner == "A") EloCalculator.calculateEloChange(avgA, avgB) else EloCalculator.calculateEloChange(avgB, avgA)

            val dateLog = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val dateDisplay = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())

            val updatedPlayers = mutableListOf<Player>()
            val newWinners = mutableListOf<Player>()
            val newLosers = mutableListOf<Player>()

            suspend fun process(list: List<Player>, won: Boolean) {
                list.forEach { p ->
                    val newElo = if(won) p.elo + delta else p.elo - delta
                    val u = p.copy(elo = newElo, matchesPlayed = p.matchesPlayed + 1, victories = if(won) p.victories + 1 else p.victories)
                    updatedPlayers.add(u)
                    if(won) newWinners.add(u) else newLosers.add(u)
                    repository.insertEloLog(PlayerEloLog(u.id, u.name, dateLog, newElo, u.groupName))
                }
            }
            process(if(winner == "A") cA else cB, true)
            process(if(winner == "A") cB else cA, false)

            _lastWinners.value = newWinners
            lastLosers = newLosers

            repository.updatePlayers(updatedPlayers)
            repository.insertMatch(MatchHistory(date = dateDisplay, teamA = cA.joinToString(", "){it.name}, teamB = cB.joinToString(", "){it.name}, winner = "Time $winner", eloPoints = delta, groupName = cA.first().groupName))

            _teamA.value = emptyList()
            _teamB.value = emptyList()
        }
    }

    fun startNextRound() {
        val conf = _currentGroupConfig.value
        val activeWinners = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val pool = (_waitingList.value + lastLosers.filter { _presentPlayerIds.value.contains(it.id) }.shuffled()).filter { p -> activeWinners.none { it.id == p.id } }

        if(_currentStreak.value >= conf.victoryLimit) {
            _currentStreak.value = 0
            _streakOwner.value = null
            startNewAutomaticGame(activeWinners + pool, conf.teamSize)
        } else {
            var teamWin = activeWinners
            var availablePool = pool

            // Completa vencedor
            if(teamWin.size < conf.teamSize) {
                val needed = conf.teamSize - teamWin.size
                if(availablePool.size >= needed) {
                    teamWin = teamWin + availablePool.take(needed)
                    availablePool = availablePool.drop(needed)
                }
            } else if(teamWin.size > conf.teamSize) {
                availablePool = teamWin.drop(conf.teamSize) + availablePool
                teamWin = teamWin.take(conf.teamSize)
            }

            // Novo desafiante
            if(availablePool.size >= conf.teamSize) {
                val teamChal = availablePool.take(conf.teamSize)
                _waitingList.value = availablePool.drop(conf.teamSize)

                if(_streakOwner.value == "B") {
                    _teamB.value = teamWin
                    _teamA.value = teamChal
                } else {
                    _teamA.value = teamWin
                    _teamB.value = teamChal
                    _streakOwner.value = "A"
                }

                // Reseta streak se time mudou IDs
                if(teamWin.map { it.id }.toSet() != _lastWinners.value.map { it.id }.toSet()) {
                    _currentStreak.value = 0
                }
            }
        }
        _hasPreviousMatch.value = false
    }

    // --- CSV & IMPORT/EXPORT ---
    private fun smartSplit(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
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

    fun importCsv(uri: Uri, type: CsvType) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = getApplication<Application>().contentResolver.openInputStream(uri) ?: return@launch
                val lines = BufferedReader(InputStreamReader(stream)).readLines().drop(1)

                when(type) {
                    CsvType.JOGADORES -> {
                        val list = lines.mapNotNull { line ->
                            val cols = smartSplit(line)
                            if(cols.size >= 2) Player(name = cols[0], elo = cols[1].toDoubleOrNull()?:1200.0, matchesPlayed = cols.getOrNull(2)?.toIntOrNull()?:0, victories = cols.getOrNull(3)?.toIntOrNull()?:0, groupName = cols.getOrNull(4)?:"Geral") else null
                        }
                        repository.insertPlayers(list)
                    }
                    CsvType.HISTORICO -> {
                        val list = lines.mapNotNull { line ->
                            val cols = smartSplit(line)
                            if(cols.size >= 5) MatchHistory(date = cols[0], teamA = cols[1], teamB = cols[2], winner = cols[3], eloPoints = cols[4].toDoubleOrNull()?:0.0, groupName = cols.getOrNull(5)?:"Geral") else null
                        }
                        repository.insertHistoryList(list)
                    }
                    CsvType.ELO_LOGS -> {
                        val list = lines.mapNotNull { line ->
                            val cols = smartSplit(line)
                            if(cols.size >= 5) PlayerEloLog(playerId = cols[0].toIntOrNull()?:0, playerNameSnapshot = cols[1], date = cols[2], elo = cols[3].toDoubleOrNull()?:1200.0, groupName = cols[4]) else null
                        }
                        list.forEach { repository.insertEloLog(it) }
                    }
                }
                stream.close()
            } catch (e: Exception) { Log.e("CSV", "Erro import: ${e.message}") }
        }
    }

    fun exportData(context: Context, type: CsvType) {
        viewModelScope.launch(Dispatchers.IO) {
            val sb = StringBuilder()
            val fileName = when(type) {
                CsvType.JOGADORES -> {
                    sb.append("Nome,Elo,Partidas,Vitorias,Grupo\n")
                    currentGroupPlayers.value.forEach { sb.append("\"${it.name}\",${it.elo},${it.matchesPlayed},${it.victories},\"${it.groupName}\"\n") }
                    "jogadores.csv"
                }
                CsvType.HISTORICO -> {
                    sb.append("Data,TimeA,TimeB,Vencedor,EloGanho,Grupo\n")
                    currentGroupHistory.value.forEach { sb.append("\"${it.date}\",\"${it.teamA}\",\"${it.teamB}\",\"${it.winner}\",${it.eloPoints},\"${it.groupName}\"\n") }
                    "historico.csv"
                }
                CsvType.ELO_LOGS -> {
                    sb.append("ID,Nome,Data,Elo,Grupo\n")
                    currentGroupEloLogs.value.forEach { sb.append("${it.playerId},\"${it.playerNameSnapshot}\",\"${it.date}\",${it.elo},\"${it.groupName}\"\n") }
                    "evolucao_elo.csv"
                }
            }
            shareFile(context, fileName, sb.toString())
        }
    }

    private fun shareFile(context: Context, name: String, content: String) {
        try {
            val file = File(context.cacheDir, name)
            FileOutputStream(file).use { it.write(content.toByteArray()) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Exportar $name").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(chooser)
        } catch (e: Exception) { Log.e("CSV", "Erro export: ${e.message}") }
    }

    // --- UTILS & COMPARTILHAMENTO DE RANKING ---
    fun shareRankingText(context: Context, playersList: List<Player>) {
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
        if (modelClass.isAssignableFrom(VoleiViewModel::class.java)) return VoleiViewModel(application, repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
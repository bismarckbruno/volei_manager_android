package com.example.voleimanager.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voleimanager.data.VoleiRepository
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.util.EloCalculator
import com.example.voleimanager.util.TeamBalancer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

// ENUM PARA OS MODOS DE TEMA
enum class ThemeMode { SYSTEM, LIGHT, DARK }

class VoleiViewModel(
    application: Application,
    private val repository: VoleiRepository
) : AndroidViewModel(application) {

    // --- ESTADOS GERAIS ---
    val players: StateFlow<List<Player>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<MatchHistory>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentGroupConfig = MutableStateFlow(GroupConfig("Geral"))
    val currentGroupConfig: StateFlow<GroupConfig> = _currentGroupConfig.asStateFlow()

    // --- ESTADO DO TEMA (NOVO) ---
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // --- ESTADOS DO JOGO ---
    private val _teamA = MutableStateFlow<List<Player>>(emptyList())
    val teamA: StateFlow<List<Player>> = _teamA.asStateFlow()

    private val _teamB = MutableStateFlow<List<Player>>(emptyList())
    val teamB: StateFlow<List<Player>> = _teamB.asStateFlow()

    private val _waitingList = MutableStateFlow<List<Player>>(emptyList())
    val waitingList: StateFlow<List<Player>> = _waitingList.asStateFlow()

    private val _hasPreviousMatch = MutableStateFlow(false)
    val hasPreviousMatch: StateFlow<Boolean> = _hasPreviousMatch.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _streakOwner = MutableStateFlow<String?>(null)
    val streakOwner: StateFlow<String?> = _streakOwner.asStateFlow()

    private val _lastWinners = MutableStateFlow<List<Player>>(emptyList())
    val lastWinners: StateFlow<List<Player>> = _lastWinners.asStateFlow()

    private var lastLosers: List<Player> = emptyList()

    private val _presentPlayerIds = MutableStateFlow<Set<Int>>(emptySet())
    val presentPlayerIds: StateFlow<Set<Int>> = _presentPlayerIds.asStateFlow()

    // --- INICIALIZAÇÃO (Carregar Tema) ---
    init {
        loadThemePreference()
    }

    // --- LÓGICA DE TEMA ---
    private fun loadThemePreference() {
        val prefs = getApplication<Application>().getSharedPreferences("volei_prefs", Context.MODE_PRIVATE)
        val savedThemeName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        _themeMode.value = try {
            ThemeMode.valueOf(savedThemeName ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("volei_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // --- GERENCIAMENTO DE GRUPOS ---
    fun renameGroup(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renameGroup(oldName, newName)
            if (_currentGroupConfig.value.groupName == oldName) {
                loadGroupConfig(newName)
            }
        }
    }

    fun deleteGroup(groupName: String) {
        viewModelScope.launch {
            repository.deleteGroup(groupName)
            if (_currentGroupConfig.value.groupName == groupName) {
                loadGroupConfig("Geral")
            }
        }
    }

    // --- PRESENÇA ---
    fun togglePlayerPresence(player: Player) {
        val currentIds = _presentPlayerIds.value.toMutableSet()
        val isCurrentlyPresent = currentIds.contains(player.id)

        if (isCurrentlyPresent) {
            currentIds.remove(player.id)
            val currentWaiting = _waitingList.value.toMutableList()
            currentWaiting.removeAll { it.id == player.id }
            _waitingList.value = currentWaiting
        } else {
            currentIds.add(player.id)
            val gameRunning = _teamA.value.isNotEmpty() || _teamB.value.isNotEmpty()
            val waitingForNextRound = _hasPreviousMatch.value
            val isWinnerWaiting = waitingForNextRound && _lastWinners.value.any { it.id == player.id }

            if (!isWinnerWaiting && (gameRunning || waitingForNextRound)) {
                val isPlayingA = _teamA.value.any { it.id == player.id }
                val isPlayingB = _teamB.value.any { it.id == player.id }
                val isAlreadyWaiting = _waitingList.value.any { it.id == player.id }
                if (!isPlayingA && !isPlayingB && !isAlreadyWaiting) {
                    _waitingList.value = _waitingList.value + player
                }
            }
        }
        _presentPlayerIds.value = currentIds
    }

    fun setAllPlayersPresence(players: List<Player>, isPresent: Boolean) {
        if (isPresent) {
            _presentPlayerIds.value = players.map { it.id }.toSet()
            val gameRunning = _teamA.value.isNotEmpty() || _teamB.value.isNotEmpty() || _hasPreviousMatch.value
            if (gameRunning) {
                val currentWait = _waitingList.value.toMutableList()
                players.forEach { p ->
                    val playing = _teamA.value.any { it.id == p.id } || _teamB.value.any { it.id == p.id }
                    val waiting = currentWait.any { it.id == p.id }
                    if (!playing && !waiting) currentWait.add(p)
                }
                _waitingList.value = currentWait
            }
        } else {
            _presentPlayerIds.value = emptySet()
            _waitingList.value = emptyList()
        }
    }

    // --- CONFIGURAÇÃO ---
    fun loadGroupConfig(groupName: String) {
        val isSameGroup = _currentGroupConfig.value.groupName == groupName
        viewModelScope.launch {
            val config = repository.getGroupConfig(groupName)
            if (config != null) {
                _currentGroupConfig.value = config
            } else {
                val newConfig = GroupConfig(groupName, teamSize = 6, victoryLimit = 3)
                _currentGroupConfig.value = newConfig
                repository.saveGroupConfig(newConfig)
            }
            if (!isSameGroup) {
                _teamA.value = emptyList(); _teamB.value = emptyList(); _waitingList.value = emptyList()
                _currentStreak.value = 0; _streakOwner.value = null; _presentPlayerIds.value = emptySet(); _hasPreviousMatch.value = false
            }
        }
    }

    fun updateConfig(newTeamSize: Int, newVictoryLimit: Int) {
        val current = _currentGroupConfig.value
        val updated = current.copy(teamSize = newTeamSize, victoryLimit = newVictoryLimit)
        _currentGroupConfig.value = updated
        viewModelScope.launch { repository.saveGroupConfig(updated) }
    }

    // --- CRUD ---
    fun addPlayer(name: String, initialElo: Double, group: String) {
        viewModelScope.launch {
            val newPlayer = Player(name = name, elo = initialElo, groupName = group, matchesPlayed = 0, victories = 0)
            repository.insertPlayer(newPlayer)
        }
    }

    fun deletePlayer(player: Player) { viewModelScope.launch { repository.deletePlayer(player) } }

    fun renamePlayer(player: Player, newName: String) {
        viewModelScope.launch {
            val updatedPlayer = player.copy(name = newName)
            repository.updatePlayer(updatedPlayer)

            _teamA.value = _teamA.value.map { if (it.id == player.id) updatedPlayer else it }
            _teamB.value = _teamB.value.map { if (it.id == player.id) updatedPlayer else it }
            _waitingList.value = _waitingList.value.map { if (it.id == player.id) updatedPlayer else it }

            _lastWinners.value = _lastWinners.value.map { if (it.id == player.id) updatedPlayer else it }
            lastLosers = lastLosers.map { if (it.id == player.id) updatedPlayer else it }
        }
    }

    // --- JOGO ---
    fun startNewAutomaticGame(allGroupPlayers: List<Player>, teamSize: Int) {
        val availablePlayers = allGroupPlayers.filter { _presentPlayerIds.value.contains(it.id) }
        if (availablePlayers.size < teamSize * 2) return
        val shuffled = availablePlayers.shuffled()
        val needed = teamSize * 2
        val pool = shuffled.take(needed)
        val remaining = shuffled.drop(needed)
        val result = TeamBalancer.createBalancedTeams(pool, teamSize)
        _teamA.value = result.teamA; _teamB.value = result.teamB; _waitingList.value = remaining
        _hasPreviousMatch.value = false; _currentStreak.value = 0; _streakOwner.value = null
    }

    fun cancelGame() {
        _waitingList.value = emptyList(); _teamA.value = emptyList(); _teamB.value = emptyList()
        _currentStreak.value = 0; _streakOwner.value = null; _hasPreviousMatch.value = false
    }

    fun substitutePlayer(playerOut: Player, playerIn: Player) {
        val newA = _teamA.value.toMutableList()
        val indexA = newA.indexOfFirst { it.id == playerOut.id }
        val newB = _teamB.value.toMutableList()
        val indexB = newB.indexOfFirst { it.id == playerOut.id }
        val newWaiting = _waitingList.value.toMutableList()
        newWaiting.removeAll { it.id == playerIn.id }
        newWaiting.add(playerOut)
        if (indexA != -1) { newA[indexA] = playerIn; _teamA.value = newA; if (_streakOwner.value == "A") _currentStreak.value = 0 }
        else if (indexB != -1) { newB[indexB] = playerIn; _teamB.value = newB; if (_streakOwner.value == "B") _currentStreak.value = 0 }
        _waitingList.value = newWaiting
    }

    fun finishGame(winner: String) {
        val currentA = _teamA.value
        val currentB = _teamB.value
        if (currentA.isEmpty() || currentB.isEmpty()) return
        if (_streakOwner.value == winner) _currentStreak.value += 1 else { _streakOwner.value = winner; _currentStreak.value = 1 }

        if (winner == "A") { _lastWinners.value = currentA; lastLosers = currentB } else { _lastWinners.value = currentB; lastLosers = currentA }

        _hasPreviousMatch.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val avgEloA = currentA.map { it.elo }.average()
            val avgEloB = currentB.map { it.elo }.average()
            val delta = if (winner == "A") EloCalculator.calculateEloChange(avgEloA, avgEloB) else EloCalculator.calculateEloChange(avgEloB, avgEloA)
            val updatedPlayers = mutableListOf<Player>(); val newWinners = mutableListOf<Player>(); val newLosers = mutableListOf<Player>()
            currentA.forEach { p -> updatePlayerState(p, winner == "A", delta, updatedPlayers, newWinners, newLosers) }
            currentB.forEach { p -> updatePlayerState(p, winner == "B", delta, updatedPlayers, newWinners, newLosers) }

            _lastWinners.value = newWinners; lastLosers = newLosers

            repository.updatePlayers(updatedPlayers)
            repository.insertMatch(MatchHistory(date = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date()), teamA = currentA.joinToString(", ") { it.name }, teamB = currentB.joinToString(", ") { it.name }, winner = "Time $winner", eloPoints = delta, groupName = currentA.firstOrNull()?.groupName ?: "Geral"))
            _teamA.value = emptyList(); _teamB.value = emptyList()
        }
    }

    private fun updatePlayerState(p: Player, win: Boolean, delta: Double, all: MutableList<Player>, wins: MutableList<Player>, loses: MutableList<Player>) {
        val newElo = if (win) p.elo + delta else p.elo - delta
        val updated = p.copy(elo = newElo, matchesPlayed = p.matchesPlayed + 1, victories = if (win) p.victories + 1 else p.victories)
        all.add(updated)
        if (win) wins.add(updated) else loses.add(updated)
    }

    fun startNextRound() {
        val config = _currentGroupConfig.value
        val teamSize = config.teamSize
        val victoryLimit = config.victoryLimit

        val activeWinners = _lastWinners.value.filter { _presentPlayerIds.value.contains(it.id) }
        val currentQueue = _waitingList.value
        val activeLosers = lastLosers.filter { _presentPlayerIds.value.contains(it.id) }
        val losersShuffled = activeLosers.shuffled()
        val rawPool = currentQueue + losersShuffled
        var availablePool = rawPool.filter { p -> activeWinners.none { w -> w.id == p.id } }

        if (_currentStreak.value >= victoryLimit) {
            _currentStreak.value = 0; _streakOwner.value = null
            val winnersShuffled = activeWinners.shuffled()
            val splitIndex = winnersShuffled.size / 2
            val seedA = winnersShuffled.take(splitIndex).toMutableList()
            val seedB = winnersShuffled.drop(splitIndex).toMutableList()
            val neededA = teamSize - seedA.size; val neededB = teamSize - seedB.size
            if (availablePool.size >= neededA + neededB) {
                val entrants = availablePool.take(neededA + neededB)
                val remainingQueue = availablePool.drop(neededA + neededB)
                val result = TeamBalancer.createBalancedTeams(entrants, teamSize, seedA, seedB)
                _teamA.value = result.teamA; _teamB.value = result.teamB; _waitingList.value = remainingQueue
            }
        } else {
            var newWinningTeam = activeWinners
            val originalWinnerIds = _lastWinners.value.map { it.id }.toSet()
            if (newWinningTeam.size < teamSize) {
                val needed = teamSize - newWinningTeam.size
                if (availablePool.size >= needed) { val reinforcements = availablePool.take(needed); availablePool = availablePool.drop(needed); newWinningTeam = newWinningTeam + reinforcements }
            } else if (newWinningTeam.size > teamSize) {
                val cutPlayers = newWinningTeam.drop(teamSize); newWinningTeam = newWinningTeam.take(teamSize); availablePool = cutPlayers + availablePool
            }
            if (availablePool.size >= teamSize) {
                val challengers = availablePool.take(teamSize); val remainingQueue = availablePool.drop(teamSize)
                _teamA.value = newWinningTeam; _teamB.value = challengers; _waitingList.value = remainingQueue
                if (_currentStreak.value > 0) {
                    _streakOwner.value = "A"
                    val newWinnerIds = newWinningTeam.map { it.id }.toSet()
                    if (originalWinnerIds != newWinnerIds) _currentStreak.value = 0
                }
            }
        }
    }

    fun startManualGame(manualTeamA: List<Player>, manualTeamB: List<Player>, remainingPlayers: List<Player>) {
        _teamA.value = manualTeamA
        _teamB.value = manualTeamB
        _waitingList.value = remainingPlayers
        _hasPreviousMatch.value = false
        _currentStreak.value = 0
        _streakOwner.value = null
    }

    enum class ImportType { JOGADORES, HISTORICO }
    fun importCsv(uri: Uri, type: ImportType) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines().map { it.replace("\uFEFF", "") }
                if (type == ImportType.JOGADORES) parseAndSavePlayers(lines) else parseAndSaveHistory(lines)
                reader.close(); inputStream.close()
            } catch (e: Exception) { Log.e("VoleiImport", "Erro: ${e.message}") }
        }
    }
    private fun smartSplit(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder(); var inQuotes = false
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> { result.add(currentField.toString()); currentField.clear() }
                else -> currentField.append(char)
            }
        }
        result.add(currentField.toString())
        return result
    }
    private suspend fun parseAndSavePlayers(lines: List<String>) {
        val playersList = mutableListOf<Player>()
        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) continue
            if (index == 0 && (line.lowercase().contains("nome"))) continue
            try {
                val parts = smartSplit(line)
                if (parts.size >= 2) {
                    val nome = parts[0].trim().replace("\"", "")
                    val elo = parts[1].replace("\"", "").replace(",", ".").trim().toDoubleOrNull() ?: 1200.0
                    val partidas = parts.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
                    val vitorias = parts.getOrNull(3)?.trim()?.toIntOrNull() ?: 0
                    val grupo = parts.getOrNull(4)?.trim()?.replace("\"", "")?.ifBlank { "Geral" } ?: "Geral"
                    playersList.add(Player(name = nome, elo = elo, matchesPlayed = partidas, victories = vitorias, groupName = grupo))
                }
            } catch (e: Exception) { Log.e("VoleiImport", "Erro linha: $line") }
        }
        repository.insertPlayers(playersList)
    }
    private suspend fun parseAndSaveHistory(lines: List<String>) {
        val historyList = mutableListOf<MatchHistory>()
        for ((index, line) in lines.withIndex()) {
            if (line.isBlank()) continue
            if (index == 0 && (line.lowercase().contains("data"))) continue
            try {
                val parts = smartSplit(line)
                if (parts.size >= 5) {
                    val data = parts[0].trim().replace("\"", "")
                    val timeA = parts[1].trim().replace("\"", "").replace(";", ",")
                    val timeB = parts[2].trim().replace("\"", "").replace(";", ",")
                    val vencedor = parts[3].trim().replace("\"", "")
                    val eloPoints = parts[4].replace("\"", "").replace("'", "").replace("+", "").replace(",", ".").trim().toDoubleOrNull() ?: 0.0
                    val grupo = parts.getOrNull(5)?.trim()?.replace("\"", "")?.ifBlank { "Geral" } ?: "Geral"
                    historyList.add(MatchHistory(date = data, teamA = timeA, teamB = timeB, winner = vencedor, eloPoints = eloPoints, groupName = grupo))
                }
            } catch (e: Exception) { Log.e("VoleiImport", "Erro linha: $line") }
        }
        repository.insertHistoryList(historyList)
    }
}

class VoleiViewModelFactory(private val application: Application, private val repository: VoleiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoleiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VoleiViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
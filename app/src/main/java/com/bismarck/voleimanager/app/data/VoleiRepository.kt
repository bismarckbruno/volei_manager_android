package com.bismarck.voleimanager.app.data

import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import kotlinx.coroutines.flow.Flow

class VoleiRepository(private val voleiDao: com.bismarck.voleimanager.app.data.VoleiDao) {

    val allPlayers: Flow<List<com.bismarck.voleimanager.app.data.model.Player>> = voleiDao.getAllPlayers()
    val history: Flow<List<com.bismarck.voleimanager.app.data.model.MatchHistory>> = voleiDao.getHistory()

    // --- ESTA LINHA CORRIGE O ERRO 'eloLogs' ---
    val eloLogs: Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>> = voleiDao.getAllEloLogs()

    // --- Players ---
    suspend fun insertPlayer(player: com.bismarck.voleimanager.app.data.model.Player): Long = voleiDao.insertPlayer(player)
    suspend fun insertPlayers(players: List<com.bismarck.voleimanager.app.data.model.Player>) = voleiDao.insertPlayers(players)
    suspend fun updatePlayers(players: List<com.bismarck.voleimanager.app.data.model.Player>) = voleiDao.updatePlayers(players)
    suspend fun updatePlayer(player: com.bismarck.voleimanager.app.data.model.Player) = voleiDao.updatePlayer(player)

    suspend fun renamePlayerCascade(playerId: Int, oldName: String, newName: String, groupName: String) {
        val historyToUpdate = voleiDao.getAllHistorySync().filter { match ->
            val idsA = if (match.teamAIds.isBlank()) emptyList() else match.teamAIds.split(",").map { it.trim() }
            val idsB = if (match.teamBIds.isBlank()) emptyList() else match.teamBIds.split(",").map { it.trim() }
            match.groupName == groupName && (
                idsA.contains(playerId.toString()) ||
                idsB.contains(playerId.toString()) ||
                (idsA.isEmpty() && match.teamA.split(", ").contains(oldName)) ||
                (idsB.isEmpty() && match.teamB.split(", ").contains(oldName))
            )
        }.map { match ->
            val namesA = match.teamA.split(", ").toMutableList()
            val idsA = if (match.teamAIds.isBlank()) emptyList() else match.teamAIds.split(",").map { it.trim() }
            val mutableIdsA = idsA.toMutableList()
            
            if (idsA.contains(playerId.toString())) {
                val index = idsA.indexOf(playerId.toString())
                if (index >= 0 && index < namesA.size) namesA[index] = newName
            } else if (idsA.isEmpty()) {
                val index = namesA.indexOf(oldName)
                if (index >= 0) namesA[index] = newName
            }
            
            val pairedA = namesA.zip(if (idsA.isEmpty()) List(namesA.size) { "" } else mutableIdsA).sortedBy { it.first.lowercase() }
            val newTeamA = pairedA.joinToString(", ") { it.first }
            val newTeamAIds = pairedA.joinToString(",") { it.second }.takeIf { idsA.isNotEmpty() } ?: ""

            val namesB = match.teamB.split(", ").toMutableList()
            val idsB = if (match.teamBIds.isBlank()) emptyList() else match.teamBIds.split(",").map { it.trim() }
            val mutableIdsB = idsB.toMutableList()
            
            if (idsB.contains(playerId.toString())) {
                val index = idsB.indexOf(playerId.toString())
                if (index >= 0 && index < namesB.size) namesB[index] = newName
            } else if (idsB.isEmpty()) {
                val index = namesB.indexOf(oldName)
                if (index >= 0) namesB[index] = newName
            }

            val pairedB = namesB.zip(if (idsB.isEmpty()) List(namesB.size) { "" } else mutableIdsB).sortedBy { it.first.lowercase() }
            val newTeamB = pairedB.joinToString(", ") { it.first }
            val newTeamBIds = pairedB.joinToString(",") { it.second }.takeIf { idsB.isNotEmpty() } ?: ""

            match.copy(teamA = newTeamA, teamAIds = newTeamAIds, teamB = newTeamB, teamBIds = newTeamBIds)
        }
        if (historyToUpdate.isNotEmpty()) {
            voleiDao.updateMatchHistories(historyToUpdate)
        }

        val logsToUpdate = voleiDao.getAllEloLogsSync().filter {
            it.groupName == groupName && it.playerId == playerId
        }.map { log ->
            log.copy(playerNameSnapshot = newName)
        }
        if (logsToUpdate.isNotEmpty()) {
            voleiDao.updatePlayerEloLogs(logsToUpdate)
        }
    }
    suspend fun deletePlayer(player: com.bismarck.voleimanager.app.data.model.Player) = voleiDao.deletePlayer(player)

    // --- History ---
    suspend fun insertMatch(match: com.bismarck.voleimanager.app.data.model.MatchHistory) = voleiDao.insertMatch(match)
    suspend fun insertHistoryList(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>) = voleiDao.insertHistoryList(history)

    // --- Elo Logs (ESTA FUNÇÃO CORRIGE O ERRO 'insertEloLog') ---
    suspend fun insertEloLog(log: com.bismarck.voleimanager.app.data.model.PlayerEloLog) = voleiDao.insertEloLog(log)

    // --- Configs ---
    suspend fun getGroupConfig(groupName: String) = voleiDao.getGroupConfig(groupName)
    suspend fun saveGroupConfig(config: com.bismarck.voleimanager.app.data.model.GroupConfig) = voleiDao.saveGroupConfig(config)
    suspend fun getAllGroupConfigs() = voleiDao.getAllGroupConfigs()

    // --- Group Management ---
    suspend fun renameGroup(oldName: String, newName: String) {
        voleiDao.updatePlayerGroupNames(oldName, newName)
        voleiDao.updateHistoryGroupNames(oldName, newName)
        voleiDao.updateConfigGroupNames(oldName, newName)
        // Atualiza logs também
        voleiDao.updateEloLogGroupNames(oldName, newName)
    }

    suspend fun deleteGroup(groupName: String) {
        voleiDao.deletePlayersByGroup(groupName)
        voleiDao.deleteHistoryByGroup(groupName)
        voleiDao.deleteConfigByGroup(groupName)
        // Deleta logs também
        voleiDao.deleteEloLogsByGroup(groupName)
    }
}



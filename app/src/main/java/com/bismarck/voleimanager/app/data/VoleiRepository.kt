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
    suspend fun deletePlayer(player: com.bismarck.voleimanager.app.data.model.Player) = voleiDao.deletePlayer(player)

    // --- History ---
    suspend fun insertMatch(match: com.bismarck.voleimanager.app.data.model.MatchHistory) = voleiDao.insertMatch(match)
    suspend fun insertHistoryList(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>) = voleiDao.insertHistoryList(history)

    // --- Elo Logs (ESTA FUNÇÃO CORRIGE O ERRO 'insertEloLog') ---
    suspend fun insertEloLog(log: com.bismarck.voleimanager.app.data.model.PlayerEloLog) = voleiDao.insertEloLog(log)

    // --- Configs ---
    suspend fun getGroupConfig(groupName: String) = voleiDao.getGroupConfig(groupName)
    suspend fun saveGroupConfig(config: com.bismarck.voleimanager.app.data.model.GroupConfig) = voleiDao.saveGroupConfig(config)

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

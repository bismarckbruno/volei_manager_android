package com.example.voleimanager.data

import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog
import kotlinx.coroutines.flow.Flow

class VoleiRepository(private val voleiDao: VoleiDao) {

    val allPlayers: Flow<List<Player>> = voleiDao.getAllPlayers()
    val history: Flow<List<MatchHistory>> = voleiDao.getHistory()

    // --- ESTA LINHA CORRIGE O ERRO 'eloLogs' ---
    val eloLogs: Flow<List<PlayerEloLog>> = voleiDao.getAllEloLogs()

    // --- Players ---
    suspend fun insertPlayer(player: Player) = voleiDao.insertPlayer(player)
    suspend fun insertPlayers(players: List<Player>) = voleiDao.insertPlayers(players)
    suspend fun updatePlayers(players: List<Player>) = voleiDao.updatePlayers(players)
    suspend fun updatePlayer(player: Player) = voleiDao.updatePlayer(player)
    suspend fun deletePlayer(player: Player) = voleiDao.deletePlayer(player)

    // --- History ---
    suspend fun insertMatch(match: MatchHistory) = voleiDao.insertMatch(match)
    suspend fun insertHistoryList(history: List<MatchHistory>) = voleiDao.insertHistoryList(history)

    // --- Elo Logs (ESTA FUNÇÃO CORRIGE O ERRO 'insertEloLog') ---
    suspend fun insertEloLog(log: PlayerEloLog) = voleiDao.insertEloLog(log)

    // --- Configs ---
    suspend fun getGroupConfig(groupName: String) = voleiDao.getGroupConfig(groupName)
    suspend fun saveGroupConfig(config: GroupConfig) = voleiDao.saveGroupConfig(config)

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
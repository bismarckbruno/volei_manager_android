package com.example.voleimanager.data

import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import kotlinx.coroutines.flow.Flow

class VoleiRepository(private val dao: VoleiDao) {

    // --- CORREÇÃO DOS ERROS AQUI ---
    // O ViewModel estava procurando por 'allPlayers' e 'history', que agora voltaram:
    val allPlayers: Flow<List<Player>> = dao.getAllPlayers()
    val history: Flow<List<MatchHistory>> = dao.getAllHistory()

    // Métodos específicos de grupo
    fun getPlayersByGroup(group: String): Flow<List<Player>> = dao.getPlayersByGroup(group)
    fun getHistoryByGroup(group: String): Flow<List<MatchHistory>> = dao.getHistoryByGroup(group)

    val allGroups: Flow<List<String>> = dao.getAllGroups()

    // Métodos de inserção
    suspend fun insertPlayer(player: Player) { dao.insertPlayer(player) }
    suspend fun insertPlayers(players: List<Player>) { dao.insertAllPlayers(players) }

    suspend fun insertMatch(match: MatchHistory) { dao.insertMatch(match) }
    suspend fun insertHistoryList(matches: List<MatchHistory>) { dao.insertAllMatches(matches) }

    // Métodos de configuração
    suspend fun getGroupConfig(group: String): GroupConfig? = dao.getGroupConfig(group)
    suspend fun saveGroupConfig(config: GroupConfig) { dao.insertGroupConfig(config) }

    suspend fun deletePlayer(player: Player) { dao.deletePlayer(player) }

    suspend fun updatePlayer(player: Player) { dao.updatePlayer(player) }
}
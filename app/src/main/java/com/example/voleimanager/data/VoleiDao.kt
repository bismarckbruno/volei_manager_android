package com.example.voleimanager.data

import androidx.room.*
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface VoleiDao {
    // --- JOGADORES ---
    @Query("SELECT * FROM players ORDER BY elo DESC")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    // Usado na Importação (Protege dados existentes)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(players: List<Player>)

    // NOVO: Usado no Fim do Jogo (Força a atualização dos Elos)
    @Update
    suspend fun updatePlayers(players: List<Player>)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    // --- HISTÓRICO ---
    @Query("SELECT * FROM match_history ORDER BY id DESC")
    fun getHistory(): Flow<List<MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMatch(match: MatchHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryList(history: List<MatchHistory>)

    // --- CONFIGURAÇÕES DE GRUPO ---
    @Query("SELECT * FROM group_configs WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupConfig(groupName: String): GroupConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGroupConfig(config: GroupConfig)

    // --- GERENCIAMENTO DE GRUPOS ---
    @Query("UPDATE players SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updatePlayerGroupNames(oldName: String, newName: String)

    @Query("UPDATE match_history SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateHistoryGroupNames(oldName: String, newName: String)

    @Query("UPDATE group_configs SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateConfigGroupNames(oldName: String, newName: String)

    @Query("DELETE FROM players WHERE groupName = :groupName")
    suspend fun deletePlayersByGroup(groupName: String)

    @Query("DELETE FROM match_history WHERE groupName = :groupName")
    suspend fun deleteHistoryByGroup(groupName: String)

    @Query("DELETE FROM group_configs WHERE groupName = :groupName")
    suspend fun deleteConfigByGroup(groupName: String)
}
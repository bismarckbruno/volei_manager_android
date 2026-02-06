package com.example.voleimanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog
import kotlinx.coroutines.flow.Flow

@Dao
interface VoleiDao {

    // --- PLAYERS ---
    @Query("SELECT * FROM players ORDER BY elo DESC")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(players: List<Player>)

    @Update
    suspend fun updatePlayers(players: List<Player>)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    // --- HISTORY ---
    @Query("SELECT * FROM match_history ORDER BY id DESC")
    fun getHistory(): Flow<List<MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryList(history: List<MatchHistory>)

    // --- ELO LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEloLog(log: PlayerEloLog)

    @Query("SELECT * FROM elo_logs ORDER BY date ASC")
    fun getAllEloLogs(): Flow<List<PlayerEloLog>>

    // --- CONFIGS ---
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

    @Query("UPDATE elo_logs SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateEloLogGroupNames(oldName: String, newName: String)

    @Query("DELETE FROM players WHERE groupName = :groupName")
    suspend fun deletePlayersByGroup(groupName: String)

    @Query("DELETE FROM match_history WHERE groupName = :groupName")
    suspend fun deleteHistoryByGroup(groupName: String)

    @Query("DELETE FROM group_configs WHERE groupName = :groupName")
    suspend fun deleteConfigByGroup(groupName: String)

    @Query("DELETE FROM elo_logs WHERE groupName = :groupName")
    suspend fun deleteEloLogsByGroup(groupName: String)
}
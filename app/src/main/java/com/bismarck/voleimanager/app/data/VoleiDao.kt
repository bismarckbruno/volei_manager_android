package com.bismarck.voleimanager.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import kotlinx.coroutines.flow.Flow

@Dao
interface VoleiDao {

    // --- PLAYERS ---
    @Query("SELECT * FROM players ORDER BY elo DESC")
    fun getAllPlayers(): Flow<List<com.bismarck.voleimanager.app.data.model.Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: com.bismarck.voleimanager.app.data.model.Player): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(players: List<com.bismarck.voleimanager.app.data.model.Player>)

    @Update
    suspend fun updatePlayers(players: List<com.bismarck.voleimanager.app.data.model.Player>)

    @Update
    suspend fun updatePlayer(player: com.bismarck.voleimanager.app.data.model.Player)

    @Delete
    suspend fun deletePlayer(player: com.bismarck.voleimanager.app.data.model.Player)

    // --- HISTORY ---
    @Query("SELECT * FROM match_history ORDER BY id DESC")
    fun getHistory(): Flow<List<com.bismarck.voleimanager.app.data.model.MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: com.bismarck.voleimanager.app.data.model.MatchHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryList(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>)

    @Update
    suspend fun updateMatchHistories(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>)

    @Query("SELECT * FROM match_history")
    suspend fun getAllHistorySync(): List<com.bismarck.voleimanager.app.data.model.MatchHistory>

    // --- ELO LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEloLog(log: com.bismarck.voleimanager.app.data.model.PlayerEloLog)

    @Query("SELECT * FROM elo_logs ORDER BY date ASC")
    fun getAllEloLogs(): Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>>

    @Query("SELECT * FROM elo_logs")
    suspend fun getAllEloLogsSync(): List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>

    @Update
    suspend fun updatePlayerEloLogs(logs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>)

    // --- CONFIGS ---
    @Query("SELECT * FROM group_configs WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupConfig(groupName: String): com.bismarck.voleimanager.app.data.model.GroupConfig?

    @Query("SELECT * FROM group_configs")
    suspend fun getAllGroupConfigs(): List<com.bismarck.voleimanager.app.data.model.GroupConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGroupConfig(config: com.bismarck.voleimanager.app.data.model.GroupConfig)

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



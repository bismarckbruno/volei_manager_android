package com.example.voleimanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface VoleiDao {
    // --- JOGADORES ---

    // ESTA É A FUNÇÃO QUE FALTAVA: Pega todos (para calcular os grupos na tela inicial)
    @Query("SELECT * FROM players ORDER BY elo DESC")
    fun getAllPlayers(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE groupName = :group ORDER BY elo DESC")
    fun getPlayersByGroup(group: String): Flow<List<Player>>

    @Query("SELECT DISTINCT groupName FROM players")
    fun getAllGroups(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlayers(players: List<Player>)

    // --- HISTÓRICO ---

    // ESTA TAMBÉM FALTAVA:
    @Query("SELECT * FROM match_history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<MatchHistory>>

    @Query("SELECT * FROM match_history WHERE groupName = :group ORDER BY id DESC")
    fun getHistoryByGroup(group: String): Flow<List<MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMatches(matches: List<MatchHistory>)

    // --- CONFIGURAÇÃO DE GRUPO ---
    @Query("SELECT * FROM group_configs WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupConfig(groupName: String): GroupConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupConfig(config: GroupConfig)

    @androidx.room.Delete
    suspend fun deletePlayer(player: Player)

    @androidx.room.Update
    suspend fun updatePlayer(player: Player)
}
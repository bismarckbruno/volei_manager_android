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

    @Query("SELECT * FROM players WHERE groupName = :groupName ORDER BY elo DESC")
    fun getPlayersByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.Player>>

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

    @Query("SELECT * FROM match_history WHERE groupName = :groupName ORDER BY id DESC")
    fun getHistoryByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.MatchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: com.bismarck.voleimanager.app.data.model.MatchHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryList(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>)

    @Update
    suspend fun updateMatchHistories(history: List<com.bismarck.voleimanager.app.data.model.MatchHistory>)

    @Query("SELECT * FROM match_history")
    suspend fun getAllHistorySync(): List<com.bismarck.voleimanager.app.data.model.MatchHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEloLogs(logs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>)

    @Query("SELECT * FROM players WHERE groupName = :groupName")
    suspend fun getPlayersByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.Player>

    @Query("SELECT * FROM match_history WHERE groupName = :groupName")
    suspend fun getHistoryByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.MatchHistory>

    @Query("SELECT * FROM elo_logs WHERE groupName = :groupName")
    suspend fun getEloLogsByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>

    // --- ELO LOGS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEloLog(log: com.bismarck.voleimanager.app.data.model.PlayerEloLog)

    @Query("SELECT * FROM elo_logs ORDER BY date ASC")
    fun getAllEloLogs(): Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>>

    @Query("SELECT * FROM elo_logs WHERE groupName = :groupName ORDER BY date ASC, id ASC")
    fun getEloLogsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>>

    @Query("SELECT * FROM elo_logs")
    suspend fun getAllEloLogsSync(): List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>

    @Update
    suspend fun updatePlayerEloLogs(logs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>)

    // --- CONFIGS ---
    @Query("SELECT * FROM group_configs WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupConfig(groupName: String): com.bismarck.voleimanager.app.data.model.GroupConfig?

    @Query("SELECT * FROM group_configs")
    suspend fun getAllGroupConfigs(): List<com.bismarck.voleimanager.app.data.model.GroupConfig>

    @Query("SELECT * FROM group_configs")
    fun getAllGroupConfigsFlow(): Flow<List<com.bismarck.voleimanager.app.data.model.GroupConfig>>

    @Query(
        """
        SELECT groupName FROM group_configs
        UNION
        SELECT groupName FROM players
        UNION
        SELECT groupName FROM match_history
        UNION
        SELECT groupName FROM elo_logs
        ORDER BY groupName COLLATE NOCASE
        """
    )
    suspend fun getAllGroupNames(): List<String>

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

    // --- TOURNAMENT TEAMS ---
    @Query("SELECT * FROM tournament_teams WHERE groupName = :groupName ORDER BY teamKey ASC")
    fun getTournamentTeamsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentTeam>>

    @Query("SELECT * FROM tournament_teams WHERE groupName = :groupName ORDER BY teamKey ASC")
    suspend fun getTournamentTeamsByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.TournamentTeam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeams(teams: List<com.bismarck.voleimanager.app.data.model.TournamentTeam>)

    @Update
    suspend fun updateTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam)

    @Delete
    suspend fun deleteTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam)

    // --- TOURNAMENT TEAM MEMBERS ---
    @Query("SELECT * FROM tournament_team_members WHERE groupName = :groupName ORDER BY id ASC")
    fun getTournamentTeamMembersByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>>

    @Query("SELECT * FROM tournament_team_members WHERE groupName = :groupName ORDER BY id ASC")
    suspend fun getTournamentTeamMembersByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeamMember(member: com.bismarck.voleimanager.app.data.model.TournamentTeamMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeamMembers(members: List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>)

    @Update
    suspend fun updateTournamentTeamMembers(members: List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>)

    @Delete
    suspend fun deleteTournamentTeamMember(member: com.bismarck.voleimanager.app.data.model.TournamentTeamMember)

    // --- TOURNAMENT MATCHES ---
    @Query("SELECT * FROM tournament_matches WHERE groupName = :groupName ORDER BY roundIndex ASC, orderInRound ASC")
    fun getTournamentMatchesByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentMatch>>

    @Query("SELECT * FROM tournament_matches WHERE groupName = :groupName ORDER BY roundIndex ASC, orderInRound ASC")
    suspend fun getTournamentMatchesByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.TournamentMatch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentMatch(match: com.bismarck.voleimanager.app.data.model.TournamentMatch): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentMatches(matches: List<com.bismarck.voleimanager.app.data.model.TournamentMatch>)

    @Update
    suspend fun updateTournamentMatch(match: com.bismarck.voleimanager.app.data.model.TournamentMatch)

    @Update
    suspend fun updateTournamentMatches(matches: List<com.bismarck.voleimanager.app.data.model.TournamentMatch>)

    // --- GROUP LOGS ---
    @Query("SELECT * FROM group_logs WHERE groupName = :groupName ORDER BY timestamp DESC, id DESC")
    fun getGroupLogsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.GroupLog>>

    @Query("SELECT * FROM group_logs WHERE groupName = :groupName ORDER BY timestamp DESC, id DESC")
    suspend fun getGroupLogsByGroupSync(groupName: String): List<com.bismarck.voleimanager.app.data.model.GroupLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupLog(log: com.bismarck.voleimanager.app.data.model.GroupLog): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroupLogs(logs: List<com.bismarck.voleimanager.app.data.model.GroupLog>)

    // --- GERENCIAMENTO DE GRUPOS (tabelas de campeonato e logs) ---
    @Query("UPDATE tournament_teams SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateTournamentTeamGroupNames(oldName: String, newName: String)

    @Query("UPDATE tournament_team_members SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateTournamentTeamMemberGroupNames(oldName: String, newName: String)

    @Query("UPDATE tournament_matches SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateTournamentMatchGroupNames(oldName: String, newName: String)

    @Query("UPDATE group_logs SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateGroupLogGroupNames(oldName: String, newName: String)

    @Query("DELETE FROM tournament_teams WHERE groupName = :groupName")
    suspend fun deleteTournamentTeamsByGroup(groupName: String)

    @Query("DELETE FROM tournament_team_members WHERE groupName = :groupName")
    suspend fun deleteTournamentTeamMembersByGroup(groupName: String)

    @Query("DELETE FROM tournament_matches WHERE groupName = :groupName")
    suspend fun deleteTournamentMatchesByGroup(groupName: String)

    @Query("DELETE FROM group_logs WHERE groupName = :groupName")
    suspend fun deleteGroupLogsByGroup(groupName: String)
}

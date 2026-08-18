package com.bismarck.voleimanager.app.data

import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import kotlinx.coroutines.flow.Flow

class VoleiRepository(private val voleiDao: com.bismarck.voleimanager.app.data.VoleiDao) {

    val allPlayers: Flow<List<com.bismarck.voleimanager.app.data.model.Player>> = voleiDao.getAllPlayers()
    val history: Flow<List<com.bismarck.voleimanager.app.data.model.MatchHistory>> = voleiDao.getHistory()
    val allGroupConfigs: Flow<List<com.bismarck.voleimanager.app.data.model.GroupConfig>> = voleiDao.getAllGroupConfigsFlow()

    // --- ESTA LINHA CORRIGE O ERRO 'eloLogs' ---
    val eloLogs: Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>> = voleiDao.getAllEloLogs()
    fun playersByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.Player>> =
        voleiDao.getPlayersByGroup(groupName)

    fun historyByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.MatchHistory>> =
        voleiDao.getHistoryByGroup(groupName)

    fun eloLogsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>> =
        voleiDao.getEloLogsByGroup(groupName)

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
    suspend fun insertEloLogs(logs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>) = voleiDao.insertEloLogs(logs)

    suspend fun getPlayersByGroupSync(groupName: String) = voleiDao.getPlayersByGroupSync(groupName)
    suspend fun getHistoryByGroupSync(groupName: String) = voleiDao.getHistoryByGroupSync(groupName)
    suspend fun getEloLogsByGroupSync(groupName: String) = voleiDao.getEloLogsByGroupSync(groupName)

    // --- Configs ---
    suspend fun getGroupConfig(groupName: String) = voleiDao.getGroupConfig(groupName)
    suspend fun saveGroupConfig(config: com.bismarck.voleimanager.app.data.model.GroupConfig) = voleiDao.saveGroupConfig(config)
    suspend fun getAllGroupConfigs() = voleiDao.getAllGroupConfigs()
    suspend fun getAllGroupNames() = voleiDao.getAllGroupNames()

    // --- Group Management ---
    suspend fun renameGroup(oldName: String, newName: String) {
        voleiDao.updatePlayerGroupNames(oldName, newName)
        voleiDao.updateHistoryGroupNames(oldName, newName)
        voleiDao.updateConfigGroupNames(oldName, newName)
        // Atualiza logs também
        voleiDao.updateEloLogGroupNames(oldName, newName)
        voleiDao.updateTournamentTeamGroupNames(oldName, newName)
        voleiDao.updateTournamentTeamMemberGroupNames(oldName, newName)
        voleiDao.updateTournamentMatchGroupNames(oldName, newName)
        voleiDao.updateGroupLogGroupNames(oldName, newName)
    }

    suspend fun deleteGroup(groupName: String) {
        voleiDao.deletePlayersByGroup(groupName)
        voleiDao.deleteHistoryByGroup(groupName)
        voleiDao.deleteConfigByGroup(groupName)
        // Deleta logs também
        voleiDao.deleteEloLogsByGroup(groupName)
        voleiDao.deleteTournamentTeamsByGroup(groupName)
        voleiDao.deleteTournamentTeamMembersByGroup(groupName)
        voleiDao.deleteTournamentMatchesByGroup(groupName)
        voleiDao.deleteGroupLogsByGroup(groupName)
    }

    // --- Tournament Teams ---
    fun tournamentTeamsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentTeam>> =
        voleiDao.getTournamentTeamsByGroup(groupName)

    suspend fun getTournamentTeamsByGroupSync(groupName: String) = voleiDao.getTournamentTeamsByGroupSync(groupName)
    suspend fun insertTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam) = voleiDao.insertTournamentTeam(team)
    suspend fun insertTournamentTeams(teams: List<com.bismarck.voleimanager.app.data.model.TournamentTeam>) = voleiDao.insertTournamentTeams(teams)
    suspend fun updateTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam) = voleiDao.updateTournamentTeam(team)
    suspend fun deleteTournamentTeam(team: com.bismarck.voleimanager.app.data.model.TournamentTeam) = voleiDao.deleteTournamentTeam(team)

    // --- Tournament Team Members ---
    fun tournamentTeamMembersByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>> =
        voleiDao.getTournamentTeamMembersByGroup(groupName)

    suspend fun getTournamentTeamMembersByGroupSync(groupName: String) = voleiDao.getTournamentTeamMembersByGroupSync(groupName)
    suspend fun insertTournamentTeamMember(member: com.bismarck.voleimanager.app.data.model.TournamentTeamMember) = voleiDao.insertTournamentTeamMember(member)
    suspend fun insertTournamentTeamMembers(members: List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>) = voleiDao.insertTournamentTeamMembers(members)
    suspend fun updateTournamentTeamMembers(members: List<com.bismarck.voleimanager.app.data.model.TournamentTeamMember>) = voleiDao.updateTournamentTeamMembers(members)
    suspend fun deleteTournamentTeamMember(member: com.bismarck.voleimanager.app.data.model.TournamentTeamMember) = voleiDao.deleteTournamentTeamMember(member)

    // --- Tournament Matches ---
    fun tournamentMatchesByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.TournamentMatch>> =
        voleiDao.getTournamentMatchesByGroup(groupName)

    suspend fun getTournamentMatchesByGroupSync(groupName: String) = voleiDao.getTournamentMatchesByGroupSync(groupName)
    suspend fun insertTournamentMatch(match: com.bismarck.voleimanager.app.data.model.TournamentMatch) = voleiDao.insertTournamentMatch(match)
    suspend fun insertTournamentMatches(matches: List<com.bismarck.voleimanager.app.data.model.TournamentMatch>) = voleiDao.insertTournamentMatches(matches)
    suspend fun updateTournamentMatch(match: com.bismarck.voleimanager.app.data.model.TournamentMatch) = voleiDao.updateTournamentMatch(match)
    suspend fun updateTournamentMatches(matches: List<com.bismarck.voleimanager.app.data.model.TournamentMatch>) = voleiDao.updateTournamentMatches(matches)

    // --- Group Logs ---
    fun groupLogsByGroup(groupName: String): Flow<List<com.bismarck.voleimanager.app.data.model.GroupLog>> =
        voleiDao.getGroupLogsByGroup(groupName)

    suspend fun getGroupLogsByGroupSync(groupName: String) = voleiDao.getGroupLogsByGroupSync(groupName)
    suspend fun insertGroupLog(log: com.bismarck.voleimanager.app.data.model.GroupLog) = voleiDao.insertGroupLog(log)
    suspend fun insertGroupLogs(logs: List<com.bismarck.voleimanager.app.data.model.GroupLog>) = voleiDao.insertGroupLogs(logs)
}

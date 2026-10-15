package com.bismarck.voleimanager.app.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _voleiDao: Lazy<VoleiDao> = lazy {
    VoleiDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(17, "b0a1367011eba2f1efbec2c7f49d102c", "abc0fb3aa130e622c41b0c35baf4900e") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `elo` REAL NOT NULL, `matchesPlayed` INTEGER NOT NULL, `victories` INTEGER NOT NULL, `isPriority` INTEGER NOT NULL, `groupName` TEXT NOT NULL, `dailyToll` INTEGER NOT NULL, `tollDate` TEXT NOT NULL, `preferredPosition` TEXT, `secondaryPosition` TEXT, `publicId` TEXT NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_players_groupName_elo` ON `players` (`groupName`, `elo`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_players_publicId` ON `players` (`publicId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `match_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `teamA` TEXT NOT NULL, `teamB` TEXT NOT NULL, `teamAIds` TEXT NOT NULL, `teamBIds` TEXT NOT NULL, `winner` TEXT NOT NULL, `eloPoints` REAL NOT NULL, `groupName` TEXT NOT NULL, `teamAAverageElo` REAL, `teamBAverageElo` REAL, `teamAScore` INTEGER, `teamBScore` INTEGER, `startTimestamp` INTEGER, `endTimestamp` INTEGER, `teamAId` INTEGER, `teamBId` INTEGER, `teamALabel` TEXT, `teamBLabel` TEXT, `matchFormat` TEXT, `setScores` TEXT, `teamASetsWon` INTEGER, `teamBSetsWon` INTEGER)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_match_history_groupName_id` ON `match_history` (`groupName`, `id`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `group_configs` (`groupName` TEXT NOT NULL, `teamSize` INTEGER NOT NULL, `victoryLimit` INTEGER NOT NULL, `priorityEnabled` INTEGER NOT NULL, `scoreEnabled` INTEGER NOT NULL, `balancingMode` TEXT NOT NULL, `onboardingStep` INTEGER NOT NULL, `groupType` TEXT NOT NULL, `tournamentFormat` TEXT, `tournamentStarted` INTEGER NOT NULL, `guaranteeSetter` INTEGER NOT NULL, `matchFormat` TEXT NOT NULL, `regularSetPoints` INTEGER NOT NULL, `tiebreakSetPoints` INTEGER NOT NULL, `winByTwo` INTEGER NOT NULL, `publicId` TEXT NOT NULL, `isCloudSynced` INTEGER NOT NULL, `cloudGroupId` TEXT, `lastPremiumSwitchAt` INTEGER, `teamAColorName` TEXT, `teamBColorName` TEXT, `remoteRole` TEXT, `pendingOwnershipTransferTo` TEXT, `shareHistoryWithObservers` INTEGER NOT NULL, `showEloToObservers` INTEGER NOT NULL, `shareOnlyTodayHistory` INTEGER NOT NULL, `historyBackfilledAt` INTEGER, `activeAdminDeviceId` TEXT, `activeAdminSince` INTEGER, PRIMARY KEY(`groupName`))")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_group_configs_publicId` ON `group_configs` (`publicId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `elo_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `playerId` INTEGER NOT NULL, `playerNameSnapshot` TEXT NOT NULL, `date` TEXT NOT NULL, `elo` REAL NOT NULL, `groupName` TEXT NOT NULL, `won` INTEGER, `matchHistoryId` INTEGER)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_playerId` ON `elo_logs` (`playerId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_groupName_date` ON `elo_logs` (`groupName`, `date`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_groupName_playerId_date` ON `elo_logs` (`groupName`, `playerId`, `date`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tournament_teams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupName` TEXT NOT NULL, `teamKey` TEXT NOT NULL, `customName` TEXT, `seed` INTEGER, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `matchesPlayed` INTEGER NOT NULL, `wins` INTEGER NOT NULL, `losses` INTEGER NOT NULL, `setsWon` INTEGER NOT NULL, `setsLost` INTEGER NOT NULL, `pointsWon` INTEGER NOT NULL, `pointsLost` INTEGER NOT NULL, `tournamentPoints` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tournament_teams_groupName_teamKey` ON `tournament_teams` (`groupName`, `teamKey`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tournament_team_members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupName` TEXT NOT NULL, `teamId` INTEGER NOT NULL, `playerId` INTEGER NOT NULL, `position` TEXT, `isActive` INTEGER NOT NULL, `joinedAt` INTEGER NOT NULL, `leftAt` INTEGER)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_teamId` ON `tournament_team_members` (`groupName`, `teamId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_playerId` ON `tournament_team_members` (`groupName`, `playerId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tournament_matches` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupName` TEXT NOT NULL, `phase` TEXT NOT NULL, `phaseLabel` TEXT, `roundIndex` INTEGER NOT NULL, `orderInRound` INTEGER NOT NULL, `homeTeamId` INTEGER, `awayTeamId` INTEGER, `homeSourceMatchId` INTEGER, `awaySourceMatchId` INTEGER, `homeScore` INTEGER, `awayScore` INTEGER, `winnerTeamId` INTEGER, `status` TEXT NOT NULL, `matchHistoryId` INTEGER, `startTimestamp` INTEGER, `endTimestamp` INTEGER, `matchFormat` TEXT, `homeSetsWon` INTEGER, `awaySetsWon` INTEGER, `setScores` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_matches_groupName_phase_roundIndex_orderInRound` ON `tournament_matches` (`groupName`, `phase`, `roundIndex`, `orderInRound`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `group_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupName` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `date` TEXT NOT NULL, `type` TEXT NOT NULL, `message` TEXT NOT NULL, `playerId` INTEGER, `teamId` INTEGER, `relatedTeamId` INTEGER, `metadata` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_group_logs_groupName_timestamp` ON `group_logs` (`groupName`, `timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b0a1367011eba2f1efbec2c7f49d102c')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `players`")
        connection.execSQL("DROP TABLE IF EXISTS `match_history`")
        connection.execSQL("DROP TABLE IF EXISTS `group_configs`")
        connection.execSQL("DROP TABLE IF EXISTS `elo_logs`")
        connection.execSQL("DROP TABLE IF EXISTS `tournament_teams`")
        connection.execSQL("DROP TABLE IF EXISTS `tournament_team_members`")
        connection.execSQL("DROP TABLE IF EXISTS `tournament_matches`")
        connection.execSQL("DROP TABLE IF EXISTS `group_logs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsPlayers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlayers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("elo", TableInfo.Column("elo", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("matchesPlayed", TableInfo.Column("matchesPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("victories", TableInfo.Column("victories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("isPriority", TableInfo.Column("isPriority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("dailyToll", TableInfo.Column("dailyToll", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("tollDate", TableInfo.Column("tollDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("preferredPosition", TableInfo.Column("preferredPosition", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("secondaryPosition", TableInfo.Column("secondaryPosition", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlayers.put("publicId", TableInfo.Column("publicId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlayers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlayers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPlayers.add(TableInfo.Index("index_players_groupName_elo", false, listOf("groupName", "elo"), listOf("ASC", "ASC")))
        _indicesPlayers.add(TableInfo.Index("index_players_publicId", true, listOf("publicId"), listOf("ASC")))
        val _infoPlayers: TableInfo = TableInfo("players", _columnsPlayers, _foreignKeysPlayers, _indicesPlayers)
        val _existingPlayers: TableInfo = read(connection, "players")
        if (!_infoPlayers.equals(_existingPlayers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |players(com.bismarck.voleimanager.app.data.model.Player).
              | Expected:
              |""".trimMargin() + _infoPlayers + """
              |
              | Found:
              |""".trimMargin() + _existingPlayers)
        }
        val _columnsMatchHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMatchHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamA", TableInfo.Column("teamA", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamB", TableInfo.Column("teamB", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamAIds", TableInfo.Column("teamAIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBIds", TableInfo.Column("teamBIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("winner", TableInfo.Column("winner", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("eloPoints", TableInfo.Column("eloPoints", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamAAverageElo", TableInfo.Column("teamAAverageElo", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBAverageElo", TableInfo.Column("teamBAverageElo", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamAScore", TableInfo.Column("teamAScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBScore", TableInfo.Column("teamBScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("startTimestamp", TableInfo.Column("startTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("endTimestamp", TableInfo.Column("endTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamAId", TableInfo.Column("teamAId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBId", TableInfo.Column("teamBId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamALabel", TableInfo.Column("teamALabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBLabel", TableInfo.Column("teamBLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("matchFormat", TableInfo.Column("matchFormat", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("setScores", TableInfo.Column("setScores", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamASetsWon", TableInfo.Column("teamASetsWon", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMatchHistory.put("teamBSetsWon", TableInfo.Column("teamBSetsWon", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMatchHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMatchHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMatchHistory.add(TableInfo.Index("index_match_history_groupName_id", false, listOf("groupName", "id"), listOf("ASC", "ASC")))
        val _infoMatchHistory: TableInfo = TableInfo("match_history", _columnsMatchHistory, _foreignKeysMatchHistory, _indicesMatchHistory)
        val _existingMatchHistory: TableInfo = read(connection, "match_history")
        if (!_infoMatchHistory.equals(_existingMatchHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |match_history(com.bismarck.voleimanager.app.data.model.MatchHistory).
              | Expected:
              |""".trimMargin() + _infoMatchHistory + """
              |
              | Found:
              |""".trimMargin() + _existingMatchHistory)
        }
        val _columnsGroupConfigs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGroupConfigs.put("groupName", TableInfo.Column("groupName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("teamSize", TableInfo.Column("teamSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("victoryLimit", TableInfo.Column("victoryLimit", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("priorityEnabled", TableInfo.Column("priorityEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("scoreEnabled", TableInfo.Column("scoreEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("balancingMode", TableInfo.Column("balancingMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("onboardingStep", TableInfo.Column("onboardingStep", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("groupType", TableInfo.Column("groupType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("tournamentFormat", TableInfo.Column("tournamentFormat", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("tournamentStarted", TableInfo.Column("tournamentStarted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("guaranteeSetter", TableInfo.Column("guaranteeSetter", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("matchFormat", TableInfo.Column("matchFormat", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("regularSetPoints", TableInfo.Column("regularSetPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("tiebreakSetPoints", TableInfo.Column("tiebreakSetPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("winByTwo", TableInfo.Column("winByTwo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("publicId", TableInfo.Column("publicId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("isCloudSynced", TableInfo.Column("isCloudSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("cloudGroupId", TableInfo.Column("cloudGroupId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("lastPremiumSwitchAt", TableInfo.Column("lastPremiumSwitchAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("teamAColorName", TableInfo.Column("teamAColorName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("teamBColorName", TableInfo.Column("teamBColorName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("remoteRole", TableInfo.Column("remoteRole", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("pendingOwnershipTransferTo", TableInfo.Column("pendingOwnershipTransferTo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("shareHistoryWithObservers", TableInfo.Column("shareHistoryWithObservers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("showEloToObservers", TableInfo.Column("showEloToObservers", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("shareOnlyTodayHistory", TableInfo.Column("shareOnlyTodayHistory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("historyBackfilledAt", TableInfo.Column("historyBackfilledAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("activeAdminDeviceId", TableInfo.Column("activeAdminDeviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupConfigs.put("activeAdminSince", TableInfo.Column("activeAdminSince", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGroupConfigs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGroupConfigs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGroupConfigs.add(TableInfo.Index("index_group_configs_publicId", true, listOf("publicId"), listOf("ASC")))
        val _infoGroupConfigs: TableInfo = TableInfo("group_configs", _columnsGroupConfigs, _foreignKeysGroupConfigs, _indicesGroupConfigs)
        val _existingGroupConfigs: TableInfo = read(connection, "group_configs")
        if (!_infoGroupConfigs.equals(_existingGroupConfigs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |group_configs(com.bismarck.voleimanager.app.data.model.GroupConfig).
              | Expected:
              |""".trimMargin() + _infoGroupConfigs + """
              |
              | Found:
              |""".trimMargin() + _existingGroupConfigs)
        }
        val _columnsEloLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEloLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("playerId", TableInfo.Column("playerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("playerNameSnapshot", TableInfo.Column("playerNameSnapshot", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("elo", TableInfo.Column("elo", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("won", TableInfo.Column("won", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEloLogs.put("matchHistoryId", TableInfo.Column("matchHistoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEloLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEloLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesEloLogs.add(TableInfo.Index("index_elo_logs_playerId", false, listOf("playerId"), listOf("ASC")))
        _indicesEloLogs.add(TableInfo.Index("index_elo_logs_groupName_date", false, listOf("groupName", "date"), listOf("ASC", "ASC")))
        _indicesEloLogs.add(TableInfo.Index("index_elo_logs_groupName_playerId_date", false, listOf("groupName", "playerId", "date"), listOf("ASC", "ASC", "ASC")))
        val _infoEloLogs: TableInfo = TableInfo("elo_logs", _columnsEloLogs, _foreignKeysEloLogs, _indicesEloLogs)
        val _existingEloLogs: TableInfo = read(connection, "elo_logs")
        if (!_infoEloLogs.equals(_existingEloLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |elo_logs(com.bismarck.voleimanager.app.data.model.PlayerEloLog).
              | Expected:
              |""".trimMargin() + _infoEloLogs + """
              |
              | Found:
              |""".trimMargin() + _existingEloLogs)
        }
        val _columnsTournamentTeams: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTournamentTeams.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("teamKey", TableInfo.Column("teamKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("customName", TableInfo.Column("customName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("seed", TableInfo.Column("seed", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("matchesPlayed", TableInfo.Column("matchesPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("wins", TableInfo.Column("wins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("losses", TableInfo.Column("losses", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("setsWon", TableInfo.Column("setsWon", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("setsLost", TableInfo.Column("setsLost", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("pointsWon", TableInfo.Column("pointsWon", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("pointsLost", TableInfo.Column("pointsLost", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeams.put("tournamentPoints", TableInfo.Column("tournamentPoints", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTournamentTeams: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTournamentTeams: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTournamentTeams.add(TableInfo.Index("index_tournament_teams_groupName_teamKey", true, listOf("groupName", "teamKey"), listOf("ASC", "ASC")))
        val _infoTournamentTeams: TableInfo = TableInfo("tournament_teams", _columnsTournamentTeams, _foreignKeysTournamentTeams, _indicesTournamentTeams)
        val _existingTournamentTeams: TableInfo = read(connection, "tournament_teams")
        if (!_infoTournamentTeams.equals(_existingTournamentTeams)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tournament_teams(com.bismarck.voleimanager.app.data.model.TournamentTeam).
              | Expected:
              |""".trimMargin() + _infoTournamentTeams + """
              |
              | Found:
              |""".trimMargin() + _existingTournamentTeams)
        }
        val _columnsTournamentTeamMembers: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTournamentTeamMembers.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("teamId", TableInfo.Column("teamId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("playerId", TableInfo.Column("playerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("position", TableInfo.Column("position", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("joinedAt", TableInfo.Column("joinedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentTeamMembers.put("leftAt", TableInfo.Column("leftAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTournamentTeamMembers: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTournamentTeamMembers: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTournamentTeamMembers.add(TableInfo.Index("index_tournament_team_members_groupName_teamId", false, listOf("groupName", "teamId"), listOf("ASC", "ASC")))
        _indicesTournamentTeamMembers.add(TableInfo.Index("index_tournament_team_members_groupName_playerId", false, listOf("groupName", "playerId"), listOf("ASC", "ASC")))
        val _infoTournamentTeamMembers: TableInfo = TableInfo("tournament_team_members", _columnsTournamentTeamMembers, _foreignKeysTournamentTeamMembers, _indicesTournamentTeamMembers)
        val _existingTournamentTeamMembers: TableInfo = read(connection, "tournament_team_members")
        if (!_infoTournamentTeamMembers.equals(_existingTournamentTeamMembers)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tournament_team_members(com.bismarck.voleimanager.app.data.model.TournamentTeamMember).
              | Expected:
              |""".trimMargin() + _infoTournamentTeamMembers + """
              |
              | Found:
              |""".trimMargin() + _existingTournamentTeamMembers)
        }
        val _columnsTournamentMatches: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTournamentMatches.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("phase", TableInfo.Column("phase", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("phaseLabel", TableInfo.Column("phaseLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("roundIndex", TableInfo.Column("roundIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("orderInRound", TableInfo.Column("orderInRound", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("homeTeamId", TableInfo.Column("homeTeamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("awayTeamId", TableInfo.Column("awayTeamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("homeSourceMatchId", TableInfo.Column("homeSourceMatchId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("awaySourceMatchId", TableInfo.Column("awaySourceMatchId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("homeScore", TableInfo.Column("homeScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("awayScore", TableInfo.Column("awayScore", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("winnerTeamId", TableInfo.Column("winnerTeamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("matchHistoryId", TableInfo.Column("matchHistoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("startTimestamp", TableInfo.Column("startTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("endTimestamp", TableInfo.Column("endTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("matchFormat", TableInfo.Column("matchFormat", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("homeSetsWon", TableInfo.Column("homeSetsWon", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("awaySetsWon", TableInfo.Column("awaySetsWon", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTournamentMatches.put("setScores", TableInfo.Column("setScores", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTournamentMatches: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTournamentMatches: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTournamentMatches.add(TableInfo.Index("index_tournament_matches_groupName_phase_roundIndex_orderInRound", false, listOf("groupName", "phase", "roundIndex", "orderInRound"), listOf("ASC", "ASC", "ASC", "ASC")))
        val _infoTournamentMatches: TableInfo = TableInfo("tournament_matches", _columnsTournamentMatches, _foreignKeysTournamentMatches, _indicesTournamentMatches)
        val _existingTournamentMatches: TableInfo = read(connection, "tournament_matches")
        if (!_infoTournamentMatches.equals(_existingTournamentMatches)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tournament_matches(com.bismarck.voleimanager.app.data.model.TournamentMatch).
              | Expected:
              |""".trimMargin() + _infoTournamentMatches + """
              |
              | Found:
              |""".trimMargin() + _existingTournamentMatches)
        }
        val _columnsGroupLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGroupLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("groupName", TableInfo.Column("groupName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("message", TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("playerId", TableInfo.Column("playerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("teamId", TableInfo.Column("teamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("relatedTeamId", TableInfo.Column("relatedTeamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGroupLogs.put("metadata", TableInfo.Column("metadata", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGroupLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGroupLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGroupLogs.add(TableInfo.Index("index_group_logs_groupName_timestamp", false, listOf("groupName", "timestamp"), listOf("ASC", "ASC")))
        val _infoGroupLogs: TableInfo = TableInfo("group_logs", _columnsGroupLogs, _foreignKeysGroupLogs, _indicesGroupLogs)
        val _existingGroupLogs: TableInfo = read(connection, "group_logs")
        if (!_infoGroupLogs.equals(_existingGroupLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |group_logs(com.bismarck.voleimanager.app.data.model.GroupLog).
              | Expected:
              |""".trimMargin() + _infoGroupLogs + """
              |
              | Found:
              |""".trimMargin() + _existingGroupLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "players", "match_history", "group_configs", "elo_logs", "tournament_teams", "tournament_team_members", "tournament_matches", "group_logs")
  }

  public override fun clearAllTables() {
    super.performClear(false, "players", "match_history", "group_configs", "elo_logs", "tournament_teams", "tournament_team_members", "tournament_matches", "group_logs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(VoleiDao::class, VoleiDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun voleiDao(): VoleiDao = _voleiDao.value
}

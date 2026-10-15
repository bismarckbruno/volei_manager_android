package com.bismarck.voleimanager.app.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.bismarck.voleimanager.app.`data`.model.GroupConfig
import com.bismarck.voleimanager.app.`data`.model.GroupLog
import com.bismarck.voleimanager.app.`data`.model.MatchHistory
import com.bismarck.voleimanager.app.`data`.model.Player
import com.bismarck.voleimanager.app.`data`.model.PlayerEloLog
import com.bismarck.voleimanager.app.`data`.model.TournamentMatch
import com.bismarck.voleimanager.app.`data`.model.TournamentTeam
import com.bismarck.voleimanager.app.`data`.model.TournamentTeamMember
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class VoleiDao_Impl(
  __db: RoomDatabase,
) : VoleiDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlayer: EntityInsertAdapter<Player>

  private val __insertAdapterOfPlayer_1: EntityInsertAdapter<Player>

  private val __insertAdapterOfMatchHistory: EntityInsertAdapter<MatchHistory>

  private val __insertAdapterOfMatchHistory_1: EntityInsertAdapter<MatchHistory>

  private val __insertAdapterOfPlayerEloLog: EntityInsertAdapter<PlayerEloLog>

  private val __insertAdapterOfPlayerEloLog_1: EntityInsertAdapter<PlayerEloLog>

  private val __insertAdapterOfGroupConfig: EntityInsertAdapter<GroupConfig>

  private val __insertAdapterOfTournamentTeam: EntityInsertAdapter<TournamentTeam>

  private val __insertAdapterOfTournamentTeamMember: EntityInsertAdapter<TournamentTeamMember>

  private val __insertAdapterOfTournamentMatch: EntityInsertAdapter<TournamentMatch>

  private val __insertAdapterOfGroupLog: EntityInsertAdapter<GroupLog>

  private val __insertAdapterOfGroupLog_1: EntityInsertAdapter<GroupLog>

  private val __deleteAdapterOfPlayer: EntityDeleteOrUpdateAdapter<Player>

  private val __deleteAdapterOfMatchHistory: EntityDeleteOrUpdateAdapter<MatchHistory>

  private val __deleteAdapterOfTournamentTeam: EntityDeleteOrUpdateAdapter<TournamentTeam>

  private val __deleteAdapterOfTournamentTeamMember:
      EntityDeleteOrUpdateAdapter<TournamentTeamMember>

  private val __updateAdapterOfPlayer: EntityDeleteOrUpdateAdapter<Player>

  private val __updateAdapterOfMatchHistory: EntityDeleteOrUpdateAdapter<MatchHistory>

  private val __updateAdapterOfPlayerEloLog: EntityDeleteOrUpdateAdapter<PlayerEloLog>

  private val __updateAdapterOfTournamentTeam: EntityDeleteOrUpdateAdapter<TournamentTeam>

  private val __updateAdapterOfTournamentTeamMember:
      EntityDeleteOrUpdateAdapter<TournamentTeamMember>

  private val __updateAdapterOfTournamentMatch: EntityDeleteOrUpdateAdapter<TournamentMatch>
  init {
    this.__db = __db
    this.__insertAdapterOfPlayer = object : EntityInsertAdapter<Player>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `players` (`id`,`name`,`elo`,`matchesPlayed`,`victories`,`isPriority`,`groupName`,`dailyToll`,`tollDate`,`preferredPosition`,`secondaryPosition`,`publicId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Player) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.elo)
        statement.bindLong(4, entity.matchesPlayed.toLong())
        statement.bindLong(5, entity.victories.toLong())
        val _tmp: Int = if (entity.isPriority) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.groupName)
        statement.bindLong(8, entity.dailyToll.toLong())
        statement.bindText(9, entity.tollDate)
        val _tmpPreferredPosition: String? = entity.preferredPosition
        if (_tmpPreferredPosition == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpPreferredPosition)
        }
        val _tmpSecondaryPosition: String? = entity.secondaryPosition
        if (_tmpSecondaryPosition == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpSecondaryPosition)
        }
        statement.bindText(12, entity.publicId)
      }
    }
    this.__insertAdapterOfPlayer_1 = object : EntityInsertAdapter<Player>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `players` (`id`,`name`,`elo`,`matchesPlayed`,`victories`,`isPriority`,`groupName`,`dailyToll`,`tollDate`,`preferredPosition`,`secondaryPosition`,`publicId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Player) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.elo)
        statement.bindLong(4, entity.matchesPlayed.toLong())
        statement.bindLong(5, entity.victories.toLong())
        val _tmp: Int = if (entity.isPriority) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.groupName)
        statement.bindLong(8, entity.dailyToll.toLong())
        statement.bindText(9, entity.tollDate)
        val _tmpPreferredPosition: String? = entity.preferredPosition
        if (_tmpPreferredPosition == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpPreferredPosition)
        }
        val _tmpSecondaryPosition: String? = entity.secondaryPosition
        if (_tmpSecondaryPosition == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpSecondaryPosition)
        }
        statement.bindText(12, entity.publicId)
      }
    }
    this.__insertAdapterOfMatchHistory = object : EntityInsertAdapter<MatchHistory>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `match_history` (`id`,`date`,`teamA`,`teamB`,`teamAIds`,`teamBIds`,`winner`,`eloPoints`,`groupName`,`teamAAverageElo`,`teamBAverageElo`,`teamAScore`,`teamBScore`,`startTimestamp`,`endTimestamp`,`teamAId`,`teamBId`,`teamALabel`,`teamBLabel`,`matchFormat`,`setScores`,`teamASetsWon`,`teamBSetsWon`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MatchHistory) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.teamA)
        statement.bindText(4, entity.teamB)
        statement.bindText(5, entity.teamAIds)
        statement.bindText(6, entity.teamBIds)
        statement.bindText(7, entity.winner)
        statement.bindDouble(8, entity.eloPoints)
        statement.bindText(9, entity.groupName)
        val _tmpTeamAAverageElo: Double? = entity.teamAAverageElo
        if (_tmpTeamAAverageElo == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpTeamAAverageElo)
        }
        val _tmpTeamBAverageElo: Double? = entity.teamBAverageElo
        if (_tmpTeamBAverageElo == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpTeamBAverageElo)
        }
        val _tmpTeamAScore: Int? = entity.teamAScore
        if (_tmpTeamAScore == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTeamAScore.toLong())
        }
        val _tmpTeamBScore: Int? = entity.teamBScore
        if (_tmpTeamBScore == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpTeamBScore.toLong())
        }
        val _tmpStartTimestamp: Long? = entity.startTimestamp
        if (_tmpStartTimestamp == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpStartTimestamp)
        }
        val _tmpEndTimestamp: Long? = entity.endTimestamp
        if (_tmpEndTimestamp == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpEndTimestamp)
        }
        val _tmpTeamAId: Int? = entity.teamAId
        if (_tmpTeamAId == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpTeamAId.toLong())
        }
        val _tmpTeamBId: Int? = entity.teamBId
        if (_tmpTeamBId == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpTeamBId.toLong())
        }
        val _tmpTeamALabel: String? = entity.teamALabel
        if (_tmpTeamALabel == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpTeamALabel)
        }
        val _tmpTeamBLabel: String? = entity.teamBLabel
        if (_tmpTeamBLabel == null) {
          statement.bindNull(19)
        } else {
          statement.bindText(19, _tmpTeamBLabel)
        }
        val _tmpMatchFormat: String? = entity.matchFormat
        if (_tmpMatchFormat == null) {
          statement.bindNull(20)
        } else {
          statement.bindText(20, _tmpMatchFormat)
        }
        val _tmpSetScores: String? = entity.setScores
        if (_tmpSetScores == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpSetScores)
        }
        val _tmpTeamASetsWon: Int? = entity.teamASetsWon
        if (_tmpTeamASetsWon == null) {
          statement.bindNull(22)
        } else {
          statement.bindLong(22, _tmpTeamASetsWon.toLong())
        }
        val _tmpTeamBSetsWon: Int? = entity.teamBSetsWon
        if (_tmpTeamBSetsWon == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpTeamBSetsWon.toLong())
        }
      }
    }
    this.__insertAdapterOfMatchHistory_1 = object : EntityInsertAdapter<MatchHistory>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `match_history` (`id`,`date`,`teamA`,`teamB`,`teamAIds`,`teamBIds`,`winner`,`eloPoints`,`groupName`,`teamAAverageElo`,`teamBAverageElo`,`teamAScore`,`teamBScore`,`startTimestamp`,`endTimestamp`,`teamAId`,`teamBId`,`teamALabel`,`teamBLabel`,`matchFormat`,`setScores`,`teamASetsWon`,`teamBSetsWon`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MatchHistory) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.teamA)
        statement.bindText(4, entity.teamB)
        statement.bindText(5, entity.teamAIds)
        statement.bindText(6, entity.teamBIds)
        statement.bindText(7, entity.winner)
        statement.bindDouble(8, entity.eloPoints)
        statement.bindText(9, entity.groupName)
        val _tmpTeamAAverageElo: Double? = entity.teamAAverageElo
        if (_tmpTeamAAverageElo == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpTeamAAverageElo)
        }
        val _tmpTeamBAverageElo: Double? = entity.teamBAverageElo
        if (_tmpTeamBAverageElo == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpTeamBAverageElo)
        }
        val _tmpTeamAScore: Int? = entity.teamAScore
        if (_tmpTeamAScore == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTeamAScore.toLong())
        }
        val _tmpTeamBScore: Int? = entity.teamBScore
        if (_tmpTeamBScore == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpTeamBScore.toLong())
        }
        val _tmpStartTimestamp: Long? = entity.startTimestamp
        if (_tmpStartTimestamp == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpStartTimestamp)
        }
        val _tmpEndTimestamp: Long? = entity.endTimestamp
        if (_tmpEndTimestamp == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpEndTimestamp)
        }
        val _tmpTeamAId: Int? = entity.teamAId
        if (_tmpTeamAId == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpTeamAId.toLong())
        }
        val _tmpTeamBId: Int? = entity.teamBId
        if (_tmpTeamBId == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpTeamBId.toLong())
        }
        val _tmpTeamALabel: String? = entity.teamALabel
        if (_tmpTeamALabel == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpTeamALabel)
        }
        val _tmpTeamBLabel: String? = entity.teamBLabel
        if (_tmpTeamBLabel == null) {
          statement.bindNull(19)
        } else {
          statement.bindText(19, _tmpTeamBLabel)
        }
        val _tmpMatchFormat: String? = entity.matchFormat
        if (_tmpMatchFormat == null) {
          statement.bindNull(20)
        } else {
          statement.bindText(20, _tmpMatchFormat)
        }
        val _tmpSetScores: String? = entity.setScores
        if (_tmpSetScores == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpSetScores)
        }
        val _tmpTeamASetsWon: Int? = entity.teamASetsWon
        if (_tmpTeamASetsWon == null) {
          statement.bindNull(22)
        } else {
          statement.bindLong(22, _tmpTeamASetsWon.toLong())
        }
        val _tmpTeamBSetsWon: Int? = entity.teamBSetsWon
        if (_tmpTeamBSetsWon == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpTeamBSetsWon.toLong())
        }
      }
    }
    this.__insertAdapterOfPlayerEloLog = object : EntityInsertAdapter<PlayerEloLog>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `elo_logs` (`id`,`playerId`,`playerNameSnapshot`,`date`,`elo`,`groupName`,`won`,`matchHistoryId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEloLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.playerId.toLong())
        statement.bindText(3, entity.playerNameSnapshot)
        statement.bindText(4, entity.date)
        statement.bindDouble(5, entity.elo)
        statement.bindText(6, entity.groupName)
        val _tmpWon: Boolean? = entity.won
        val _tmp: Int? = _tmpWon?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp.toLong())
        }
        val _tmpMatchHistoryId: Int? = entity.matchHistoryId
        if (_tmpMatchHistoryId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpMatchHistoryId.toLong())
        }
      }
    }
    this.__insertAdapterOfPlayerEloLog_1 = object : EntityInsertAdapter<PlayerEloLog>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `elo_logs` (`id`,`playerId`,`playerNameSnapshot`,`date`,`elo`,`groupName`,`won`,`matchHistoryId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEloLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.playerId.toLong())
        statement.bindText(3, entity.playerNameSnapshot)
        statement.bindText(4, entity.date)
        statement.bindDouble(5, entity.elo)
        statement.bindText(6, entity.groupName)
        val _tmpWon: Boolean? = entity.won
        val _tmp: Int? = _tmpWon?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp.toLong())
        }
        val _tmpMatchHistoryId: Int? = entity.matchHistoryId
        if (_tmpMatchHistoryId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpMatchHistoryId.toLong())
        }
      }
    }
    this.__insertAdapterOfGroupConfig = object : EntityInsertAdapter<GroupConfig>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `group_configs` (`groupName`,`teamSize`,`victoryLimit`,`priorityEnabled`,`scoreEnabled`,`balancingMode`,`onboardingStep`,`groupType`,`tournamentFormat`,`tournamentStarted`,`guaranteeSetter`,`matchFormat`,`regularSetPoints`,`tiebreakSetPoints`,`winByTwo`,`publicId`,`isCloudSynced`,`cloudGroupId`,`lastPremiumSwitchAt`,`teamAColorName`,`teamBColorName`,`remoteRole`,`pendingOwnershipTransferTo`,`shareHistoryWithObservers`,`showEloToObservers`,`shareOnlyTodayHistory`,`historyBackfilledAt`,`activeAdminDeviceId`,`activeAdminSince`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GroupConfig) {
        statement.bindText(1, entity.groupName)
        statement.bindLong(2, entity.teamSize.toLong())
        statement.bindLong(3, entity.victoryLimit.toLong())
        val _tmp: Int = if (entity.priorityEnabled) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.scoreEnabled) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindText(6, entity.balancingMode)
        statement.bindLong(7, entity.onboardingStep.toLong())
        statement.bindText(8, entity.groupType)
        val _tmpTournamentFormat: String? = entity.tournamentFormat
        if (_tmpTournamentFormat == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpTournamentFormat)
        }
        val _tmp_2: Int = if (entity.tournamentStarted) 1 else 0
        statement.bindLong(10, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.guaranteeSetter) 1 else 0
        statement.bindLong(11, _tmp_3.toLong())
        statement.bindText(12, entity.matchFormat)
        statement.bindLong(13, entity.regularSetPoints.toLong())
        statement.bindLong(14, entity.tiebreakSetPoints.toLong())
        val _tmp_4: Int = if (entity.winByTwo) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
        statement.bindText(16, entity.publicId)
        val _tmp_5: Int = if (entity.isCloudSynced) 1 else 0
        statement.bindLong(17, _tmp_5.toLong())
        val _tmpCloudGroupId: String? = entity.cloudGroupId
        if (_tmpCloudGroupId == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpCloudGroupId)
        }
        val _tmpLastPremiumSwitchAt: Long? = entity.lastPremiumSwitchAt
        if (_tmpLastPremiumSwitchAt == null) {
          statement.bindNull(19)
        } else {
          statement.bindLong(19, _tmpLastPremiumSwitchAt)
        }
        val _tmpTeamAColorName: String? = entity.teamAColorName
        if (_tmpTeamAColorName == null) {
          statement.bindNull(20)
        } else {
          statement.bindText(20, _tmpTeamAColorName)
        }
        val _tmpTeamBColorName: String? = entity.teamBColorName
        if (_tmpTeamBColorName == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpTeamBColorName)
        }
        val _tmpRemoteRole: String? = entity.remoteRole
        if (_tmpRemoteRole == null) {
          statement.bindNull(22)
        } else {
          statement.bindText(22, _tmpRemoteRole)
        }
        val _tmpPendingOwnershipTransferTo: String? = entity.pendingOwnershipTransferTo
        if (_tmpPendingOwnershipTransferTo == null) {
          statement.bindNull(23)
        } else {
          statement.bindText(23, _tmpPendingOwnershipTransferTo)
        }
        val _tmp_6: Int = if (entity.shareHistoryWithObservers) 1 else 0
        statement.bindLong(24, _tmp_6.toLong())
        val _tmp_7: Int = if (entity.showEloToObservers) 1 else 0
        statement.bindLong(25, _tmp_7.toLong())
        val _tmp_8: Int = if (entity.shareOnlyTodayHistory) 1 else 0
        statement.bindLong(26, _tmp_8.toLong())
        val _tmpHistoryBackfilledAt: Long? = entity.historyBackfilledAt
        if (_tmpHistoryBackfilledAt == null) {
          statement.bindNull(27)
        } else {
          statement.bindLong(27, _tmpHistoryBackfilledAt)
        }
        val _tmpActiveAdminDeviceId: String? = entity.activeAdminDeviceId
        if (_tmpActiveAdminDeviceId == null) {
          statement.bindNull(28)
        } else {
          statement.bindText(28, _tmpActiveAdminDeviceId)
        }
        val _tmpActiveAdminSince: Long? = entity.activeAdminSince
        if (_tmpActiveAdminSince == null) {
          statement.bindNull(29)
        } else {
          statement.bindLong(29, _tmpActiveAdminSince)
        }
      }
    }
    this.__insertAdapterOfTournamentTeam = object : EntityInsertAdapter<TournamentTeam>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `tournament_teams` (`id`,`groupName`,`teamKey`,`customName`,`seed`,`isActive`,`createdAt`,`matchesPlayed`,`wins`,`losses`,`setsWon`,`setsLost`,`pointsWon`,`pointsLost`,`tournamentPoints`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeam) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindText(3, entity.teamKey)
        val _tmpCustomName: String? = entity.customName
        if (_tmpCustomName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCustomName)
        }
        val _tmpSeed: Int? = entity.seed
        if (_tmpSeed == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpSeed.toLong())
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.createdAt)
        statement.bindLong(8, entity.matchesPlayed.toLong())
        statement.bindLong(9, entity.wins.toLong())
        statement.bindLong(10, entity.losses.toLong())
        statement.bindLong(11, entity.setsWon.toLong())
        statement.bindLong(12, entity.setsLost.toLong())
        statement.bindLong(13, entity.pointsWon.toLong())
        statement.bindLong(14, entity.pointsLost.toLong())
        statement.bindLong(15, entity.tournamentPoints.toLong())
      }
    }
    this.__insertAdapterOfTournamentTeamMember = object : EntityInsertAdapter<TournamentTeamMember>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `tournament_team_members` (`id`,`groupName`,`teamId`,`playerId`,`position`,`isActive`,`joinedAt`,`leftAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeamMember) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindLong(3, entity.teamId.toLong())
        statement.bindLong(4, entity.playerId.toLong())
        val _tmpPosition: String? = entity.position
        if (_tmpPosition == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpPosition)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.joinedAt)
        val _tmpLeftAt: Long? = entity.leftAt
        if (_tmpLeftAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpLeftAt)
        }
      }
    }
    this.__insertAdapterOfTournamentMatch = object : EntityInsertAdapter<TournamentMatch>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `tournament_matches` (`id`,`groupName`,`phase`,`phaseLabel`,`roundIndex`,`orderInRound`,`homeTeamId`,`awayTeamId`,`homeSourceMatchId`,`awaySourceMatchId`,`homeScore`,`awayScore`,`winnerTeamId`,`status`,`matchHistoryId`,`startTimestamp`,`endTimestamp`,`matchFormat`,`homeSetsWon`,`awaySetsWon`,`setScores`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentMatch) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindText(3, entity.phase)
        val _tmpPhaseLabel: String? = entity.phaseLabel
        if (_tmpPhaseLabel == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpPhaseLabel)
        }
        statement.bindLong(5, entity.roundIndex.toLong())
        statement.bindLong(6, entity.orderInRound.toLong())
        val _tmpHomeTeamId: Int? = entity.homeTeamId
        if (_tmpHomeTeamId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpHomeTeamId.toLong())
        }
        val _tmpAwayTeamId: Int? = entity.awayTeamId
        if (_tmpAwayTeamId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpAwayTeamId.toLong())
        }
        val _tmpHomeSourceMatchId: Int? = entity.homeSourceMatchId
        if (_tmpHomeSourceMatchId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpHomeSourceMatchId.toLong())
        }
        val _tmpAwaySourceMatchId: Int? = entity.awaySourceMatchId
        if (_tmpAwaySourceMatchId == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAwaySourceMatchId.toLong())
        }
        val _tmpHomeScore: Int? = entity.homeScore
        if (_tmpHomeScore == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpHomeScore.toLong())
        }
        val _tmpAwayScore: Int? = entity.awayScore
        if (_tmpAwayScore == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpAwayScore.toLong())
        }
        val _tmpWinnerTeamId: Int? = entity.winnerTeamId
        if (_tmpWinnerTeamId == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpWinnerTeamId.toLong())
        }
        statement.bindText(14, entity.status)
        val _tmpMatchHistoryId: Int? = entity.matchHistoryId
        if (_tmpMatchHistoryId == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpMatchHistoryId.toLong())
        }
        val _tmpStartTimestamp: Long? = entity.startTimestamp
        if (_tmpStartTimestamp == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpStartTimestamp)
        }
        val _tmpEndTimestamp: Long? = entity.endTimestamp
        if (_tmpEndTimestamp == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpEndTimestamp)
        }
        val _tmpMatchFormat: String? = entity.matchFormat
        if (_tmpMatchFormat == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpMatchFormat)
        }
        val _tmpHomeSetsWon: Int? = entity.homeSetsWon
        if (_tmpHomeSetsWon == null) {
          statement.bindNull(19)
        } else {
          statement.bindLong(19, _tmpHomeSetsWon.toLong())
        }
        val _tmpAwaySetsWon: Int? = entity.awaySetsWon
        if (_tmpAwaySetsWon == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpAwaySetsWon.toLong())
        }
        val _tmpSetScores: String? = entity.setScores
        if (_tmpSetScores == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpSetScores)
        }
      }
    }
    this.__insertAdapterOfGroupLog = object : EntityInsertAdapter<GroupLog>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `group_logs` (`id`,`groupName`,`timestamp`,`date`,`type`,`message`,`playerId`,`teamId`,`relatedTeamId`,`metadata`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GroupLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.date)
        statement.bindText(5, entity.type)
        statement.bindText(6, entity.message)
        val _tmpPlayerId: Int? = entity.playerId
        if (_tmpPlayerId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpPlayerId.toLong())
        }
        val _tmpTeamId: Int? = entity.teamId
        if (_tmpTeamId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpTeamId.toLong())
        }
        val _tmpRelatedTeamId: Int? = entity.relatedTeamId
        if (_tmpRelatedTeamId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpRelatedTeamId.toLong())
        }
        val _tmpMetadata: String? = entity.metadata
        if (_tmpMetadata == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpMetadata)
        }
      }
    }
    this.__insertAdapterOfGroupLog_1 = object : EntityInsertAdapter<GroupLog>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `group_logs` (`id`,`groupName`,`timestamp`,`date`,`type`,`message`,`playerId`,`teamId`,`relatedTeamId`,`metadata`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GroupLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.date)
        statement.bindText(5, entity.type)
        statement.bindText(6, entity.message)
        val _tmpPlayerId: Int? = entity.playerId
        if (_tmpPlayerId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpPlayerId.toLong())
        }
        val _tmpTeamId: Int? = entity.teamId
        if (_tmpTeamId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpTeamId.toLong())
        }
        val _tmpRelatedTeamId: Int? = entity.relatedTeamId
        if (_tmpRelatedTeamId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpRelatedTeamId.toLong())
        }
        val _tmpMetadata: String? = entity.metadata
        if (_tmpMetadata == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpMetadata)
        }
      }
    }
    this.__deleteAdapterOfPlayer = object : EntityDeleteOrUpdateAdapter<Player>() {
      protected override fun createQuery(): String = "DELETE FROM `players` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Player) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__deleteAdapterOfMatchHistory = object : EntityDeleteOrUpdateAdapter<MatchHistory>() {
      protected override fun createQuery(): String = "DELETE FROM `match_history` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MatchHistory) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__deleteAdapterOfTournamentTeam = object : EntityDeleteOrUpdateAdapter<TournamentTeam>() {
      protected override fun createQuery(): String = "DELETE FROM `tournament_teams` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeam) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__deleteAdapterOfTournamentTeamMember = object : EntityDeleteOrUpdateAdapter<TournamentTeamMember>() {
      protected override fun createQuery(): String = "DELETE FROM `tournament_team_members` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeamMember) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfPlayer = object : EntityDeleteOrUpdateAdapter<Player>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `players` SET `id` = ?,`name` = ?,`elo` = ?,`matchesPlayed` = ?,`victories` = ?,`isPriority` = ?,`groupName` = ?,`dailyToll` = ?,`tollDate` = ?,`preferredPosition` = ?,`secondaryPosition` = ?,`publicId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Player) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.elo)
        statement.bindLong(4, entity.matchesPlayed.toLong())
        statement.bindLong(5, entity.victories.toLong())
        val _tmp: Int = if (entity.isPriority) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.groupName)
        statement.bindLong(8, entity.dailyToll.toLong())
        statement.bindText(9, entity.tollDate)
        val _tmpPreferredPosition: String? = entity.preferredPosition
        if (_tmpPreferredPosition == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpPreferredPosition)
        }
        val _tmpSecondaryPosition: String? = entity.secondaryPosition
        if (_tmpSecondaryPosition == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpSecondaryPosition)
        }
        statement.bindText(12, entity.publicId)
        statement.bindLong(13, entity.id.toLong())
      }
    }
    this.__updateAdapterOfMatchHistory = object : EntityDeleteOrUpdateAdapter<MatchHistory>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `match_history` SET `id` = ?,`date` = ?,`teamA` = ?,`teamB` = ?,`teamAIds` = ?,`teamBIds` = ?,`winner` = ?,`eloPoints` = ?,`groupName` = ?,`teamAAverageElo` = ?,`teamBAverageElo` = ?,`teamAScore` = ?,`teamBScore` = ?,`startTimestamp` = ?,`endTimestamp` = ?,`teamAId` = ?,`teamBId` = ?,`teamALabel` = ?,`teamBLabel` = ?,`matchFormat` = ?,`setScores` = ?,`teamASetsWon` = ?,`teamBSetsWon` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MatchHistory) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.date)
        statement.bindText(3, entity.teamA)
        statement.bindText(4, entity.teamB)
        statement.bindText(5, entity.teamAIds)
        statement.bindText(6, entity.teamBIds)
        statement.bindText(7, entity.winner)
        statement.bindDouble(8, entity.eloPoints)
        statement.bindText(9, entity.groupName)
        val _tmpTeamAAverageElo: Double? = entity.teamAAverageElo
        if (_tmpTeamAAverageElo == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpTeamAAverageElo)
        }
        val _tmpTeamBAverageElo: Double? = entity.teamBAverageElo
        if (_tmpTeamBAverageElo == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpTeamBAverageElo)
        }
        val _tmpTeamAScore: Int? = entity.teamAScore
        if (_tmpTeamAScore == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTeamAScore.toLong())
        }
        val _tmpTeamBScore: Int? = entity.teamBScore
        if (_tmpTeamBScore == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpTeamBScore.toLong())
        }
        val _tmpStartTimestamp: Long? = entity.startTimestamp
        if (_tmpStartTimestamp == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpStartTimestamp)
        }
        val _tmpEndTimestamp: Long? = entity.endTimestamp
        if (_tmpEndTimestamp == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpEndTimestamp)
        }
        val _tmpTeamAId: Int? = entity.teamAId
        if (_tmpTeamAId == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpTeamAId.toLong())
        }
        val _tmpTeamBId: Int? = entity.teamBId
        if (_tmpTeamBId == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpTeamBId.toLong())
        }
        val _tmpTeamALabel: String? = entity.teamALabel
        if (_tmpTeamALabel == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpTeamALabel)
        }
        val _tmpTeamBLabel: String? = entity.teamBLabel
        if (_tmpTeamBLabel == null) {
          statement.bindNull(19)
        } else {
          statement.bindText(19, _tmpTeamBLabel)
        }
        val _tmpMatchFormat: String? = entity.matchFormat
        if (_tmpMatchFormat == null) {
          statement.bindNull(20)
        } else {
          statement.bindText(20, _tmpMatchFormat)
        }
        val _tmpSetScores: String? = entity.setScores
        if (_tmpSetScores == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpSetScores)
        }
        val _tmpTeamASetsWon: Int? = entity.teamASetsWon
        if (_tmpTeamASetsWon == null) {
          statement.bindNull(22)
        } else {
          statement.bindLong(22, _tmpTeamASetsWon.toLong())
        }
        val _tmpTeamBSetsWon: Int? = entity.teamBSetsWon
        if (_tmpTeamBSetsWon == null) {
          statement.bindNull(23)
        } else {
          statement.bindLong(23, _tmpTeamBSetsWon.toLong())
        }
        statement.bindLong(24, entity.id.toLong())
      }
    }
    this.__updateAdapterOfPlayerEloLog = object : EntityDeleteOrUpdateAdapter<PlayerEloLog>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `elo_logs` SET `id` = ?,`playerId` = ?,`playerNameSnapshot` = ?,`date` = ?,`elo` = ?,`groupName` = ?,`won` = ?,`matchHistoryId` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlayerEloLog) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.playerId.toLong())
        statement.bindText(3, entity.playerNameSnapshot)
        statement.bindText(4, entity.date)
        statement.bindDouble(5, entity.elo)
        statement.bindText(6, entity.groupName)
        val _tmpWon: Boolean? = entity.won
        val _tmp: Int? = _tmpWon?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp.toLong())
        }
        val _tmpMatchHistoryId: Int? = entity.matchHistoryId
        if (_tmpMatchHistoryId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpMatchHistoryId.toLong())
        }
        statement.bindLong(9, entity.id.toLong())
      }
    }
    this.__updateAdapterOfTournamentTeam = object : EntityDeleteOrUpdateAdapter<TournamentTeam>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `tournament_teams` SET `id` = ?,`groupName` = ?,`teamKey` = ?,`customName` = ?,`seed` = ?,`isActive` = ?,`createdAt` = ?,`matchesPlayed` = ?,`wins` = ?,`losses` = ?,`setsWon` = ?,`setsLost` = ?,`pointsWon` = ?,`pointsLost` = ?,`tournamentPoints` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeam) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindText(3, entity.teamKey)
        val _tmpCustomName: String? = entity.customName
        if (_tmpCustomName == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpCustomName)
        }
        val _tmpSeed: Int? = entity.seed
        if (_tmpSeed == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpSeed.toLong())
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.createdAt)
        statement.bindLong(8, entity.matchesPlayed.toLong())
        statement.bindLong(9, entity.wins.toLong())
        statement.bindLong(10, entity.losses.toLong())
        statement.bindLong(11, entity.setsWon.toLong())
        statement.bindLong(12, entity.setsLost.toLong())
        statement.bindLong(13, entity.pointsWon.toLong())
        statement.bindLong(14, entity.pointsLost.toLong())
        statement.bindLong(15, entity.tournamentPoints.toLong())
        statement.bindLong(16, entity.id.toLong())
      }
    }
    this.__updateAdapterOfTournamentTeamMember = object : EntityDeleteOrUpdateAdapter<TournamentTeamMember>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `tournament_team_members` SET `id` = ?,`groupName` = ?,`teamId` = ?,`playerId` = ?,`position` = ?,`isActive` = ?,`joinedAt` = ?,`leftAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentTeamMember) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindLong(3, entity.teamId.toLong())
        statement.bindLong(4, entity.playerId.toLong())
        val _tmpPosition: String? = entity.position
        if (_tmpPosition == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpPosition)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.joinedAt)
        val _tmpLeftAt: Long? = entity.leftAt
        if (_tmpLeftAt == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpLeftAt)
        }
        statement.bindLong(9, entity.id.toLong())
      }
    }
    this.__updateAdapterOfTournamentMatch = object : EntityDeleteOrUpdateAdapter<TournamentMatch>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `tournament_matches` SET `id` = ?,`groupName` = ?,`phase` = ?,`phaseLabel` = ?,`roundIndex` = ?,`orderInRound` = ?,`homeTeamId` = ?,`awayTeamId` = ?,`homeSourceMatchId` = ?,`awaySourceMatchId` = ?,`homeScore` = ?,`awayScore` = ?,`winnerTeamId` = ?,`status` = ?,`matchHistoryId` = ?,`startTimestamp` = ?,`endTimestamp` = ?,`matchFormat` = ?,`homeSetsWon` = ?,`awaySetsWon` = ?,`setScores` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TournamentMatch) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.groupName)
        statement.bindText(3, entity.phase)
        val _tmpPhaseLabel: String? = entity.phaseLabel
        if (_tmpPhaseLabel == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpPhaseLabel)
        }
        statement.bindLong(5, entity.roundIndex.toLong())
        statement.bindLong(6, entity.orderInRound.toLong())
        val _tmpHomeTeamId: Int? = entity.homeTeamId
        if (_tmpHomeTeamId == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpHomeTeamId.toLong())
        }
        val _tmpAwayTeamId: Int? = entity.awayTeamId
        if (_tmpAwayTeamId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpAwayTeamId.toLong())
        }
        val _tmpHomeSourceMatchId: Int? = entity.homeSourceMatchId
        if (_tmpHomeSourceMatchId == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpHomeSourceMatchId.toLong())
        }
        val _tmpAwaySourceMatchId: Int? = entity.awaySourceMatchId
        if (_tmpAwaySourceMatchId == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpAwaySourceMatchId.toLong())
        }
        val _tmpHomeScore: Int? = entity.homeScore
        if (_tmpHomeScore == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmpHomeScore.toLong())
        }
        val _tmpAwayScore: Int? = entity.awayScore
        if (_tmpAwayScore == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpAwayScore.toLong())
        }
        val _tmpWinnerTeamId: Int? = entity.winnerTeamId
        if (_tmpWinnerTeamId == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpWinnerTeamId.toLong())
        }
        statement.bindText(14, entity.status)
        val _tmpMatchHistoryId: Int? = entity.matchHistoryId
        if (_tmpMatchHistoryId == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmpMatchHistoryId.toLong())
        }
        val _tmpStartTimestamp: Long? = entity.startTimestamp
        if (_tmpStartTimestamp == null) {
          statement.bindNull(16)
        } else {
          statement.bindLong(16, _tmpStartTimestamp)
        }
        val _tmpEndTimestamp: Long? = entity.endTimestamp
        if (_tmpEndTimestamp == null) {
          statement.bindNull(17)
        } else {
          statement.bindLong(17, _tmpEndTimestamp)
        }
        val _tmpMatchFormat: String? = entity.matchFormat
        if (_tmpMatchFormat == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpMatchFormat)
        }
        val _tmpHomeSetsWon: Int? = entity.homeSetsWon
        if (_tmpHomeSetsWon == null) {
          statement.bindNull(19)
        } else {
          statement.bindLong(19, _tmpHomeSetsWon.toLong())
        }
        val _tmpAwaySetsWon: Int? = entity.awaySetsWon
        if (_tmpAwaySetsWon == null) {
          statement.bindNull(20)
        } else {
          statement.bindLong(20, _tmpAwaySetsWon.toLong())
        }
        val _tmpSetScores: String? = entity.setScores
        if (_tmpSetScores == null) {
          statement.bindNull(21)
        } else {
          statement.bindText(21, _tmpSetScores)
        }
        statement.bindLong(22, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertPlayer(player: Player): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlayer.insertAndReturnId(_connection, player)
    _result
  }

  public override suspend fun insertPlayers(players: List<Player>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlayer_1.insert(_connection, players)
  }

  public override suspend fun insertMatch(match: MatchHistory): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMatchHistory.insertAndReturnId(_connection, match)
    _result
  }

  public override suspend fun insertHistoryList(history: List<MatchHistory>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMatchHistory_1.insert(_connection, history)
  }

  public override suspend fun insertEloLogs(logs: List<PlayerEloLog>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlayerEloLog.insert(_connection, logs)
  }

  public override suspend fun insertEloLog(log: PlayerEloLog): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlayerEloLog_1.insert(_connection, log)
  }

  public override suspend fun saveGroupConfig(config: GroupConfig): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGroupConfig.insert(_connection, config)
  }

  public override suspend fun insertTournamentTeam(team: TournamentTeam): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTournamentTeam.insertAndReturnId(_connection, team)
    _result
  }

  public override suspend fun insertTournamentTeams(teams: List<TournamentTeam>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTournamentTeam.insert(_connection, teams)
  }

  public override suspend fun insertTournamentTeamMember(member: TournamentTeamMember): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTournamentTeamMember.insertAndReturnId(_connection, member)
    _result
  }

  public override suspend fun insertTournamentTeamMembers(members: List<TournamentTeamMember>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTournamentTeamMember.insert(_connection, members)
  }

  public override suspend fun insertTournamentMatch(match: TournamentMatch): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTournamentMatch.insertAndReturnId(_connection, match)
    _result
  }

  public override suspend fun insertTournamentMatches(matches: List<TournamentMatch>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTournamentMatch.insert(_connection, matches)
  }

  public override suspend fun insertGroupLog(log: GroupLog): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfGroupLog.insertAndReturnId(_connection, log)
    _result
  }

  public override suspend fun insertGroupLogs(logs: List<GroupLog>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGroupLog_1.insert(_connection, logs)
  }

  public override suspend fun deletePlayer(player: Player): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPlayer.handle(_connection, player)
  }

  public override suspend fun deleteMatch(match: MatchHistory): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMatchHistory.handle(_connection, match)
  }

  public override suspend fun deleteTournamentTeam(team: TournamentTeam): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTournamentTeam.handle(_connection, team)
  }

  public override suspend fun deleteTournamentTeamMember(member: TournamentTeamMember): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTournamentTeamMember.handle(_connection, member)
  }

  public override suspend fun updatePlayers(players: List<Player>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlayer.handleMultiple(_connection, players)
  }

  public override suspend fun updatePlayer(player: Player): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlayer.handle(_connection, player)
  }

  public override suspend fun updateMatchHistories(history: List<MatchHistory>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfMatchHistory.handleMultiple(_connection, history)
  }

  public override suspend fun updatePlayerEloLogs(logs: List<PlayerEloLog>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlayerEloLog.handleMultiple(_connection, logs)
  }

  public override suspend fun updateTournamentTeam(team: TournamentTeam): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTournamentTeam.handle(_connection, team)
  }

  public override suspend fun updateTournamentTeamMembers(members: List<TournamentTeamMember>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTournamentTeamMember.handleMultiple(_connection, members)
  }

  public override suspend fun updateTournamentMatch(match: TournamentMatch): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTournamentMatch.handle(_connection, match)
  }

  public override suspend fun updateTournamentMatches(matches: List<TournamentMatch>): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTournamentMatch.handleMultiple(_connection, matches)
  }

  public override fun getAllPlayers(): Flow<List<Player>> {
    val _sql: String = "SELECT * FROM players ORDER BY elo DESC"
    return createFlow(__db, false, arrayOf("players")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfMatchesPlayed: Int = getColumnIndexOrThrow(_stmt, "matchesPlayed")
        val _columnIndexOfVictories: Int = getColumnIndexOrThrow(_stmt, "victories")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfDailyToll: Int = getColumnIndexOrThrow(_stmt, "dailyToll")
        val _columnIndexOfTollDate: Int = getColumnIndexOrThrow(_stmt, "tollDate")
        val _columnIndexOfPreferredPosition: Int = getColumnIndexOrThrow(_stmt, "preferredPosition")
        val _columnIndexOfSecondaryPosition: Int = getColumnIndexOrThrow(_stmt, "secondaryPosition")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _result: MutableList<Player> = mutableListOf()
        while (_stmt.step()) {
          val _item: Player
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpMatchesPlayed: Int
          _tmpMatchesPlayed = _stmt.getLong(_columnIndexOfMatchesPlayed).toInt()
          val _tmpVictories: Int
          _tmpVictories = _stmt.getLong(_columnIndexOfVictories).toInt()
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpDailyToll: Int
          _tmpDailyToll = _stmt.getLong(_columnIndexOfDailyToll).toInt()
          val _tmpTollDate: String
          _tmpTollDate = _stmt.getText(_columnIndexOfTollDate)
          val _tmpPreferredPosition: String?
          if (_stmt.isNull(_columnIndexOfPreferredPosition)) {
            _tmpPreferredPosition = null
          } else {
            _tmpPreferredPosition = _stmt.getText(_columnIndexOfPreferredPosition)
          }
          val _tmpSecondaryPosition: String?
          if (_stmt.isNull(_columnIndexOfSecondaryPosition)) {
            _tmpSecondaryPosition = null
          } else {
            _tmpSecondaryPosition = _stmt.getText(_columnIndexOfSecondaryPosition)
          }
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          _item = Player(_tmpId,_tmpName,_tmpElo,_tmpMatchesPlayed,_tmpVictories,_tmpIsPriority,_tmpGroupName,_tmpDailyToll,_tmpTollDate,_tmpPreferredPosition,_tmpSecondaryPosition,_tmpPublicId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlayersByGroup(groupName: String): Flow<List<Player>> {
    val _sql: String = "SELECT * FROM players WHERE groupName = ? ORDER BY elo DESC"
    return createFlow(__db, false, arrayOf("players")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfMatchesPlayed: Int = getColumnIndexOrThrow(_stmt, "matchesPlayed")
        val _columnIndexOfVictories: Int = getColumnIndexOrThrow(_stmt, "victories")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfDailyToll: Int = getColumnIndexOrThrow(_stmt, "dailyToll")
        val _columnIndexOfTollDate: Int = getColumnIndexOrThrow(_stmt, "tollDate")
        val _columnIndexOfPreferredPosition: Int = getColumnIndexOrThrow(_stmt, "preferredPosition")
        val _columnIndexOfSecondaryPosition: Int = getColumnIndexOrThrow(_stmt, "secondaryPosition")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _result: MutableList<Player> = mutableListOf()
        while (_stmt.step()) {
          val _item: Player
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpMatchesPlayed: Int
          _tmpMatchesPlayed = _stmt.getLong(_columnIndexOfMatchesPlayed).toInt()
          val _tmpVictories: Int
          _tmpVictories = _stmt.getLong(_columnIndexOfVictories).toInt()
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpDailyToll: Int
          _tmpDailyToll = _stmt.getLong(_columnIndexOfDailyToll).toInt()
          val _tmpTollDate: String
          _tmpTollDate = _stmt.getText(_columnIndexOfTollDate)
          val _tmpPreferredPosition: String?
          if (_stmt.isNull(_columnIndexOfPreferredPosition)) {
            _tmpPreferredPosition = null
          } else {
            _tmpPreferredPosition = _stmt.getText(_columnIndexOfPreferredPosition)
          }
          val _tmpSecondaryPosition: String?
          if (_stmt.isNull(_columnIndexOfSecondaryPosition)) {
            _tmpSecondaryPosition = null
          } else {
            _tmpSecondaryPosition = _stmt.getText(_columnIndexOfSecondaryPosition)
          }
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          _item = Player(_tmpId,_tmpName,_tmpElo,_tmpMatchesPlayed,_tmpVictories,_tmpIsPriority,_tmpGroupName,_tmpDailyToll,_tmpTollDate,_tmpPreferredPosition,_tmpSecondaryPosition,_tmpPublicId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getHistory(): Flow<List<MatchHistory>> {
    val _sql: String = "SELECT * FROM match_history ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("match_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTeamA: Int = getColumnIndexOrThrow(_stmt, "teamA")
        val _columnIndexOfTeamB: Int = getColumnIndexOrThrow(_stmt, "teamB")
        val _columnIndexOfTeamAIds: Int = getColumnIndexOrThrow(_stmt, "teamAIds")
        val _columnIndexOfTeamBIds: Int = getColumnIndexOrThrow(_stmt, "teamBIds")
        val _columnIndexOfWinner: Int = getColumnIndexOrThrow(_stmt, "winner")
        val _columnIndexOfEloPoints: Int = getColumnIndexOrThrow(_stmt, "eloPoints")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamAAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamAAverageElo")
        val _columnIndexOfTeamBAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamBAverageElo")
        val _columnIndexOfTeamAScore: Int = getColumnIndexOrThrow(_stmt, "teamAScore")
        val _columnIndexOfTeamBScore: Int = getColumnIndexOrThrow(_stmt, "teamBScore")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfTeamAId: Int = getColumnIndexOrThrow(_stmt, "teamAId")
        val _columnIndexOfTeamBId: Int = getColumnIndexOrThrow(_stmt, "teamBId")
        val _columnIndexOfTeamALabel: Int = getColumnIndexOrThrow(_stmt, "teamALabel")
        val _columnIndexOfTeamBLabel: Int = getColumnIndexOrThrow(_stmt, "teamBLabel")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _columnIndexOfTeamASetsWon: Int = getColumnIndexOrThrow(_stmt, "teamASetsWon")
        val _columnIndexOfTeamBSetsWon: Int = getColumnIndexOrThrow(_stmt, "teamBSetsWon")
        val _result: MutableList<MatchHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: MatchHistory
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTeamA: String
          _tmpTeamA = _stmt.getText(_columnIndexOfTeamA)
          val _tmpTeamB: String
          _tmpTeamB = _stmt.getText(_columnIndexOfTeamB)
          val _tmpTeamAIds: String
          _tmpTeamAIds = _stmt.getText(_columnIndexOfTeamAIds)
          val _tmpTeamBIds: String
          _tmpTeamBIds = _stmt.getText(_columnIndexOfTeamBIds)
          val _tmpWinner: String
          _tmpWinner = _stmt.getText(_columnIndexOfWinner)
          val _tmpEloPoints: Double
          _tmpEloPoints = _stmt.getDouble(_columnIndexOfEloPoints)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamAAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamAAverageElo)) {
            _tmpTeamAAverageElo = null
          } else {
            _tmpTeamAAverageElo = _stmt.getDouble(_columnIndexOfTeamAAverageElo)
          }
          val _tmpTeamBAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamBAverageElo)) {
            _tmpTeamBAverageElo = null
          } else {
            _tmpTeamBAverageElo = _stmt.getDouble(_columnIndexOfTeamBAverageElo)
          }
          val _tmpTeamAScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamAScore)) {
            _tmpTeamAScore = null
          } else {
            _tmpTeamAScore = _stmt.getLong(_columnIndexOfTeamAScore).toInt()
          }
          val _tmpTeamBScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamBScore)) {
            _tmpTeamBScore = null
          } else {
            _tmpTeamBScore = _stmt.getLong(_columnIndexOfTeamBScore).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpTeamAId: Int?
          if (_stmt.isNull(_columnIndexOfTeamAId)) {
            _tmpTeamAId = null
          } else {
            _tmpTeamAId = _stmt.getLong(_columnIndexOfTeamAId).toInt()
          }
          val _tmpTeamBId: Int?
          if (_stmt.isNull(_columnIndexOfTeamBId)) {
            _tmpTeamBId = null
          } else {
            _tmpTeamBId = _stmt.getLong(_columnIndexOfTeamBId).toInt()
          }
          val _tmpTeamALabel: String?
          if (_stmt.isNull(_columnIndexOfTeamALabel)) {
            _tmpTeamALabel = null
          } else {
            _tmpTeamALabel = _stmt.getText(_columnIndexOfTeamALabel)
          }
          val _tmpTeamBLabel: String?
          if (_stmt.isNull(_columnIndexOfTeamBLabel)) {
            _tmpTeamBLabel = null
          } else {
            _tmpTeamBLabel = _stmt.getText(_columnIndexOfTeamBLabel)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          val _tmpTeamASetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamASetsWon)) {
            _tmpTeamASetsWon = null
          } else {
            _tmpTeamASetsWon = _stmt.getLong(_columnIndexOfTeamASetsWon).toInt()
          }
          val _tmpTeamBSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamBSetsWon)) {
            _tmpTeamBSetsWon = null
          } else {
            _tmpTeamBSetsWon = _stmt.getLong(_columnIndexOfTeamBSetsWon).toInt()
          }
          _item = MatchHistory(_tmpId,_tmpDate,_tmpTeamA,_tmpTeamB,_tmpTeamAIds,_tmpTeamBIds,_tmpWinner,_tmpEloPoints,_tmpGroupName,_tmpTeamAAverageElo,_tmpTeamBAverageElo,_tmpTeamAScore,_tmpTeamBScore,_tmpStartTimestamp,_tmpEndTimestamp,_tmpTeamAId,_tmpTeamBId,_tmpTeamALabel,_tmpTeamBLabel,_tmpMatchFormat,_tmpSetScores,_tmpTeamASetsWon,_tmpTeamBSetsWon)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getHistoryByGroup(groupName: String): Flow<List<MatchHistory>> {
    val _sql: String = "SELECT * FROM match_history WHERE groupName = ? ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("match_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTeamA: Int = getColumnIndexOrThrow(_stmt, "teamA")
        val _columnIndexOfTeamB: Int = getColumnIndexOrThrow(_stmt, "teamB")
        val _columnIndexOfTeamAIds: Int = getColumnIndexOrThrow(_stmt, "teamAIds")
        val _columnIndexOfTeamBIds: Int = getColumnIndexOrThrow(_stmt, "teamBIds")
        val _columnIndexOfWinner: Int = getColumnIndexOrThrow(_stmt, "winner")
        val _columnIndexOfEloPoints: Int = getColumnIndexOrThrow(_stmt, "eloPoints")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamAAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamAAverageElo")
        val _columnIndexOfTeamBAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamBAverageElo")
        val _columnIndexOfTeamAScore: Int = getColumnIndexOrThrow(_stmt, "teamAScore")
        val _columnIndexOfTeamBScore: Int = getColumnIndexOrThrow(_stmt, "teamBScore")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfTeamAId: Int = getColumnIndexOrThrow(_stmt, "teamAId")
        val _columnIndexOfTeamBId: Int = getColumnIndexOrThrow(_stmt, "teamBId")
        val _columnIndexOfTeamALabel: Int = getColumnIndexOrThrow(_stmt, "teamALabel")
        val _columnIndexOfTeamBLabel: Int = getColumnIndexOrThrow(_stmt, "teamBLabel")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _columnIndexOfTeamASetsWon: Int = getColumnIndexOrThrow(_stmt, "teamASetsWon")
        val _columnIndexOfTeamBSetsWon: Int = getColumnIndexOrThrow(_stmt, "teamBSetsWon")
        val _result: MutableList<MatchHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: MatchHistory
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTeamA: String
          _tmpTeamA = _stmt.getText(_columnIndexOfTeamA)
          val _tmpTeamB: String
          _tmpTeamB = _stmt.getText(_columnIndexOfTeamB)
          val _tmpTeamAIds: String
          _tmpTeamAIds = _stmt.getText(_columnIndexOfTeamAIds)
          val _tmpTeamBIds: String
          _tmpTeamBIds = _stmt.getText(_columnIndexOfTeamBIds)
          val _tmpWinner: String
          _tmpWinner = _stmt.getText(_columnIndexOfWinner)
          val _tmpEloPoints: Double
          _tmpEloPoints = _stmt.getDouble(_columnIndexOfEloPoints)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamAAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamAAverageElo)) {
            _tmpTeamAAverageElo = null
          } else {
            _tmpTeamAAverageElo = _stmt.getDouble(_columnIndexOfTeamAAverageElo)
          }
          val _tmpTeamBAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamBAverageElo)) {
            _tmpTeamBAverageElo = null
          } else {
            _tmpTeamBAverageElo = _stmt.getDouble(_columnIndexOfTeamBAverageElo)
          }
          val _tmpTeamAScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamAScore)) {
            _tmpTeamAScore = null
          } else {
            _tmpTeamAScore = _stmt.getLong(_columnIndexOfTeamAScore).toInt()
          }
          val _tmpTeamBScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamBScore)) {
            _tmpTeamBScore = null
          } else {
            _tmpTeamBScore = _stmt.getLong(_columnIndexOfTeamBScore).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpTeamAId: Int?
          if (_stmt.isNull(_columnIndexOfTeamAId)) {
            _tmpTeamAId = null
          } else {
            _tmpTeamAId = _stmt.getLong(_columnIndexOfTeamAId).toInt()
          }
          val _tmpTeamBId: Int?
          if (_stmt.isNull(_columnIndexOfTeamBId)) {
            _tmpTeamBId = null
          } else {
            _tmpTeamBId = _stmt.getLong(_columnIndexOfTeamBId).toInt()
          }
          val _tmpTeamALabel: String?
          if (_stmt.isNull(_columnIndexOfTeamALabel)) {
            _tmpTeamALabel = null
          } else {
            _tmpTeamALabel = _stmt.getText(_columnIndexOfTeamALabel)
          }
          val _tmpTeamBLabel: String?
          if (_stmt.isNull(_columnIndexOfTeamBLabel)) {
            _tmpTeamBLabel = null
          } else {
            _tmpTeamBLabel = _stmt.getText(_columnIndexOfTeamBLabel)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          val _tmpTeamASetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamASetsWon)) {
            _tmpTeamASetsWon = null
          } else {
            _tmpTeamASetsWon = _stmt.getLong(_columnIndexOfTeamASetsWon).toInt()
          }
          val _tmpTeamBSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamBSetsWon)) {
            _tmpTeamBSetsWon = null
          } else {
            _tmpTeamBSetsWon = _stmt.getLong(_columnIndexOfTeamBSetsWon).toInt()
          }
          _item = MatchHistory(_tmpId,_tmpDate,_tmpTeamA,_tmpTeamB,_tmpTeamAIds,_tmpTeamBIds,_tmpWinner,_tmpEloPoints,_tmpGroupName,_tmpTeamAAverageElo,_tmpTeamBAverageElo,_tmpTeamAScore,_tmpTeamBScore,_tmpStartTimestamp,_tmpEndTimestamp,_tmpTeamAId,_tmpTeamBId,_tmpTeamALabel,_tmpTeamBLabel,_tmpMatchFormat,_tmpSetScores,_tmpTeamASetsWon,_tmpTeamBSetsWon)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastMatchForGroupSync(groupName: String): MatchHistory? {
    val _sql: String = "SELECT * FROM match_history WHERE groupName = ? ORDER BY id DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTeamA: Int = getColumnIndexOrThrow(_stmt, "teamA")
        val _columnIndexOfTeamB: Int = getColumnIndexOrThrow(_stmt, "teamB")
        val _columnIndexOfTeamAIds: Int = getColumnIndexOrThrow(_stmt, "teamAIds")
        val _columnIndexOfTeamBIds: Int = getColumnIndexOrThrow(_stmt, "teamBIds")
        val _columnIndexOfWinner: Int = getColumnIndexOrThrow(_stmt, "winner")
        val _columnIndexOfEloPoints: Int = getColumnIndexOrThrow(_stmt, "eloPoints")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamAAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamAAverageElo")
        val _columnIndexOfTeamBAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamBAverageElo")
        val _columnIndexOfTeamAScore: Int = getColumnIndexOrThrow(_stmt, "teamAScore")
        val _columnIndexOfTeamBScore: Int = getColumnIndexOrThrow(_stmt, "teamBScore")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfTeamAId: Int = getColumnIndexOrThrow(_stmt, "teamAId")
        val _columnIndexOfTeamBId: Int = getColumnIndexOrThrow(_stmt, "teamBId")
        val _columnIndexOfTeamALabel: Int = getColumnIndexOrThrow(_stmt, "teamALabel")
        val _columnIndexOfTeamBLabel: Int = getColumnIndexOrThrow(_stmt, "teamBLabel")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _columnIndexOfTeamASetsWon: Int = getColumnIndexOrThrow(_stmt, "teamASetsWon")
        val _columnIndexOfTeamBSetsWon: Int = getColumnIndexOrThrow(_stmt, "teamBSetsWon")
        val _result: MatchHistory?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTeamA: String
          _tmpTeamA = _stmt.getText(_columnIndexOfTeamA)
          val _tmpTeamB: String
          _tmpTeamB = _stmt.getText(_columnIndexOfTeamB)
          val _tmpTeamAIds: String
          _tmpTeamAIds = _stmt.getText(_columnIndexOfTeamAIds)
          val _tmpTeamBIds: String
          _tmpTeamBIds = _stmt.getText(_columnIndexOfTeamBIds)
          val _tmpWinner: String
          _tmpWinner = _stmt.getText(_columnIndexOfWinner)
          val _tmpEloPoints: Double
          _tmpEloPoints = _stmt.getDouble(_columnIndexOfEloPoints)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamAAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamAAverageElo)) {
            _tmpTeamAAverageElo = null
          } else {
            _tmpTeamAAverageElo = _stmt.getDouble(_columnIndexOfTeamAAverageElo)
          }
          val _tmpTeamBAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamBAverageElo)) {
            _tmpTeamBAverageElo = null
          } else {
            _tmpTeamBAverageElo = _stmt.getDouble(_columnIndexOfTeamBAverageElo)
          }
          val _tmpTeamAScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamAScore)) {
            _tmpTeamAScore = null
          } else {
            _tmpTeamAScore = _stmt.getLong(_columnIndexOfTeamAScore).toInt()
          }
          val _tmpTeamBScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamBScore)) {
            _tmpTeamBScore = null
          } else {
            _tmpTeamBScore = _stmt.getLong(_columnIndexOfTeamBScore).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpTeamAId: Int?
          if (_stmt.isNull(_columnIndexOfTeamAId)) {
            _tmpTeamAId = null
          } else {
            _tmpTeamAId = _stmt.getLong(_columnIndexOfTeamAId).toInt()
          }
          val _tmpTeamBId: Int?
          if (_stmt.isNull(_columnIndexOfTeamBId)) {
            _tmpTeamBId = null
          } else {
            _tmpTeamBId = _stmt.getLong(_columnIndexOfTeamBId).toInt()
          }
          val _tmpTeamALabel: String?
          if (_stmt.isNull(_columnIndexOfTeamALabel)) {
            _tmpTeamALabel = null
          } else {
            _tmpTeamALabel = _stmt.getText(_columnIndexOfTeamALabel)
          }
          val _tmpTeamBLabel: String?
          if (_stmt.isNull(_columnIndexOfTeamBLabel)) {
            _tmpTeamBLabel = null
          } else {
            _tmpTeamBLabel = _stmt.getText(_columnIndexOfTeamBLabel)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          val _tmpTeamASetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamASetsWon)) {
            _tmpTeamASetsWon = null
          } else {
            _tmpTeamASetsWon = _stmt.getLong(_columnIndexOfTeamASetsWon).toInt()
          }
          val _tmpTeamBSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamBSetsWon)) {
            _tmpTeamBSetsWon = null
          } else {
            _tmpTeamBSetsWon = _stmt.getLong(_columnIndexOfTeamBSetsWon).toInt()
          }
          _result = MatchHistory(_tmpId,_tmpDate,_tmpTeamA,_tmpTeamB,_tmpTeamAIds,_tmpTeamBIds,_tmpWinner,_tmpEloPoints,_tmpGroupName,_tmpTeamAAverageElo,_tmpTeamBAverageElo,_tmpTeamAScore,_tmpTeamBScore,_tmpStartTimestamp,_tmpEndTimestamp,_tmpTeamAId,_tmpTeamBId,_tmpTeamALabel,_tmpTeamBLabel,_tmpMatchFormat,_tmpSetScores,_tmpTeamASetsWon,_tmpTeamBSetsWon)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllHistorySync(): List<MatchHistory> {
    val _sql: String = "SELECT * FROM match_history"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTeamA: Int = getColumnIndexOrThrow(_stmt, "teamA")
        val _columnIndexOfTeamB: Int = getColumnIndexOrThrow(_stmt, "teamB")
        val _columnIndexOfTeamAIds: Int = getColumnIndexOrThrow(_stmt, "teamAIds")
        val _columnIndexOfTeamBIds: Int = getColumnIndexOrThrow(_stmt, "teamBIds")
        val _columnIndexOfWinner: Int = getColumnIndexOrThrow(_stmt, "winner")
        val _columnIndexOfEloPoints: Int = getColumnIndexOrThrow(_stmt, "eloPoints")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamAAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamAAverageElo")
        val _columnIndexOfTeamBAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamBAverageElo")
        val _columnIndexOfTeamAScore: Int = getColumnIndexOrThrow(_stmt, "teamAScore")
        val _columnIndexOfTeamBScore: Int = getColumnIndexOrThrow(_stmt, "teamBScore")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfTeamAId: Int = getColumnIndexOrThrow(_stmt, "teamAId")
        val _columnIndexOfTeamBId: Int = getColumnIndexOrThrow(_stmt, "teamBId")
        val _columnIndexOfTeamALabel: Int = getColumnIndexOrThrow(_stmt, "teamALabel")
        val _columnIndexOfTeamBLabel: Int = getColumnIndexOrThrow(_stmt, "teamBLabel")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _columnIndexOfTeamASetsWon: Int = getColumnIndexOrThrow(_stmt, "teamASetsWon")
        val _columnIndexOfTeamBSetsWon: Int = getColumnIndexOrThrow(_stmt, "teamBSetsWon")
        val _result: MutableList<MatchHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: MatchHistory
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTeamA: String
          _tmpTeamA = _stmt.getText(_columnIndexOfTeamA)
          val _tmpTeamB: String
          _tmpTeamB = _stmt.getText(_columnIndexOfTeamB)
          val _tmpTeamAIds: String
          _tmpTeamAIds = _stmt.getText(_columnIndexOfTeamAIds)
          val _tmpTeamBIds: String
          _tmpTeamBIds = _stmt.getText(_columnIndexOfTeamBIds)
          val _tmpWinner: String
          _tmpWinner = _stmt.getText(_columnIndexOfWinner)
          val _tmpEloPoints: Double
          _tmpEloPoints = _stmt.getDouble(_columnIndexOfEloPoints)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamAAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamAAverageElo)) {
            _tmpTeamAAverageElo = null
          } else {
            _tmpTeamAAverageElo = _stmt.getDouble(_columnIndexOfTeamAAverageElo)
          }
          val _tmpTeamBAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamBAverageElo)) {
            _tmpTeamBAverageElo = null
          } else {
            _tmpTeamBAverageElo = _stmt.getDouble(_columnIndexOfTeamBAverageElo)
          }
          val _tmpTeamAScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamAScore)) {
            _tmpTeamAScore = null
          } else {
            _tmpTeamAScore = _stmt.getLong(_columnIndexOfTeamAScore).toInt()
          }
          val _tmpTeamBScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamBScore)) {
            _tmpTeamBScore = null
          } else {
            _tmpTeamBScore = _stmt.getLong(_columnIndexOfTeamBScore).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpTeamAId: Int?
          if (_stmt.isNull(_columnIndexOfTeamAId)) {
            _tmpTeamAId = null
          } else {
            _tmpTeamAId = _stmt.getLong(_columnIndexOfTeamAId).toInt()
          }
          val _tmpTeamBId: Int?
          if (_stmt.isNull(_columnIndexOfTeamBId)) {
            _tmpTeamBId = null
          } else {
            _tmpTeamBId = _stmt.getLong(_columnIndexOfTeamBId).toInt()
          }
          val _tmpTeamALabel: String?
          if (_stmt.isNull(_columnIndexOfTeamALabel)) {
            _tmpTeamALabel = null
          } else {
            _tmpTeamALabel = _stmt.getText(_columnIndexOfTeamALabel)
          }
          val _tmpTeamBLabel: String?
          if (_stmt.isNull(_columnIndexOfTeamBLabel)) {
            _tmpTeamBLabel = null
          } else {
            _tmpTeamBLabel = _stmt.getText(_columnIndexOfTeamBLabel)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          val _tmpTeamASetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamASetsWon)) {
            _tmpTeamASetsWon = null
          } else {
            _tmpTeamASetsWon = _stmt.getLong(_columnIndexOfTeamASetsWon).toInt()
          }
          val _tmpTeamBSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamBSetsWon)) {
            _tmpTeamBSetsWon = null
          } else {
            _tmpTeamBSetsWon = _stmt.getLong(_columnIndexOfTeamBSetsWon).toInt()
          }
          _item = MatchHistory(_tmpId,_tmpDate,_tmpTeamA,_tmpTeamB,_tmpTeamAIds,_tmpTeamBIds,_tmpWinner,_tmpEloPoints,_tmpGroupName,_tmpTeamAAverageElo,_tmpTeamBAverageElo,_tmpTeamAScore,_tmpTeamBScore,_tmpStartTimestamp,_tmpEndTimestamp,_tmpTeamAId,_tmpTeamBId,_tmpTeamALabel,_tmpTeamBLabel,_tmpMatchFormat,_tmpSetScores,_tmpTeamASetsWon,_tmpTeamBSetsWon)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlayersByGroupSync(groupName: String): List<Player> {
    val _sql: String = "SELECT * FROM players WHERE groupName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfMatchesPlayed: Int = getColumnIndexOrThrow(_stmt, "matchesPlayed")
        val _columnIndexOfVictories: Int = getColumnIndexOrThrow(_stmt, "victories")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfDailyToll: Int = getColumnIndexOrThrow(_stmt, "dailyToll")
        val _columnIndexOfTollDate: Int = getColumnIndexOrThrow(_stmt, "tollDate")
        val _columnIndexOfPreferredPosition: Int = getColumnIndexOrThrow(_stmt, "preferredPosition")
        val _columnIndexOfSecondaryPosition: Int = getColumnIndexOrThrow(_stmt, "secondaryPosition")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _result: MutableList<Player> = mutableListOf()
        while (_stmt.step()) {
          val _item: Player
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpMatchesPlayed: Int
          _tmpMatchesPlayed = _stmt.getLong(_columnIndexOfMatchesPlayed).toInt()
          val _tmpVictories: Int
          _tmpVictories = _stmt.getLong(_columnIndexOfVictories).toInt()
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpDailyToll: Int
          _tmpDailyToll = _stmt.getLong(_columnIndexOfDailyToll).toInt()
          val _tmpTollDate: String
          _tmpTollDate = _stmt.getText(_columnIndexOfTollDate)
          val _tmpPreferredPosition: String?
          if (_stmt.isNull(_columnIndexOfPreferredPosition)) {
            _tmpPreferredPosition = null
          } else {
            _tmpPreferredPosition = _stmt.getText(_columnIndexOfPreferredPosition)
          }
          val _tmpSecondaryPosition: String?
          if (_stmt.isNull(_columnIndexOfSecondaryPosition)) {
            _tmpSecondaryPosition = null
          } else {
            _tmpSecondaryPosition = _stmt.getText(_columnIndexOfSecondaryPosition)
          }
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          _item = Player(_tmpId,_tmpName,_tmpElo,_tmpMatchesPlayed,_tmpVictories,_tmpIsPriority,_tmpGroupName,_tmpDailyToll,_tmpTollDate,_tmpPreferredPosition,_tmpSecondaryPosition,_tmpPublicId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPlayerIdsSync(): List<Int> {
    val _sql: String = "SELECT id FROM players"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<Int> = mutableListOf()
        while (_stmt.step()) {
          val _item: Int
          _item = _stmt.getLong(0).toInt()
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPlayerPublicIdsSync(): List<String> {
    val _sql: String = "SELECT publicId FROM players"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getHistoryByGroupSync(groupName: String): List<MatchHistory> {
    val _sql: String = "SELECT * FROM match_history WHERE groupName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfTeamA: Int = getColumnIndexOrThrow(_stmt, "teamA")
        val _columnIndexOfTeamB: Int = getColumnIndexOrThrow(_stmt, "teamB")
        val _columnIndexOfTeamAIds: Int = getColumnIndexOrThrow(_stmt, "teamAIds")
        val _columnIndexOfTeamBIds: Int = getColumnIndexOrThrow(_stmt, "teamBIds")
        val _columnIndexOfWinner: Int = getColumnIndexOrThrow(_stmt, "winner")
        val _columnIndexOfEloPoints: Int = getColumnIndexOrThrow(_stmt, "eloPoints")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamAAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamAAverageElo")
        val _columnIndexOfTeamBAverageElo: Int = getColumnIndexOrThrow(_stmt, "teamBAverageElo")
        val _columnIndexOfTeamAScore: Int = getColumnIndexOrThrow(_stmt, "teamAScore")
        val _columnIndexOfTeamBScore: Int = getColumnIndexOrThrow(_stmt, "teamBScore")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfTeamAId: Int = getColumnIndexOrThrow(_stmt, "teamAId")
        val _columnIndexOfTeamBId: Int = getColumnIndexOrThrow(_stmt, "teamBId")
        val _columnIndexOfTeamALabel: Int = getColumnIndexOrThrow(_stmt, "teamALabel")
        val _columnIndexOfTeamBLabel: Int = getColumnIndexOrThrow(_stmt, "teamBLabel")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _columnIndexOfTeamASetsWon: Int = getColumnIndexOrThrow(_stmt, "teamASetsWon")
        val _columnIndexOfTeamBSetsWon: Int = getColumnIndexOrThrow(_stmt, "teamBSetsWon")
        val _result: MutableList<MatchHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: MatchHistory
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpTeamA: String
          _tmpTeamA = _stmt.getText(_columnIndexOfTeamA)
          val _tmpTeamB: String
          _tmpTeamB = _stmt.getText(_columnIndexOfTeamB)
          val _tmpTeamAIds: String
          _tmpTeamAIds = _stmt.getText(_columnIndexOfTeamAIds)
          val _tmpTeamBIds: String
          _tmpTeamBIds = _stmt.getText(_columnIndexOfTeamBIds)
          val _tmpWinner: String
          _tmpWinner = _stmt.getText(_columnIndexOfWinner)
          val _tmpEloPoints: Double
          _tmpEloPoints = _stmt.getDouble(_columnIndexOfEloPoints)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamAAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamAAverageElo)) {
            _tmpTeamAAverageElo = null
          } else {
            _tmpTeamAAverageElo = _stmt.getDouble(_columnIndexOfTeamAAverageElo)
          }
          val _tmpTeamBAverageElo: Double?
          if (_stmt.isNull(_columnIndexOfTeamBAverageElo)) {
            _tmpTeamBAverageElo = null
          } else {
            _tmpTeamBAverageElo = _stmt.getDouble(_columnIndexOfTeamBAverageElo)
          }
          val _tmpTeamAScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamAScore)) {
            _tmpTeamAScore = null
          } else {
            _tmpTeamAScore = _stmt.getLong(_columnIndexOfTeamAScore).toInt()
          }
          val _tmpTeamBScore: Int?
          if (_stmt.isNull(_columnIndexOfTeamBScore)) {
            _tmpTeamBScore = null
          } else {
            _tmpTeamBScore = _stmt.getLong(_columnIndexOfTeamBScore).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpTeamAId: Int?
          if (_stmt.isNull(_columnIndexOfTeamAId)) {
            _tmpTeamAId = null
          } else {
            _tmpTeamAId = _stmt.getLong(_columnIndexOfTeamAId).toInt()
          }
          val _tmpTeamBId: Int?
          if (_stmt.isNull(_columnIndexOfTeamBId)) {
            _tmpTeamBId = null
          } else {
            _tmpTeamBId = _stmt.getLong(_columnIndexOfTeamBId).toInt()
          }
          val _tmpTeamALabel: String?
          if (_stmt.isNull(_columnIndexOfTeamALabel)) {
            _tmpTeamALabel = null
          } else {
            _tmpTeamALabel = _stmt.getText(_columnIndexOfTeamALabel)
          }
          val _tmpTeamBLabel: String?
          if (_stmt.isNull(_columnIndexOfTeamBLabel)) {
            _tmpTeamBLabel = null
          } else {
            _tmpTeamBLabel = _stmt.getText(_columnIndexOfTeamBLabel)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          val _tmpTeamASetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamASetsWon)) {
            _tmpTeamASetsWon = null
          } else {
            _tmpTeamASetsWon = _stmt.getLong(_columnIndexOfTeamASetsWon).toInt()
          }
          val _tmpTeamBSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfTeamBSetsWon)) {
            _tmpTeamBSetsWon = null
          } else {
            _tmpTeamBSetsWon = _stmt.getLong(_columnIndexOfTeamBSetsWon).toInt()
          }
          _item = MatchHistory(_tmpId,_tmpDate,_tmpTeamA,_tmpTeamB,_tmpTeamAIds,_tmpTeamBIds,_tmpWinner,_tmpEloPoints,_tmpGroupName,_tmpTeamAAverageElo,_tmpTeamBAverageElo,_tmpTeamAScore,_tmpTeamBScore,_tmpStartTimestamp,_tmpEndTimestamp,_tmpTeamAId,_tmpTeamBId,_tmpTeamALabel,_tmpTeamBLabel,_tmpMatchFormat,_tmpSetScores,_tmpTeamASetsWon,_tmpTeamBSetsWon)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEloLogsByGroupSync(groupName: String): List<PlayerEloLog> {
    val _sql: String = "SELECT * FROM elo_logs WHERE groupName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: MutableList<PlayerEloLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEloLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _item = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getEloLogsForMatchSync(matchHistoryId: Int): List<PlayerEloLog> {
    val _sql: String = "SELECT * FROM elo_logs WHERE matchHistoryId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, matchHistoryId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: MutableList<PlayerEloLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEloLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _item = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPreviousEloLogSync(
    groupName: String,
    playerId: Int,
    beforeId: Int,
  ): PlayerEloLog? {
    val _sql: String = "SELECT * FROM elo_logs WHERE groupName = ? AND playerId = ? AND id < ? ORDER BY id DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _argIndex = 2
        _stmt.bindLong(_argIndex, playerId.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, beforeId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: PlayerEloLog?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _result = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllEloLogs(): Flow<List<PlayerEloLog>> {
    val _sql: String = "SELECT * FROM elo_logs ORDER BY date ASC"
    return createFlow(__db, false, arrayOf("elo_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: MutableList<PlayerEloLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEloLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _item = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEloLogsByGroup(groupName: String): Flow<List<PlayerEloLog>> {
    val _sql: String = "SELECT * FROM elo_logs WHERE groupName = ? ORDER BY date ASC, id ASC"
    return createFlow(__db, false, arrayOf("elo_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: MutableList<PlayerEloLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEloLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _item = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllEloLogsSync(): List<PlayerEloLog> {
    val _sql: String = "SELECT * FROM elo_logs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPlayerNameSnapshot: Int = getColumnIndexOrThrow(_stmt, "playerNameSnapshot")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfElo: Int = getColumnIndexOrThrow(_stmt, "elo")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfWon: Int = getColumnIndexOrThrow(_stmt, "won")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _result: MutableList<PlayerEloLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlayerEloLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPlayerNameSnapshot: String
          _tmpPlayerNameSnapshot = _stmt.getText(_columnIndexOfPlayerNameSnapshot)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpElo: Double
          _tmpElo = _stmt.getDouble(_columnIndexOfElo)
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpWon: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfWon)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfWon).toInt()
          }
          _tmpWon = _tmp?.let { it != 0 }
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          _item = PlayerEloLog(_tmpId,_tmpPlayerId,_tmpPlayerNameSnapshot,_tmpDate,_tmpElo,_tmpGroupName,_tmpWon,_tmpMatchHistoryId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGroupConfig(groupName: String): GroupConfig? {
    val _sql: String = "SELECT * FROM group_configs WHERE groupName = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamSize: Int = getColumnIndexOrThrow(_stmt, "teamSize")
        val _columnIndexOfVictoryLimit: Int = getColumnIndexOrThrow(_stmt, "victoryLimit")
        val _columnIndexOfPriorityEnabled: Int = getColumnIndexOrThrow(_stmt, "priorityEnabled")
        val _columnIndexOfScoreEnabled: Int = getColumnIndexOrThrow(_stmt, "scoreEnabled")
        val _columnIndexOfBalancingMode: Int = getColumnIndexOrThrow(_stmt, "balancingMode")
        val _columnIndexOfOnboardingStep: Int = getColumnIndexOrThrow(_stmt, "onboardingStep")
        val _columnIndexOfGroupType: Int = getColumnIndexOrThrow(_stmt, "groupType")
        val _columnIndexOfTournamentFormat: Int = getColumnIndexOrThrow(_stmt, "tournamentFormat")
        val _columnIndexOfTournamentStarted: Int = getColumnIndexOrThrow(_stmt, "tournamentStarted")
        val _columnIndexOfGuaranteeSetter: Int = getColumnIndexOrThrow(_stmt, "guaranteeSetter")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfRegularSetPoints: Int = getColumnIndexOrThrow(_stmt, "regularSetPoints")
        val _columnIndexOfTiebreakSetPoints: Int = getColumnIndexOrThrow(_stmt, "tiebreakSetPoints")
        val _columnIndexOfWinByTwo: Int = getColumnIndexOrThrow(_stmt, "winByTwo")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _columnIndexOfIsCloudSynced: Int = getColumnIndexOrThrow(_stmt, "isCloudSynced")
        val _columnIndexOfCloudGroupId: Int = getColumnIndexOrThrow(_stmt, "cloudGroupId")
        val _columnIndexOfLastPremiumSwitchAt: Int = getColumnIndexOrThrow(_stmt, "lastPremiumSwitchAt")
        val _columnIndexOfTeamAColorName: Int = getColumnIndexOrThrow(_stmt, "teamAColorName")
        val _columnIndexOfTeamBColorName: Int = getColumnIndexOrThrow(_stmt, "teamBColorName")
        val _columnIndexOfRemoteRole: Int = getColumnIndexOrThrow(_stmt, "remoteRole")
        val _columnIndexOfPendingOwnershipTransferTo: Int = getColumnIndexOrThrow(_stmt, "pendingOwnershipTransferTo")
        val _columnIndexOfShareHistoryWithObservers: Int = getColumnIndexOrThrow(_stmt, "shareHistoryWithObservers")
        val _columnIndexOfShowEloToObservers: Int = getColumnIndexOrThrow(_stmt, "showEloToObservers")
        val _columnIndexOfShareOnlyTodayHistory: Int = getColumnIndexOrThrow(_stmt, "shareOnlyTodayHistory")
        val _columnIndexOfHistoryBackfilledAt: Int = getColumnIndexOrThrow(_stmt, "historyBackfilledAt")
        val _columnIndexOfActiveAdminDeviceId: Int = getColumnIndexOrThrow(_stmt, "activeAdminDeviceId")
        val _columnIndexOfActiveAdminSince: Int = getColumnIndexOrThrow(_stmt, "activeAdminSince")
        val _result: GroupConfig?
        if (_stmt.step()) {
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamSize: Int
          _tmpTeamSize = _stmt.getLong(_columnIndexOfTeamSize).toInt()
          val _tmpVictoryLimit: Int
          _tmpVictoryLimit = _stmt.getLong(_columnIndexOfVictoryLimit).toInt()
          val _tmpPriorityEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPriorityEnabled).toInt()
          _tmpPriorityEnabled = _tmp != 0
          val _tmpScoreEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfScoreEnabled).toInt()
          _tmpScoreEnabled = _tmp_1 != 0
          val _tmpBalancingMode: String
          _tmpBalancingMode = _stmt.getText(_columnIndexOfBalancingMode)
          val _tmpOnboardingStep: Int
          _tmpOnboardingStep = _stmt.getLong(_columnIndexOfOnboardingStep).toInt()
          val _tmpGroupType: String
          _tmpGroupType = _stmt.getText(_columnIndexOfGroupType)
          val _tmpTournamentFormat: String?
          if (_stmt.isNull(_columnIndexOfTournamentFormat)) {
            _tmpTournamentFormat = null
          } else {
            _tmpTournamentFormat = _stmt.getText(_columnIndexOfTournamentFormat)
          }
          val _tmpTournamentStarted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfTournamentStarted).toInt()
          _tmpTournamentStarted = _tmp_2 != 0
          val _tmpGuaranteeSetter: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfGuaranteeSetter).toInt()
          _tmpGuaranteeSetter = _tmp_3 != 0
          val _tmpMatchFormat: String
          _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          val _tmpRegularSetPoints: Int
          _tmpRegularSetPoints = _stmt.getLong(_columnIndexOfRegularSetPoints).toInt()
          val _tmpTiebreakSetPoints: Int
          _tmpTiebreakSetPoints = _stmt.getLong(_columnIndexOfTiebreakSetPoints).toInt()
          val _tmpWinByTwo: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfWinByTwo).toInt()
          _tmpWinByTwo = _tmp_4 != 0
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          val _tmpIsCloudSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsCloudSynced).toInt()
          _tmpIsCloudSynced = _tmp_5 != 0
          val _tmpCloudGroupId: String?
          if (_stmt.isNull(_columnIndexOfCloudGroupId)) {
            _tmpCloudGroupId = null
          } else {
            _tmpCloudGroupId = _stmt.getText(_columnIndexOfCloudGroupId)
          }
          val _tmpLastPremiumSwitchAt: Long?
          if (_stmt.isNull(_columnIndexOfLastPremiumSwitchAt)) {
            _tmpLastPremiumSwitchAt = null
          } else {
            _tmpLastPremiumSwitchAt = _stmt.getLong(_columnIndexOfLastPremiumSwitchAt)
          }
          val _tmpTeamAColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamAColorName)) {
            _tmpTeamAColorName = null
          } else {
            _tmpTeamAColorName = _stmt.getText(_columnIndexOfTeamAColorName)
          }
          val _tmpTeamBColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamBColorName)) {
            _tmpTeamBColorName = null
          } else {
            _tmpTeamBColorName = _stmt.getText(_columnIndexOfTeamBColorName)
          }
          val _tmpRemoteRole: String?
          if (_stmt.isNull(_columnIndexOfRemoteRole)) {
            _tmpRemoteRole = null
          } else {
            _tmpRemoteRole = _stmt.getText(_columnIndexOfRemoteRole)
          }
          val _tmpPendingOwnershipTransferTo: String?
          if (_stmt.isNull(_columnIndexOfPendingOwnershipTransferTo)) {
            _tmpPendingOwnershipTransferTo = null
          } else {
            _tmpPendingOwnershipTransferTo = _stmt.getText(_columnIndexOfPendingOwnershipTransferTo)
          }
          val _tmpShareHistoryWithObservers: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfShareHistoryWithObservers).toInt()
          _tmpShareHistoryWithObservers = _tmp_6 != 0
          val _tmpShowEloToObservers: Boolean
          val _tmp_7: Int
          _tmp_7 = _stmt.getLong(_columnIndexOfShowEloToObservers).toInt()
          _tmpShowEloToObservers = _tmp_7 != 0
          val _tmpShareOnlyTodayHistory: Boolean
          val _tmp_8: Int
          _tmp_8 = _stmt.getLong(_columnIndexOfShareOnlyTodayHistory).toInt()
          _tmpShareOnlyTodayHistory = _tmp_8 != 0
          val _tmpHistoryBackfilledAt: Long?
          if (_stmt.isNull(_columnIndexOfHistoryBackfilledAt)) {
            _tmpHistoryBackfilledAt = null
          } else {
            _tmpHistoryBackfilledAt = _stmt.getLong(_columnIndexOfHistoryBackfilledAt)
          }
          val _tmpActiveAdminDeviceId: String?
          if (_stmt.isNull(_columnIndexOfActiveAdminDeviceId)) {
            _tmpActiveAdminDeviceId = null
          } else {
            _tmpActiveAdminDeviceId = _stmt.getText(_columnIndexOfActiveAdminDeviceId)
          }
          val _tmpActiveAdminSince: Long?
          if (_stmt.isNull(_columnIndexOfActiveAdminSince)) {
            _tmpActiveAdminSince = null
          } else {
            _tmpActiveAdminSince = _stmt.getLong(_columnIndexOfActiveAdminSince)
          }
          _result = GroupConfig(_tmpGroupName,_tmpTeamSize,_tmpVictoryLimit,_tmpPriorityEnabled,_tmpScoreEnabled,_tmpBalancingMode,_tmpOnboardingStep,_tmpGroupType,_tmpTournamentFormat,_tmpTournamentStarted,_tmpGuaranteeSetter,_tmpMatchFormat,_tmpRegularSetPoints,_tmpTiebreakSetPoints,_tmpWinByTwo,_tmpPublicId,_tmpIsCloudSynced,_tmpCloudGroupId,_tmpLastPremiumSwitchAt,_tmpTeamAColorName,_tmpTeamBColorName,_tmpRemoteRole,_tmpPendingOwnershipTransferTo,_tmpShareHistoryWithObservers,_tmpShowEloToObservers,_tmpShareOnlyTodayHistory,_tmpHistoryBackfilledAt,_tmpActiveAdminDeviceId,_tmpActiveAdminSince)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllGroupConfigs(): List<GroupConfig> {
    val _sql: String = "SELECT * FROM group_configs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamSize: Int = getColumnIndexOrThrow(_stmt, "teamSize")
        val _columnIndexOfVictoryLimit: Int = getColumnIndexOrThrow(_stmt, "victoryLimit")
        val _columnIndexOfPriorityEnabled: Int = getColumnIndexOrThrow(_stmt, "priorityEnabled")
        val _columnIndexOfScoreEnabled: Int = getColumnIndexOrThrow(_stmt, "scoreEnabled")
        val _columnIndexOfBalancingMode: Int = getColumnIndexOrThrow(_stmt, "balancingMode")
        val _columnIndexOfOnboardingStep: Int = getColumnIndexOrThrow(_stmt, "onboardingStep")
        val _columnIndexOfGroupType: Int = getColumnIndexOrThrow(_stmt, "groupType")
        val _columnIndexOfTournamentFormat: Int = getColumnIndexOrThrow(_stmt, "tournamentFormat")
        val _columnIndexOfTournamentStarted: Int = getColumnIndexOrThrow(_stmt, "tournamentStarted")
        val _columnIndexOfGuaranteeSetter: Int = getColumnIndexOrThrow(_stmt, "guaranteeSetter")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfRegularSetPoints: Int = getColumnIndexOrThrow(_stmt, "regularSetPoints")
        val _columnIndexOfTiebreakSetPoints: Int = getColumnIndexOrThrow(_stmt, "tiebreakSetPoints")
        val _columnIndexOfWinByTwo: Int = getColumnIndexOrThrow(_stmt, "winByTwo")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _columnIndexOfIsCloudSynced: Int = getColumnIndexOrThrow(_stmt, "isCloudSynced")
        val _columnIndexOfCloudGroupId: Int = getColumnIndexOrThrow(_stmt, "cloudGroupId")
        val _columnIndexOfLastPremiumSwitchAt: Int = getColumnIndexOrThrow(_stmt, "lastPremiumSwitchAt")
        val _columnIndexOfTeamAColorName: Int = getColumnIndexOrThrow(_stmt, "teamAColorName")
        val _columnIndexOfTeamBColorName: Int = getColumnIndexOrThrow(_stmt, "teamBColorName")
        val _columnIndexOfRemoteRole: Int = getColumnIndexOrThrow(_stmt, "remoteRole")
        val _columnIndexOfPendingOwnershipTransferTo: Int = getColumnIndexOrThrow(_stmt, "pendingOwnershipTransferTo")
        val _columnIndexOfShareHistoryWithObservers: Int = getColumnIndexOrThrow(_stmt, "shareHistoryWithObservers")
        val _columnIndexOfShowEloToObservers: Int = getColumnIndexOrThrow(_stmt, "showEloToObservers")
        val _columnIndexOfShareOnlyTodayHistory: Int = getColumnIndexOrThrow(_stmt, "shareOnlyTodayHistory")
        val _columnIndexOfHistoryBackfilledAt: Int = getColumnIndexOrThrow(_stmt, "historyBackfilledAt")
        val _columnIndexOfActiveAdminDeviceId: Int = getColumnIndexOrThrow(_stmt, "activeAdminDeviceId")
        val _columnIndexOfActiveAdminSince: Int = getColumnIndexOrThrow(_stmt, "activeAdminSince")
        val _result: MutableList<GroupConfig> = mutableListOf()
        while (_stmt.step()) {
          val _item: GroupConfig
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamSize: Int
          _tmpTeamSize = _stmt.getLong(_columnIndexOfTeamSize).toInt()
          val _tmpVictoryLimit: Int
          _tmpVictoryLimit = _stmt.getLong(_columnIndexOfVictoryLimit).toInt()
          val _tmpPriorityEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPriorityEnabled).toInt()
          _tmpPriorityEnabled = _tmp != 0
          val _tmpScoreEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfScoreEnabled).toInt()
          _tmpScoreEnabled = _tmp_1 != 0
          val _tmpBalancingMode: String
          _tmpBalancingMode = _stmt.getText(_columnIndexOfBalancingMode)
          val _tmpOnboardingStep: Int
          _tmpOnboardingStep = _stmt.getLong(_columnIndexOfOnboardingStep).toInt()
          val _tmpGroupType: String
          _tmpGroupType = _stmt.getText(_columnIndexOfGroupType)
          val _tmpTournamentFormat: String?
          if (_stmt.isNull(_columnIndexOfTournamentFormat)) {
            _tmpTournamentFormat = null
          } else {
            _tmpTournamentFormat = _stmt.getText(_columnIndexOfTournamentFormat)
          }
          val _tmpTournamentStarted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfTournamentStarted).toInt()
          _tmpTournamentStarted = _tmp_2 != 0
          val _tmpGuaranteeSetter: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfGuaranteeSetter).toInt()
          _tmpGuaranteeSetter = _tmp_3 != 0
          val _tmpMatchFormat: String
          _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          val _tmpRegularSetPoints: Int
          _tmpRegularSetPoints = _stmt.getLong(_columnIndexOfRegularSetPoints).toInt()
          val _tmpTiebreakSetPoints: Int
          _tmpTiebreakSetPoints = _stmt.getLong(_columnIndexOfTiebreakSetPoints).toInt()
          val _tmpWinByTwo: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfWinByTwo).toInt()
          _tmpWinByTwo = _tmp_4 != 0
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          val _tmpIsCloudSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsCloudSynced).toInt()
          _tmpIsCloudSynced = _tmp_5 != 0
          val _tmpCloudGroupId: String?
          if (_stmt.isNull(_columnIndexOfCloudGroupId)) {
            _tmpCloudGroupId = null
          } else {
            _tmpCloudGroupId = _stmt.getText(_columnIndexOfCloudGroupId)
          }
          val _tmpLastPremiumSwitchAt: Long?
          if (_stmt.isNull(_columnIndexOfLastPremiumSwitchAt)) {
            _tmpLastPremiumSwitchAt = null
          } else {
            _tmpLastPremiumSwitchAt = _stmt.getLong(_columnIndexOfLastPremiumSwitchAt)
          }
          val _tmpTeamAColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamAColorName)) {
            _tmpTeamAColorName = null
          } else {
            _tmpTeamAColorName = _stmt.getText(_columnIndexOfTeamAColorName)
          }
          val _tmpTeamBColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamBColorName)) {
            _tmpTeamBColorName = null
          } else {
            _tmpTeamBColorName = _stmt.getText(_columnIndexOfTeamBColorName)
          }
          val _tmpRemoteRole: String?
          if (_stmt.isNull(_columnIndexOfRemoteRole)) {
            _tmpRemoteRole = null
          } else {
            _tmpRemoteRole = _stmt.getText(_columnIndexOfRemoteRole)
          }
          val _tmpPendingOwnershipTransferTo: String?
          if (_stmt.isNull(_columnIndexOfPendingOwnershipTransferTo)) {
            _tmpPendingOwnershipTransferTo = null
          } else {
            _tmpPendingOwnershipTransferTo = _stmt.getText(_columnIndexOfPendingOwnershipTransferTo)
          }
          val _tmpShareHistoryWithObservers: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfShareHistoryWithObservers).toInt()
          _tmpShareHistoryWithObservers = _tmp_6 != 0
          val _tmpShowEloToObservers: Boolean
          val _tmp_7: Int
          _tmp_7 = _stmt.getLong(_columnIndexOfShowEloToObservers).toInt()
          _tmpShowEloToObservers = _tmp_7 != 0
          val _tmpShareOnlyTodayHistory: Boolean
          val _tmp_8: Int
          _tmp_8 = _stmt.getLong(_columnIndexOfShareOnlyTodayHistory).toInt()
          _tmpShareOnlyTodayHistory = _tmp_8 != 0
          val _tmpHistoryBackfilledAt: Long?
          if (_stmt.isNull(_columnIndexOfHistoryBackfilledAt)) {
            _tmpHistoryBackfilledAt = null
          } else {
            _tmpHistoryBackfilledAt = _stmt.getLong(_columnIndexOfHistoryBackfilledAt)
          }
          val _tmpActiveAdminDeviceId: String?
          if (_stmt.isNull(_columnIndexOfActiveAdminDeviceId)) {
            _tmpActiveAdminDeviceId = null
          } else {
            _tmpActiveAdminDeviceId = _stmt.getText(_columnIndexOfActiveAdminDeviceId)
          }
          val _tmpActiveAdminSince: Long?
          if (_stmt.isNull(_columnIndexOfActiveAdminSince)) {
            _tmpActiveAdminSince = null
          } else {
            _tmpActiveAdminSince = _stmt.getLong(_columnIndexOfActiveAdminSince)
          }
          _item = GroupConfig(_tmpGroupName,_tmpTeamSize,_tmpVictoryLimit,_tmpPriorityEnabled,_tmpScoreEnabled,_tmpBalancingMode,_tmpOnboardingStep,_tmpGroupType,_tmpTournamentFormat,_tmpTournamentStarted,_tmpGuaranteeSetter,_tmpMatchFormat,_tmpRegularSetPoints,_tmpTiebreakSetPoints,_tmpWinByTwo,_tmpPublicId,_tmpIsCloudSynced,_tmpCloudGroupId,_tmpLastPremiumSwitchAt,_tmpTeamAColorName,_tmpTeamBColorName,_tmpRemoteRole,_tmpPendingOwnershipTransferTo,_tmpShareHistoryWithObservers,_tmpShowEloToObservers,_tmpShareOnlyTodayHistory,_tmpHistoryBackfilledAt,_tmpActiveAdminDeviceId,_tmpActiveAdminSince)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllGroupConfigsFlow(): Flow<List<GroupConfig>> {
    val _sql: String = "SELECT * FROM group_configs"
    return createFlow(__db, false, arrayOf("group_configs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamSize: Int = getColumnIndexOrThrow(_stmt, "teamSize")
        val _columnIndexOfVictoryLimit: Int = getColumnIndexOrThrow(_stmt, "victoryLimit")
        val _columnIndexOfPriorityEnabled: Int = getColumnIndexOrThrow(_stmt, "priorityEnabled")
        val _columnIndexOfScoreEnabled: Int = getColumnIndexOrThrow(_stmt, "scoreEnabled")
        val _columnIndexOfBalancingMode: Int = getColumnIndexOrThrow(_stmt, "balancingMode")
        val _columnIndexOfOnboardingStep: Int = getColumnIndexOrThrow(_stmt, "onboardingStep")
        val _columnIndexOfGroupType: Int = getColumnIndexOrThrow(_stmt, "groupType")
        val _columnIndexOfTournamentFormat: Int = getColumnIndexOrThrow(_stmt, "tournamentFormat")
        val _columnIndexOfTournamentStarted: Int = getColumnIndexOrThrow(_stmt, "tournamentStarted")
        val _columnIndexOfGuaranteeSetter: Int = getColumnIndexOrThrow(_stmt, "guaranteeSetter")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfRegularSetPoints: Int = getColumnIndexOrThrow(_stmt, "regularSetPoints")
        val _columnIndexOfTiebreakSetPoints: Int = getColumnIndexOrThrow(_stmt, "tiebreakSetPoints")
        val _columnIndexOfWinByTwo: Int = getColumnIndexOrThrow(_stmt, "winByTwo")
        val _columnIndexOfPublicId: Int = getColumnIndexOrThrow(_stmt, "publicId")
        val _columnIndexOfIsCloudSynced: Int = getColumnIndexOrThrow(_stmt, "isCloudSynced")
        val _columnIndexOfCloudGroupId: Int = getColumnIndexOrThrow(_stmt, "cloudGroupId")
        val _columnIndexOfLastPremiumSwitchAt: Int = getColumnIndexOrThrow(_stmt, "lastPremiumSwitchAt")
        val _columnIndexOfTeamAColorName: Int = getColumnIndexOrThrow(_stmt, "teamAColorName")
        val _columnIndexOfTeamBColorName: Int = getColumnIndexOrThrow(_stmt, "teamBColorName")
        val _columnIndexOfRemoteRole: Int = getColumnIndexOrThrow(_stmt, "remoteRole")
        val _columnIndexOfPendingOwnershipTransferTo: Int = getColumnIndexOrThrow(_stmt, "pendingOwnershipTransferTo")
        val _columnIndexOfShareHistoryWithObservers: Int = getColumnIndexOrThrow(_stmt, "shareHistoryWithObservers")
        val _columnIndexOfShowEloToObservers: Int = getColumnIndexOrThrow(_stmt, "showEloToObservers")
        val _columnIndexOfShareOnlyTodayHistory: Int = getColumnIndexOrThrow(_stmt, "shareOnlyTodayHistory")
        val _columnIndexOfHistoryBackfilledAt: Int = getColumnIndexOrThrow(_stmt, "historyBackfilledAt")
        val _columnIndexOfActiveAdminDeviceId: Int = getColumnIndexOrThrow(_stmt, "activeAdminDeviceId")
        val _columnIndexOfActiveAdminSince: Int = getColumnIndexOrThrow(_stmt, "activeAdminSince")
        val _result: MutableList<GroupConfig> = mutableListOf()
        while (_stmt.step()) {
          val _item: GroupConfig
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamSize: Int
          _tmpTeamSize = _stmt.getLong(_columnIndexOfTeamSize).toInt()
          val _tmpVictoryLimit: Int
          _tmpVictoryLimit = _stmt.getLong(_columnIndexOfVictoryLimit).toInt()
          val _tmpPriorityEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPriorityEnabled).toInt()
          _tmpPriorityEnabled = _tmp != 0
          val _tmpScoreEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfScoreEnabled).toInt()
          _tmpScoreEnabled = _tmp_1 != 0
          val _tmpBalancingMode: String
          _tmpBalancingMode = _stmt.getText(_columnIndexOfBalancingMode)
          val _tmpOnboardingStep: Int
          _tmpOnboardingStep = _stmt.getLong(_columnIndexOfOnboardingStep).toInt()
          val _tmpGroupType: String
          _tmpGroupType = _stmt.getText(_columnIndexOfGroupType)
          val _tmpTournamentFormat: String?
          if (_stmt.isNull(_columnIndexOfTournamentFormat)) {
            _tmpTournamentFormat = null
          } else {
            _tmpTournamentFormat = _stmt.getText(_columnIndexOfTournamentFormat)
          }
          val _tmpTournamentStarted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfTournamentStarted).toInt()
          _tmpTournamentStarted = _tmp_2 != 0
          val _tmpGuaranteeSetter: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfGuaranteeSetter).toInt()
          _tmpGuaranteeSetter = _tmp_3 != 0
          val _tmpMatchFormat: String
          _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          val _tmpRegularSetPoints: Int
          _tmpRegularSetPoints = _stmt.getLong(_columnIndexOfRegularSetPoints).toInt()
          val _tmpTiebreakSetPoints: Int
          _tmpTiebreakSetPoints = _stmt.getLong(_columnIndexOfTiebreakSetPoints).toInt()
          val _tmpWinByTwo: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfWinByTwo).toInt()
          _tmpWinByTwo = _tmp_4 != 0
          val _tmpPublicId: String
          _tmpPublicId = _stmt.getText(_columnIndexOfPublicId)
          val _tmpIsCloudSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsCloudSynced).toInt()
          _tmpIsCloudSynced = _tmp_5 != 0
          val _tmpCloudGroupId: String?
          if (_stmt.isNull(_columnIndexOfCloudGroupId)) {
            _tmpCloudGroupId = null
          } else {
            _tmpCloudGroupId = _stmt.getText(_columnIndexOfCloudGroupId)
          }
          val _tmpLastPremiumSwitchAt: Long?
          if (_stmt.isNull(_columnIndexOfLastPremiumSwitchAt)) {
            _tmpLastPremiumSwitchAt = null
          } else {
            _tmpLastPremiumSwitchAt = _stmt.getLong(_columnIndexOfLastPremiumSwitchAt)
          }
          val _tmpTeamAColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamAColorName)) {
            _tmpTeamAColorName = null
          } else {
            _tmpTeamAColorName = _stmt.getText(_columnIndexOfTeamAColorName)
          }
          val _tmpTeamBColorName: String?
          if (_stmt.isNull(_columnIndexOfTeamBColorName)) {
            _tmpTeamBColorName = null
          } else {
            _tmpTeamBColorName = _stmt.getText(_columnIndexOfTeamBColorName)
          }
          val _tmpRemoteRole: String?
          if (_stmt.isNull(_columnIndexOfRemoteRole)) {
            _tmpRemoteRole = null
          } else {
            _tmpRemoteRole = _stmt.getText(_columnIndexOfRemoteRole)
          }
          val _tmpPendingOwnershipTransferTo: String?
          if (_stmt.isNull(_columnIndexOfPendingOwnershipTransferTo)) {
            _tmpPendingOwnershipTransferTo = null
          } else {
            _tmpPendingOwnershipTransferTo = _stmt.getText(_columnIndexOfPendingOwnershipTransferTo)
          }
          val _tmpShareHistoryWithObservers: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfShareHistoryWithObservers).toInt()
          _tmpShareHistoryWithObservers = _tmp_6 != 0
          val _tmpShowEloToObservers: Boolean
          val _tmp_7: Int
          _tmp_7 = _stmt.getLong(_columnIndexOfShowEloToObservers).toInt()
          _tmpShowEloToObservers = _tmp_7 != 0
          val _tmpShareOnlyTodayHistory: Boolean
          val _tmp_8: Int
          _tmp_8 = _stmt.getLong(_columnIndexOfShareOnlyTodayHistory).toInt()
          _tmpShareOnlyTodayHistory = _tmp_8 != 0
          val _tmpHistoryBackfilledAt: Long?
          if (_stmt.isNull(_columnIndexOfHistoryBackfilledAt)) {
            _tmpHistoryBackfilledAt = null
          } else {
            _tmpHistoryBackfilledAt = _stmt.getLong(_columnIndexOfHistoryBackfilledAt)
          }
          val _tmpActiveAdminDeviceId: String?
          if (_stmt.isNull(_columnIndexOfActiveAdminDeviceId)) {
            _tmpActiveAdminDeviceId = null
          } else {
            _tmpActiveAdminDeviceId = _stmt.getText(_columnIndexOfActiveAdminDeviceId)
          }
          val _tmpActiveAdminSince: Long?
          if (_stmt.isNull(_columnIndexOfActiveAdminSince)) {
            _tmpActiveAdminSince = null
          } else {
            _tmpActiveAdminSince = _stmt.getLong(_columnIndexOfActiveAdminSince)
          }
          _item = GroupConfig(_tmpGroupName,_tmpTeamSize,_tmpVictoryLimit,_tmpPriorityEnabled,_tmpScoreEnabled,_tmpBalancingMode,_tmpOnboardingStep,_tmpGroupType,_tmpTournamentFormat,_tmpTournamentStarted,_tmpGuaranteeSetter,_tmpMatchFormat,_tmpRegularSetPoints,_tmpTiebreakSetPoints,_tmpWinByTwo,_tmpPublicId,_tmpIsCloudSynced,_tmpCloudGroupId,_tmpLastPremiumSwitchAt,_tmpTeamAColorName,_tmpTeamBColorName,_tmpRemoteRole,_tmpPendingOwnershipTransferTo,_tmpShareHistoryWithObservers,_tmpShowEloToObservers,_tmpShareOnlyTodayHistory,_tmpHistoryBackfilledAt,_tmpActiveAdminDeviceId,_tmpActiveAdminSince)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllGroupNames(): List<String> {
    val _sql: String = """
        |
        |        SELECT groupName FROM group_configs
        |        UNION
        |        SELECT groupName FROM players
        |        UNION
        |        SELECT groupName FROM match_history
        |        UNION
        |        SELECT groupName FROM elo_logs
        |        ORDER BY groupName COLLATE NOCASE
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTournamentTeamsByGroup(groupName: String): Flow<List<TournamentTeam>> {
    val _sql: String = "SELECT * FROM tournament_teams WHERE groupName = ? ORDER BY teamKey ASC"
    return createFlow(__db, false, arrayOf("tournament_teams")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamKey: Int = getColumnIndexOrThrow(_stmt, "teamKey")
        val _columnIndexOfCustomName: Int = getColumnIndexOrThrow(_stmt, "customName")
        val _columnIndexOfSeed: Int = getColumnIndexOrThrow(_stmt, "seed")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfMatchesPlayed: Int = getColumnIndexOrThrow(_stmt, "matchesPlayed")
        val _columnIndexOfWins: Int = getColumnIndexOrThrow(_stmt, "wins")
        val _columnIndexOfLosses: Int = getColumnIndexOrThrow(_stmt, "losses")
        val _columnIndexOfSetsWon: Int = getColumnIndexOrThrow(_stmt, "setsWon")
        val _columnIndexOfSetsLost: Int = getColumnIndexOrThrow(_stmt, "setsLost")
        val _columnIndexOfPointsWon: Int = getColumnIndexOrThrow(_stmt, "pointsWon")
        val _columnIndexOfPointsLost: Int = getColumnIndexOrThrow(_stmt, "pointsLost")
        val _columnIndexOfTournamentPoints: Int = getColumnIndexOrThrow(_stmt, "tournamentPoints")
        val _result: MutableList<TournamentTeam> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentTeam
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamKey: String
          _tmpTeamKey = _stmt.getText(_columnIndexOfTeamKey)
          val _tmpCustomName: String?
          if (_stmt.isNull(_columnIndexOfCustomName)) {
            _tmpCustomName = null
          } else {
            _tmpCustomName = _stmt.getText(_columnIndexOfCustomName)
          }
          val _tmpSeed: Int?
          if (_stmt.isNull(_columnIndexOfSeed)) {
            _tmpSeed = null
          } else {
            _tmpSeed = _stmt.getLong(_columnIndexOfSeed).toInt()
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpMatchesPlayed: Int
          _tmpMatchesPlayed = _stmt.getLong(_columnIndexOfMatchesPlayed).toInt()
          val _tmpWins: Int
          _tmpWins = _stmt.getLong(_columnIndexOfWins).toInt()
          val _tmpLosses: Int
          _tmpLosses = _stmt.getLong(_columnIndexOfLosses).toInt()
          val _tmpSetsWon: Int
          _tmpSetsWon = _stmt.getLong(_columnIndexOfSetsWon).toInt()
          val _tmpSetsLost: Int
          _tmpSetsLost = _stmt.getLong(_columnIndexOfSetsLost).toInt()
          val _tmpPointsWon: Int
          _tmpPointsWon = _stmt.getLong(_columnIndexOfPointsWon).toInt()
          val _tmpPointsLost: Int
          _tmpPointsLost = _stmt.getLong(_columnIndexOfPointsLost).toInt()
          val _tmpTournamentPoints: Int
          _tmpTournamentPoints = _stmt.getLong(_columnIndexOfTournamentPoints).toInt()
          _item = TournamentTeam(_tmpId,_tmpGroupName,_tmpTeamKey,_tmpCustomName,_tmpSeed,_tmpIsActive,_tmpCreatedAt,_tmpMatchesPlayed,_tmpWins,_tmpLosses,_tmpSetsWon,_tmpSetsLost,_tmpPointsWon,_tmpPointsLost,_tmpTournamentPoints)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTournamentTeamsByGroupSync(groupName: String): List<TournamentTeam> {
    val _sql: String = "SELECT * FROM tournament_teams WHERE groupName = ? ORDER BY teamKey ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamKey: Int = getColumnIndexOrThrow(_stmt, "teamKey")
        val _columnIndexOfCustomName: Int = getColumnIndexOrThrow(_stmt, "customName")
        val _columnIndexOfSeed: Int = getColumnIndexOrThrow(_stmt, "seed")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfMatchesPlayed: Int = getColumnIndexOrThrow(_stmt, "matchesPlayed")
        val _columnIndexOfWins: Int = getColumnIndexOrThrow(_stmt, "wins")
        val _columnIndexOfLosses: Int = getColumnIndexOrThrow(_stmt, "losses")
        val _columnIndexOfSetsWon: Int = getColumnIndexOrThrow(_stmt, "setsWon")
        val _columnIndexOfSetsLost: Int = getColumnIndexOrThrow(_stmt, "setsLost")
        val _columnIndexOfPointsWon: Int = getColumnIndexOrThrow(_stmt, "pointsWon")
        val _columnIndexOfPointsLost: Int = getColumnIndexOrThrow(_stmt, "pointsLost")
        val _columnIndexOfTournamentPoints: Int = getColumnIndexOrThrow(_stmt, "tournamentPoints")
        val _result: MutableList<TournamentTeam> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentTeam
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamKey: String
          _tmpTeamKey = _stmt.getText(_columnIndexOfTeamKey)
          val _tmpCustomName: String?
          if (_stmt.isNull(_columnIndexOfCustomName)) {
            _tmpCustomName = null
          } else {
            _tmpCustomName = _stmt.getText(_columnIndexOfCustomName)
          }
          val _tmpSeed: Int?
          if (_stmt.isNull(_columnIndexOfSeed)) {
            _tmpSeed = null
          } else {
            _tmpSeed = _stmt.getLong(_columnIndexOfSeed).toInt()
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpMatchesPlayed: Int
          _tmpMatchesPlayed = _stmt.getLong(_columnIndexOfMatchesPlayed).toInt()
          val _tmpWins: Int
          _tmpWins = _stmt.getLong(_columnIndexOfWins).toInt()
          val _tmpLosses: Int
          _tmpLosses = _stmt.getLong(_columnIndexOfLosses).toInt()
          val _tmpSetsWon: Int
          _tmpSetsWon = _stmt.getLong(_columnIndexOfSetsWon).toInt()
          val _tmpSetsLost: Int
          _tmpSetsLost = _stmt.getLong(_columnIndexOfSetsLost).toInt()
          val _tmpPointsWon: Int
          _tmpPointsWon = _stmt.getLong(_columnIndexOfPointsWon).toInt()
          val _tmpPointsLost: Int
          _tmpPointsLost = _stmt.getLong(_columnIndexOfPointsLost).toInt()
          val _tmpTournamentPoints: Int
          _tmpTournamentPoints = _stmt.getLong(_columnIndexOfTournamentPoints).toInt()
          _item = TournamentTeam(_tmpId,_tmpGroupName,_tmpTeamKey,_tmpCustomName,_tmpSeed,_tmpIsActive,_tmpCreatedAt,_tmpMatchesPlayed,_tmpWins,_tmpLosses,_tmpSetsWon,_tmpSetsLost,_tmpPointsWon,_tmpPointsLost,_tmpTournamentPoints)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTournamentTeamMembersByGroup(groupName: String): Flow<List<TournamentTeamMember>> {
    val _sql: String = "SELECT * FROM tournament_team_members WHERE groupName = ? ORDER BY id ASC"
    return createFlow(__db, false, arrayOf("tournament_team_members")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamId: Int = getColumnIndexOrThrow(_stmt, "teamId")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfLeftAt: Int = getColumnIndexOrThrow(_stmt, "leftAt")
        val _result: MutableList<TournamentTeamMember> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentTeamMember
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamId: Int
          _tmpTeamId = _stmt.getLong(_columnIndexOfTeamId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPosition: String?
          if (_stmt.isNull(_columnIndexOfPosition)) {
            _tmpPosition = null
          } else {
            _tmpPosition = _stmt.getText(_columnIndexOfPosition)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpLeftAt: Long?
          if (_stmt.isNull(_columnIndexOfLeftAt)) {
            _tmpLeftAt = null
          } else {
            _tmpLeftAt = _stmt.getLong(_columnIndexOfLeftAt)
          }
          _item = TournamentTeamMember(_tmpId,_tmpGroupName,_tmpTeamId,_tmpPlayerId,_tmpPosition,_tmpIsActive,_tmpJoinedAt,_tmpLeftAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTournamentTeamMembersByGroupSync(groupName: String): List<TournamentTeamMember> {
    val _sql: String = "SELECT * FROM tournament_team_members WHERE groupName = ? ORDER BY id ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTeamId: Int = getColumnIndexOrThrow(_stmt, "teamId")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfJoinedAt: Int = getColumnIndexOrThrow(_stmt, "joinedAt")
        val _columnIndexOfLeftAt: Int = getColumnIndexOrThrow(_stmt, "leftAt")
        val _result: MutableList<TournamentTeamMember> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentTeamMember
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTeamId: Int
          _tmpTeamId = _stmt.getLong(_columnIndexOfTeamId).toInt()
          val _tmpPlayerId: Int
          _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          val _tmpPosition: String?
          if (_stmt.isNull(_columnIndexOfPosition)) {
            _tmpPosition = null
          } else {
            _tmpPosition = _stmt.getText(_columnIndexOfPosition)
          }
          val _tmpIsActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp != 0
          val _tmpJoinedAt: Long
          _tmpJoinedAt = _stmt.getLong(_columnIndexOfJoinedAt)
          val _tmpLeftAt: Long?
          if (_stmt.isNull(_columnIndexOfLeftAt)) {
            _tmpLeftAt = null
          } else {
            _tmpLeftAt = _stmt.getLong(_columnIndexOfLeftAt)
          }
          _item = TournamentTeamMember(_tmpId,_tmpGroupName,_tmpTeamId,_tmpPlayerId,_tmpPosition,_tmpIsActive,_tmpJoinedAt,_tmpLeftAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTournamentMatchesByGroup(groupName: String): Flow<List<TournamentMatch>> {
    val _sql: String = "SELECT * FROM tournament_matches WHERE groupName = ? ORDER BY roundIndex ASC, orderInRound ASC"
    return createFlow(__db, false, arrayOf("tournament_matches")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfPhase: Int = getColumnIndexOrThrow(_stmt, "phase")
        val _columnIndexOfPhaseLabel: Int = getColumnIndexOrThrow(_stmt, "phaseLabel")
        val _columnIndexOfRoundIndex: Int = getColumnIndexOrThrow(_stmt, "roundIndex")
        val _columnIndexOfOrderInRound: Int = getColumnIndexOrThrow(_stmt, "orderInRound")
        val _columnIndexOfHomeTeamId: Int = getColumnIndexOrThrow(_stmt, "homeTeamId")
        val _columnIndexOfAwayTeamId: Int = getColumnIndexOrThrow(_stmt, "awayTeamId")
        val _columnIndexOfHomeSourceMatchId: Int = getColumnIndexOrThrow(_stmt, "homeSourceMatchId")
        val _columnIndexOfAwaySourceMatchId: Int = getColumnIndexOrThrow(_stmt, "awaySourceMatchId")
        val _columnIndexOfHomeScore: Int = getColumnIndexOrThrow(_stmt, "homeScore")
        val _columnIndexOfAwayScore: Int = getColumnIndexOrThrow(_stmt, "awayScore")
        val _columnIndexOfWinnerTeamId: Int = getColumnIndexOrThrow(_stmt, "winnerTeamId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfHomeSetsWon: Int = getColumnIndexOrThrow(_stmt, "homeSetsWon")
        val _columnIndexOfAwaySetsWon: Int = getColumnIndexOrThrow(_stmt, "awaySetsWon")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _result: MutableList<TournamentMatch> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentMatch
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpPhase: String
          _tmpPhase = _stmt.getText(_columnIndexOfPhase)
          val _tmpPhaseLabel: String?
          if (_stmt.isNull(_columnIndexOfPhaseLabel)) {
            _tmpPhaseLabel = null
          } else {
            _tmpPhaseLabel = _stmt.getText(_columnIndexOfPhaseLabel)
          }
          val _tmpRoundIndex: Int
          _tmpRoundIndex = _stmt.getLong(_columnIndexOfRoundIndex).toInt()
          val _tmpOrderInRound: Int
          _tmpOrderInRound = _stmt.getLong(_columnIndexOfOrderInRound).toInt()
          val _tmpHomeTeamId: Int?
          if (_stmt.isNull(_columnIndexOfHomeTeamId)) {
            _tmpHomeTeamId = null
          } else {
            _tmpHomeTeamId = _stmt.getLong(_columnIndexOfHomeTeamId).toInt()
          }
          val _tmpAwayTeamId: Int?
          if (_stmt.isNull(_columnIndexOfAwayTeamId)) {
            _tmpAwayTeamId = null
          } else {
            _tmpAwayTeamId = _stmt.getLong(_columnIndexOfAwayTeamId).toInt()
          }
          val _tmpHomeSourceMatchId: Int?
          if (_stmt.isNull(_columnIndexOfHomeSourceMatchId)) {
            _tmpHomeSourceMatchId = null
          } else {
            _tmpHomeSourceMatchId = _stmt.getLong(_columnIndexOfHomeSourceMatchId).toInt()
          }
          val _tmpAwaySourceMatchId: Int?
          if (_stmt.isNull(_columnIndexOfAwaySourceMatchId)) {
            _tmpAwaySourceMatchId = null
          } else {
            _tmpAwaySourceMatchId = _stmt.getLong(_columnIndexOfAwaySourceMatchId).toInt()
          }
          val _tmpHomeScore: Int?
          if (_stmt.isNull(_columnIndexOfHomeScore)) {
            _tmpHomeScore = null
          } else {
            _tmpHomeScore = _stmt.getLong(_columnIndexOfHomeScore).toInt()
          }
          val _tmpAwayScore: Int?
          if (_stmt.isNull(_columnIndexOfAwayScore)) {
            _tmpAwayScore = null
          } else {
            _tmpAwayScore = _stmt.getLong(_columnIndexOfAwayScore).toInt()
          }
          val _tmpWinnerTeamId: Int?
          if (_stmt.isNull(_columnIndexOfWinnerTeamId)) {
            _tmpWinnerTeamId = null
          } else {
            _tmpWinnerTeamId = _stmt.getLong(_columnIndexOfWinnerTeamId).toInt()
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpHomeSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfHomeSetsWon)) {
            _tmpHomeSetsWon = null
          } else {
            _tmpHomeSetsWon = _stmt.getLong(_columnIndexOfHomeSetsWon).toInt()
          }
          val _tmpAwaySetsWon: Int?
          if (_stmt.isNull(_columnIndexOfAwaySetsWon)) {
            _tmpAwaySetsWon = null
          } else {
            _tmpAwaySetsWon = _stmt.getLong(_columnIndexOfAwaySetsWon).toInt()
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          _item = TournamentMatch(_tmpId,_tmpGroupName,_tmpPhase,_tmpPhaseLabel,_tmpRoundIndex,_tmpOrderInRound,_tmpHomeTeamId,_tmpAwayTeamId,_tmpHomeSourceMatchId,_tmpAwaySourceMatchId,_tmpHomeScore,_tmpAwayScore,_tmpWinnerTeamId,_tmpStatus,_tmpMatchHistoryId,_tmpStartTimestamp,_tmpEndTimestamp,_tmpMatchFormat,_tmpHomeSetsWon,_tmpAwaySetsWon,_tmpSetScores)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTournamentMatchesByGroupSync(groupName: String): List<TournamentMatch> {
    val _sql: String = "SELECT * FROM tournament_matches WHERE groupName = ? ORDER BY roundIndex ASC, orderInRound ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfPhase: Int = getColumnIndexOrThrow(_stmt, "phase")
        val _columnIndexOfPhaseLabel: Int = getColumnIndexOrThrow(_stmt, "phaseLabel")
        val _columnIndexOfRoundIndex: Int = getColumnIndexOrThrow(_stmt, "roundIndex")
        val _columnIndexOfOrderInRound: Int = getColumnIndexOrThrow(_stmt, "orderInRound")
        val _columnIndexOfHomeTeamId: Int = getColumnIndexOrThrow(_stmt, "homeTeamId")
        val _columnIndexOfAwayTeamId: Int = getColumnIndexOrThrow(_stmt, "awayTeamId")
        val _columnIndexOfHomeSourceMatchId: Int = getColumnIndexOrThrow(_stmt, "homeSourceMatchId")
        val _columnIndexOfAwaySourceMatchId: Int = getColumnIndexOrThrow(_stmt, "awaySourceMatchId")
        val _columnIndexOfHomeScore: Int = getColumnIndexOrThrow(_stmt, "homeScore")
        val _columnIndexOfAwayScore: Int = getColumnIndexOrThrow(_stmt, "awayScore")
        val _columnIndexOfWinnerTeamId: Int = getColumnIndexOrThrow(_stmt, "winnerTeamId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfMatchHistoryId: Int = getColumnIndexOrThrow(_stmt, "matchHistoryId")
        val _columnIndexOfStartTimestamp: Int = getColumnIndexOrThrow(_stmt, "startTimestamp")
        val _columnIndexOfEndTimestamp: Int = getColumnIndexOrThrow(_stmt, "endTimestamp")
        val _columnIndexOfMatchFormat: Int = getColumnIndexOrThrow(_stmt, "matchFormat")
        val _columnIndexOfHomeSetsWon: Int = getColumnIndexOrThrow(_stmt, "homeSetsWon")
        val _columnIndexOfAwaySetsWon: Int = getColumnIndexOrThrow(_stmt, "awaySetsWon")
        val _columnIndexOfSetScores: Int = getColumnIndexOrThrow(_stmt, "setScores")
        val _result: MutableList<TournamentMatch> = mutableListOf()
        while (_stmt.step()) {
          val _item: TournamentMatch
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpPhase: String
          _tmpPhase = _stmt.getText(_columnIndexOfPhase)
          val _tmpPhaseLabel: String?
          if (_stmt.isNull(_columnIndexOfPhaseLabel)) {
            _tmpPhaseLabel = null
          } else {
            _tmpPhaseLabel = _stmt.getText(_columnIndexOfPhaseLabel)
          }
          val _tmpRoundIndex: Int
          _tmpRoundIndex = _stmt.getLong(_columnIndexOfRoundIndex).toInt()
          val _tmpOrderInRound: Int
          _tmpOrderInRound = _stmt.getLong(_columnIndexOfOrderInRound).toInt()
          val _tmpHomeTeamId: Int?
          if (_stmt.isNull(_columnIndexOfHomeTeamId)) {
            _tmpHomeTeamId = null
          } else {
            _tmpHomeTeamId = _stmt.getLong(_columnIndexOfHomeTeamId).toInt()
          }
          val _tmpAwayTeamId: Int?
          if (_stmt.isNull(_columnIndexOfAwayTeamId)) {
            _tmpAwayTeamId = null
          } else {
            _tmpAwayTeamId = _stmt.getLong(_columnIndexOfAwayTeamId).toInt()
          }
          val _tmpHomeSourceMatchId: Int?
          if (_stmt.isNull(_columnIndexOfHomeSourceMatchId)) {
            _tmpHomeSourceMatchId = null
          } else {
            _tmpHomeSourceMatchId = _stmt.getLong(_columnIndexOfHomeSourceMatchId).toInt()
          }
          val _tmpAwaySourceMatchId: Int?
          if (_stmt.isNull(_columnIndexOfAwaySourceMatchId)) {
            _tmpAwaySourceMatchId = null
          } else {
            _tmpAwaySourceMatchId = _stmt.getLong(_columnIndexOfAwaySourceMatchId).toInt()
          }
          val _tmpHomeScore: Int?
          if (_stmt.isNull(_columnIndexOfHomeScore)) {
            _tmpHomeScore = null
          } else {
            _tmpHomeScore = _stmt.getLong(_columnIndexOfHomeScore).toInt()
          }
          val _tmpAwayScore: Int?
          if (_stmt.isNull(_columnIndexOfAwayScore)) {
            _tmpAwayScore = null
          } else {
            _tmpAwayScore = _stmt.getLong(_columnIndexOfAwayScore).toInt()
          }
          val _tmpWinnerTeamId: Int?
          if (_stmt.isNull(_columnIndexOfWinnerTeamId)) {
            _tmpWinnerTeamId = null
          } else {
            _tmpWinnerTeamId = _stmt.getLong(_columnIndexOfWinnerTeamId).toInt()
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpMatchHistoryId: Int?
          if (_stmt.isNull(_columnIndexOfMatchHistoryId)) {
            _tmpMatchHistoryId = null
          } else {
            _tmpMatchHistoryId = _stmt.getLong(_columnIndexOfMatchHistoryId).toInt()
          }
          val _tmpStartTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfStartTimestamp)) {
            _tmpStartTimestamp = null
          } else {
            _tmpStartTimestamp = _stmt.getLong(_columnIndexOfStartTimestamp)
          }
          val _tmpEndTimestamp: Long?
          if (_stmt.isNull(_columnIndexOfEndTimestamp)) {
            _tmpEndTimestamp = null
          } else {
            _tmpEndTimestamp = _stmt.getLong(_columnIndexOfEndTimestamp)
          }
          val _tmpMatchFormat: String?
          if (_stmt.isNull(_columnIndexOfMatchFormat)) {
            _tmpMatchFormat = null
          } else {
            _tmpMatchFormat = _stmt.getText(_columnIndexOfMatchFormat)
          }
          val _tmpHomeSetsWon: Int?
          if (_stmt.isNull(_columnIndexOfHomeSetsWon)) {
            _tmpHomeSetsWon = null
          } else {
            _tmpHomeSetsWon = _stmt.getLong(_columnIndexOfHomeSetsWon).toInt()
          }
          val _tmpAwaySetsWon: Int?
          if (_stmt.isNull(_columnIndexOfAwaySetsWon)) {
            _tmpAwaySetsWon = null
          } else {
            _tmpAwaySetsWon = _stmt.getLong(_columnIndexOfAwaySetsWon).toInt()
          }
          val _tmpSetScores: String?
          if (_stmt.isNull(_columnIndexOfSetScores)) {
            _tmpSetScores = null
          } else {
            _tmpSetScores = _stmt.getText(_columnIndexOfSetScores)
          }
          _item = TournamentMatch(_tmpId,_tmpGroupName,_tmpPhase,_tmpPhaseLabel,_tmpRoundIndex,_tmpOrderInRound,_tmpHomeTeamId,_tmpAwayTeamId,_tmpHomeSourceMatchId,_tmpAwaySourceMatchId,_tmpHomeScore,_tmpAwayScore,_tmpWinnerTeamId,_tmpStatus,_tmpMatchHistoryId,_tmpStartTimestamp,_tmpEndTimestamp,_tmpMatchFormat,_tmpHomeSetsWon,_tmpAwaySetsWon,_tmpSetScores)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getGroupLogsByGroup(groupName: String): Flow<List<GroupLog>> {
    val _sql: String = "SELECT * FROM group_logs WHERE groupName = ? ORDER BY timestamp DESC, id DESC"
    return createFlow(__db, false, arrayOf("group_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfMessage: Int = getColumnIndexOrThrow(_stmt, "message")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfTeamId: Int = getColumnIndexOrThrow(_stmt, "teamId")
        val _columnIndexOfRelatedTeamId: Int = getColumnIndexOrThrow(_stmt, "relatedTeamId")
        val _columnIndexOfMetadata: Int = getColumnIndexOrThrow(_stmt, "metadata")
        val _result: MutableList<GroupLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: GroupLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpMessage: String
          _tmpMessage = _stmt.getText(_columnIndexOfMessage)
          val _tmpPlayerId: Int?
          if (_stmt.isNull(_columnIndexOfPlayerId)) {
            _tmpPlayerId = null
          } else {
            _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          }
          val _tmpTeamId: Int?
          if (_stmt.isNull(_columnIndexOfTeamId)) {
            _tmpTeamId = null
          } else {
            _tmpTeamId = _stmt.getLong(_columnIndexOfTeamId).toInt()
          }
          val _tmpRelatedTeamId: Int?
          if (_stmt.isNull(_columnIndexOfRelatedTeamId)) {
            _tmpRelatedTeamId = null
          } else {
            _tmpRelatedTeamId = _stmt.getLong(_columnIndexOfRelatedTeamId).toInt()
          }
          val _tmpMetadata: String?
          if (_stmt.isNull(_columnIndexOfMetadata)) {
            _tmpMetadata = null
          } else {
            _tmpMetadata = _stmt.getText(_columnIndexOfMetadata)
          }
          _item = GroupLog(_tmpId,_tmpGroupName,_tmpTimestamp,_tmpDate,_tmpType,_tmpMessage,_tmpPlayerId,_tmpTeamId,_tmpRelatedTeamId,_tmpMetadata)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGroupLogsByGroupSync(groupName: String): List<GroupLog> {
    val _sql: String = "SELECT * FROM group_logs WHERE groupName = ? ORDER BY timestamp DESC, id DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfGroupName: Int = getColumnIndexOrThrow(_stmt, "groupName")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfMessage: Int = getColumnIndexOrThrow(_stmt, "message")
        val _columnIndexOfPlayerId: Int = getColumnIndexOrThrow(_stmt, "playerId")
        val _columnIndexOfTeamId: Int = getColumnIndexOrThrow(_stmt, "teamId")
        val _columnIndexOfRelatedTeamId: Int = getColumnIndexOrThrow(_stmt, "relatedTeamId")
        val _columnIndexOfMetadata: Int = getColumnIndexOrThrow(_stmt, "metadata")
        val _result: MutableList<GroupLog> = mutableListOf()
        while (_stmt.step()) {
          val _item: GroupLog
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpGroupName: String
          _tmpGroupName = _stmt.getText(_columnIndexOfGroupName)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpMessage: String
          _tmpMessage = _stmt.getText(_columnIndexOfMessage)
          val _tmpPlayerId: Int?
          if (_stmt.isNull(_columnIndexOfPlayerId)) {
            _tmpPlayerId = null
          } else {
            _tmpPlayerId = _stmt.getLong(_columnIndexOfPlayerId).toInt()
          }
          val _tmpTeamId: Int?
          if (_stmt.isNull(_columnIndexOfTeamId)) {
            _tmpTeamId = null
          } else {
            _tmpTeamId = _stmt.getLong(_columnIndexOfTeamId).toInt()
          }
          val _tmpRelatedTeamId: Int?
          if (_stmt.isNull(_columnIndexOfRelatedTeamId)) {
            _tmpRelatedTeamId = null
          } else {
            _tmpRelatedTeamId = _stmt.getLong(_columnIndexOfRelatedTeamId).toInt()
          }
          val _tmpMetadata: String?
          if (_stmt.isNull(_columnIndexOfMetadata)) {
            _tmpMetadata = null
          } else {
            _tmpMetadata = _stmt.getText(_columnIndexOfMetadata)
          }
          _item = GroupLog(_tmpId,_tmpGroupName,_tmpTimestamp,_tmpDate,_tmpType,_tmpMessage,_tmpPlayerId,_tmpTeamId,_tmpRelatedTeamId,_tmpMetadata)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteEloLogsForMatch(matchHistoryId: Int) {
    val _sql: String = "DELETE FROM elo_logs WHERE matchHistoryId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, matchHistoryId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePlayerGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE players SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateHistoryGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE match_history SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateConfigGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE group_configs SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateEloLogGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE elo_logs SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePlayersByGroup(groupName: String) {
    val _sql: String = "DELETE FROM players WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteHistoryByGroup(groupName: String) {
    val _sql: String = "DELETE FROM match_history WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteConfigByGroup(groupName: String) {
    val _sql: String = "DELETE FROM group_configs WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteEloLogsByGroup(groupName: String) {
    val _sql: String = "DELETE FROM elo_logs WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTournamentTeamGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE tournament_teams SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTournamentTeamMemberGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE tournament_team_members SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTournamentMatchGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE tournament_matches SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateGroupLogGroupNames(oldName: String, newName: String) {
    val _sql: String = "UPDATE group_logs SET groupName = ? WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newName)
        _argIndex = 2
        _stmt.bindText(_argIndex, oldName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTournamentTeamsByGroup(groupName: String) {
    val _sql: String = "DELETE FROM tournament_teams WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTournamentTeamMembersByGroup(groupName: String) {
    val _sql: String = "DELETE FROM tournament_team_members WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTournamentMatchesByGroup(groupName: String) {
    val _sql: String = "DELETE FROM tournament_matches WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteGroupLogsByGroup(groupName: String) {
    val _sql: String = "DELETE FROM group_logs WHERE groupName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, groupName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

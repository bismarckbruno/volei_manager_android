package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_history",
    indices = [Index(value = ["groupName", "id"])]
)
data class MatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val teamA: String,
    val teamB: String,
    val teamAIds: String = "",
    val teamBIds: String = "",
    val winner: String,
    val eloPoints: Double,
    val groupName: String,
    val teamAAverageElo: Double? = null,
    val teamBAverageElo: Double? = null,
    val teamAScore: Int? = null,
    val teamBScore: Int? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    /** Id do time de campeonato (tournament_teams) que jogou como Time A, quando aplicável. */
    val teamAId: Int? = null,
    /** Id do time de campeonato (tournament_teams) que jogou como Time B, quando aplicável. */
    val teamBId: Int? = null,
    /** Nome exibido do time A no momento da partida (snapshot). */
    val teamALabel: String? = null,
    /** Nome exibido do time B no momento da partida (snapshot). */
    val teamBLabel: String? = null
)

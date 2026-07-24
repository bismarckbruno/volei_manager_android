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
    val endTimestamp: Long? = null
)

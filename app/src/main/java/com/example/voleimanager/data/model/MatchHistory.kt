package com.example.voleimanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val teamA: String,
    val teamB: String,
    val winner: String,
    val eloPoints: Double,
    val groupName: String
)
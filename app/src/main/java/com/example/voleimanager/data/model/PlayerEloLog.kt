package com.example.voleimanager.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "elo_logs",
    indices = [Index(value = ["playerId"])]
)
data class PlayerEloLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val playerNameSnapshot: String,
    val date: String,
    val elo: Double,
    val groupName: String
)
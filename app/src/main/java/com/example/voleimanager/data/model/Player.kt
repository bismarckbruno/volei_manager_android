package com.example.voleimanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val elo: Double = 1200.0,
    val matchesPlayed: Int = 0,
    val victories: Int = 0,
    val isSetter: Boolean = false,
    val groupName: String
)
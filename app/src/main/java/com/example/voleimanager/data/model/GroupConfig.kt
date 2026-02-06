package com.example.voleimanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_configs")
data class GroupConfig(
    @PrimaryKey val groupName: String,
    val teamSize: Int = 6, // Padrão 6
    val victoryLimit: Int = 3, // Padrão 3
    val genderPriorityEnabled: Boolean = true
)
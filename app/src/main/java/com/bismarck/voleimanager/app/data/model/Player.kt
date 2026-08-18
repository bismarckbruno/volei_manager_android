package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    indices = [Index(value = ["groupName", "elo"])]
)
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val elo: Double = 1200.0,
    val matchesPlayed: Int = 0,
    val victories: Int = 0,
    val isPriority: Boolean = false,
    val groupName: String,
    val dailyToll: Int = 0,
    val tollDate: String = "",
    /**
     * Posição preferida ([PlayerPosition]). Nulo = sem preferência.
     * Jogador sem nenhuma posição indicada é considerado coringa.
     */
    val preferredPosition: String? = null,
    /** Segunda posição preferida ([PlayerPosition]); usada quando não há vaga na preferida. */
    val secondaryPosition: String? = null
)

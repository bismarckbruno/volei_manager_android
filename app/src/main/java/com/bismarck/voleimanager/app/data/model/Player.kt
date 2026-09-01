package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    indices = [
        Index(value = ["groupName", "elo"]),
        Index(value = ["publicId"], unique = true)
    ]
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
    val secondaryPosition: String? = null,
    /**
     * Identidade estável e imutável do jogador (UUID gerado uma única vez na criação).
     * Independe do [id] local (autoGenerate) e sobrevive a rename/edição — preparação de terreno
     * para uma futura sincronização em nuvem (ex.: vincular o jogador a uma conta/dispositivo).
     */
    val publicId: String = java.util.UUID.randomUUID().toString()
)

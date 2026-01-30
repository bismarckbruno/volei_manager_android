package com.example.voleimanager.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elo_logs",
    primaryKeys = ["playerId", "date"],
    // REMOVEMOS a ForeignKey com CASCADE.
    // Assim, se o jogador for deletado, este registro PERMANECE no banco.
    indices = [Index(value = ["playerId"])]
)
data class PlayerEloLog(
    val playerId: Int,
    val playerNameSnapshot: String, // Adicionei o nome aqui para sabermos quem era, mesmo se o jogador for deletado
    val date: String,
    val elo: Double,
    val groupName: String
)
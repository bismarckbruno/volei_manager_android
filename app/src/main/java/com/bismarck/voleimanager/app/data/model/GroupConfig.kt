package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BalancingMode { REBALANCE, WINNER_RESTS, BOTH_REST }

@Entity(tableName = "group_configs")
data class GroupConfig(
    @PrimaryKey val groupName: String,
    val teamSize: Int = 6, // Padrão 6
    val victoryLimit: Int = 3, // Padrão 3
    val priorityEnabled: Boolean = true,
    val scoreEnabled: Boolean = true, // Exibir e registrar placar nas partidas
    val balancingMode: String = BalancingMode.REBALANCE.name // "REBALANCE" | "WINNER_RESTS" | "BOTH_REST"
)

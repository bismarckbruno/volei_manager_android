package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BalancingMode { REBALANCE, WINNER_RESTS, BOTH_REST }

const val ONBOARDING_STEP_TEAM_SIZE = 0
const val ONBOARDING_STEP_MIN_PLAYERS = 1
const val ONBOARDING_STEP_COMPLETE = 2

@Entity(tableName = "group_configs")
data class GroupConfig(
    @PrimaryKey val groupName: String,
    val teamSize: Int = 6, // Padrão 6
    val victoryLimit: Int = 3, // Padrão 3
    val priorityEnabled: Boolean = true,
    val scoreEnabled: Boolean = true, // Exibir e registrar placar nas partidas
    val balancingMode: String = BalancingMode.REBALANCE.name, // "REBALANCE" | "WINNER_RESTS" | "BOTH_REST"
    val onboardingStep: Int = ONBOARDING_STEP_TEAM_SIZE
)

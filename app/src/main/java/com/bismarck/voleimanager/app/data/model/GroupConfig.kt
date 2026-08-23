package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BalancingMode {
    REBALANCE,
    REST;

    companion object {
        fun fromStoredValue(value: String?): BalancingMode {
            return when (value) {
                REST.name,
                "BOTH_REST",
                "WINNER_RESTS" -> REST
                else -> REBALANCE
            }
        }
    }
}

const val ONBOARDING_STEP_GROUP_NAME = 0
const val ONBOARDING_STEP_GROUP_TYPE = 1
const val ONBOARDING_STEP_BALANCING_MODE = 2
const val ONBOARDING_STEP_TEAM_SIZE = 3
const val ONBOARDING_STEP_MIN_PLAYERS = 4
const val ONBOARDING_STEP_COMPLETE = 5

@Entity(tableName = "group_configs")
data class GroupConfig(
    @PrimaryKey val groupName: String,
    val teamSize: Int = 6, // Padrão 6
    val victoryLimit: Int = 3, // Padrão 3
    val priorityEnabled: Boolean = true,
    val scoreEnabled: Boolean = true, // Exibir e registrar placar nas partidas
    val balancingMode: String = BalancingMode.REBALANCE.name, // "REBALANCE" | "REST"
    val onboardingStep: Int = ONBOARDING_STEP_GROUP_NAME,
    /** Tipo de funcionamento do grupo ([GroupType]). Imutável nos tipos de campeonato. */
    val groupType: String = GroupType.RECREATIONAL.name,
    /** Formato do chaveamento ([TournamentFormat]); nulo fora dos tipos de campeonato. */
    val tournamentFormat: String? = null,
    /** Indica que o campeonato saiu da primeira fase e não aceita mais novos times. */
    val tournamentStarted: Boolean = false,
    /**
     * Nos tipos com posições fixas, garante que a vaga de levantador seja preenchida antes das
     * demais. Desligado, essa vaga vira uma vaga de ataque. Irrelevante nos tipos recreativos.
     */
    val guaranteeSetter: Boolean = true
) {
    val type: GroupType
        get() = GroupType.fromStoredValue(groupType)
}

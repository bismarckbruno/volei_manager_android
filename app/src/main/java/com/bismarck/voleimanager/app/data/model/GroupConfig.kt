package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
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

/**
 * Formato de disputa da partida (número de sets). O Elo é sempre calculado 1x por partida
 * completa, nunca por set individual, independente do formato.
 */
enum class MatchFormat {
    BO1,
    BO3,
    BO5;

    /** Sets necessários para vencer a partida neste formato. */
    val setsToWin: Int
        get() = when (this) {
            BO1 -> 1
            BO3 -> 2
            BO5 -> 3
        }

    companion object {
        fun fromStoredValue(value: String?): MatchFormat {
            return entries.firstOrNull { it.name == value } ?: BO1
        }
    }
}

const val ONBOARDING_STEP_GROUP_NAME = 0
const val ONBOARDING_STEP_GROUP_TYPE = 1
const val ONBOARDING_STEP_BALANCING_MODE = 2
const val ONBOARDING_STEP_TEAM_SIZE = 3
const val ONBOARDING_STEP_MIN_PLAYERS = 4
const val ONBOARDING_STEP_COMPLETE = 5

@Entity(
    tableName = "group_configs",
    indices = [Index(value = ["publicId"], unique = true)]
)
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
    val guaranteeSetter: Boolean = true,
    /**
     * Formato de disputa da partida ([MatchFormat]): BO1 (padrão atual), BO3 ou BO5. Preparação
     * de terreno para partidas com múltiplos sets — ainda não implementado na engine do jogo.
     */
    val matchFormat: String = MatchFormat.BO1.name,
    /** Pontos necessários para fechar um set regular (não decisivo). */
    val regularSetPoints: Int = 25,
    /** Pontos necessários para fechar o set decisivo (tie-break) em formatos BO3/BO5. */
    val tiebreakSetPoints: Int = 15,
    /** Exige diferença mínima de 2 pontos para fechar o set. */
    val winByTwo: Boolean = true,
    /**
     * Identidade estável e imutável do grupo (UUID gerado uma única vez na criação).
     * Independe de [groupName] (editável pelo usuário, cascateado manualmente em renames) —
     * preparação de terreno para uma futura referência estável de "grupo na nuvem".
     */
    val publicId: String = java.util.UUID.randomUUID().toString()
) {
    val type: GroupType
        get() = GroupType.fromStoredValue(groupType)

    val format: MatchFormat
        get() = MatchFormat.fromStoredValue(matchFormat)
}

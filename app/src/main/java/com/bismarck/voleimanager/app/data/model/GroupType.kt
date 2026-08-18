package com.bismarck.voleimanager.app.data.model

/**
 * Tipo de funcionamento do grupo. Não confundir com [BalancingMode], que define o modo de
 * balanceamento (rebalancear / descansar) usado apenas pelos tipos não-campeonato.
 */
enum class GroupType {
    /** Recreativo padrão: dois times genéricos, sem posições fixas. */
    RECREATIONAL,

    /** Posições fixas (com ou sem líbero): dois times genéricos, jogadores com posições preferidas. */
    FIXED_POSITIONS,

    /** Campeonato recreativo: múltiplos times nomeados, sem posições fixas. */
    TOURNAMENT_RECREATIONAL,

    /** Campeonato profissional: múltiplos times nomeados, com posições fixas e líbero. */
    TOURNAMENT_PRO;

    val isTournament: Boolean
        get() = this == TOURNAMENT_RECREATIONAL || this == TOURNAMENT_PRO

    val usesPositions: Boolean
        get() = this == FIXED_POSITIONS || this == TOURNAMENT_PRO

    val minTeamSize: Int
        get() = 2

    val maxTeamSize: Int
        get() = when (this) {
            RECREATIONAL -> 6
            FIXED_POSITIONS -> 7
            TOURNAMENT_RECREATIONAL, TOURNAMENT_PRO -> 14
        }

    val teamSizeRange: IntRange
        get() = minTeamSize..maxTeamSize

    /** Campeonatos não têm rebalanceamento nem descanso. */
    val supportsBalancingMode: Boolean
        get() = !isTournament

    /** Prioridade ("distribuir por igual") existe em todos, exceto no campeonato profissional. */
    val supportsPriority: Boolean
        get() = this != TOURNAMENT_PRO

    /** Garantir jogador na próxima partida só faz sentido fora dos campeonatos. */
    val supportsGuaranteedNextMatch: Boolean
        get() = !isTournament

    /** Times com nome personalizado (armazenados apenas como sufixo: "A", "B", "C"...). */
    val supportsCustomTeamNames: Boolean
        get() = isTournament

    /** Grupos de campeonato nunca podem ser convertidos em outro tipo. */
    val isImmutable: Boolean
        get() = isTournament

    /** Só é possível converter livremente entre RECREATIONAL e FIXED_POSITIONS. */
    fun canConvertTo(target: GroupType): Boolean {
        if (target == this) return true
        if (isImmutable || target.isImmutable) return false
        return true
    }

    fun coerceTeamSize(teamSize: Int): Int = teamSize.coerceIn(minTeamSize, maxTeamSize)

    companion object {
        fun fromStoredValue(value: String?): GroupType {
            return entries.firstOrNull { it.name == value } ?: RECREATIONAL
        }
    }
}

/** Formatos de chaveamento suportados pelos tipos de campeonato. */
enum class TournamentFormat {
    KNOCKOUT,
    ROUND_ROBIN,
    GROUPS_KNOCKOUT;

    companion object {
        fun fromStoredValue(value: String?): TournamentFormat? =
            entries.firstOrNull { it.name == value }
    }
}

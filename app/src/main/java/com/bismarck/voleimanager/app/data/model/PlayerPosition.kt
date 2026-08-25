package com.bismarck.voleimanager.app.data.model

/** Papel em quadra derivado da posição. */
enum class PositionRole {
    /** Armador (levantador). */
    PLAYMAKER,

    /** Ataque (ponteiro e oposto). */
    ATTACK,

    /** Defesa (central e líbero). */
    DEFENSE
}

/** Posições de quadra usadas pelos tipos de grupo com posições fixas. */
enum class PlayerPosition(val role: PositionRole) {
    /** Levantador. */
    SETTER(PositionRole.PLAYMAKER),

    /** Ponteiro. */
    OUTSIDE_HITTER(PositionRole.ATTACK),

    /** Central. */
    MIDDLE_BLOCKER(PositionRole.DEFENSE),

    /** Oposto. */
    OPPOSITE(PositionRole.ATTACK),

    /** Líbero. Em times de 2 a 5 jogadores é tratado como central. */
    LIBERO(PositionRole.DEFENSE);

    companion object {
        fun fromStoredValue(value: String?): PlayerPosition? =
            entries.firstOrNull { it.name == value }
    }
}

/**
 * Vaga exigida na composição de um time.
 *
 * Quando [position] é nulo, qualquer jogador cujo [PlayerPosition.role] seja [role] preenche a vaga.
 * Quando [position] é preenchido, a vaga é específica e só aceita [position] ou [fallbackPosition].
 */
data class TeamSlot(
    val role: PositionRole,
    val position: PlayerPosition? = null,
    val fallbackPosition: PlayerPosition? = null,
    val acceptedPositions: Set<PlayerPosition>? = null,
    val assignedPositionOverride: PlayerPosition? = null
) {
    fun accepts(playerPosition: PlayerPosition?): Boolean {
        if (playerPosition == null) return false
        acceptedPositions?.let { return playerPosition in it }
        if (position == null) return playerPosition.role == role
        return playerPosition == position || playerPosition == fallbackPosition
    }
}

/**
 * Composição mínima de um time por número de jogadores, no Modo Posições Fixas.
 *
 * De 2 a 5 jogadores a exigência é por papel (o líbero conta como central/defesa).
 * Com 6 ou 7 jogadores as vagas são por posição, e o líbero tem vaga garantida — substituída por
 * um central quando não houver líbero disponível.
 */
object TeamComposition {

    const val MIN_TEAM_SIZE = 2
    const val MAX_TEAM_SIZE = 7

    fun requiredSlots(
        teamSize: Int,
        guaranteeSetter: Boolean = true
    ): List<TeamSlot> {
        val attackCore = TeamSlot(
            role = PositionRole.ATTACK,
            acceptedPositions = setOf(PlayerPosition.OUTSIDE_HITTER, PlayerPosition.OPPOSITE)
        )
        val attackFlex = TeamSlot(
            role = PositionRole.ATTACK,
            acceptedPositions = setOf(
                PlayerPosition.SETTER,
                PlayerPosition.OUTSIDE_HITTER,
                PlayerPosition.OPPOSITE
            )
        )
        val defenseFlex = TeamSlot(
            role = PositionRole.DEFENSE,
            acceptedPositions = setOf(
                PlayerPosition.OUTSIDE_HITTER,
                PlayerPosition.MIDDLE_BLOCKER,
                PlayerPosition.LIBERO
            )
        )
        val defenseCore = TeamSlot(
            role = PositionRole.DEFENSE,
            acceptedPositions = setOf(PlayerPosition.MIDDLE_BLOCKER, PlayerPosition.LIBERO)
        )
        // Vaga livre: aceita qualquer posição que não seja levantador. Usada no 2x2 com
        // "garantir levantador" ativado, já que a dupla do levantador pode ser qualquer outra posição.
        val anyNonSetter = TeamSlot(
            role = PositionRole.ATTACK,
            acceptedPositions = setOf(
                PlayerPosition.OUTSIDE_HITTER,
                PlayerPosition.OPPOSITE,
                PlayerPosition.MIDDLE_BLOCKER,
                PlayerPosition.LIBERO
            )
        )
        val leadingSlot = if (guaranteeSetter) {
            TeamSlot(PositionRole.PLAYMAKER, PlayerPosition.SETTER)
        } else {
            attackFlex
        }

        return when (teamSize.coerceIn(MIN_TEAM_SIZE, MAX_TEAM_SIZE)) {
            2 -> listOf(
                leadingSlot,
                if (guaranteeSetter) anyNonSetter else defenseCore
            )

            3 -> listOf(
                leadingSlot,
                attackCore,
                defenseCore
            )

            4 -> listOf(
                leadingSlot,
                attackCore,
                defenseFlex,
                defenseCore
            )

            5 -> listOf(
                leadingSlot,
                attackCore,
                attackCore,
                defenseFlex,
                defenseCore
            )

            6 -> listOf(
                leadingSlot,
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
                TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OPPOSITE),
                TeamSlot(PositionRole.DEFENSE, PlayerPosition.LIBERO, PlayerPosition.MIDDLE_BLOCKER)
            )

            else -> listOf(
                leadingSlot,
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
                TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
                TeamSlot(PositionRole.ATTACK, PlayerPosition.OPPOSITE),
                TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
                TeamSlot(
                    role = PositionRole.DEFENSE,
                    position = PlayerPosition.LIBERO,
                    fallbackPosition = PlayerPosition.MIDDLE_BLOCKER,
                    assignedPositionOverride = PlayerPosition.LIBERO
                )
            )
        }
    }

    /** Em times de 2 a 5 jogadores o líbero é tratado como central. */
    fun effectivePosition(position: PlayerPosition?, teamSize: Int): PlayerPosition? {
        if (position == PlayerPosition.LIBERO && teamSize < 6) return PlayerPosition.MIDDLE_BLOCKER
        return position
    }

    /** Há líbero disponível quando algum jogador presente o tem como posição preferida. */
    fun hasLibero(presentPlayers: List<Player>): Boolean = presentPlayers.any {
        PlayerPosition.fromStoredValue(it.preferredPosition) == PlayerPosition.LIBERO
    }

    /** Jogador coringa: não indicou nenhuma posição preferida. */
    fun isWildcard(player: Player): Boolean =
        PlayerPosition.fromStoredValue(player.preferredPosition) == null &&
            PlayerPosition.fromStoredValue(player.secondaryPosition) == null
}

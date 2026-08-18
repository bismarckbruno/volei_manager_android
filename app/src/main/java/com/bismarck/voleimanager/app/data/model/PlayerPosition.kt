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
    val fallbackPosition: PlayerPosition? = null
) {
    fun accepts(playerPosition: PlayerPosition?): Boolean {
        if (playerPosition == null) return false
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

    fun requiredSlots(teamSize: Int): List<TeamSlot> = when (teamSize.coerceIn(MIN_TEAM_SIZE, MAX_TEAM_SIZE)) {
        2 -> listOf(
            TeamSlot(PositionRole.PLAYMAKER),
            TeamSlot(PositionRole.ATTACK)
        )

        3 -> listOf(
            TeamSlot(PositionRole.PLAYMAKER),
            TeamSlot(PositionRole.ATTACK),
            TeamSlot(PositionRole.DEFENSE)
        )

        4 -> listOf(
            TeamSlot(PositionRole.PLAYMAKER),
            TeamSlot(PositionRole.ATTACK),
            TeamSlot(PositionRole.ATTACK),
            TeamSlot(PositionRole.DEFENSE)
        )

        5 -> listOf(
            TeamSlot(PositionRole.PLAYMAKER),
            TeamSlot(PositionRole.ATTACK),
            TeamSlot(PositionRole.ATTACK),
            TeamSlot(PositionRole.DEFENSE),
            TeamSlot(PositionRole.DEFENSE)
        )

        6 -> listOf(
            TeamSlot(PositionRole.PLAYMAKER, PlayerPosition.SETTER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
            TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OPPOSITE),
            TeamSlot(PositionRole.DEFENSE, PlayerPosition.LIBERO, PlayerPosition.MIDDLE_BLOCKER)
        )

        else -> listOf(
            TeamSlot(PositionRole.PLAYMAKER, PlayerPosition.SETTER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OUTSIDE_HITTER),
            TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
            TeamSlot(PositionRole.DEFENSE, PlayerPosition.MIDDLE_BLOCKER),
            TeamSlot(PositionRole.ATTACK, PlayerPosition.OPPOSITE),
            TeamSlot(PositionRole.DEFENSE, PlayerPosition.LIBERO, PlayerPosition.MIDDLE_BLOCKER)
        )
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

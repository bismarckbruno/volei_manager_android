package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.TeamComposition
import com.bismarck.voleimanager.app.data.model.TeamSlot
import kotlin.math.abs

/**
 * Distribuição de jogadores por posição no Modo Posições Fixas.
 *
 * Estratégia: **posições primeiro, Elo depois**. As vagas obrigatórias de cada time são
 * preenchidas respeitando a posição preferida e, na falta de vaga, a segunda preferida. Entre os
 * candidatos elegíveis para uma mesma vaga, escolhe-se aquele que melhor equilibra o Elo dos times.
 */
object PositionAssigner {

    /** Vaga de um time com o jogador alocado (ou `null` quando ninguém a preencheu). */
    data class FilledSlot(
        val slot: TeamSlot,
        val player: Player?,
        /** `true` quando a vaga foi preenchida por alguém fora da posição exigida. */
        val isImprovised: Boolean = false
    )

    data class TeamAssignment(
        val players: List<Player>,
        val positions: Map<Int, PlayerPosition>,
        val slots: List<FilledSlot>
    ) {
        val missingSlots: List<TeamSlot>
            get() = slots.filter { it.player == null || it.isImprovised }.map { it.slot }

        val isComplete: Boolean
            get() = missingSlots.isEmpty()
    }

    data class PositionAssignment(
        val teamA: List<Player>,
        val teamB: List<Player>,
        val positions: Map<Int, PlayerPosition>,
        val unfilledSlots: List<TeamSlot>
    ) {
        val isComplete: Boolean
            get() = unfilledSlots.isEmpty()
    }

    private const val TIER_PREFERRED = 0
    private const val TIER_SECONDARY = 1
    private const val TIER_WILDCARD = 2
    private const val TIER_ANY = 3

    /**
     * Monta dois times de [teamSize] jogadores a partir de [pool], cumprindo a composição mínima
     * e equilibrando o Elo. Jogadores excedentes não entram em nenhum time e devem ser tratados
     * pelo chamador (fila de espera).
     */
    fun buildBalancedTeams(pool: List<Player>, teamSize: Int): PositionAssignment {
        val hasLibero = TeamComposition.hasLibero(pool)
        val slotsA = requiredSlots(teamSize, hasLibero)
        val slotsB = requiredSlots(teamSize, hasLibero)

        val remaining = pool.toMutableList()
        val teamA = mutableListOf<Player>()
        val teamB = mutableListOf<Player>()
        val positions = mutableMapOf<Int, PlayerPosition>()
        val unfilled = mutableListOf<TeamSlot>()

        // Vagas dos dois times entrelaçadas, da mais restritiva para a menos restritiva: assim uma
        // vaga com poucos candidatos é resolvida antes que um coringa a ocupe desnecessariamente.
        val pendingSlots = (slotsA.map { it to teamA } + slotsB.map { it to teamB })
            .sortedBy { (slot, _) -> candidateCount(slot, pool, teamSize) }

        pendingSlots.forEach { (slot, team) ->
            if (team.size >= teamSize) return@forEach
            val other = if (team === teamA) teamB else teamA
            val choice = pickCandidate(slot, remaining, team, other, teamSize)
            if (choice == null) {
                unfilled.add(slot)
                return@forEach
            }
            remaining.remove(choice.player)
            team.add(choice.player)
            positions[choice.player.id] = choice.position
            if (choice.tier > TIER_SECONDARY) unfilled.add(slot)
        }

        // Sobra defensiva: se alguma vaga ficou vazia por falta de jogadores elegíveis na etapa
        // acima, completa com quem restou para não devolver times incompletos.
        while ((teamA.size < teamSize || teamB.size < teamSize) && remaining.isNotEmpty()) {
            val team = if (teamA.size < teamSize && (teamB.size >= teamSize || eloOf(teamA) <= eloOf(teamB))) {
                teamA
            } else {
                teamB
            }
            val player = remaining.removeAt(0)
            team.add(player)
            positions[player.id] = TeamComposition
                .effectivePosition(PlayerPosition.fromStoredValue(player.preferredPosition), teamSize)
                ?: PlayerPosition.OUTSIDE_HITTER
        }

        return PositionAssignment(
            teamA = teamA,
            teamB = teamB,
            positions = positions,
            unfilledSlots = unfilled
        )
    }

    /**
     * Recalcula as posições de um time já formado, sem remontá-lo. Usado após substituições,
     * trocas de lado e rebalanceamentos.
     */
    fun assignPositionsToExistingTeam(team: List<Player>, teamSize: Int): TeamAssignment {
        val effectiveSize = maxOf(teamSize, team.size).coerceAtMost(TeamComposition.MAX_TEAM_SIZE)
        val hasLibero = TeamComposition.hasLibero(team)
        val slots = requiredSlots(effectiveSize, hasLibero)

        val remaining = team.toMutableList()
        val positions = mutableMapOf<Int, PlayerPosition>()
        val filled = mutableListOf<FilledSlot>()

        slots.sortedBy { candidateCount(it, team, effectiveSize) }.forEach { slot ->
            val choice = pickForSlot(slot, remaining, effectiveSize)
            if (choice == null) {
                filled.add(FilledSlot(slot, null))
                return@forEach
            }
            remaining.remove(choice.player)
            positions[choice.player.id] = choice.position
            filled.add(FilledSlot(slot, choice.player, isImprovised = choice.tier > TIER_SECONDARY))
        }

        // Jogadores além da composição mínima mantêm a posição preferida, quando houver.
        remaining.forEach { player ->
            positions[player.id] = TeamComposition
                .effectivePosition(PlayerPosition.fromStoredValue(player.preferredPosition), effectiveSize)
                ?: PlayerPosition.OUTSIDE_HITTER
        }

        return TeamAssignment(players = team, positions = positions, slots = filled)
    }

    /** Composição de um time para exibição (vagas preenchidas e faltando). */
    fun describeComposition(team: List<Player>, teamSize: Int): List<FilledSlot> =
        assignPositionsToExistingTeam(team, teamSize).slots

    /**
     * Escolhe [count] jogadores de [pool] priorizando os que cobrem as vagas ainda descobertas
     * considerando [teams] times de [teamSize] jogadores, tendo [base] como já escalados.
     *
     * A ordem de [pool] é a ordem de justiça definida pelo chamador (menos jogos primeiro): entre
     * candidatos que cobrem a mesma vaga, o primeiro do pool vence. Quando nenhuma vaga descoberta
     * tem candidato, cai para o próximo jogador do pool.
     *
     * @return os escolhidos e o pool restante, ambos preservando a ordem original.
     */
    fun pickToCoverComposition(
        base: List<Player>,
        pool: List<Player>,
        count: Int,
        teamSize: Int,
        teams: Int = 2
    ): Pair<List<Player>, List<Player>> {
        if (count <= 0 || pool.isEmpty()) return emptyList<Player>() to pool
        val hasLibero = TeamComposition.hasLibero(base + pool)
        val needs = (1..teams)
            .flatMap { requiredSlots(teamSize, hasLibero) }
            .toMutableList()

        // Vagas já cobertas por quem está escalado saem da lista de necessidades.
        base.forEach { player -> consumeSlotFor(needs, player, teamSize) }

        val remaining = pool.toMutableList()
        val picked = mutableListOf<Player>()
        repeat(count) {
            if (remaining.isEmpty()) return@repeat
            val candidate = needs
                .sortedBy { candidateCount(it, remaining, teamSize) }
                .firstNotNullOfOrNull { slot ->
                    remaining.firstOrNull { player ->
                        slot.accepts(effectivePreferred(player, teamSize)) ||
                            slot.accepts(effectiveSecondary(player, teamSize))
                    }
                }
                ?: remaining.first()
            remaining.remove(candidate)
            picked.add(candidate)
            consumeSlotFor(needs, candidate, teamSize)
        }
        return picked to remaining
    }

    /** Remove de [needs] a vaga mais específica que [player] consegue cobrir. */
    private fun consumeSlotFor(needs: MutableList<TeamSlot>, player: Player, teamSize: Int) {
        val preferred = effectivePreferred(player, teamSize)
        val secondary = effectiveSecondary(player, teamSize)
        val preferredIndex = needs.indexOfFirst { it.accepts(preferred) }
        val index = if (preferredIndex >= 0) preferredIndex else needs.indexOfFirst { it.accepts(secondary) }
        if (index >= 0) needs.removeAt(index)
    }

    // --- internos ---

    private data class Choice(val player: Player, val position: PlayerPosition, val tier: Int)

    private fun requiredSlots(teamSize: Int, hasLibero: Boolean): List<TeamSlot> {
        val slots = TeamComposition.requiredSlots(teamSize)
        if (hasLibero) return slots
        // Sem líbero presente, a vaga garantida do líbero é ocupada por outro central.
        return slots.map { slot ->
            if (slot.position == PlayerPosition.LIBERO) {
                TeamSlot(slot.role, PlayerPosition.MIDDLE_BLOCKER)
            } else {
                slot
            }
        }
    }

    private fun eloOf(team: List<Player>): Double = team.sumOf { it.elo }

    /** Número de jogadores que preenchem a vaga na posição preferida ou secundária. */
    private fun candidateCount(slot: TeamSlot, pool: List<Player>, teamSize: Int): Int =
        pool.count { player ->
            slot.accepts(effectivePreferred(player, teamSize)) ||
                slot.accepts(effectiveSecondary(player, teamSize))
        }

    private fun effectivePreferred(player: Player, teamSize: Int): PlayerPosition? =
        TeamComposition.effectivePosition(
            PlayerPosition.fromStoredValue(player.preferredPosition),
            teamSize
        )

    private fun effectiveSecondary(player: Player, teamSize: Int): PlayerPosition? =
        TeamComposition.effectivePosition(
            PlayerPosition.fromStoredValue(player.secondaryPosition),
            teamSize
        )

    /**
     * Camada do jogador para a vaga: quanto menor, melhor o encaixe.
     * `null` significa que o jogador não pode ocupar a vaga nesta rodada de escolha.
     */
    private fun tierFor(slot: TeamSlot, player: Player, teamSize: Int): Pair<Int, PlayerPosition>? {
        val preferred = effectivePreferred(player, teamSize)
        if (slot.accepts(preferred)) return TIER_PREFERRED to slotPosition(slot, preferred)

        val secondary = effectiveSecondary(player, teamSize)
        if (slot.accepts(secondary)) return TIER_SECONDARY to slotPosition(slot, secondary)

        if (TeamComposition.isWildcard(player)) return TIER_WILDCARD to fallbackPosition(slot)

        return TIER_ANY to fallbackPosition(slot)
    }

    private fun slotPosition(slot: TeamSlot, matched: PlayerPosition?): PlayerPosition =
        matched ?: fallbackPosition(slot)

    private fun fallbackPosition(slot: TeamSlot): PlayerPosition =
        slot.position ?: defaultPositionForRole(slot)

    private fun defaultPositionForRole(slot: TeamSlot): PlayerPosition =
        PlayerPosition.entries.first { it.role == slot.role }

    /**
     * Escolhe o jogador para uma vaga: primeiro pela melhor camada disponível e, dentro dela,
     * pelo Elo que melhor equilibra os dois times.
     */
    private fun pickCandidate(
        slot: TeamSlot,
        remaining: List<Player>,
        team: List<Player>,
        other: List<Player>,
        teamSize: Int
    ): Choice? {
        val candidates = remaining.mapNotNull { player ->
            tierFor(slot, player, teamSize)?.let { (tier, position) -> Choice(player, position, tier) }
        }
        if (candidates.isEmpty()) return null

        val bestTier = candidates.minOf { it.tier }
        val eligible = candidates.filter { it.tier == bestTier }

        // O time com menor Elo acumulado recebe o candidato mais forte da camada, e vice-versa,
        // mantendo as somas próximas ao longo da montagem.
        val teamElo = eloOf(team)
        val otherElo = eloOf(other)
        return if (teamElo <= otherElo) {
            eligible.maxByOrNull { it.player.elo }
        } else {
            eligible.minByOrNull { it.player.elo }
        }
    }

    /** Variante de [pickCandidate] para um único time (sem comparação de Elo entre lados). */
    private fun pickForSlot(slot: TeamSlot, remaining: List<Player>, teamSize: Int): Choice? {
        val candidates = remaining.mapNotNull { player ->
            tierFor(slot, player, teamSize)?.let { (tier, position) -> Choice(player, position, tier) }
        }
        if (candidates.isEmpty()) return null
        val bestTier = candidates.minOf { it.tier }
        return candidates.filter { it.tier == bestTier }.maxByOrNull { it.player.elo }
    }

    /** Diferença absoluta de Elo entre os times, para diagnósticos e testes. */
    fun eloGap(teamA: List<Player>, teamB: List<Player>): Double = abs(eloOf(teamA) - eloOf(teamB))
}

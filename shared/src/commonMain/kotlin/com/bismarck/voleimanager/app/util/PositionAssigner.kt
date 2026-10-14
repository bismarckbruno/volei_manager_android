package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.PositionRole
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
        /** Vagas vazias ou preenchidas por encaixe imperfeito (usado só para exibição detalhada). */
        val missingSlots: List<TeamSlot>
            get() = slots.filter { it.player == null || it.isImprovised }.map { it.slot }

        /**
         * `false` apenas quando alguma vaga foi preenchida por encaixe imperfeito (coringa ou
         * realocação). Uma vaga vazia (time ainda incompleto por falta de gente na fila) não conta
         * aqui, pois não há indicador de posição incorreto a mostrar nesse caso.
         */
        val isComplete: Boolean
            get() = slots.none { it.isImprovised }
    }

    data class PositionAssignment(
        val teamA: List<Player>,
        val teamB: List<Player>,
        val positions: Map<Int, PlayerPosition>,
        val unfilledSlots: List<TeamSlot>,
        /** Vagas preenchidas por encaixe imperfeito (coringa ou realocação), subconjunto de [unfilledSlots]. */
        val improvisedSlots: List<TeamSlot> = emptyList()
    ) {
        /**
         * `false` apenas quando alguma vaga foi preenchida por encaixe imperfeito. Uma vaga vazia
         * (sem gente suficiente na fila) não conta aqui — não há indicador de posição incorreto a
         * mostrar nesse caso.
         */
        val isComplete: Boolean
            get() = improvisedSlots.isEmpty()
    }

    private const val TIER_PREFERRED = 0
    private const val TIER_SECONDARY = 1
    private const val TIER_WILDCARD = 2
    private const val TIER_ATTACK = 3
    private const val TIER_DEFENSE = 4

    /**
     * Monta dois times de [teamSize] jogadores a partir de [pool], cumprindo a composição mínima
     * e equilibrando o Elo. Jogadores excedentes não entram em nenhum time e devem ser tratados
     * pelo chamador (fila de espera).
     */
    fun buildBalancedTeams(
        pool: List<Player>,
        teamSize: Int,
        guaranteeSetter: Boolean = true
    ): PositionAssignment {
        val hasLibero = TeamComposition.hasLibero(pool)
        val slotsA = requiredSlots(teamSize, hasLibero, guaranteeSetter)
        val slotsB = requiredSlots(teamSize, hasLibero, guaranteeSetter)

        val remaining = pool.toMutableList()
        val teamA = mutableListOf<Player>()
        val teamB = mutableListOf<Player>()
        val positions = mutableMapOf<Int, PlayerPosition>()
        val unfilled = mutableListOf<TeamSlot>()
        val improvised = mutableListOf<TeamSlot>()

        // Vagas dos dois times entrelaçadas, da mais restritiva para a menos restritiva: assim uma
        // vaga com poucos candidatos é resolvida antes que um coringa a ocupe desnecessariamente.
        // Com a garantia de levantador ligada, a vaga de armador é sempre a primeira a ser resolvida.
        val pendingSlots = (slotsA.map { it to teamA } + slotsB.map { it to teamB })
            .sortedWith(
                compareBy(
                    { (slot, _) -> setterFirstRank(slot, guaranteeSetter) },
                    { (slot, _) -> candidateCount(slot, pool, teamSize) }
                )
            )

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
            if (choice.tier > TIER_SECONDARY) {
                unfilled.add(slot)
                improvised.add(slot)
            }
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
            unfilledSlots = unfilled,
            improvisedSlots = improvised
        )
    }

    /**
     * Recalcula as posições de um time já formado, sem remontá-lo. Usado após substituições,
     * trocas de lado e rebalanceamentos.
     */
    fun assignPositionsToExistingTeam(
        team: List<Player>,
        teamSize: Int,
        guaranteeSetter: Boolean = true
    ): TeamAssignment {
        val effectiveSize = maxOf(teamSize, team.size).coerceAtMost(TeamComposition.MAX_TEAM_SIZE)
        val hasLibero = TeamComposition.hasLibero(team)
        val slots = requiredSlots(effectiveSize, hasLibero, guaranteeSetter)

        val remaining = team.toMutableList()
        val positions = mutableMapOf<Int, PlayerPosition>()
        val filled = mutableListOf<FilledSlot>()

        val orderedSlots = slots.withIndex().sortedWith(
            compareBy(
                { (_, slot) -> setterFirstRank(slot, guaranteeSetter) },
                { (_, slot) -> candidateCount(slot, team, effectiveSize) }
            )
        )
        val filledByIndex = arrayOfNulls<FilledSlot>(slots.size)
        orderedSlots.forEach { (slotIndex, slot) ->
            val choice = pickForSlot(slot, remaining, effectiveSize)
            if (choice == null) {
                filledByIndex[slotIndex] = FilledSlot(slot, null)
                return@forEach
            }
            remaining.remove(choice.player)
            positions[choice.player.id] = choice.position
            filledByIndex[slotIndex] = FilledSlot(slot, choice.player, isImprovised = choice.tier > TIER_SECONDARY)
        }

        // Jogadores além da composição mínima mantêm a posição preferida, quando houver.
        remaining.forEach { player ->
            positions[player.id] = TeamComposition
                .effectivePosition(PlayerPosition.fromStoredValue(player.preferredPosition), effectiveSize)
                ?: PlayerPosition.OUTSIDE_HITTER
        }

        filled.addAll(filledByIndex.map { it ?: FilledSlot(TeamSlot(PositionRole.ATTACK), null) })
        return TeamAssignment(players = team, positions = positions, slots = filled)
    }

    /** Composição de um time para exibição (vagas preenchidas e faltando). */
    fun describeComposition(
        team: List<Player>,
        teamSize: Int,
        guaranteeSetter: Boolean = true
    ): List<FilledSlot> = assignPositionsToExistingTeam(team, teamSize, guaranteeSetter).slots

    /**
     * Escolhe [count] jogadores de [pool] para cobrir as vagas ainda descobertas, considerando
     * [teams] times de [teamSize] jogadores e [base] como já escalados.
     *
     * **Encaixe posicional valorizado**: para cada vaga em aberto busca-se o melhor encaixe em
     * toda a fila (1ª opção → 2ª opção → coringa → realocação como último recurso), com empate
     * resolvido pela ordem de chegada em [pool].
     *
     * **Justiça mínima da fila**: pelo menos metade (arredondada pra cima) das vagas preenchidas
     * nesta operação vai obrigatoriamente para quem está no topo da fila, mesmo sem encaixe
     * perfeito — evita que alguém fique travado indefinidamente enquanto o time novo é sempre
     * montado só pelo melhor encaixe. A vaga já resolvida pela busca de levantador (abaixo) conta
     * dentro dessa metade.
     *
     * **Limite para o time que acabou de perder**: [losers] identifica quem acabou de sair de
     * quadra. No máximo um desses jogadores pode ser reaproveitado por encaixar melhor numa vaga,
     * e apenas quando o restante da fila (sem contar os perdedores) já tiver gente suficiente para
     * fechar sozinho as [count] vagas — do contrário não há limite, pois não haveria alternativa.
     * Isso evita o ciclo vicioso de um perdedor voltar direto pra quadra rodada após rodada só
     * porque encaixa bem numa posição.
     *
     * @return os escolhidos e o pool restante, ambos preservando a ordem original.
     */
    fun pickToCoverComposition(
        base: List<Player>,
        pool: List<Player>,
        count: Int,
        teamSize: Int,
        teams: Int = 2,
        guaranteeSetter: Boolean = true,
        losers: List<Player> = emptyList()
    ): Pair<List<Player>, List<Player>> {
        if (count <= 0 || pool.isEmpty()) return emptyList<Player>() to pool
        val hasLibero = TeamComposition.hasLibero(base + pool)
        val needs = (1..teams)
            .flatMap { requiredSlots(teamSize, hasLibero, guaranteeSetter) }
            .toMutableList()

        // Vagas já cobertas por quem está escalado saem da lista de necessidades.
        base.forEach { player -> consumeSlotFor(needs, player, teamSize) }

        val loserIds = losers.map { it.id }.toSet()
        val remaining = pool.toMutableList()
        val picked = mutableListOf<Player>()
        var losersPicked = 0
        val nonLoserTotal = pool.count { it.id !in loserIds }
        // Sem gente suficiente na fila "de verdade", não há como limitar quantos perdedores voltam.
        val maxLosersAllowed = if (nonLoserTotal >= count) 1 else Int.MAX_VALUE

        fun take(player: Player) {
            remaining.remove(player)
            picked.add(player)
            consumeSlotFor(needs, player, teamSize)
            if (player.id in loserIds) losersPicked++
        }

        // Único caso em que se pode "furar fila" incondicionalmente: buscar um levantador para a
        // vaga de armador ainda descoberta, quando a garantia estiver ligada (1ª opção, depois 2ª).
        if (guaranteeSetter && picked.size < count) {
            val playmakerIndex = needs.indexOfFirst { it.role == PositionRole.PLAYMAKER }
            if (playmakerIndex >= 0) {
                val slot = needs[playmakerIndex]
                val allowLoser = losersPicked < maxLosersAllowed
                val setter = remaining.firstOrNull { (allowLoser || it.id !in loserIds) && slot.accepts(effectivePreferred(it, teamSize)) }
                    ?: remaining.firstOrNull { (allowLoser || it.id !in loserIds) && slot.accepts(effectiveSecondary(it, teamSize)) }
                if (setter != null) {
                    needs.removeAt(playmakerIndex)
                    take(setter)
                }
            }
        }

        // Justiça mínima: pelo menos metade (arredondada pra cima) das vagas preenchidas nesta
        // operação é garantida a quem está no topo da fila, independentemente do encaixe.
        val guaranteedFrontCount = (count + 1) / 2
        while (picked.size < guaranteedFrontCount && remaining.isNotEmpty()) {
            take(remaining.first())
        }

        // Demais vagas: melhor encaixe posicional em toda a fila restante, respeitando o limite
        // de reaproveitamento de perdedores.
        while (picked.size < count && remaining.isNotEmpty()) {
            val allowLoser = losersPicked < maxLosersAllowed
            val candidates = if (allowLoser) remaining else remaining.filterNot { it.id in loserIds }
            val searchPool = candidates.ifEmpty { remaining }
            val chosen = searchPool.withIndex().minWithOrNull(
                compareBy(
                    { (_, player) -> bestTierForOpenNeeds(needs, player, teamSize) },
                    { (index, _) -> index }
                )
            )!!.value
            take(chosen)
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

    /** Melhor camada de encaixe de [player] entre as vagas ainda abertas em [needs]. */
    private fun bestTierForOpenNeeds(needs: List<TeamSlot>, player: Player, teamSize: Int): Int {
        if (needs.isEmpty()) return TIER_WILDCARD
        val preferred = effectivePreferred(player, teamSize)
        if (needs.any { it.accepts(preferred) }) return TIER_PREFERRED
        val secondary = effectiveSecondary(player, teamSize)
        if (needs.any { it.accepts(secondary) }) return TIER_SECONDARY
        if (TeamComposition.isWildcard(player)) return TIER_WILDCARD
        val role = (preferred ?: secondary)?.role
        return if (role == PositionRole.ATTACK) TIER_ATTACK else TIER_DEFENSE
    }

    /**
     * Ordena um grupo de jogadores que cai junto na fila de espera (Modo Posições Fixas) seguindo
     * o padrão ideal de composição de [teamSize]: armador primeiro (quando [guaranteeSetter]),
     * depois as demais vagas se revezando, repetindo o ciclo para o restante do grupo. Dentro da
     * mesma vaga, prioriza quem jogou menos partidas no dia, via [effectiveGames].
     *
     * De 2 a 5 jogadores só o papel importa (ataque e defesa se intercalando, líbero tratado como
     * central). Com 6 ou 7 jogadores as posições específicas entram no revezamento, na ordem
     * canônica ponteiro → central → oposto → líbero (quando houver), repetindo o ciclo para as
     * vagas duplicadas (2º ponteiro, 2º central).
     */
    fun orderByIdealComposition(
        players: List<Player>,
        teamSize: Int,
        guaranteeSetter: Boolean,
        effectiveGames: (Player) -> Int
    ): List<Player> {
        if (players.size <= 1) return players
        val hasLibero = TeamComposition.hasLibero(players)
        val pattern = slotPattern(teamSize, hasLibero, guaranteeSetter)
        val remaining = players.toMutableList()
        val ordered = mutableListOf<Player>()
        var step = 0
        while (remaining.isNotEmpty()) {
            val slot = pattern[step % pattern.size]
            step++
            val chosen = pickBestForSlot(remaining, slot, teamSize, effectiveGames)
            remaining.remove(chosen)
            ordered.add(chosen)
        }
        return ordered
    }

    /** Sequência ideal de vagas de um time segue a ordem canônica declarada em [TeamComposition]. */
    private fun slotPattern(teamSize: Int, hasLibero: Boolean, guaranteeSetter: Boolean): List<TeamSlot> =
        requiredSlots(teamSize, hasLibero, guaranteeSetter)
            .ifEmpty { listOf(TeamSlot(PositionRole.ATTACK)) }

    /**
     * Escolhe, dentro de [remaining], quem melhor cobre [slot] (preferida > secundária > coringa
     * > realocação), priorizando quem jogou menos partidas ([effectiveGames]) dentro do mesmo
     * nível de encaixe.
     */
    private fun pickBestForSlot(
        remaining: List<Player>,
        slot: TeamSlot,
        teamSize: Int,
        effectiveGames: (Player) -> Int
    ): Player {
        val bestTier = remaining.minOf { tierFor(slot, it, teamSize)!!.first }
        return remaining
            .filter { tierFor(slot, it, teamSize)!!.first == bestTier }
            .minWithOrNull(compareBy({ effectiveGames(it) }, { -it.elo }))!!
    }

    // --- internos ---

    private data class Choice(val player: Player, val position: PlayerPosition, val tier: Int)

    private fun requiredSlots(
        teamSize: Int,
        hasLibero: Boolean,
        guaranteeSetter: Boolean
    ): List<TeamSlot> {
        val slots = TeamComposition.requiredSlots(teamSize, guaranteeSetter).map { slot ->
            when {
                // No 6x6, sem líbero presente, a última vaga vira mais uma vaga de central.
                !hasLibero && teamSize < 7 && slot.position == PlayerPosition.LIBERO ->
                    TeamSlot(slot.role, PlayerPosition.MIDDLE_BLOCKER)
                else -> slot
            }
        }
        return slots
    }

    /** Ordena a vaga de armador antes das demais quando a garantia de levantador está ligada. */
    private fun setterFirstRank(slot: TeamSlot, guaranteeSetter: Boolean): Int =
        if (guaranteeSetter && slot.role == PositionRole.PLAYMAKER) 0 else 1

    /**
     * Divide [players] em duas metades com somas de Elo próximas, preservando tamanhos iguais
     * (ou diferença de um). Usado no rebalanceamento por quebra de sequência.
     */
    fun splitByElo(players: List<Player>): Pair<List<Player>, List<Player>> {
        if (players.isEmpty()) return emptyList<Player>() to emptyList()
        val targetA = (players.size + 1) / 2
        val targetB = players.size / 2
        val teamA = mutableListOf<Player>()
        val teamB = mutableListOf<Player>()
        players.sortedByDescending { it.elo }.forEach { player ->
            val toA = when {
                teamA.size >= targetA -> false
                teamB.size >= targetB -> true
                else -> eloOf(teamA) <= eloOf(teamB)
            }
            if (toA) teamA.add(player) else teamB.add(player)
        }
        return teamA to teamB
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

        // Realocar um atacante custa menos que realocar um defensor.
        val role = (preferred ?: secondary)?.role
        val tier = if (role == PositionRole.ATTACK) TIER_ATTACK else TIER_DEFENSE
        return tier to fallbackPosition(slot)
    }

    private fun slotPosition(slot: TeamSlot, matched: PlayerPosition?): PlayerPosition =
        slot.assignedPositionOverride ?: matched ?: fallbackPosition(slot)

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

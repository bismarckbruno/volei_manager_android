package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.PositionRole
import com.bismarck.voleimanager.app.data.model.TeamComposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionAssignerTest {

    private var nextId = 1

    private fun player(
        preferred: PlayerPosition? = null,
        secondary: PlayerPosition? = null,
        elo: Double = 1200.0,
        name: String = "P${nextId}"
    ) = Player(
        id = nextId++,
        name = name,
        elo = elo,
        groupName = "G",
        preferredPosition = preferred?.name,
        secondaryPosition = secondary?.name
    )

    private fun rolesOf(
        team: List<Player>,
        positions: Map<Int, PlayerPosition>
    ): List<PositionRole> = team.map { positions.getValue(it.id).role }

    /** Papel derivado da posição preferida cadastrada do jogador (usado nos testes de ordenação). */
    private fun roleOf(player: Player): PositionRole =
        PlayerPosition.fromStoredValue(player.preferredPosition)!!.role

    @Test
    fun twoVsTwo_requiresPlaymakerAndAttackOnBothTeams() {
        val pool = listOf(
            player(PlayerPosition.SETTER),
            player(PlayerPosition.SETTER),
            player(PlayerPosition.OUTSIDE_HITTER),
            player(PlayerPosition.OPPOSITE)
        )

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 2)

        assertEquals(2, result.teamA.size)
        assertEquals(2, result.teamB.size)
        listOf(result.teamA, result.teamB).forEach { team ->
            val roles = rolesOf(team, result.positions)
            assertTrue(roles.contains(PositionRole.PLAYMAKER))
            assertTrue(roles.contains(PositionRole.ATTACK))
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun fourVsFour_requiresOnePlaymakerTwoAttackersOneDefender() {
        val pool = buildList {
            repeat(2) { add(player(PlayerPosition.SETTER)) }
            repeat(4) { add(player(PlayerPosition.OUTSIDE_HITTER)) }
            repeat(2) { add(player(PlayerPosition.MIDDLE_BLOCKER)) }
        }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 4)

        listOf(result.teamA, result.teamB).forEach { team ->
            val roles = rolesOf(team, result.positions)
            assertEquals(1, roles.count { it == PositionRole.PLAYMAKER })
            assertEquals(2, roles.count { it == PositionRole.ATTACK })
            assertEquals(1, roles.count { it == PositionRole.DEFENSE })
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun liberoCountsAsMiddleBlockerBelowSixPlayers() {
        // Em times de 2 a 5, o líbero ocupa o papel de central.
        val pool = listOf(
            player(PlayerPosition.SETTER),
            player(PlayerPosition.SETTER),
            player(PlayerPosition.OUTSIDE_HITTER),
            player(PlayerPosition.OUTSIDE_HITTER),
            player(PlayerPosition.LIBERO),
            player(PlayerPosition.LIBERO)
        )

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 3)

        listOf(result.teamA, result.teamB).forEach { team ->
            val positions = team.map { result.positions.getValue(it.id) }
            assertTrue(positions.contains(PlayerPosition.MIDDLE_BLOCKER))
            assertTrue(positions.none { it == PlayerPosition.LIBERO })
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun sixVsSix_reservesLiberoSlotWhenLiberoExists() {
        val pool = buildList {
            repeat(2) { add(player(PlayerPosition.SETTER)) }
            repeat(4) { add(player(PlayerPosition.OUTSIDE_HITTER)) }
            repeat(2) { add(player(PlayerPosition.MIDDLE_BLOCKER)) }
            repeat(2) { add(player(PlayerPosition.OPPOSITE)) }
            repeat(2) { add(player(PlayerPosition.LIBERO)) }
        }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 6)

        listOf(result.teamA, result.teamB).forEach { team ->
            val positions = team.map { result.positions.getValue(it.id) }
            assertEquals(1, positions.count { it == PlayerPosition.SETTER })
            assertEquals(2, positions.count { it == PlayerPosition.OUTSIDE_HITTER })
            assertEquals(1, positions.count { it == PlayerPosition.MIDDLE_BLOCKER })
            assertEquals(1, positions.count { it == PlayerPosition.OPPOSITE })
            assertEquals(1, positions.count { it == PlayerPosition.LIBERO })
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun sixVsSix_liberoSlotBecomesMiddleBlockerWhenNoLiberoAvailable() {
        val pool = buildList {
            repeat(2) { add(player(PlayerPosition.SETTER)) }
            repeat(4) { add(player(PlayerPosition.OUTSIDE_HITTER)) }
            repeat(4) { add(player(PlayerPosition.MIDDLE_BLOCKER)) }
            repeat(2) { add(player(PlayerPosition.OPPOSITE)) }
        }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 6)

        listOf(result.teamA, result.teamB).forEach { team ->
            val positions = team.map { result.positions.getValue(it.id) }
            assertEquals(2, positions.count { it == PlayerPosition.MIDDLE_BLOCKER })
            assertEquals(0, positions.count { it == PlayerPosition.LIBERO })
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun sevenVsSeven_requiresTwoMiddleBlockersPlusLibero() {
        val pool = buildList {
            repeat(2) { add(player(PlayerPosition.SETTER)) }
            repeat(4) { add(player(PlayerPosition.OUTSIDE_HITTER)) }
            repeat(4) { add(player(PlayerPosition.MIDDLE_BLOCKER)) }
            repeat(2) { add(player(PlayerPosition.OPPOSITE)) }
            repeat(2) { add(player(PlayerPosition.LIBERO)) }
        }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 7)

        assertEquals(7, result.teamA.size)
        assertEquals(7, result.teamB.size)
        listOf(result.teamA, result.teamB).forEach { team ->
            val positions = team.map { result.positions.getValue(it.id) }
            assertEquals(1, positions.count { it == PlayerPosition.SETTER })
            assertEquals(2, positions.count { it == PlayerPosition.OUTSIDE_HITTER })
            assertEquals(2, positions.count { it == PlayerPosition.MIDDLE_BLOCKER })
            assertEquals(1, positions.count { it == PlayerPosition.OPPOSITE })
            assertEquals(1, positions.count { it == PlayerPosition.LIBERO })
        }
        assertTrue(result.isComplete)
    }

    @Test
    fun fallsBackToSecondaryPreferenceWhenPreferredSlotIsTaken() {
        // Três levantadores para duas vagas de armador: o terceiro cai para a 2ª preferência.
        val pool = listOf(
            player(PlayerPosition.SETTER),
            player(PlayerPosition.SETTER),
            player(PlayerPosition.SETTER, PlayerPosition.OPPOSITE),
            player(PlayerPosition.OUTSIDE_HITTER)
        )

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 2)

        // Ninguém precisou ser improvisado: a 2ª preferência cobriu a vaga de ataque.
        assertTrue(result.isComplete)
        val attackers = (result.teamA + result.teamB)
            .filter { result.positions.getValue(it.id).role == PositionRole.ATTACK }
        assertEquals(2, attackers.size)
    }

    @Test
    fun wildcardFillsSlotWithoutSpecialist() {
        val wildcard = player()
        val pool = listOf(
            player(PlayerPosition.SETTER),
            player(PlayerPosition.SETTER),
            player(PlayerPosition.OUTSIDE_HITTER),
            wildcard
        )

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 2)

        assertEquals(2, result.teamA.size)
        assertEquals(2, result.teamB.size)
        assertNotNull(result.positions[wildcard.id])
        // O coringa entrou numa vaga de ataque, então a composição é sinalizada como incompleta.
        assertTrue(result.unfilledSlots.isNotEmpty())
    }

    @Test
    fun reportsUnfilledSlotsWhenNoPlaymakerExists() {
        val pool = buildList { repeat(4) { add(player(PlayerPosition.OUTSIDE_HITTER)) } }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 2)

        assertEquals(2, result.teamA.size)
        assertEquals(2, result.teamB.size)
        assertTrue(!result.isComplete)
        assertTrue(result.unfilledSlots.any { it.role == PositionRole.PLAYMAKER })
    }

    @Test
    fun balancesEloBetweenTeams() {
        val pool = listOf(
            player(PlayerPosition.SETTER, elo = 1400.0),
            player(PlayerPosition.SETTER, elo = 1000.0),
            player(PlayerPosition.OUTSIDE_HITTER, elo = 1350.0),
            player(PlayerPosition.OUTSIDE_HITTER, elo = 1050.0)
        )

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 2)

        // O levantador forte não pode cair no mesmo time do ponteiro forte.
        assertEquals(100.0, PositionAssigner.eloGap(result.teamA, result.teamB), 0.001)
    }

    @Test
    fun assignPositionsToExistingTeam_marksMissingSlots() {
        val team = listOf(
            player(PlayerPosition.OUTSIDE_HITTER),
            player(PlayerPosition.OUTSIDE_HITTER)
        )

        val assignment = PositionAssigner.assignPositionsToExistingTeam(team, teamSize = 2)

        assertEquals(2, assignment.positions.size)
        assertTrue(!assignment.isComplete)
        assertTrue(assignment.missingSlots.any { it.role == PositionRole.PLAYMAKER })
    }

    @Test
    fun describeComposition_listsOneEntryPerRequiredSlot() {
        val team = listOf(
            player(PlayerPosition.SETTER),
            player(PlayerPosition.OUTSIDE_HITTER),
            player(PlayerPosition.MIDDLE_BLOCKER)
        )

        val slots = PositionAssigner.describeComposition(team, teamSize = 3)

        assertEquals(TeamComposition.requiredSlots(3).size, slots.size)
        assertTrue(slots.all { it.player != null })
        assertTrue(slots.none { it.isImprovised })
    }

    @Test
    fun pickToCoverComposition_prefersSetterFromQueueWhenGuaranteeSetterEnabled() {
        // Time perdedor ficou com um atacante só; falta o levantador. A fila tem um atacante e um
        // levantador — com a garantia ligada, o levantador da fila deve ser priorizado.
        val losingTeam = listOf(player(PlayerPosition.OPPOSITE))
        val queue = listOf(
            player(PlayerPosition.OUTSIDE_HITTER, name = "QueueAttacker"),
            player(PlayerPosition.SETTER, name = "QueueSetter")
        )

        val (picked, _) = PositionAssigner.pickToCoverComposition(
            base = losingTeam,
            pool = queue,
            count = 1,
            teamSize = 2,
            teams = 1,
            guaranteeSetter = true
        )

        assertEquals(1, picked.size)
        assertEquals("QueueSetter", picked.first().name)
    }

    @Test
    fun pickToCoverComposition_withGuaranteeSetterDisabled_convertsPlaymakerSlotToAttack() {
        // Sem garantia de levantador, a vaga de armador vira vaga de ataque, então quem estiver
        // no topo da fila entra, mesmo sendo atacante.
        val losingTeam = listOf(player(PlayerPosition.OPPOSITE))
        val queue = listOf(
            player(PlayerPosition.OUTSIDE_HITTER, name = "QueueAttacker"),
            player(PlayerPosition.SETTER, name = "QueueSetter")
        )

        val (picked, _) = PositionAssigner.pickToCoverComposition(
            base = losingTeam,
            pool = queue,
            count = 1,
            teamSize = 2,
            teams = 1,
            guaranteeSetter = false
        )

        assertEquals(1, picked.size)
        assertEquals("QueueAttacker", picked.first().name)
    }

    @Test
    fun pickToCoverComposition_singleSlotFillsStrictlyFromFrontOfQueue() {
        // Para uma única vaga em aberto, a garantia de justiça mínima (metade arredondada pra
        // cima = 1) cobre 100% da vaga: o topo da fila entra mesmo quando alguém mais atrás
        // encaixaria melhor na posição.
        val losingTeam = listOf(player(PlayerPosition.SETTER))
        val queue = listOf(
            player(name = "FrontOfQueueWildcard"), // sem posição cadastrada, no topo da fila
            player(PlayerPosition.OUTSIDE_HITTER, name = "BetterFitDeeperInQueue")
        )

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = losingTeam,
            pool = queue,
            count = 1,
            teamSize = 2,
            teams = 1,
            guaranteeSetter = true
        )

        assertEquals(1, picked.size)
        assertEquals("FrontOfQueueWildcard", picked.first().name)
        assertEquals(listOf("BetterFitDeeperInQueue"), remaining.map { it.name })
    }

    @Test
    fun pickToCoverComposition_capsLoserReentryToOneWhenWaitingQueueHasEnoughNonLosers() {
        // Dois atacantes do time que acabou de perder encaixam perfeitamente nas duas vagas de
        // ataque, mas a fila de espera "de verdade" (sem os perdedores) já tem gente suficiente
        // para fechar as duas vagas sozinha. Por isso, no máximo 1 perdedor pode voltar direto.
        val waiting1 = player(name = "Waiting1")
        val waiting2 = player(name = "Waiting2")
        val loser1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser1")
        val loser2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser2")
        val losers = listOf(loser1, loser2)
        val pool = listOf(waiting1, waiting2, loser1, loser2)

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = emptyList(),
            pool = pool,
            count = 2,
            teamSize = 2,
            teams = 1,
            guaranteeSetter = false,
            losers = losers
        )

        val pickedLosers = picked.count { it.id in losers.map { l -> l.id } }
        assertEquals(1, pickedLosers)
        assertTrue(picked.any { it.name == "Waiting1" })
        assertEquals(2, remaining.size)
    }

    @Test
    fun pickToCoverComposition_allowsMultipleLosersWhenWaitingQueueLacksDepth() {
        // Se a fila "de verdade" não tem gente suficiente para fechar o time sozinha, não há
        // motivo para limitar quantos perdedores voltam — não existe alternativa mesmo. Usa
        // count=4 para que a garantia de justiça mínima (2 vagas) não consuma sozinha todas as
        // vagas restantes, deixando a busca de melhor encaixe (sem limite de perdedores) decidir
        // o resto.
        val waiting1 = player(name = "Waiting1")
        val loser1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser1")
        val loser2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser2")
        val loser3 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser3")
        val loser4 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser4")
        val loser5 = player(PlayerPosition.OUTSIDE_HITTER, name = "Loser5")
        val losers = listOf(loser1, loser2, loser3, loser4, loser5)
        val pool = listOf(waiting1) + losers

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = emptyList(),
            pool = pool,
            count = 4,
            teamSize = 2,
            teams = 1,
            guaranteeSetter = false,
            losers = losers
        )

        val pickedLosers = picked.count { it.id in losers.map { l -> l.id } }
        assertEquals(3, pickedLosers)
        assertEquals(listOf("Waiting1", "Loser1", "Loser2", "Loser3"), picked.map { it.name })
        assertEquals(listOf("Loser4", "Loser5"), remaining.map { it.name })
    }

    @Test
    fun pickToCoverComposition_guaranteesAtLeastHalfRoundedUpFromFrontOfQueue_threeSlots() {
        // Em um time de 3x3, pelo menos 2 das 3 vagas preenchidas nesta operação (metade
        // arredondada pra cima) precisam ir para quem está no topo da fila, mesmo que outro
        // jogador mais atrás encaixe melhor na posição em aberto.
        val front1 = player(name = "Front1")
        val front2 = player(name = "Front2")
        val betterFitDeeper = player(PlayerPosition.MIDDLE_BLOCKER, name = "BetterFitDeeper")
        val pool = listOf(front1, front2, betterFitDeeper)

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = emptyList(),
            pool = pool,
            count = 3,
            teamSize = 3,
            teams = 1,
            guaranteeSetter = false
        )

        assertEquals(listOf("Front1", "Front2", "BetterFitDeeper"), picked.map { it.name })
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun pickToCoverComposition_guaranteesAtLeastHalfRoundedUpFromFrontOfQueue_fiveSlots() {
        // Em um time de 5x5, pelo menos 3 das 5 vagas (metade de 5 arredondada pra cima) vão
        // obrigatoriamente para o topo da fila; só as 2 vagas restantes usam o melhor encaixe.
        val front1 = player(name = "Front1")
        val front2 = player(name = "Front2")
        val front3 = player(name = "Front3")
        val betterFitDeeper1 = player(PlayerPosition.MIDDLE_BLOCKER, name = "BetterFitDeeper1")
        val betterFitDeeper2 = player(PlayerPosition.MIDDLE_BLOCKER, name = "BetterFitDeeper2")
        val pool = listOf(front1, front2, front3, betterFitDeeper1, betterFitDeeper2)

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = emptyList(),
            pool = pool,
            count = 5,
            teamSize = 5,
            teams = 1,
            guaranteeSetter = false
        )

        assertEquals(
            listOf("Front1", "Front2", "Front3", "BetterFitDeeper1", "BetterFitDeeper2"),
            picked.map { it.name }
        )
        assertTrue(remaining.isEmpty())
    }

    @Test
    fun pickToCoverComposition_frontGuaranteeStillRespectsBestFitForRemainingSlots() {
        // Com 5 vagas e 8 candidatos: a busca de levantador (incondicional) resolve 1 vaga, as 2
        // primeiras pessoas da fila (garantia de justiça mínima) resolvem mais 2, e as 2 vagas
        // finais (ataque/defesa) usam o melhor encaixe posicional entre quem sobrou — mesmo que
        // isso signifique pular coringas sem posição cadastrada em favor de especialistas mais
        // atrás na fila.
        val front1 = player(name = "Front1")
        val front2 = player(name = "Front2")
        val front3 = player(name = "Front3")
        val wildcard1 = player(name = "Wildcard1")
        val wildcard2 = player(name = "Wildcard2")
        val setterDeep = player(PlayerPosition.SETTER, name = "SetterDeep")
        val attackerDeep = player(PlayerPosition.OUTSIDE_HITTER, name = "AttackerDeep")
        val defenderDeep = player(PlayerPosition.MIDDLE_BLOCKER, name = "DefenderDeep")
        val pool = listOf(front1, front2, front3, wildcard1, wildcard2, setterDeep, attackerDeep, defenderDeep)

        val (picked, remaining) = PositionAssigner.pickToCoverComposition(
            base = emptyList(),
            pool = pool,
            count = 5,
            teamSize = 5,
            teams = 1,
            guaranteeSetter = true
        )

        assertEquals(
            setOf("SetterDeep", "Front1", "Front2", "AttackerDeep", "DefenderDeep"),
            picked.map { it.name }.toSet()
        )
        assertEquals(setOf("Front3", "Wildcard1", "Wildcard2"), remaining.map { it.name }.toSet())
    }

    @Test
    fun assignPositionsToExistingTeam_keepsExistingSetterInPlaymakerSlot() {
        // Confirma que o levantador já presente no time (por exemplo, o levantador perdedor que
        // permaneceu) é mantido na vaga de armador quando a garantia está ligada.
        val setter = player(PlayerPosition.SETTER, name = "LosingSetter")
        val team = listOf(setter, player(PlayerPosition.OUTSIDE_HITTER))

        val assignment = PositionAssigner.assignPositionsToExistingTeam(team, teamSize = 2, guaranteeSetter = true)

        assertEquals(PlayerPosition.SETTER, assignment.positions[setter.id])
        assertTrue(assignment.isComplete)
    }

    @Test
    fun splitByElo_producesBalancedHalvesWithEqualOrOffByOneSizes() {
        val players = listOf(
            player(elo = 1500.0), player(elo = 1400.0),
            player(elo = 1300.0), player(elo = 1200.0),
            player(elo = 1100.0)
        )

        val (halfA, halfB) = PositionAssigner.splitByElo(players)

        assertTrue(Math.abs(halfA.size - halfB.size) <= 1)
        assertEquals(players.size, halfA.size + halfB.size)
        val gap = Math.abs(halfA.sumOf { it.elo } - halfB.sumOf { it.elo })
        // A diferença não deve exceder o Elo do jogador mais forte (garantia frouxa de equilíbrio).
        assertTrue(gap <= players.maxOf { it.elo })
    }

    @Test
    fun orderByIdealComposition_fourVsFour_followsPlaymakerThenAttackThenTwoDefenseSlots() {
        // Para 4x4 a composição ideal agora é 1 armador, 1 ataque e 2 defesas.
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val attacker1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Attacker1")
        val defender1 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Defender1")
        val defender2 = player(PlayerPosition.LIBERO, name = "Defender2")
        val group = listOf(defender2, attacker1, setter, defender1)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 4, guaranteeSetter = true) { 0 }

        assertEquals(PositionRole.PLAYMAKER, roleOf(ordered[0]))
        assertEquals(PositionRole.ATTACK, roleOf(ordered[1]))
        assertEquals(PositionRole.DEFENSE, roleOf(ordered[2]))
        assertEquals(PositionRole.DEFENSE, roleOf(ordered[3]))
    }

    @Test
    fun orderByIdealComposition_prefersWhoPlayedFewerGamesWithinTheSameRole() {
        // Em 5x5 há duas vagas seguidas de ataque: quem jogou menos partidas hoje deve aparecer
        // antes dentro desse mesmo bloco.
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val defender = player(PlayerPosition.MIDDLE_BLOCKER, name = "Defender")
        val attackerPlayedMore = player(PlayerPosition.OUTSIDE_HITTER, name = "PlayedMore")
        val attackerPlayedLess = player(PlayerPosition.OPPOSITE, name = "PlayedLess")
        val effectiveGames = mapOf(attackerPlayedMore.id to 3, attackerPlayedLess.id to 0)
        val secondDefender = player(PlayerPosition.LIBERO, name = "SecondDefender")
        val group = listOf(setter, attackerPlayedMore, defender, attackerPlayedLess, secondDefender)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 5, guaranteeSetter = true) {
            effectiveGames[it.id] ?: 0
        }

        // Padrão 5x5: armador, ataque, ataque, defesa, defesa.
        assertEquals("PlayedLess", ordered[1].name)
        assertEquals("PlayedMore", ordered[2].name)
    }

    @Test
    fun orderByIdealComposition_withGuaranteeSetterDisabled_hasNoDedicatedPlaymakerSlot() {
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val attacker = player(PlayerPosition.OUTSIDE_HITTER, name = "Attacker")
        val group = listOf(attacker, setter)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 2, guaranteeSetter = false) { 0 }

        // Sem garantia de levantador, a vaga de armador vira vaga de ataque — o atacante (encaixe
        // preferido) fica na frente do levantador (que só serve como coringa/realocação aqui).
        assertEquals("Attacker", ordered[0].name)
    }

    @Test
    fun orderByIdealComposition_sixVsSix_withLibero_followsSetterToLiberoDisplayOrder() {
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val outside1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside1")
        val outside2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside2")
        val middle = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle")
        val opposite = player(PlayerPosition.OPPOSITE, name = "Opposite")
        val libero = player(PlayerPosition.LIBERO, name = "Libero")
        val group = listOf(libero, opposite, middle, outside2, setter, outside1)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 6, guaranteeSetter = true) { 0 }
        assertEquals(
            listOf("Setter", "Outside2", "Outside1", "Middle", "Opposite", "Libero"),
            ordered.map { it.name }
        )
    }

    @Test
    fun orderByIdealComposition_sixVsSix_withoutLibero_convertsLiberoSlotToMiddleBlocker() {
        // Sem líbero disponível, o último slot do 6x6 vira mais um central.
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val outside1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside1")
        val outside2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside2")
        val middle1 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle1")
        val middle2 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle2")
        val opposite = player(PlayerPosition.OPPOSITE, name = "Opposite")
        val group = listOf(middle2, opposite, middle1, outside2, setter, outside1)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 6, guaranteeSetter = true) { 0 }

        assertEquals(
            listOf("Setter", "Outside2", "Outside1", "Middle2", "Opposite", "Middle1"),
            ordered.map { it.name }
        )
    }

    @Test
    fun orderByIdealComposition_sevenVsSeven_withLibero_putsBenchLiberoLast() {
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val outside1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside1")
        val outside2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside2")
        val middle1 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle1")
        val middle2 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle2")
        val opposite = player(PlayerPosition.OPPOSITE, name = "Opposite")
        val libero = player(PlayerPosition.LIBERO, name = "Libero")
        val group = listOf(middle2, libero, opposite, middle1, outside2, setter, outside1)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 7, guaranteeSetter = true) { 0 }
        assertEquals(
            listOf("Setter", "Outside2", "Outside1", "Middle2", "Opposite", "Middle1", "Libero"),
            ordered.map { it.name }
        )
    }

    @Test
    fun orderByIdealComposition_sixVsSix_withGuaranteeSetterDisabled_startsWithGenericAttackSlot() {
        // Cenário legado de defesa: a vaga inicial vira ataque genérico.
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val outside1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside1")
        val outside2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside2")
        val middle = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle")
        val opposite = player(PlayerPosition.OPPOSITE, name = "Opposite")
        val libero = player(PlayerPosition.LIBERO, name = "Libero")
        val group = listOf(libero, opposite, middle, outside2, setter, outside1)

        val ordered = PositionAssigner.orderByIdealComposition(group, teamSize = 6, guaranteeSetter = false) { 0 }

        assertEquals("Opposite", ordered[0].name)
    }

    @Test
    fun assignPositionsToExistingTeam_sevenVsSeven_withoutLibero_keepsABenchLiberoSlot() {
        val setter = player(PlayerPosition.SETTER, name = "Setter")
        val outside1 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside1")
        val outside2 = player(PlayerPosition.OUTSIDE_HITTER, name = "Outside2")
        val middle1 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle1")
        val middle2 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle2")
        val middle3 = player(PlayerPosition.MIDDLE_BLOCKER, name = "Middle3")
        val opposite = player(PlayerPosition.OPPOSITE, name = "Opposite")

        val assignment = PositionAssigner.assignPositionsToExistingTeam(
            team = listOf(middle3, opposite, middle2, middle1, outside2, setter, outside1),
            teamSize = 7,
            guaranteeSetter = true
        )

        assertEquals(1, assignment.positions.values.count { it == PlayerPosition.LIBERO })
        val benchCandidates = listOf(middle1, middle2, middle3)
        assertTrue(benchCandidates.any { assignment.positions[it.id] == PlayerPosition.LIBERO })
    }

    @Test
    fun everyPlayerInCourtReceivesAPosition() {
        val pool = buildList {
            repeat(3) { add(player(PlayerPosition.SETTER)) }
            repeat(3) { add(player()) }
            repeat(6) { add(player(PlayerPosition.OUTSIDE_HITTER, PlayerPosition.MIDDLE_BLOCKER)) }
        }

        val result = PositionAssigner.buildBalancedTeams(pool, teamSize = 5)

        (result.teamA + result.teamB).forEach { player ->
            assertNotNull("Jogador ${player.id} sem posição", result.positions[player.id])
        }
        assertEquals(10, result.teamA.size + result.teamB.size)
    }
}

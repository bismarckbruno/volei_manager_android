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

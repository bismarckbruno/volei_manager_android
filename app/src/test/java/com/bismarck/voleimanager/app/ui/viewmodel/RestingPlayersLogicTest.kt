package com.bismarck.voleimanager.app.ui.viewmodel

import com.bismarck.voleimanager.app.data.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestingPlayersLogicTest {

    private fun p(id: Int, name: String = "P$id") =
        Player(id = id, name = name, groupName = "G")

    @Test
    fun applyRestingMark_marksPlayersAndMovesThemToQueueEndWithoutDuplicates() {
        val waiting = listOf(p(1), p(2), p(3), p(4))
        val currentResting = mapOf(9 to 5)
        val playersToRest = listOf(p(2), p(4))

        val result = applyRestingMark(
            currentResting = currentResting,
            currentWaiting = waiting,
            playersToRest = playersToRest,
            returnRound = 7
        )

        assertEquals(7, result.restingPlayers[2])
        assertEquals(7, result.restingPlayers[4])
        assertEquals(5, result.restingPlayers[9])
        assertEquals(listOf(1, 3, 2, 4), result.waitingList.map { it.id })
        assertEquals(4, result.waitingList.map { it.id }.toSet().size)
    }

    @Test
    fun applyRestingMark_withEmptyPlayers_keepsStateUnchanged() {
        val waiting = listOf(p(1), p(2))
        val currentResting = mapOf(1 to 3)

        val result = applyRestingMark(
            currentResting = currentResting,
            currentWaiting = waiting,
            playersToRest = emptyList(),
            returnRound = 10
        )

        assertEquals(currentResting, result.restingPlayers)
        assertEquals(waiting, result.waitingList)
    }

    @Test
    fun resolveReturningPlayers_whenNobodyIsDue_keepsStateUnchanged() {
        val waiting = listOf(p(1), p(2), p(3))
        val currentResting = mapOf(2 to 5, 3 to 6)

        val result = resolveReturningPlayers(
            currentResting = currentResting,
            currentWaiting = waiting,
            roundCounter = 4
        )

        assertTrue(result.returningIds.isEmpty())
        assertEquals(currentResting, result.restingPlayers)
        assertEquals(waiting, result.waitingList)
    }

    @Test
    fun resolveReturningPlayers_removesReturnedFromRestingAndWaiting() {
        val waiting = listOf(p(1), p(2), p(3), p(4))
        val currentResting = mapOf(2 to 4, 3 to 6, 4 to 4)

        val result = resolveReturningPlayers(
            currentResting = currentResting,
            currentWaiting = waiting,
            roundCounter = 4
        )

        assertEquals(setOf(2, 4), result.returningIds)
        assertEquals(mapOf(3 to 6), result.restingPlayers)
        assertEquals(listOf(1, 3), result.waitingList.map { it.id })
    }

    @Test
    fun markThenResolve_roundTripProducesExpectedState() {
        val waiting = listOf(p(1), p(2), p(3))

        val marked = applyRestingMark(
            currentResting = emptyMap(),
            currentWaiting = waiting,
            playersToRest = listOf(p(2), p(3)),
            returnRound = 8
        )

        val resolved = resolveReturningPlayers(
            currentResting = marked.restingPlayers,
            currentWaiting = marked.waitingList,
            roundCounter = 8
        )

        assertEquals(setOf(2, 3), resolved.returningIds)
        assertTrue(resolved.restingPlayers.isEmpty())
        assertEquals(listOf(1), resolved.waitingList.map { it.id })
    }
}


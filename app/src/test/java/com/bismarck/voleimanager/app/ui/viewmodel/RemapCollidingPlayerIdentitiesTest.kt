package com.bismarck.voleimanager.app.ui.viewmodel

import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre a colisão de identidade (id/publicId) ao importar um backup (.vlz): como ambos os
 * campos são únicos globalmente (todos os grupos) e `insertPlayers` usa
 * [androidx.room.OnConflictStrategy.IGNORE], um jogador importado cujo id/publicId já pertença
 * a outro jogador no dispositivo (de outro grupo, ou de um import parcial anterior) seria
 * descartado silenciosamente sem este remapeamento.
 */
class RemapCollidingPlayerIdentitiesTest {

    @Test
    fun remapCollidingPlayerIdentities_reassignsIdOnCollisionWithExistingPlayer() {
        val imported = listOf(
            Player(id = 1, name = "Ana", groupName = "Teste", publicId = "pub-1"),
            Player(id = 2, name = "Bia", groupName = "Teste", publicId = "pub-2")
        )

        val (remapped, _, _) = remapCollidingPlayerIdentities(
            players = imported,
            history = emptyList(),
            logs = emptyList(),
            existingIds = setOf(1), // id=1 já usado por um jogador de outro grupo
            existingPublicIds = emptySet()
        )

        assertEquals(2, remapped.size)
        assertNotEquals(1, remapped[0].id)
        assertTrue(remapped[0].id > 1)
        assertEquals(2, remapped[1].id) // sem colisão, mantém o id original
    }

    @Test
    fun remapCollidingPlayerIdentities_reassignsPublicIdOnCollision() {
        val imported = listOf(
            Player(id = 5, name = "Carla", groupName = "Teste", publicId = "dup-publicid")
        )

        val (remapped, _, _) = remapCollidingPlayerIdentities(
            players = imported,
            history = emptyList(),
            logs = emptyList(),
            existingIds = emptySet(),
            existingPublicIds = setOf("dup-publicid")
        )

        assertEquals(5, remapped[0].id) // id sem colisão, preservado
        assertNotEquals("dup-publicid", remapped[0].publicId)
    }

    @Test
    fun remapCollidingPlayerIdentities_propagatesRemappedIdToHistoryAndEloLogs() {
        val imported = listOf(
            Player(id = 1, name = "Ana", groupName = "Teste", publicId = "pub-1"),
            Player(id = 2, name = "Bia", groupName = "Teste", publicId = "pub-2")
        )
        val history = listOf(
            MatchHistory(
                id = 0,
                date = "01/01/2026 10:00",
                teamA = "Ana, Bia",
                teamB = "Outro",
                teamAIds = "1,2",
                teamBIds = "",
                winner = "Ana, Bia",
                eloPoints = 10.0,
                groupName = "Teste"
            )
        )
        val logs = listOf(
            PlayerEloLog(id = 0, playerId = 1, playerNameSnapshot = "Ana", date = "2026-01-01", elo = 1210.0, groupName = "Teste"),
            PlayerEloLog(id = 0, playerId = 2, playerNameSnapshot = "Bia", date = "2026-01-01", elo = 1190.0, groupName = "Teste")
        )

        val (remappedPlayers, remappedHistory, remappedLogs) = remapCollidingPlayerIdentities(
            players = imported,
            history = history,
            logs = logs,
            existingIds = setOf(1), // apenas o id=1 colide
            existingPublicIds = emptySet()
        )

        val newIdForAna = remappedPlayers.first { it.name == "Ana" }.id
        assertNotEquals(1, newIdForAna)

        val remappedIds = remappedHistory[0].teamAIds.split(",").map { it.toInt() }
        assertTrue(newIdForAna in remappedIds)
        assertTrue(2 in remappedIds) // Bia manteve o id original

        assertEquals(newIdForAna, remappedLogs.first { it.playerNameSnapshot == "Ana" }.playerId)
        assertEquals(2, remappedLogs.first { it.playerNameSnapshot == "Bia" }.playerId)
    }

    @Test
    fun remapCollidingPlayerIdentities_noCollisions_keepsEverythingUnchanged() {
        val imported = listOf(
            Player(id = 10, name = "Duda", groupName = "Teste", publicId = "pub-10")
        )

        val (remapped, history, logs) = remapCollidingPlayerIdentities(
            players = imported,
            history = emptyList(),
            logs = emptyList(),
            existingIds = setOf(1, 2, 3),
            existingPublicIds = setOf("outro-pub")
        )

        assertEquals(10, remapped[0].id)
        assertEquals("pub-10", remapped[0].publicId)
        assertTrue(history.isEmpty())
        assertTrue(logs.isEmpty())
    }
}

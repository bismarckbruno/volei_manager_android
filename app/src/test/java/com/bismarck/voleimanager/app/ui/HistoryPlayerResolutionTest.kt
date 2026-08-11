package com.bismarck.voleimanager.app.ui

import com.bismarck.voleimanager.app.data.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryPlayerResolutionTest {

    @Test
    fun resolveHistoryPlayer_fallsBackToNameWhenIdPointsToAnotherPerson() {
        val bismarck = Player(id = 1, name = "Bismarck", groupName = "G")
        val fernando = Player(id = 52, name = "Fernando", groupName = "G")
        val wrongPlayerFor52 = Player(id = 52, name = "Marconio", groupName = "G")

        val resolved = resolveHistoryPlayer(
            identifier = PlayerIdentifier(id = 52, name = "Fernando"),
            playersById = mapOf(52 to wrongPlayerFor52, 1 to bismarck),
            playersByCanonicalName = mapOf(
                canonicalHistoryName("Bismarck") to bismarck,
                canonicalHistoryName("Fernando") to fernando,
                canonicalHistoryName("Marconio") to wrongPlayerFor52
            )
        )

        assertEquals("Fernando", resolved?.name)
        assertEquals(52, resolved?.id)
    }

    @Test
    fun resolveHistoryPlayer_returnsNullWhenNoIdAndNoNameMatch() {
        val resolved = resolveHistoryPlayer(
            identifier = PlayerIdentifier(id = null, name = "Fernando"),
            playersById = emptyMap(),
            playersByCanonicalName = emptyMap()
        )

        assertNull(resolved)
    }
}

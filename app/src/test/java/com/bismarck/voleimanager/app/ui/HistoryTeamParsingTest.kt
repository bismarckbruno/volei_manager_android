package com.bismarck.voleimanager.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryTeamParsingTest {

    @Test
    fun parseTeamIdentifiers_doesNotShiftIdsWhenOneIsMissing() {
        val identifiers = parseTeamIdentifiers(
            teamNamesRaw = "Bismarck, Mari, Tiago",
            teamIdsRaw = "1,,2"
        )

        assertEquals(3, identifiers.size)
        assertEquals(1, identifiers[0].id)
        assertEquals("Bismarck", identifiers[0].name)
        assertEquals(null, identifiers[1].id)
        assertEquals("Mari", identifiers[1].name)
        assertEquals(2, identifiers[2].id)
        assertEquals("Tiago", identifiers[2].name)
    }
}

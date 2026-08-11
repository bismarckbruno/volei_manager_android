package com.bismarck.voleimanager.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryIdentifierDedupTest {

    @Test
    fun buildUniqueHistoryIdentifiers_keepsBothNamesWhenSameIdIsCorrupted() {
        val unique = buildUniqueHistoryIdentifiers(
            listOf(
                PlayerIdentifier(52, "Fernando"),
                PlayerIdentifier(52, "Marcão")
            )
        )

        assertEquals(2, unique.size)
        assertEquals("Fernando", unique[0].name)
        assertEquals(52, unique[0].id)
        assertEquals("Marcão", unique[1].name)
        assertEquals(null, unique[1].id)
    }

    @Test
    fun buildUniqueHistoryIdentifiers_mergesSameNameAndPrefersEntryWithId() {
        val unique = buildUniqueHistoryIdentifiers(
            listOf(
                PlayerIdentifier(null, "Fernando"),
                PlayerIdentifier(52, "Fernando")
            )
        )

        assertEquals(1, unique.size)
        assertEquals("Fernando", unique[0].name)
        assertEquals(52, unique[0].id)
    }
}

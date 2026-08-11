package com.bismarck.voleimanager.app.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class TeamSnapshotNormalizationTest {

    @Test
    fun normalizeTeamSnapshotWithIds_sortsNamesAndIdsTogether() {
        val snapshot = normalizeTeamSnapshotWithIds(
            rawNames = "Lukano, Marconio, Bismarck",
            rawIds = "3,4,1"
        ) { it.trim() }

        assertEquals("Bismarck, Lukano, Marconio", snapshot.names)
        assertEquals("1,3,4", snapshot.ids)
    }

    @Test
    fun normalizeTeamSnapshotWithIds_keepsMissingIdPositionAfterSorting() {
        val snapshot = normalizeTeamSnapshotWithIds(
            rawNames = "Tiago, Bismarck, Mari",
            rawIds = ",1,2"
        ) { it.trim() }

        assertEquals("Bismarck, Mari, Tiago", snapshot.names)
        assertEquals("1,2,", snapshot.ids)
    }
}

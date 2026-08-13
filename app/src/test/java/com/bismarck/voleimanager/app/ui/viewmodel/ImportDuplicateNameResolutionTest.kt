package com.bismarck.voleimanager.app.ui.viewmodel

import com.bismarck.voleimanager.app.data.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportDuplicateNameResolutionTest {

    @Test
    fun resolveImportedPlayersForInsert_skipsDuplicateNamesWithinTheSamePayload() {
        val imported = listOf(
            Player(id = 1, name = "Ana", groupName = "Grupo A"),
            Player(id = 2, name = "Ana", groupName = "Grupo A"),
            Player(id = 3, name = "Joao", groupName = "Grupo A")
        )

        val (accepted, skipped) = resolveImportedPlayersForInsert(imported)

        assertEquals(2, accepted.size)
        assertEquals(listOf("Ana [Grupo A]"), skipped)
        assertEquals(listOf(1, 3), accepted.map { it.id })
    }

    @Test
    fun resolveImportedPlayersForInsert_skipsDuplicateNamesAcrossCanonicalVariants() {
        val imported = listOf(
            Player(id = 4, name = "José", groupName = "Grupo B"),
            Player(id = 5, name = "Jose", groupName = "Grupo B"),
            Player(id = 6, name = "Maria", groupName = "Grupo B")
        )

        val (accepted, skipped) = resolveImportedPlayersForInsert(imported)

        assertEquals(2, accepted.size)
        assertEquals(listOf("Jose [Grupo B]"), skipped)
        assertEquals(listOf(4, 6), accepted.map { it.id })
    }

    @Test
    fun resolveImportedPlayersWithAutoRename_renamesRepeatedNamesWithSuffix() {
        val imported = listOf(
            Player(id = 7, name = "Ana", groupName = "Grupo C"),
            Player(id = 8, name = "Ana", groupName = "Grupo C"),
            Player(id = 9, name = "Ana", groupName = "Grupo C")
        )

        val (accepted, renamed) = resolveImportedPlayersWithAutoRename(imported)

        assertEquals(3, accepted.size)
        assertEquals(listOf("Ana -> Ana 2 [Grupo C]", "Ana -> Ana 3 [Grupo C]"), renamed)
        assertEquals(listOf("Ana", "Ana 2", "Ana 3"), accepted.map { it.name })
    }

    @Test
    fun resolveImportedPlayersForInsert_skipsExistingCanonicalNamesFromDatabase() {
        val imported = listOf(
            Player(id = 10, name = "Pedro", groupName = "Grupo D"),
            Player(id = 11, name = "Pedro", groupName = "Grupo D")
        )

        val existing = mapOf(
            "Grupo D" to setOf("pedro")
        )

        val (accepted, skipped) = resolveImportedPlayersForInsert(imported, existing)

        assertEquals(0, accepted.size)
        assertEquals(listOf("Pedro [Grupo D]"), skipped)
    }
}
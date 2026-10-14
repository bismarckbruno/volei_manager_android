package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TextSearchTest {
    @Test
    fun normalizeForSearch_removesAccentsAndLowercases() {
        assertEquals("silvio joao", normalizeForSearch("Sílvio JOÃO"))
        assertTrue("Sílvio".containsIgnoreDiacritics("silv"))
    }

    @Test
    fun faqNormalize_removesAccents() {
        assertEquals("posicao", FaqSearch.normalize("Posição"))
    }

    @Test
    fun publicId_isRandomUuidV4() {
        val a = Player(name = "A", groupName = "G").publicId
        val b = Player(name = "B", groupName = "G").publicId
        assertNotEquals(a, b)
        assertTrue(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$").matches(a), a)
    }
}

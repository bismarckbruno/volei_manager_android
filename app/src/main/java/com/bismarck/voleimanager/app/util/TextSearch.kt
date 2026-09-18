package com.bismarck.voleimanager.app.util

import java.text.Normalizer
import java.util.Locale

/**
 * Accent/diacritic-insensitive text search helpers.
 *
 * Used to match player names regardless of acute/grave accents, tildes, umlauts, etc.,
 * so searching "silvio" also finds "Sílvio".
 */
private val diacriticsRegex = Regex("\\p{M}+")

fun normalizeForSearch(text: String): String {
    val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
    return decomposed.replace(diacriticsRegex, "").lowercase(Locale.ROOT)
}

fun String.containsIgnoreDiacritics(query: String): Boolean {
    if (query.isEmpty()) return true
    return normalizeForSearch(this).contains(normalizeForSearch(query))
}

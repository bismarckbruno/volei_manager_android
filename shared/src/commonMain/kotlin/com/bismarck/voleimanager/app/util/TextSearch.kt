package com.bismarck.voleimanager.app.util


/**
 * Accent/diacritic-insensitive text search helpers.
 *
 * Used to match player names regardless of acute/grave accents, tildes, umlauts, etc.,
 * so searching "silvio" also finds "Sílvio".
 */
private val diacriticsRegex = Regex("\\p{M}+")

fun normalizeForSearch(text: String): String {
    val decomposed = decomposeNfd(text)
    return decomposed.replace(diacriticsRegex, "").lowercase()
}

fun String.containsIgnoreDiacritics(query: String): Boolean {
    if (query.isEmpty()) return true
    return normalizeForSearch(this).contains(normalizeForSearch(query))
}

package com.bismarck.voleimanager.app.util

/** Decomposição Unicode canônica (NFD), usada para remover acentos em buscas. */
internal expect fun decomposeNfd(text: String): String

/** Formata um número sem casas decimais, com separador de milhar do locale atual. */
internal expect fun formatWholeNumber(value: Double): String

/** Duas casas decimais com ponto, independente do locale (equivale a `"%.2f"` em `Locale.US`). */
fun formatTwoDecimals(value: Double): String {
    val negative = value < 0
    val cents = kotlin.math.floor(kotlin.math.abs(value) * 100 + 0.5).toLong()
    val text = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
    return if (negative && cents != 0L) "-$text" else text
}

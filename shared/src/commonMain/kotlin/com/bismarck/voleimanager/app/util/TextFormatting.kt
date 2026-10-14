package com.bismarck.voleimanager.app.util

/** Decomposição Unicode canônica (NFD), usada para remover acentos em buscas. */
internal expect fun decomposeNfd(text: String): String

/** Formata um número sem casas decimais, com separador de milhar do locale atual. */
internal expect fun formatWholeNumber(value: Double): String

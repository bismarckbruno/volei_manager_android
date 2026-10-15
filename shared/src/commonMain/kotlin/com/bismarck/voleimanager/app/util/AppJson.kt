package com.bismarck.voleimanager.app.util

import kotlinx.serialization.json.Json

/**
 * Configuração JSON única do app (substitui o Gson), mantendo o formato que o Gson produzia/aceitava:
 * - [Json.encodeDefaults]: grava todos os campos, inclusive os iguais ao valor padrão (o Gson
 *   sempre gravava; versões antigas do app que leem o backup dependem disso).
 * - [Json.explicitNulls] = false: omite campos nulos ao gravar (como o Gson) e trata campo anulável
 *   ausente como null ao ler.
 * - [Json.ignoreUnknownKeys]: backups/estados gravados por versões mais novas continuam abrindo.
 * - [Json.coerceInputValues]: `null` em campo não anulável com valor padrão usa o padrão.
 */
val AppJson: Json = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

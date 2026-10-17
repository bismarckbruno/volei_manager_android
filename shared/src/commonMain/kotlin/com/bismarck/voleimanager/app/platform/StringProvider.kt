package com.bismarck.voleimanager.app.platform

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/**
 * Resolve strings localizadas (composeResources) fora de composables, como mensagens do ViewModel.
 * Em composables, prefira `stringResource`.
 */
interface StringProvider {
    fun get(resource: StringResource, vararg args: Any): String
}

/** Implementação padrão: lê o recurso do idioma atual do dispositivo. */
object AppStrings : StringProvider {
    override fun get(resource: StringResource, vararg args: Any): String =
        runBlockingCompat { getString(resource, *args) }
}

internal expect fun <T> runBlockingCompat(block: suspend () -> T): T

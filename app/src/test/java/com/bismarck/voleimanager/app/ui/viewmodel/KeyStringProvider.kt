package com.bismarck.voleimanager.app.ui.viewmodel

import com.bismarck.voleimanager.app.platform.StringProvider
import org.jetbrains.compose.resources.StringResource

/** Os recursos do Compose não carregam no Robolectric; devolve a chave + argumentos. */
object KeyStringProvider : StringProvider {
    override fun get(resource: StringResource, vararg args: Any): String =
        (listOf(resource.key) + args.map { it.toString() }).joinToString(" ")
}

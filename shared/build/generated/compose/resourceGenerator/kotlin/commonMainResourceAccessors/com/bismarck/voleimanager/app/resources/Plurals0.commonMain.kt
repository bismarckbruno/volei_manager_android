@file:OptIn(InternalResourceApi::class)

package com.bismarck.voleimanager.app.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/com.bismarck.voleimanager.app.resources/"

public val Res.plurals.cloud_sync_groups_limit_label: PluralStringResource by lazy {
      PluralStringResource("plurals:cloud_sync_groups_limit_label", "cloud_sync_groups_limit_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 10, 137),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 10, 137),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 10, 121),
      ))
    }

public val Res.plurals.live_screen_spectator_groups_count: PluralStringResource by lazy {
      PluralStringResource("plurals:live_screen_spectator_groups_count", "live_screen_spectator_groups_count", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 148, 158),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 148, 166),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 132, 146),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainPlurals0Resources(map: MutableMap<String, PluralStringResource>) {
  map.put("cloud_sync_groups_limit_label", Res.plurals.cloud_sync_groups_limit_label)
  map.put("live_screen_spectator_groups_count", Res.plurals.live_screen_spectator_groups_count)
}

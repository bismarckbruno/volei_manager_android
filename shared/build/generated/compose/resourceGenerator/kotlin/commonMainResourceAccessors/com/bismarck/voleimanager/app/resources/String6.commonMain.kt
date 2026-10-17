@file:OptIn(InternalResourceApi::class)

package com.bismarck.voleimanager.app.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceItem
import org.jetbrains.compose.resources.StringResource

private const val MD: String = "composeResources/com.bismarck.voleimanager.app.resources/"

public val Res.string.team_bench_parentheses: StringResource by lazy {
      StringResource("string:team_bench_parentheses", "team_bench_parentheses", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67806, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65958, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61194, 46),
      ))
    }

public val Res.string.team_colors_group_hint: StringResource by lazy {
      StringResource("string:team_colors_group_hint", "team_colors_group_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67888, 150),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66032, 142),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61264, 146),
      ))
    }

public val Res.string.team_colors_group_title: StringResource by lazy {
      StringResource("string:team_colors_group_title", "team_colors_group_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68039, 55),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66175, 51),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61411, 47),
      ))
    }

public val Res.string.team_colors_hint: StringResource by lazy {
      StringResource("string:team_colors_hint", "team_colors_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68095, 112),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66227, 108),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61459, 100),
      ))
    }

public val Res.string.team_colors_locked_hint: StringResource by lazy {
      StringResource("string:team_colors_locked_hint", "team_colors_locked_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68208, 151),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66336, 131),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61560, 111),
      ))
    }

public val Res.string.team_colors_personal_override_hint: StringResource by lazy {
      StringResource("string:team_colors_personal_override_hint", "team_colors_personal_override_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68360, 186),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66468, 174),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61672, 162),
      ))
    }

public val Res.string.team_colors_personal_override_title: StringResource by lazy {
      StringResource("string:team_colors_personal_override_title", "team_colors_personal_override_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68547, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66643, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61835, 59),
      ))
    }

public val Res.string.team_colors_title: StringResource by lazy {
      StringResource("string:team_colors_title", "team_colors_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68611, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66703, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61895, 41),
      ))
    }

public val Res.string.team_x: StringResource by lazy {
      StringResource("string:team_x", "team_x", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68713, 30),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66793, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61969, 26),
      ))
    }

public val Res.string.team_x_wins: StringResource by lazy {
      StringResource("string:team_x_wins", "team_x_wins", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68669, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66749, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61937, 31),
      ))
    }

public val Res.string.telemetry_consent_allow: StringResource by lazy {
      StringResource("string:telemetry_consent_allow", "telemetry_consent_allow", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68744, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66820, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61996, 39),
      ))
    }

public val Res.string.telemetry_consent_body: StringResource by lazy {
      StringResource("string:telemetry_consent_body", "telemetry_consent_body", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 68788, 258),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66864, 250),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62036, 206),
      ))
    }

public val Res.string.telemetry_consent_deny: StringResource by lazy {
      StringResource("string:telemetry_consent_deny", "telemetry_consent_deny", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69047, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67115, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62243, 42),
      ))
    }

public val Res.string.telemetry_consent_menu_item: StringResource by lazy {
      StringResource("string:telemetry_consent_menu_item", "telemetry_consent_menu_item", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69090, 79),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67162, 83),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62286, 71),
      ))
    }

public val Res.string.telemetry_consent_menu_tooltip: StringResource by lazy {
      StringResource("string:telemetry_consent_menu_tooltip", "telemetry_consent_menu_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69170, 310),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67246, 302),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62358, 250),
      ))
    }

public val Res.string.telemetry_consent_title: StringResource by lazy {
      StringResource("string:telemetry_consent_title", "telemetry_consent_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69481, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67549, 67),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62609, 55),
      ))
    }

public val Res.string.template_example_name: StringResource by lazy {
      StringResource("string:template_example_name", "template_example_name", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69545, 89),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67617, 89),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62665, 85),
      ))
    }

public val Res.string.template_position_hint: StringResource by lazy {
      StringResource("string:template_position_hint", "template_position_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69635, 334),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 67707, 318),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 62751, 310),
      ))
    }

public val Res.string.template_secondary_position_hint: StringResource by lazy {
      StringResource("string:template_secondary_position_hint", "template_secondary_position_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 69970, 240),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 68026, 232),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 63062, 232),
      ))
    }

public val Res.string.terms_of_use: StringResource by lazy {
      StringResource("string:terms_of_use", "terms_of_use", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74390, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72186, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66734, 36),
      ))
    }

public val Res.string.terms_of_use_text: StringResource by lazy {
      StringResource("string:terms_of_use_text", "terms_of_use_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 70211, 4053),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 68259, 3805),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 63295, 3325),
      ))
    }

public val Res.string.terms_of_use_url: StringResource by lazy {
      StringResource("string:terms_of_use_url", "terms_of_use_url", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74265, 124),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72065, 120),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66621, 112),
      ))
    }

public val Res.string.theme: StringResource by lazy {
      StringResource("string:theme", "theme", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74523, 21),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72315, 21),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66855, 21),
      ))
    }

public val Res.string.theme_dark: StringResource by lazy {
      StringResource("string:theme_dark", "theme_dark", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74435, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72227, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66771, 26),
      ))
    }

public val Res.string.theme_light: StringResource by lazy {
      StringResource("string:theme_light", "theme_light", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74462, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72254, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66798, 27),
      ))
    }

public val Res.string.theme_system: StringResource by lazy {
      StringResource("string:theme_system", "theme_system", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74490, 32),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72282, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66826, 28),
      ))
    }

public val Res.string.time_played: StringResource by lazy {
      StringResource("string:time_played", "time_played", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74545, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72337, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66877, 35),
      ))
    }

public val Res.string.to_start_add_players: StringResource by lazy {
      StringResource("string:to_start_add_players", "to_start_add_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74581, 156),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72373, 136),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 66913, 148),
      ))
    }

public val Res.string.transfer_ownership_cancel: StringResource by lazy {
      StringResource("string:transfer_ownership_cancel", "transfer_ownership_cancel", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74738, 45),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72510, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67062, 41),
      ))
    }

public val Res.string.transfer_ownership_confirm: StringResource by lazy {
      StringResource("string:transfer_ownership_confirm", "transfer_ownership_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74784, 66),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72556, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67104, 58),
      ))
    }

public val Res.string.transfer_ownership_email_label: StringResource by lazy {
      StringResource("string:transfer_ownership_email_label", "transfer_ownership_email_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74851, 86),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72623, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67163, 62),
      ))
    }

public val Res.string.transfer_ownership_hint: StringResource by lazy {
      StringResource("string:transfer_ownership_hint", "transfer_ownership_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 74938, 267),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72690, 255),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67226, 235),
      ))
    }

public val Res.string.transfer_ownership_menu_item: StringResource by lazy {
      StringResource("string:transfer_ownership_menu_item", "transfer_ownership_menu_item", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75206, 68),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 72946, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67462, 64),
      ))
    }

public val Res.string.transfer_ownership_pending_label: StringResource by lazy {
      StringResource("string:transfer_ownership_pending_label", "transfer_ownership_pending_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75275, 84),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73011, 84),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67527, 72),
      ))
    }

public val Res.string.transfer_ownership_title: StringResource by lazy {
      StringResource("string:transfer_ownership_title", "transfer_ownership_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75360, 72),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73096, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67600, 68),
      ))
    }

public val Res.string.uncheck_all: StringResource by lazy {
      StringResource("string:uncheck_all", "uncheck_all", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75433, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73161, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67669, 31),
      ))
    }

public val Res.string.undo: StringResource by lazy {
      StringResource("string:undo", "undo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75525, 24),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73253, 24),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67749, 20),
      ))
    }

public val Res.string.undo_last_match: StringResource by lazy {
      StringResource("string:undo_last_match", "undo_last_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75465, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73193, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67701, 47),
      ))
    }

public val Res.string.update_downloaded_action: StringResource by lazy {
      StringResource("string:update_downloaded_action", "update_downloaded_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75550, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73278, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67770, 44),
      ))
    }

public val Res.string.update_downloaded_message: StringResource by lazy {
      StringResource("string:update_downloaded_message", "update_downloaded_message", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75595, 113),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73323, 113),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67815, 105),
      ))
    }

public val Res.string.use_score: StringResource by lazy {
      StringResource("string:use_score", "use_score", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75875, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73599, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68083, 29),
      ))
    }

public val Res.string.use_score_tooltip: StringResource by lazy {
      StringResource("string:use_score_tooltip", "use_score_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75709, 165),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73437, 161),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 67921, 161),
      ))
    }

public val Res.string.user_profile_assistant: StringResource by lazy {
      StringResource("string:user_profile_assistant", "user_profile_assistant", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76033, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73761, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68221, 42),
      ))
    }

public val Res.string.user_profile_assistant_desc: StringResource by lazy {
      StringResource("string:user_profile_assistant_desc", "user_profile_assistant_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 75913, 119),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73633, 127),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68113, 107),
      ))
    }

public val Res.string.user_profile_organizer: StringResource by lazy {
      StringResource("string:user_profile_organizer", "user_profile_organizer", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76196, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73928, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68384, 46),
      ))
    }

public val Res.string.user_profile_organizer_desc: StringResource by lazy {
      StringResource("string:user_profile_organizer_desc", "user_profile_organizer_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76076, 119),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73804, 123),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68264, 119),
      ))
    }

public val Res.string.user_profile_viewer: StringResource by lazy {
      StringResource("string:user_profile_viewer", "user_profile_viewer", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76360, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74092, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68540, 35),
      ))
    }

public val Res.string.user_profile_viewer_desc: StringResource by lazy {
      StringResource("string:user_profile_viewer_desc", "user_profile_viewer_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76247, 112),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 73979, 112),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68431, 108),
      ))
    }

public val Res.string.verification_email_sent: StringResource by lazy {
      StringResource("string:verification_email_sent", "verification_email_sent", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76404, 115),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74136, 115),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68576, 91),
      ))
    }

public val Res.string.victories: StringResource by lazy {
      StringResource("string:victories", "victories", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76520, 29),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74252, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68668, 29),
      ))
    }

public val Res.string.victorious_team: StringResource by lazy {
      StringResource("string:victorious_team", "victorious_team", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76550, 67),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74282, 55),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68698, 55),
      ))
    }

public val Res.string.victory: StringResource by lazy {
      StringResource("string:victory", "victory", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76916, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74636, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69028, 27),
      ))
    }

public val Res.string.victory_limit: StringResource by lazy {
      StringResource("string:victory_limit", "victory_limit", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76824, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74544, 57),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68944, 49),
      ))
    }

public val Res.string.victory_limit_tooltip: StringResource by lazy {
      StringResource("string:victory_limit_tooltip", "victory_limit_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76618, 205),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74338, 205),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68754, 189),
      ))
    }

public val Res.string.victory_short: StringResource by lazy {
      StringResource("string:victory_short", "victory_short", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76882, 33),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74602, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 68994, 33),
      ))
    }

public val Res.string.view_in_browser: StringResource by lazy {
      StringResource("string:view_in_browser", "view_in_browser", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76944, 51),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74664, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69056, 43),
      ))
    }

public val Res.string.vs: StringResource by lazy {
      StringResource("string:vs", "vs", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 76996, 14),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74712, 14),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69100, 14),
      ))
    }

public val Res.string.waiting: StringResource by lazy {
      StringResource("string:waiting", "waiting", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77147, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74863, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69255, 27),
      ))
    }

public val Res.string.waiting_list: StringResource by lazy {
      StringResource("string:waiting_list", "waiting_list", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77058, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74774, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69158, 48),
      ))
    }

public val Res.string.waiting_list_label: StringResource by lazy {
      StringResource("string:waiting_list_label", "waiting_list_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77011, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74727, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69115, 42),
      ))
    }

public val Res.string.waiting_parentheses: StringResource by lazy {
      StringResource("string:waiting_parentheses", "waiting_parentheses", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77103, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74819, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69207, 47),
      ))
    }

public val Res.string.went_to_end_queue: StringResource by lazy {
      StringResource("string:went_to_end_queue", "went_to_end_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77175, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74891, 65),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69283, 69),
      ))
    }

public val Res.string.went_to_start_queue: StringResource by lazy {
      StringResource("string:went_to_start_queue", "went_to_start_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77241, 71),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 74957, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69353, 75),
      ))
    }

public val Res.string.wildcard_hint: StringResource by lazy {
      StringResource("string:wildcard_hint", "wildcard_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77313, 133),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75029, 121),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69429, 149),
      ))
    }

public val Res.string.winner: StringResource by lazy {
      StringResource("string:winner", "winner", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77684, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75380, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69808, 30),
      ))
    }

public val Res.string.winner_score_validation_snackbar: StringResource by lazy {
      StringResource("string:winner_score_validation_snackbar", "winner_score_validation_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77447, 204),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75151, 196),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69579, 200),
      ))
    }

public val Res.string.winner_word: StringResource by lazy {
      StringResource("string:winner_word", "winner_word", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77652, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75348, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69780, 27),
      ))
    }

public val Res.string.x_matches: StringResource by lazy {
      StringResource("string:x_matches", "x_matches", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77711, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75407, 37),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69839, 33),
      ))
    }

public val Res.string.x_victories: StringResource by lazy {
      StringResource("string:x_victories", "x_victories", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77749, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75445, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69873, 39),
      ))
    }

public val Res.string.yes: StringResource by lazy {
      StringResource("string:yes", "yes", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77823, 15),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75519, 15),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69947, 15),
      ))
    }

public val Res.string.yes_clear: StringResource by lazy {
      StringResource("string:yes_clear", "yes_clear", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 77789, 33),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 75485, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 69913, 33),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString6Resources(map: MutableMap<String, StringResource>) {
  map.put("team_bench_parentheses", Res.string.team_bench_parentheses)
  map.put("team_colors_group_hint", Res.string.team_colors_group_hint)
  map.put("team_colors_group_title", Res.string.team_colors_group_title)
  map.put("team_colors_hint", Res.string.team_colors_hint)
  map.put("team_colors_locked_hint", Res.string.team_colors_locked_hint)
  map.put("team_colors_personal_override_hint", Res.string.team_colors_personal_override_hint)
  map.put("team_colors_personal_override_title", Res.string.team_colors_personal_override_title)
  map.put("team_colors_title", Res.string.team_colors_title)
  map.put("team_x", Res.string.team_x)
  map.put("team_x_wins", Res.string.team_x_wins)
  map.put("telemetry_consent_allow", Res.string.telemetry_consent_allow)
  map.put("telemetry_consent_body", Res.string.telemetry_consent_body)
  map.put("telemetry_consent_deny", Res.string.telemetry_consent_deny)
  map.put("telemetry_consent_menu_item", Res.string.telemetry_consent_menu_item)
  map.put("telemetry_consent_menu_tooltip", Res.string.telemetry_consent_menu_tooltip)
  map.put("telemetry_consent_title", Res.string.telemetry_consent_title)
  map.put("template_example_name", Res.string.template_example_name)
  map.put("template_position_hint", Res.string.template_position_hint)
  map.put("template_secondary_position_hint", Res.string.template_secondary_position_hint)
  map.put("terms_of_use", Res.string.terms_of_use)
  map.put("terms_of_use_text", Res.string.terms_of_use_text)
  map.put("terms_of_use_url", Res.string.terms_of_use_url)
  map.put("theme", Res.string.theme)
  map.put("theme_dark", Res.string.theme_dark)
  map.put("theme_light", Res.string.theme_light)
  map.put("theme_system", Res.string.theme_system)
  map.put("time_played", Res.string.time_played)
  map.put("to_start_add_players", Res.string.to_start_add_players)
  map.put("transfer_ownership_cancel", Res.string.transfer_ownership_cancel)
  map.put("transfer_ownership_confirm", Res.string.transfer_ownership_confirm)
  map.put("transfer_ownership_email_label", Res.string.transfer_ownership_email_label)
  map.put("transfer_ownership_hint", Res.string.transfer_ownership_hint)
  map.put("transfer_ownership_menu_item", Res.string.transfer_ownership_menu_item)
  map.put("transfer_ownership_pending_label", Res.string.transfer_ownership_pending_label)
  map.put("transfer_ownership_title", Res.string.transfer_ownership_title)
  map.put("uncheck_all", Res.string.uncheck_all)
  map.put("undo", Res.string.undo)
  map.put("undo_last_match", Res.string.undo_last_match)
  map.put("update_downloaded_action", Res.string.update_downloaded_action)
  map.put("update_downloaded_message", Res.string.update_downloaded_message)
  map.put("use_score", Res.string.use_score)
  map.put("use_score_tooltip", Res.string.use_score_tooltip)
  map.put("user_profile_assistant", Res.string.user_profile_assistant)
  map.put("user_profile_assistant_desc", Res.string.user_profile_assistant_desc)
  map.put("user_profile_organizer", Res.string.user_profile_organizer)
  map.put("user_profile_organizer_desc", Res.string.user_profile_organizer_desc)
  map.put("user_profile_viewer", Res.string.user_profile_viewer)
  map.put("user_profile_viewer_desc", Res.string.user_profile_viewer_desc)
  map.put("verification_email_sent", Res.string.verification_email_sent)
  map.put("victories", Res.string.victories)
  map.put("victorious_team", Res.string.victorious_team)
  map.put("victory", Res.string.victory)
  map.put("victory_limit", Res.string.victory_limit)
  map.put("victory_limit_tooltip", Res.string.victory_limit_tooltip)
  map.put("victory_short", Res.string.victory_short)
  map.put("view_in_browser", Res.string.view_in_browser)
  map.put("vs", Res.string.vs)
  map.put("waiting", Res.string.waiting)
  map.put("waiting_list", Res.string.waiting_list)
  map.put("waiting_list_label", Res.string.waiting_list_label)
  map.put("waiting_parentheses", Res.string.waiting_parentheses)
  map.put("went_to_end_queue", Res.string.went_to_end_queue)
  map.put("went_to_start_queue", Res.string.went_to_start_queue)
  map.put("wildcard_hint", Res.string.wildcard_hint)
  map.put("winner", Res.string.winner)
  map.put("winner_score_validation_snackbar", Res.string.winner_score_validation_snackbar)
  map.put("winner_word", Res.string.winner_word)
  map.put("x_matches", Res.string.x_matches)
  map.put("x_victories", Res.string.x_victories)
  map.put("yes", Res.string.yes)
  map.put("yes_clear", Res.string.yes_clear)
}

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

public val Res.string.group_type_recreational: StringResource by lazy {
      StringResource("string:group_type_recreational", "group_type_recreational", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36247, 47),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 34907, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 32731, 47),
      ))
    }

public val Res.string.group_type_recreational_desc: StringResource by lazy {
      StringResource("string:group_type_recreational_desc", "group_type_recreational_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36074, 172),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 34726, 180),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 32574, 156),
      ))
    }

public val Res.string.group_type_title: StringResource by lazy {
      StringResource("string:group_type_title", "group_type_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36295, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 34955, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 32779, 40),
      ))
    }

public val Res.string.guarantee_next_match: StringResource by lazy {
      StringResource("string:guarantee_next_match", "guarantee_next_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36340, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35000, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 32820, 56),
      ))
    }

public val Res.string.guarantee_setter: StringResource by lazy {
      StringResource("string:guarantee_setter", "guarantee_setter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37014, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35698, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33454, 48),
      ))
    }

public val Res.string.guarantee_setter_locked_tooltip: StringResource by lazy {
      StringResource("string:guarantee_setter_locked_tooltip", "guarantee_setter_locked_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36405, 275),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35065, 267),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 32877, 255),
      ))
    }

public val Res.string.guarantee_setter_tooltip: StringResource by lazy {
      StringResource("string:guarantee_setter_tooltip", "guarantee_setter_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 36681, 332),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35333, 364),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33133, 320),
      ))
    }

public val Res.string.has_entered_end_queue: StringResource by lazy {
      StringResource("string:has_entered_end_queue", "has_entered_end_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37063, 73),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35751, 69),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33503, 73),
      ))
    }

public val Res.string.has_entered_start_queue: StringResource by lazy {
      StringResource("string:has_entered_start_queue", "has_entered_start_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37137, 79),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35821, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33577, 79),
      ))
    }

public val Res.string.header_info_tooltip: StringResource by lazy {
      StringResource("string:header_info_tooltip", "header_info_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37305, 95),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35981, 95),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33741, 91),
      ))
    }

public val Res.string.header_info_tooltip_no_mode: StringResource by lazy {
      StringResource("string:header_info_tooltip_no_mode", "header_info_tooltip_no_mode", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37217, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 35893, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33657, 83),
      ))
    }

public val Res.string.header_scroll_tooltip: StringResource by lazy {
      StringResource("string:header_scroll_tooltip", "header_scroll_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37401, 161),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36077, 153),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33833, 141),
      ))
    }

public val Res.string.hide_password: StringResource by lazy {
      StringResource("string:hide_password", "hide_password", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37563, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36231, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 33975, 41),
      ))
    }

public val Res.string.history: StringResource by lazy {
      StringResource("string:history", "history", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37613, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36273, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34017, 27),
      ))
    }

public val Res.string.import_csv: StringResource by lazy {
      StringResource("string:import_csv", "import_csv", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37641, 62),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36305, 62),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34045, 58),
      ))
    }

public val Res.string.import_data: StringResource by lazy {
      StringResource("string:import_data", "import_data", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37704, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36368, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34104, 35),
      ))
    }

public val Res.string.import_duplicate_name_dialog_text: StringResource by lazy {
      StringResource("string:import_duplicate_name_dialog_text", "import_duplicate_name_dialog_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 37744, 385),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36408, 369),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34140, 361),
      ))
    }

public val Res.string.import_duplicate_names_detected: StringResource by lazy {
      StringResource("string:import_duplicate_names_detected", "import_duplicate_names_detected", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38130, 107),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36778, 99),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34502, 95),
      ))
    }

public val Res.string.import_error_empty_or_unreadable: StringResource by lazy {
      StringResource("string:import_error_empty_or_unreadable", "import_error_empty_or_unreadable", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38238, 132),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 36878, 140),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34598, 132),
      ))
    }

public val Res.string.import_error_generic: StringResource by lazy {
      StringResource("string:import_error_generic", "import_error_generic", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38371, 120),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37019, 120),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34731, 96),
      ))
    }

public val Res.string.import_error_no_valid_rows: StringResource by lazy {
      StringResource("string:import_error_no_valid_rows", "import_error_no_valid_rows", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38492, 170),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37140, 166),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34828, 130),
      ))
    }

public val Res.string.import_error_xls_unsupported: StringResource by lazy {
      StringResource("string:import_error_xls_unsupported", "import_error_xls_unsupported", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38663, 204),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37307, 200),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 34959, 176),
      ))
    }

public val Res.string.import_keep_first_and_skip: StringResource by lazy {
      StringResource("string:import_keep_first_and_skip", "import_keep_first_and_skip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38868, 74),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37508, 70),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35136, 62),
      ))
    }

public val Res.string.import_merge_action: StringResource by lazy {
      StringResource("string:import_merge_action", "import_merge_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38943, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37579, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35199, 35),
      ))
    }

public val Res.string.import_merge_text: StringResource by lazy {
      StringResource("string:import_merge_text", "import_merge_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 38983, 565),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 37619, 549),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35235, 521),
      ))
    }

public val Res.string.import_merge_title: StringResource by lazy {
      StringResource("string:import_merge_title", "import_merge_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39549, 62),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38169, 58),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35757, 50),
      ))
    }

public val Res.string.import_rename_duplicates: StringResource by lazy {
      StringResource("string:import_rename_duplicates", "import_rename_duplicates", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39612, 60),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38228, 60),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35808, 56),
      ))
    }

public val Res.string.import_text: StringResource by lazy {
      StringResource("string:import_text", "import_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39673, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38289, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35865, 27),
      ))
    }

public val Res.string.import_word: StringResource by lazy {
      StringResource("string:import_word", "import_word", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39705, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38321, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35893, 27),
      ))
    }

public val Res.string.importing: StringResource by lazy {
      StringResource("string:importing", "importing", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39737, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38353, 37),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35921, 33),
      ))
    }

public val Res.string.increase_score: StringResource by lazy {
      StringResource("string:increase_score", "increase_score", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39775, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38391, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35955, 42),
      ))
    }

public val Res.string.initial_elo: StringResource by lazy {
      StringResource("string:initial_elo", "initial_elo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39822, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38434, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 35998, 35),
      ))
    }

public val Res.string.invalid_backup_format: StringResource by lazy {
      StringResource("string:invalid_backup_format", "invalid_backup_format", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39858, 85),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38470, 65),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36034, 57),
      ))
    }

public val Res.string.join_existing_group: StringResource by lazy {
      StringResource("string:join_existing_group", "join_existing_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40168, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38748, 67),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36284, 55),
      ))
    }

public val Res.string.join_existing_group_code_label: StringResource by lazy {
      StringResource("string:join_existing_group_code_label", "join_existing_group_code_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39944, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38536, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36092, 46),
      ))
    }

public val Res.string.join_existing_group_confirm: StringResource by lazy {
      StringResource("string:join_existing_group_confirm", "join_existing_group_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 39995, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38587, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36139, 43),
      ))
    }

public val Res.string.join_existing_group_hint: StringResource by lazy {
      StringResource("string:join_existing_group_hint", "join_existing_group_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40039, 128),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38631, 116),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36183, 100),
      ))
    }

public val Res.string.join_group_already_joined: StringResource by lazy {
      StringResource("string:join_group_already_joined", "join_group_already_joined", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40232, 69),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38816, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36340, 73),
      ))
    }

public val Res.string.join_group_auxiliar_unavailable: StringResource by lazy {
      StringResource("string:join_group_auxiliar_unavailable", "join_group_auxiliar_unavailable", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40302, 111),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 38890, 115),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36414, 115),
      ))
    }

public val Res.string.join_group_invalid_code: StringResource by lazy {
      StringResource("string:join_group_invalid_code", "join_group_invalid_code", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40414, 71),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39006, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36530, 63),
      ))
    }

public val Res.string.join_group_remote_group_name: StringResource by lazy {
      StringResource("string:join_group_remote_group_name", "join_group_remote_group_name", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40486, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39078, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36594, 64),
      ))
    }

public val Res.string.keyboard_arrow_down: StringResource by lazy {
      StringResource("string:keyboard_arrow_down", "keyboard_arrow_down", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40551, 51),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39143, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36659, 55),
      ))
    }

public val Res.string.leave_group: StringResource by lazy {
      StringResource("string:leave_group", "leave_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40603, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39191, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36715, 35),
      ))
    }

public val Res.string.limit_reached_text: StringResource by lazy {
      StringResource("string:limit_reached_text", "limit_reached_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41770, 142),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40298, 138),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37782, 114),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_eq_two_teams: StringResource by lazy {
      StringResource("string:limit_reached_text_rest_selected_eq_two_teams", "limit_reached_text_rest_selected_eq_two_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40842, 177),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39430, 161),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36950, 149),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_eq_two_teams_plus_one: StringResource by
    lazy {
      StringResource("string:limit_reached_text_rest_selected_eq_two_teams_plus_one", "limit_reached_text_rest_selected_eq_two_teams_plus_one", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 40643, 198),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39231, 198),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 36751, 198),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_gt_two_teams_plus_one: StringResource by
    lazy {
      StringResource("string:limit_reached_text_rest_selected_gt_two_teams_plus_one", "limit_reached_text_rest_selected_gt_two_teams_plus_one", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41020, 206),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39592, 202),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37100, 202),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_gte_four_teams: StringResource by lazy {
      StringResource("string:limit_reached_text_rest_selected_gte_four_teams", "limit_reached_text_rest_selected_gte_four_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41227, 223),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39795, 199),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37303, 191),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_gte_three_teams: StringResource by lazy {
      StringResource("string:limit_reached_text_rest_selected_gte_three_teams", "limit_reached_text_rest_selected_gte_three_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41451, 180),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 39995, 168),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37495, 164),
      ))
    }

public val Res.string.limit_reached_text_rest_selected_lt_two_teams: StringResource by lazy {
      StringResource("string:limit_reached_text_rest_selected_lt_two_teams", "limit_reached_text_rest_selected_lt_two_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41632, 137),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40164, 133),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37660, 121),
      ))
    }

public val Res.string.limit_reached_title: StringResource by lazy {
      StringResource("string:limit_reached_title", "limit_reached_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41913, 79),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40437, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37897, 83),
      ))
    }

public val Res.string.live_screen_no_live_state: StringResource by lazy {
      StringResource("string:live_screen_no_live_state", "live_screen_no_live_state", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 41993, 105),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40509, 105),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 37981, 101),
      ))
    }

public val Res.string.live_screen_spectator_no_group: StringResource by lazy {
      StringResource("string:live_screen_spectator_no_group", "live_screen_spectator_no_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42099, 202),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40615, 198),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38083, 178),
      ))
    }

public val Res.string.live_screen_spectator_placeholder: StringResource by lazy {
      StringResource("string:live_screen_spectator_placeholder", "live_screen_spectator_placeholder", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42302, 73),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40814, 77),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38262, 69),
      ))
    }

public val Res.string.live_screen_spectator_title: StringResource by lazy {
      StringResource("string:live_screen_spectator_title", "live_screen_spectator_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42376, 47),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40892, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38332, 43),
      ))
    }

public val Res.string.live_screen_team_a_label: StringResource by lazy {
      StringResource("string:live_screen_team_a_label", "live_screen_team_a_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42424, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40940, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38376, 40),
      ))
    }

public val Res.string.live_screen_team_b_label: StringResource by lazy {
      StringResource("string:live_screen_team_b_label", "live_screen_team_b_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42469, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 40981, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38417, 40),
      ))
    }

public val Res.string.live_screen_waiting_list_empty: StringResource by lazy {
      StringResource("string:live_screen_waiting_list_empty", "live_screen_waiting_list_empty", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42514, 62),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41022, 62),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38458, 58),
      ))
    }

public val Res.string.live_screen_waiting_list_label: StringResource by lazy {
      StringResource("string:live_screen_waiting_list_label", "live_screen_waiting_list_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42577, 58),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41085, 58),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38517, 58),
      ))
    }

public val Res.string.login_confirm: StringResource by lazy {
      StringResource("string:login_confirm", "login_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42636, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41144, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38576, 29),
      ))
    }

public val Res.string.login_switch_to_signup: StringResource by lazy {
      StringResource("string:login_switch_to_signup", "login_switch_to_signup", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42678, 74),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41174, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38606, 70),
      ))
    }

public val Res.string.login_title: StringResource by lazy {
      StringResource("string:login_title", "login_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42753, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41241, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38677, 27),
      ))
    }

public val Res.string.logout: StringResource by lazy {
      StringResource("string:logout", "logout", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42954, 34),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41406, 22),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38842, 26),
      ))
    }

public val Res.string.logout_confirm_text: StringResource by lazy {
      StringResource("string:logout_confirm_text", "logout_confirm_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42793, 107),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41269, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38705, 95),
      ))
    }

public val Res.string.logout_confirm_title: StringResource by lazy {
      StringResource("string:logout_confirm_title", "logout_confirm_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42901, 52),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41357, 48),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38801, 40),
      ))
    }

public val Res.string.manual_teams: StringResource by lazy {
      StringResource("string:manual_teams", "manual_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 42989, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41429, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38869, 52),
      ))
    }

public val Res.string.match: StringResource by lazy {
      StringResource("string:match", "match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43162, 25),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41598, 25),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39034, 21),
      ))
    }

public val Res.string.matches: StringResource by lazy {
      StringResource("string:matches", "matches", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43134, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41570, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39006, 27),
      ))
    }

public val Res.string.matches_date: StringResource by lazy {
      StringResource("string:matches_date", "matches_date", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43046, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41482, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38922, 40),
      ))
    }

public val Res.string.matches_played: StringResource by lazy {
      StringResource("string:matches_played", "matches_played", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43087, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41523, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 38963, 42),
      ))
    }

public val Res.string.max_guaranteed_next_match_players: StringResource by lazy {
      StringResource("string:max_guaranteed_next_match_players", "max_guaranteed_next_match_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43188, 129),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41624, 137),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39056, 117),
      ))
    }

public val Res.string.min_priority: StringResource by lazy {
      StringResource("string:min_priority", "min_priority", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43447, 60),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41891, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39303, 52),
      ))
    }

public val Res.string.min_priority_tooltip: StringResource by lazy {
      StringResource("string:min_priority_tooltip", "min_priority_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43318, 128),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41762, 128),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39174, 128),
      ))
    }

public val Res.string.mode_rebalance: StringResource by lazy {
      StringResource("string:mode_rebalance", "mode_rebalance", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43667, 38),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42171, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39535, 42),
      ))
    }

public val Res.string.mode_rebalance_tooltip: StringResource by lazy {
      StringResource("string:mode_rebalance_tooltip", "mode_rebalance_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43508, 158),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 41948, 222),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39356, 178),
      ))
    }

public val Res.string.mode_rest: StringResource by lazy {
      StringResource("string:mode_rest", "mode_rest", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43916, 29),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42416, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39784, 25),
      ))
    }

public val Res.string.mode_rest_tooltip: StringResource by lazy {
      StringResource("string:mode_rest_tooltip", "mode_rest_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43706, 209),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42214, 201),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39578, 205),
      ))
    }

public val Res.string.move_down: StringResource by lazy {
      StringResource("string:move_down", "move_down", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43946, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42446, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39810, 29),
      ))
    }

public val Res.string.move_up: StringResource by lazy {
      StringResource("string:move_up", "move_up", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 43988, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42476, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39840, 27),
      ))
    }

public val Res.string.name: StringResource by lazy {
      StringResource("string:name", "name", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44028, 20),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42504, 20),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39868, 20),
      ))
    }

public val Res.string.new_email_label: StringResource by lazy {
      StringResource("string:new_email_label", "new_email_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44049, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42525, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39889, 39),
      ))
    }

public val Res.string.new_name: StringResource by lazy {
      StringResource("string:new_name", "new_name", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44089, 32),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42565, 28),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39929, 28),
      ))
    }

public val Res.string.new_password_label: StringResource by lazy {
      StringResource("string:new_password_label", "new_password_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44122, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42594, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 39958, 42),
      ))
    }

public val Res.string.new_registration: StringResource by lazy {
      StringResource("string:new_registration", "new_registration", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44173, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42637, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40001, 48),
      ))
    }

public val Res.string.newest_first: StringResource by lazy {
      StringResource("string:newest_first", "newest_first", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44218, 52),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42682, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40050, 36),
      ))
    }

public val Res.string.nickname_hint: StringResource by lazy {
      StringResource("string:nickname_hint", "nickname_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44271, 113),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42735, 89),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40087, 93),
      ))
    }

public val Res.string.nickname_label: StringResource by lazy {
      StringResource("string:nickname_label", "nickname_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44385, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42825, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40181, 38),
      ))
    }

public val Res.string.no: StringResource by lazy {
      StringResource("string:no", "no", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44944, 14),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43364, 18),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40648, 14),
      ))
    }

public val Res.string.no_entries: StringResource by lazy {
      StringResource("string:no_entries", "no_entries", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44428, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42868, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40220, 34),
      ))
    }

public val Res.string.no_games: StringResource by lazy {
      StringResource("string:no_games", "no_games", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44471, 32),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42907, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40255, 32),
      ))
    }

public val Res.string.no_matches: StringResource by lazy {
      StringResource("string:no_matches", "no_matches", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44565, 34),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43001, 34),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40337, 34),
      ))
    }

public val Res.string.no_matches_found: StringResource by lazy {
      StringResource("string:no_matches_found", "no_matches_found", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44504, 60),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 42940, 60),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40288, 48),
      ))
    }

public val Res.string.no_player_waiting: StringResource by lazy {
      StringResource("string:no_player_waiting", "no_player_waiting", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44600, 73),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43036, 69),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40372, 65),
      ))
    }

public val Res.string.no_players: StringResource by lazy {
      StringResource("string:no_players", "no_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44762, 58),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43190, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40502, 42),
      ))
    }

public val Res.string.no_players_swap: StringResource by lazy {
      StringResource("string:no_players_swap", "no_players_swap", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44674, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43106, 83),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40438, 63),
      ))
    }

public val Res.string.no_victories: StringResource by lazy {
      StringResource("string:no_victories", "no_victories", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44821, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43245, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40545, 36),
      ))
    }

public val Res.string.none_selected: StringResource by lazy {
      StringResource("string:none_selected", "none_selected", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44866, 77),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43290, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40582, 65),
      ))
    }

public val Res.string.off_waitlist: StringResource by lazy {
      StringResource("string:off_waitlist", "off_waitlist", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 44959, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43383, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40663, 56),
      ))
    }

public val Res.string.ok: StringResource by lazy {
      StringResource("string:ok", "ok", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45024, 14),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43440, 14),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40720, 14),
      ))
    }

public val Res.string.oldest_first: StringResource by lazy {
      StringResource("string:oldest_first", "oldest_first", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45039, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43455, 48),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40735, 36),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString3Resources(map: MutableMap<String, StringResource>) {
  map.put("group_type_recreational", Res.string.group_type_recreational)
  map.put("group_type_recreational_desc", Res.string.group_type_recreational_desc)
  map.put("group_type_title", Res.string.group_type_title)
  map.put("guarantee_next_match", Res.string.guarantee_next_match)
  map.put("guarantee_setter", Res.string.guarantee_setter)
  map.put("guarantee_setter_locked_tooltip", Res.string.guarantee_setter_locked_tooltip)
  map.put("guarantee_setter_tooltip", Res.string.guarantee_setter_tooltip)
  map.put("has_entered_end_queue", Res.string.has_entered_end_queue)
  map.put("has_entered_start_queue", Res.string.has_entered_start_queue)
  map.put("header_info_tooltip", Res.string.header_info_tooltip)
  map.put("header_info_tooltip_no_mode", Res.string.header_info_tooltip_no_mode)
  map.put("header_scroll_tooltip", Res.string.header_scroll_tooltip)
  map.put("hide_password", Res.string.hide_password)
  map.put("history", Res.string.history)
  map.put("import_csv", Res.string.import_csv)
  map.put("import_data", Res.string.import_data)
  map.put("import_duplicate_name_dialog_text", Res.string.import_duplicate_name_dialog_text)
  map.put("import_duplicate_names_detected", Res.string.import_duplicate_names_detected)
  map.put("import_error_empty_or_unreadable", Res.string.import_error_empty_or_unreadable)
  map.put("import_error_generic", Res.string.import_error_generic)
  map.put("import_error_no_valid_rows", Res.string.import_error_no_valid_rows)
  map.put("import_error_xls_unsupported", Res.string.import_error_xls_unsupported)
  map.put("import_keep_first_and_skip", Res.string.import_keep_first_and_skip)
  map.put("import_merge_action", Res.string.import_merge_action)
  map.put("import_merge_text", Res.string.import_merge_text)
  map.put("import_merge_title", Res.string.import_merge_title)
  map.put("import_rename_duplicates", Res.string.import_rename_duplicates)
  map.put("import_text", Res.string.import_text)
  map.put("import_word", Res.string.import_word)
  map.put("importing", Res.string.importing)
  map.put("increase_score", Res.string.increase_score)
  map.put("initial_elo", Res.string.initial_elo)
  map.put("invalid_backup_format", Res.string.invalid_backup_format)
  map.put("join_existing_group", Res.string.join_existing_group)
  map.put("join_existing_group_code_label", Res.string.join_existing_group_code_label)
  map.put("join_existing_group_confirm", Res.string.join_existing_group_confirm)
  map.put("join_existing_group_hint", Res.string.join_existing_group_hint)
  map.put("join_group_already_joined", Res.string.join_group_already_joined)
  map.put("join_group_auxiliar_unavailable", Res.string.join_group_auxiliar_unavailable)
  map.put("join_group_invalid_code", Res.string.join_group_invalid_code)
  map.put("join_group_remote_group_name", Res.string.join_group_remote_group_name)
  map.put("keyboard_arrow_down", Res.string.keyboard_arrow_down)
  map.put("leave_group", Res.string.leave_group)
  map.put("limit_reached_text", Res.string.limit_reached_text)
  map.put("limit_reached_text_rest_selected_eq_two_teams", Res.string.limit_reached_text_rest_selected_eq_two_teams)
  map.put("limit_reached_text_rest_selected_eq_two_teams_plus_one", Res.string.limit_reached_text_rest_selected_eq_two_teams_plus_one)
  map.put("limit_reached_text_rest_selected_gt_two_teams_plus_one", Res.string.limit_reached_text_rest_selected_gt_two_teams_plus_one)
  map.put("limit_reached_text_rest_selected_gte_four_teams", Res.string.limit_reached_text_rest_selected_gte_four_teams)
  map.put("limit_reached_text_rest_selected_gte_three_teams", Res.string.limit_reached_text_rest_selected_gte_three_teams)
  map.put("limit_reached_text_rest_selected_lt_two_teams", Res.string.limit_reached_text_rest_selected_lt_two_teams)
  map.put("limit_reached_title", Res.string.limit_reached_title)
  map.put("live_screen_no_live_state", Res.string.live_screen_no_live_state)
  map.put("live_screen_spectator_no_group", Res.string.live_screen_spectator_no_group)
  map.put("live_screen_spectator_placeholder", Res.string.live_screen_spectator_placeholder)
  map.put("live_screen_spectator_title", Res.string.live_screen_spectator_title)
  map.put("live_screen_team_a_label", Res.string.live_screen_team_a_label)
  map.put("live_screen_team_b_label", Res.string.live_screen_team_b_label)
  map.put("live_screen_waiting_list_empty", Res.string.live_screen_waiting_list_empty)
  map.put("live_screen_waiting_list_label", Res.string.live_screen_waiting_list_label)
  map.put("login_confirm", Res.string.login_confirm)
  map.put("login_switch_to_signup", Res.string.login_switch_to_signup)
  map.put("login_title", Res.string.login_title)
  map.put("logout", Res.string.logout)
  map.put("logout_confirm_text", Res.string.logout_confirm_text)
  map.put("logout_confirm_title", Res.string.logout_confirm_title)
  map.put("manual_teams", Res.string.manual_teams)
  map.put("match", Res.string.match)
  map.put("matches", Res.string.matches)
  map.put("matches_date", Res.string.matches_date)
  map.put("matches_played", Res.string.matches_played)
  map.put("max_guaranteed_next_match_players", Res.string.max_guaranteed_next_match_players)
  map.put("min_priority", Res.string.min_priority)
  map.put("min_priority_tooltip", Res.string.min_priority_tooltip)
  map.put("mode_rebalance", Res.string.mode_rebalance)
  map.put("mode_rebalance_tooltip", Res.string.mode_rebalance_tooltip)
  map.put("mode_rest", Res.string.mode_rest)
  map.put("mode_rest_tooltip", Res.string.mode_rest_tooltip)
  map.put("move_down", Res.string.move_down)
  map.put("move_up", Res.string.move_up)
  map.put("name", Res.string.name)
  map.put("new_email_label", Res.string.new_email_label)
  map.put("new_name", Res.string.new_name)
  map.put("new_password_label", Res.string.new_password_label)
  map.put("new_registration", Res.string.new_registration)
  map.put("newest_first", Res.string.newest_first)
  map.put("nickname_hint", Res.string.nickname_hint)
  map.put("nickname_label", Res.string.nickname_label)
  map.put("no", Res.string.no)
  map.put("no_entries", Res.string.no_entries)
  map.put("no_games", Res.string.no_games)
  map.put("no_matches", Res.string.no_matches)
  map.put("no_matches_found", Res.string.no_matches_found)
  map.put("no_player_waiting", Res.string.no_player_waiting)
  map.put("no_players", Res.string.no_players)
  map.put("no_players_swap", Res.string.no_players_swap)
  map.put("no_victories", Res.string.no_victories)
  map.put("none_selected", Res.string.none_selected)
  map.put("off_waitlist", Res.string.off_waitlist)
  map.put("ok", Res.string.ok)
  map.put("oldest_first", Res.string.oldest_first)
}

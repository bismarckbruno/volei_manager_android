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

public val Res.string.rebalanced_player_tooltip: StringResource by lazy {
      StringResource("string:rebalanced_player_tooltip", "rebalanced_player_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59773, 121),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57957, 117),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53881, 121),
      ))
    }

public val Res.string.recent_activity_title: StringResource by lazy {
      StringResource("string:recent_activity_title", "recent_activity_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59895, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58075, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54003, 49),
      ))
    }

public val Res.string.remote_group_content_description: StringResource by lazy {
      StringResource("string:remote_group_content_description", "remote_group_content_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59949, 80),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58129, 80),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54053, 88),
      ))
    }

public val Res.string.remove_from_queue: StringResource by lazy {
      StringResource("string:remove_from_queue", "remove_from_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60030, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58210, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54142, 49),
      ))
    }

public val Res.string.remove_next_match_guarantee: StringResource by lazy {
      StringResource("string:remove_next_match_guarantee", "remove_next_match_guarantee", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60084, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58256, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54192, 59),
      ))
    }

public val Res.string.rename_group: StringResource by lazy {
      StringResource("string:rename_group", "rename_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60144, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58316, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54252, 36),
      ))
    }

public val Res.string.replace_player: StringResource by lazy {
      StringResource("string:replace_player", "replace_player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60185, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58357, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54289, 38),
      ))
    }

public val Res.string.resend_verification_email: StringResource by lazy {
      StringResource("string:resend_verification_email", "resend_verification_email", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60228, 77),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58400, 77),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54328, 69),
      ))
    }

public val Res.string.restore_backup: StringResource by lazy {
      StringResource("string:restore_backup", "restore_backup", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60306, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58478, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54398, 50),
      ))
    }

public val Res.string.save: StringResource by lazy {
      StringResource("string:save", "save", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60395, 24),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58567, 20),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54479, 20),
      ))
    }

public val Res.string.save_file: StringResource by lazy {
      StringResource("string:save_file", "save_file", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60361, 33),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58533, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54449, 29),
      ))
    }

public val Res.string.score_latest_point_tooltip: StringResource by lazy {
      StringResource("string:score_latest_point_tooltip", "score_latest_point_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60420, 82),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58588, 74),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54500, 82),
      ))
    }

public val Res.string.score_rotation_tooltip: StringResource by lazy {
      StringResource("string:score_rotation_tooltip", "score_rotation_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60503, 62),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58663, 70),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54583, 58),
      ))
    }

public val Res.string.search_player: StringResource by lazy {
      StringResource("string:search_player", "search_player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60566, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58734, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54642, 41),
      ))
    }

public val Res.string.secondary_position: StringResource by lazy {
      StringResource("string:secondary_position", "secondary_position", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60608, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58768, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54684, 46),
      ))
    }

public val Res.string.select_equal_number: StringResource by lazy {
      StringResource("string:select_equal_number", "select_equal_number", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60659, 127),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58819, 123),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54731, 115),
      ))
    }

public val Res.string.select_minimum_4_players: StringResource by lazy {
      StringResource("string:select_minimum_4_players", "select_minimum_4_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60787, 76),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 58943, 72),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54847, 68),
      ))
    }

public val Res.string.select_minimum_players: StringResource by lazy {
      StringResource("string:select_minimum_players", "select_minimum_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61016, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59164, 74),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55044, 70),
      ))
    }

public val Res.string.select_minimum_players_long: StringResource by lazy {
      StringResource("string:select_minimum_players_long", "select_minimum_players_long", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 60864, 151),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59016, 147),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 54916, 127),
      ))
    }

public val Res.string.select_player: StringResource by lazy {
      StringResource("string:select_player", "select_player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61095, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59239, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55115, 41),
      ))
    }

public val Res.string.select_specific_date: StringResource by lazy {
      StringResource("string:select_specific_date", "select_specific_date", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61145, 112),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59285, 112),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55157, 88),
      ))
    }

public val Res.string.select_word: StringResource by lazy {
      StringResource("string:select_word", "select_word", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61258, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59398, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55246, 27),
      ))
    }

public val Res.string.selected_group: StringResource by lazy {
      StringResource("string:selected_group", "selected_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61294, 38),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59430, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55274, 30),
      ))
    }

public val Res.string.send_feedback: StringResource by lazy {
      StringResource("string:send_feedback", "send_feedback", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61333, 45),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59469, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55305, 41),
      ))
    }

public val Res.string.send_question: StringResource by lazy {
      StringResource("string:send_question", "send_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61677, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59817, 49),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55625, 41),
      ))
    }

public val Res.string.send_question_dialog_confirm: StringResource by lazy {
      StringResource("string:send_question_dialog_confirm", "send_question_dialog_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61379, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59511, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55347, 56),
      ))
    }

public val Res.string.send_question_dialog_text: StringResource by lazy {
      StringResource("string:send_question_dialog_text", "send_question_dialog_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61436, 161),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59568, 173),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55404, 153),
      ))
    }

public val Res.string.send_question_dialog_title: StringResource by lazy {
      StringResource("string:send_question_dialog_title", "send_question_dialog_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61598, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59742, 74),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55558, 66),
      ))
    }

public val Res.string.set_priority: StringResource by lazy {
      StringResource("string:set_priority", "set_priority", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61727, 52),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59867, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55667, 40),
      ))
    }

public val Res.string.settings: StringResource by lazy {
      StringResource("string:settings", "settings", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61780, 36),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59920, 36),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55708, 28),
      ))
    }

public val Res.string.share_history: StringResource by lazy {
      StringResource("string:share_history", "share_history", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61817, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 59957, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55737, 41),
      ))
    }

public val Res.string.show_elo: StringResource by lazy {
      StringResource("string:show_elo", "show_elo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61996, 32),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60128, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55884, 28),
      ))
    }

public val Res.string.show_elo_tooltip: StringResource by lazy {
      StringResource("string:show_elo_tooltip", "show_elo_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 61867, 128),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60011, 116),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55779, 104),
      ))
    }

public val Res.string.show_lateness: StringResource by lazy {
      StringResource("string:show_lateness", "show_lateness", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62267, 45),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60407, 49),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56163, 37),
      ))
    }

public val Res.string.show_lateness_tooltip: StringResource by lazy {
      StringResource("string:show_lateness_tooltip", "show_lateness_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62029, 237),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60161, 245),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 55913, 249),
      ))
    }

public val Res.string.show_password: StringResource by lazy {
      StringResource("string:show_password", "show_password", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62313, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60457, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56201, 41),
      ))
    }

public val Res.string.side_menu: StringResource by lazy {
      StringResource("string:side_menu", "side_menu", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62363, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60499, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56243, 29),
      ))
    }

public val Res.string.signup_confirm: StringResource by lazy {
      StringResource("string:signup_confirm", "signup_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62401, 38),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60533, 34),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56273, 34),
      ))
    }

public val Res.string.signup_switch_to_login: StringResource by lazy {
      StringResource("string:signup_switch_to_login", "signup_switch_to_login", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62440, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60568, 58),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56308, 74),
      ))
    }

public val Res.string.signup_title: StringResource by lazy {
      StringResource("string:signup_title", "signup_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62519, 36),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60627, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56383, 32),
      ))
    }

public val Res.string.sort_alphabetical_order: StringResource by lazy {
      StringResource("string:sort_alphabetical_order", "sort_alphabetical_order", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62556, 67),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60660, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56416, 63),
      ))
    }

public val Res.string.sort_highest_elo: StringResource by lazy {
      StringResource("string:sort_highest_elo", "sort_highest_elo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62700, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60812, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56544, 48),
      ))
    }

public val Res.string.sort_highest_elo_change: StringResource by lazy {
      StringResource("string:sort_highest_elo_change", "sort_highest_elo_change", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62624, 75),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60732, 79),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56480, 63),
      ))
    }

public val Res.string.sort_highest_percentage: StringResource by lazy {
      StringResource("string:sort_highest_percentage", "sort_highest_percentage", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62749, 67),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60869, 71),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56593, 63),
      ))
    }

public val Res.string.sort_largest_score_difference: StringResource by lazy {
      StringResource("string:sort_largest_score_difference", "sort_largest_score_difference", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62817, 89),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 60941, 89),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56657, 77),
      ))
    }

public val Res.string.sort_most_matches: StringResource by lazy {
      StringResource("string:sort_most_matches", "sort_most_matches", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62907, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61031, 57),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56735, 49),
      ))
    }

public val Res.string.sort_most_played_time: StringResource by lazy {
      StringResource("string:sort_most_played_time", "sort_most_played_time", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 62965, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61089, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56785, 61),
      ))
    }

public val Res.string.sort_most_victories: StringResource by lazy {
      StringResource("string:sort_most_victories", "sort_most_victories", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63031, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61163, 63),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56847, 55),
      ))
    }

public val Res.string.sort_newest_first: StringResource by lazy {
      StringResource("string:sort_newest_first", "sort_newest_first", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63091, 69),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61227, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56903, 49),
      ))
    }

public val Res.string.sort_oldest_first: StringResource by lazy {
      StringResource("string:sort_oldest_first", "sort_oldest_first", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63161, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61301, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 56953, 49),
      ))
    }

public val Res.string.sort_word: StringResource by lazy {
      StringResource("string:sort_word", "sort_word", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63227, 29),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61375, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57003, 25),
      ))
    }

public val Res.string.spectator_access_revoked_message: StringResource by lazy {
      StringResource("string:spectator_access_revoked_message", "spectator_access_revoked_message", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63257, 168),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61405, 168),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57029, 160),
      ))
    }

public val Res.string.spectator_code_copied: StringResource by lazy {
      StringResource("string:spectator_code_copied", "spectator_code_copied", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63426, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61574, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57190, 45),
      ))
    }

public val Res.string.spectator_code_copy: StringResource by lazy {
      StringResource("string:spectator_code_copy", "spectator_code_copy", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63480, 47),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61628, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57236, 39),
      ))
    }

public val Res.string.spectator_code_live_viewers: StringResource by lazy {
      StringResource("string:spectator_code_live_viewers", "spectator_code_live_viewers", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63528, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61676, 63),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57276, 67),
      ))
    }

public val Res.string.spectator_code_loading: StringResource by lazy {
      StringResource("string:spectator_code_loading", "spectator_code_loading", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63588, 90),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61740, 86),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57344, 86),
      ))
    }

public val Res.string.spectator_code_qr_button: StringResource by lazy {
      StringResource("string:spectator_code_qr_button", "spectator_code_qr_button", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63679, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61827, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57431, 48),
      ))
    }

public val Res.string.spectator_code_qr_hint: StringResource by lazy {
      StringResource("string:spectator_code_qr_hint", "spectator_code_qr_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63736, 190),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 61880, 182),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57480, 174),
      ))
    }

public val Res.string.spectator_code_qr_title: StringResource by lazy {
      StringResource("string:spectator_code_qr_title", "spectator_code_qr_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63927, 71),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62063, 67),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57655, 55),
      ))
    }

public val Res.string.spectator_code_regenerate_button: StringResource by lazy {
      StringResource("string:spectator_code_regenerate_button", "spectator_code_regenerate_button", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 63999, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62131, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57711, 56),
      ))
    }

public val Res.string.spectator_code_regenerate_confirm_action: StringResource by lazy {
      StringResource("string:spectator_code_regenerate_confirm_action", "spectator_code_regenerate_confirm_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64064, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62196, 60),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57768, 56),
      ))
    }

public val Res.string.spectator_code_regenerate_confirm_message: StringResource by lazy {
      StringResource("string:spectator_code_regenerate_confirm_message", "spectator_code_regenerate_confirm_message", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64129, 337),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62257, 341),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 57825, 321),
      ))
    }

public val Res.string.spectator_code_regenerate_confirm_title: StringResource by lazy {
      StringResource("string:spectator_code_regenerate_confirm_title", "spectator_code_regenerate_confirm_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64467, 95),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62599, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58147, 71),
      ))
    }

public val Res.string.spectator_code_regenerate_error: StringResource by lazy {
      StringResource("string:spectator_code_regenerate_error", "spectator_code_regenerate_error", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64563, 119),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62687, 123),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58219, 103),
      ))
    }

public val Res.string.spectator_code_regenerated_message: StringResource by lazy {
      StringResource("string:spectator_code_regenerated_message", "spectator_code_regenerated_message", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64683, 150),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62811, 138),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58323, 126),
      ))
    }

public val Res.string.spectator_code_retry_button: StringResource by lazy {
      StringResource("string:spectator_code_retry_button", "spectator_code_retry_button", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64834, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 62950, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58450, 47),
      ))
    }

public val Res.string.spectator_code_section_hint: StringResource by lazy {
      StringResource("string:spectator_code_section_hint", "spectator_code_section_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 64894, 287),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63010, 279),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58498, 255),
      ))
    }

public val Res.string.spectator_code_section_title: StringResource by lazy {
      StringResource("string:spectator_code_section_title", "spectator_code_section_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65182, 84),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63290, 80),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58754, 64),
      ))
    }

public val Res.string.spectator_code_share_button: StringResource by lazy {
      StringResource("string:spectator_code_share_button", "spectator_code_share_button", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65267, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63371, 63),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58819, 51),
      ))
    }

public val Res.string.spectator_code_share_text: StringResource by lazy {
      StringResource("string:spectator_code_share_text", "spectator_code_share_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65327, 197),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63435, 181),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 58871, 173),
      ))
    }

public val Res.string.spectator_code_syncing_history: StringResource by lazy {
      StringResource("string:spectator_code_syncing_history", "spectator_code_syncing_history", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65525, 190),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63617, 206),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59045, 178),
      ))
    }

public val Res.string.spectator_waiting_game_start: StringResource by lazy {
      StringResource("string:spectator_waiting_game_start", "spectator_waiting_game_start", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65716, 80),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63824, 72),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59224, 76),
      ))
    }

public val Res.string.start: StringResource by lazy {
      StringResource("string:start", "start", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65941, 25),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64029, 25),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59429, 21),
      ))
    }

public val Res.string.start_match: StringResource by lazy {
      StringResource("string:start_match", "start_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65797, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63897, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59301, 35),
      ))
    }

public val Res.string.start_next_match: StringResource by lazy {
      StringResource("string:start_next_match", "start_next_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65841, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63933, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59337, 48),
      ))
    }

public val Res.string.start_of_queue: StringResource by lazy {
      StringResource("string:start_of_queue", "start_of_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65890, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 63986, 42),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59386, 42),
      ))
    }

public val Res.string.streak_adjusted_same_team_snackbar: StringResource by lazy {
      StringResource("string:streak_adjusted_same_team_snackbar", "streak_adjusted_same_team_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 65967, 90),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64055, 98),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59451, 90),
      ))
    }

public val Res.string.streak_cleared_snackbar: StringResource by lazy {
      StringResource("string:streak_cleared_snackbar", "streak_cleared_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66058, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64154, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59542, 83),
      ))
    }

public val Res.string.streak_history_cleared: StringResource by lazy {
      StringResource("string:streak_history_cleared", "streak_history_cleared", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66146, 58),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64242, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59626, 54),
      ))
    }

public val Res.string.streak_history_same_team: StringResource by lazy {
      StringResource("string:streak_history_same_team", "streak_history_same_team", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66205, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64297, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59681, 56),
      ))
    }

public val Res.string.streak_history_started: StringResource by lazy {
      StringResource("string:streak_history_started", "streak_history_started", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66262, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64354, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59738, 54),
      ))
    }

public val Res.string.streak_history_title: StringResource by lazy {
      StringResource("string:streak_history_title", "streak_history_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66317, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64409, 76),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59793, 56),
      ))
    }

public val Res.string.streak_history_transferred: StringResource by lazy {
      StringResource("string:streak_history_transferred", "streak_history_transferred", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66382, 70),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64486, 74),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59850, 74),
      ))
    }

public val Res.string.streak_started_snackbar: StringResource by lazy {
      StringResource("string:streak_started_snackbar", "streak_started_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66453, 83),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64561, 91),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 59925, 83),
      ))
    }

public val Res.string.streak_transferred_snackbar: StringResource by lazy {
      StringResource("string:streak_transferred_snackbar", "streak_transferred_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66537, 99),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64653, 107),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60009, 103),
      ))
    }

public val Res.string.streak_undo_unavailable: StringResource by lazy {
      StringResource("string:streak_undo_unavailable", "streak_undo_unavailable", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66637, 91),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64761, 115),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60113, 99),
      ))
    }

public val Res.string.substitution_history_from_team_bench: StringResource by lazy {
      StringResource("string:substitution_history_from_team_bench", "substitution_history_from_team_bench", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66729, 172),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 64877, 176),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60213, 152),
      ))
    }

public val Res.string.substitution_history_from_waiting: StringResource by lazy {
      StringResource("string:substitution_history_from_waiting", "substitution_history_from_waiting", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 66902, 129),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65054, 129),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60366, 129),
      ))
    }

public val Res.string.substitution_history_team_swap: StringResource by lazy {
      StringResource("string:substitution_history_team_swap", "substitution_history_team_swap", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67032, 102),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65184, 114),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60496, 102),
      ))
    }

public val Res.string.substitution_history_title: StringResource by lazy {
      StringResource("string:substitution_history_title", "substitution_history_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67135, 66),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65299, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60599, 62),
      ))
    }

public val Res.string.substitution_preview: StringResource by lazy {
      StringResource("string:substitution_preview", "substitution_preview", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67202, 72),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65366, 76),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60662, 68),
      ))
    }

public val Res.string.substitution_select_hint: StringResource by lazy {
      StringResource("string:substitution_select_hint", "substitution_select_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67275, 104),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65443, 108),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60731, 92),
      ))
    }

public val Res.string.substitution_snackbar: StringResource by lazy {
      StringResource("string:substitution_snackbar", "substitution_snackbar", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67380, 73),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65552, 73),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60824, 65),
      ))
    }

public val Res.string.swap_team_sides: StringResource by lazy {
      StringResource("string:swap_team_sides", "swap_team_sides", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67454, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65626, 55),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60890, 43),
      ))
    }

public val Res.string.switch_teams: StringResource by lazy {
      StringResource("string:switch_teams", "switch_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67518, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65682, 36),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60934, 36),
      ))
    }

public val Res.string.team_a: StringResource by lazy {
      StringResource("string:team_a", "team_a", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67602, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65758, 22),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61010, 22),
      ))
    }

public val Res.string.team_a_parentheses: StringResource by lazy {
      StringResource("string:team_a_parentheses", "team_a_parentheses", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67559, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65719, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 60971, 38),
      ))
    }

public val Res.string.team_b: StringResource by lazy {
      StringResource("string:team_b", "team_b", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67861, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 66009, 22),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61241, 22),
      ))
    }

public val Res.string.team_b_parentheses: StringResource by lazy {
      StringResource("string:team_b_parentheses", "team_b_parentheses", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67629, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65781, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61033, 38),
      ))
    }

public val Res.string.team_bench_libero_tooltip: StringResource by lazy {
      StringResource("string:team_bench_libero_tooltip", "team_bench_libero_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 67672, 133),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 65820, 137),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 61072, 121),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString5Resources(map: MutableMap<String, StringResource>) {
  map.put("rebalanced_player_tooltip", Res.string.rebalanced_player_tooltip)
  map.put("recent_activity_title", Res.string.recent_activity_title)
  map.put("remote_group_content_description", Res.string.remote_group_content_description)
  map.put("remove_from_queue", Res.string.remove_from_queue)
  map.put("remove_next_match_guarantee", Res.string.remove_next_match_guarantee)
  map.put("rename_group", Res.string.rename_group)
  map.put("replace_player", Res.string.replace_player)
  map.put("resend_verification_email", Res.string.resend_verification_email)
  map.put("restore_backup", Res.string.restore_backup)
  map.put("save", Res.string.save)
  map.put("save_file", Res.string.save_file)
  map.put("score_latest_point_tooltip", Res.string.score_latest_point_tooltip)
  map.put("score_rotation_tooltip", Res.string.score_rotation_tooltip)
  map.put("search_player", Res.string.search_player)
  map.put("secondary_position", Res.string.secondary_position)
  map.put("select_equal_number", Res.string.select_equal_number)
  map.put("select_minimum_4_players", Res.string.select_minimum_4_players)
  map.put("select_minimum_players", Res.string.select_minimum_players)
  map.put("select_minimum_players_long", Res.string.select_minimum_players_long)
  map.put("select_player", Res.string.select_player)
  map.put("select_specific_date", Res.string.select_specific_date)
  map.put("select_word", Res.string.select_word)
  map.put("selected_group", Res.string.selected_group)
  map.put("send_feedback", Res.string.send_feedback)
  map.put("send_question", Res.string.send_question)
  map.put("send_question_dialog_confirm", Res.string.send_question_dialog_confirm)
  map.put("send_question_dialog_text", Res.string.send_question_dialog_text)
  map.put("send_question_dialog_title", Res.string.send_question_dialog_title)
  map.put("set_priority", Res.string.set_priority)
  map.put("settings", Res.string.settings)
  map.put("share_history", Res.string.share_history)
  map.put("show_elo", Res.string.show_elo)
  map.put("show_elo_tooltip", Res.string.show_elo_tooltip)
  map.put("show_lateness", Res.string.show_lateness)
  map.put("show_lateness_tooltip", Res.string.show_lateness_tooltip)
  map.put("show_password", Res.string.show_password)
  map.put("side_menu", Res.string.side_menu)
  map.put("signup_confirm", Res.string.signup_confirm)
  map.put("signup_switch_to_login", Res.string.signup_switch_to_login)
  map.put("signup_title", Res.string.signup_title)
  map.put("sort_alphabetical_order", Res.string.sort_alphabetical_order)
  map.put("sort_highest_elo", Res.string.sort_highest_elo)
  map.put("sort_highest_elo_change", Res.string.sort_highest_elo_change)
  map.put("sort_highest_percentage", Res.string.sort_highest_percentage)
  map.put("sort_largest_score_difference", Res.string.sort_largest_score_difference)
  map.put("sort_most_matches", Res.string.sort_most_matches)
  map.put("sort_most_played_time", Res.string.sort_most_played_time)
  map.put("sort_most_victories", Res.string.sort_most_victories)
  map.put("sort_newest_first", Res.string.sort_newest_first)
  map.put("sort_oldest_first", Res.string.sort_oldest_first)
  map.put("sort_word", Res.string.sort_word)
  map.put("spectator_access_revoked_message", Res.string.spectator_access_revoked_message)
  map.put("spectator_code_copied", Res.string.spectator_code_copied)
  map.put("spectator_code_copy", Res.string.spectator_code_copy)
  map.put("spectator_code_live_viewers", Res.string.spectator_code_live_viewers)
  map.put("spectator_code_loading", Res.string.spectator_code_loading)
  map.put("spectator_code_qr_button", Res.string.spectator_code_qr_button)
  map.put("spectator_code_qr_hint", Res.string.spectator_code_qr_hint)
  map.put("spectator_code_qr_title", Res.string.spectator_code_qr_title)
  map.put("spectator_code_regenerate_button", Res.string.spectator_code_regenerate_button)
  map.put("spectator_code_regenerate_confirm_action", Res.string.spectator_code_regenerate_confirm_action)
  map.put("spectator_code_regenerate_confirm_message", Res.string.spectator_code_regenerate_confirm_message)
  map.put("spectator_code_regenerate_confirm_title", Res.string.spectator_code_regenerate_confirm_title)
  map.put("spectator_code_regenerate_error", Res.string.spectator_code_regenerate_error)
  map.put("spectator_code_regenerated_message", Res.string.spectator_code_regenerated_message)
  map.put("spectator_code_retry_button", Res.string.spectator_code_retry_button)
  map.put("spectator_code_section_hint", Res.string.spectator_code_section_hint)
  map.put("spectator_code_section_title", Res.string.spectator_code_section_title)
  map.put("spectator_code_share_button", Res.string.spectator_code_share_button)
  map.put("spectator_code_share_text", Res.string.spectator_code_share_text)
  map.put("spectator_code_syncing_history", Res.string.spectator_code_syncing_history)
  map.put("spectator_waiting_game_start", Res.string.spectator_waiting_game_start)
  map.put("start", Res.string.start)
  map.put("start_match", Res.string.start_match)
  map.put("start_next_match", Res.string.start_next_match)
  map.put("start_of_queue", Res.string.start_of_queue)
  map.put("streak_adjusted_same_team_snackbar", Res.string.streak_adjusted_same_team_snackbar)
  map.put("streak_cleared_snackbar", Res.string.streak_cleared_snackbar)
  map.put("streak_history_cleared", Res.string.streak_history_cleared)
  map.put("streak_history_same_team", Res.string.streak_history_same_team)
  map.put("streak_history_started", Res.string.streak_history_started)
  map.put("streak_history_title", Res.string.streak_history_title)
  map.put("streak_history_transferred", Res.string.streak_history_transferred)
  map.put("streak_started_snackbar", Res.string.streak_started_snackbar)
  map.put("streak_transferred_snackbar", Res.string.streak_transferred_snackbar)
  map.put("streak_undo_unavailable", Res.string.streak_undo_unavailable)
  map.put("substitution_history_from_team_bench", Res.string.substitution_history_from_team_bench)
  map.put("substitution_history_from_waiting", Res.string.substitution_history_from_waiting)
  map.put("substitution_history_team_swap", Res.string.substitution_history_team_swap)
  map.put("substitution_history_title", Res.string.substitution_history_title)
  map.put("substitution_preview", Res.string.substitution_preview)
  map.put("substitution_select_hint", Res.string.substitution_select_hint)
  map.put("substitution_snackbar", Res.string.substitution_snackbar)
  map.put("swap_team_sides", Res.string.swap_team_sides)
  map.put("switch_teams", Res.string.switch_teams)
  map.put("team_a", Res.string.team_a)
  map.put("team_a_parentheses", Res.string.team_a_parentheses)
  map.put("team_b", Res.string.team_b)
  map.put("team_b_parentheses", Res.string.team_b_parentheses)
  map.put("team_bench_libero_tooltip", Res.string.team_bench_libero_tooltip)
}

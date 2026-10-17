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

public val Res.string.about_app: StringResource by lazy {
      StringResource("string:about_app", "about_app", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 1002, 45),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 938, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 866, 37),
      ))
    }

public val Res.string.about_app_desc: StringResource by lazy {
      StringResource("string:about_app_desc", "about_app_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 307, 642),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 315, 574),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 279, 542),
      ))
    }

public val Res.string.about_app_title: StringResource by lazy {
      StringResource("string:about_app_title", "about_app_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 950, 51),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 890, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 822, 43),
      ))
    }

public val Res.string.about_code: StringResource by lazy {
      StringResource("string:about_code", "about_code", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 1536, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 1404, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1316, 38),
      ))
    }

public val Res.string.about_code_desc: StringResource by lazy {
      StringResource("string:about_code_desc", "about_code_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 1048, 487),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 972, 431),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 904, 411),
      ))
    }

public val Res.string.about_dev: StringResource by lazy {
      StringResource("string:about_dev", "about_dev", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2030, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 1870, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1766, 45),
      ))
    }

public val Res.string.about_dev_desc: StringResource by lazy {
      StringResource("string:about_dev_desc", "about_dev_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 1579, 450),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 1443, 426),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1355, 410),
      ))
    }

public val Res.string.absent: StringResource by lazy {
      StringResource("string:absent", "absent", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2080, 34),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 1916, 34),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1812, 34),
      ))
    }

public val Res.string.account_avatar_content_description: StringResource by lazy {
      StringResource("string:account_avatar_content_description", "account_avatar_content_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2115, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 1951, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1847, 54),
      ))
    }

public val Res.string.add: StringResource by lazy {
      StringResource("string:add", "add", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2326, 23),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2166, 23),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2038, 15),
      ))
    }

public val Res.string.add_new_player: StringResource by lazy {
      StringResource("string:add_new_player", "add_new_player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2166, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2002, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1902, 42),
      ))
    }

public val Res.string.add_profile_photo_title: StringResource by lazy {
      StringResource("string:add_profile_photo_title", "add_profile_photo_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2217, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2057, 63),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 1945, 55),
      ))
    }

public val Res.string.add_to_queue: StringResource by lazy {
      StringResource("string:add_to_queue", "add_to_queue", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2281, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2121, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2001, 36),
      ))
    }

public val Res.string.admin_session_transfer_button: StringResource by lazy {
      StringResource("string:admin_session_transfer_button", "admin_session_transfer_button", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2350, 109),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2190, 101),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2054, 89),
      ))
    }

public val Res.string.admin_session_transfer_confirm_action: StringResource by lazy {
      StringResource("string:admin_session_transfer_confirm_action", "admin_session_transfer_confirm_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2460, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2292, 57),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2144, 57),
      ))
    }

public val Res.string.admin_session_transfer_confirm_message: StringResource by lazy {
      StringResource("string:admin_session_transfer_confirm_message", "admin_session_transfer_confirm_message", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2514, 282),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2350, 274),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2202, 238),
      ))
    }

public val Res.string.admin_session_transfer_confirm_title: StringResource by lazy {
      StringResource("string:admin_session_transfer_confirm_title", "admin_session_transfer_confirm_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2797, 96),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2625, 88),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2441, 76),
      ))
    }

public val Res.string.admin_session_transfer_warning: StringResource by lazy {
      StringResource("string:admin_session_transfer_warning", "admin_session_transfer_warning", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 2894, 274),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2714, 254),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2518, 202),
      ))
    }

public val Res.string.all_dates: StringResource by lazy {
      StringResource("string:all_dates", "all_dates", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3235, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3031, 37),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2779, 29),
      ))
    }

public val Res.string.all_dates_with_player: StringResource by lazy {
      StringResource("string:all_dates_with_player", "all_dates_with_player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3169, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 2969, 61),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2721, 57),
      ))
    }

public val Res.string.all_players: StringResource by lazy {
      StringResource("string:all_players", "all_players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3369, 47),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3161, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2885, 35),
      ))
    }

public val Res.string.all_players_present: StringResource by lazy {
      StringResource("string:all_players_present", "all_players_present", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3277, 91),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3069, 91),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2809, 75),
      ))
    }

public val Res.string.alphabetical: StringResource by lazy {
      StringResource("string:alphabetical", "alphabetical", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3417, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3205, 48),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2921, 36),
      ))
    }

public val Res.string.app_name: StringResource by lazy {
      StringResource("string:app_name", "app_name", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3466, 28),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3254, 28),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2958, 36),
      ))
    }

public val Res.string.apply_filter: StringResource by lazy {
      StringResource("string:apply_filter", "apply_filter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3495, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3283, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 2995, 36),
      ))
    }

public val Res.string.assemble_teams: StringResource by lazy {
      StringResource("string:assemble_teams", "assemble_teams", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3536, 42),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3324, 38),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3032, 42),
      ))
    }

public val Res.string.auth_invalid_email: StringResource by lazy {
      StringResource("string:auth_invalid_email", "auth_invalid_email", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3579, 62),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3363, 62),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3075, 66),
      ))
    }

public val Res.string.auth_invalid_password: StringResource by lazy {
      StringResource("string:auth_invalid_password", "auth_invalid_password", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3642, 173),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3426, 161),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3142, 161),
      ))
    }

public val Res.string.auth_or_divider: StringResource by lazy {
      StringResource("string:auth_or_divider", "auth_or_divider", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3816, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3588, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3304, 27),
      ))
    }

public val Res.string.auto_selected_loser_player_icon_cd: StringResource by lazy {
      StringResource("string:auto_selected_loser_player_icon_cd", "auto_selected_loser_player_icon_cd", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 3844, 174),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3616, 170),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3332, 146),
      ))
    }

public val Res.string.auto_selected_loser_player_tooltip: StringResource by lazy {
      StringResource("string:auto_selected_loser_player_tooltip", "auto_selected_loser_player_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4019, 174),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3787, 174),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3479, 154),
      ))
    }

public val Res.string.avatar_crop_confirm: StringResource by lazy {
      StringResource("string:avatar_crop_confirm", "avatar_crop_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4194, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 3962, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3634, 39),
      ))
    }

public val Res.string.avatar_crop_hint: StringResource by lazy {
      StringResource("string:avatar_crop_hint", "avatar_crop_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4234, 144),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4002, 136),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3674, 120),
      ))
    }

public val Res.string.avatar_crop_rotate: StringResource by lazy {
      StringResource("string:avatar_crop_rotate", "avatar_crop_rotate", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4379, 34),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4139, 34),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3795, 34),
      ))
    }

public val Res.string.avatar_crop_title: StringResource by lazy {
      StringResource("string:avatar_crop_title", "avatar_crop_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4414, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4174, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3830, 41),
      ))
    }

public val Res.string.average_duration: StringResource by lazy {
      StringResource("string:average_duration", "average_duration", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4456, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4216, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3872, 56),
      ))
    }

public val Res.string.average_elo: StringResource by lazy {
      StringResource("string:average_elo", "average_elo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4732, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4480, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4112, 43),
      ))
    }

public val Res.string.average_elo_indicator_cd: StringResource by lazy {
      StringResource("string:average_elo_indicator_cd", "average_elo_indicator_cd", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4513, 80),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4273, 76),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3929, 68),
      ))
    }

public val Res.string.average_elo_indicator_tooltip: StringResource by lazy {
      StringResource("string:average_elo_indicator_tooltip", "average_elo_indicator_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4594, 137),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4350, 129),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 3998, 113),
      ))
    }

public val Res.string.back: StringResource by lazy {
      StringResource("string:back", "back", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4776, 20),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4524, 20),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4156, 20),
      ))
    }

public val Res.string.balance_mode_long_press_hint: StringResource by lazy {
      StringResource("string:balance_mode_long_press_hint", "balance_mode_long_press_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4797, 104),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4545, 108),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4177, 88),
      ))
    }

public val Res.string.balance_mode_title: StringResource by lazy {
      StringResource("string:balance_mode_title", "balance_mode_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4902, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4654, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4266, 42),
      ))
    }

public val Res.string.big_scoreboard_back_cd: StringResource by lazy {
      StringResource("string:big_scoreboard_back_cd", "big_scoreboard_back_cd", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 4949, 70),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4709, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4309, 62),
      ))
    }

public val Res.string.big_scoreboard_back_tooltip: StringResource by lazy {
      StringResource("string:big_scoreboard_back_tooltip", "big_scoreboard_back_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5020, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4776, 79),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4372, 83),
      ))
    }

public val Res.string.big_scoreboard_cd: StringResource by lazy {
      StringResource("string:big_scoreboard_cd", "big_scoreboard_cd", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5108, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4856, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4456, 53),
      ))
    }

public val Res.string.big_scoreboard_tooltip: StringResource by lazy {
      StringResource("string:big_scoreboard_tooltip", "big_scoreboard_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5162, 126),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 4910, 114),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4510, 114),
      ))
    }

public val Res.string.birth_date_invalid: StringResource by lazy {
      StringResource("string:birth_date_invalid", "birth_date_invalid", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5289, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5025, 78),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4625, 70),
      ))
    }

public val Res.string.birth_date_label: StringResource by lazy {
      StringResource("string:birth_date_label", "birth_date_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5368, 52),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5104, 48),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4696, 44),
      ))
    }

public val Res.string.birth_date_placeholder: StringResource by lazy {
      StringResource("string:birth_date_placeholder", "birth_date_placeholder", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5421, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5153, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4741, 46),
      ))
    }

public val Res.string.by_elo: StringResource by lazy {
      StringResource("string:by_elo", "by_elo", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5518, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5250, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4830, 22),
      ))
    }

public val Res.string.by_elo_change: StringResource by lazy {
      StringResource("string:by_elo_change", "by_elo_change", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5468, 49),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5200, 49),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4788, 41),
      ))
    }

public val Res.string.by_matches: StringResource by lazy {
      StringResource("string:by_matches", "by_matches", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5545, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5277, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4853, 42),
      ))
    }

public val Res.string.by_percentage: StringResource by lazy {
      StringResource("string:by_percentage", "by_percentage", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5596, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5324, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4896, 41),
      ))
    }

public val Res.string.by_played_time: StringResource by lazy {
      StringResource("string:by_played_time", "by_played_time", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5638, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5366, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4938, 42),
      ))
    }

public val Res.string.by_score_diff: StringResource by lazy {
      StringResource("string:by_score_diff", "by_score_diff", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5685, 61),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5413, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 4981, 49),
      ))
    }

public val Res.string.by_victories: StringResource by lazy {
      StringResource("string:by_victories", "by_victories", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5747, 52),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5467, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5031, 36),
      ))
    }

public val Res.string.cancel: StringResource by lazy {
      StringResource("string:cancel", "cancel", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5901, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5617, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5153, 22),
      ))
    }

public val Res.string.cancel_match: StringResource by lazy {
      StringResource("string:cancel_match", "cancel_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5852, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5572, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5112, 40),
      ))
    }

public val Res.string.cancel_match_action: StringResource by lazy {
      StringResource("string:cancel_match_action", "cancel_match_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5800, 51),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5520, 51),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5068, 43),
      ))
    }

public val Res.string.change_anyway: StringResource by lazy {
      StringResource("string:change_anyway", "change_anyway", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5928, 53),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5644, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5176, 41),
      ))
    }

public val Res.string.change_email_confirm: StringResource by lazy {
      StringResource("string:change_email_confirm", "change_email_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6141, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5853, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5373, 52),
      ))
    }

public val Res.string.change_email_confirmation_sent: StringResource by lazy {
      StringResource("string:change_email_confirmation_sent", "change_email_confirmation_sent", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 5982, 158),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5690, 162),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5218, 154),
      ))
    }

public val Res.string.change_email_menu_item: StringResource by lazy {
      StringResource("string:change_email_menu_item", "change_email_menu_item", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6198, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5910, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5426, 50),
      ))
    }

public val Res.string.change_email_title: StringResource by lazy {
      StringResource("string:change_email_title", "change_email_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6249, 46),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 5961, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5477, 46),
      ))
    }

public val Res.string.change_group_text: StringResource by lazy {
      StringResource("string:change_group_text", "change_group_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6296, 153),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6008, 157),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5524, 145),
      ))
    }

public val Res.string.change_group_title: StringResource by lazy {
      StringResource("string:change_group_title", "change_group_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6450, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6166, 46),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5670, 46),
      ))
    }

public val Res.string.change_password_confirm: StringResource by lazy {
      StringResource("string:change_password_confirm", "change_password_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6505, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6213, 51),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5717, 51),
      ))
    }

public val Res.string.change_password_menu_item: StringResource by lazy {
      StringResource("string:change_password_menu_item", "change_password_menu_item", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6565, 61),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6265, 53),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5769, 53),
      ))
    }

public val Res.string.change_password_title: StringResource by lazy {
      StringResource("string:change_password_title", "change_password_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6627, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6319, 49),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5823, 49),
      ))
    }

public val Res.string.check_all: StringResource by lazy {
      StringResource("string:check_all", "check_all", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6685, 25),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6369, 25),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5873, 25),
      ))
    }

public val Res.string.clear_filter: StringResource by lazy {
      StringResource("string:clear_filter", "clear_filter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6711, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6395, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5899, 36),
      ))
    }

public val Res.string.clear_match: StringResource by lazy {
      StringResource("string:clear_match", "clear_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6995, 51),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6671, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6159, 47),
      ))
    }

public val Res.string.clear_match_desc: StringResource by lazy {
      StringResource("string:clear_match_desc", "clear_match_desc", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6752, 180),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6436, 184),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 5936, 168),
      ))
    }

public val Res.string.clear_match_title: StringResource by lazy {
      StringResource("string:clear_match_title", "clear_match_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 6933, 61),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6621, 49),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6105, 53),
      ))
    }

public val Res.string.clear_search: StringResource by lazy {
      StringResource("string:clear_search", "clear_search", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7047, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6715, 36),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6207, 36),
      ))
    }

public val Res.string.click_to_start_match: StringResource by lazy {
      StringResource("string:click_to_start_match", "click_to_start_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7092, 80),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6752, 76),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6244, 76),
      ))
    }

public val Res.string.close: StringResource by lazy {
      StringResource("string:close", "close", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7173, 21),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6829, 21),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6321, 21),
      ))
    }

public val Res.string.cloud_sync: StringResource by lazy {
      StringResource("string:cloud_sync", "cloud_sync", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 13882, 30),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 13234, 30),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 12310, 30),
      ))
    }

public val Res.string.cloud_sync_activation_error: StringResource by lazy {
      StringResource("string:cloud_sync_activation_error", "cloud_sync_activation_error", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7195, 227),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 6851, 219),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6343, 191),
      ))
    }

public val Res.string.cloud_sync_auxiliar_groups_title: StringResource by lazy {
      StringResource("string:cloud_sync_auxiliar_groups_title", "cloud_sync_auxiliar_groups_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7423, 100),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7071, 96),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6535, 80),
      ))
    }

public val Res.string.cloud_sync_cancel_subscription: StringResource by lazy {
      StringResource("string:cloud_sync_cancel_subscription", "cloud_sync_cancel_subscription", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7524, 66),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7168, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6616, 66),
      ))
    }

public val Res.string.cloud_sync_debug_cancel_simulation: StringResource by lazy {
      StringResource("string:cloud_sync_debug_cancel_simulation", "cloud_sync_debug_cancel_simulation", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7591, 70),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7235, 70),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6683, 66),
      ))
    }

public val Res.string.cloud_sync_debug_hint: StringResource by lazy {
      StringResource("string:cloud_sync_debug_hint", "cloud_sync_debug_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7662, 321),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7306, 297),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 6750, 265),
      ))
    }

public val Res.string.cloud_sync_debug_simulate_multi: StringResource by lazy {
      StringResource("string:cloud_sync_debug_simulate_multi", "cloud_sync_debug_simulate_multi", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 7984, 71),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7604, 67),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7016, 71),
      ))
    }

public val Res.string.cloud_sync_debug_simulate_single: StringResource by lazy {
      StringResource("string:cloud_sync_debug_simulate_single", "cloud_sync_debug_simulate_single", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8056, 60),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7672, 60),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7088, 64),
      ))
    }

public val Res.string.cloud_sync_deletion_error: StringResource by lazy {
      StringResource("string:cloud_sync_deletion_error", "cloud_sync_deletion_error", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8117, 269),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 7733, 281),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7153, 233),
      ))
    }

public val Res.string.cloud_sync_groups_activate_label: StringResource by lazy {
      StringResource("string:cloud_sync_groups_activate_label", "cloud_sync_groups_activate_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8387, 72),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8015, 72),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7387, 56),
      ))
    }

public val Res.string.cloud_sync_groups_activate_tooltip: StringResource by lazy {
      StringResource("string:cloud_sync_groups_activate_tooltip", "cloud_sync_groups_activate_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8460, 242),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8088, 226),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7444, 190),
      ))
    }

public val Res.string.cloud_sync_groups_empty: StringResource by lazy {
      StringResource("string:cloud_sync_groups_empty", "cloud_sync_groups_empty", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8703, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8315, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7635, 71),
      ))
    }

public val Res.string.cloud_sync_groups_hint: StringResource by lazy {
      StringResource("string:cloud_sync_groups_hint", "cloud_sync_groups_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 8791, 230),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8403, 222),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7707, 198),
      ))
    }

public val Res.string.cloud_sync_groups_locked_hint: StringResource by lazy {
      StringResource("string:cloud_sync_groups_locked_hint", "cloud_sync_groups_locked_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9022, 129),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8626, 121),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 7906, 121),
      ))
    }

public val Res.string.cloud_sync_groups_selector_label: StringResource by lazy {
      StringResource("string:cloud_sync_groups_selector_label", "cloud_sync_groups_selector_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9152, 48),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8748, 48),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8028, 48),
      ))
    }

public val Res.string.cloud_sync_groups_synced_badge: StringResource by lazy {
      StringResource("string:cloud_sync_groups_synced_badge", "cloud_sync_groups_synced_badge", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9201, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8797, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8077, 46),
      ))
    }

public val Res.string.cloud_sync_groups_title: StringResource by lazy {
      StringResource("string:cloud_sync_groups_title", "cloud_sync_groups_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9256, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8852, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8124, 51),
      ))
    }

public val Res.string.cloud_sync_limit_reached: StringResource by lazy {
      StringResource("string:cloud_sync_limit_reached", "cloud_sync_limit_reached", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9316, 116),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 8912, 104),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8176, 96),
      ))
    }

public val Res.string.cloud_sync_period_annual: StringResource by lazy {
      StringResource("string:cloud_sync_period_annual", "cloud_sync_period_annual", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9433, 40),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 9017, 40),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8273, 40),
      ))
    }

public val Res.string.cloud_sync_period_monthly: StringResource by lazy {
      StringResource("string:cloud_sync_period_monthly", "cloud_sync_period_monthly", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9474, 45),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 9058, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8314, 45),
      ))
    }

public val Res.string.cloud_sync_plan_active_badge: StringResource by lazy {
      StringResource("string:cloud_sync_plan_active_badge", "cloud_sync_plan_active_badge", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9520, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 9100, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8360, 44),
      ))
    }

public val Res.string.cloud_sync_plan_annual_price: StringResource by lazy {
      StringResource("string:cloud_sync_plan_annual_price", "cloud_sync_plan_annual_price", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9565, 72),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 9145, 84),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8405, 76),
      ))
    }

public val Res.string.cloud_sync_plan_benefit_code: StringResource by lazy {
      StringResource("string:cloud_sync_plan_benefit_code", "cloud_sync_plan_benefit_code", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 9638, 184),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 9230, 168),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 8482, 152),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString0Resources(map: MutableMap<String, StringResource>) {
  map.put("about_app", Res.string.about_app)
  map.put("about_app_desc", Res.string.about_app_desc)
  map.put("about_app_title", Res.string.about_app_title)
  map.put("about_code", Res.string.about_code)
  map.put("about_code_desc", Res.string.about_code_desc)
  map.put("about_dev", Res.string.about_dev)
  map.put("about_dev_desc", Res.string.about_dev_desc)
  map.put("absent", Res.string.absent)
  map.put("account_avatar_content_description", Res.string.account_avatar_content_description)
  map.put("add", Res.string.add)
  map.put("add_new_player", Res.string.add_new_player)
  map.put("add_profile_photo_title", Res.string.add_profile_photo_title)
  map.put("add_to_queue", Res.string.add_to_queue)
  map.put("admin_session_transfer_button", Res.string.admin_session_transfer_button)
  map.put("admin_session_transfer_confirm_action", Res.string.admin_session_transfer_confirm_action)
  map.put("admin_session_transfer_confirm_message", Res.string.admin_session_transfer_confirm_message)
  map.put("admin_session_transfer_confirm_title", Res.string.admin_session_transfer_confirm_title)
  map.put("admin_session_transfer_warning", Res.string.admin_session_transfer_warning)
  map.put("all_dates", Res.string.all_dates)
  map.put("all_dates_with_player", Res.string.all_dates_with_player)
  map.put("all_players", Res.string.all_players)
  map.put("all_players_present", Res.string.all_players_present)
  map.put("alphabetical", Res.string.alphabetical)
  map.put("app_name", Res.string.app_name)
  map.put("apply_filter", Res.string.apply_filter)
  map.put("assemble_teams", Res.string.assemble_teams)
  map.put("auth_invalid_email", Res.string.auth_invalid_email)
  map.put("auth_invalid_password", Res.string.auth_invalid_password)
  map.put("auth_or_divider", Res.string.auth_or_divider)
  map.put("auto_selected_loser_player_icon_cd", Res.string.auto_selected_loser_player_icon_cd)
  map.put("auto_selected_loser_player_tooltip", Res.string.auto_selected_loser_player_tooltip)
  map.put("avatar_crop_confirm", Res.string.avatar_crop_confirm)
  map.put("avatar_crop_hint", Res.string.avatar_crop_hint)
  map.put("avatar_crop_rotate", Res.string.avatar_crop_rotate)
  map.put("avatar_crop_title", Res.string.avatar_crop_title)
  map.put("average_duration", Res.string.average_duration)
  map.put("average_elo", Res.string.average_elo)
  map.put("average_elo_indicator_cd", Res.string.average_elo_indicator_cd)
  map.put("average_elo_indicator_tooltip", Res.string.average_elo_indicator_tooltip)
  map.put("back", Res.string.back)
  map.put("balance_mode_long_press_hint", Res.string.balance_mode_long_press_hint)
  map.put("balance_mode_title", Res.string.balance_mode_title)
  map.put("big_scoreboard_back_cd", Res.string.big_scoreboard_back_cd)
  map.put("big_scoreboard_back_tooltip", Res.string.big_scoreboard_back_tooltip)
  map.put("big_scoreboard_cd", Res.string.big_scoreboard_cd)
  map.put("big_scoreboard_tooltip", Res.string.big_scoreboard_tooltip)
  map.put("birth_date_invalid", Res.string.birth_date_invalid)
  map.put("birth_date_label", Res.string.birth_date_label)
  map.put("birth_date_placeholder", Res.string.birth_date_placeholder)
  map.put("by_elo", Res.string.by_elo)
  map.put("by_elo_change", Res.string.by_elo_change)
  map.put("by_matches", Res.string.by_matches)
  map.put("by_percentage", Res.string.by_percentage)
  map.put("by_played_time", Res.string.by_played_time)
  map.put("by_score_diff", Res.string.by_score_diff)
  map.put("by_victories", Res.string.by_victories)
  map.put("cancel", Res.string.cancel)
  map.put("cancel_match", Res.string.cancel_match)
  map.put("cancel_match_action", Res.string.cancel_match_action)
  map.put("change_anyway", Res.string.change_anyway)
  map.put("change_email_confirm", Res.string.change_email_confirm)
  map.put("change_email_confirmation_sent", Res.string.change_email_confirmation_sent)
  map.put("change_email_menu_item", Res.string.change_email_menu_item)
  map.put("change_email_title", Res.string.change_email_title)
  map.put("change_group_text", Res.string.change_group_text)
  map.put("change_group_title", Res.string.change_group_title)
  map.put("change_password_confirm", Res.string.change_password_confirm)
  map.put("change_password_menu_item", Res.string.change_password_menu_item)
  map.put("change_password_title", Res.string.change_password_title)
  map.put("check_all", Res.string.check_all)
  map.put("clear_filter", Res.string.clear_filter)
  map.put("clear_match", Res.string.clear_match)
  map.put("clear_match_desc", Res.string.clear_match_desc)
  map.put("clear_match_title", Res.string.clear_match_title)
  map.put("clear_search", Res.string.clear_search)
  map.put("click_to_start_match", Res.string.click_to_start_match)
  map.put("close", Res.string.close)
  map.put("cloud_sync", Res.string.cloud_sync)
  map.put("cloud_sync_activation_error", Res.string.cloud_sync_activation_error)
  map.put("cloud_sync_auxiliar_groups_title", Res.string.cloud_sync_auxiliar_groups_title)
  map.put("cloud_sync_cancel_subscription", Res.string.cloud_sync_cancel_subscription)
  map.put("cloud_sync_debug_cancel_simulation", Res.string.cloud_sync_debug_cancel_simulation)
  map.put("cloud_sync_debug_hint", Res.string.cloud_sync_debug_hint)
  map.put("cloud_sync_debug_simulate_multi", Res.string.cloud_sync_debug_simulate_multi)
  map.put("cloud_sync_debug_simulate_single", Res.string.cloud_sync_debug_simulate_single)
  map.put("cloud_sync_deletion_error", Res.string.cloud_sync_deletion_error)
  map.put("cloud_sync_groups_activate_label", Res.string.cloud_sync_groups_activate_label)
  map.put("cloud_sync_groups_activate_tooltip", Res.string.cloud_sync_groups_activate_tooltip)
  map.put("cloud_sync_groups_empty", Res.string.cloud_sync_groups_empty)
  map.put("cloud_sync_groups_hint", Res.string.cloud_sync_groups_hint)
  map.put("cloud_sync_groups_locked_hint", Res.string.cloud_sync_groups_locked_hint)
  map.put("cloud_sync_groups_selector_label", Res.string.cloud_sync_groups_selector_label)
  map.put("cloud_sync_groups_synced_badge", Res.string.cloud_sync_groups_synced_badge)
  map.put("cloud_sync_groups_title", Res.string.cloud_sync_groups_title)
  map.put("cloud_sync_limit_reached", Res.string.cloud_sync_limit_reached)
  map.put("cloud_sync_period_annual", Res.string.cloud_sync_period_annual)
  map.put("cloud_sync_period_monthly", Res.string.cloud_sync_period_monthly)
  map.put("cloud_sync_plan_active_badge", Res.string.cloud_sync_plan_active_badge)
  map.put("cloud_sync_plan_annual_price", Res.string.cloud_sync_plan_annual_price)
  map.put("cloud_sync_plan_benefit_code", Res.string.cloud_sync_plan_benefit_code)
}

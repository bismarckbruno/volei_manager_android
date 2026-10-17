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

public val Res.string.onboarding_add_players_instruction: StringResource by lazy {
      StringResource("string:onboarding_add_players_instruction", "onboarding_add_players_instruction", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45088, 154),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43504, 146),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40772, 146),
      ))
    }

public val Res.string.onboarding_auth_required_hint: StringResource by lazy {
      StringResource("string:onboarding_auth_required_hint", "onboarding_auth_required_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45243, 277),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43651, 261),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 40919, 261),
      ))
    }

public val Res.string.onboarding_auth_required_title: StringResource by lazy {
      StringResource("string:onboarding_auth_required_title", "onboarding_auth_required_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45521, 58),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43913, 58),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41181, 62),
      ))
    }

public val Res.string.onboarding_balance_mode_hint: StringResource by lazy {
      StringResource("string:onboarding_balance_mode_hint", "onboarding_balance_mode_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45580, 104),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 43972, 100),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41244, 92),
      ))
    }

public val Res.string.onboarding_balance_mode_question: StringResource by lazy {
      StringResource("string:onboarding_balance_mode_question", "onboarding_balance_mode_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45685, 92),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44073, 100),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41337, 92),
      ))
    }

public val Res.string.onboarding_elo_description: StringResource by lazy {
      StringResource("string:onboarding_elo_description", "onboarding_elo_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45778, 154),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44174, 154),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41430, 150),
      ))
    }

public val Res.string.onboarding_elo_title: StringResource by lazy {
      StringResource("string:onboarding_elo_title", "onboarding_elo_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45933, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44329, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41581, 44),
      ))
    }

public val Res.string.onboarding_explore_without_group: StringResource by lazy {
      StringResource("string:onboarding_explore_without_group", "onboarding_explore_without_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 45978, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44374, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41626, 72),
      ))
    }

public val Res.string.onboarding_group_name_hint: StringResource by lazy {
      StringResource("string:onboarding_group_name_hint", "onboarding_group_name_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46043, 214),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44439, 190),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41699, 190),
      ))
    }

public val Res.string.onboarding_group_name_question: StringResource by lazy {
      StringResource("string:onboarding_group_name_question", "onboarding_group_name_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46258, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44630, 74),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41890, 78),
      ))
    }

public val Res.string.onboarding_group_type_hint: StringResource by lazy {
      StringResource("string:onboarding_group_type_hint", "onboarding_group_type_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46337, 98),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44705, 98),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 41969, 90),
      ))
    }

public val Res.string.onboarding_group_type_question: StringResource by lazy {
      StringResource("string:onboarding_group_type_question", "onboarding_group_type_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46436, 78),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44804, 70),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42060, 70),
      ))
    }

public val Res.string.onboarding_name_description: StringResource by lazy {
      StringResource("string:onboarding_name_description", "onboarding_name_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46515, 75),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44875, 75),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42131, 79),
      ))
    }

public val Res.string.onboarding_name_title: StringResource by lazy {
      StringResource("string:onboarding_name_title", "onboarding_name_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46591, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44951, 37),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42211, 37),
      ))
    }

public val Res.string.onboarding_position_description: StringResource by lazy {
      StringResource("string:onboarding_position_description", "onboarding_position_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46629, 287),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 44989, 291),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42249, 295),
      ))
    }

public val Res.string.onboarding_position_title: StringResource by lazy {
      StringResource("string:onboarding_position_title", "onboarding_position_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46917, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45281, 57),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42545, 49),
      ))
    }

public val Res.string.onboarding_positions_description: StringResource by lazy {
      StringResource("string:onboarding_positions_description", "onboarding_positions_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 46975, 196),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45339, 192),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42595, 192),
      ))
    }

public val Res.string.onboarding_positions_title: StringResource by lazy {
      StringResource("string:onboarding_positions_title", "onboarding_positions_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47172, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45532, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42788, 46),
      ))
    }

public val Res.string.onboarding_priority_description: StringResource by lazy {
      StringResource("string:onboarding_priority_description", "onboarding_priority_description", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47223, 223),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45583, 223),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 42835, 215),
      ))
    }

public val Res.string.onboarding_priority_title: StringResource by lazy {
      StringResource("string:onboarding_priority_title", "onboarding_priority_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47447, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45807, 65),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43051, 53),
      ))
    }

public val Res.string.onboarding_profile_hint: StringResource by lazy {
      StringResource("string:onboarding_profile_hint", "onboarding_profile_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47513, 231),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 45873, 235),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43105, 203),
      ))
    }

public val Res.string.onboarding_profile_question: StringResource by lazy {
      StringResource("string:onboarding_profile_question", "onboarding_profile_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47745, 87),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46109, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43309, 75),
      ))
    }

public val Res.string.onboarding_skip: StringResource by lazy {
      StringResource("string:onboarding_skip", "onboarding_skip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47833, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46197, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43385, 31),
      ))
    }

public val Res.string.onboarding_spectator_auth_hint: StringResource by lazy {
      StringResource("string:onboarding_spectator_auth_hint", "onboarding_spectator_auth_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 47865, 266),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46229, 258),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43417, 246),
      ))
    }

public val Res.string.onboarding_spectator_auth_title: StringResource by lazy {
      StringResource("string:onboarding_spectator_auth_title", "onboarding_spectator_auth_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48132, 59),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46488, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43664, 63),
      ))
    }

public val Res.string.onboarding_spectator_join_hint: StringResource by lazy {
      StringResource("string:onboarding_spectator_join_hint", "onboarding_spectator_join_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48192, 250),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46548, 250),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43728, 214),
      ))
    }

public val Res.string.onboarding_spectator_join_title: StringResource by lazy {
      StringResource("string:onboarding_spectator_join_title", "onboarding_spectator_join_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48443, 99),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46799, 87),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 43943, 75),
      ))
    }

public val Res.string.onboarding_team_size_hint: StringResource by lazy {
      StringResource("string:onboarding_team_size_hint", "onboarding_team_size_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48543, 153),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 46887, 145),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44019, 129),
      ))
    }

public val Res.string.onboarding_team_size_question: StringResource by lazy {
      StringResource("string:onboarding_team_size_question", "onboarding_team_size_question", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48697, 97),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47033, 93),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44149, 89),
      ))
    }

public val Res.string.onboarding_team_size_seven_bench_hint: StringResource by lazy {
      StringResource("string:onboarding_team_size_seven_bench_hint", "onboarding_team_size_seven_bench_hint", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48795, 173),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47127, 177),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44239, 177),
      ))
    }

public val Res.string.one_game: StringResource by lazy {
      StringResource("string:one_game", "one_game", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48969, 28),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47305, 24),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44417, 28),
      ))
    }

public val Res.string.one_match: StringResource by lazy {
      StringResource("string:one_match", "one_match", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 48998, 29),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47330, 29),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44446, 29),
      ))
    }

public val Res.string.one_victory: StringResource by lazy {
      StringResource("string:one_victory", "one_victory", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49028, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47360, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44476, 31),
      ))
    }

public val Res.string.options_menu: StringResource by lazy {
      StringResource("string:options_menu", "options_menu", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49064, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47396, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44508, 36),
      ))
    }

public val Res.string.ownership_transfer_requested: StringResource by lazy {
      StringResource("string:ownership_transfer_requested", "ownership_transfer_requested", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49109, 180),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47441, 176),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44545, 144),
      ))
    }

public val Res.string.ownership_transfer_requires_own_premium_group: StringResource by lazy {
      StringResource("string:ownership_transfer_requires_own_premium_group", "ownership_transfer_requires_own_premium_group", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49290, 165),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47618, 157),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44690, 149),
      ))
    }

public val Res.string.password_label: StringResource by lazy {
      StringResource("string:password_label", "password_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49456, 38),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47776, 30),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44840, 34),
      ))
    }

public val Res.string.password_requirement_digit: StringResource by lazy {
      StringResource("string:password_requirement_digit", "password_requirement_digit", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49495, 50),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47807, 50),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44875, 50),
      ))
    }

public val Res.string.password_requirement_length: StringResource by lazy {
      StringResource("string:password_requirement_length", "password_requirement_length", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49546, 67),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47858, 67),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44926, 67),
      ))
    }

public val Res.string.password_requirement_lowercase: StringResource by lazy {
      StringResource("string:password_requirement_lowercase", "password_requirement_lowercase", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49614, 66),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47926, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 44994, 66),
      ))
    }

public val Res.string.password_requirement_met: StringResource by lazy {
      StringResource("string:password_requirement_met", "password_requirement_met", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49681, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 47993, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45061, 52),
      ))
    }

public val Res.string.password_requirement_not_met: StringResource by lazy {
      StringResource("string:password_requirement_not_met", "password_requirement_not_met", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49738, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48050, 68),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45114, 64),
      ))
    }

public val Res.string.password_requirement_special: StringResource by lazy {
      StringResource("string:password_requirement_special", "password_requirement_special", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49803, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48119, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45179, 64),
      ))
    }

public val Res.string.password_requirement_uppercase: StringResource by lazy {
      StringResource("string:password_requirement_uppercase", "password_requirement_uppercase", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49868, 66),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48184, 66),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45244, 66),
      ))
    }

public val Res.string.password_reset_email_sent: StringResource by lazy {
      StringResource("string:password_reset_email_sent", "password_reset_email_sent", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 49935, 137),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48251, 133),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45311, 105),
      ))
    }

public val Res.string.played_time: StringResource by lazy {
      StringResource("string:played_time", "played_time", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50073, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48385, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45417, 35),
      ))
    }

public val Res.string.player: StringResource by lazy {
      StringResource("string:player", "player", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50688, 26),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48988, 26),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45996, 22),
      ))
    }

public val Res.string.player_was_deleted: StringResource by lazy {
      StringResource("string:player_was_deleted", "player_was_deleted", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50113, 138),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48421, 138),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45453, 134),
      ))
    }

public val Res.string.players: StringResource by lazy {
      StringResource("string:players", "players", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50660, 27),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48960, 27),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45968, 27),
      ))
    }

public val Res.string.players_date: StringResource by lazy {
      StringResource("string:players_date", "players_date", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50252, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48560, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45588, 40),
      ))
    }

public val Res.string.players_name_already_exists: StringResource by lazy {
      StringResource("string:players_name_already_exists", "players_name_already_exists", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50297, 99),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48605, 95),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45629, 103),
      ))
    }

public val Res.string.players_per_team: StringResource by lazy {
      StringResource("string:players_per_team", "players_per_team", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50566, 60),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48870, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45878, 56),
      ))
    }

public val Res.string.players_per_team_tooltip: StringResource by lazy {
      StringResource("string:players_per_team_tooltip", "players_per_team_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50397, 168),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48701, 168),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45733, 144),
      ))
    }

public val Res.string.players_word: StringResource by lazy {
      StringResource("string:players_word", "players_word", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50627, 32),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 48927, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 45935, 32),
      ))
    }

public val Res.string.position_badge_improvised_tooltip: StringResource by lazy {
      StringResource("string:position_badge_improvised_tooltip", "position_badge_improvised_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50846, 105),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49146, 105),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46146, 101),
      ))
    }

public val Res.string.position_badge_improvised_tooltip_with_choices: StringResource by lazy {
      StringResource("string:position_badge_improvised_tooltip_with_choices", "position_badge_improvised_tooltip_with_choices", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50715, 130),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49015, 130),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46019, 126),
      ))
    }

public val Res.string.position_badge_no_preference_tooltip: StringResource by lazy {
      StringResource("string:position_badge_no_preference_tooltip", "position_badge_no_preference_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 50952, 100),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49252, 100),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46248, 96),
      ))
    }

public val Res.string.position_badge_secondary_tooltip: StringResource by lazy {
      StringResource("string:position_badge_secondary_tooltip", "position_badge_secondary_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51053, 104),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49353, 108),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46345, 104),
      ))
    }

public val Res.string.position_libero: StringResource by lazy {
      StringResource("string:position_libero", "position_libero", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51158, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49462, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46450, 31),
      ))
    }

public val Res.string.position_list_two_items: StringResource by lazy {
      StringResource("string:position_list_two_items", "position_list_two_items", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51194, 47),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49498, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46482, 51),
      ))
    }

public val Res.string.position_middle_blocker: StringResource by lazy {
      StringResource("string:position_middle_blocker", "position_middle_blocker", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51242, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49546, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46534, 51),
      ))
    }

public val Res.string.position_none: StringResource by lazy {
      StringResource("string:position_none", "position_none", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51286, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49590, 45),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46586, 41),
      ))
    }

public val Res.string.position_opposite: StringResource by lazy {
      StringResource("string:position_opposite", "position_opposite", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51328, 37),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49636, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46628, 37),
      ))
    }

public val Res.string.position_outside_hitter: StringResource by lazy {
      StringResource("string:position_outside_hitter", "position_outside_hitter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51366, 39),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49670, 43),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46666, 51),
      ))
    }

public val Res.string.position_preferred_label: StringResource by lazy {
      StringResource("string:position_preferred_label", "position_preferred_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51406, 68),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49714, 68),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46718, 64),
      ))
    }

public val Res.string.position_role_attack: StringResource by lazy {
      StringResource("string:position_role_attack", "position_role_attack", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51475, 36),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49783, 36),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46783, 36),
      ))
    }

public val Res.string.position_role_defense: StringResource by lazy {
      StringResource("string:position_role_defense", "position_role_defense", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51512, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49820, 37),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46820, 41),
      ))
    }

public val Res.string.position_role_playmaker: StringResource by lazy {
      StringResource("string:position_role_playmaker", "position_role_playmaker", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51554, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49858, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46862, 39),
      ))
    }

public val Res.string.position_secondary_label: StringResource by lazy {
      StringResource("string:position_secondary_label", "position_secondary_label", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51598, 64),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49906, 64),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46902, 60),
      ))
    }

public val Res.string.position_setter: StringResource by lazy {
      StringResource("string:position_setter", "position_setter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51663, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 49971, 39),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46963, 31),
      ))
    }

public val Res.string.position_short_libero: StringResource by lazy {
      StringResource("string:position_short_libero", "position_short_libero", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51699, 33),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50011, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 46995, 33),
      ))
    }

public val Res.string.position_short_middle_blocker: StringResource by lazy {
      StringResource("string:position_short_middle_blocker", "position_short_middle_blocker", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51733, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50045, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47029, 41),
      ))
    }

public val Res.string.position_short_opposite: StringResource by lazy {
      StringResource("string:position_short_opposite", "position_short_opposite", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51775, 35),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50087, 35),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47071, 35),
      ))
    }

public val Res.string.position_short_outside_hitter: StringResource by lazy {
      StringResource("string:position_short_outside_hitter", "position_short_outside_hitter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51811, 41),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50123, 41),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47107, 41),
      ))
    }

public val Res.string.position_short_setter: StringResource by lazy {
      StringResource("string:position_short_setter", "position_short_setter", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51853, 33),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50165, 33),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47149, 33),
      ))
    }

public val Res.string.preferred_position: StringResource by lazy {
      StringResource("string:preferred_position", "preferred_position", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51887, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50199, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47183, 50),
      ))
    }

public val Res.string.premium_nudge_action: StringResource by lazy {
      StringResource("string:premium_nudge_action", "premium_nudge_action", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51942, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50254, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47234, 44),
      ))
    }

public val Res.string.premium_purchase_gating_banner: StringResource by lazy {
      StringResource("string:premium_purchase_gating_banner", "premium_purchase_gating_banner", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 51987, 166),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50299, 154),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47279, 146),
      ))
    }

public val Res.string.premium_purchase_gating_login_required: StringResource by lazy {
      StringResource("string:premium_purchase_gating_login_required", "premium_purchase_gating_login_required", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52154, 154),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50454, 146),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47426, 138),
      ))
    }

public val Res.string.premium_purchase_gating_verify_required: StringResource by lazy {
      StringResource("string:premium_purchase_gating_verify_required", "premium_purchase_gating_verify_required", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52309, 251),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50601, 235),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47565, 231),
      ))
    }

public val Res.string.premium_screen_persona_admin: StringResource by lazy {
      StringResource("string:premium_screen_persona_admin", "premium_screen_persona_admin", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52561, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50837, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47797, 44),
      ))
    }

public val Res.string.premium_screen_persona_espectador: StringResource by lazy {
      StringResource("string:premium_screen_persona_espectador", "premium_screen_persona_espectador", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52606, 57),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50882, 57),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47842, 53),
      ))
    }

public val Res.string.premium_subscriber_badge: StringResource by lazy {
      StringResource("string:premium_subscriber_badge", "premium_subscriber_badge", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52664, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50940, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47896, 56),
      ))
    }

public val Res.string.priority: StringResource by lazy {
      StringResource("string:priority", "priority", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52721, 28),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 50997, 32),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47953, 28),
      ))
    }

public val Res.string.privacy: StringResource by lazy {
      StringResource("string:privacy", "privacy", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58596, 31),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56800, 31),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52820, 27),
      ))
    }

public val Res.string.privacy_policy: StringResource by lazy {
      StringResource("string:privacy_policy", "privacy_policy", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58541, 54),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56745, 54),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52777, 42),
      ))
    }

public val Res.string.privacy_policy_text: StringResource by lazy {
      StringResource("string:privacy_policy_text", "privacy_policy_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 52750, 5663),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 51030, 5587),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 47982, 4675),
      ))
    }

public val Res.string.privacy_policy_url: StringResource by lazy {
      StringResource("string:privacy_policy_url", "privacy_policy_url", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58414, 126),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56618, 126),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52658, 118),
      ))
    }

public val Res.string.profile_photo_choose_from_gallery: StringResource by lazy {
      StringResource("string:profile_photo_choose_from_gallery", "profile_photo_choose_from_gallery", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58628, 69),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56832, 69),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52848, 69),
      ))
    }

public val Res.string.profile_photo_remove: StringResource by lazy {
      StringResource("string:profile_photo_remove", "profile_photo_remove", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58698, 44),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56902, 44),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52918, 44),
      ))
    }

public val Res.string.progress_lost: StringResource by lazy {
      StringResource("string:progress_lost", "progress_lost", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58743, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 56947, 65),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 52963, 61),
      ))
    }

public val Res.string.project_docs: StringResource by lazy {
      StringResource("string:project_docs", "project_docs", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58809, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57013, 56),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53025, 48),
      ))
    }

public val Res.string.rate_app: StringResource by lazy {
      StringResource("string:rate_app", "rate_app", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59205, 56),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57405, 52),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53385, 52),
      ))
    }

public val Res.string.rate_app_dialog_confirm: StringResource by lazy {
      StringResource("string:rate_app_dialog_confirm", "rate_app_dialog_confirm", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58866, 63),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57070, 59),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53074, 55),
      ))
    }

public val Res.string.rate_app_dialog_dismiss: StringResource by lazy {
      StringResource("string:rate_app_dialog_dismiss", "rate_app_dialog_dismiss", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58930, 43),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57130, 47),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53130, 43),
      ))
    }

public val Res.string.rate_app_dialog_text: StringResource by lazy {
      StringResource("string:rate_app_dialog_text", "rate_app_dialog_text", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 58974, 164),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57178, 164),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53174, 156),
      ))
    }

public val Res.string.rate_app_dialog_title: StringResource by lazy {
      StringResource("string:rate_app_dialog_title", "rate_app_dialog_title", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59139, 65),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57343, 61),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53331, 53),
      ))
    }

public val Res.string.readme_url: StringResource by lazy {
      StringResource("string:readme_url", "readme_url", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59262, 110),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57458, 106),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53438, 98),
      ))
    }

public val Res.string.rebalanced_and_auto_selected_loser_player_tooltip: StringResource by lazy {
      StringResource("string:rebalanced_and_auto_selected_loser_player_tooltip", "rebalanced_and_auto_selected_loser_player_tooltip", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59373, 273),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57565, 269),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53537, 237),
      ))
    }

public val Res.string.rebalanced_player_icon_cd: StringResource by lazy {
      StringResource("string:rebalanced_player_icon_cd", "rebalanced_player_icon_cd", setOf(
        ResourceItem(setOf(LanguageQualifier("es"), ), "${MD}values-es/strings.commonMain.cvr", 59647, 125),
        ResourceItem(setOf(LanguageQualifier("pt"), RegionQualifier("BR"), ), "${MD}values-pt-rBR/strings.commonMain.cvr", 57835, 121),
        ResourceItem(setOf(), "${MD}values/strings.commonMain.cvr", 53775, 105),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainString4Resources(map: MutableMap<String, StringResource>) {
  map.put("onboarding_add_players_instruction", Res.string.onboarding_add_players_instruction)
  map.put("onboarding_auth_required_hint", Res.string.onboarding_auth_required_hint)
  map.put("onboarding_auth_required_title", Res.string.onboarding_auth_required_title)
  map.put("onboarding_balance_mode_hint", Res.string.onboarding_balance_mode_hint)
  map.put("onboarding_balance_mode_question", Res.string.onboarding_balance_mode_question)
  map.put("onboarding_elo_description", Res.string.onboarding_elo_description)
  map.put("onboarding_elo_title", Res.string.onboarding_elo_title)
  map.put("onboarding_explore_without_group", Res.string.onboarding_explore_without_group)
  map.put("onboarding_group_name_hint", Res.string.onboarding_group_name_hint)
  map.put("onboarding_group_name_question", Res.string.onboarding_group_name_question)
  map.put("onboarding_group_type_hint", Res.string.onboarding_group_type_hint)
  map.put("onboarding_group_type_question", Res.string.onboarding_group_type_question)
  map.put("onboarding_name_description", Res.string.onboarding_name_description)
  map.put("onboarding_name_title", Res.string.onboarding_name_title)
  map.put("onboarding_position_description", Res.string.onboarding_position_description)
  map.put("onboarding_position_title", Res.string.onboarding_position_title)
  map.put("onboarding_positions_description", Res.string.onboarding_positions_description)
  map.put("onboarding_positions_title", Res.string.onboarding_positions_title)
  map.put("onboarding_priority_description", Res.string.onboarding_priority_description)
  map.put("onboarding_priority_title", Res.string.onboarding_priority_title)
  map.put("onboarding_profile_hint", Res.string.onboarding_profile_hint)
  map.put("onboarding_profile_question", Res.string.onboarding_profile_question)
  map.put("onboarding_skip", Res.string.onboarding_skip)
  map.put("onboarding_spectator_auth_hint", Res.string.onboarding_spectator_auth_hint)
  map.put("onboarding_spectator_auth_title", Res.string.onboarding_spectator_auth_title)
  map.put("onboarding_spectator_join_hint", Res.string.onboarding_spectator_join_hint)
  map.put("onboarding_spectator_join_title", Res.string.onboarding_spectator_join_title)
  map.put("onboarding_team_size_hint", Res.string.onboarding_team_size_hint)
  map.put("onboarding_team_size_question", Res.string.onboarding_team_size_question)
  map.put("onboarding_team_size_seven_bench_hint", Res.string.onboarding_team_size_seven_bench_hint)
  map.put("one_game", Res.string.one_game)
  map.put("one_match", Res.string.one_match)
  map.put("one_victory", Res.string.one_victory)
  map.put("options_menu", Res.string.options_menu)
  map.put("ownership_transfer_requested", Res.string.ownership_transfer_requested)
  map.put("ownership_transfer_requires_own_premium_group", Res.string.ownership_transfer_requires_own_premium_group)
  map.put("password_label", Res.string.password_label)
  map.put("password_requirement_digit", Res.string.password_requirement_digit)
  map.put("password_requirement_length", Res.string.password_requirement_length)
  map.put("password_requirement_lowercase", Res.string.password_requirement_lowercase)
  map.put("password_requirement_met", Res.string.password_requirement_met)
  map.put("password_requirement_not_met", Res.string.password_requirement_not_met)
  map.put("password_requirement_special", Res.string.password_requirement_special)
  map.put("password_requirement_uppercase", Res.string.password_requirement_uppercase)
  map.put("password_reset_email_sent", Res.string.password_reset_email_sent)
  map.put("played_time", Res.string.played_time)
  map.put("player", Res.string.player)
  map.put("player_was_deleted", Res.string.player_was_deleted)
  map.put("players", Res.string.players)
  map.put("players_date", Res.string.players_date)
  map.put("players_name_already_exists", Res.string.players_name_already_exists)
  map.put("players_per_team", Res.string.players_per_team)
  map.put("players_per_team_tooltip", Res.string.players_per_team_tooltip)
  map.put("players_word", Res.string.players_word)
  map.put("position_badge_improvised_tooltip", Res.string.position_badge_improvised_tooltip)
  map.put("position_badge_improvised_tooltip_with_choices", Res.string.position_badge_improvised_tooltip_with_choices)
  map.put("position_badge_no_preference_tooltip", Res.string.position_badge_no_preference_tooltip)
  map.put("position_badge_secondary_tooltip", Res.string.position_badge_secondary_tooltip)
  map.put("position_libero", Res.string.position_libero)
  map.put("position_list_two_items", Res.string.position_list_two_items)
  map.put("position_middle_blocker", Res.string.position_middle_blocker)
  map.put("position_none", Res.string.position_none)
  map.put("position_opposite", Res.string.position_opposite)
  map.put("position_outside_hitter", Res.string.position_outside_hitter)
  map.put("position_preferred_label", Res.string.position_preferred_label)
  map.put("position_role_attack", Res.string.position_role_attack)
  map.put("position_role_defense", Res.string.position_role_defense)
  map.put("position_role_playmaker", Res.string.position_role_playmaker)
  map.put("position_secondary_label", Res.string.position_secondary_label)
  map.put("position_setter", Res.string.position_setter)
  map.put("position_short_libero", Res.string.position_short_libero)
  map.put("position_short_middle_blocker", Res.string.position_short_middle_blocker)
  map.put("position_short_opposite", Res.string.position_short_opposite)
  map.put("position_short_outside_hitter", Res.string.position_short_outside_hitter)
  map.put("position_short_setter", Res.string.position_short_setter)
  map.put("preferred_position", Res.string.preferred_position)
  map.put("premium_nudge_action", Res.string.premium_nudge_action)
  map.put("premium_purchase_gating_banner", Res.string.premium_purchase_gating_banner)
  map.put("premium_purchase_gating_login_required", Res.string.premium_purchase_gating_login_required)
  map.put("premium_purchase_gating_verify_required", Res.string.premium_purchase_gating_verify_required)
  map.put("premium_screen_persona_admin", Res.string.premium_screen_persona_admin)
  map.put("premium_screen_persona_espectador", Res.string.premium_screen_persona_espectador)
  map.put("premium_subscriber_badge", Res.string.premium_subscriber_badge)
  map.put("priority", Res.string.priority)
  map.put("privacy", Res.string.privacy)
  map.put("privacy_policy", Res.string.privacy_policy)
  map.put("privacy_policy_text", Res.string.privacy_policy_text)
  map.put("privacy_policy_url", Res.string.privacy_policy_url)
  map.put("profile_photo_choose_from_gallery", Res.string.profile_photo_choose_from_gallery)
  map.put("profile_photo_remove", Res.string.profile_photo_remove)
  map.put("progress_lost", Res.string.progress_lost)
  map.put("project_docs", Res.string.project_docs)
  map.put("rate_app", Res.string.rate_app)
  map.put("rate_app_dialog_confirm", Res.string.rate_app_dialog_confirm)
  map.put("rate_app_dialog_dismiss", Res.string.rate_app_dialog_dismiss)
  map.put("rate_app_dialog_text", Res.string.rate_app_dialog_text)
  map.put("rate_app_dialog_title", Res.string.rate_app_dialog_title)
  map.put("readme_url", Res.string.readme_url)
  map.put("rebalanced_and_auto_selected_loser_player_tooltip", Res.string.rebalanced_and_auto_selected_loser_player_tooltip)
  map.put("rebalanced_player_icon_cd", Res.string.rebalanced_player_icon_cd)
}

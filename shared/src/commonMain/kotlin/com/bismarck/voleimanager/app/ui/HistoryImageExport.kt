package com.bismarck.voleimanager.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.ui.platform.PlatformActions
import com.bismarck.voleimanager.app.ui.theme.AppTheme
import com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel

/** Largura fixa (em dp) de [ExportableImageContent]; a captura usa a densidade real do aparelho. */
private const val EXPORT_WIDTH_DP = 400

/**
 * Renderiza o histórico fora da tela via [PlatformActions.captureComposableAsPng], converte em PNG
 * e entrega ao [VoleiViewModel.shareHistoryImage].
 */
fun captureHistoryScreenAsImage(
    viewModel: VoleiViewModel,
    actions: PlatformActions,
    matches: List<MatchHistory>?,
    matchSortMode: MatchSortMode?,
    players: List<HistoryPlayerInfo>?,
    playerSortMode: PlayerSortMode?,
    date: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean,
    matchDurationsMinutes: Map<Int, Int>? = null,
    averagePlayersEloText: String? = null,
    averageMatchDurationText: String? = null
) {
    val groupName = viewModel.currentGroupConfig.value.groupName
    val usesPositions = viewModel.currentGroupConfig.value.type.usesPositions
    val teamAColorFamily = teamAccentColorFamily(viewModel.effectiveTeamAColor.value, isDarkTheme)
    val teamBColorFamily = teamAccentColorFamily(viewModel.effectiveTeamBColor.value, isDarkTheme)
    actions.captureComposableAsPng(
        widthDp = EXPORT_WIDTH_DP,
        content = {
            AppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ExportableImageContent(
                        matches = matches,
                        matchSortMode = matchSortMode,
                        players = players,
                        playerSortMode = playerSortMode,
                        groupName = groupName,
                        date = date,
                        isDarkTheme = isDarkTheme,
                        showElo = showElo,
                        showScore = showScore,
                        matchDurationsMinutes = matchDurationsMinutes,
                        averagePlayersEloText = averagePlayersEloText,
                        averageMatchDurationText = averageMatchDurationText,
                        usesPositions = usesPositions,
                        teamAColorFamily = teamAColorFamily,
                        teamBColorFamily = teamBColorFamily
                    )
                }
            }
        },
        onPng = { pngBytes -> viewModel.shareHistoryImage(pngBytes, date) }
    )
}

package com.bismarck.voleimanager.app.ui

import android.content.Context
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import java.io.ByteArrayOutputStream

/**
 * Renderiza o hist?rico num [androidx.compose.ui.platform.ComposeView] fora da tela, converte em
 * PNG e entrega ao [VoleiViewModel.shareHistoryImage]. Fica na camada de UI Android porque usa
 * `View`/`Bitmap` (no iOS ser? substitu?do por captura via `GraphicsLayer`).
 */
fun captureHistoryScreenAsImage(
    viewModel: VoleiViewModel,
    context: Context,
    view: android.view.View,
    matches: List<MatchHistory>?,
    matchSortMode: com.bismarck.voleimanager.app.ui.MatchSortMode?,
    players: List<com.bismarck.voleimanager.app.ui.HistoryPlayerInfo>?,
    playerSortMode: com.bismarck.voleimanager.app.ui.PlayerSortMode?,
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
    val teamAColorFamily = com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily(viewModel.effectiveTeamAColor.value, isDarkTheme)
    val teamBColorFamily = com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily(viewModel.effectiveTeamBColor.value, isDarkTheme)
    val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
        setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

        setContent {
            com.bismarck.voleimanager.app.ui.theme.AppTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                androidx.compose.material3.Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    com.bismarck.voleimanager.app.ui.ExportableImageContent(
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
        }
    }
    val scrollView = android.widget.ScrollView(context).apply {
        addView(composeView)
        alpha = 0f
        isVerticalScrollBarEnabled = false
    }
    val root = view.rootView as? android.view.ViewGroup
    if (root != null) {
        root.addView(scrollView, android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT))
        scrollView.postDelayed({
            try {
                // ExportableImageContent tem largura fixa de 400.dp (ver AppScreens.kt). Convertemos
                // esse valor para pixels usando a densidade real do dispositivo — usar um valor fixo
                // em pixels (ex.: 1440) fazia a imagem sair com margens em branco desproporcionais
                // (e por consequência "mais larga") em aparelhos com densidade de tela menor, já que
                // o mesmo "1440px" representa uma largura em dp bem maior nesses aparelhos. Isso
                // acontecia igualmente para Administrador e Espectador, mas só ficava visível quando
                // os dois usavam celulares com densidades diferentes nos testes.
                val exportWidthPx = (400f * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
                composeView.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(exportWidthPx, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                )
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
                if (composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
                    val bitmap = android.graphics.Bitmap.createBitmap(composeView.measuredWidth, composeView.measuredHeight, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    composeView.draw(canvas)
                    val pngBytes = ByteArrayOutputStream().also {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }.toByteArray()
                    viewModel.shareHistoryImage(pngBytes, date)
                }
            } catch (e: Exception) { e.printStackTrace() } finally { root.removeView(scrollView) }
        }, 500)
    }
}

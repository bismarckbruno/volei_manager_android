package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.simpleScrollbar(state: LazyListState): Modifier = this.drawWithContent {
    drawContent()
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return@drawWithContent
    val totalItems = state.layoutInfo.totalItemsCount
    if (totalItems <= visibleItems.size) return@drawWithContent

    val firstItem = visibleItems.first()
    val firstOffset = firstItem.offset.coerceAtMost(0).toFloat() / firstItem.size.coerceAtLeast(1).toFloat()
    val exactIndex = firstItem.index.toFloat() - firstOffset

    val fractionVisible = visibleItems.size.toFloat() / totalItems.toFloat()
    val fractionScrolled = exactIndex / totalItems.toFloat()

    val verticalPadding = 8.dp.toPx() // Distância do topo e rodapé do container
    val availableHeight = size.height - (verticalPadding * 2)

    val scrollbarHeight = availableHeight * fractionVisible
    val scrollbarY = verticalPadding + (availableHeight * fractionScrolled)

    drawRoundRect(
        color = Color.Gray.copy(alpha = 0.5f),
        topLeft = Offset(size.width - 8.dp.toPx(), scrollbarY), // Empurra um pouco mais para a esquerda
        size = Size(4.dp.toPx(), scrollbarHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
}

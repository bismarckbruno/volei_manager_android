package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun LazyListFastScroller(
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableFloatStateOf(0f) }
    var keepVisible by remember { mutableStateOf(false) }
    val layoutInfo by remember(state) { derivedStateOf { state.layoutInfo } }
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    if (totalItems <= 0 || visibleItems.isEmpty()) return

    val viewportHeightPx =
        (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat().coerceAtLeast(1f)
    val avgItemSizePx = visibleItems.map { it.size }.average().toFloat().coerceAtLeast(1f)
    val estimatedContentHeightPx = (avgItemSizePx * totalItems.toFloat()).coerceAtLeast(viewportHeightPx)
    val currentScrollPx =
        (state.firstVisibleItemIndex.toFloat() * avgItemSizePx) + state.firstVisibleItemScrollOffset.toFloat()
    val maxScrollablePx = (estimatedContentHeightPx - viewportHeightPx).coerceAtLeast(1f)
    val scrollFraction = (currentScrollPx / maxScrollablePx).coerceIn(0f, 1f)

    val visibleFraction = (viewportHeightPx / estimatedContentHeightPx).coerceIn(0.05f, 1f)
    val minThumbHeightPx = with(density) { 22.dp.toPx() }
    val maxThumbHeightPx = (trackHeightPx * 0.75f).coerceAtLeast(minThumbHeightPx)
    val thumbHeightPx = (trackHeightPx * visibleFraction).coerceIn(minThumbHeightPx, maxThumbHeightPx)
    val thumbTopPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f) * scrollFraction
    val thumbHeightDp = with(density) { thumbHeightPx.toDp() }
    val targetAlpha = if (state.isScrollInProgress || keepVisible) 1f else 0f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = if (targetAlpha > 0f) 120 else 320),
        label = "FastScrollerFade"
    )

    LaunchedEffect(Unit) {
        // Briefly reveal the indicator when a screen with a scrollable list appears.
        keepVisible = true
        delay(800)
        if (!state.isScrollInProgress) keepVisible = false
    }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            keepVisible = true
        } else {
            delay(800)
            keepVisible = false
        }
    }

    fun scrollToFraction(rawFraction: Float) {
        if (totalItems <= 1) return
        keepVisible = true
        val fraction = rawFraction.coerceIn(0f, 1f)
        val targetIndex = (fraction * (totalItems - 1)).roundToInt().coerceIn(0, totalItems - 1)
        scope.launch {
            state.scrollToItem(targetIndex)
            delay(800)
            if (!state.isScrollInProgress) keepVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(14.dp)
            .padding(vertical = 8.dp)
            .alpha(alpha)
            .onSizeChanged { trackHeightPx = it.height.toFloat() }
            .pointerInput(totalItems) {
                detectTapGestures { tapOffset: Offset ->
                    val fraction = (tapOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                    scrollToFraction(fraction)
                }
            }
            .pointerInput(totalItems) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                    scrollToFraction(fraction)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, thumbTopPx.roundToInt()) }
                .height(thumbHeightDp)
                .size(width = 4.dp, height = thumbHeightDp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(999.dp)
                )
        )
    }
}



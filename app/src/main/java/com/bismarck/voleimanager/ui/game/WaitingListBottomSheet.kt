package com.bismarck.voleimanager.ui.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bismarck.voleimanager.data.model.Player
import com.bismarck.voleimanager.ui.components.simpleScrollbar
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator
import kotlin.math.roundToInt

private enum class WaitingSection { ACTIVE, INACTIVE }

private data class DragState(
    val player: Player,
    val fromSection: WaitingSection,
    val initialCenterY: Float,
    val offsetY: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WaitingListBottomSheet(
    viewModel: VoleiViewModel,
    waitingList: List<Player>,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>,
    showElo: Boolean,
    onDismiss: () -> Unit
) {
    val absentPlayers = remember(allPlayers, presentPlayerIds) {
        allPlayers
            .filter { !presentPlayerIds.contains(it.id) }
            .sortedBy { it.name.lowercase() }
    }
    val activeBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val inactiveBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    var emptyActiveBounds by remember { mutableStateOf<ClosedFloatingPointRange<Float>?>(null) }
    var inactiveHeaderBounds by remember { mutableStateOf<ClosedFloatingPointRange<Float>?>(null) }
    var dragState by remember { mutableStateOf<DragState?>(null) }
    var selectedPlayerForMenu by remember { mutableStateOf<Player?>(null) }
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    fun activeTargetIndex(dropY: Float): Int? {
        if (waitingList.isEmpty()) {
            return if (emptyActiveBounds?.let { dropY in it } == true) 0 else null
        }

        val measured = waitingList.mapIndexedNotNull { index, player ->
            activeBounds[player.id]?.let { index to it }
        }
        if (measured.isEmpty()) return null

        val firstCenter = (measured.first().second.start + measured.first().second.endInclusive) / 2f
        if (dropY <= firstCenter) return 0

        measured.forEachIndexed { measuredIndex, (_, bounds) ->
            val center = (bounds.start + bounds.endInclusive) / 2f
            if (dropY <= center) {
                return measuredIndex
            }
        }

        return measured.size
    }

    fun isOverInactiveSection(dropY: Float): Boolean {
        if (inactiveHeaderBounds?.let { dropY in it } == true) return true
        return inactiveBounds.values.any { dropY in it }
    }

    fun finishDrag() {
        val currentDrag = dragState ?: return
        val dropY = currentDrag.initialCenterY + currentDrag.offsetY
        when (currentDrag.fromSection) {
            WaitingSection.ACTIVE -> {
                if (isOverInactiveSection(dropY)) {
                    viewModel.removePlayerFromWaitingList(currentDrag.player)
                } else {
                    val currentIndex = waitingList.indexOfFirst { it.id == currentDrag.player.id }
                    val targetIndex = activeTargetIndex(dropY)
                    if (currentIndex != -1 && targetIndex != null) {
                        val adjustedTarget = if (targetIndex > currentIndex) targetIndex - 1 else targetIndex
                        viewModel.moveWaitingPlayerToIndex(currentDrag.player, adjustedTarget)
                    }
                }
            }

            WaitingSection.INACTIVE -> {
                val targetIndex = activeTargetIndex(dropY)
                if (targetIndex != null) {
                    viewModel.insertPlayerIntoWaitingList(currentDrag.player, targetIndex)
                }
            }
        }
        dragState = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = "Na espera (${waitingList.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .simpleScrollbar(listState)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (waitingList.isEmpty()) {
                    item(key = "empty_active") {
                        Card(
                            modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxWidth()
                                .onGloballyPositioned {
                                    val top = it.positionInParent().y
                                    emptyActiveBounds = top..(top + it.size.height)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Arraste jogadores disponíveis para cá",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                } else {
                    itemsIndexed(waitingList, key = { _, player -> "active_${player.id}" }) { index, player ->
                        val isThisDragging = dragState?.player?.id == player.id
                        WaitingListPlayerItem(
                            modifier = Modifier.animateItemPlacement(),
                            index = index,
                            player = player,
                            showElo = showElo,
                            isDragging = isThisDragging,
                            dragOffsetY = if (isThisDragging) dragState?.offsetY ?: 0f else 0f,
                            onMeasured = { range -> activeBounds[player.id] = range },
                            onLongPress = { selectedPlayerForMenu = player },
                            onDragStart = {
                                val bounds = activeBounds[player.id] ?: return@WaitingListPlayerItem
                                dragState = DragState(
                                    player = player,
                                    fromSection = WaitingSection.ACTIVE,
                                    initialCenterY = (bounds.start + bounds.endInclusive) / 2f
                                )
                            },
                            onDrag = { dragAmount ->
                                dragState = dragState?.takeIf { it.player.id == player.id }?.let { current ->
                                    current.copy(offsetY = current.offsetY + dragAmount)
                                }
                            },
                            onDragEnd = ::finishDrag
                        )
                    }
                }

                item(key = "inactive_header") {
                    Column(
                        modifier = Modifier
                            .animateItemPlacement()
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                val top = it.positionInParent().y
                                inactiveHeaderBounds = top..(top + it.size.height)
                            }
                    ) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Text(
                            "Jogadores disponíveis (${absentPlayers.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (absentPlayers.isEmpty()) {
                    item(key = "inactive_empty") {
                        Text(
                            text = "Arraste jogadores ativos para cá para removê-los da fila",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    itemsIndexed(absentPlayers, key = { _, player -> "inactive_${player.id}" }) { _, player ->
                        val isThisDragging = dragState?.player?.id == player.id
                        InactivePlayerItem(
                            modifier = Modifier.animateItemPlacement(),
                            player = player,
                            showElo = showElo,
                            isDragging = isThisDragging,
                            dragOffsetY = if (isThisDragging) dragState?.offsetY ?: 0f else 0f,
                            onMeasured = { range -> inactiveBounds[player.id] = range },
                            onLongPress = { selectedPlayerForMenu = player },
                            onDragStart = {
                                val bounds = inactiveBounds[player.id] ?: return@InactivePlayerItem
                                dragState = DragState(
                                    player = player,
                                    fromSection = WaitingSection.INACTIVE,
                                    initialCenterY = (bounds.start + bounds.endInclusive) / 2f
                                )
                            },
                            onDrag = { dragAmount ->
                                dragState = dragState?.takeIf { it.player.id == player.id }?.let { current ->
                                    current.copy(offsetY = current.offsetY + dragAmount)
                                }
                            },
                            onDragEnd = ::finishDrag
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    selectedPlayerForMenu?.let { player ->
        val isActive = waitingList.any { it.id == player.id }
        AlertDialog(
            onDismissRequest = { selectedPlayerForMenu = null },
            title = { Text(player.name) },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedPlayerForMenu = null }) {
                    Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.movePlayerToBeginning(player)
                            selectedPlayerForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Começo da fila")
                    }

                    Button(
                        onClick = {
                            viewModel.movePlayerToEnd(player)
                            selectedPlayerForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Final da fila")
                    }

                    if (isActive) {
                        Button(
                            onClick = {
                                viewModel.removePlayerFromWaitingList(player)
                                selectedPlayerForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Remover da fila")
                        }
                    }

                    TextButton(
                        onClick = { selectedPlayerForMenu = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WaitingListPlayerItem(
    modifier: Modifier = Modifier,
    index: Int,
    player: Player,
    showElo: Boolean,
    isDragging: Boolean,
    dragOffsetY: Float,
    onMeasured: (ClosedFloatingPointRange<Float>) -> Unit,
    onLongPress: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .alpha(if (isDragging) 0.92f else 1f)
            .onGloballyPositioned {
                val top = it.positionInParent().y
                onMeasured(top..(top + it.size.height))
            }
            .combinedClickable(
                onClick = { },
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${index + 1}º",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (showElo) {
                        Text(
                            EloCalculator.formatElo(player.elo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Arrastar para reordenar",
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(player.id) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd,
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            }
                        )
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InactivePlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
    showElo: Boolean,
    isDragging: Boolean,
    dragOffsetY: Float,
    onMeasured: (ClosedFloatingPointRange<Float>) -> Unit,
    onLongPress: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f)
            .alpha(if (isDragging) 0.92f else 1f)
            .onGloballyPositioned {
                val top = it.positionInParent().y
                onMeasured(top..(top + it.size.height))
            }
            .combinedClickable(
                onClick = { },
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (showElo) {
                    Text(
                        EloCalculator.formatElo(player.elo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Arrastar para adicionar à fila",
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(player.id) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd,
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            }
                        )
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}













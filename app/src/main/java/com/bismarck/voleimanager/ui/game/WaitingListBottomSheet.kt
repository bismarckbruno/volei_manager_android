package com.bismarck.voleimanager.ui.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bismarck.voleimanager.data.model.Player
import com.bismarck.voleimanager.ui.components.simpleScrollbar
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator
import kotlin.math.roundToInt

private enum class WaitingSection { ACTIVE }

private data class DragState(
    val player: Player,
    val fromSection: WaitingSection,
    val fingerY: Float  // posição atual do dedo no viewport
)

private sealed class UndoAction {
    data class Move(val player: Player, val fromIndex: Int, val toIndex: Int, val section: WaitingSection) : UndoAction()
    data class Remove(val player: Player, val fromIndex: Int) : UndoAction()
    data class Add(val player: Player, val toIndex: Int) : UndoAction()
}

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
        allPlayers.filter { !presentPlayerIds.contains(it.id) }.sortedBy { it.name.lowercase() }
    }
    val activeBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    val inactiveBounds = remember { mutableStateMapOf<Int, ClosedFloatingPointRange<Float>>() }
    var emptyActiveBounds by remember { mutableStateOf<ClosedFloatingPointRange<Float>?>(null) }
    var dragState by remember { mutableStateOf<DragState?>(null) }
    var localReorder by remember { mutableStateOf<List<Player>?>(null) }
    var menuAnchorPlayer by remember { mutableStateOf<Player?>(null) }
    var menuAnchorOffset by remember { mutableStateOf(DpOffset.Zero) }
    var undoAction by remember { mutableStateOf<UndoAction?>(null) }
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current

    fun openMenuForPlayer(player: Player, offset: DpOffset) {
        menuAnchorPlayer = player
        menuAnchorOffset = offset
    }

    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    fun activeTargetIndex(dropY: Float): Int? {
        val currentList = localReorder ?: waitingList
        if (currentList.isEmpty()) {
            return if (emptyActiveBounds?.let { dropY in it } == true) 0 else null
        }
        val measured = currentList.mapIndexedNotNull { index, player ->
            activeBounds[player.id]?.let { index to it }
        }
        if (measured.isEmpty()) return null

        val firstVisible = measured.first()
        val firstCenter = (firstVisible.second.start + firstVisible.second.endInclusive) / 2f
        if (dropY <= firstCenter) return firstVisible.first

        for ((index, bounds) in measured) {
            val center = (bounds.start + bounds.endInclusive) / 2f
            if (dropY <= center) return index
        }
        return measured.last().first + 1
    }

    fun updateLiveReorder(fingerY: Float) {
        val currentDrag = dragState ?: return
        val targetIndex = activeTargetIndex(fingerY) ?: return
        val currentList = localReorder ?: waitingList
        val currentIndex = currentList.indexOfFirst { it.id == currentDrag.player.id }
        if (currentIndex != -1 && currentIndex != targetIndex) {
            val adjustedTarget = if (targetIndex > currentIndex) targetIndex - 1 else targetIndex
            if (adjustedTarget != currentIndex && adjustedTarget in 0..currentList.size) {
                val mutList = currentList.toMutableList()
                val p = mutList.removeAt(currentIndex)
                mutList.add(adjustedTarget.coerceIn(0, mutList.size), p)
                localReorder = mutList
            }
        }
    }

    fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = scope.launch {
            while (true) {
                val current = dragState ?: break
                val layout = listState.layoutInfo
                val topEdge = layout.viewportStartOffset.toFloat()
                val bottomEdge = layout.viewportEndOffset.toFloat()
                val edgeThreshold = 120f
                val fingerY = current.fingerY
                val delta = when {
                    fingerY < topEdge + edgeThreshold && listState.canScrollBackward -> {
                        val intensity = ((topEdge + edgeThreshold - fingerY) / edgeThreshold).coerceIn(0f, 1f)
                        -(8f + 28f * intensity)
                    }
                    fingerY > bottomEdge - edgeThreshold && listState.canScrollForward -> {
                        val intensity = ((fingerY - (bottomEdge - edgeThreshold)) / edgeThreshold).coerceIn(0f, 1f)
                        8f + 28f * intensity
                    }
                    else -> 0f
                }
                if (delta != 0f) {
                    listState.scrollBy(delta)
                    updateLiveReorder(fingerY)
                }
                delay(16)
            }
            autoScrollJob = null
        }
    }

    fun centerActivePlayer(index: Int) {
        scope.launch {
            delay(120)
            val total = listState.layoutInfo.totalItemsCount
            if (total <= 0) return@launch
            val safeIndex = index.coerceIn(0, total - 1)
            val viewportHeight = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            if (viewportHeight > 0) {
                val itemHeightGuess = with(density) { 72.dp.roundToPx() }
                val centeredOffset = ((viewportHeight - itemHeightGuess) / 2).coerceAtLeast(0)
                listState.animateScrollToItem(safeIndex, centeredOffset)
            } else {
                listState.animateScrollToItem(safeIndex)
            }
        }
    }

    fun performUndo() {
        val action = undoAction ?: return
        when (action) {
            is UndoAction.Move -> viewModel.moveWaitingPlayerToIndex(action.player, action.fromIndex)
            is UndoAction.Remove -> viewModel.insertPlayerIntoWaitingList(action.player, action.fromIndex)
            is UndoAction.Add -> viewModel.removePlayerFromWaitingList(action.player)
        }
        undoAction = null
    }

    fun showSnackbar(message: String, hasUndo: Boolean) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (hasUndo) "Desfazer" else null,
                duration = SnackbarDuration.Short
            )
            if (result.toString() == "ActionPerformed" && hasUndo) performUndo() else undoAction = null
        }
    }

    fun movePlayerInWaitingList(player: Player, targetIndex: Int) {
        val currentIndex = waitingList.indexOfFirst { it.id == player.id }
        if (currentIndex != -1 && currentIndex != targetIndex) {
            undoAction = UndoAction.Move(player, currentIndex, targetIndex, WaitingSection.ACTIVE)
            viewModel.moveWaitingPlayerToIndex(player, targetIndex)
            showSnackbar("${player.name} foi de ${currentIndex + 1}º para ${targetIndex + 1}º", true)
            centerActivePlayer(targetIndex)
        }
    }

    fun handleRemoveFromWaiting(player: Player) {
        val index = waitingList.indexOfFirst { it.id == player.id }
        undoAction = UndoAction.Remove(player, index)
        viewModel.removePlayerFromWaitingList(player)
        showSnackbar("${player.name} saiu da fila de espera", true)
    }

    fun finishDrag() {
        stopAutoScroll()
        val currentDrag = dragState ?: return
        dragState = null
        val finalReorder = localReorder
        if (finalReorder != null) {
            val newIndex = finalReorder.indexOfFirst { it.id == currentDrag.player.id }
            val oldIndex = waitingList.indexOfFirst { it.id == currentDrag.player.id }
            if (newIndex != -1 && oldIndex != -1 && newIndex != oldIndex) {
                 viewModel.moveWaitingPlayerToIndex(currentDrag.player, newIndex)
                 undoAction = UndoAction.Move(currentDrag.player, oldIndex, newIndex, WaitingSection.ACTIVE)
                 showSnackbar("${currentDrag.player.name} foi movido", true)
                 centerActivePlayer(newIndex)
            }
        }
        localReorder = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = "Na espera (${waitingList.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            // Box: LazyColumn + overlay flutuante do card arrastado
            Box(modifier = Modifier.weight(1f, fill = true).fillMaxWidth()) {
                val currentWaitingList = localReorder ?: waitingList
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().simpleScrollbar(listState).padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (currentWaitingList.isEmpty()) {
                        item(key = "empty_active") {
                            Card(
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .fillMaxWidth()
                                    .onGloballyPositioned {
                                        val top = it.positionInParent().y
                                        emptyActiveBounds = top..(top + it.size.height)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "Nenhum jogador na fila de espera",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            }
                        }
                    } else {
                        itemsIndexed(currentWaitingList, key = { _, player -> "active_${player.id}" }) { index, player ->
                            val isThisDragging = dragState?.player?.id == player.id
                            WaitingListPlayerItem(
                                modifier = Modifier.animateItemPlacement(),
                                index = index,
                                player = player,
                                showElo = showElo,
                                isDragging = isThisDragging,
                                onMeasured = { range -> activeBounds[player.id] = range },
                                onLongClick = { offset -> openMenuForPlayer(player, offset) },
                                onDragStart = {
                                    val bounds = activeBounds[player.id] ?: return@WaitingListPlayerItem
                                    dragState = DragState(
                                        player = player,
                                        fromSection = WaitingSection.ACTIVE,
                                        fingerY = (bounds.start + bounds.endInclusive) / 2f
                                    )
                                    startAutoScroll()
                                },
                                onDrag = { dragAmount ->
                                    dragState = dragState?.takeIf { it.player.id == player.id }
                                        ?.let { current -> 
                                            val newY = current.fingerY + dragAmount
                                            updateLiveReorder(newY)
                                            current.copy(fingerY = newY) 
                                        }
                                },
                                onDragEnd = ::finishDrag
                            )
                        }
                    }

                    item(key = "inactive_header") {
                        Column(modifier = Modifier.animateItemPlacement().fillMaxWidth()) {
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
                                text = "Todos os jogadores estão na fila",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.animateItemPlacement().padding(horizontal = 8.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        itemsIndexed(absentPlayers, key = { _, player -> "inactive_${player.id}" }) { _, player ->
                            InactivePlayerItem(
                                modifier = Modifier.animateItemPlacement(),
                                player = player,
                                showElo = showElo,
                                onMeasured = { range -> inactiveBounds[player.id] = range },
                                onLongClick = { offset -> openMenuForPlayer(player, offset) },
                                onPlusClick = { offset -> openMenuForPlayer(player, offset) }
                            )
                        }
                    }
                } // end LazyColumn

                // Overlay: card flutuante que segue o dedo — nunca sai da tela com o scroll
                dragState?.let { state ->
                    val currentList = localReorder ?: waitingList
                    val currentIndex = currentList.indexOfFirst { it.id == state.player.id }
                    val itemHeightPx = activeBounds[state.player.id]
                        ?.let { it.endInclusive - it.start }
                        ?: with(density) { 68.dp.toPx() }
                    if (currentIndex != -1) {
                        DraggedItemOverlay(
                            player = state.player,
                            index = currentIndex,
                            fingerY = state.fingerY,
                            itemHeightPx = itemHeightPx,
                            showElo = showElo
                        )
                    }
                }
            } // end Box

            Spacer(Modifier.height(16.dp))

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } // end Column
    } // end ModalBottomSheet

    // Dropdown menu para jogadores ativos e inativos
    menuAnchorPlayer?.let { player ->
        val isActive = waitingList.any { it.id == player.id }
        Box {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { menuAnchorPlayer = null },
                offset = menuAnchorOffset
            ) {
                if (isActive) {
                    DropdownMenuItem(
                        text = { Text("Começo da fila") },
                        onClick = {
                            val oldIndex = waitingList.indexOfFirst { it.id == player.id }
                            viewModel.movePlayerToBeginning(player)
                            undoAction = UndoAction.Move(player, oldIndex, 0, WaitingSection.ACTIVE)
                            showSnackbar("${player.name} foi para o começo da fila", true)
                            menuAnchorPlayer = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Final da fila") },
                        onClick = {
                            val oldIndex = waitingList.indexOfFirst { it.id == player.id }
                            viewModel.movePlayerToEnd(player)
                            undoAction = UndoAction.Move(player, oldIndex, waitingList.size - 1, WaitingSection.ACTIVE)
                            showSnackbar("${player.name} foi para o final da fila", true)
                            menuAnchorPlayer = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remover da fila", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            handleRemoveFromWaiting(player)
                            menuAnchorPlayer = null
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Começo da fila") },
                        onClick = {
                            val targetIndex = 0
                            undoAction = UndoAction.Add(player, targetIndex)
                            viewModel.insertPlayerIntoWaitingList(player, targetIndex)
                            showSnackbar("${player.name} entrou no começo da fila", true)
                            menuAnchorPlayer = null
                            scope.launch { delay(100); listState.animateScrollToItem(0) }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Final da fila") },
                        onClick = {
                            val targetIndex = waitingList.size
                            undoAction = UndoAction.Add(player, targetIndex)
                            viewModel.insertPlayerIntoWaitingList(player, targetIndex)
                            showSnackbar("${player.name} entrou no final da fila", true)
                            menuAnchorPlayer = null
                            scope.launch {
                                delay(100)
                                if (waitingList.isNotEmpty()) listState.animateScrollToItem(waitingList.size - 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

// Card flutuante fora do LazyColumn: segue o dedo durante o drag e nunca é descomposto pelo scroll.
@Composable
private fun DraggedItemOverlay(
    player: Player,
    index: Int,
    fingerY: Float,
    itemHeightPx: Float,
    showElo: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .offset { IntOffset(0, (fingerY - itemHeightPx / 2).roundToInt()) }
            .zIndex(1f)
            .alpha(0.93f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
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
                    Text(player.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
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
    onMeasured: (ClosedFloatingPointRange<Float>) -> Unit,
    onLongClick: (DpOffset) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val density = LocalDensity.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            // Placeholder invisível durante drag: mantém espaço na lista mas não renderiza
            .alpha(if (isDragging) 0f else 1f)
            .onGloballyPositioned {
                val top = it.positionInParent().y
                onMeasured(top..(top + it.size.height))
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { pressOffset ->
                            onLongClick(with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) })
                        }
                    )
                },
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
                    Text(player.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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
    onMeasured: (ClosedFloatingPointRange<Float>) -> Unit,
    onLongClick: (DpOffset) -> Unit,
    onPlusClick: (DpOffset) -> Unit
) {
    val density = LocalDensity.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                val top = it.positionInParent().y
                onMeasured(top..(top + it.size.height))
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { pressOffset ->
                            onLongClick(with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) })
                        }
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (showElo) {
                    Text(
                        EloCalculator.formatElo(player.elo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.Add,
                contentDescription = "Adicionar à fila",
                modifier = Modifier
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { pressOffset ->
                                onPlusClick(with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) })
                            },
                            onLongPress = { pressOffset ->
                                onPlusClick(with(density) { DpOffset(pressOffset.x.toDp(), pressOffset.y.toDp()) })
                            }
                        )
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

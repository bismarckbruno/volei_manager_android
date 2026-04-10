package com.bismarck.voleimanager.ui.game

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.style.TextOverflow
import com.bismarck.voleimanager.data.model.Player
import com.bismarck.voleimanager.ui.components.simpleScrollbar
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator

private enum class WaitingSection { ACTIVE }

private sealed class UndoAction {
    data class Move(
        val player: Player,
        val fromIndex: Int,
        val toIndex: Int,
        val section: WaitingSection
    ) : UndoAction()

    data class Remove(val player: Player, val fromIndex: Int) : UndoAction()
    data class Add(val player: Player, val toIndex: Int) : UndoAction()
}


private const val WAITING_ITEM_REORDER_DURATION_MS = 220

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WaitingListContent(
    viewModel: VoleiViewModel,
    waitingList: List<Player>,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>,
    showElo: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    externalSnackbarHostState: SnackbarHostState? = null
) {
    val absentPlayers = remember(allPlayers, presentPlayerIds) {
        allPlayers.filter { !presentPlayerIds.contains(it.id) }.sortedBy { it.name.lowercase() }
    }
    var undoAction by remember { mutableStateOf<UndoAction?>(null) }
    var scrollHighlightJob by remember { mutableStateOf<Job?>(null) }
    var highlightedPlayerId by remember { mutableStateOf<Int?>(null) }
    var highlightPulse by remember { mutableIntStateOf(0) }
    var previousWaitingIds by remember { mutableStateOf(waitingList.map { it.id }.toSet()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val internalSnackbarHostState = remember { SnackbarHostState() }
    val snackbarHostState = externalSnackbarHostState ?: internalSnackbarHostState
    val reorderAnimationSpec = remember {
        tween<IntOffset>(durationMillis = WAITING_ITEM_REORDER_DURATION_MS)
    }

    fun performUndo() {
        val action = undoAction ?: return
        when (action) {
            is UndoAction.Move -> viewModel.moveWaitingPlayerToIndex(
                action.player,
                action.fromIndex
            )

            is UndoAction.Remove -> viewModel.insertPlayerIntoWaitingList(
                action.player,
                action.fromIndex
            )

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
            if (result.toString() == "ActionPerformed" && hasUndo) performUndo() else undoAction =
                null
        }
    }

    fun scrollToAndHighlight(playerId: Int, targetIndex: Int) {
        scrollHighlightJob?.cancel()
        scrollHighlightJob = scope.launch {
            delay(300)

            val layoutInfo = listState.layoutInfo

            // Check if an item is fully visible (not just partially peeking at an edge)
            fun isFullyVisible(index: Int): Boolean {
                val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    ?: return false
                return item.offset >= layoutInfo.viewportStartOffset &&
                        item.offset + item.size <= layoutInfo.viewportEndOffset
            }

            // Ensure both the target AND one position above are fully visible,
            // so the swap animation context is shown (e.g. 1st↔2nd both on screen)
            val contextIndex = (targetIndex - 1).coerceAtLeast(0)

            if (!isFullyVisible(contextIndex) || !isFullyVisible(targetIndex)) {
                val visibleIndices = layoutInfo.visibleItemsInfo.map { it.index }
                val firstVisible = visibleIndices.firstOrNull() ?: 0
                val scrollTarget = if (targetIndex <= firstVisible) {
                    // Card is at or above viewport → show context above the target
                    contextIndex
                } else {
                    // Card is below viewport → position it near the bottom
                    val visibleCount = visibleIndices.size.coerceAtLeast(1)
                    (targetIndex - visibleCount + 2).coerceAtLeast(0)
                }
                listState.animateScrollToItem(scrollTarget)
            }

            delay(WAITING_ITEM_REORDER_DURATION_MS.toLong())
            highlightedPlayerId = playerId
            highlightPulse += 1
        }
    }

    // Auto-reset highlight after animation to prevent re-trigger on scroll recycling
    LaunchedEffect(highlightedPlayerId) {
        if (highlightedPlayerId != null) {
            delay(700) // highlight animation (500ms) + safety buffer
            highlightedPlayerId = null
        }
    }

    // Detect newly added players (from inactive list, registration, or undo of removal)
    LaunchedEffect(waitingList) {
        val currentIds = waitingList.map { it.id }.toSet()
        val newIds = currentIds - previousWaitingIds
        previousWaitingIds = currentIds

        if (newIds.size == 1 && scrollHighlightJob?.isActive != true) {
            val newPlayerId = newIds.first()
            val newIndex = waitingList.indexOfFirst { it.id == newPlayerId }
            if (newIndex >= 0) {
                // Wait for any pending scroll (e.g. from inactive→active callback) and composition
                delay(350)
                // Scroll to the new player if not already visible
                val isVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == newIndex }
                if (!isVisible) {
                    listState.animateScrollToItem(newIndex)
                    delay(200)
                }
                highlightedPlayerId = newPlayerId
                highlightPulse += 1
            }
        }
    }

    fun handleRemoveFromWaiting(player: Player) {
        val index = waitingList.indexOfFirst { it.id == player.id }
        undoAction = UndoAction.Remove(player, index)
        viewModel.removePlayerFromWaitingList(player)
        showSnackbar("${player.name} saiu da fila de espera", true)
    }

    // Ensure enough end padding so the scrollbar track (drawn at width−8dp, 4dp wide)
    // doesn't overlap the cards. At least 10dp keeps a small gap.
    val endPadding = maxOf(horizontalPadding, 10.dp)

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .simpleScrollbar(listState)
                .padding(start = horizontalPadding, end = endPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            if (waitingList.isEmpty()) {
                item(key = "empty_active") {
                    Card(
                        modifier = Modifier
                            .animateItemPlacement()
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = "Nenhum jogador na fila de espera",
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
                itemsIndexed(
                    waitingList,
                    key = { _, player -> "active_${player.id}" }) { index, player ->
                    WaitingListPlayerItem(
                        modifier = Modifier.animateItemPlacement(reorderAnimationSpec),
                        index = index,
                        isFirst = index == 0,
                        isLast = index == waitingList.lastIndex,
                        player = player,
                        showElo = showElo,
                        highlightPulse = if (highlightedPlayerId == player.id) highlightPulse else 0,
                        onMoveUp = {
                            if (index > 0) {
                                viewModel.moveWaitingPlayerToIndex(player, index - 1)
                                scrollToAndHighlight(player.id, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (index < waitingList.lastIndex) {
                                viewModel.moveWaitingPlayerToIndex(player, index + 1)
                                scrollToAndHighlight(player.id, index + 1)
                            }
                        },
                        onMoveToBeginning = {
                            val oldIndex = waitingList.indexOfFirst { it.id == player.id }
                            viewModel.movePlayerToBeginning(player)
                            scrollToAndHighlight(player.id, 0)
                            undoAction =
                                UndoAction.Move(player, oldIndex, 0, WaitingSection.ACTIVE)
                            showSnackbar("${player.name} foi para o começo da fila", true)
                        },
                        onMoveToEnd = {
                            val oldIndex = waitingList.indexOfFirst { it.id == player.id }
                            viewModel.movePlayerToEnd(player)
                            scrollToAndHighlight(player.id, waitingList.size - 1)
                            undoAction = UndoAction.Move(
                                player,
                                oldIndex,
                                waitingList.size - 1,
                                WaitingSection.ACTIVE
                            )
                            showSnackbar("${player.name} foi para o final da fila", true)
                        },
                        onRemove = {
                            handleRemoveFromWaiting(player)
                        }
                    )
                }
            }

            item(key = "inactive_header") {
                Column(
                    modifier = Modifier
                        .animateItemPlacement()
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ausentes (${absentPlayers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (absentPlayers.isEmpty()) {
                item(key = "inactive_empty") {
                    Text(
                        text = "Todos os jogadores cadastrados estão presentes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                itemsIndexed(
                    absentPlayers,
                    key = { _, player -> "inactive_${player.id}" }) { _, player ->
                    InactivePlayerItem(
                        modifier = Modifier.animateItemPlacement(),
                        player = player,
                        showElo = showElo,
                        onMoveToBeginning = {
                            val targetIndex = 0
                            undoAction = UndoAction.Add(player, targetIndex)
                            viewModel.insertPlayerIntoWaitingList(player, targetIndex)
                            showSnackbar("${player.name} entrou no começo da fila", true)
                            scope.launch { delay(100); listState.animateScrollToItem(0) }
                        },
                        onMoveToEnd = {
                            val targetIndex = waitingList.size
                            undoAction = UndoAction.Add(player, targetIndex)
                            viewModel.insertPlayerIntoWaitingList(player, targetIndex)
                            showSnackbar("${player.name} entrou no final da fila", true)
                            scope.launch {
                                delay(100)
                                if (waitingList.isNotEmpty()) listState.animateScrollToItem(
                                    waitingList.size - 1
                                )
                            }
                        }
                    )
                }
            }
        } // end LazyColumn

        if (externalSnackbarHostState == null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    } // end Box
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WaitingListBottomSheet(
    viewModel: VoleiViewModel,
    waitingList: List<Player>,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>,
    showElo: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = "Na espera (${waitingList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            WaitingListContent(
                viewModel = viewModel,
                waitingList = waitingList,
                presentPlayerIds = presentPlayerIds,
                allPlayers = allPlayers,
                showElo = showElo,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            )
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WaitingListPlayerItem(
    modifier: Modifier = Modifier,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    player: Player,
    showElo: Boolean,
    highlightPulse: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveToBeginning: () -> Unit,
    onMoveToEnd: () -> Unit,
    onRemove: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    val highlightAlpha = remember { Animatable(0f) }

    LaunchedEffect(highlightPulse) {
        if (highlightPulse <= 0) return@LaunchedEffect
        highlightAlpha.snapTo(0.22f)
        highlightAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 120.dp)
                    .heightIn(min = 60.dp)
                    .padding(12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
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
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                player.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (player.isPriority) {
                                Spacer(Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Prioridade",
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (showElo) {
                            Text(
                                EloCalculator.formatElo(player.elo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (highlightAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Color.White.copy(alpha = highlightAlpha.value),
                        shape = MaterialTheme.shapes.medium
                    )
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Começo da fila") },
                leadingIcon = { Icon(Icons.Default.VerticalAlignTop, contentDescription = null) },
                onClick = {
                    onMoveToBeginning()
                    showMenu = false
                },
                enabled = !isFirst
            )
            DropdownMenuItem(
                text = { Text("Subir um") },
                leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) },
                onClick = {
                    onMoveUp()
                    showMenu = false
                },
                enabled = !isFirst
            )
            DropdownMenuItem(
                text = { Text("Descer um") },
                leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                onClick = {
                    onMoveDown()
                    showMenu = false
                },
                enabled = !isLast
            )
            DropdownMenuItem(
                text = { Text("Final da fila") },
                leadingIcon = {
                    Icon(
                        Icons.Default.VerticalAlignBottom,
                        contentDescription = null
                    )
                },
                onClick = {
                    onMoveToEnd()
                    showMenu = false
                },
                enabled = !isLast
            )
            DropdownMenuItem(
                text = { Text("Remover da fila", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    onRemove()
                    showMenu = false
                }
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
    onMoveToBeginning: () -> Unit,
    onMoveToEnd: () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                contentAlignment = Alignment.CenterStart
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .padding(start = 16.dp, end = 12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMenu = true
                            },
                            onTap = {
                                showMenu = true
                            }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isPriority) {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (showElo) {
                        Text(
                            EloCalculator.formatElo(player.elo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.PersonAddAlt1,
                    contentDescription = "Adicionar à fila",
                    modifier = Modifier
                        .size(20.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showMenu = true
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            )
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Começo da fila") },
                leadingIcon = { Icon(Icons.Default.VerticalAlignTop, contentDescription = null) },
                onClick = {
                    onMoveToBeginning()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Final da fila") },
                leadingIcon = {
                    Icon(
                        Icons.Default.VerticalAlignBottom,
                        contentDescription = null
                    )
                },
                onClick = {
                    onMoveToEnd()
                    showMenu = false
                }
            )
        }
    }
}

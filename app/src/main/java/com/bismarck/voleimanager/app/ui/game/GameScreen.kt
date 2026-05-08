package com.bismarck.voleimanager.app.ui.game

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.ManualSetupScreen
import com.bismarck.voleimanager.app.ui.components.EditPlayerDialog
import com.bismarck.voleimanager.app.ui.components.SubstitutionDialog
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.EloCalculator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import android.app.Activity
import com.bismarck.voleimanager.app.ui.getDisplayGroupName

@Composable
fun GameScreenContent(
    viewModel: VoleiViewModel,
    selectedGroup: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showToll: Boolean,
    showScore: Boolean,
    isSetupMode: Boolean,
    onSetupModeChange: (Boolean) -> Unit,
    onDeleteRequest: (Player) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val sortedPlayers by viewModel.sortedPlayersForPresence.collectAsState()
    val gamesPlayedMap by viewModel.gamesPlayedTodayMap.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val waitingList by viewModel.waitingList.collectAsState()
    val presentIds by viewModel.presentPlayerIds.collectAsState()
    val hasPrev by viewModel.hasPreviousMatch.collectAsState()
    val config by viewModel.currentGroupConfig.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val owner by viewModel.streakOwner.collectAsState()
    val winners by viewModel.lastWinners.collectAsState()

    var showCancel by remember { mutableStateOf(false) }
    var subOut by remember { mutableStateOf<Player?>(null) }
    var editP by remember { mutableStateOf<Player?>(null) }
    var confirmWinTeam by remember { mutableStateOf<String?>(null) }

    if (showCancel) AlertDialog(
        onDismissRequest = { showCancel = false },
        title = { Text(stringResource(R.string.cancel_match)) },
        text = { Text(stringResource(R.string.progress_lost)) },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    viewModel.cancelGame()
                    showCancel = false
                }) { Text(stringResource(R.string.yes)) }
        },
        dismissButton = {
            TextButton(onClick = { showCancel = false }) {
                Text(
                    stringResource(R.string.no),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        })

    subOut?.let { p ->
        SubstitutionDialog(
            p,
            waitingList,
            teamA,
            teamB,
            { subOut = null },
            {
                viewModel.substitutePlayer(p, it)
                subOut = null
            })
    }
    editP?.let { p ->
        EditPlayerDialog(
            p,
            { editP = null },
            { name, prio ->
                viewModel.editPlayer(p, name, prio)
                editP = null
            })
    }

    confirmWinTeam?.let { team ->
        AlertDialog(
            onDismissRequest = { confirmWinTeam = null },
            title = { Text(stringResource(R.string.victorious_team, team)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishGame(team)
                        confirmWinTeam = null
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmWinTeam = null }) {
                    Text(
                        stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    val presentPlayers =
        remember(sortedPlayers, presentIds) { sortedPlayers.filter { presentIds.contains(it.id) } }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isSetupMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                    animationSpec = tween(500)
                )
            },
            label = "SetupModeAnim"
        ) { inSetup ->
            if (inSetup) {
                ManualSetupScreen(
                    presentPlayers,
                    showElo,
                    { tA, tB, b, teamSize ->
                        viewModel.updateConfig(
                            teamSize,
                            config.victoryLimit,
                            config.priorityEnabled,
                            config.scoreEnabled
                        )
                        viewModel.startManualGame(tA, tB, b)
                        onSetupModeChange(false)
                    },
                    { onSetupModeChange(false) }
                )
            } else {
                AnimatedContent(
                    targetState = teamA.isNotEmpty() || teamB.isNotEmpty(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith fadeOut(
                            animationSpec = tween(
                                150
                            )
                        )
                    },
                    label = "GameActiveAnim"
                ) { active ->
                    if (active) {
                        ActiveGameView(
                            viewModel,
                            teamA,
                            teamB,
                            waitingList,
                            owner,
                            streak,
                            isDarkTheme,
                            showElo,
                            showScore,
                            { showCancel = true },
                            { subOut = it },
                            { confirmWinTeam = it },
                            presentIds,
                            sortedPlayers
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 80.dp,
                                    top = 0.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    EmptyStateCard(
                                        presentIds.size,
                                        selectedGroup,
                                        config.teamSize,
                                        { onSetupModeChange(true) },
                                        {
                                            if (presentIds.size >= config.teamSize * 2) viewModel.startNewAutomaticGame(
                                                sortedPlayers,
                                                config.teamSize
                                            )
                                        },
                                        hasPrev,
                                        { viewModel.startNextRound() },
                                        winners,
                                        owner,
                                        streak,
                                        config.victoryLimit,
                                        isDarkTheme,
                                        onShowSnackbar = onShowSnackbar,
                                        onClearRecent = { viewModel.clearRecentGameData() }
                                    )
                                }

                                if (sortedPlayers.isEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.to_start_add_players),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp)
                                        )
                                    }
                                } else {
                                    item {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            Arrangement.SpaceBetween,
                                            Alignment.CenterVertically
                                        ) {
                                            Text(
                                                stringResource(R.string.players_word),
                                                fontWeight = FontWeight.Bold
                                            )
                                            val all =
                                                sortedPlayers.all { presentIds.contains(it.id) }
                                            TextButton(
                                                onClick = {
                                                    viewModel.setAllPlayersPresence(
                                                        sortedPlayers,
                                                        !all
                                                    )
                                                }) {
                                                Text(
                                                    if (all) stringResource(R.string.uncheck_all) else stringResource(
                                                        R.string.check_all
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    items(sortedPlayers) { p ->
                                        PlayerCard(
                                            p,
                                            presentIds.contains(p.id),
                                            gamesPlayedMap[p.id],
                                            targetDate,
                                            showElo,
                                            showToll,
                                            { viewModel.togglePlayerPresence(p) },
                                            { onDeleteRequest(p) },
                                            { editP = p })
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .width(IntrinsicSize.Max)
                                    .padding(start = 22.dp, end = 22.dp, bottom = 8.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shadowElevation = 4.dp
                            ) {
                                val selCount = presentIds.size
                                val totalCount = sortedPlayers.size
                                val text = if (selCount == 0) {
                                    stringResource(
                                        R.string.none_selected,
                                        totalCount,
                                        if (totalCount > 1) stringResource(R.string.group_s_ies) else stringResource(R.string.group_s_y)
                                    )
                                } else {
                                    stringResource(
                                        R.string.selected,
                                        selCount,
                                        if (selCount > 1) stringResource(R.string.group_s) else "",
                                        totalCount,
                                        if (totalCount > 1) stringResource(R.string.group_s_ies) else stringResource(R.string.group_s_y)
                                    )
                                }
                                Text(
                                    text = if (totalCount == 0) stringResource(R.string.no_entry) else text,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameView(
    viewModel: VoleiViewModel,
    teamA: List<Player>,
    teamB: List<Player>,
    waitingList: List<Player>,
    streakOwner: String?,
    currentStreak: Int,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean,
    onCancelRequest: () -> Unit,
    onSubRequest: (Player) -> Unit,
    onWinRequest: (String) -> Unit,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>
) {
    val waitingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showWaitingListSheet by remember { mutableStateOf(false) }
    var waitingPreviewDragProgress by remember { mutableFloatStateOf(0f) }
    val teamAStreak = if (streakOwner == "A") currentStreak else 0
    val teamBStreak = if (streakOwner == "B") currentStreak else 0

    // Closing crossfade: only computed once the sheet has settled into Expanded.
    // During opening, the phantom overlay handles the visual fade-in instead.
    val sheetFadeProgress = if (showWaitingListSheet) {
        val sheetSettled = waitingSheetState.currentValue == SheetValue.Expanded
        if (!sheetSettled) {
            0f
        } else {
            val screenHeightPx = with(LocalDensity.current) {
                LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
            val offset = try {
                waitingSheetState.requireOffset()
            } catch (_: Exception) {
                0f
            }
            val sheetTopFraction = (offset / screenHeightPx).coerceIn(0f, 1f)
            val fadeStart = 0.80f
            if (sheetTopFraction >= fadeStart) {
                ((sheetTopFraction - fadeStart) / (1f - fadeStart)).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    } else {
        0f
    }

    // Preview: hidden during opening (0), crossfades in during closing
    val waitingPreviewAlpha = if (showWaitingListSheet) {
        sheetFadeProgress
    } else {
        1f - waitingPreviewDragProgress.coerceIn(0f, 1f)
    }

    // Sheet: fully opaque during opening, crossfades out during closing
    val sheetContentAlpha = 1f - sheetFadeProgress

    fun openWaitingSheet() {
        waitingPreviewDragProgress = 0f
        showWaitingListSheet = true
    }

    fun closeWaitingSheet() {
        showWaitingListSheet = false
        waitingPreviewDragProgress = 0f
    }

    val scoreA by viewModel.scoreA.collectAsState()
    val scoreB by viewModel.scoreB.collectAsState()

    val cardColorA = MaterialTheme.colorScheme.primaryContainer
    val btnColorA = MaterialTheme.colorScheme.primary
    val btnTextColorA = MaterialTheme.colorScheme.onPrimary

    val cardColorB = LocalExtendedColors.current.anotherPrime.colorContainer
    val btnColorB = LocalExtendedColors.current.anotherPrime.color
    val btnTextColorB = LocalExtendedColors.current.anotherPrime.onColor

    val defaultStreakColor = Color(0xFFFF6F00)
    val yellowStreakColor = Color(0xFFFFD600)
    val streakColorA = if (isDarkTheme) yellowStreakColor else defaultStreakColor
    val streakColorB = if (isDarkTheme) yellowStreakColor else defaultStreakColor

    val teamsSwapped by viewModel.teamsSwapped.collectAsState()

    // Display-order slots: first = top/left, second = bottom/right
    val firstName =
        if (teamsSwapped) stringResource(R.string.team_b) else stringResource(R.string.team_a)
    val firstPlayers = if (teamsSwapped) teamB else teamA
    val firstCardColor = if (teamsSwapped) cardColorB else cardColorA
    val firstBtnColor = if (teamsSwapped) btnColorB else btnColorA
    val firstBtnTextColor = if (teamsSwapped) btnTextColorB else btnTextColorA
    val firstStreakColor = if (teamsSwapped) streakColorB else streakColorA
    val firstStreak = if (teamsSwapped) teamBStreak else teamAStreak
    val firstScore = if (teamsSwapped) scoreB else scoreA
    val firstOnIncrement: () -> Unit = if (teamsSwapped) {
        { viewModel.incrementScoreB() }
    } else {
        { viewModel.incrementScoreA() }
    }
    val firstOnDecrement: () -> Unit = if (teamsSwapped) {
        { viewModel.decrementScoreB() }
    } else {
        { viewModel.decrementScoreA() }
    }
    val firstWinId = if (teamsSwapped) "B" else "A"

    val secondName =
        if (teamsSwapped) stringResource(R.string.team_a) else stringResource(R.string.team_b)
    val secondPlayers = if (teamsSwapped) teamA else teamB
    val secondCardColor = if (teamsSwapped) cardColorA else cardColorB
    val secondBtnColor = if (teamsSwapped) btnColorA else btnColorB
    val secondBtnTextColor = if (teamsSwapped) btnTextColorA else btnTextColorB
    val secondStreakColor = if (teamsSwapped) streakColorA else streakColorB
    val secondStreak = if (teamsSwapped) teamAStreak else teamBStreak
    val secondScore = if (teamsSwapped) scoreA else scoreB
    val secondOnIncrement: () -> Unit = if (teamsSwapped) {
        { viewModel.incrementScoreA() }
    } else {
        { viewModel.incrementScoreB() }
    }
    val secondOnDecrement: () -> Unit = if (teamsSwapped) {
        { viewModel.decrementScoreA() }
    } else {
        { viewModel.decrementScoreB() }
    }
    val secondWinId = if (teamsSwapped) "A" else "B"

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val view = LocalView.current
    val navBarColor = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()
    val transparentColor = Color.Transparent.toArgb()

    if (!view.isInEditMode) {
        // ✅ MUDANÇA AQUI: Adicionamos o 'isDarkTheme' e o 'navBarColor' como chaves!
        // Agora, se o usuário trocar o tema, o Compose destrói o efeito antigo e roda o novo com a cor correta.
        DisposableEffect(isLandscape, isDarkTheme, navBarColor) {
            val window = (view.context as Activity).window

            // Se for retrato (celular em pé), a base da tela é SEMPRE o seu Surface.
            window.navigationBarColor = if (!isLandscape) navBarColor else transparentColor

            onDispose {
                // Quando o jogo é cancelado ou finalizado, a barra volta ao normal
                window.navigationBarColor = transparentColor
            }
        }
    }

    // Dismiss bottom sheet when rotating to landscape (inline list replaces it)
    LaunchedEffect(isLandscape) {
        if (isLandscape) showWaitingListSheet = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLandscape) {
                val landscapeSnackbarHostState = remember { SnackbarHostState() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.75f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 250.dp)
                                ) {
                                    ActiveTeamCard(
                                        firstName,
                                        firstPlayers,
                                        firstCardColor,
                                        firstBtnColor,
                                        firstBtnTextColor,
                                        firstStreakColor,
                                        firstStreak,
                                        showElo,
                                        score = firstScore,
                                        showScore = showScore,
                                        onIncrementScore = firstOnIncrement,
                                        onDecrementScore = firstOnDecrement,
                                        onPlayerClick = onSubRequest
                                    ) { onWinRequest(firstWinId) }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable { viewModel.toggleTeamsSwapped() },
                                    contentAlignment = Alignment.Center
                                ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 250.dp)
                                ) {
                                    ActiveTeamCard(
                                        secondName,
                                        secondPlayers,
                                        secondCardColor,
                                        secondBtnColor,
                                        secondBtnTextColor,
                                        secondStreakColor,
                                        secondStreak,
                                        showElo,
                                        score = secondScore,
                                        showScore = showScore,
                                        onIncrementScore = secondOnIncrement,
                                        onDecrementScore = secondOnDecrement,
                                        onPlayerClick = onSubRequest
                                    ) { onWinRequest(secondWinId) }
                                }

                            }
                            TextButton(
                                onClick = onCancelRequest,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.cancel_match_action),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleSmall,
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(
                            modifier = Modifier
                                .weight(0.25f)
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = stringResource(R.string.waiting_list, waitingList.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            WaitingListContent(
                                viewModel = viewModel,
                                waitingList = waitingList,
                                presentPlayerIds = presentPlayerIds,
                                allPlayers = allPlayers,
                                showElo = showElo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalPadding = 4.dp,
                                externalSnackbarHostState = landscapeSnackbarHostState
                            )
                        }
                    } // end Row

                    SnackbarHost(
                        hostState = landscapeSnackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                } // end Box
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp)
                    ) {
                        ActiveTeamCard(
                            firstName,
                            firstPlayers,
                            firstCardColor,
                            firstBtnColor,
                            firstBtnTextColor,
                            firstStreakColor,
                            firstStreak,
                            showElo,
                            score = firstScore,
                            showScore = showScore,
                            onIncrementScore = firstOnIncrement,
                            onDecrementScore = firstOnDecrement,
                            onPlayerClick = onSubRequest
                        ) { onWinRequest(firstWinId) }
                    }
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(50.dp)
                            .clickable { viewModel.toggleTeamsSwapped() },
                        contentAlignment = Alignment.Center
                    ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp)
                    ) {
                        ActiveTeamCard(
                            secondName,
                            secondPlayers,
                            secondCardColor,
                            secondBtnColor,
                            secondBtnTextColor,
                            secondStreakColor,
                            secondStreak,
                            showElo,
                            score = secondScore,
                            showScore = showScore,
                            onIncrementScore = secondOnIncrement,
                            onDecrementScore = secondOnDecrement,
                            onPlayerClick = onSubRequest
                        ) { onWinRequest(secondWinId) }
                    }

                    TextButton(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.cancel_match_action),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(waitingPreviewAlpha),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column {
                        WaitingListPreviewHeader(
                            waitingCount = waitingList.size,
                            onOpen = ::openWaitingSheet,
                            onDragProgress = { waitingPreviewDragProgress = it },
                            onDragRelease = { shouldOpen ->
                                if (shouldOpen) {
                                    openWaitingSheet()
                                } else {
                                    waitingPreviewDragProgress = 0f
                                }
                            },
                            interactionEnabled = !showWaitingListSheet,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .heightIn(min = 60.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            if (waitingList.isEmpty()) {
                                item(key = "empty_active") {
                                    Card(
                                        modifier = Modifier
                                            .animateItemPlacement()
                                            .fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        onClick = ::openWaitingSheet
                                    ) {
                                        Text(
                                            text = stringResource(R.string.no_player_waiting),
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
                                itemsIndexed(waitingList) { i, p ->
                                    WaitingPlayerCard(
                                        i + 1,
                                        p,
                                        showElo,
                                        onClick = ::openWaitingSheet
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Phantom sheet overlay during drag-to-open (portrait only).
        // Fades in and rises from the bottom in sync with the preview fading out.
        if (!isLandscape && !showWaitingListSheet && waitingPreviewDragProgress > 0f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .alpha(waitingPreviewDragProgress)
                    .offset(y = 48.dp * (1f - waitingPreviewDragProgress)),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle()
                    Text(
                        text = stringResource(R.string.waiting_list, waitingList.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    Spacer(Modifier.height(47.dp))
                }
            }
        }
    } // end Box

    // Bottom Sheet for waiting list management (portrait only)
    if (!isLandscape && showWaitingListSheet) {
        WaitingListBottomSheet(
            viewModel = viewModel,
            waitingList = waitingList,
            presentPlayerIds = presentPlayerIds,
            allPlayers = allPlayers,
            showElo = showElo,
            sheetState = waitingSheetState,
            contentAlpha = sheetContentAlpha,
            onDismiss = ::closeWaitingSheet
        )
    }
}

@Composable
private fun WaitingListPreviewHeader(
    waitingCount: Int,
    onOpen: () -> Unit,
    onDragProgress: (Float) -> Unit,
    onDragRelease: (Boolean) -> Unit,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dragRangePx = with(LocalDensity.current) { 200.dp.toPx() }
    val openThreshold = 0.35f
    var accumulatedDragPx by remember { mutableFloatStateOf(0f) }

    fun updateProgress(valuePx: Float) {
        val clamped = valuePx.coerceIn(0f, dragRangePx)
        onDragProgress((clamped / dragRangePx).coerceIn(0f, 1f))
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = interactionEnabled,
                onClick = onOpen
            )
            .pointerInput(onOpen, interactionEnabled) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        if (!interactionEnabled) return@detectVerticalDragGestures
                        change.consume()
                        if (dragAmount < 0f || accumulatedDragPx > 0f) {
                            accumulatedDragPx = (accumulatedDragPx - dragAmount).coerceAtLeast(0f)
                            updateProgress(accumulatedDragPx)
                        } else {
                            accumulatedDragPx = 0f
                            onDragProgress(0f)
                        }
                    },
                    onDragEnd = {
                        if (interactionEnabled) {
                            val progress = (accumulatedDragPx / dragRangePx).coerceIn(0f, 1f)
                            onDragRelease(progress >= openThreshold)
                        }
                        accumulatedDragPx = 0f
                        onDragProgress(0f)
                    },
                    onDragCancel = {
                        accumulatedDragPx = 0f
                        onDragProgress(0f)
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.waiting_list, waitingCount),
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveTeamCard(
    name: String,
    players: List<Player>,
    cardColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    streakColor: Color,
    streak: Int,
    showElo: Boolean,
    score: Int,
    showScore: Boolean = true,
    onIncrementScore: () -> Unit,
    onDecrementScore: () -> Unit,
    onPlayerClick: (Player) -> Unit,
    onWin: () -> Unit
) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black
    val dividerColor = contentColor.copy(alpha = 0.2f)
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    if (streak > 0) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "🔥$streak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = streakColor,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
                if (showElo) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "(${EloCalculator.formatElo(avgElo)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }

            // Score Counter
            if (showScore) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(buttonColor.copy(alpha = 0.1f), shape = CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    RepeatingScoreButton(
                        onClick = onDecrementScore,
                        canRepeat = { score > 0 },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = stringResource(R.string.decrease_score),
                            tint = buttonColor
                        )
                    }
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = buttonColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 60.dp)
                            .padding(horizontal = 12.dp)
                    )
                    RepeatingScoreButton(
                        onClick = onIncrementScore,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.increase_score),
                            tint = buttonColor
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = dividerColor)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                players.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 30.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayerClick(p)
                                }
                            )
                    ) {
                        Text(
                            text = if (showElo) "${p.name} (${EloCalculator.formatElo(p.elo)})" else p.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, color = contentColor
                        )
                        if (p.isPriority) {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.priority),
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onWin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(R.string.victory),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = buttonTextColor
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    selectedCount: Int,
    currentGroup: String,
    currentTeamSize: Int,
    onStartManualClick: () -> Unit,
    onStartAutoClick: () -> Unit,
    hasPreviousMatch: Boolean = false,
    onNextRoundClick: () -> Unit = {},
    lastWinners: List<Player> = emptyList(),
    streakOwner: String? = null,
    currentStreak: Int = 0,
    victoryLimit: Int = 3,
    isDarkTheme: Boolean = false,
    onShowSnackbar: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showSecondaryMenu by remember { mutableStateOf(false) }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.clear_game_title)) },
            text = { Text(stringResource(R.string.clear_game_desc)) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onClearRecent()
                        showClearConfirmation = false
                    }) { Text(stringResource(R.string.yes_clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(
                        stringResource(R.string.no),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            })
    }

    val minNeeded = currentTeamSize * 2
    val select_minimum_players = stringResource(R.string.select_minimum_players, minNeeded)
    val select_minimum_players_long = stringResource(R.string.select_minimum_players_long, minNeeded)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasPreviousMatch) {
                val limitReached = currentStreak >= victoryLimit
                if (limitReached) {
                    val kingTextColor = MaterialTheme.colorScheme.tertiary
                    Text(
                        text = stringResource(R.string.limit_reached_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = kingTextColor,
                        textAlign = TextAlign.Center
                    );
                    Spacer(modifier = Modifier.height(4.dp));
                    Text(
                        text = stringResource(R.string.limit_reached_text, currentStreak),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val teamName =
                        if (streakOwner == "A") stringResource(R.string.team_a) else if (streakOwner == "B") stringResource(
                            R.string.team_b
                        ) else stringResource(
                            R.string.winner
                        )
                    val playerNames = lastWinners.joinToString(", ") { it.name }
                    Text(
                        text = stringResource(R.string.team_x_wins, teamName),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "($playerNames)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    stringResource(R.string.selected_group, getDisplayGroupName(currentGroup)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (selectedCount < minNeeded) stringResource(
                        R.string.select_minimum_players,
                        minNeeded
                    ) else stringResource(
                        R.string.click_to_start_game
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedCount < minNeeded) MaterialTheme.colorScheme.error else Color.Unspecified,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Leading Button: Iniciar jogo / Iniciar próximo jogo
                    Button(
                        onClick = {
                            if (hasPreviousMatch) {
                                if (selectedCount >= minNeeded) {
                                    onNextRoundClick()
                                } else {
                                    onShowSnackbar(select_minimum_players)
                                }
                            } else {
                                val canStartAuto = selectedCount >= minNeeded
                                if (canStartAuto) {
                                    onStartAutoClick()
                                } else {
                                    onShowSnackbar(select_minimum_players_long)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(
                            topEnd = 4.dp,
                            topStart = 56.dp,
                            bottomStart = 56.dp,
                            bottomEnd = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasPreviousMatch) stringResource(R.string.start_next_game) else stringResource(
                                R.string.start_game
                            ),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Trailing Button: Dropdown menu
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight()
                    ) {
                        val rotation by animateFloatAsState(
                            targetValue = if (showSecondaryMenu) 180f else 0f,
                            animationSpec = tween(durationMillis = 200),
                            label = "MenuRotation"
                        )
                        val iconOffset by animateDpAsState(
                            targetValue = if (showSecondaryMenu) 0.dp else (-2).dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "MenuIconOffset"
                        )
                        val cornerRadius by animateDpAsState(
                            targetValue = if (showSecondaryMenu) 28.dp else 4.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "MenuCornerRadius"
                        )
                        val trailingColor =
                            if (showSecondaryMenu) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                        val trailingIconColor = MaterialTheme.colorScheme.onPrimary
                        val select_minimum_players = stringResource(R.string.select_minimum_players)

                        Button(
                            onClick = { showSecondaryMenu = !showSecondaryMenu },
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = trailingColor
                            ),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(
                                topStart = cornerRadius,
                                topEnd = 28.dp,
                                bottomEnd = 28.dp,
                                bottomStart = cornerRadius
                            )
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.options_menu),
                                modifier = Modifier
                                    .size(26.dp)
                                    .offset(x = iconOffset)
                                    .rotate(rotation),
                                tint = trailingIconColor
                            )
                        }

                        DropdownMenu(
                            expanded = showSecondaryMenu,
                            onDismissRequest = { showSecondaryMenu = false },
                            offset = DpOffset(0.dp, 4.dp)
                        ) {
                            // Opção: Montar times manualmente
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.manual_teams)) },
                                onClick = {
                                    showSecondaryMenu = false
                                    if (selectedCount >= 4) {
                                        onStartManualClick()
                                    } else {
                                        onShowSnackbar(select_minimum_players)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Groups,
                                        contentDescription = null
                                    )
                                }
                            )

                            // Opção: Limpar jogo atual
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.clear_game)) },
                                onClick = {
                                    showSecondaryMenu = false
                                    showClearConfirmation = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(
    player: Player,
    isPresent: Boolean,
    gamesPlayed: Int?,
    targetDate: String,
    showElo: Boolean,
    showToll: Boolean,
    onTogglePresence: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onTogglePresence,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            border = if (isPresent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() })
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = player.name, fontWeight = FontWeight.Bold)
                        if (player.isPriority) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.priority),
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (showElo) {
                        Text(
                            text = "Elo: ${EloCalculator.formatElo(player.elo)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val actualGames = gamesPlayed ?: 0
                    val today = remember {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(Date())
                    }
                    val hasToll =
                        player.dailyToll > 0 && (player.tollDate == targetDate || player.tollDate == today)

                    val info = if (actualGames == 0 && !hasToll) {
                        stringResource(R.string.no_game)
                    } else {
                        val gamesStr =
                            if (actualGames == 1) stringResource(R.string.one_game) else stringResource(
                                R.string.n_games, actualGames
                            )
                        if (showToll && hasToll) {
                            "$gamesStr (+${player.dailyToll})"
                        } else {
                            gamesStr
                        }
                    }
                    Text(text = info, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = { showMenu = false; onEdit() },
                leadingIcon = { Icon(Icons.Default.Edit, null) })
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaitingPlayerCard(index: Int, player: Player, showElo: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 120.dp)
            .clickable(onClick = onClick)
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
                    .padding(12.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${index}º",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(8.dp))
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
                                contentDescription = stringResource(R.string.priority),
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
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
}

/**
 * A button that fires [onClick] immediately on press (with haptic feedback),
 * then repeats it continuously while the finger is held down.
 * If [canRepeat] is provided and returns false, the repeat loop (and vibration) stops
 * until the user lifts and presses again.
 */
@Composable
fun RepeatingScoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    canRepeat: (() -> Boolean)? = null,
    initialDelayMs: Long = 400L,
    repeatDelayMs: Long = 80L,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentCanRepeat by rememberUpdatedState(canRepeat)
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentOnClick()
            delay(initialDelayMs)
            while (currentCanRepeat?.invoke() != false) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentOnClick()
                delay(repeatDelayMs)
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

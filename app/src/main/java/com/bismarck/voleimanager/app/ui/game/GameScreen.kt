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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.bismarck.voleimanager.app.R
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_BALANCING_MODE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_NAME
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_MIN_PLAYERS
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_TEAM_SIZE
import com.bismarck.voleimanager.app.ui.ManualSetupScreen
import com.bismarck.voleimanager.app.ui.components.EditPlayerDialog
import com.bismarck.voleimanager.app.ui.components.LazyListFastScroller
import com.bismarck.voleimanager.app.ui.components.SubstitutionDialog
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.ui.viewmodel.ManualStreakAdjustmentLog
import com.bismarck.voleimanager.app.ui.viewmodel.ManualSubstitutionLog
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.EloCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import android.app.Activity
import androidx.compose.ui.Modifier
import com.bismarck.voleimanager.app.ui.getDisplayGroupName

@Composable
fun GameScreenContent(
    viewModel: VoleiViewModel,
    selectedGroup: String,
    onSelectedGroupChange: (String) -> Unit,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showToll: Boolean,
    showScore: Boolean,
    isSetupMode: Boolean,
    onSetupModeChange: (Boolean) -> Unit,
    onDeleteRequest: (Player) -> Unit,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit
) {
    val context = LocalContext.current
    val undoLabel = stringResource(R.string.undo)
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
    val manualStreakAdjustments by viewModel.manualStreakAdjustments.collectAsState()
    val manualSubstitutions by viewModel.manualSubstitutions.collectAsState()

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
            { replacement ->
                viewModel.substitutePlayer(p, replacement)
                subOut = null
                val message = context.getString(R.string.substitution_snackbar, p.name, replacement.name)
                onShowSnackbar(
                    message,
                    undoLabel
                ) {
                    viewModel.substitutePlayer(replacement, p)
                }
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
    val recentStreakAdjustments = remember(manualStreakAdjustments, selectedGroup) {
        manualStreakAdjustments
            .filter { it.groupName == selectedGroup }
            .takeLast(6)
            .asReversed()
    }
    val recentSubstitutions = remember(manualSubstitutions, selectedGroup) {
        manualSubstitutions
            .filter { it.groupName == selectedGroup }
            .takeLast(6)
            .asReversed()
    }
    val onboardingStep = config.onboardingStep
    val minimumPlayersNeeded = config.teamSize * 2
    val isOnboardingComplete = onboardingStep >= ONBOARDING_STEP_COMPLETE
    var onboardingGroupName by rememberSaveable {
        mutableStateOf(config.groupName)
    }
    var onboardingGroupNameSource by rememberSaveable { mutableStateOf<String?>(null) }
    var onboardingBalanceMode by rememberSaveable(selectedGroup) {
        mutableStateOf(config.balancingMode)
    }
    var wentThroughBalanceMode by rememberSaveable(selectedGroup) {
        mutableStateOf(false)
    }
    var onboardingTeamSizeSelection: Int? by rememberSaveable(selectedGroup) {
        mutableStateOf(
            if (onboardingStep == ONBOARDING_STEP_TEAM_SIZE) null else config.teamSize.coerceIn(2, 6)
        )
    }
    LaunchedEffect(onboardingStep, config.groupName) {
        if (onboardingStep == ONBOARDING_STEP_GROUP_NAME && onboardingGroupNameSource != config.groupName) {
            onboardingGroupName = config.groupName
            onboardingGroupNameSource = config.groupName
        }
    }

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
                            recentStreakAdjustments,
                            recentSubstitutions,
                            owner,
                            streak,
                            config.victoryLimit,
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
                            val listState = rememberLazyListState()
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 80.dp,
                                    top = 0.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                when (onboardingStep) {
                                    ONBOARDING_STEP_GROUP_NAME -> {
                                        item {
                                            GroupOnboardingNameCard(
                                                groupName = onboardingGroupName,
                                                onGroupNameChange = { newValue ->
                                                    if (newValue.length <= MAX_GROUP_NAME_LENGTH) onboardingGroupName = newValue
                                                },
                                                onContinue = {
                                                    val normalizedName = onboardingGroupName.trim()
                                                        .replace(Regex("\\s+"), " ")
                                                        .take(MAX_GROUP_NAME_LENGTH)
                                                    if (normalizedName.isNotBlank()) {
                                                        onboardingGroupName = normalizedName
                                                        onSelectedGroupChange(normalizedName)
                                                        viewModel.continueCurrentGroupOnboardingWithGroupName(normalizedName)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    ONBOARDING_STEP_BALANCING_MODE -> {
                                        item {
                                            GroupOnboardingBalanceModeCard(
                                                selectedMode = onboardingBalanceMode,
                                                onModeSelected = { onboardingBalanceMode = it },
                                                onBack = { viewModel.returnCurrentGroupOnboardingToGroupNameStep() },
                                                onContinue = {
                                                    wentThroughBalanceMode = true
                                                    viewModel.continueCurrentGroupOnboardingWithBalancingMode(onboardingBalanceMode)
                                                }
                                            )
                                        }
                                    }

                                    ONBOARDING_STEP_TEAM_SIZE -> {
                                        item {
                                            GroupOnboardingTeamSizeCard(
                                                selectedTeamSize = onboardingTeamSizeSelection,
                                                onTeamSizeSelected = { onboardingTeamSizeSelection = it },
                                                showBack = wentThroughBalanceMode,
                                                onBack = { viewModel.returnCurrentGroupOnboardingToBalancingModeStep() },
                                                onContinue = {
                                                    val chosenTeamSize = onboardingTeamSizeSelection
                                                    if (chosenTeamSize != null) {
                                                        viewModel.continueCurrentGroupOnboardingWithTeamSize(chosenTeamSize)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    ONBOARDING_STEP_MIN_PLAYERS -> {
                                        item {
                                            GroupOnboardingMinimumPlayersCard(
                                                minimumPlayers = minimumPlayersNeeded,
                                                currentPlayers = sortedPlayers.size,
                                                onBack = {
                                                    onboardingTeamSizeSelection = config.teamSize.coerceIn(2, 6)
                                                    viewModel.returnCurrentGroupOnboardingToTeamSizeStep()
                                                },
                                                onContinue = { viewModel.completeCurrentGroupOnboarding() }
                                            )
                                        }

                                        if (sortedPlayers.isNotEmpty()) {
                                            item {
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
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
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
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

                                    else -> {
                                        val dynamicFullTeamsInWaitingQueue = if (config.teamSize <= 0) {
                                            0
                                        } else {
                                            val winnerIds = winners.map { it.id }.toSet()
                                            val waitingIds = waitingList.map { it.id }.toSet()
                                            val waitingWithoutWinners =
                                                waitingList.count { !winnerIds.contains(it.id) }
                                            val newlyPresentOutsideWaiting = presentIds.count { id ->
                                                !winnerIds.contains(id) && !waitingIds.contains(id)
                                            }
                                            (waitingWithoutWinners + newlyPresentOutsideWaiting) / config.teamSize
                                        }
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
                                                config.balancingMode,
                                                dynamicFullTeamsInWaitingQueue,
                                                waitingList.size,
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
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 6.dp),
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
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
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
                                }
                            }

                            if (isOnboardingComplete || onboardingStep == ONBOARDING_STEP_MIN_PLAYERS) {
                                LazyListFastScroller(
                                    state = listState,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 4.dp)
                                )

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
                                            if (totalCount > 1) stringResource(R.string.group_s) else ""
                                        )
                                    } else {
                                        stringResource(
                                            R.string.selected,
                                            selCount,
                                            if (selCount > 1) stringResource(R.string.group_s) else "",
                                            totalCount,
                                            if (totalCount > 1) stringResource(R.string.group_s) else ""
                                        )
                                    }
                                    Text(
                                        text = if (totalCount == 0) stringResource(R.string.no_entries) else text,
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
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameView(
    viewModel: VoleiViewModel,
    teamA: List<Player>,
    teamB: List<Player>,
    waitingList: List<Player>,
    streakAdjustments: List<ManualStreakAdjustmentLog>,
    substitutionAdjustments: List<ManualSubstitutionLog>,
    streakOwner: String?,
    currentStreak: Int,
    victoryLimit: Int,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean,
    onCancelRequest: () -> Unit,
    onSubRequest: (Player) -> Unit,
    onWinRequest: (String) -> Unit,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.undo)
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
    val restingMap by viewModel.restingPlayers.collectAsState()
    var streakDialogTeam by remember { mutableStateOf<String?>(null) }
    var streakDraftValue by remember { mutableIntStateOf(0) }
    val maxEditableStreak = (victoryLimit - 1).coerceAtLeast(0)

    fun teamName(teamId: String?): String = when (teamId) {
        "A" -> context.getString(R.string.team_a)
        "B" -> context.getString(R.string.team_b)
        else -> context.getString(R.string.winner)
    }

    fun buildStreakAdjustmentMessage(
        oldOwner: String?,
        oldStreak: Int,
        newOwner: String?,
        newStreak: Int
    ): String {
        return when {
            oldOwner != null && newOwner != null && oldOwner != newOwner -> {
                context.getString(
                    R.string.streak_transferred_snackbar,
                    teamName(oldOwner),
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            newOwner != null && oldOwner == newOwner -> {
                context.getString(
                    R.string.streak_adjusted_same_team_snackbar,
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            oldOwner == null && newOwner != null -> {
                context.getString(
                    R.string.streak_started_snackbar,
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            oldOwner != null && newOwner == null -> {
                context.getString(
                    R.string.streak_cleared_snackbar,
                    teamName(oldOwner),
                    oldStreak,
                    newStreak
                )
            }

            else -> {
                context.getString(
                    R.string.streak_adjusted_same_team_snackbar,
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }
        }
    }

    @Composable
    fun buildStreakHistoryLine(log: ManualStreakAdjustmentLog): String {
        val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
        val teamLabel = when (log.team) {
            "A" -> stringResource(R.string.team_a)
            "B" -> stringResource(R.string.team_b)
            else -> stringResource(R.string.winner)
        }
        val ownerLabel: String = when {
            log.oldOwner != null && log.newOwner != null && log.oldOwner != log.newOwner -> {
                val fromLabel = when (log.oldOwner) {
                    "A" -> stringResource(R.string.team_a)
                    "B" -> stringResource(R.string.team_b)
                    else -> stringResource(R.string.winner)
                }
                val toLabel = when (log.newOwner) {
                    "A" -> stringResource(R.string.team_a)
                    "B" -> stringResource(R.string.team_b)
                    else -> stringResource(R.string.winner)
                }
                stringResource(R.string.streak_history_transferred, fromLabel, toLabel)
            }

            log.newOwner != null && log.oldOwner == log.newOwner -> {
                stringResource(R.string.streak_history_same_team, teamLabel)
            }

            log.oldOwner == null && log.newOwner != null -> {
                stringResource(R.string.streak_history_started, teamLabel)
            }

            log.oldOwner != null && log.newOwner == null -> {
                stringResource(R.string.streak_history_cleared, teamLabel)
            }

            else -> {
                stringResource(R.string.streak_history_same_team, teamLabel)
            }
        }

        return "$timeLabel • $ownerLabel (${log.oldStreak} → ${log.newStreak})"
    }

    fun substitutionLocationLabel(location: String): String = when (location) {
        "A" -> context.getString(R.string.team_a)
        "B" -> context.getString(R.string.team_b)
        "WAIT" -> context.getString(R.string.waiting_list_label)
        else -> context.getString(R.string.waiting_list_label)
    }

    fun buildSubstitutionHistoryLine(log: ManualSubstitutionLog): String {
        val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
        val targetTeamLabel = substitutionLocationLabel(log.targetTeam)
        val sourceLabel = substitutionLocationLabel(log.incomingSource)
        val description = if (log.incomingSource == "WAIT") {
            context.getString(
                R.string.substitution_history_from_waiting,
                log.playerInName,
                targetTeamLabel,
                log.playerOutName
            )
        } else {
            context.getString(
                R.string.substitution_history_team_swap,
                log.playerInName,
                targetTeamLabel,
                sourceLabel,
                log.playerOutName
            )
        }
        return "$timeLabel • $description"
    }

    data class RecentActivityEntry(
        val timestamp: Long,
        val streakLog: ManualStreakAdjustmentLog? = null,
        val substitutionLog: ManualSubstitutionLog? = null
    )

    val recentActivityEntries = remember(streakAdjustments, substitutionAdjustments) {
        (
            streakAdjustments.map { RecentActivityEntry(timestamp = it.timestamp, streakLog = it) } +
                substitutionAdjustments.map {
                    RecentActivityEntry(timestamp = it.timestamp, substitutionLog = it)
                }
            )
            .sortedByDescending { it.timestamp }
            .take(6)
    }

    @Composable
    fun RecentActivityCard(modifier: Modifier = Modifier) {
        if (recentActivityEntries.isEmpty()) return

        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.recent_activity_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                recentActivityEntries.forEachIndexed { index, entry ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.streakLog != null) {
                                Icons.Default.LocalFireDepartment
                            } else {
                                Icons.Default.Groups
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() })
                        )
                        Text(
                            text = entry.streakLog?.let { buildStreakHistoryLine(it) }
                                ?: entry.substitutionLog?.let { buildSubstitutionHistoryLine(it) }
                                ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    fun openStreakDialog(teamId: String) {
        streakDialogTeam = teamId
    }

    fun requestWinConfirmation(teamId: String) {
        val selectedScore = if (teamId == "A") scoreA else scoreB
        val otherScore = if (teamId == "A") scoreB else scoreA
        val bothScoresAreZero = scoreA == 0 && scoreB == 0
        val isSelectedTeamLeading = selectedScore > otherScore

        if (bothScoresAreZero || isSelectedTeamLeading) {
            onWinRequest(teamId)
            return
        }

        scope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.winner_score_validation_snackbar),
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(streakDialogTeam, streakOwner, currentStreak, maxEditableStreak) {
        val selectedTeam = streakDialogTeam ?: return@LaunchedEffect
        streakDraftValue = if (streakOwner == selectedTeam) currentStreak else 0
        streakDraftValue = streakDraftValue.coerceIn(0, maxEditableStreak)
    }

    streakDialogTeam?.let { teamId ->
        val teamName = if (teamId == "A") stringResource(R.string.team_a) else stringResource(R.string.team_b)
        AlertDialog(
            onDismissRequest = { streakDialogTeam = null },
            title = { Text(stringResource(R.string.edit_streak_title, teamName)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.edit_streak_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            RepeatingScoreButton(
                                onClick = {
                                    streakDraftValue = (streakDraftValue - 1).coerceAtLeast(0)
                                },
                                canRepeat = { streakDraftValue > 0 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = stringResource(R.string.decrease_score),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Text(
                            text = streakDraftValue.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp)
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            RepeatingScoreButton(
                                onClick = {
                                    streakDraftValue = (streakDraftValue + 1).coerceAtMost(maxEditableStreak)
                                },
                                canRepeat = { streakDraftValue < maxEditableStreak },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(R.string.increase_score),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val applied = viewModel.setStreakForTeam(
                            team = teamId,
                            streakValue = streakDraftValue.coerceIn(0, maxEditableStreak)
                        )
                        streakDialogTeam = null
                        if (applied != null) {
                            scope.launch {
                                val message = buildStreakAdjustmentMessage(
                                    oldOwner = applied.oldOwner,
                                    oldStreak = applied.oldStreak,
                                    newOwner = applied.newOwner,
                                    newStreak = applied.newStreak
                                )
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = undoLabel,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val undone = viewModel.undoLastManualStreakAdjustment()
                                    if (!undone) {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.streak_undo_unavailable),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { streakDialogTeam = null }) {
                    Text(
                        stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

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
    val firstStreakTeamId = if (teamsSwapped) "B" else "A"

    val secondName =
        if (teamsSwapped) stringResource(R.string.team_a) else stringResource(R.string.team_b)
    val secondPlayers = if (teamsSwapped) teamA else teamB
    val secondCardColor = if (teamsSwapped) cardColorA else cardColorB
    val secondBtnColor = if (teamsSwapped) btnColorA else btnColorB
    val secondBtnTextColor = if (teamsSwapped) btnTextColorA else btnTextColorB
    val secondStreakColor = if (teamsSwapped) streakColorA else streakColorB
    val secondStreak = if (teamsSwapped) teamAStreak else teamBStreak
    val secondStreakTeamId = if (teamsSwapped) "A" else "B"
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
    val useCompactTeamCards = !showScore && firstPlayers.size <= 2 && secondPlayers.size <= 2

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val view = LocalView.current
    val navBarColor = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()
    val transparentColor = Color.Transparent.toArgb()

    if (!view.isInEditMode) {
        DisposableEffect(isLandscape, isDarkTheme, navBarColor) {
            val window = (view.context as Activity).window
            window.navigationBarColor = if (!isLandscape) navBarColor else transparentColor

            onDispose {
                window.navigationBarColor = transparentColor
            }
        }
    }

    LaunchedEffect(isLandscape) {
        if (isLandscape) showWaitingListSheet = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (isLandscape) {
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
                                        .then(
                                            if (useCompactTeamCards) Modifier else Modifier.heightIn(min = 250.dp)
                                        )
                                ) {
                                    ActiveTeamCard(
                                        firstName,
                                        firstPlayers,
                                        firstCardColor,
                                        firstBtnColor,
                                        firstBtnTextColor,
                                        firstStreakColor,
                                        firstStreak,
                                        firstStreakTeamId,
                                        showElo,
                                        score = firstScore,
                                        showScore = showScore,
                                        onIncrementScore = firstOnIncrement,
                                        onDecrementScore = firstOnDecrement,
                                        onStreakLongClick = ::openStreakDialog,
                                        onPlayerClick = onSubRequest
                                    ) { requestWinConfirmation(firstWinId) }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .align(Alignment.CenterVertically)
                                        .clip(CircleShape)
                                        .clickable { viewModel.toggleTeamsSwapped() },
                                    contentAlignment = Alignment.Center
                                ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (useCompactTeamCards) Modifier else Modifier.heightIn(min = 250.dp)
                                        )
                                ) {
                                    ActiveTeamCard(
                                        secondName,
                                        secondPlayers,
                                        secondCardColor,
                                        secondBtnColor,
                                        secondBtnTextColor,
                                        secondStreakColor,
                                        secondStreak,
                                        secondStreakTeamId,
                                        showElo,
                                        score = secondScore,
                                        showScore = showScore,
                                        onIncrementScore = secondOnIncrement,
                                        onDecrementScore = secondOnDecrement,
                                        onStreakLongClick = ::openStreakDialog,
                                        onPlayerClick = onSubRequest
                                    ) { requestWinConfirmation(secondWinId) }
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
                            RecentActivityCard(modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(8.dp))
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
                                externalSnackbarHostState = snackbarHostState
                            )
                        }
                    } // end Row
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
                            .then(
                                if (useCompactTeamCards) Modifier else Modifier.heightIn(min = 250.dp)
                            )
                    ) {
                        ActiveTeamCard(
                            firstName,
                            firstPlayers,
                            firstCardColor,
                            firstBtnColor,
                            firstBtnTextColor,
                            firstStreakColor,
                            firstStreak,
                            firstStreakTeamId,
                            showElo,
                            score = firstScore,
                            showScore = showScore,
                            onIncrementScore = firstOnIncrement,
                            onDecrementScore = firstOnDecrement,
                            onStreakLongClick = ::openStreakDialog,
                            onPlayerClick = onSubRequest
                        ) { requestWinConfirmation(firstWinId) }
                    }
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.toggleTeamsSwapped() },
                        contentAlignment = Alignment.Center
                    ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (useCompactTeamCards) Modifier else Modifier.heightIn(min = 250.dp)
                            )
                    ) {
                        ActiveTeamCard(
                            secondName,
                            secondPlayers,
                            secondCardColor,
                            secondBtnColor,
                            secondBtnTextColor,
                            secondStreakColor,
                            secondStreak,
                            secondStreakTeamId,
                            showElo,
                            score = secondScore,
                            showScore = showScore,
                            onIncrementScore = secondOnIncrement,
                            onDecrementScore = secondOnDecrement,
                            onStreakLongClick = ::openStreakDialog,
                            onPlayerClick = onSubRequest
                        ) { requestWinConfirmation(secondWinId) }
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
                    RecentActivityCard(modifier = Modifier.padding(top = 8.dp))
                    Spacer(Modifier.height(8.dp))
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
                                        restingMap.containsKey(p.id),
                                        onClick = ::openWaitingSheet
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

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
    streakTeamId: String,
    showElo: Boolean,
    score: Int,
    showScore: Boolean = true,
    onIncrementScore: () -> Unit,
    onDecrementScore: () -> Unit,
    onStreakLongClick: (String) -> Unit,
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
                .fillMaxWidth()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(30.dp))
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStreakLongClick(streakTeamId)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(R.string.edit_streak_cd),
                            tint = if (streak > 0) streakColor else contentColor.copy(alpha = 0.38f),
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() })
                        )
                        Text(
                            text = if (streak > 0) streak.toString() else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (streak > 0) streakColor else contentColor.copy(alpha = 0.5f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() }),
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        EloCalculator.formatElo(avgElo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(4.dp))
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
                        canRepeat = { score < 99 },
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
                            .clip(RoundedCornerShape(30.dp))
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayerClick(p)
                                }
                            )
                    ) {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, color = contentColor
                        )
                        if (showElo) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = EloCalculator.formatElo(p.elo),
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
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
private fun GroupOnboardingNameCard(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    var didSetInitialCursor by remember(groupName) { mutableStateOf(false) }
    var textFieldValue by rememberSaveable(groupName, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = groupName,
                selection = TextRange(groupName.length)
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_group_name_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onGroupNameChange(it.text)
                },
                label = { Text(stringResource(R.string.group_name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !didSetInitialCursor) {
                            didSetInitialCursor = true
                            textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
                        }
                    }
            )
            Text(
                text = stringResource(R.string.onboarding_group_name_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onContinue,
                    enabled = groupName.trim().isNotBlank()
                ) {
                    Text(stringResource(R.string.continue_word))
                }
            }
        }
    }
}

@Composable
private fun GroupOnboardingBalanceModeCard(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val modes = remember {
        listOf(
            Triple(
                com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name,
                R.string.mode_rebalance,
                R.string.mode_rebalance_tooltip
            ),
            Triple(
                com.bismarck.voleimanager.app.data.model.BalancingMode.REST.name,
                R.string.mode_rest,
                R.string.mode_rest_tooltip
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.onboarding_balance_mode_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.balance_mode_long_press_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            modes.forEach { (value, labelRes, tooltipRes) ->
                OnboardingBalanceModeRow(
                    label = stringResource(labelRes),
                    tooltip = stringResource(tooltipRes),
                    selected = selectedMode == value,
                    onSelect = { onModeSelected(value) }
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_balance_mode_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Button(onClick = onContinue) {
                    Text(stringResource(R.string.continue_word))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingBalanceModeRow(
    label: String,
    tooltip: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(text = tooltip, style = MaterialTheme.typography.bodySmall)
            }
        },
        state = tooltipState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .combinedClickable(
                    onClick = {
                        tooltipState.dismiss()
                        onSelect()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { tooltipState.show() }
                    }
                )
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    tooltipState.dismiss()
                    onSelect()
                }
            )
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GroupOnboardingTeamSizeCard(
    selectedTeamSize: Int?,
    onTeamSizeSelected: (Int) -> Unit,
    showBack: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val options = remember { (2..6).toList() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_team_size_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { teamSize ->
                    val isSelected = selectedTeamSize == teamSize
                    Card(
                        onClick = { onTeamSizeSelected(teamSize) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(teamSize.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.onboarding_team_size_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                Button(
                    onClick = onContinue,
                    enabled = selectedTeamSize != null
                ) {
                    Text(stringResource(R.string.continue_word))
                }
            }
        }
    }
}

@Composable
private fun GroupOnboardingMinimumPlayersCard(
    minimumPlayers: Int,
    currentPlayers: Int,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val showFieldHints = currentPlayers < minimumPlayers

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_add_players_instruction, minimumPlayers),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (showFieldHints) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    Text(
                        text = stringResource(R.string.onboarding_name_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.onboarding_name_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null)
                    Text(
                        text = stringResource(R.string.onboarding_elo_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.onboarding_elo_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                    Text(
                        text = stringResource(R.string.onboarding_priority_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.onboarding_priority_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Button(
                    onClick = onContinue,
                    enabled = currentPlayers >= minimumPlayers
                ) {
                    Text(stringResource(R.string.continue_word))
                }
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
    balancingMode: String = com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name,
    fullTeamsInWaitingQueue: Int = 0,
    waitingCount: Int = 0,
    isDarkTheme: Boolean = false,
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    onClearRecent: () -> Unit
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showSecondaryMenu by remember { mutableStateOf(false) }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.clear_match_title)) },
            text = { Text(stringResource(R.string.clear_match_desc)) },
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
    val selectMinimumPlayers = stringResource(R.string.select_minimum_players, minNeeded)
    val selectMinimumPlayersLong =
        stringResource(R.string.select_minimum_players_long, minNeeded)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val limitReached = hasPreviousMatch && currentStreak >= victoryLimit
            val kingTextColor = MaterialTheme.colorScheme.tertiary
            val mainLogo = if (isDarkTheme) {
                R.drawable.bola_volei_fundo_escuro
            } else {
                R.drawable.logo_volei_manager
            }
            if (limitReached) {
                Icon(
                    painter = painterResource(R.drawable.coroa_icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = kingTextColor
                )
            } else {
                Icon(
                    painter = painterResource(mainLogo),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (hasPreviousMatch) {
                if (limitReached) {
                    Text(
                        text = stringResource(R.string.limit_reached_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = kingTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp));
                    // Adapta a mensagem dependendo do modo de balanceamento
                    val body = when (BalancingMode.fromStoredValue(balancingMode)) {
                        BalancingMode.REBALANCE -> stringResource(R.string.limit_reached_text, currentStreak)
                        BalancingMode.REST -> {
                            when {
                                selectedCount >= currentTeamSize * 4 -> stringResource(
                                    R.string.limit_reached_text_rest_selected_gte_four_teams,
                                    currentStreak
                                )
                                selectedCount >= currentTeamSize * 3 -> stringResource(
                                    R.string.limit_reached_text_rest_selected_gte_three_teams,
                                    currentStreak
                                )
                                selectedCount == (currentTeamSize * 3) - 1 -> stringResource(
                                    R.string.limit_reached_text_rest_selected_eq_two_teams_plus_one,
                                    currentStreak
                                )
                                selectedCount > currentTeamSize * 2 -> stringResource(
                                    R.string.limit_reached_text_rest_selected_gt_two_teams_plus_one,
                                    currentStreak
                                )
                                selectedCount == currentTeamSize * 2 -> stringResource(
                                    R.string.limit_reached_text_rest_selected_eq_two_teams,
                                    currentStreak
                                )
                                else -> stringResource(
                                    R.string.limit_reached_text_rest_selected_lt_two_teams,
                                    currentStreak,
                                    currentTeamSize * 2
                                )
                            }
                        }
                    }
                    Text(
                        text = body,
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
                        R.string.click_to_start_match
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
                                    onShowSnackbar(selectMinimumPlayers, null, null)
                                }
                            } else {
                                val canStartAuto = selectedCount >= minNeeded
                                if (canStartAuto) {
                                    onStartAutoClick()
                                } else {
                                    onShowSnackbar(selectMinimumPlayersLong, null, null)
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
                            if (hasPreviousMatch) stringResource(R.string.start_next_match)
                            else stringResource(R.string.start_match),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        val select_minimum_4_players =
                            stringResource(R.string.select_minimum_4_players)

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
                                        onShowSnackbar(select_minimum_4_players, null, null)
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
                                text = { Text(stringResource(R.string.clear_match)) },
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
    val cardShape = RoundedCornerShape(12.dp)

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            border = if (isPresent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .combinedClickable(
                        onClick = onTogglePresence,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    )
                    .padding(start = 2.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() })
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = EloCalculator.formatElo(player.elo),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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

                    val gamesStr = if (actualGames == 0) {
                        stringResource(R.string.no_games)
                    } else {
                        if (actualGames == 1) stringResource(R.string.one_game) else stringResource(
                            R.string.x_matches, actualGames
                        )
                    }
                    val info =
                        if (showToll && hasToll) {
                            "$gamesStr (+${player.dailyToll})"
                        } else {
                            gamesStr
                        }
                    Text(text = info,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
fun WaitingPlayerCard(index: Int, player: Player, showElo: Boolean, isResting: Boolean, onClick: () -> Unit)
{    Card(
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
                    "$index.",
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
                        if (isResting) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.text_zzz),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                            )
                        }
                    }
                    if (showElo) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
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

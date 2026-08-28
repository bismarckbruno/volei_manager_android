package com.bismarck.voleimanager.app.ui.game

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_BALANCING_MODE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_NAME
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_TYPE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_MIN_PLAYERS
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_TEAM_SIZE
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.ManualSetupScreen
import com.bismarck.voleimanager.app.ui.components.EditPlayerDialog
import com.bismarck.voleimanager.app.ui.components.GroupTypeOptionRow
import com.bismarck.voleimanager.app.ui.components.AssignedPositionBadge
import com.bismarck.voleimanager.app.ui.components.PlayerNameWithPositionBadges
import com.bismarck.voleimanager.app.ui.components.PlayerPositionBadges
import com.bismarck.voleimanager.app.ui.components.assignedPositionTooltip
import com.bismarck.voleimanager.app.ui.components.RoundedSearchTextField
import com.bismarck.voleimanager.app.ui.components.SubstitutionDialog
import com.bismarck.voleimanager.app.ui.getDisplayGroupName
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.ManualStreakAdjustmentLog
import com.bismarck.voleimanager.app.ui.viewmodel.ManualSubstitutionLog
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.EloCalculator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


@Composable
private fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        if (configuration.locales.isEmpty) Locale.ROOT else configuration.locales[0]
    }
}

private fun orderedActiveTeamPlayers(
    players: List<Player>,
    assignedSlotIndices: Map<Int, Int>?,
    reverseOrder: Boolean
): List<Player> {
    if (assignedSlotIndices.isNullOrEmpty()) return players
    val ordered = players.sortedWith(
        compareBy<Player>({ assignedSlotIndices[it.id] ?: Int.MAX_VALUE }, { it.name.lowercase(Locale.ROOT) })
    )
    return if (reverseOrder) ordered.asReversed() else ordered
}

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
    onShowSnackbar: (String, String?, (() -> Unit)?) -> Unit,
    headerDoubleTapTick: Int = 0
) {
    val resources = LocalResources.current
    val focusManager = LocalFocusManager.current
    val undoLabel = stringResource(R.string.undo)
    val sortedPlayers by viewModel.sortedPlayersForPresence.collectAsState()
    val currentGroupHistory by viewModel.currentGroupHistory.collectAsState()
    val gamesPlayedMap by viewModel.gamesPlayedTodayMap.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val waitingList by viewModel.waitingList.collectAsState()
    val presentIds by viewModel.presentPlayerIds.collectAsState()
    val hasPrev by viewModel.hasPreviousMatch.collectAsState()
    val config by viewModel.currentGroupConfig.collectAsState()
    val assignedPositions by viewModel.assignedPositions.collectAsState()
    val assignedSlotIndices by viewModel.assignedSlotIndices.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val owner by viewModel.streakOwner.collectAsState()
    val winners by viewModel.lastWinners.collectAsState()
    val rebalancedPlayerIds by viewModel.rebalancedPlayerIds.collectAsState()
    val autoSelectedLoserPlayerIds by viewModel.autoSelectedLoserPlayerIds.collectAsState()
    val guaranteedNextMatchPlayerIds by viewModel.guaranteedNextMatchPlayerIds.collectAsState()
    val manualStreakAdjustments by viewModel.manualStreakAdjustments.collectAsState()
    val manualSubstitutions by viewModel.manualSubstitutions.collectAsState()

    var showCancel by remember { mutableStateOf(false) }
    var subOut by remember { mutableStateOf<Player?>(null) }
    var editP by remember { mutableStateOf<Player?>(null) }
    var confirmWinTeam by remember { mutableStateOf<String?>(null) }
    var playerSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var playerSearchQuery by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = playerSearchExpanded) {
        playerSearchQuery = ""
        playerSearchExpanded = false
        focusManager.clearFocus(force = true)
    }
    val visiblePlayers = remember(sortedPlayers, playerSearchQuery) {
        if (playerSearchQuery.isBlank()) {
            sortedPlayers
        } else {
            val query = playerSearchQuery.trim()
            sortedPlayers.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    val historyPlayerIds = remember(currentGroupHistory) {
        currentGroupHistory.asSequence()
            .flatMap { match ->
                sequenceOf(match.teamAIds, match.teamBIds).flatMap { rawIds ->
                    rawIds.split(",").asSequence().mapNotNull { idText -> idText.trim().toIntOrNull() }
                }
            }
            .toSet()
    }
    val historyPlayerNames = remember(currentGroupHistory) {
        fun canonicalName(name: String): String {
            return name.trim().lowercase(Locale.ROOT)
        }
        currentGroupHistory.asSequence()
            .flatMap { match ->
                sequenceOf(match.teamA, match.teamB).flatMap { rawNames ->
                    rawNames.split(",").asSequence()
                        .map { canonicalName(it) }
                        .filter { it.isNotEmpty() }
                }
            }
            .toSet()
    }

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
            config.type.usesPositions,
            config.teamSize,
            assignedPositions,
            assignedSlotIndices,
            { subOut = null },
            { replacement ->
                viewModel.substitutePlayer(p, replacement)
                subOut = null
                val message = resources.getString(R.string.substitution_snackbar, p.name, replacement.name)
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
            player = p,
            usesPositions = config.type.usesPositions,
            onDismiss = { editP = null },
            onConfirm = { name, prio, preferred, secondary ->
                viewModel.editPlayer(p, name, prio, preferred, secondary)
                editP = null
            })
    }

    confirmWinTeam?.let { team ->
        val selectedTeamNames = if (team == "B") {
            teamB.joinToString(", ") { it.name }
        } else {
            teamA.joinToString(", ") { it.name }
        }.ifBlank { "-" }
        AlertDialog(
            onDismissRequest = { confirmWinTeam = null },
            title = { Text(stringResource(R.string.victorious_team, team)) },
            text = {
                Text(
                    text = "$selectedTeamNames.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
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
    var onboardingGroupType by rememberSaveable(selectedGroup) {
        mutableStateOf(config.type)
    }
    var wentThroughBalanceMode by rememberSaveable(selectedGroup) {
        mutableStateOf(false)
    }
    var onboardingTeamSizeSelection: Int? by rememberSaveable(selectedGroup) {
        mutableStateOf(
            if (onboardingStep == ONBOARDING_STEP_TEAM_SIZE) null else config.type.coerceTeamSize(config.teamSize)
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
                    players = presentPlayers,
                    showElo = showElo,
                    groupType = config.type,
                    onConfirm = { tA, tB, b, teamSize ->
                        viewModel.updateConfig(
                            teamSize,
                            config.victoryLimit,
                            config.priorityEnabled,
                            config.scoreEnabled
                        )
                        viewModel.startManualGame(tA, tB, b)
                        onSetupModeChange(false)
                    },
                    onCancel = { onSetupModeChange(false) }
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
                            showToll,
                            showScore,
                            rebalancedPlayerIds,
                            autoSelectedLoserPlayerIds,
                            { showCancel = true },
                            { subOut = it },
                            { confirmWinTeam = it },
                            presentIds,
                            sortedPlayers,
                            headerDoubleTapTick
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

                                    ONBOARDING_STEP_GROUP_TYPE -> {
                                        item {
                                            GroupOnboardingGroupTypeCard(
                                                selectedType = onboardingGroupType,
                                                onTypeSelected = { onboardingGroupType = it },
                                                onBack = { viewModel.returnCurrentGroupOnboardingToGroupNameStep() },
                                                onContinue = {
                                                    viewModel.continueCurrentGroupOnboardingWithGroupType(onboardingGroupType.name)
                                                }
                                            )
                                        }
                                    }

                                    ONBOARDING_STEP_BALANCING_MODE -> {
                                        item {
                                            GroupOnboardingBalanceModeCard(
                                                selectedMode = onboardingBalanceMode,
                                                onModeSelected = { onboardingBalanceMode = it },
                                                onBack = { viewModel.returnCurrentGroupOnboardingToGroupTypeStep() },
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
                                                groupType = config.type,
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
                                                    onboardingTeamSizeSelection = config.type.coerceTeamSize(config.teamSize)
                                                    viewModel.returnCurrentGroupOnboardingToTeamSizeStep()
                                                },
                                                onContinue = { viewModel.completeCurrentGroupOnboarding() }
                                            )
                                        }

                                        if (sortedPlayers.isNotEmpty()) {
                                            item {
                                                PlayerListHeader(
                                                    title = stringResource(R.string.players_word),
                                                    allPlayersSelected = visiblePlayers.isNotEmpty() && visiblePlayers.all { presentIds.contains(it.id) },
                                                    visiblePlayerCount = visiblePlayers.size,
                                                    onToggleAll = { checked -> viewModel.setAllPlayersPresence(visiblePlayers, checked) },
                                                    searchExpanded = playerSearchExpanded,
                                                    searchQuery = playerSearchQuery,
                                                    onSearchExpandedChange = { playerSearchExpanded = it },
                                                    onSearchQueryChange = { playerSearchQuery = it }
                                                )
                                            }
                                            items(visiblePlayers) { p ->
                                                PlayerCard(
                                                    p,
                                                    presentIds.contains(p.id),
                                                    guaranteedNextMatchPlayerIds.contains(p.id),
                                                    gamesPlayedMap[p.id],
                                                    targetDate,
                                                    showElo,
                                                    showToll,
                                                    !historyPlayerIds.contains(p.id) && !historyPlayerNames.contains(p.name.trim().lowercase(Locale.ROOT)),
                                                    config.type.usesPositions,
                                                    { viewModel.togglePlayerPresence(p) },
                                                    { viewModel.toggleGuaranteedNextMatchPlayer(p) },
                                                    { onDeleteRequest(p) },
                                                    { editP = p })
                                            }
                                            if (visiblePlayers.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = stringResource(R.string.no_players),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp)
                                                    )
                                                }
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
                                                presentIds,
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
                                                Spacer(modifier = Modifier.height(8.dp))
                                                PlayerListHeader(
                                                    title = stringResource(R.string.players_word),
                                                    allPlayersSelected = visiblePlayers.isNotEmpty() && visiblePlayers.all { presentIds.contains(it.id) },
                                                    visiblePlayerCount = visiblePlayers.size,
                                                    onToggleAll = { checked -> viewModel.setAllPlayersPresence(visiblePlayers, checked) },
                                                    searchExpanded = playerSearchExpanded,
                                                    searchQuery = playerSearchQuery,
                                                    onSearchExpandedChange = { playerSearchExpanded = it },
                                                    onSearchQueryChange = { playerSearchQuery = it }
                                                )
                                            }
                                            items(visiblePlayers) { p ->
                                                PlayerCard(
                                                    p,
                                                    presentIds.contains(p.id),
                                                    guaranteedNextMatchPlayerIds.contains(p.id),
                                                    gamesPlayedMap[p.id],
                                                    targetDate,
                                                    showElo,
                                                    showToll,
                                                    !historyPlayerIds.contains(p.id) && !historyPlayerNames.contains(p.name.trim().lowercase(Locale.ROOT)),
                                                    config.type.usesPositions,
                                                    { viewModel.togglePlayerPresence(p) },
                                                    { viewModel.toggleGuaranteedNextMatchPlayer(p) },
                                                    { onDeleteRequest(p) },
                                                    { editP = p })
                                            }
                                            if (visiblePlayers.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = stringResource(R.string.no_players),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isOnboardingComplete || onboardingStep == ONBOARDING_STEP_MIN_PLAYERS) {

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .width(IntrinsicSize.Max)
                                        .padding(start = 22.dp, end = 22.dp, bottom = 8.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    val selCount = presentIds.size
                                    val totalCount = sortedPlayers.size
                                    if (totalCount == 0) {
                                        Text(
                                            text = stringResource(R.string.no_entries),
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    } else {
                                        val iconSize = with(LocalDensity.current) {
                                            MaterialTheme.typography.bodyLarge.fontSize.toDp()
                                        }
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = selCount.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Icon(
                                                Icons.Default.Groups,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = totalCount.toString(),
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
    showToll: Boolean,
    showScore: Boolean,
    rebalancedPlayerIds: Set<Int>,
    autoSelectedLoserPlayerIds: Set<Int>,
    onCancelRequest: () -> Unit,
    onSubRequest: (Player) -> Unit,
    onWinRequest: (String) -> Unit,
    presentPlayerIds: Set<Int>,
    allPlayers: List<Player>,
    headerDoubleTapTick: Int = 0
) {
    val resources = LocalResources.current
    val locale = currentLocale()
    val assignedPositions by viewModel.assignedPositions.collectAsState()
    val assignedSlotIndices by viewModel.assignedSlotIndices.collectAsState()
    val compositionIncomplete by viewModel.compositionIncomplete.collectAsState()
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val usesPositions = groupConfig.type.usesPositions
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
            val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat().coerceAtLeast(1f)
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
    val gamesPlayedMap by viewModel.gamesPlayedStrictTodayMap.collectAsState()
    val lastScoringTeamId by viewModel.lastScoringTeam.collectAsState()
    val rotationRequiredForTeamId by viewModel.rotationRequiredForTeam.collectAsState()
    val restingMap by viewModel.restingPlayers.collectAsState()
    val targetDate = remember(locale) {
        SimpleDateFormat("yyyy-MM-dd", locale).format(Date())
    }
    var streakDialogTeam by remember { mutableStateOf<String?>(null) }
    var streakDraftValue by remember { mutableIntStateOf(0) }
    val maxEditableStreak = (victoryLimit - 1).coerceAtLeast(0)

    // Big scoreboard: full-screen, landscape-only, larger scoreboard reachable from the
    // regular game screen without leaving the app or cancelling the match.
    var showBigScoreboard by rememberSaveable { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    DisposableEffect(showBigScoreboard, activity) {
        if (showBigScoreboard) {
            val previousOrientation = activity?.requestedOrientation
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            onDispose {
                activity?.requestedOrientation = previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } else {
            onDispose { }
        }
    }
    BackHandler(enabled = showBigScoreboard) {
        showBigScoreboard = false
    }
    // If "Usar placar" gets disabled while the big scoreboard is open, close it and
    // return to the regular detailed game screen (the big scoreboard requires scoring).
    LaunchedEffect(showScore) {
        if (!showScore) {
            showBigScoreboard = false
        }
    }

    fun teamName(teamId: String?): String = when (teamId) {
        "A" -> resources.getString(R.string.team_a)
        "B" -> resources.getString(R.string.team_b)
        else -> resources.getString(R.string.winner)
    }

    fun buildStreakAdjustmentMessage(
        oldOwner: String?,
        oldStreak: Int,
        newOwner: String?,
        newStreak: Int
    ): String {
        return when {
            oldOwner != null && newOwner != null && oldOwner != newOwner -> {
                resources.getString(
                    R.string.streak_transferred_snackbar,
                    teamName(oldOwner),
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            newOwner != null && oldOwner == newOwner -> {
                resources.getString(
                    R.string.streak_adjusted_same_team_snackbar,
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            oldOwner == null && newOwner != null -> {
                resources.getString(
                    R.string.streak_started_snackbar,
                    teamName(newOwner),
                    oldStreak,
                    newStreak
                )
            }

            oldOwner != null && newOwner == null -> {
                resources.getString(
                    R.string.streak_cleared_snackbar,
                    teamName(oldOwner),
                    oldStreak,
                    newStreak
                )
            }

            else -> {
                resources.getString(
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
        val timeLabel = SimpleDateFormat("HH:mm", locale).format(Date(log.timestamp))
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
        "A" -> resources.getString(R.string.team_a)
        "B" -> resources.getString(R.string.team_b)
        "WAIT" -> resources.getString(R.string.waiting_list_label)
        else -> resources.getString(R.string.waiting_list_label)
    }

    fun buildSubstitutionHistoryLine(log: ManualSubstitutionLog): String {
        val timeLabel = SimpleDateFormat("HH:mm", locale).format(Date(log.timestamp))
        val targetTeamLabel = substitutionLocationLabel(log.targetTeam)
        val sourceLabel = substitutionLocationLabel(log.incomingSource)
        val description = when (log.incomingSource) {
            "WAIT" -> resources.getString(
                R.string.substitution_history_from_waiting,
                log.playerInName,
                targetTeamLabel,
                log.playerOutName
            )
            "BENCH" -> resources.getString(
                R.string.substitution_history_from_team_bench,
                log.playerInName,
                targetTeamLabel,
                log.playerOutName
            )
            else -> resources.getString(
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
                message = resources.getString(R.string.winner_score_validation_snackbar),
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
                    Spacer(modifier = Modifier.height(24.dp))
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
                                modifier = Modifier.size(48.dp)
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
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = stringResource(R.string.increase_score),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
                                            message = resources.getString(R.string.streak_undo_unavailable),
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
    val firstTeamId = if (teamsSwapped) "B" else "A"
    val secondTeamId = if (teamsSwapped) "A" else "B"
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
    val firstWinId = firstTeamId
    val firstStreakTeamId = firstTeamId

    val secondName =
        if (teamsSwapped) stringResource(R.string.team_a) else stringResource(R.string.team_b)
    val secondPlayers = if (teamsSwapped) teamA else teamB
    val secondCardColor = if (teamsSwapped) cardColorA else cardColorB
    val secondBtnColor = if (teamsSwapped) btnColorA else btnColorB
    val secondBtnTextColor = if (teamsSwapped) btnTextColorA else btnTextColorB
    val secondStreakColor = if (teamsSwapped) streakColorA else streakColorB
    val secondStreak = if (teamsSwapped) teamAStreak else teamBStreak
    val secondStreakTeamId = secondTeamId
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
    val secondWinId = secondTeamId
    // These "last point"/"rotation" indicators only make sense when the scoreboard itself
    // is enabled; hide them entirely when "Usar placar" is off.
    val firstShowRotationIndicator = showScore && rotationRequiredForTeamId == firstTeamId
    val firstShowLatestPointBorder = showScore && !firstShowRotationIndicator && lastScoringTeamId == firstTeamId
    val firstScoreTooltip = when {
        firstShowRotationIndicator -> stringResource(R.string.score_rotation_tooltip, firstName)
        firstShowLatestPointBorder -> stringResource(R.string.score_latest_point_tooltip, firstName)
        else -> null
    }

    val secondShowRotationIndicator = showScore && rotationRequiredForTeamId == secondTeamId
    val secondShowLatestPointBorder = showScore && !secondShowRotationIndicator && lastScoringTeamId == secondTeamId
    val secondScoreTooltip = when {
        secondShowRotationIndicator -> stringResource(R.string.score_rotation_tooltip, secondName)
        secondShowLatestPointBorder -> stringResource(R.string.score_latest_point_tooltip, secondName)
        else -> null
    }
    val useCompactTeamCards = !showScore && firstPlayers.size <= 2 && secondPlayers.size <= 2

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val orderedFirstPlayers = remember(firstPlayers, assignedSlotIndices, isLandscape, usesPositions, gamesPlayedMap) {
        if (usesPositions) {
            orderedActiveTeamPlayers(
                players = firstPlayers,
                assignedSlotIndices = assignedSlotIndices,
                reverseOrder = !isLandscape
            )
        } else if (!isLandscape) {
            // Portrait, top card: fewest games played today first.
            firstPlayers.sortedBy { gamesPlayedMap[it.id] ?: 0 }
        } else {
            firstPlayers
        }
    }
    val orderedSecondPlayers = remember(secondPlayers, assignedSlotIndices, isLandscape, usesPositions, gamesPlayedMap) {
        if (usesPositions) {
            orderedActiveTeamPlayers(
                players = secondPlayers,
                assignedSlotIndices = assignedSlotIndices,
                reverseOrder = false
            )
        } else if (!isLandscape) {
            // Portrait, bottom card: most games played today first.
            secondPlayers.sortedByDescending { gamesPlayedMap[it.id] ?: 0 }
        } else {
            secondPlayers
        }
    }

    val navBarColor = MaterialTheme.colorScheme.surfaceContainerLow

    LaunchedEffect(isLandscape) {
        if (isLandscape) showWaitingListSheet = false
    }

    // Portrait: centers the "VS" button roughly at the vertical center of the screen when the
    // screen is entered or rotated back to portrait. Landscape: keeps the players/cards column
    // scrolled to the top so the TeamControlPanel stays in view. No more time-based auto-scroll.
    val portraitScrollState = rememberScrollState()
    val landscapeScrollState = rememberScrollState()
    var portraitViewportHeightPx by remember { mutableFloatStateOf(0f) }
    var portraitViewportTopInRootPx by remember { mutableFloatStateOf(0f) }
    var vsContentPositionPx by remember { mutableFloatStateOf(-1f) }
    var vsHeightPx by remember { mutableFloatStateOf(0f) }

    val portraitVsTargetScrollPx by remember {
        derivedStateOf {
            if (portraitViewportHeightPx <= 0f || vsContentPositionPx < 0f) {
                null
            } else {
                val maxValue = portraitScrollState.maxValue.toFloat().coerceAtLeast(0f)
                (vsContentPositionPx + vsHeightPx / 2f - portraitViewportHeightPx / 2f)
                    .coerceIn(0f, maxValue)
            }
        }
    }

    // Recenter/scroll-to-top on entering the screen and whenever the orientation flips.
    LaunchedEffect(isLandscape) {
        if (isLandscape) {
            landscapeScrollState.scrollTo(0)
        } else {
            val target = snapshotFlow { portraitVsTargetScrollPx }.filterNotNull().first()
            portraitScrollState.scrollTo(target.roundToInt())
        }
    }

    // Double-tap on the app header: quick recenter (portrait), with a small
    // overshoot-and-settle bounce, or a clean slide-to-top (landscape), no bounce.
    val headerTapPortraitAnimationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val headerTapLandscapeAnimationSpec = tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)
    LaunchedEffect(headerDoubleTapTick) {
        if (headerDoubleTapTick <= 0) return@LaunchedEffect
        if (isLandscape) {
            landscapeScrollState.animateScrollTo(0, animationSpec = headerTapLandscapeAnimationSpec)
        } else {
            val target = portraitVsTargetScrollPx?.roundToInt() ?: return@LaunchedEffect
            portraitScrollState.animateScrollTo(target, animationSpec = headerTapPortraitAnimationSpec)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = showBigScoreboard,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "BigScoreboardAnim",
            modifier = Modifier.fillMaxSize()
        ) { big ->
        if (big) {
            BigScoreboardScreen(
                firstName = firstName,
                firstCardColor = firstCardColor,
                firstBtnColor = firstBtnColor,
                firstBtnTextColor = firstBtnTextColor,
                firstStreakColor = firstStreakColor,
                firstStreak = firstStreak,
                firstStreakTeamId = firstStreakTeamId,
                firstScore = firstScore,
                firstShowLatestPointBorder = firstShowLatestPointBorder,
                firstShowRotationIndicator = firstShowRotationIndicator,
                firstScoreTooltip = firstScoreTooltip,
                onFirstIncrement = firstOnIncrement,
                onFirstDecrement = firstOnDecrement,
                onFirstWin = { requestWinConfirmation(firstWinId) },
                secondName = secondName,
                secondCardColor = secondCardColor,
                secondBtnColor = secondBtnColor,
                secondBtnTextColor = secondBtnTextColor,
                secondStreakColor = secondStreakColor,
                secondStreak = secondStreak,
                secondStreakTeamId = secondStreakTeamId,
                secondScore = secondScore,
                secondShowLatestPointBorder = secondShowLatestPointBorder,
                secondShowRotationIndicator = secondShowRotationIndicator,
                secondScoreTooltip = secondScoreTooltip,
                onSecondIncrement = secondOnIncrement,
                onSecondDecrement = secondOnDecrement,
                onSecondWin = { requestWinConfirmation(secondWinId) },
                onStreakLongClick = ::openStreakDialog,
                onSwapTeams = { viewModel.toggleTeamsSwapped() },
                onBack = { showBigScoreboard = false }
            )
            return@AnimatedContent
        }
    Box(modifier = Modifier.fillMaxSize()) {
        // Draws a scrim behind the (transparent, edge-to-edge) system navigation bar in portrait
        // so it visually blends with the screen background instead of setting a deprecated
        // Window.navigationBarColor.
        if (!isLandscape) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(navBarColor)
            )
        }
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
                                .verticalScroll(landscapeScrollState)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (useCompactTeamCards) Modifier else Modifier.heightIn(
                                                min = 250.dp
                                            )
                                        )
                                ) {
                                    ActiveTeamCard(
                                        firstName,
                                        firstTeamId,
                                        orderedFirstPlayers,
                                        firstCardColor,
                                        firstBtnColor,
                                        firstBtnTextColor,
                                        firstStreakColor,
                                        firstStreak,
                                        firstStreakTeamId,
                                        showElo,
                                        showToll,
                                        targetDate,
                                        gamesPlayedMap,
                                        rebalancedPlayerIds,
                                        autoSelectedLoserPlayerIds,
                                        assignedPositions = assignedPositions,
                                        usesPositions = usesPositions,
                                        positionTeamSize = groupConfig.teamSize,
                                        portraitPlayersFirst = true,
                                        score = firstScore,
                                        showScore = showScore,
                                        showLatestPointBorder = firstShowLatestPointBorder,
                                        showRotationIndicator = firstShowRotationIndicator,
                                        scoreIndicatorTooltip = firstScoreTooltip,
                                        onIncrementScore = firstOnIncrement,
                                        onDecrementScore = firstOnDecrement,
                                        onStreakLongClick = ::openStreakDialog,
                                        onPlayerClick = onSubRequest
                                    ) { requestWinConfirmation(firstWinId) }
                                }
                                Spacer(modifier = Modifier.width(4.dp))

                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ){
                                    if (showScore) {
                                        BigScoreboardToggleButton(isBack = false) {
                                            showBigScoreboard = true
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                    } else {
                                        // Compensates for the missing scoreboard toggle
                                        // button (48.dp) + spacer (20.dp) above, so the VS
                                        // swap button keeps the same vertical position.
                                        Spacer(modifier = Modifier.height(68.dp))
                                    }

                                    VsSwapButton(
                                        isLandscape = true
                                    ) { viewModel.toggleTeamsSwapped() }
                                }

                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (useCompactTeamCards) Modifier else Modifier.heightIn(
                                                min = 250.dp
                                            )
                                        )
                                ) {
                                    ActiveTeamCard(
                                        secondName,
                                        secondTeamId,
                                        orderedSecondPlayers,
                                        secondCardColor,
                                        secondBtnColor,
                                        secondBtnTextColor,
                                        secondStreakColor,
                                        secondStreak,
                                        secondStreakTeamId,
                                        showElo,
                                        showToll,
                                        targetDate,
                                        gamesPlayedMap,
                                        rebalancedPlayerIds,
                                        autoSelectedLoserPlayerIds,
                                        assignedPositions = assignedPositions,
                                        usesPositions = usesPositions,
                                        positionTeamSize = groupConfig.teamSize,
                                        portraitPlayersFirst = false,
                                        score = secondScore,
                                        showScore = showScore,
                                        showLatestPointBorder = secondShowLatestPointBorder,
                                        showRotationIndicator = secondShowRotationIndicator,
                                        scoreIndicatorTooltip = secondScoreTooltip,
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
                                    .defaultMinSize(minHeight = 48.dp)
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
                        .onGloballyPositioned { coordinates ->
                            portraitViewportHeightPx = coordinates.size.height.toFloat()
                            portraitViewportTopInRootPx = coordinates.positionInRoot().y
                        }
                        .verticalScroll(portraitScrollState),
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
                            firstTeamId,
                            orderedFirstPlayers,
                            firstCardColor,
                            firstBtnColor,
                            firstBtnTextColor,
                            firstStreakColor,
                            firstStreak,
                            firstStreakTeamId,
                            showElo,
                            showToll,
                            targetDate,
                            gamesPlayedMap,
                            rebalancedPlayerIds,
                            autoSelectedLoserPlayerIds,
                            assignedPositions = assignedPositions,
                            usesPositions = usesPositions,
                            positionTeamSize = groupConfig.teamSize,
                            portraitPlayersFirst = true,
                            score = firstScore,
                            showScore = showScore,
                            showLatestPointBorder = firstShowLatestPointBorder,
                            showRotationIndicator = firstShowRotationIndicator,
                            scoreIndicatorTooltip = firstScoreTooltip,
                            onIncrementScore = firstOnIncrement,
                            onDecrementScore = firstOnDecrement,
                            onStreakLongClick = ::openStreakDialog,
                            onPlayerClick = onSubRequest
                        ) { requestWinConfirmation(firstWinId) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Leading spacer balances the trailing toggle button's width so the
                        // VS button below stays centered on the row regardless of whether
                        // the toggle is shown.
                        Spacer(modifier = Modifier.width(48.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        VsSwapButton(
                            isLandscape = false,
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    val displayedOffsetWithinViewport =
                                        coordinates.positionInRoot().y - portraitViewportTopInRootPx
                                    vsContentPositionPx = displayedOffsetWithinViewport + portraitScrollState.value
                                    vsHeightPx = coordinates.size.height.toFloat()
                                }
                        ) { viewModel.toggleTeamsSwapped() }
                        Spacer(modifier = Modifier.weight(1f))

                        if (showScore) {
                            BigScoreboardToggleButton(isBack = false) {
                                showBigScoreboard = true
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (useCompactTeamCards) Modifier else Modifier.heightIn(min = 250.dp)
                            )
                    ) {
                        ActiveTeamCard(
                            secondName,
                            secondTeamId,
                            orderedSecondPlayers,
                            secondCardColor,
                            secondBtnColor,
                            secondBtnTextColor,
                            secondStreakColor,
                            secondStreak,
                            secondStreakTeamId,
                            showElo,
                            showToll,
                            targetDate,
                            gamesPlayedMap,
                            rebalancedPlayerIds,
                            autoSelectedLoserPlayerIds,
                            assignedPositions = assignedPositions,
                            usesPositions = usesPositions,
                            positionTeamSize = groupConfig.teamSize,
                            portraitPlayersFirst = false,
                            score = secondScore,
                            showScore = showScore,
                            showLatestPointBorder = secondShowLatestPointBorder,
                            showRotationIndicator = secondShowRotationIndicator,
                            scoreIndicatorTooltip = secondScoreTooltip,
                            onIncrementScore = secondOnIncrement,
                            onDecrementScore = secondOnDecrement,
                            onStreakLongClick = ::openStreakDialog,
                            onPlayerClick = onSubRequest
                        ) { requestWinConfirmation(secondWinId) }
                    }
                    TextButton(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .defaultMinSize(minHeight = 48.dp)
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
                                            .animateItem()
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
                                        usesPositions = usesPositions,
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
    } // end AnimatedContent(showBigScoreboard) branch

        // Shown for both the regular detailed game screen and the big scoreboard screen,
        // so validation messages (e.g. trying to declare a winner with fewer points) are
        // visible regardless of which one is currently open.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    } // end outer Box
}

@Composable
private fun PlayerListHeader(
    title: String,
    allPlayersSelected: Boolean,
    visiblePlayerCount: Int,
    onToggleAll: (Boolean) -> Unit,
    searchExpanded: Boolean,
    searchQuery: String,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            hasFocus = false
            focusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (searchExpanded) {
                RoundedSearchTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_player), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            onSearchQueryChange("")
                            onSearchExpandedChange(false)
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    focusRequester = focusRequester,
                    onFocusChanged = { state ->
                        if (state.isFocused) {
                            hasFocus = true
                        } else if (hasFocus) {
                            hasFocus = false
                            onSearchQueryChange("")
                            onSearchExpandedChange(false)
                        }
                    }
                )
            } else {
                IconButton(
                    onClick = { onSearchExpandedChange(true) },
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_player),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = !searchExpanded,
                enter = fadeIn(animationSpec = tween(100)),
                exit = fadeOut(animationSpec = tween(5)),
                modifier = Modifier.padding(start = 54.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        TextButton(
            onClick = { onToggleAll(!allPlayersSelected) },
            modifier = Modifier.height(48.dp),
            enabled = visiblePlayerCount > 0
        ) {
            Text(
                "${if (allPlayersSelected) stringResource(R.string.uncheck_all) else stringResource(R.string.check_all)} ($visiblePlayerCount)",
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VsSwapButton(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var rotationTurns by remember { mutableIntStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = rotationTurns * 180f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "vsSwapRotation"
    )

    val arrowColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
    val vsColor = MaterialTheme.colorScheme.onSurface

    val switchTeamsLabel = stringResource(R.string.switch_teams)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = switchTeamsLabel) {
                rotationTurns++
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(
                if (isLandscape) R.drawable.yin_yang_horizontal
                else R.drawable.yin_yang_vertical
            ),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .rotate(rotation),
            tint = arrowColor
        )
        Icon(
            painter = painterResource(R.drawable.vs_text),
            contentDescription = "versus",
            modifier = Modifier.size(48.dp),
            tint = vsColor
        )
    }
}

/**
 * Toggle button placed alongside [VsSwapButton] that opens/closes the big scoreboard
 * screen. Shows a scoreboard icon on the regular game screen and an arrow-back icon
 * once inside the big scoreboard. A long press shows an explanatory tooltip, matching
 * the existing pattern used by the average Elo and streak indicators.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BigScoreboardToggleButton(
    isBack: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val tooltipText = if (isBack) {
        stringResource(R.string.big_scoreboard_back_tooltip)
    } else {
        stringResource(R.string.big_scoreboard_tooltip)
    }
    val contentDescription = if (isBack) {
        stringResource(R.string.big_scoreboard_back_cd)
    } else {
        stringResource(R.string.big_scoreboard_cd)
    }
    val tint = MaterialTheme.colorScheme.onSurface

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = tooltipText, style = MaterialTheme.typography.bodySmall)
            }
        },
        state = tooltipState,
        enableUserInput = false
    ) {
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClickLabel = contentDescription,
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.dismiss()
                            tooltipState.show()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Scoreboard,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}

/**
 * Icon shown in the top-left corner of each big scoreboard card indicating whether that
 * team scored the most recent point (optionally also winning back the serve/rotation).
 * Reuses the same [showRotationIndicator]/[showLatestPointBorder] logic already computed
 * for the regular score border indicator. A long press shows the same explanatory
 * tooltip text used by that indicator.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PointIndicatorIcon(
    showRotationIndicator: Boolean,
    showLatestPointBorder: Boolean,
    tooltipText: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (!showRotationIndicator && !showLatestPointBorder) return

    val haptic = LocalHapticFeedback.current
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val drawableRes = if (showRotationIndicator) {
        R.drawable.point_and_rotation_icon
    } else {
        R.drawable.point_icon
    }

    val iconContent: @Composable () -> Unit = {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = tooltipText,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }

    if (tooltipText.isNullOrBlank()) {
        Box(
            modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            contentAlignment = Alignment.Center
        ) { iconContent() }
        return
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = tooltipText, style = MaterialTheme.typography.bodySmall)
            }
        },
        state = tooltipState,
        enableUserInput = false
    ) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.dismiss()
                            tooltipState.show()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
    }
}

/**
 * Full-screen, landscape-only "big scoreboard" view: two large team cards with a giant,
 * far-visible score, reachable from [ActiveGameView] without leaving the game or losing
 * any in-progress state. No average Elo, player lists, app header, or waiting list here.
 */
@Composable
private fun BigScoreboardScreen(
    firstName: String,
    firstCardColor: Color,
    firstBtnColor: Color,
    firstBtnTextColor: Color,
    firstStreakColor: Color,
    firstStreak: Int,
    firstStreakTeamId: String,
    firstScore: Int,
    firstShowLatestPointBorder: Boolean,
    firstShowRotationIndicator: Boolean,
    firstScoreTooltip: String?,
    onFirstIncrement: () -> Unit,
    onFirstDecrement: () -> Unit,
    onFirstWin: () -> Unit,
    secondName: String,
    secondCardColor: Color,
    secondBtnColor: Color,
    secondBtnTextColor: Color,
    secondStreakColor: Color,
    secondStreak: Int,
    secondStreakTeamId: String,
    secondScore: Int,
    secondShowLatestPointBorder: Boolean,
    secondShowRotationIndicator: Boolean,
    secondScoreTooltip: String?,
    onSecondIncrement: () -> Unit,
    onSecondDecrement: () -> Unit,
    onSecondWin: () -> Unit,
    onStreakLongClick: (String) -> Unit,
    onSwapTeams: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 0.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BigScoreCard(
            name = firstName,
            isLeftCard = true,
            cardColor = firstCardColor,
            buttonColor = firstBtnColor,
            buttonTextColor = firstBtnTextColor,
            streakColor = firstStreakColor,
            streak = firstStreak,
            streakTeamId = firstStreakTeamId,
            score = firstScore,
            showLatestPointBorder = firstShowLatestPointBorder,
            showRotationIndicator = firstShowRotationIndicator,
            scoreTooltip = firstScoreTooltip,
            onIncrementScore = onFirstIncrement,
            onDecrementScore = onFirstDecrement,
            onStreakLongClick = onStreakLongClick,
            onWin = onFirstWin,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            BigScoreboardToggleButton(
                isBack = true,
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            VsSwapButton(
                isLandscape = true,
                onClick = onSwapTeams,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        BigScoreCard(
            name = secondName,
            isLeftCard = false,
            cardColor = secondCardColor,
            buttonColor = secondBtnColor,
            buttonTextColor = secondBtnTextColor,
            streakColor = secondStreakColor,
            streak = secondStreak,
            streakTeamId = secondStreakTeamId,
            score = secondScore,
            showLatestPointBorder = secondShowLatestPointBorder,
            showRotationIndicator = secondShowRotationIndicator,
            scoreTooltip = secondScoreTooltip,
            onIncrementScore = onSecondIncrement,
            onDecrementScore = onSecondDecrement,
            onStreakLongClick = onStreakLongClick,
            onWin = onSecondWin,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BigScoreCard(
    name: String,
    isLeftCard: Boolean,
    cardColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    streakColor: Color,
    streak: Int,
    streakTeamId: String,
    score: Int,
    showLatestPointBorder: Boolean,
    showRotationIndicator: Boolean,
    scoreTooltip: String?,
    onIncrementScore: () -> Unit,
    onDecrementScore: () -> Unit,
    onStreakLongClick: (String) -> Unit,
    onWin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black
    val victoryLabel = stringResource(R.string.victory_short)

    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Tap zones covering the whole card. The zone closer to the center of the
            // screen (the "inner" side) decreases the score and is narrower (1/3 of the
            // card width); the outer zone (2/3) increases it. Placed behind the content
            // so the streak chip, point icon and victory button (each individually
            // clickable) still intercept their own taps.
            Row(modifier = Modifier.matchParentSize()) {
                val incrementZone = @Composable {
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .rememberHoldToRepeatModifier(
                                onTrigger = onIncrementScore,
                                canRepeat = { score < 99 }
                            )
                    )
                }
                val decrementZone = @Composable {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .rememberHoldToRepeatModifier(
                                onTrigger = onDecrementScore,
                                canRepeat = { score > 0 }
                            )
                    )
                }
                if (isLeftCard) {
                    incrementZone()
                    decrementZone()
                } else {
                    decrementZone()
                    incrementZone()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: point/rotation icon and streak swap sides depending on which
                // side of the screen this card is on (left card: icon on the left,
                // streak on the right; right card: mirrored).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pointIcon: @Composable () -> Unit = {
                        Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterStart) {
                            PointIndicatorIcon(
                                showRotationIndicator = showRotationIndicator,
                                showLatestPointBorder = showLatestPointBorder,
                                tooltipText = scoreTooltip,
                                tint = contentColor
                            )
                        }
                    }
                    val streakChip: @Composable () -> Unit = {
                        Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterEnd) {
                            StreakIndicator(
                                streak = streak,
                                streakColor = streakColor,
                                contentColor = contentColor,
                                onLongClick = { onStreakLongClick(streakTeamId) }
                            )
                        }
                    }

                    if (isLeftCard) {
                        pointIcon()
                    } else {
                        streakChip()
                    }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = buttonColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    if (isLeftCard) {
                        streakChip()
                    } else {
                        pointIcon()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                ScoreValueIndicator(
                    score = score,
                    textColor = buttonColor,
                    indicatorColor = buttonColor,
                    showLatestPointBorder = showLatestPointBorder,
                    showRotationIndicator = showRotationIndicator,
                    tooltipText = null,
                    circleSize = 220.dp,
                    strokeWidth = 5.dp,
                    textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp)
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            // Purely decorative +/- indicators, mirrored so the "+" sits on the outer
            // edge of the card (away from the center of the screen) and the "-" sits on
            // the inner edge (matching the wider/narrower tap zones above). Centered
            // vertically relative to the whole card, not just the score row.
            val plusMinusStyle = MaterialTheme.typography.displayMedium.copy(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            val plusIndicator: @Composable () -> Unit = {
                Text(
                    text = "+",
                    style = plusMinusStyle,
                    color = contentColor.copy(alpha = 0.4f)
                )
            }
            val minusIndicator: @Composable () -> Unit = {
                Text(
                    text = "\u2212",
                    style = plusMinusStyle,
                    color = contentColor.copy(alpha = 0.4f)
                )
            }
            Box(
                modifier = Modifier
                    .align(if (isLeftCard) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 20.dp)
            ) {
                plusIndicator()
            }
            Box(
                modifier = Modifier
                    .align(if (isLeftCard) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 20.dp)
            ) {
                minusIndicator()
            }

            FilledIconButton(
                onClick = onWin,
                modifier = Modifier
                    .align(if (isLeftCard) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = buttonColor,
                    contentColor = buttonTextColor
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.crown_icon),
                    contentDescription = victoryLabel,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Remembers a press-and-hold gesture modifier that fires [onTrigger] once on a quick
 * tap, or repeatedly (with an initial delay and a shorter delay between repeats) while
 * the touch is held down, matching the existing behavior of [RepeatingScoreButton].
 * Also fires the same discreet haptic feedback used by the +/- buttons on the regular
 * game screen.
 */
@Composable
private fun Modifier.rememberHoldToRepeatModifier(
    onTrigger: () -> Unit,
    canRepeat: () -> Boolean,
    initialDelayMs: Long = 400L,
    repeatDelayMs: Long = 80L
): Modifier {
    val haptic = LocalHapticFeedback.current
    val currentOnTrigger by rememberUpdatedState(onTrigger)
    val currentCanRepeat by rememberUpdatedState(canRepeat)

    return this.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                var holdFired = false
                coroutineScope {
                    val job = launch {
                        delay(initialDelayMs)
                        holdFired = true
                        while (currentCanRepeat()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentOnTrigger()
                            delay(repeatDelayMs)
                        }
                    }
                    tryAwaitRelease()
                    job.cancel()
                    if (!holdFired) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentOnTrigger()
                    }
                }
            }
        )
    }
}

/** Streak indicator reused by [BigScoreCard], matching [ActiveTeamCard]'s streak chip. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StreakIndicator(
    streak: Int,
    streakColor: Color,
    contentColor: Color,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = stringResource(R.string.edit_streak_cd),
            tint = if (streak > 0) streakColor else contentColor.copy(alpha = 0.5f),
            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleMedium.fontSize.toDp() })
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = if (streak > 0) streak.toString() else "--",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (streak > 0) streakColor else contentColor.copy(alpha = 0.5f)
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveTeamCard(
    name: String,
    shortName: String,
    players: List<Player>,
    cardColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    streakColor: Color,
    streak: Int,
    streakTeamId: String,
    showElo: Boolean,
    showToll: Boolean,
    targetDate: String,
    gamesPlayedMap: Map<Int, Int>,
    rebalancedPlayerIds: Set<Int>,
    autoSelectedLoserPlayerIds: Set<Int>,
    assignedPositions: Map<Int, PlayerPosition> = emptyMap(),
    usesPositions: Boolean = false,
    positionTeamSize: Int = 6,
    portraitPlayersFirst: Boolean = false,
    score: Int,
    showScore: Boolean = true,
    showLatestPointBorder: Boolean = false,
    showRotationIndicator: Boolean = false,
    scoreIndicatorTooltip: String? = null,
    onIncrementScore: () -> Unit,
    onDecrementScore: () -> Unit,
    onStreakLongClick: (String) -> Unit,
    onPlayerClick: (Player) -> Unit,
    onWin: () -> Unit
) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black
    val haptic = LocalHapticFeedback.current
    val avgEloTooltipState = rememberTooltipState(isPersistent = true)
    val avgEloTooltipScope = rememberCoroutineScope()
    val avgEloTooltipText = stringResource(R.string.average_elo_indicator_tooltip)
    val avgEloIndicatorCd = stringResource(R.string.average_elo_indicator_cd)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val teamNameStyle = if (!isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium

    @Composable
    fun TeamHeader() {
        // In landscape (small scoreboard side-by-side layout), the average Elo and streak
        // indicators are mirrored on the right-side team card so their positions match the
        // big scoreboard layout (relative to the central VS button). Portrait is unaffected.
        val mirrorHeader = isLandscape && !portraitPlayersFirst
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
                val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

                val avgEloContent: @Composable () -> Unit = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = {
                            PlainTooltip(
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = avgEloTooltipText,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        state = avgEloTooltipState,
                        enableUserInput = false
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
                                .combinedClickable(
                                    onClick = { avgEloTooltipState.dismiss() },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        avgEloTooltipScope.launch {
                                            avgEloTooltipState.dismiss()
                                            avgEloTooltipState.show()
                                        }
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = avgEloIndicatorCd,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() }),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                EloCalculator.formatElo(avgElo),
                                fontSize = 14.sp,
                                color = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                val streakContent: @Composable () -> Unit = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStreakLongClick(streakTeamId)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = stringResource(R.string.edit_streak_cd),
                            tint = if (streak > 0) streakColor else contentColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() })
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = if (streak > 0) streak.toString() else "--",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (streak > 0) streakColor else contentColor.copy(alpha = 0.5f)
                        )
                    }
                }

                // In landscape, the point/rotation indicator replaces the average Elo
                // indicator, matching the big scoreboard's header layout. Portrait keeps
                // the average Elo indicator unchanged.
                val primaryContent: @Composable () -> Unit = if (isLandscape) {
                    {
                        // Wrapped in a Box so a measurable is always emitted, even when
                        // PointIndicatorIcon renders nothing (no border/rotation to show).
                        Box(
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PointIndicatorIcon(
                                showRotationIndicator = showRotationIndicator,
                                showLatestPointBorder = showLatestPointBorder,
                                tooltipText = scoreIndicatorTooltip,
                                tint = contentColor
                            )
                        }
                    }
                } else {
                    avgEloContent
                }

                val leadingPlaceable = subcompose("leading") {
                    if (mirrorHeader) streakContent() else primaryContent()
                }.first().measure(looseConstraints)

                val trailingPlaceable = subcompose("trailing") {
                    if (mirrorHeader) primaryContent() else streakContent()
                }.first().measure(looseConstraints)

                val sideReserve = maxOf(leadingPlaceable.width, trailingPlaceable.width)
                val centerMaxWidth = (constraints.maxWidth - sideReserve * 2).coerceAtLeast(0)

                val unboundedConstraints = Constraints(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = Constraints.Infinity,
                    maxHeight = Constraints.Infinity
                )
                val fullNameNaturalWidth = subcompose("nameMeasure") {
                    Text(
                        name,
                        style = teamNameStyle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }.first().measure(unboundedConstraints).width
                val displayName = if (fullNameNaturalWidth > centerMaxWidth) shortName else name

                val centerPlaceable = subcompose("name") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            displayName,
                            style = teamNameStyle,
                            fontWeight = FontWeight.Bold,
                            color = buttonColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }.first().measure(looseConstraints.copy(maxWidth = centerMaxWidth))

                val layoutWidth = constraints.maxWidth
                val layoutHeight = maxOf(leadingPlaceable.height, trailingPlaceable.height, centerPlaceable.height)

                layout(layoutWidth, layoutHeight) {
                    leadingPlaceable.placeRelative(0, (layoutHeight - leadingPlaceable.height) / 2)
                    trailingPlaceable.placeRelative(
                        layoutWidth - trailingPlaceable.width,
                        (layoutHeight - trailingPlaceable.height) / 2
                    )
                    centerPlaceable.placeRelative(
                        (layoutWidth - centerPlaceable.width) / 2,
                        (layoutHeight - centerPlaceable.height) / 2
                    )
                }
            }
        }
    }

    @Composable
    fun ScoreCounter() {
        // In landscape, mirror the -/+ buttons on the left team card so they match the big
        // scoreboard layout (relative to the central VS button). Portrait is unaffected.
        val swapButtons = isLandscape && portraitPlayersFirst
        val decrementButton: @Composable () -> Unit = {
            RepeatingScoreButton(
                onClick = onDecrementScore,
                canRepeat = { score > 0 },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = stringResource(R.string.decrease_score),
                    tint = buttonColor
                )
            }
        }
        val incrementButton: @Composable () -> Unit = {
            RepeatingScoreButton(
                onClick = onIncrementScore,
                canRepeat = { score < 99 },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.increase_score),
                    tint = buttonColor
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(buttonColor.copy(alpha = 0.1f), shape = CircleShape)
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            if (swapButtons) incrementButton() else decrementButton()
            Spacer(Modifier.width(4.dp))
            ScoreValueIndicator(
                score = score,
                textColor = buttonColor,
                indicatorColor = buttonColor,
                showLatestPointBorder = showLatestPointBorder,
                showRotationIndicator = showRotationIndicator,
                tooltipText = scoreIndicatorTooltip
            )
            Spacer(Modifier.width(4.dp))
            if (swapButtons) decrementButton() else incrementButton()
        }
    }

    @Composable
    fun TeamPlayers() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            players.forEach { p ->
                key(p.id) {
                    val isRebalancedPlayer = rebalancedPlayerIds.contains(p.id)
                    val isAutoSelectedLoser = autoSelectedLoserPlayerIds.contains(p.id)
                    val assignedPosition = assignedPositions[p.id]
                    val isBenchLibero = usesPositions && positionTeamSize == 7 && assignedPosition == PlayerPosition.LIBERO
                    val positionTooltip = listOfNotNull(
                        if (isBenchLibero) stringResource(R.string.team_bench_libero_tooltip) else null,
                        assignedPosition?.let { assignedPositionTooltip(p, it, positionTeamSize) }
                    ).joinToString("\n\n").ifBlank { null }
                    val statusTooltip = playerStatusTooltipText(isRebalancedPlayer, isAutoSelectedLoser)
                    val rowTooltipText = listOfNotNull(positionTooltip, statusTooltip)
                        .joinToString("\n\n")
                        .ifBlank { null }
                    val tooltipState = rememberTooltipState(isPersistent = true)
                    val playerRowScope = rememberCoroutineScope()
                    val actualGamesToday = gamesPlayedMap[p.id] ?: 0
                    val hasToll = p.dailyToll > 0 && p.tollDate == targetDate

                    SubcomposeLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(CircleShape)
                            .then(
                                if (isBenchLibero) {
                                    Modifier.border(1.dp, contentColor.copy(alpha = 0.4f), CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .combinedClickable(
                                onClick = {
                                    if (rowTooltipText != null) {
                                        playerRowScope.launch { tooltipState.show() }
                                    }
                                },
                                onLongClick = {
                                    tooltipState.dismiss()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayerClick(p)
                                }
                            )
                    ) { constraints ->
                        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

                        val leadingPlaceable = subcompose("statusIcons") {
                            if (isRebalancedPlayer || isAutoSelectedLoser || assignedPosition != null) {
                                val leadingRow: @Composable () -> Unit = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        if (assignedPosition != null) {
                                            AssignedPositionBadge(
                                                player = p,
                                                position = assignedPosition,
                                                teamSize = positionTeamSize,
                                                contentColor = contentColor.copy(alpha = 0.7f)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        if (isRebalancedPlayer || isAutoSelectedLoser) {
                                            PlayerStatusIcons(
                                                isRebalancedPlayer = isRebalancedPlayer,
                                                isAutoSelectedLoser = isAutoSelectedLoser,
                                                contentColor = contentColor
                                            )
                                        }
                                    }
                                }
                                if (rowTooltipText != null) {
                                    TooltipBox(
                                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                            TooltipAnchorPosition.Above
                                        ),
                                        tooltip = {
                                            PlainTooltip(modifier = Modifier.padding(horizontal = 8.dp)) {
                                                Text(
                                                    text = rowTooltipText,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        state = tooltipState,
                                        enableUserInput = false
                                    ) {
                                        leadingRow()
                                    }
                                } else {
                                    leadingRow()
                                }
                            } else {
                                Spacer(Modifier)
                            }
                        }.first().measure(looseConstraints)

                        val trailingPlaceable = subcompose("toll") {
                            if (showToll) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.volei_manager_icon),
                                        contentDescription = null,
                                        tint = contentColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(
                                            with(LocalDensity.current) {
                                                MaterialTheme.typography.bodyMedium.fontSize.toDp()
                                            }
                                        )
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = actualGamesToday.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentColor.copy(alpha = 0.7f),
                                        maxLines = 1
                                    )
                                    if (hasToll) {
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = "+${p.dailyToll}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                            } else {
                                Spacer(Modifier)
                            }
                        }.first().measure(looseConstraints)

                        val sideReserve = maxOf(leadingPlaceable.width, trailingPlaceable.width)
                        val centerMaxWidth = (constraints.maxWidth - sideReserve * 2).coerceAtLeast(0)

                        val centerPlaceable = subcompose("identity") {
                            PlayerIdentityInlineRow(
                                name = p.name,
                                showPriority = !usesPositions && p.isPriority,
                                showElo = showElo,
                                eloText = EloCalculator.formatElo(p.elo),
                                contentColor = contentColor,
                                enableMarquee = isLandscape
                            )
                        }.first().measure(looseConstraints.copy(maxWidth = centerMaxWidth))

                        val layoutWidth = constraints.maxWidth
                        val layoutHeight = maxOf(
                            leadingPlaceable.height,
                            trailingPlaceable.height,
                            centerPlaceable.height,
                            constraints.minHeight
                        )

                        layout(layoutWidth, layoutHeight) {
                            leadingPlaceable.placeRelative(0, (layoutHeight - leadingPlaceable.height) / 2)
                            trailingPlaceable.placeRelative(
                                layoutWidth - trailingPlaceable.width,
                                (layoutHeight - trailingPlaceable.height) / 2
                            )
                            centerPlaceable.placeRelative(
                                (layoutWidth - centerPlaceable.width) / 2,
                                (layoutHeight - centerPlaceable.height) / 2
                            )
                        }
                    }
                }
            }
        }
    }

    // Corner radius of the outer card, so the control panel can align flush with it
    // on whichever edge (top or bottom) it occupies.
    val cardTopCorner = 30.dp
    val cardBottomCorner = 30.dp
    val panelInnerCorner = 30.dp

    @Composable
    fun TeamControlPanel(atTop: Boolean, content: @Composable ColumnScope.() -> Unit) {
        val topCorner = if (atTop) cardTopCorner else panelInnerCorner
        val bottomCorner = if (atTop) panelInnerCorner else cardBottomCorner
        val shape = RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner
        )
        // Cards with a light (light-theme) background need a much subtler scrim, otherwise
        // the panel reads as a heavy dark smudge; dark cards can take a stronger overlay.
        val isDarkCard = cardColor.luminance() < 0.5f
        val panelBackgroundAlpha = if (isDarkCard) 0.10f else 0.07f
        val ringColor = MaterialTheme.colorScheme.background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The ring is drawn OUTSIDE the panel's own layout bounds (from the edge out to
                // ~4dp), so it must be placed before .clip() in the chain: modifiers listed
                // earlier are unaffected by a later .clip(), letting this overflow past both the
                // panel and (since the outer team card no longer clips) past the card itself.
                .drawWithCache {
                    val borderPx = 4.dp.toPx()
                    val topCornerPx = topCorner.toPx()
                    val bottomCornerPx = bottomCorner.toPx()
                    val innerPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(0f, 0f, size.width, size.height),
                                topLeft = CornerRadius(topCornerPx),
                                topRight = CornerRadius(topCornerPx),
                                bottomLeft = CornerRadius(bottomCornerPx),
                                bottomRight = CornerRadius(bottomCornerPx)
                            )
                        )
                    }
                    val outerPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(-borderPx, -borderPx, size.width + borderPx, size.height + borderPx),
                                topLeft = CornerRadius(topCornerPx + borderPx),
                                topRight = CornerRadius(topCornerPx + borderPx),
                                bottomLeft = CornerRadius(bottomCornerPx + borderPx),
                                bottomRight = CornerRadius(bottomCornerPx + borderPx)
                            )
                        )
                    }
                    val ringPath = Path().apply {
                        op(outerPath, innerPath, PathOperation.Difference)
                    }
                    onDrawBehind {
                        drawPath(ringPath, color = ringColor)
                    }
                }
                .clip(shape)
                .background(Color.Black.copy(alpha = panelBackgroundAlpha))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }

    // The outer team card paints its own rounded background without clipping its content,
    // so TeamControlPanel's outward ring can spill past the card's true edges instead of
    // being cut off at them.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                cardColor,
                RoundedCornerShape(topStart = cardTopCorner, topEnd = cardTopCorner, bottomStart = cardBottomCorner, bottomEnd = cardBottomCorner)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLandscape) {
                TeamControlPanel(atTop = true) {
                    TeamHeader()
                    if (showScore) {
                        Spacer(Modifier.height(4.dp))
                        ScoreCounter()
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onWin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            stringResource(R.string.victory),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = buttonTextColor
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)) {
                    TeamPlayers()
                }

            } else {
                if (portraitPlayersFirst) {
                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)) {
                        TeamPlayers()
                    }
                    Spacer(Modifier.height(8.dp))
                    TeamControlPanel(atTop = false) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = onWin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.victory_short),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = buttonTextColor
                                )
                            }

                            if (showScore) {
                                Spacer(Modifier.height(4.dp))
                                ScoreCounter()
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TeamHeader()
                    }
                } else {
                    TeamControlPanel(atTop = true) {
                        TeamHeader()
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showScore) {
                                ScoreCounter()
                                Spacer(Modifier.height(4.dp))
                            }
                            Button(
                                onClick = onWin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.victory_short),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = buttonTextColor
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)) {
                        TeamPlayers()
                    }
                }
            }
        }
    }
}

/** Texto de status do jogador (rebalanceado / perdedor reaproveitado), ou `null` se não houver. */
@Composable
private fun playerStatusTooltipText(
    isRebalancedPlayer: Boolean,
    isAutoSelectedLoser: Boolean
): String? = when {
    isRebalancedPlayer && isAutoSelectedLoser ->
        stringResource(R.string.rebalanced_and_auto_selected_loser_player_tooltip)
    isRebalancedPlayer -> stringResource(R.string.rebalanced_player_tooltip)
    isAutoSelectedLoser -> stringResource(R.string.auto_selected_loser_player_tooltip)
    else -> null
}

/** Ícones de status. A tooltip é responsabilidade da linha do jogador, que a unifica. */
@Composable
private fun PlayerStatusIcons(
    isRebalancedPlayer: Boolean,
    isAutoSelectedLoser: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val inlineIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }
    val rebalancedIconCd = stringResource(R.string.rebalanced_player_icon_cd)
    val autoSelectedLoserIconCd = stringResource(R.string.auto_selected_loser_player_icon_cd)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (isRebalancedPlayer) {
            Icon(
                painter = painterResource(R.drawable.arrowsbothsides),
                contentDescription = rebalancedIconCd,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(inlineIconSize)
            )
            Spacer(Modifier.width(4.dp))
        }
        if (isAutoSelectedLoser) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.arrowdown),
                contentDescription = autoSelectedLoserIconCd,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(inlineIconSize)
            )
        }
    }
}

@Composable
private fun PlayerIdentityInlineRow(
    name: String,
    showPriority: Boolean,
    showElo: Boolean,
    eloText: String,
    contentColor: Color,
    enableMarquee: Boolean,
    modifier: Modifier = Modifier
) {
    val inlineIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }
    val priorityCd = stringResource(R.string.priority)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enableMarquee) {
                    Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            softWrap = false,
            overflow = if (enableMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
            color = contentColor
        )
        if (showPriority) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.Star,
                contentDescription = priorityCd,
                modifier = Modifier.size(inlineIconSize),
                tint = contentColor.copy(alpha = 0.7f)
            )
        }
        if (showElo) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                modifier = Modifier.size(inlineIconSize),
                tint = contentColor.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = eloText,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScoreValueIndicator(
    score: Int,
    textColor: Color,
    indicatorColor: Color,
    showLatestPointBorder: Boolean,
    showRotationIndicator: Boolean,
    tooltipText: String?,
    circleSize: Dp = 48.dp,
    strokeWidth: Dp = 1.dp,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
    val hasTooltip = !tooltipText.isNullOrBlank()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val scoreContent: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .defaultMinSize(minWidth = circleSize, minHeight = circleSize)
        ) {
            when {
                showRotationIndicator -> {
                    Canvas(modifier = Modifier.size(circleSize)) {
                        drawCircle(
                            color = indicatorColor,
                            radius = size.minDimension / 2f - strokeWidth.toPx(),
                            style = Stroke(
                                width = strokeWidth.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                                )
                            )
                        )
                    }
                }

                showLatestPointBorder -> {
                    Canvas(modifier = Modifier.size(circleSize)) {
                        drawCircle(
                            color = indicatorColor,
                            radius = size.minDimension / 2f - strokeWidth.toPx(),
                            style = Stroke(width = strokeWidth.toPx())
                        )
                    }
                }
            }

            Text(
                text = score.toString(),
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }

    if (!hasTooltip) {
        scoreContent()
        return
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(
                    text = tooltipText.orEmpty(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        state = tooltipState,
        enableUserInput = false
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.dismiss()
                            tooltipState.show()
                        }
                    }
                )
        ) {
            scoreContent()
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 38.dp, bottomEnd = 38.dp)
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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !didSetInitialCursor) {
                            didSetInitialCursor = true
                            textFieldValue =
                                textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
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
private fun GroupOnboardingGroupTypeCard(
    selectedType: GroupType,
    onTypeSelected: (GroupType) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 38.dp, bottomEnd = 38.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.onboarding_group_type_question),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            GroupType.selectableTypes.forEach { type ->
                GroupTypeOptionRow(
                    type = type,
                    selected = selectedType == type,
                    onSelect = { onTypeSelected(type) }
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_group_type_hint),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 38.dp, bottomEnd = 38.dp)
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
                    onSelect = { onModeSelected(value) },
                    iconRes = if (value == com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name) {
                        R.drawable.arrowsbothsides
                    } else {
                        R.drawable.zzz_rest
                    }
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
    onSelect: () -> Unit,
    iconRes: Int,
    iconSize: Dp = 20.dp
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(text = tooltip, style = MaterialTheme.typography.bodySmall)
            }
        },
        state = tooltipState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
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
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun GroupOnboardingTeamSizeCard(
    selectedTeamSize: Int?,
    onTeamSizeSelected: (Int) -> Unit,
    showBack: Boolean,
    groupType: GroupType,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val options = remember(groupType) { groupType.teamSizeRange.toList() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 38.dp, bottomEnd = 38.dp)
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
            if (groupType.usesPositions && selectedTeamSize == 7) {
                Text(
                    text = stringResource(R.string.onboarding_team_size_seven_bench_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 38.dp, bottomEnd = 38.dp)
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
    presentPlayerIds: Set<Int> = emptySet(),
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
    val winnerNamesAnnotated = remember(lastWinners, presentPlayerIds) {
        buildAnnotatedString {
            append("(")
            lastWinners.forEachIndexed { index, player ->
                if (index > 0) append(", ")
                if (presentPlayerIds.contains(player.id)) {
                    append(player.name)
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(player.name)
                    }
                }
            }
            append(")")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 48.dp, bottomEnd = 48.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val limitReached = hasPreviousMatch && currentStreak >= victoryLimit
            val winnerNames = lastWinners.map { it.name }
            val kingTextColor = MaterialTheme.colorScheme.tertiary
            val mainLogo = if (isDarkTheme) {
                R.drawable.bola_volei_fundo_escuro
            } else {
                R.drawable.logo_volei_manager
            }
            if (limitReached) {
                Icon(
                    painter = painterResource(R.drawable.crown_icon),
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
                    if (winnerNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = winnerNamesAnnotated,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
                    Text(
                        text = stringResource(R.string.team_x_wins, teamName),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = winnerNamesAnnotated,
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
    isGuaranteedNextMatch: Boolean,
    gamesPlayed: Int?,
    targetDate: String,
    showElo: Boolean,
    showToll: Boolean,
    isWithoutHistory: Boolean,
    usesPositions: Boolean = false,
    onTogglePresence: () -> Unit,
    onToggleGuaranteedNextMatch: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val locale = currentLocale()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cardShape = RoundedCornerShape(12.dp)
    val border = when {
        isGuaranteedNextMatch -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
        isPresent -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        isWithoutHistory -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        else -> null
    }
    val containerColor = when {
        isGuaranteedNextMatch -> MaterialTheme.colorScheme.tertiaryContainer
        isPresent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border
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
                Checkbox(
                    checked = isPresent,
                    onCheckedChange = { onTogglePresence() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if(isGuaranteedNextMatch) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (isLandscape && usesPositions) {
                        // Em paisagem, nomes longos já rolam via marquee; a quebra de linha dos
                        // selos não se aplica aqui (a linha inteira já tem espaço ilimitado).
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                        ) {
                            Text(
                                text = player.name,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                            PlayerPositionBadges(
                                player = player,
                                usesPositions = true,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    } else {
                        PlayerNameWithPositionBadges(
                            player = player,
                            usesPositions = usesPositions,
                            nameFontWeight = FontWeight.Bold,
                            isPriority = player.isPriority,
                            priorityIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() },
                            priorityTint = if (isGuaranteedNextMatch) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }
                    if (showElo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = EloCalculator.formatElo(player.elo),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val actualGames = gamesPlayed ?: 0
                    val today = remember(locale) {
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            locale
                        ).format(Date())
                    }
                    val hasToll =
                        player.dailyToll > 0 && (player.tollDate == targetDate || player.tollDate == today)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.volei_manager_icon),
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = actualGames.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showToll && hasToll) {
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "+${player.dailyToll}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (isGuaranteedNextMatch) {
                            stringResource(R.string.remove_next_match_guarantee)
                        } else {
                            stringResource(R.string.guarantee_next_match)
                        }
                    )
                },
                onClick = { showMenu = false; onToggleGuaranteedNextMatch() },
                leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
            )
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
fun WaitingPlayerCard(
    index: Int,
    player: Player,
    showElo: Boolean,
    isResting: Boolean,
    usesPositions: Boolean = false,
    onClick: () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    // In portrait every fillMaxWidth()/weight() below is skipped so the whole card hugs its
    // content (name + star/position badges); those only make sense to enable the landscape
    // marquee, which needs a fixed width range to scroll long names within.
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .then(
                if (isLandscape) Modifier.widthIn(min = 120.dp, max = 220.dp) else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .then(if (isLandscape) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = 60.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .then(if (isLandscape) Modifier.fillMaxWidth() else Modifier)
                    .padding(12.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$index.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = if (isLandscape) Modifier.weight(1f) else Modifier) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .then(if (isLandscape) Modifier.fillMaxWidth() else Modifier)
                            .then(
                                if (isLandscape) {
                                    Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = if (isLandscape) TextOverflow.Clip else TextOverflow.Ellipsis
                        )
                        if (usesPositions) {
                            PlayerPositionBadges(
                                player = player,
                                usesPositions = true,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else if (player.isPriority) {
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
                                painter = painterResource(R.drawable.zzz_rest),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .offset(y = (-4).dp)
                                    .size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() })
                            )
                        }
                    }
                    if (showElo) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(2.dp))
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
 * A button that fires [onClick] on confirmed tap (with haptic feedback),
 * or repeats it continuously while the finger is held down.
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
    val interactionSource = remember { MutableInteractionSource() }
    // Tracks whether the hold-repeat already fired, to avoid double-fire in onTap.
    var holdFired by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        holdFired = false
                        var holdPress: PressInteraction.Press? = null
                        // launch runs concurrently while tryAwaitRelease() waits for finger up.
                        // coroutineScope {} creates a child scope tied to this suspend lambda.
                        coroutineScope {
                            val job = launch {
                                delay(initialDelayMs)
                                holdFired = true
                                // Show visual press state only when the hold action actually starts.
                                holdPress = PressInteraction.Press(offset)
                                interactionSource.emit(holdPress!!)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnClick()
                                while (currentCanRepeat?.invoke() != false) {
                                    delay(repeatDelayMs)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentOnClick()
                                }
                            }
                            val released = tryAwaitRelease()
                            job.cancel()
                            holdPress?.let { press ->
                                if (released) interactionSource.emit(PressInteraction.Release(press))
                                else interactionSource.emit(PressInteraction.Cancel(press))
                            }
                        }
                    },
                    // onTap only fires on a confirmed tap (not on scroll/drag),
                    // so the score only changes when the user actually taps the button.
                    onTap = { offset ->
                        if (!holdFired) {
                            val tapPress = PressInteraction.Press(offset)
                            interactionSource.tryEmit(tapPress)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentOnClick()
                            interactionSource.tryEmit(PressInteraction.Release(tapPress))
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

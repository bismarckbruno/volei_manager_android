package com.bismarck.voleimanager.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.EloCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class HistoryPlayerInfo(
    val player: Player,
    val displayElo: Double,
    val name: String,
    val gamesPlayed: Int,
    val victories: Int,
    val playedMinutes: Int
)

enum class PlayerSortMode { ALPHABETICAL, ELO, GAMES, VICTORIES, PERCENTAGE, PLAYED_TIME }
enum class MatchSortMode { NEWEST, OLDEST, ELO_DELTA, SCORE_DIFF }

private data class PlayerIdentifier(val id: Int?, val name: String)

private data class HistoryComputationResult(
    val sortedHistory: List<MatchHistory>,
    val matchDurationsMinutes: Map<Int, Int>,
    val averageMatchDurationMinutes: Int?,
    val uniquePlayerCount: Int,
    val historyPlayerList: List<HistoryPlayerInfo>,
    val averagePlayersEloText: String?
)

private fun computeHistoryComputation(
    groupHistory: List<MatchHistory>,
    historyDate: String?,
    matchSortMode: MatchSortMode,
    groupPlayers: List<Player>,
    eloLogs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>,
    playerSortMode: PlayerSortMode
): HistoryComputationResult {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val filteredHistory = groupHistory.filter { historyDate == null || it.date.startsWith(historyDate) }
    val sortedHistory = when (matchSortMode) {
        MatchSortMode.NEWEST -> filteredHistory.sortedWith(
            compareByDescending<MatchHistory> {
                try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
            }.thenByDescending { it.id }
        )
        MatchSortMode.OLDEST -> filteredHistory.sortedWith(
            compareBy<MatchHistory> {
                try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
            }.thenByDescending { it.id }
        )
        MatchSortMode.ELO_DELTA -> filteredHistory.sortedWith(
            compareByDescending<MatchHistory> { it.eloPoints }.thenByDescending { it.id }
        )
        MatchSortMode.SCORE_DIFF -> filteredHistory.sortedWith(
            compareByDescending<MatchHistory> {
                kotlin.math.abs((it.teamAScore ?: 0) - (it.teamBScore ?: 0))
            }.thenByDescending { it.id }
        )
    }

    val matchDurationsMinutes = buildMap {
        sortedHistory.forEach { match ->
            if (match.startTimestamp != null && match.endTimestamp != null && match.endTimestamp > match.startTimestamp) {
                put(match.id, ((match.endTimestamp - match.startTimestamp) / 60000L).toInt().coerceAtLeast(1))
            }
        }
    }
    val averageMatchDurationMinutes = if (matchDurationsMinutes.isEmpty()) null else matchDurationsMinutes.values.average().toInt()

    fun parseTeam(teamNamesRaw: String, teamIdsRaw: String): List<PlayerIdentifier> {
        val names = teamNamesRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val ids = teamIdsRaw.split(",").mapNotNull { it.trim().toIntOrNull() }
        return names.mapIndexed { index, name -> PlayerIdentifier(ids.getOrNull(index), name) }
    }

    val allIdentifiers = buildList {
        sortedHistory.forEach { match ->
            addAll(parseTeam(match.teamA, match.teamAIds))
            addAll(parseTeam(match.teamB, match.teamBIds))
        }
    }

    val uniquePlayerIdentifiers = mutableListOf<PlayerIdentifier>()
    allIdentifiers.forEach { identifier ->
        if (identifier.id != null && uniquePlayerIdentifiers.any { it.id == identifier.id }) return@forEach
        val existingByName = uniquePlayerIdentifiers.find { it.name == identifier.name }
        if (existingByName == null) {
            uniquePlayerIdentifiers.add(identifier)
        } else if (existingByName.id == null && identifier.id != null) {
            uniquePlayerIdentifiers.remove(existingByName)
            uniquePlayerIdentifiers.add(identifier)
        }
    }

    val playedMinutesByIdentifier = mutableMapOf<PlayerIdentifier, Int>()
    sortedHistory.forEach { match ->
        val duration = matchDurationsMinutes[match.id] ?: 0
        if (duration <= 0) return@forEach
        val matchIdentifiers = (parseTeam(match.teamA, match.teamAIds) + parseTeam(match.teamB, match.teamBIds)).toSet()
        matchIdentifiers.forEach { identifier ->
            playedMinutesByIdentifier[identifier] = (playedMinutesByIdentifier[identifier] ?: 0) + duration
        }
    }

    val playersById = groupPlayers.associateBy { it.id }
    val playersByName = groupPlayers.associateBy { it.name }
    val eloDateStr = if (historyDate != null) {
        try {
            val parts = historyDate.split("/")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
        } catch (_: Exception) {
            null
        }
    } else null
    val filteredLogs = if (eloDateStr != null) eloLogs.filter { it.date == eloDateStr } else eloLogs
    val logsByPlayerId = filteredLogs.groupBy { it.playerId }
    val logsByName = filteredLogs.groupBy { it.playerNameSnapshot }

    val playerDataList = uniquePlayerIdentifiers.map { identifier ->
        val player = if (identifier.id != null) playersById[identifier.id] else playersByName[identifier.name]
        val logsForPlayer = when {
            player != null -> logsByPlayerId[player.id].orEmpty()
            else -> logsByName[identifier.name].orEmpty()
        }
        val games = logsForPlayer.size
        val victories = logsForPlayer.count { it.won == true }
        val eloForDisplay = logsForPlayer.maxByOrNull { it.id }?.elo ?: (player?.elo ?: 1200.0)
        val effectivePlayer = player ?: Player(name = identifier.name, groupName = "", elo = 1200.0)

        HistoryPlayerInfo(
            player = effectivePlayer,
            displayElo = eloForDisplay,
            name = player?.name ?: identifier.name,
            gamesPlayed = games,
            victories = victories,
            playedMinutes = playedMinutesByIdentifier[identifier] ?: 0
        )
    }

    fun HistoryPlayerInfo.winRate(): Double = if (gamesPlayed > 0) victories.toDouble() / gamesPlayed else 0.0

    val sortedPlayers = when (playerSortMode) {
        PlayerSortMode.ELO -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.displayElo }
                .thenByDescending { it.winRate() }
        )
        PlayerSortMode.GAMES -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.gamesPlayed }
                .thenByDescending { it.winRate() }
                .thenByDescending { it.displayElo }
        )
        PlayerSortMode.VICTORIES -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.victories }
                .thenByDescending { it.winRate() }
                .thenByDescending { it.displayElo }
        )
        PlayerSortMode.PERCENTAGE -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.winRate() }
                .thenBy { it.gamesPlayed }
                .thenByDescending { it.displayElo }
        )
        PlayerSortMode.PLAYED_TIME -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.playedMinutes }
                .thenByDescending { it.displayElo }
                .thenByDescending { it.victories }
                .thenByDescending { it.winRate() }
        )
        PlayerSortMode.ALPHABETICAL -> playerDataList.sortedWith(
            compareBy<HistoryPlayerInfo> { it.player.name.lowercase() }
                .thenByDescending { it.displayElo }
        )
    }

    val averagePlayersEloText = if (sortedPlayers.isEmpty()) null
    else NumberFormat.getIntegerInstance(Locale.getDefault()).format(sortedPlayers.map { it.displayElo }.average().toInt())

    return HistoryComputationResult(
        sortedHistory = sortedHistory,
        matchDurationsMinutes = matchDurationsMinutes,
        averageMatchDurationMinutes = averageMatchDurationMinutes,
        uniquePlayerCount = uniquePlayerIdentifiers.size,
        historyPlayerList = sortedPlayers,
        averagePlayersEloText = averagePlayersEloText
    )
}

fun formatLocalizedDate(internalDate: String): String {
    val language = Locale.getDefault().language
    if (language != "en") return internalDate

    return try {
        if (internalDate.contains(":")) {
            val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT)
            val date = parser.parse(internalDate)
            val formatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
            date?.let { formatter.format(it) } ?: internalDate
        } else {
            val parser = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
            val date = parser.parse(internalDate)
            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: internalDate
        }
    } catch (e: Exception) {
        internalDate
    }
}

private fun formatPlayedDuration(minutes: Int): String {
    val safeMinutes = minutes.coerceAtLeast(0)
    val days = safeMinutes / (24 * 60)
    val hours = (safeMinutes % (24 * 60)) / 60
    val remainingMinutes = safeMinutes % 60
    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (remainingMinutes > 0 || parts.isEmpty()) parts.add("${remainingMinutes}min")
    return parts.joinToString(" ")
}

// --- TELA DE HISTÓRICO ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: VoleiViewModel,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean = true,
    selectedTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    playerSortMode: PlayerSortMode = PlayerSortMode.ALPHABETICAL,
    matchSortMode: MatchSortMode = MatchSortMode.NEWEST,
    onMatchSortModeChanged: (MatchSortMode) -> Unit = {},
    onPlayerSortModeChanged: (PlayerSortMode) -> Unit = {},
    onContentReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val groupHistory by viewModel.currentGroupHistory.collectAsState()
    val historyDate by viewModel.historyDateFilter.collectAsState()
    val availableDates by viewModel.availableHistoryDates.collectAsState()
    val eloLogs by viewModel.currentGroupEloLogs.collectAsState()
    val groupPlayers by viewModel.currentGroupPlayers.collectAsState()

    // 0 = Partidas, 1 = Jogadores
    val pagerState = rememberPagerState(initialPage = selectedTab) { 2 }
    LaunchedEffect(pagerState.currentPage) {
        onTabChanged(pagerState.currentPage)
    }
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    // removed matchSortMode local state
    var expandedFilter by remember { mutableStateOf(false) }

    var historyComputation by remember { mutableStateOf<HistoryComputationResult?>(null) }
    var isComputingHistory by remember { mutableStateOf(false) }
    LaunchedEffect(groupHistory, historyDate, matchSortMode, groupPlayers, eloLogs, playerSortMode) {
        isComputingHistory = true
        try {
            historyComputation = withContext(Dispatchers.Default) {
                computeHistoryComputation(
                    groupHistory = groupHistory,
                    historyDate = historyDate,
                    matchSortMode = matchSortMode,
                    groupPlayers = groupPlayers,
                    eloLogs = eloLogs,
                    playerSortMode = playerSortMode
                )
            }
        } finally {
            isComputingHistory = false
        }
    }
    LaunchedEffect(historyComputation) {
        if (historyComputation != null) onContentReady()
    }

    val sortedHistory = historyComputation?.sortedHistory.orEmpty()
    val matchDurationsMinutes = historyComputation?.matchDurationsMinutes.orEmpty()
    val averageMatchDurationMinutes = historyComputation?.averageMatchDurationMinutes
    val uniquePlayerCount = historyComputation?.uniquePlayerCount ?: 0
    val historyPlayerList = historyComputation?.historyPlayerList.orEmpty()
    val averagePlayersEloText = historyComputation?.averagePlayersEloText

    // --- Compute layout mode once for all player cards ---
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val nameTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
    val statsTextStyle = MaterialTheme.typography.bodySmall
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    // Overhead: 16dp*2 (Column padding) + 12dp*2 (Card padding) + 28dp (rank) + 8dp (spacer) + 12dp (gap)
    val contentOverheadPx = with(density) { 104.dp.roundToPx() }
    val availableContentPx = screenWidthPx - contentOverheadPx
    val minNamePx = with(density) { 80.dp.roundToPx() }

    val playersSideBySide = remember(historyPlayerList, availableContentPx, showElo, context) {
        if (historyPlayerList.isEmpty() || availableContentPx <= 0) true
        else historyPlayerList.all { info ->
            // Left column width (name + optional star)
            val nameW = textMeasurer.measure(info.player.name, nameTextStyle).size.width +
                    (if (info.player.isPriority) with(density) { 14.dp.roundToPx() } else 0)
            val eloW = if (showElo) {
                textMeasurer.measure(EloCalculator.formatElo(info.displayElo), statsTextStyle).size.width +
                        with(density) { 16.dp.roundToPx() }
            } else 0
            val playedTimeW = textMeasurer.measure(
                formatPlayedDuration(info.playedMinutes),
                statsTextStyle
            ).size.width + with(density) { 16.dp.roundToPx() }
            val leftW = maxOf(nameW, eloW, playedTimeW)

            // Right column width (stats line is always the widest)
            val vText = when (info.victories) {
                0 -> context.getString(R.string.no_victories)
                1 -> context.getString(R.string.one_victory)
                else -> context.getString(R.string.x_victories, info.victories)
            }
            val gLabel = if (info.gamesPlayed == 1) context.getString(R.string.game) else context.getString(R.string.games)
            val rightW = textMeasurer.measure("$vText / ${info.gamesPlayed} $gLabel", statsTextStyle).size.width

            (leftW + rightW <= availableContentPx) && (availableContentPx - rightW >= minNamePx)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {

        // --- Date filter dropdown ---
        var dateExpanded by remember { mutableStateOf(false) }

        // Obtém a altura da tela atual em Dp
        val configuration = LocalConfiguration.current

        // Define a porcentagem dependendo da orientação
        val heightFraction = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            0.33f // altura da janela relativamente à tela quando esta estiver na HORIZONTAL
        } else {
            0.57f // altura da janela relativamente à tela quando esta estiver na VERTICAL
        }
        val maxMenuHeight = (configuration.screenHeightDp * heightFraction).dp

        ExposedDropdownMenuBox(
            expanded = dateExpanded,
            onExpandedChange = { dateExpanded = !dateExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { dateExpanded = true },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    historyDate?.let { formatLocalizedDate(it) } ?: stringResource(R.string.all_dates),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                val rotation by animateFloatAsState(
                    targetValue = if (dateExpanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "HistoryDateRotation"
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }
            DropdownMenu(
                expanded = dateExpanded,
                onDismissRequest = { dateExpanded = false },
                offset = DpOffset(x = 36.dp, y = 0.dp),
                modifier = Modifier
                    .heightIn(max = maxMenuHeight)
                    .width(IntrinsicSize.Min)
            ) {
                val allDatesSelected = historyDate == null
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.all_dates),
                                fontWeight = if (allDatesSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (allDatesSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (allDatesSelected) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = { viewModel.setHistoryDateFilter(null); dateExpanded = false }
                )
                availableDates.forEach { date ->
                    val isSelected = historyDate == date
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    formatLocalizedDate(date),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (isSelected) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = { viewModel.setHistoryDateFilter(date); dateExpanded = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // --- Segmented button row + filter icon ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Crossfade(
                            targetState = selectedTab == 0,
                            label = "tabIcon0"
                        ) { isSelected ->
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.bola_de_volei_solida_para_variar_a_cor),
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        val matchLabel = if (sortedHistory.size == 1) stringResource(R.string.match) else stringResource(
                            R.string.matches
                        )
                        Text("${sortedHistory.size} $matchLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Crossfade(
                            targetState = selectedTab == 1,
                            label = "tabIcon1"
                        ) { isSelected ->
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Groups,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        val playerLabel = if (uniquePlayerCount == 1) stringResource(R.string.player) else stringResource(
                            R.string.players
                        )
                        Text("$uniquePlayerCount $playerLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Filter/sort icon button
            Box {
                IconButton(onClick = { expandedFilter = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = stringResource(R.string.sort_word),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = expandedFilter, onDismissRequest = { expandedFilter = false }) {
                    if (selectedTab == 0) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.NEWEST, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.newest_first))
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.NEWEST); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.OLDEST, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.oldest_first))
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.OLDEST); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.ELO_DELTA, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_elo_change))
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.ELO_DELTA); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.SCORE_DIFF, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_score_diff))
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.SCORE_DIFF); expandedFilter = false }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.ALPHABETICAL, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.alphabetical))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.ALPHABETICAL); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.ELO, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_elo))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.ELO); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.GAMES, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_matches))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.GAMES); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.VICTORIES, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_victories))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.VICTORIES); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.PERCENTAGE, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_percentage))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.PERCENTAGE); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.PLAYED_TIME, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_played_time))
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.PLAYED_TIME); expandedFilter = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // --- HorizontalPager for swipe between matches and players ---
        if (historyComputation == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 16.dp
                ) { page ->
                    when (page) {
                    0 -> {
                        // --- Matches view ---
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (sortedHistory.isNotEmpty()) {
                                item {
                                    val avgDurationText = averageMatchDurationMinutes?.let { "${it}min" } ?: "--"
                                    HistorySummaryItem(
                                        text = stringResource(
                                            R.string.average_duration,
                                            avgDurationText
                                        ),
                                        leadingIcon = Icons.Default.AccessTime
                                    )
                                }
                            }
                            items(sortedHistory, key = { it.id }) { match ->
                                HistoryItem(
                                    match = match,
                                    isDarkTheme = isDarkTheme,
                                    showElo = showElo,
                                    showScore = showScore,
                                    durationMinutes = matchDurationsMinutes[match.id]
                                )
                            }
                            if (sortedHistory.isEmpty()) item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(R.string.no_matches_found),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            item {  }
                        }
                    }
                    1 -> {
                        // --- Players view ---
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (historyPlayerList.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            stringResource(R.string.no_players),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                item {
                                    HistorySummaryItem(
                                        text = stringResource(
                                            R.string.average_elo,
                                            averagePlayersEloText ?: "--"
                                        ),
                                        leadingIcon = Icons.Default.WorkspacePremium
                                    )
                                }
                                itemsIndexed(
                                    items = historyPlayerList,
                                    key = { _, info -> "${info.player.id}_${info.name}" }
                                ) { index, info ->
                                    HistoryPlayerCard(
                                        rank = if (playerSortMode != PlayerSortMode.ALPHABETICAL) index + 1 else null,
                                        player = info.player,
                                        displayElo = info.displayElo,
                                        showElo = showElo,
                                        gamesPlayed = info.gamesPlayed,
                                        victories = info.victories,
                                        playedMinutes = info.playedMinutes,
                                        useSideBySide = playersSideBySide
                                    )
                                }
                            }
                            item {  }
                            }
                        }
                    }
                }
                Crossfade(
                    targetState = isComputingHistory,
                    label = "historyLoadingProgress"
                ) { isVisible ->
                    if (isVisible) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryItem(text: String, leadingIcon: ImageVector? = null) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (leadingIcon != null) {
                // Define o estilo de tipografia com base no ícone
                val textStyle = if (leadingIcon == Icons.Default.WorkspacePremium) {
                    MaterialTheme.typography.bodyLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                }
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(
                        with(LocalDensity.current) { textStyle.fontSize.toDp()}
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistoryPlayerCard(
    rank: Int?,
    player: Player,
    displayElo: Double,
    showElo: Boolean,
    gamesPlayed: Int = 0,
    victories: Int = 0,
    playedMinutes: Int = 0,
    useSideBySide: Boolean = true
) {
    val victoriesText = when (victories) {
        0 -> stringResource(R.string.no_victories)
        1 -> stringResource(R.string.one_victory)
        else -> stringResource(R.string.x_victories, victories)
    }
    val gamesLabel = when (gamesPlayed) {
        0 -> stringResource(R.string.no_matches)
        1 -> stringResource(R.string.one_match)
        else -> stringResource(R.string.x_matches, gamesPlayed)
    }
    val percentage = if (gamesPlayed > 0) {
        victories.toDouble() / gamesPlayed * 100.0
    } else 0.0
    val percentageFormatted = NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(percentage)
    val playedTimeText = formatPlayedDuration(playedMinutes)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 120.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(12.dp),
            verticalAlignment = if (useSideBySide) Alignment.CenterVertically else Alignment.Top
        ) {
            Box(
                modifier = Modifier.widthIn(min = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rank != null) {
                    Text(
                        "$rank.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))

            if (useSideBySide) {
                // Wide layout: name+elo left, stats right
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                EloCalculator.formatElo(displayElo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(1.dp))
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            playedTimeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$victoriesText / $gamesLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$percentageFormatted%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (showElo) {
                        Text(
                            "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                }
            } else {
                // Narrow layout: everything stacked vertically
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                EloCalculator.formatElo(displayElo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            playedTimeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "$victoriesText / $gamesLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$percentageFormatted%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HistoryItem(
    match: MatchHistory,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean = true,
    durationMinutes: Int? = null
) {
    val isTeamAWin = match.winner == "A" || match.winner == "Time A"
    val teamANames = remember(match.teamA) {
        match.teamA.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .sortedBy { it.lowercase() }
            .joinToString(", ")
    }
    val teamBNames = remember(match.teamB) {
        match.teamB.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .sortedBy { it.lowercase() }
            .joinToString(", ")
    }

    val cardBgColor = if (isTeamAWin) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        LocalExtendedColors.current.anotherPrime.colorContainer
    }

    val contentColor = if (isTeamAWin) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        LocalExtendedColors.current.anotherPrime.onColorContainer
    }

    val crownColor = if (isTeamAWin) {
        MaterialTheme.colorScheme.primary
    } else {
        LocalExtendedColors.current.anotherPrime.color
    }

    val scoreA = match.teamAScore ?: 0
    val scoreB = match.teamBScore ?: 0
    val hasScore = scoreA > 0 || scoreB > 0
    val formattedDelta = remember(match.eloPoints) {
        NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(match.eloPoints)
    }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxWidthPx = with(density) { maxWidth.roundToPx() }
                val dateWidthPx = textMeasurer.measure(formatLocalizedDate(match.date), style = MaterialTheme.typography.labelMedium).size.width
                val eloWidthPx = if (showElo) {
                    textMeasurer.measure(
                        "±$formattedDelta",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    ).size.width
                } else 0
                val durationWidthPx = if (durationMinutes != null) {
                    textMeasurer.measure(
                        "${durationMinutes}min",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    ).size.width + with(density) { 16.dp.roundToPx() }
                } else 0
                val durationGapPx = if (durationMinutes != null) with(density) { 8.dp.roundToPx() } else 0
                val minGapLeftRightPx = if (showElo) with(density) { 12.dp.roundToPx() } else 0

                val shouldBreakDurationLine = showElo && durationMinutes != null &&
                    (dateWidthPx + durationGapPx + durationWidthPx + minGapLeftRightPx + eloWidthPx > maxWidthPx)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    if (shouldBreakDurationLine) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(formatLocalizedDate(match.date), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        contentColor.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                                        tint = contentColor
                                    )
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        text = "${durationMinutes}min",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formatLocalizedDate(match.date), style = MaterialTheme.typography.labelMedium)
                            if (durationMinutes != null) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            contentColor.copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                                            tint = contentColor
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = "${durationMinutes}min",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showElo) {
                        Text(
                            "±$formattedDelta",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                color = contentColor.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTeamAWin) {
                        Icon(
                            painter = painterResource(R.drawable.coroa_icon),
                            contentDescription = stringResource(R.string.winner_word),
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                            tint = crownColor
                        )
                    }
                }
                Spacer(Modifier.width(34.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isTeamAWin) {
                        Icon(
                            painter = painterResource(R.drawable.coroa_icon),
                            contentDescription = stringResource(R.string.winner),
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                            tint = crownColor
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(stringResource(R.string.team_a), fontWeight = FontWeight.Bold)
                    if (showScore && hasScore){
                        Box(
                            modifier = Modifier
                                .background(
                                    contentColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$scoreA",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = contentColor
                            )
                        }
                    }

                    if (showElo && match.teamAAverageElo != null) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = contentColor.copy(alpha = 0.8f)
                            )
                            Text(
                                EloCalculator.formatElo(match.teamAAverageElo),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        teamANames,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    "VS",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(stringResource(R.string.team_b), fontWeight = FontWeight.Bold)
                    if (showScore && hasScore) {
                        Box(
                            modifier = Modifier
                                .background(
                                    contentColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$scoreB",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = contentColor
                            )
                        }
                    }

                    if (showElo && match.teamBAverageElo != null) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = contentColor.copy(alpha = 0.8f)
                            )
                            Text(
                                EloCalculator.formatElo(match.teamBAverageElo),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        teamBNames,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- TELA DE FAQ / AJUDA ---
@Composable
fun FAQScreen() {
    val faqItems = listOf(
        stringResource(R.string.faq_q1) to stringResource(R.string.faq_a1),
        stringResource(R.string.faq_q10) to stringResource(R.string.faq_a10),
        stringResource(R.string.faq_q2) to stringResource(R.string.faq_a2),
        stringResource(R.string.faq_q3) to stringResource(R.string.faq_a3),
        stringResource(R.string.faq_q4) to stringResource(R.string.faq_a4),
        stringResource(R.string.faq_q5) to stringResource(R.string.faq_a5),
        stringResource(R.string.faq_q7) to stringResource(R.string.faq_a7),
        stringResource(R.string.faq_q6) to stringResource(R.string.faq_a6),
        stringResource(R.string.faq_q8) to stringResource(R.string.faq_a8),
        stringResource(R.string.faq_q9) to stringResource(R.string.faq_a9)
    )
    var expandedIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.faq_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        faqItems.forEachIndexed { index, (question, answer) ->
            FAQItem(
                question = question,
                answer = answer,
                isExpanded = expandedIndex == index,
                onClick = {
                    expandedIndex = if (expandedIndex == index) null else index
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun FAQItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "FaqArrowRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotation)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// --- TELA SOBRE ---
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // ========== SEÇÃO 1: SOBRE O APLICATIVO ==========
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.about_app_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    stringResource(R.string.about_app_desc),
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/WkE1Dd7X8emHMid66"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.send_feedback), fontWeight = FontWeight.Bold)
                }
            }
        }

        // ========== SEÇÃO 2: SOBRE O CÓDIGO ==========
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.about_code),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    stringResource(R.string.about_code_desc),
                    style = MaterialTheme.typography.bodyMedium
                )

                val readmeUrl = stringResource(R.string.readme_url)

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, readmeUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(stringResource(R.string.project_docs), fontWeight = FontWeight.Bold)
                }
            }
        }

        // ========== SEÇÃO 3: SOBRE O DESENVOLVEDOR ==========
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.about_dev),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    stringResource(R.string.about_dev_desc),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Foto de perfil circular
                    val imageResId = remember {
                        try {
                            R.drawable.foto_perfil_desenvolvedor
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (imageResId != null) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = "Bruno Bismarck",
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .padding(2.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback if image not found
                        Surface(
                            modifier = Modifier
                                .size(80.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Pílula do Instagram
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://www.instagram.com/bismarckbruno/".toUri())
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        val instagramIcon = remember {
                            androidx.compose.ui.graphics.vector.ImageVector.Builder(
                                name = "Instagram",
                                defaultWidth = 20.dp,
                                defaultHeight = 20.dp,
                                viewportWidth = 24f,
                                viewportHeight = 24f
                            ).apply {
                                path(
                                    fill = null,
                                    stroke = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.Black),
                                    strokeLineWidth = 2f,
                                    strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
                                ) {
                                    moveTo(7f, 2f)
                                    lineTo(17f, 2f)
                                    arcToRelative(5f, 5f, 0f, false, true, 5f, 5f)
                                    lineTo(22f, 17f)
                                    arcToRelative(5f, 5f, 0f, false, true, -5f, 5f)
                                    lineTo(7f, 22f)
                                    arcToRelative(5f, 5f, 0f, false, true, -5f, -5f)
                                    lineTo(2f, 7f)
                                    arcToRelative(5f, 5f, 0f, false, true, 5f, -5f)
                                    close()
                                    moveTo(16f, 12f)
                                    arcToRelative(4f, 4f, 0f, false, true, -4f, 4f)
                                    arcToRelative(4f, 4f, 0f, false, true, -4f, -4f)
                                    arcToRelative(4f, 4f, 0f, false, true, 4f, -4f)
                                    arcToRelative(4f, 4f, 0f, false, true, 4f, 4f)
                                    close()
                                    moveTo(17.5f, 6.5f)
                                    lineTo(17.51f, 6.5f)
                                }
                            }.build()
                        }
                        Icon(
                            imageVector = instagramIcon,
                            contentDescription = "Instagram",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "bismarckbruno",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier)
    }
}

@Composable
fun ExportableImageContent(
    matches: List<MatchHistory>?,
    matchSortMode: MatchSortMode?,
    players: List<HistoryPlayerInfo>?,
    playerSortMode: PlayerSortMode?,
    groupName: String,
    date: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean,
    matchDurationsMinutes: Map<Int, Int>? = null,
    averagePlayersEloText: String? = null,
    averageMatchDurationText: String? = null
) {
    Column(
        modifier = Modifier
            .width(400.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 40.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val exportLogoRes = if (isDarkTheme) {
                R.drawable.bola_volei_fundo_escuro
            } else {
                R.drawable.logo_volei_manager
            }
            androidx.compose.foundation.Image(
                painter = painterResource(id = exportLogoRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else com.bismarck.voleimanager.app.ui.theme.voleiManagerBlue
            )
        }

        val displayGroupName = getDisplayGroupName(groupName)
        val localizedGroupTitle = stringResource(R.string.export_history_group_title, displayGroupName)
        val title = "$localizedGroupTitle - ${formatLocalizedDate(date)}"
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        val sortLabel = when {
            matches != null -> when (matchSortMode) {
                MatchSortMode.NEWEST -> stringResource(R.string.sort_newest_first)
                MatchSortMode.OLDEST -> stringResource(R.string.sort_oldest_first)
                MatchSortMode.ELO_DELTA -> stringResource(R.string.sort_highest_elo_change)
                MatchSortMode.SCORE_DIFF -> stringResource(R.string.sort_largest_score_difference)
                else -> ""
            }
            players != null -> when (playerSortMode) {
                PlayerSortMode.ELO -> stringResource(R.string.sort_highest_elo)
                PlayerSortMode.GAMES -> stringResource(R.string.sort_most_matches)
                PlayerSortMode.VICTORIES -> stringResource(R.string.sort_most_victories)
                PlayerSortMode.PERCENTAGE -> stringResource(R.string.sort_highest_percentage)
                PlayerSortMode.PLAYED_TIME -> stringResource(R.string.sort_most_played_time)
                PlayerSortMode.ALPHABETICAL -> stringResource(R.string.sort_alphabetical_order)
                else -> ""
            }
            else -> ""
        }
        
        if (sortLabel.isNotEmpty()) {
            Text(
                text = sortLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = if (players != null && !showElo) Modifier.padding(bottom = 8.dp) else Modifier.padding()
            )
        }
        
        if (players != null && showElo && averagePlayersEloText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.average_elo, averagePlayersEloText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (matches != null && averageMatchDurationText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.average_duration, averageMatchDurationText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        matches?.forEach { match ->
            val duration = matchDurationsMinutes?.get(match.id)
            HistoryItem(match = match, isDarkTheme = isDarkTheme, showElo = showElo, showScore = showScore, durationMinutes = duration)
        }
        
        val isSortedByElo = playerSortMode != PlayerSortMode.ALPHABETICAL
        players?.forEachIndexed { index, info ->
            HistoryPlayerCard(
                rank = if (isSortedByElo) index + 1 else null,
                player = info.player,
                displayElo = info.displayElo,
                showElo = showElo,
                gamesPlayed = info.gamesPlayed,
                victories = info.victories,
                playedMinutes = info.playedMinutes
            )
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = composed {
    LocalDensity.current
    this.size((20 * scale).dp)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    com.bismarck.voleimanager.app.ui.theme.AppTheme {
        Surface {
            AboutScreen()
        }
    }
}

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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Scoreboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.ui.components.PlayerPositionBadges
import com.bismarck.voleimanager.app.ui.components.RoundedSearchTextField
import com.bismarck.voleimanager.app.ui.components.groupTypeIcon
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
    val playedMinutes: Int,
    val isDeleted: Boolean = false
)

enum class PlayerSortMode { ALPHABETICAL, ELO, PLAYED_TIME, GAMES, VICTORIES, PERCENTAGE }
enum class MatchSortMode { NEWEST, OLDEST, ELO_DELTA, SCORE_DIFF }

internal data class PlayerIdentifier(val id: Int?, val name: String)
private val historyDiacriticsRegex = Regex("\\p{M}+")

@Composable
private fun SortModeIcon(
    matchSortMode: MatchSortMode? = null,
    playerSortMode: PlayerSortMode? = null,
    modifier: Modifier = Modifier,
    tint: Color
) {
    when {
        matchSortMode != null -> when (matchSortMode) {
            MatchSortMode.NEWEST -> Icon(
                imageVector = ImageVector.vectorResource(R.drawable.arrowup),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            MatchSortMode.OLDEST -> Icon(
                imageVector = ImageVector.vectorResource(R.drawable.arrowdown),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            MatchSortMode.ELO_DELTA -> Icon(
                imageVector = ImageVector.vectorResource(R.drawable.plus_minus),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            MatchSortMode.SCORE_DIFF -> Icon(
                imageVector = Icons.Outlined.Scoreboard,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
        }
        playerSortMode != null -> when (playerSortMode) {
            PlayerSortMode.ALPHABETICAL -> Icon(
                imageVector = Icons.Default.SortByAlpha,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            PlayerSortMode.ELO -> Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            PlayerSortMode.PLAYED_TIME -> Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            PlayerSortMode.GAMES -> Icon(
                imageVector = ImageVector.vectorResource(R.drawable.volei_manager_icon),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            PlayerSortMode.VICTORIES -> Icon(
                imageVector = ImageVector.vectorResource(R.drawable.crown_icon),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
            PlayerSortMode.PERCENTAGE -> Icon(
                imageVector = Icons.Default.Percent,
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
        }
    }
}

internal fun parseTeamIdentifiers(teamNamesRaw: String, teamIdsRaw: String): List<PlayerIdentifier> {
    val names = teamNamesRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val ids = if (teamIdsRaw.isBlank()) {
        emptyList()
    } else {
        teamIdsRaw.split(",").map { it.trim().toIntOrNull() }
    }
    return names.mapIndexed { index, name -> PlayerIdentifier(ids.getOrNull(index), name) }
}

internal fun canonicalHistoryName(name: String): String {
    return java.text.Normalizer.normalize(name.trim(), java.text.Normalizer.Form.NFD)
        .replace(historyDiacriticsRegex, "")
        .lowercase(Locale.ROOT)
}

internal fun resolveHistoryPlayer(
    identifier: PlayerIdentifier,
    playersById: Map<Int, Player>,
    playersByCanonicalName: Map<String, Player>,
    canonicalIdentifierName: String = canonicalHistoryName(identifier.name)
): Player? {
    val byId = identifier.id?.let { playersById[it] }
    val byName = playersByCanonicalName[canonicalIdentifierName]
    if (byId == null) return byName
    return if (canonicalHistoryName(byId.name) == canonicalIdentifierName) byId else byName ?: byId
}

internal fun buildUniqueHistoryIdentifiers(allIdentifiers: List<PlayerIdentifier>): List<PlayerIdentifier> {
    val uniquePlayerIdentifiers = mutableListOf<PlayerIdentifier>()
    val seenIds = mutableSetOf<Int>()
    val indexByCanonicalName = mutableMapOf<String, Int>()

    allIdentifiers.forEach { identifier ->
        val canonicalName = canonicalHistoryName(identifier.name)
        val existingIndexByName = indexByCanonicalName[canonicalName]
        if (existingIndexByName != null) {
            val existing = uniquePlayerIdentifiers[existingIndexByName]
            if (existing.id == null && identifier.id != null) {
                uniquePlayerIdentifiers[existingIndexByName] = identifier
                seenIds.add(identifier.id)
            }
            return@forEach
        }

        if (identifier.id != null) {
            if (seenIds.contains(identifier.id)) {
                val fallbackIdentifier = identifier.copy(id = null)
                uniquePlayerIdentifiers.add(fallbackIdentifier)
                indexByCanonicalName[canonicalName] = uniquePlayerIdentifiers.lastIndex
                return@forEach
            }
            seenIds.add(identifier.id)
        }

        uniquePlayerIdentifiers.add(identifier)
        indexByCanonicalName[canonicalName] = uniquePlayerIdentifiers.lastIndex
    }
    return uniquePlayerIdentifiers
}

private data class HistoryComputationResult(
    val sortedHistory: List<MatchHistory>,
    val matchDurationsMinutes: Map<Int, Int>,
    val averageMatchDurationMinutes: Int?,
    val uniquePlayerCount: Int,
    val historyPlayerList: List<HistoryPlayerInfo>,
    val averagePlayersEloText: String?
)

private data class HistoryFilterIndex(
    val playerOptions: List<String>,
    val playerDateCounts: Map<String, Int>,
    val datesByCanonicalPlayer: Map<String, Set<String>>,
    val canonicalPlayersByMatchId: Map<Int, Set<String>>
)

private fun computeHistoryComputation(
    groupHistory: List<MatchHistory>,
    historyDate: String?,
    historyPlayerFilter: String?,
    restrictToDates: List<String>?,
    matchSortMode: MatchSortMode,
    groupPlayers: List<Player>,
    eloLogs: List<com.bismarck.voleimanager.app.data.model.PlayerEloLog>,
    playerSortMode: PlayerSortMode
): HistoryComputationResult {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val restrictedHistory = if (restrictToDates == null) {
        groupHistory
    } else {
        val allowedDates = restrictToDates.toSet()
        groupHistory.filter { match -> allowedDates.any { match.date.startsWith(it) } }
    }
    val filteredHistory = restrictedHistory.filter { historyDate == null || it.date.startsWith(historyDate) }
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

    val participantsByMatchId = sortedHistory.associate { match ->
        match.id to (parseTeamIdentifiers(match.teamA, match.teamAIds) + parseTeamIdentifiers(match.teamB, match.teamBIds))
    }

    val allIdentifiers = participantsByMatchId.values.flatten()

    val uniquePlayerIdentifiers = buildUniqueHistoryIdentifiers(allIdentifiers)

    val playedMinutesByIdentifier = mutableMapOf<PlayerIdentifier, Int>()
    sortedHistory.forEach { match ->
        val duration = matchDurationsMinutes[match.id] ?: 0
        if (duration <= 0) return@forEach
        val matchIdentifiers = participantsByMatchId[match.id].orEmpty().toSet()
        matchIdentifiers.forEach { identifier ->
            playedMinutesByIdentifier[identifier] = (playedMinutesByIdentifier[identifier] ?: 0) + duration
        }
    }

    val playersById = groupPlayers.associateBy { it.id }
    val playersByCanonicalName = groupPlayers.associateBy { canonicalHistoryName(it.name) }
    val eloDateStr = historyDate?.let {
        try {
            val parts = it.split("/")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
        } catch (_: Exception) {
            null
        }
    }
    val restrictedEloDates = when {
        eloDateStr != null -> setOf(eloDateStr)
        restrictToDates != null -> restrictToDates.mapNotNull { date ->
            try {
                val parts = date.split("/")
                if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
            } catch (_: Exception) {
                null
            }
        }.toSet()
        else -> null
    }
    val filteredLogs = when {
        restrictedEloDates != null -> eloLogs.filter { it.date in restrictedEloDates }
        else -> eloLogs
    }
    val logsByPlayerId = filteredLogs.groupBy { it.playerId }
    val logsByCanonicalName = filteredLogs.groupBy { canonicalHistoryName(it.playerNameSnapshot) }

    val playerDataList = uniquePlayerIdentifiers.map { identifier ->
        val canonicalIdentifierName = canonicalHistoryName(identifier.name)
        val player = resolveHistoryPlayer(
            identifier = identifier,
            playersById = playersById,
            playersByCanonicalName = playersByCanonicalName,
            canonicalIdentifierName = canonicalIdentifierName
        )
        val logsForPlayer = when {
            player != null -> logsByPlayerId[player.id].orEmpty()
            else -> logsByCanonicalName[canonicalIdentifierName].orEmpty()
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
            playedMinutes = playedMinutesByIdentifier[identifier] ?: 0,
            isDeleted = player == null
        )
    }

    fun HistoryPlayerInfo.winRate(): Double = if (gamesPlayed > 0) victories.toDouble() / gamesPlayed else 0.0

    val sortedPlayers = when (playerSortMode) {
        PlayerSortMode.ALPHABETICAL -> playerDataList.sortedWith(
            compareBy<HistoryPlayerInfo> { it.player.name.lowercase() }
                .thenByDescending { it.winRate() }
                .thenByDescending { it.gamesPlayed }
                .thenByDescending { it.displayElo }
        )
        PlayerSortMode.ELO -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.displayElo }
                .thenByDescending { it.winRate() }
                .thenByDescending { it.gamesPlayed }
        )
        PlayerSortMode.PLAYED_TIME -> playerDataList.sortedWith(
            compareByDescending<HistoryPlayerInfo> { it.playedMinutes }
                .thenByDescending { it.winRate() }
                .thenByDescending { it.gamesPlayed }
                .thenByDescending { it.displayElo }
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
                .thenByDescending { it.gamesPlayed }
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

@Composable
private fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        if (configuration.locales.isEmpty) Locale.ROOT else configuration.locales[0]
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
    val groupHistory by viewModel.currentGroupHistory.collectAsState()
    val historyDate by viewModel.historyDateFilter.collectAsState()
    val availableDates by viewModel.availableHistoryDates.collectAsState()
    val eloLogs by viewModel.currentGroupEloLogs.collectAsState()
    val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val usesPositions = groupConfig.type.usesPositions

    var historyPlayerFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var showHistoryPlayerDialog by remember { mutableStateOf(false) }
    var tempHistoryPlayerFilter by remember { mutableStateOf<String?>(null) }
    var historyPlayerSearchQuery by remember { mutableStateOf("") }
    val historyFilterIndex = remember(groupHistory) {
        val allIdentifiers = mutableListOf<PlayerIdentifier>()
        val datesByCanonical = mutableMapOf<String, MutableSet<String>>()
        val canonicalByMatchId = mutableMapOf<Int, Set<String>>()

        groupHistory.forEach { match ->
            val participants = parseTeamIdentifiers(match.teamA, match.teamAIds) + parseTeamIdentifiers(match.teamB, match.teamBIds)
            val canonicalParticipants = participants.map { canonicalHistoryName(it.name) }
                .filter { it.isNotBlank() }
                .toSet()
            val dateDay = match.date.substringBefore(' ')

            allIdentifiers.addAll(participants)
            canonicalByMatchId[match.id] = canonicalParticipants
            canonicalParticipants.forEach { canonical ->
                datesByCanonical.getOrPut(canonical) { mutableSetOf() }.add(dateDay)
            }
        }

        HistoryFilterIndex(
            playerOptions = buildUniqueHistoryIdentifiers(allIdentifiers)
                .map { it.name }
                .sortedBy { it.lowercase(Locale.getDefault()) },
            playerDateCounts = datesByCanonical.mapValues { it.value.size },
            datesByCanonicalPlayer = datesByCanonical.mapValues { it.value.toSet() },
            canonicalPlayersByMatchId = canonicalByMatchId
        )
    }
    val historyPlayerFilterOptions = historyFilterIndex.playerOptions
    val historyPlayerDateCounts = historyFilterIndex.playerDateCounts
    val activeHistoryPlayerFilter = historyPlayerFilter?.trim()?.ifBlank { null }
    val activeHistoryPlayerFilterCanonical = activeHistoryPlayerFilter?.let(::canonicalHistoryName)
    val selectedHistoryDate = historyDate

    val dateOptionsForPlayer = remember(availableDates, activeHistoryPlayerFilterCanonical, historyFilterIndex) {
        val targetPlayer = activeHistoryPlayerFilter.orEmpty()
        if (targetPlayer.isBlank()) {
            availableDates
        } else {
            val datesWithPlayer = historyFilterIndex.datesByCanonicalPlayer[activeHistoryPlayerFilterCanonical].orEmpty()
            availableDates.filter { date -> datesWithPlayer.contains(date) }
        }
    }

    val restrictHistoryToDates = if (activeHistoryPlayerFilter != null && selectedHistoryDate == null) dateOptionsForPlayer else null

    val visibleHistoryDateLabel = when {
        activeHistoryPlayerFilter == null -> selectedHistoryDate?.let { formatLocalizedDate(it) } ?: stringResource(R.string.all_dates)
        selectedHistoryDate == null -> stringResource(R.string.all_dates_with_player, activeHistoryPlayerFilter)
        else -> formatLocalizedDate(selectedHistoryDate)
    }

    LaunchedEffect(showHistoryPlayerDialog) {
        if (showHistoryPlayerDialog) {
            tempHistoryPlayerFilter = historyPlayerFilter
            historyPlayerSearchQuery = historyPlayerFilter.orEmpty()
        }
    }

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
    LaunchedEffect(groupHistory, historyDate, historyPlayerFilter, restrictHistoryToDates, matchSortMode, groupPlayers, eloLogs, playerSortMode) {
        isComputingHistory = true
        try {
            historyComputation = withContext(Dispatchers.Default) {
                computeHistoryComputation(
                    groupHistory = groupHistory,
                    historyDate = historyDate,
                    historyPlayerFilter = historyPlayerFilter,
                    restrictToDates = restrictHistoryToDates,
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

    fun isActiveFilteredPlayer(playerName: String): Boolean {
        val canonical = activeHistoryPlayerFilterCanonical ?: return false
        return canonicalHistoryName(playerName) == canonical
    }

    if (showHistoryPlayerDialog) {
        HistoryPlayerFilterDialog(
            playerNames = historyPlayerFilterOptions,
            playerDateCounts = historyPlayerDateCounts,
            initialSelection = tempHistoryPlayerFilter,
            searchQuery = historyPlayerSearchQuery,
            onSearchQueryChange = { historyPlayerSearchQuery = it },
            onDismiss = {
                showHistoryPlayerDialog = false
                historyPlayerSearchQuery = ""
            },
            onClear = {
                historyPlayerFilter = null
                tempHistoryPlayerFilter = null
                viewModel.setHistoryDateFilter(null)
                showHistoryPlayerDialog = false
                historyPlayerSearchQuery = ""
            },
            onConfirm = { selected ->
                historyPlayerFilter = selected
                tempHistoryPlayerFilter = selected
                viewModel.setHistoryDateFilter(null)
                showHistoryPlayerDialog = false
                historyPlayerSearchQuery = ""
            }
        )
    }

    // --- Compute layout mode once for all player cards ---
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val locale = currentLocale()
    val nameTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
    val statsTextStyle = MaterialTheme.typography.bodySmall
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.roundToPx() }
    // Overhead: 16dp*2 (Column padding) + 12dp*2 (Card padding) + 28dp (rank) + 8dp (spacer) + 12dp (gap)
    val contentOverheadPx = with(density) { 104.dp.roundToPx() }
    val availableContentPx = screenWidthPx - contentOverheadPx
    val minNamePx = with(density) { 80.dp.roundToPx() }
    val iconBodyMediumPx = with(density) { MaterialTheme.typography.bodyMedium.fontSize.toDp().roundToPx() }
    val iconBodySmallPx = with(density) { MaterialTheme.typography.bodySmall.fontSize.toDp().roundToPx() }
    val rowGapPx = with(density) { 12.dp.roundToPx() }
    val starGapPx = with(density) { 2.dp.roundToPx() }
    val smallSpacingPx = with(density) { 4.dp.roundToPx() }
    val tinySpacingPx = with(density) { 2.dp.roundToPx() }
    val iconSpacingPx = with(density) { 12.dp.roundToPx() }

    val playersSideBySide = remember(
        historyPlayerList,
        availableContentPx,
        showElo,
        locale,
        iconBodyMediumPx,
        iconBodySmallPx,
        usesPositions
    ) {
        if (historyPlayerList.isEmpty() || availableContentPx <= 0) true
        else historyPlayerList.all { info ->
            val badgeCount = if (usesPositions) {
                listOfNotNull(info.player.preferredPosition, info.player.secondaryPosition).size
            } else 0
            val nameW = textMeasurer.measure(info.player.name, nameTextStyle).size.width +
                if (usesPositions) {
                    if (badgeCount > 0) badgeCount * (starGapPx + iconBodyMediumPx) else 0
                } else if (info.player.isPriority) starGapPx + iconBodyMediumPx else 0

            val eloW = if (showElo) {
                iconBodyMediumPx +
                    smallSpacingPx +
                    textMeasurer.measure(EloCalculator.formatElo(info.displayElo), statsTextStyle).size.width
            } else 0

            val playedTimeW = iconBodySmallPx +
                smallSpacingPx +
                textMeasurer.measure(
                formatPlayedDuration(info.playedMinutes),
                statsTextStyle
            ).size.width

            val victoriesGamesW = iconBodyMediumPx +
                tinySpacingPx +
                textMeasurer.measure(info.victories.toString(), statsTextStyle).size.width +
                iconSpacingPx +
                iconBodyMediumPx +
                tinySpacingPx +
                textMeasurer.measure(info.gamesPlayed.toString(), statsTextStyle).size.width

            val percentageFormatted = NumberFormat.getInstance(locale).apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(if (info.gamesPlayed > 0) info.victories.toDouble() / info.gamesPlayed * 100.0 else 0.0)
            val percentageW = textMeasurer.measure(percentageFormatted, statsTextStyle).size.width + iconBodyMediumPx

            val topRowRequired = nameW + rowGapPx + victoriesGamesW
            val bottomRowRequired = playedTimeW + rowGapPx + percentageW
            val widestRowRequired = maxOf(topRowRequired, eloW, bottomRowRequired)

            (widestRowRequired <= availableContentPx) && (availableContentPx - victoriesGamesW >= minNamePx)
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val containerHeightPx = LocalWindowInfo.current.containerSize.height
    val containerHeightDp = with(density) { containerHeightPx.toDp() }
    val maxMenuHeight = containerHeightDp * if (isLandscape) 0.33f else 0.57f

    val highlightedMatchIds = remember(sortedHistory, activeHistoryPlayerFilterCanonical, historyFilterIndex) {
        val canonicalFilter = activeHistoryPlayerFilterCanonical ?: return@remember emptySet()
        sortedHistory
            .asSequence()
            .filter { match ->
                historyFilterIndex.canonicalPlayersByMatchId[match.id]?.contains(canonicalFilter) == true
            }
            .map { it.id }
            .toSet()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {

        if (isLandscape) {
            // --- Landscape: everything in one scrollable LazyColumn ---
            if (historyComputation == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date filter dropdown
                        item {
                            var dateExpanded2 by remember { mutableStateOf(false) }
                            val activePersonFilterText = activeHistoryPlayerFilter
                            val dateOptions = if (activePersonFilterText == null) availableDates else dateOptionsForPlayer
                            val allDatesSelected2 = historyDate == null
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedIconButton(
                                    onClick = { showHistoryPlayerDialog = true },
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    border = BorderStroke(
                                        if (activePersonFilterText != null) 2.dp else 1.dp,
                                        if (activePersonFilterText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    colors = IconButtonDefaults.outlinedIconButtonColors(
                                        contentColor = if (activePersonFilterText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = stringResource(R.string.player), modifier = Modifier.size(24.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                ExposedDropdownMenuBox(
                                    expanded = dateExpanded2,
                                    onExpandedChange = { dateExpanded2 = !dateExpanded2 },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { dateExpanded2 = true },
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier
                                            .menuAnchor(
                                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                                enabled = true
                                            )
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 12.dp)
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.width(8.dp))
                                        Text(visibleHistoryDateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.weight(1f))
                                        val rotation2 by animateFloatAsState(targetValue = if (dateExpanded2) 180f else 0f, animationSpec = tween(200), label = "DateRot2")
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.rotate(rotation2))
                                    }
                                    DropdownMenu(
                                        expanded = dateExpanded2,
                                        onDismissRequest = { dateExpanded2 = false },
                                        offset = DpOffset(x = 36.dp, y = 0.dp),
                                        modifier = Modifier.heightIn(max = maxMenuHeight).width(IntrinsicSize.Min)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        if (activePersonFilterText == null) stringResource(R.string.all_dates) else stringResource(R.string.all_dates_with_player, activePersonFilterText),
                                                        fontWeight = if (allDatesSelected2) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (allDatesSelected2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(Modifier.weight(1f))
                                                    if (allDatesSelected2) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                                                }
                                            },
                                            onClick = {
                                                viewModel.setHistoryDateFilter(null)
                                                dateExpanded2 = false
                                            }
                                        )
                                        dateOptions.forEach { date ->
                                            val isSel = historyDate == date
                                            DropdownMenuItem(
                                                text = {
                                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                        Text(formatLocalizedDate(date), fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                                        Spacer(Modifier.weight(1f))
                                                        if (isSel) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.setHistoryDateFilter(date)
                                                    dateExpanded2 = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                            // Tab selector + sort button
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f).height(48.dp)) {
                                    SegmentedButton(
                                        modifier = Modifier.fillMaxHeight(),
                                        selected = selectedTab == 0,
                                        onClick = { onTabChanged(0) },
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                        icon = { }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Crossfade(targetState = selectedTab == 0, label = "tabIconL0") { isSelected ->
                                                if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                                                else Icon(painter = painterResource(id = R.drawable.volei_manager_icon), null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            val matchLabel = if (sortedHistory.size == 1) stringResource(R.string.match) else stringResource(R.string.matches)
                                            Text("${sortedHistory.size} $matchLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    SegmentedButton(
                                        modifier = Modifier.fillMaxHeight(),
                                        selected = selectedTab == 1,
                                        onClick = { onTabChanged(1) },
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                        icon = { }
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Crossfade(targetState = selectedTab == 1, label = "tabIconL1") { isSelected ->
                                                if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                                                else Icon(Icons.Default.Groups, null, modifier = Modifier.size(SegmentedButtonDefaults.IconSize))
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            val playerLabel = if (uniquePlayerCount == 1) stringResource(R.string.player) else stringResource(R.string.players)
                                            Text("$uniquePlayerCount $playerLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                val activeSortBadgeIcon = when {
                                    selectedTab == 0 -> when (matchSortMode) {
                                        MatchSortMode.NEWEST -> ImageVector.vectorResource(R.drawable.arrowup)
                                        MatchSortMode.OLDEST -> ImageVector.vectorResource(R.drawable.arrowdown)
                                        MatchSortMode.ELO_DELTA -> ImageVector.vectorResource(R.drawable.plus_minus)
                                        MatchSortMode.SCORE_DIFF -> Icons.Outlined.Scoreboard
                                    }
                                    else -> when (playerSortMode) {
                                        PlayerSortMode.ALPHABETICAL -> Icons.Default.SortByAlpha
                                        PlayerSortMode.ELO -> Icons.Default.WorkspacePremium
                                        PlayerSortMode.PLAYED_TIME -> Icons.Default.AccessTime
                                        PlayerSortMode.GAMES -> ImageVector.vectorResource(R.drawable.volei_manager_icon)
                                        PlayerSortMode.VICTORIES -> ImageVector.vectorResource(R.drawable.crown_icon)
                                        PlayerSortMode.PERCENTAGE -> Icons.Default.Percent
                                    }
                                }
                                Box {
                                    OutlinedIconButton(
                                        onClick = { expandedFilter = true },
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = stringResource(R.string.sort_word),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.outline, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = activeSortBadgeIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedFilter,
                                        onDismissRequest = { expandedFilter = false },
                                        offset = DpOffset(x = 0.dp, y = 4.dp),
                                        modifier = Modifier.widthIn(min = 260.dp)
                                    ) {
                                        if (selectedTab == 0) {
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = matchSortMode == MatchSortMode.NEWEST, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.newest_first)); Spacer(Modifier.weight(1f)); Icon(ImageVector.vectorResource(R.drawable.arrowup), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onMatchSortModeChanged(MatchSortMode.NEWEST); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = matchSortMode == MatchSortMode.OLDEST, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.oldest_first)); Spacer(Modifier.weight(1f)); Icon(ImageVector.vectorResource(R.drawable.arrowdown), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onMatchSortModeChanged(MatchSortMode.OLDEST); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = matchSortMode == MatchSortMode.ELO_DELTA, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_elo_change)); Spacer(Modifier.weight(1f)); Icon(ImageVector.vectorResource(R.drawable.plus_minus), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onMatchSortModeChanged(MatchSortMode.ELO_DELTA); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = matchSortMode == MatchSortMode.SCORE_DIFF, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_score_diff)); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.Scoreboard, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onMatchSortModeChanged(MatchSortMode.SCORE_DIFF); expandedFilter = false })
                                        } else {
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.ALPHABETICAL, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.alphabetical)); Spacer(Modifier.weight(1f)); Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.ALPHABETICAL); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.ELO, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_elo)); Spacer(Modifier.weight(1f)); Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.ELO); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.PLAYED_TIME, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_played_time)); Spacer(Modifier.weight(1f)); Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.PLAYED_TIME); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.GAMES, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_matches)); Spacer(Modifier.weight(1f)); Icon(ImageVector.vectorResource(R.drawable.volei_manager_icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.GAMES); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.VICTORIES, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_victories)); Spacer(Modifier.weight(1f)); Icon(ImageVector.vectorResource(R.drawable.crown_icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.VICTORIES); expandedFilter = false })
                                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = playerSortMode == PlayerSortMode.PERCENTAGE, onClick = null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.by_percentage)); Spacer(Modifier.weight(1f)); Icon(Icons.Default.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }, onClick = { onPlayerSortModeChanged(PlayerSortMode.PERCENTAGE); expandedFilter = false })
                                        }
                                    }
                                }
                            }
                        }

                        // Content: matches or players based on selected tab
                        if (selectedTab == 0) {
                            if (sortedHistory.isNotEmpty()) {
                                item {
                                    val avgDurationText = averageMatchDurationMinutes?.let { "${it}min" } ?: "--"
                                    HistorySummaryItem(text = stringResource(R.string.average_duration, avgDurationText), leadingIcon = Icons.Default.AccessTime)
                                }
                            }
                            items(sortedHistory, key = { it.id }) { match ->
                                HistoryItem(
                                    match = match,
                                    isDarkTheme = isDarkTheme,
                                    showElo = showElo,
                                    showScore = showScore,
                                    durationMinutes = matchDurationsMinutes[match.id],
                                    highlightFilteredPlayer = highlightedMatchIds.contains(match.id),
                                    highlightedPlayerName = activeHistoryPlayerFilter
                                )
                            }
                            if (sortedHistory.isEmpty()) item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text(stringResource(R.string.no_matches_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            if (historyPlayerList.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text(stringResource(R.string.no_players), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                item {
                                    HistorySummaryItem(text = stringResource(R.string.average_elo, averagePlayersEloText ?: "--"), leadingIcon = Icons.Default.WorkspacePremium)
                                }
                                itemsIndexed(items = historyPlayerList, key = { _, info -> "${info.player.id}_${info.name}" }) { index, info ->
                                    HistoryPlayerCard(
                                        rank = if (playerSortMode != PlayerSortMode.ALPHABETICAL) index + 1 else null,
                                        player = info.player,
                                        displayElo = info.displayElo,
                                        showElo = showElo,
                                        gamesPlayed = info.gamesPlayed,
                                        victories = info.victories,
                                        playedMinutes = info.playedMinutes,
                                        useSideBySide = playersSideBySide,
                                        isDeleted = info.isDeleted,
                                        playerSortMode = playerSortMode,
                                        highlightFilterBorder = isActiveFilteredPlayer(info.name),
                                        usesPositions = usesPositions
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                    Crossfade(targetState = isComputingHistory, label = "historyLoadingProgressL") { isVisible ->
                        if (isVisible) {
                            LinearProgressIndicator(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth())
                        }
                    }
                }
            }
        } else {

        // --- Date filter dropdown ---
        var dateExpanded by remember { mutableStateOf(false) }
        val activePersonFilterText = activeHistoryPlayerFilter
        val dateOptions = if (activePersonFilterText == null) availableDates else dateOptionsForPlayer

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = { showHistoryPlayerDialog = true },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                border = BorderStroke(
                    if (activePersonFilterText != null) 2.dp else 1.dp,
                    if (activePersonFilterText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = if (activePersonFilterText != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Person, contentDescription = stringResource(R.string.filter_player), modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(8.dp))

            ExposedDropdownMenuBox(
                expanded = dateExpanded,
                onExpandedChange = { dateExpanded = !dateExpanded },
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                OutlinedButton(
                    onClick = { dateExpanded = true },
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                        .height(48.dp),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 12.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        visibleHistoryDateLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                                    if (activePersonFilterText == null) stringResource(R.string.all_dates) else stringResource(R.string.all_dates_with_player, activePersonFilterText),
                                    fontWeight = if (allDatesSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (allDatesSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.weight(1f))
                                if (allDatesSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        onClick = { viewModel.setHistoryDateFilter(null); dateExpanded = false }
                    )
                    dateOptions.forEach { date ->
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
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.weight(1f))
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            onClick = { viewModel.setHistoryDateFilter(date); dateExpanded = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // --- Segmented button row + filter icon ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f).height(48.dp)) {
                SegmentedButton(
                    modifier = Modifier.fillMaxHeight(),
                    selected = selectedTab == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
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
                                    painter = painterResource(id = R.drawable.volei_manager_icon),
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
                    modifier = Modifier.fillMaxHeight(),
                    selected = selectedTab == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
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
            Spacer(Modifier.width(8.dp))

            // Filter/sort icon button
            val activeSortBadgeIcon = when {
                selectedTab == 0 -> when (matchSortMode) {
                    MatchSortMode.NEWEST -> ImageVector.vectorResource(R.drawable.arrowup)
                    MatchSortMode.OLDEST -> ImageVector.vectorResource(R.drawable.arrowdown)
                    MatchSortMode.ELO_DELTA -> ImageVector.vectorResource(R.drawable.plus_minus)
                    MatchSortMode.SCORE_DIFF -> Icons.Outlined.Scoreboard
                }
                else -> when (playerSortMode) {
                    PlayerSortMode.ALPHABETICAL -> Icons.Default.SortByAlpha
                    PlayerSortMode.ELO -> Icons.Default.WorkspacePremium
                    PlayerSortMode.PLAYED_TIME -> Icons.Default.AccessTime
                    PlayerSortMode.GAMES -> ImageVector.vectorResource(R.drawable.volei_manager_icon)
                    PlayerSortMode.VICTORIES -> ImageVector.vectorResource(R.drawable.crown_icon)
                    PlayerSortMode.PERCENTAGE -> Icons.Default.Percent
                }
            }
            Box {
                OutlinedIconButton(
                    onClick = { expandedFilter = true },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = stringResource(R.string.sort_word),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activeSortBadgeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false },
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    modifier = Modifier.widthIn(min = 260.dp)
                ) {
                    if (selectedTab == 0) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.NEWEST, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.newest_first))
                                    Spacer(Modifier.weight(1f))
                                    Icon(ImageVector.vectorResource(R.drawable.arrowup), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(ImageVector.vectorResource(R.drawable.arrowdown), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(ImageVector.vectorResource(R.drawable.plus_minus), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Outlined.Scoreboard, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.SortByAlpha, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.ELO); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.PLAYED_TIME, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_played_time))
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.PLAYED_TIME); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.GAMES, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.by_matches))
                                    Spacer(Modifier.weight(1f))
                                    Icon(ImageVector.vectorResource(R.drawable.volei_manager_icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(ImageVector.vectorResource(R.drawable.crown_icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.PERCENTAGE); expandedFilter = false }
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
                                    durationMinutes = matchDurationsMinutes[match.id],
                                    highlightFilteredPlayer = highlightedMatchIds.contains(match.id),
                                    highlightedPlayerName = activeHistoryPlayerFilter
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
                                        useSideBySide = playersSideBySide,
                                        isDeleted = info.isDeleted,
                                        playerSortMode = playerSortMode,
                                        highlightFilterBorder = isActiveFilteredPlayer(info.name),
                                        usesPositions = usesPositions
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
        } // end portrait else
    }
}

@Composable
private fun HistoryPlayerFilterDialog(
    playerNames: List<String>,
    playerDateCounts: Map<String, Int>,
    initialSelection: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var selectedPlayer by remember { mutableStateOf(initialSelection) }
    var sortByDates by remember { mutableStateOf(false) }

    LaunchedEffect(initialSelection) {
        selectedPlayer = initialSelection
    }

    val filteredPlayers = remember(playerNames, searchQuery, sortByDates, playerDateCounts) {
        val filtered = if (searchQuery.isBlank()) {
            playerNames
        } else {
            playerNames.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
        }
        
        if (sortByDates) {
            filtered.sortedByDescending { playerDateCounts[canonicalHistoryName(it)] ?: 0 }
        } else {
            filtered.sortedBy { it.lowercase(Locale.getDefault()) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 420.dp)
                    .heightIn(max = 560.dp)
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                    Text(
                        text = stringResource(R.string.filter_player),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundedSearchTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.search_player)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedIconButton(
                            onClick = { sortByDates = !sortByDates },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = if (sortByDates) Icons.Default.DateRange else Icons.Default.SortByAlpha,
                                contentDescription = if (sortByDates) stringResource(R.string.by_played_time) else stringResource(R.string.alphabetical)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { selectedPlayer = null }
                                .padding(start = 4.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedPlayer == null, onClick = { selectedPlayer = null })
                            Spacer(Modifier.width(2.dp))
                            Text(stringResource(R.string.all_players))
                        }
                    }
                    items(filteredPlayers) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { selectedPlayer = player }
                                .padding(start = 4.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedPlayer == player, onClick = { selectedPlayer = player })
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = player,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            val count = playerDateCounts[canonicalHistoryName(player)] ?: 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() }),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (filteredPlayers.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_players),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.clear_filter))
                    }
                    Button(
                        onClick = { onConfirm(selectedPlayer) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.apply_filter))
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryPlayerCard(
    rank: Int?,
    player: Player,
    displayElo: Double,
    showElo: Boolean,
    gamesPlayed: Int = 0,
    victories: Int = 0,
    playedMinutes: Int = 0,
    useSideBySide: Boolean = true,
    isDeleted: Boolean = false,
    playerSortMode: PlayerSortMode = PlayerSortMode.ALPHABETICAL,
    highlightFilterBorder: Boolean = false,
    usesPositions: Boolean = false
) {
    val showDeletedIndicator = isDeleted && playerSortMode == PlayerSortMode.ALPHABETICAL
    val deletedPlayerTooltip = stringResource(R.string.player_was_deleted)
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val locale = currentLocale()

    LaunchedEffect(showDeletedIndicator) {
        if (!showDeletedIndicator) {
            tooltipState.dismiss()
        }
    }

    val percentage = if (gamesPlayed > 0) {
        victories.toDouble() / gamesPlayed * 100.0
    } else 0.0
    val percentageFormatted = NumberFormat.getInstance(locale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(percentage)
    val playedTimeText = formatPlayedDuration(playedMinutes)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 120.dp)
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                PlainTooltip {
                    Text(
                        text = deletedPlayerTooltip,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            state = tooltipState,
            enableUserInput = false
        ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = CardDefaults.shape,
            border = if (highlightFilterBorder) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape)
                .combinedClickable(
                    enabled = showDeletedIndicator,
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { tooltipState.show() }
                    }
                )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .padding(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
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
                            if (showDeletedIndicator) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = deletedPlayerTooltip,
                                    modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                                    tint = MaterialTheme.colorScheme.error
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
                    }
                    Spacer(Modifier.width(8.dp))

                    if (useSideBySide) {
                        // Wide layout: name+elo left, stats right
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        player.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
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
                                }

                                VictoriesAndGamesRow(victories, gamesPlayed)
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

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
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

                                Row(verticalAlignment = Alignment.CenterVertically){
                                    Text(
                                        "$percentageFormatted",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        Icons.Default.Percent,
                                        contentDescription = null,
                                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                        }

                    } else {
                        // Narrow layout: everything stacked vertically
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    player.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
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

                            VictoriesAndGamesRow(victories, gamesPlayed)

                            Row(verticalAlignment = Alignment.CenterVertically){
                                Text(
                                    "$percentageFormatted",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.Percent,
                                    contentDescription = null,
                                    modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun VictoriesAndGamesRow(victories: Int, gamesPlayed: Int) {
    val iconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.crown_icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(2.dp))
        Text(
            victories.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            painter = painterResource(R.drawable.volei_manager_icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(2.dp))
        Text(
            gamesPlayed.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HistoryItem(
    match: MatchHistory,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean = true,
    durationMinutes: Int? = null,
    highlightFilteredPlayer: Boolean = false,
    highlightedPlayerName: String? = null
) {
    val isTeamAWin = match.winner == "A" || match.winner == "Time A"
    val teamANameList = remember(match.teamA) {
        match.teamA.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .sortedBy { it.lowercase() }
    }
    val teamBNameList = remember(match.teamB) {
        match.teamB.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .sortedBy { it.lowercase() }
    }
    val highlightedCanonicalName = remember(highlightedPlayerName) {
        highlightedPlayerName?.let(::canonicalHistoryName)
    }
    fun buildTeamNamesText(names: List<String>) = buildAnnotatedString {
        names.forEachIndexed { index, name ->
            if (index > 0) append(", ")
            val shouldHighlight = highlightedCanonicalName != null &&
                canonicalHistoryName(name) == highlightedCanonicalName
            if (shouldHighlight) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(name) }
            } else {
                append(name)
            }
        }
    }
    val teamANamesText = remember(teamANameList, highlightedCanonicalName) { buildTeamNamesText(teamANameList) }
    val teamBNamesText = remember(teamBNameList, highlightedCanonicalName) { buildTeamNamesText(teamBNameList) }

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
    val locale = currentLocale()
    val formattedDelta = remember(match.eloPoints, locale) {
        NumberFormat.getInstance(locale).apply {
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
        ),
        border = if (highlightFilteredPlayer) BorderStroke(1.dp, if (isTeamAWin) MaterialTheme.colorScheme.primary else LocalExtendedColors.current.anotherPrime.color) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxWidthPx = with(density) { maxWidth.roundToPx() }
                val dateWidthPx = textMeasurer.measure(formatLocalizedDate(match.date), style = MaterialTheme.typography.labelMedium).size.width
                val eloWidthPx = if (showElo) {
                    val iconAndSpacingPx = with(density) {
                        MaterialTheme.typography.bodySmall.fontSize.toDp().roundToPx() + 2.dp.roundToPx()
                    }
                    textMeasurer.measure(
                        formattedDelta,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    ).size.width + iconAndSpacingPx
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.plus_minus_bold),
                                contentDescription = null,
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }),
                                tint = contentColor
                            )
                            Text(
                                formattedDelta,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
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
                            painter = painterResource(R.drawable.crown_icon),
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
                            painter = painterResource(R.drawable.crown_icon),
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
                        teamANamesText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    stringResource(R.string.vs),
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
                        teamBNamesText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- TELA DE FAQ / AJUDA ---
private data class FaqEntry(
    val question: String,
    val icon: @Composable () -> Unit,
    val answer: String? = null,
    val table: FaqTableData? = null,
    /** Ação extra (ex.: botão de download) renderizada abaixo do texto/tabela. */
    val action: (@Composable () -> Unit)? = null
)

private data class FaqTableData(
    val columnHeaders: Pair<String, String>,
    val rows: List<Pair<String, String>>,
    val firstColumnWeight: Float = 0.32f
)

/** Parses a "cell1|cell2" per line string resource into table rows. */
private fun parseFaqTableRows(raw: String): List<Pair<String, String>> =
    raw.split("\n").map { line ->
        val parts = line.split("|", limit = 2)
        parts[0].trim() to parts.getOrElse(1) { "" }.trim()
    }

/** Simple leading icon wrapper so every FAQ question can show a themed icon. */
@Composable
private fun FaqQuestionIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp)
    )
}

/** Simple leading icon wrapper for drawable-based icons (custom vector assets). */
@Composable
private fun FaqQuestionIcon(iconRes: Int) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
fun FAQScreen(viewModel: VoleiViewModel? = null) {
    val context = LocalContext.current
    val faqItems = listOf(
        FaqEntry(
            stringResource(R.string.faq_q1),
            icon = { FaqQuestionIcon(Icons.Default.WorkspacePremium) },
            answer = stringResource(R.string.faq_a1)
        ),
        FaqEntry(
            stringResource(R.string.faq_q10),
            icon = { FaqQuestionIcon(R.drawable.plus_minus) },
            answer = stringResource(R.string.faq_a10)
        ),
        FaqEntry(
            stringResource(R.string.faq_q2),
            icon = { FaqQuestionIcon(Icons.Default.Star) },
            answer = stringResource(R.string.faq_a2)
        ),
        FaqEntry(
            stringResource(R.string.faq_q3),
            icon = { FaqQuestionIcon(R.drawable.volei_manager_icon) },
            answer = stringResource(R.string.faq_a3)
        ),
        FaqEntry(
            stringResource(R.string.faq_q4),
            icon = { FaqQuestionIcon(Icons.Default.Groups) },
            answer = stringResource(R.string.faq_a4)
        ),
        FaqEntry(
            stringResource(R.string.faq_q14),
            icon = { FaqQuestionIcon(groupTypeIcon(GroupType.FIXED_POSITIONS))},
            answer = stringResource(R.string.faq_a14)
        ),
        FaqEntry(
            stringResource(R.string.faq_q5),
            icon = { FaqQuestionIcon(Icons.Filled.People) },
            answer = stringResource(R.string.faq_a5)
        ),
        FaqEntry(
            stringResource(R.string.faq_q8),
            icon = { FaqQuestionIcon(Icons.Default.PersonAddAlt1) },
            answer = stringResource(R.string.faq_a8)
        ),
        FaqEntry(
            stringResource(R.string.faq_q6),
            icon = { FaqQuestionIcon(R.drawable.arrowsbothsides) },
            answer = stringResource(R.string.faq_a6)
        ),
        FaqEntry(
            stringResource(R.string.faq_q7),
            icon = { FaqQuestionIcon(R.drawable.crown_icon) },
            answer = stringResource(R.string.faq_a7)
        ),
        FaqEntry(
            stringResource(R.string.faq_q9),
            icon = { FaqQuestionIcon(Icons.Default.Edit) },
            answer = stringResource(R.string.faq_a9)
        ),
        FaqEntry(
            stringResource(R.string.faq_q11),
            icon = { FaqQuestionIcon(groupTypeIcon(GroupType.FIXED_POSITIONS)) },
            table = FaqTableData(
                columnHeaders = stringResource(R.string.faq_a11_col1) to stringResource(R.string.faq_a11_col2),
                rows = parseFaqTableRows(stringResource(R.string.faq_a11_table)),
                firstColumnWeight = 0.3f
            )
        ),
        FaqEntry(
            stringResource(R.string.faq_q13),
            icon = { FaqQuestionIcon(groupTypeIcon(GroupType.FIXED_POSITIONS)) },
            table = FaqTableData(
                columnHeaders = stringResource(R.string.faq_a13_col1) to stringResource(R.string.faq_a13_col2),
                rows = parseFaqTableRows(stringResource(R.string.faq_a13_table)),
                firstColumnWeight = 0.3f
            )
        ),
        FaqEntry(
            stringResource(R.string.faq_q12),
            icon = { FaqQuestionIcon(groupTypeIcon(GroupType.FIXED_POSITIONS)) },
            table = FaqTableData(
                columnHeaders = stringResource(R.string.faq_a12_col1) to stringResource(R.string.faq_a12_col2),
                rows = parseFaqTableRows(stringResource(R.string.faq_a12_table)),
                firstColumnWeight = 0.25f
            )
        ),
        FaqEntry(
            stringResource(R.string.faq_q15),
            icon = { FaqQuestionIcon(Icons.Default.PeopleAlt) },
            answer = stringResource(R.string.faq_a15_intro),
            table = FaqTableData(
                columnHeaders = stringResource(R.string.faq_a15_col1) to stringResource(R.string.faq_a15_col2),
                rows = parseFaqTableRows(stringResource(R.string.faq_a15_table)),
                firstColumnWeight = 0.3f
            ),
            action = if (viewModel != null) {
                {
                    Button(onClick = { viewModel.exportPlayersTemplate(context) }) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.download_players_template))
                    }
                }
            } else null
        )
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

        faqItems.forEachIndexed { index, entry ->
            FAQItem(
                question = entry.question,
                icon = entry.icon,
                isExpanded = expandedIndex == index,
                onClick = {
                    expandedIndex = if (expandedIndex == index) null else index
                }
            ) {
                if (!entry.answer.isNullOrEmpty()) {
                    Text(
                        text = entry.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.table != null) Spacer(Modifier.height(12.dp))
                }
                if (entry.table != null) {
                    FAQTable(
                        columnHeaders = entry.table.columnHeaders,
                        rows = entry.table.rows,
                        firstColumnWeight = entry.table.firstColumnWeight
                    )
                }
                entry.action?.let {
                    Spacer(Modifier.height(12.dp))
                    it()
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

/** Simple two-column table used by FAQ answers that are better presented as a reference table. */
@Composable
private fun FAQTable(
    columnHeaders: Pair<String, String>,
    rows: List<Pair<String, String>>,
    firstColumnWeight: Float = 0.3f
) {
    val secondColumnWeight = 1f - firstColumnWeight
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = columnHeaders.first,
                modifier = Modifier.weight(firstColumnWeight),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = columnHeaders.second,
                modifier = Modifier.weight(secondColumnWeight),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        rows.forEachIndexed { index, (first, second) ->
            if (index > 0) {
                HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = first,
                    modifier = Modifier.weight(firstColumnWeight),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = second,
                    modifier = Modifier.weight(secondColumnWeight),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FAQItem(
    question: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    answerContent: @Composable () -> Unit
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
            if (icon != null) {
                Box(modifier = Modifier.padding(end = 12.dp)) {
                    icon()
                }
            }
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
            Column(modifier = Modifier.padding(top = 8.dp)) {
                answerContent()
            }
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
                            modifier = Modifier.size(24.dp)
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
    averageMatchDurationText: String? = null,
    usesPositions: Boolean = false
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
                PlayerSortMode.ALPHABETICAL -> stringResource(R.string.sort_alphabetical_order)
                PlayerSortMode.ELO -> stringResource(R.string.sort_highest_elo)
                PlayerSortMode.PLAYED_TIME -> stringResource(R.string.sort_most_played_time)
                PlayerSortMode.GAMES -> stringResource(R.string.sort_most_matches)
                PlayerSortMode.VICTORIES -> stringResource(R.string.sort_most_victories)
                PlayerSortMode.PERCENTAGE -> stringResource(R.string.sort_highest_percentage)
                else -> ""
            }
            else -> ""
        }
        
        if (sortLabel.isNotEmpty()) {
            val sortIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.fontSize.toDp() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (players != null && !showElo) Modifier.padding(bottom = 8.dp) else Modifier
            ) {
                Text(
                    text = sortLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                SortModeIcon(
                    matchSortMode = if (matches != null) matchSortMode else null,
                    playerSortMode = if (players != null) playerSortMode else null,
                    modifier = Modifier.size(sortIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                playedMinutes = info.playedMinutes,
                isDeleted = info.isDeleted,
                playerSortMode = playerSortMode ?: PlayerSortMode.ALPHABETICAL,
                usesPositions = usesPositions
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

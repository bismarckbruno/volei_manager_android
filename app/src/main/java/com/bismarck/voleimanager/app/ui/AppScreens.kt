package com.bismarck.voleimanager.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.EloCalculator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class HistoryPlayerInfo(
    val player: Player,
    val displayElo: Double,
    val name: String,
    val gamesPlayed: Int,
    val victories: Int
)

enum class PlayerSortMode { ALPHABETICAL, ELO, GAMES, VICTORIES, PERCENTAGE }
enum class MatchSortMode { NEWEST, OLDEST, ELO_DELTA, SCORE_DIFF }

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
    onPlayerSortModeChanged: (PlayerSortMode) -> Unit = {}
) {
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

    val sortedHistory = remember(groupHistory, historyDate, matchSortMode) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val filtered = groupHistory.filter {
            (historyDate == null || it.date.startsWith(historyDate!!))
        }
        when (matchSortMode) {
            MatchSortMode.NEWEST -> filtered.sortedWith(
                compareByDescending<MatchHistory> {
                    try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
                }.thenByDescending { it.id }
            )
            MatchSortMode.OLDEST -> filtered.sortedWith(
                compareBy<MatchHistory> {
                    try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
                }.thenByDescending { it.id }
            )
            MatchSortMode.ELO_DELTA -> filtered.sortedWith(
                compareByDescending<MatchHistory> { it.eloPoints }
                    .thenByDescending { it.id }
            )
            MatchSortMode.SCORE_DIFF -> filtered.sortedWith(
                compareByDescending<MatchHistory> {
                    val sa = it.teamAScore ?: 0
                    val sb = it.teamBScore ?: 0
                    kotlin.math.abs(sa - sb)
                }.thenByDescending { it.id }
            )
        }
    }

    val matchDurationsMinutes = remember(sortedHistory) {
        val result = mutableMapOf<Int, Int>()
        sortedHistory.forEach { match ->
            if (match.startTimestamp != null && match.endTimestamp != null && match.endTimestamp > match.startTimestamp) {
                result[match.id] = ((match.endTimestamp - match.startTimestamp) / 60000L).toInt().coerceAtLeast(1)
            }
        }
        result
    }

    val averageMatchDurationMinutes = remember(matchDurationsMinutes) {
        if (matchDurationsMinutes.isEmpty()) null else matchDurationsMinutes.values.average().toInt()
    }

    // Unique player names from filtered history
    val uniquePlayerNames = remember(sortedHistory) {
        sortedHistory.flatMap { match ->
            (match.teamA.split(",") + match.teamB.split(","))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }.distinct()
    }

    val uniquePlayerCount = uniquePlayerNames.size

    // Build player list with Elo and stats for the selected date
    val historyPlayerList = remember(uniquePlayerNames, groupPlayers, eloLogs, historyDate, playerSortMode, sortedHistory) {
        // Convert historyDate (dd/MM/yyyy) to elo log date format (yyyy-MM-dd)
        val eloDateStr: String? = if (historyDate != null) {
            try {
                val parts = historyDate!!.split("/")
                if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
            } catch (_: Exception) { null }
        } else null

        val playerDataList = uniquePlayerNames.mapNotNull { name ->
            val player = groupPlayers.find { it.name == name }
            val logsForPlayer = if (eloDateStr != null) {
                if (player != null) eloLogs.filter { it.playerId == player.id && it.date == eloDateStr }
                else eloLogs.filter { it.playerNameSnapshot == name && it.date == eloDateStr }
            } else {
                if (player != null) eloLogs.filter { it.playerId == player.id }
                else eloLogs.filter { it.playerNameSnapshot == name }
            }
            
            val games = logsForPlayer.size
            val victories = logsForPlayer.count { it.won == true }
            val eloForDisplay = logsForPlayer.maxByOrNull { it.id }?.elo ?: (player?.elo ?: 1200.0)

            val effectivePlayer = player ?: Player(name = name, groupName = "", elo = 1200.0)
            
            HistoryPlayerInfo(
                player = effectivePlayer,
                displayElo = eloForDisplay,
                name = name,
                gamesPlayed = games,
                victories = victories
            )
        }

        fun HistoryPlayerInfo.winRate(): Double =
            if (gamesPlayed > 0) victories.toDouble() / gamesPlayed else 0.0

        when (playerSortMode) {
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
            PlayerSortMode.ALPHABETICAL -> playerDataList.sortedWith(
                compareBy<HistoryPlayerInfo> { it.player.name.lowercase() }
                    .thenByDescending { it.displayElo }
            )
        }
    }

    val averagePlayersEloText = remember(historyPlayerList) {
        if (historyPlayerList.isEmpty()) null
        else NumberFormat.getIntegerInstance(Locale.getDefault())
            .format(historyPlayerList.map { it.displayElo }.average().toInt())
    }

    var expandedDate by remember { mutableStateOf(false) }

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

    val playersSideBySide = remember(historyPlayerList, availableContentPx, showElo) {
        if (historyPlayerList.isEmpty() || availableContentPx <= 0) true
        else historyPlayerList.all { info ->
            // Left column width (name + optional star)
            val nameW = textMeasurer.measure(info.player.name, nameTextStyle).size.width +
                    (if (info.player.isPriority) with(density) { 14.dp.roundToPx() } else 0)
            val eloW = if (showElo) textMeasurer.measure(
                "Elo: ${EloCalculator.formatElo(info.displayElo)}", statsTextStyle
            ).size.width else 0
            val leftW = maxOf(nameW, eloW)

            // Right column width (stats line is always the widest)
            val vText = when (info.victories) {
                0 -> "Nenhuma vitória"; 1 -> "1 vitória"; else -> "${info.victories} vitórias"
            }
            val gLabel = if (info.gamesPlayed == 1) "jogo" else "jogos"
            val rightW = textMeasurer.measure("$vText / ${info.gamesPlayed} $gLabel", statsTextStyle).size.width

            (leftW + rightW <= availableContentPx) && (availableContentPx - rightW >= minNamePx)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {

        // --- Date filter dropdown ---
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expandedDate = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    historyDate ?: "Todas as datas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                val rotation by animateFloatAsState(
                    targetValue = if (expandedDate) 180f else 0f,
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
            DropdownMenu(expanded = expandedDate, onDismissRequest = { expandedDate = false }) {
                DropdownMenuItem(
                    text = { Text("Todas as datas") },
                    onClick = { viewModel.setHistoryDateFilter(null); expandedDate = false })
                availableDates.forEach { date ->
                    DropdownMenuItem(
                        text = { Text(date) },
                        onClick = { viewModel.setHistoryDateFilter(date); expandedDate = false })
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
                                    painter = painterResource(id = com.bismarck.voleimanager.app.R.drawable.bola_de_v_lei_s_lida_para_variar_a_cor),
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        val matchLabel = if (sortedHistory.size == 1) "partida" else "partidas"
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
                        val playerLabel = if (uniquePlayerCount == 1) "jogador" else "jogadores"
                        Text("$uniquePlayerCount $playerLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            // Filter/sort icon button
            Box {
                IconButton(onClick = { expandedFilter = true }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Ordenar",
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
                                    Text("Mais recentes primeiro")
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.NEWEST); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.OLDEST, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mais antigos primeiro")
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.OLDEST); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.ELO_DELTA, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por Elo movimentado")
                                }
                            },
                            onClick = { onMatchSortModeChanged(MatchSortMode.ELO_DELTA); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = matchSortMode == MatchSortMode.SCORE_DIFF, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por diferença de placar")
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
                                    Text("Por ordem alfabética")
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.ALPHABETICAL); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.ELO, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por Elo")
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.ELO); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.GAMES, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por número de jogos")
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.GAMES); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.VICTORIES, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por número de vitórias")
                                }
                            },
                            onClick = { onPlayerSortModeChanged(PlayerSortMode.VICTORIES); expandedFilter = false }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = playerSortMode == PlayerSortMode.PERCENTAGE, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Por porcentagem")
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
                                val avgDurationText = averageMatchDurationMinutes?.let { "$it min" } ?: "--"
                                HistorySummaryItem(
                                    text = "Duração média: $avgDurationText"
                                )
                            }
                        }
                        items(sortedHistory) { match ->
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
                                    "Nenhuma partida encontrada.",
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
                                        "Nenhum jogador encontrado.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            item {
                                HistorySummaryItem(
                                    text = "Elo médio: ${averagePlayersEloText ?: "--"}"
                                )
                            }
                            itemsIndexed(historyPlayerList) { index, info ->
                                HistoryPlayerCard(
                                    rank = if (playerSortMode != PlayerSortMode.ALPHABETICAL) index + 1 else null,
                                    player = info.player,
                                    displayElo = info.displayElo,
                                    showElo = showElo,
                                    gamesPlayed = info.gamesPlayed,
                                    victories = info.victories,
                                    useSideBySide = playersSideBySide
                                )
                            }
                        }
                        item {  }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySummaryItem(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
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
    useSideBySide: Boolean = true
) {
    val victoriesText = when (victories) {
        0 -> "Nenhuma vitória"
        1 -> "1 vitória"
        else -> "$victories vitórias"
    }
    val gamesLabel = if (gamesPlayed == 1) "jogo" else "jogos"
    val percentage = if (gamesPlayed > 0) {
        victories.toDouble() / gamesPlayed * 100.0
    } else 0.0
    val percentageFormatted = NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(percentage)

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
                        "${rank}º",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
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
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (showElo) {
                        Text(
                            "Elo: ${EloCalculator.formatElo(displayElo)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$victoriesText / $gamesPlayed $gamesLabel",
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
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (showElo) {
                        Text(
                            "Elo: ${EloCalculator.formatElo(displayElo)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "$victoriesText / $gamesPlayed $gamesLabel",
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
    val isTeamAWin = match.winner == "Time A"
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

    val starColor = if (isTeamAWin) {
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
                val dateWidthPx = textMeasurer.measure(match.date, style = MaterialTheme.typography.labelMedium).size.width
                val eloWidthPx = if (showElo) {
                    textMeasurer.measure(
                        "±$formattedDelta",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    ).size.width
                } else 0
                val durationWidthPx = if (durationMinutes != null) {
                    textMeasurer.measure(
                        "$durationMinutes min",
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
                            Text(match.date, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        contentColor.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$durationMinutes min",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = contentColor
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(match.date, style = MaterialTheme.typography.labelMedium)
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
                                    Text(
                                        text = "$durationMinutes min",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = contentColor
                                    )
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
                            Icons.Default.WorkspacePremium,
                            contentDescription = "Vencedor",
                            modifier = Modifier.size(22.dp),
                            tint = starColor
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
                            Icons.Default.WorkspacePremium,
                            contentDescription = "Vencedor",
                            modifier = Modifier.size(22.dp),
                            tint = starColor
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
                    Text("Time A", fontWeight = FontWeight.Bold)
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
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    if (showElo && match.teamAAverageElo != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "(Elo: ${EloCalculator.formatElo(match.teamAAverageElo)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
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
                    Text("Time B", fontWeight = FontWeight.Bold)
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
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    if (showElo && match.teamBAverageElo != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "(Elo: ${EloCalculator.formatElo(match.teamBAverageElo)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Perguntas frequentes (FAQ)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        FAQItem(
            "O que é Elo e Elo Médio?",
            "O Elo é um sistema de pontuação que avalia o nível de habilidade de cada jogador. Você ganha pontos ao vencer e perde ao ser derrotado, baseado na dificuldade da partida. O Elo médio é simplesmente a soma dos pontos de uma equipe dividida pelo número de jogadores."
        )
        FAQItem(
            "Como funciona a Prioridade (Estrela)?",
            "Serve para garantir que certas posições ou níveis de habilidade sejam bem distribuídos. Por exemplo, se você marcar os levantadores com 'Prioridade', o app tentará colocar um levantador de cada lado na hora de gerar os times automaticamente."
        )
        FAQItem(
            "O que é Mostrar Atraso?",
            "Quando ativado, mostra quantos jogos fictícios foram somados a quem chegou atrasado, de acordo com o que a quadra já jogou. Isso evita que quem chega no fim passe na frente de quem espera desde o início. Quem chega junto com a turma começa com atraso zero. Só aparecem valores a partir de um."
        )
        FAQItem(
            "Como criar ou gerenciar Grupos?",
            "No menu lateral, você pode criar diferentes 'Grupos'. Isso é útil se você joga em lugares ou com turmas diferentes (ex: Vôlei de Sábado e Vôlei da Empresa). Cada grupo tem seu próprio histórico e lista de jogadores."
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(top = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    )
}

// --- TELA SOBRE ---
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Sobre o aplicativo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)

        )
        Spacer(Modifier.height(16.dp))

        Text(
            "O Vôlei Manager surgiu da necessidade real de organizar as peladas de vôlei de forma justa e dinâmica. Quem nunca passou pelo problema de times desequilibrados ou confusão na hora de saber quem é o próximo a jogar? O app cuida da fila, do nível de habilidade (através do Elo) e da diversão da galera.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Sobre o desenvolvedor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally)

        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Olá! Eu sou o Bruno Bismarck, o desenvolvedor por trás deste projeto. Criei este aplicativo com dedicação para facilitar a vida de quem organiza jogos com os amigos. Todo o feedback é bem-vindo!",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Apoie o projeto ☕",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "O Vôlei Manager é gratuito. Se ele ajudou você e sua turma, considere me seguir no Instagram para apoiar a continuidade deste projeto :)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/bismarckbruno/"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "@bismarckbruno",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ExportableImageContent(
    matches: List<MatchHistory>?,
    matchSortMode: MatchSortMode?,
    players: List<HistoryPlayerInfo>?,
    playerSortMode: PlayerSortMode?,
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
            .padding(top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(
                    id = if (isDarkTheme) com.bismarck.voleimanager.app.R.drawable.bola_de_v_lei_mais_clara_para_fundo_escuro
                    else com.bismarck.voleimanager.app.R.drawable.ic_launcher_foreground
                ),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "Vôlei Manager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else com.bismarck.voleimanager.app.ui.theme.voleiManagerBlue
            )
        }

        val title = if (matches != null) "Partidas - $date" else "Jogadores - $date"
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        val sortLabel = when {
            matches != null -> when (matchSortMode) {
                MatchSortMode.NEWEST -> "Ordenação: Mais recentes primeiro"
                MatchSortMode.OLDEST -> "Ordenação: Mais antigos primeiro"
                MatchSortMode.ELO_DELTA -> "Ordenação: Maior variação de Elo"
                MatchSortMode.SCORE_DIFF -> "Ordenação: Maior diferença de placar"
                else -> ""
            }
            players != null -> when (playerSortMode) {
                PlayerSortMode.ELO -> "Ordenação: Maior Elo"
                PlayerSortMode.GAMES -> "Ordenação: Mais jogos"
                PlayerSortMode.VICTORIES -> "Ordenação: Mais vitórias"
                PlayerSortMode.PERCENTAGE -> "Ordenação: Maior porcentagem"
                PlayerSortMode.ALPHABETICAL -> "Ordenação: Ordem Alfabética"
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
            Text(
                text = "Elo médio: $averagePlayersEloText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (matches != null && averageMatchDurationText != null) {
            Text(
                text = "Duração média: $averageMatchDurationText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                victories = info.victories
            )
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = composed {
    val density = LocalDensity.current
    this.size((20 * scale).dp)
}



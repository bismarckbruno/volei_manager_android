package com.bismarck.voleimanager.ui.game

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.data.model.Player
import com.bismarck.voleimanager.ui.ManualSetupScreen
import com.bismarck.voleimanager.ui.components.EditPlayerDialog
import com.bismarck.voleimanager.ui.components.SubstitutionDialog
import com.bismarck.voleimanager.ui.components.simpleScrollbar
import com.bismarck.voleimanager.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator

@Composable
fun GameScreenContent(
    viewModel: VoleiViewModel,
    selectedGroup: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showToll: Boolean,
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

    val absentPlayers = remember(sortedPlayers, presentIds) {
        sortedPlayers.filter { !presentIds.contains(it.id) }
    }

    var showAbsentDialog by remember { mutableStateOf(false) }
    var playerToAddFromAbsent by remember { mutableStateOf<Player?>(null) }
    var showCancel by remember { mutableStateOf(false) }
    var subOut by remember { mutableStateOf<Player?>(null) }
    var editP by remember { mutableStateOf<Player?>(null) }
    var confirmWinTeam by remember { mutableStateOf<String?>(null) }

    if (showAbsentDialog) {
        AlertDialog(
            onDismissRequest = { showAbsentDialog = false },
            title = { Text("Selecionar jogadores") },
            text = {
                if (absentPlayers.isEmpty()) {
                    Text("Todos os jogadores cadastrados já estão presentes na quadra.")
                } else {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .simpleScrollbar(listState)
                    ) {
                        items(absentPlayers) { p ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        p.name,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    playerToAddFromAbsent = p
                                    showAbsentDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAbsentDialog = false },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    playerToAddFromAbsent?.let { p ->
        val playedToday = (gamesPlayedMap[p.id] ?: 0) > 0
        AlertDialog(
            onDismissRequest = { playerToAddFromAbsent = null },
            title = { Text("Selecionar ${p.name}?") },
            text = {
                if (playedToday) {
                    Text("A pessoa selecionada irá para o final da fila de espera.")
                } else {
                    Text("A pessoa selecionada irá para o começo da fila de espera por ainda não ter jogado hoje.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.togglePlayerPresence(p)
                    playerToAddFromAbsent = null
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToAddFromAbsent = null }) {
                    Text(
                        "Cancelar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    if (showCancel) AlertDialog(
        onDismissRequest = { showCancel = false },
        title = { Text("Cancelar partida?") },
        text = { Text("O progresso atual será perdido.") },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    viewModel.cancelGame()
                    showCancel = false
                }) { Text("Sim") }
        },
        dismissButton = {
            TextButton(onClick = { showCancel = false }) {
                Text(
                    "Não",
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
            title = { Text("Vitória do Time $team?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishGame(team)
                        confirmWinTeam = null
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmWinTeam = null }) {
                    Text(
                        "Cancelar",
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
                            config.priorityEnabled
                        )
                        viewModel.startManualGame(tA, tB, b)
                        onSetupModeChange(false)
                    },
                    { onSetupModeChange(false) }
                )
            } else {
                AnimatedContent(
                    targetState = teamA.isNotEmpty() || teamB.isNotEmpty(),
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
                            { showCancel = true },
                            { subOut = it },
                            { confirmWinTeam = it },
                            onOpenAbsentList = { showAbsentDialog = true })
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
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
                                        onShowSnackbar = onShowSnackbar
                                    )
                                }

                                if (sortedPlayers.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Para começar, adicione jogadores no botão \"+\" no canto superior direito da tela.",
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
                                            Text("Jogadores", fontWeight = FontWeight.Bold)
                                            val all =
                                                sortedPlayers.all { presentIds.contains(it.id) }
                                            TextButton(
                                            onClick = {
                                                viewModel.setAllPlayersPresence(
                                                    sortedPlayers,
                                                    !all
                                                )
                                            }) { Text(if (all) "Desmarcar todos" else "Marcar todos") }
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
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 8.dp
                            ) {
                                val selCount = presentIds.size
                                val totalCount = sortedPlayers.size
                                val text = if (selCount == 0) {
                                    "Nenhum selecionado ($totalCount cadastrado${if (totalCount > 1) "s)" else ")"}"
                                } else {
                                    "$selCount selecionado${if (selCount > 1) "s" else ""} de $totalCount cadastrado${if (totalCount > 1) "s" else ""}"
                                }
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    onCancelRequest: () -> Unit,
    onSubRequest: (Player) -> Unit,
    onWinRequest: (String) -> Unit,
    onOpenAbsentList: () -> Unit
) {
    val teamAStreak = if (streakOwner == "A") currentStreak else 0
    val teamBStreak = if (streakOwner == "B") currentStreak else 0

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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .fillMaxSize()
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                        Box(modifier = Modifier.weight(1f).heightIn(min = 250.dp)) {
                            ActiveTeamCard(
                                "Time A",
                                teamA,
                                cardColorA,
                                btnColorA,
                                btnTextColorA,
                                streakColorA,
                                teamAStreak,
                                showElo,
                                score = scoreA,
                                onIncrementScore = { viewModel.incrementScoreA() },
                                onDecrementScore = { viewModel.decrementScoreA() },
                                onPlayerClick = onSubRequest
                            ) { onWinRequest("A") }
                        }
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                        Box(modifier = Modifier.weight(1f).heightIn(min = 250.dp)) {
                            ActiveTeamCard(
                                "Time B",
                                teamB,
                                cardColorB,
                                btnColorB,
                                btnTextColorB,
                                streakColorB,
                                teamBStreak,
                                showElo,
                                score = scoreB,
                                onIncrementScore = { viewModel.incrementScoreB() },
                                onDecrementScore = { viewModel.decrementScoreB() },
                                onPlayerClick = onSubRequest
                            ) { onWinRequest("B") }
                        }

                    }
                    TextButton(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            "Cancelar partida",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
                Spacer(Modifier.width(16.dp)); HorizontalDivider(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .alpha(0.2f)
            ); Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(0.25f)
                        .fillMaxHeight()
                ) {
                    Text(
                        "Na espera (${waitingList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(
                            waitingList
                        ) { i, p ->
                            WaitingPlayerCard(
                                i + 1,
                                p,
                                showElo
                            ) { viewModel.removePlayerFromWaitingList(p) }
                        }
                        item {
                            AbsentPlayerGhostCard(onClick = onOpenAbsentList)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp)) {
                    ActiveTeamCard(
                        "Time A",
                        teamA,
                        cardColorA,
                        btnColorA,
                        btnTextColorA,
                        streakColorA,
                        teamAStreak,
                        showElo,
                        score = scoreA,
                        onIncrementScore = { viewModel.incrementScoreA() },
                        onDecrementScore = { viewModel.decrementScoreA() },
                        onPlayerClick = onSubRequest
                    ) { onWinRequest("A") }
                }
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp)) {
                    ActiveTeamCard(
                        "Time B",
                        teamB,
                        cardColorB,
                        btnColorB,
                        btnTextColorB,
                        streakColorB,
                        teamBStreak,
                        showElo,
                        score = scoreB,
                        onIncrementScore = { viewModel.incrementScoreB() },
                        onDecrementScore = { viewModel.decrementScoreB() },
                        onPlayerClick = onSubRequest
                    ) { onWinRequest("B") }
                }
                
                TextButton(
                    onClick = onCancelRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "Cancelar partida",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(waitingList) { i, p ->
                    WaitingPlayerCard(
                        i + 1,
                        p,
                        showElo
                    ) { viewModel.removePlayerFromWaitingList(p) }
                }
                item {
                    AbsentPlayerGhostCard(onClick = onOpenAbsentList)
                }
            }
        }
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
    onIncrementScore: () -> Unit,
    onDecrementScore: () -> Unit,
    onPlayerClick: (Player) -> Unit,
    onWin: () -> Unit
) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black
    val dividerColor = contentColor.copy(alpha = 0.2f)

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
            Column (horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
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
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(contentColor.copy(alpha = 0.1f), shape = CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onDecrementScore,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir Placar", tint = contentColor)
                }
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = onIncrementScore,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar Placar", tint = contentColor)
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
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { onPlayerClick(p) }
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
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(12.dp),
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
                    "VITÓRIA",
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
    onShowSnackbar: (String) -> Unit
) {
    val minNeeded = currentTeamSize * 2
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
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
                        text = "👑 Rei da quadra atingiu o limite!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = kingTextColor,
                        textAlign = TextAlign.Center
                    ); Spacer(modifier = Modifier.height(4.dp)); Text(
                        text = "O time vencedor venceu $currentStreak seguidas e será redistribuído na próxima rodada.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val teamName =
                        if (streakOwner == "A") "Time A" else if (streakOwner == "B") "Time B" else "Vencedor"
                    val playerNames = lastWinners.joinToString(", ") { it.name }
                    Text(
                        text = "Vitória do $teamName",
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
                Text("Grupo: $currentGroup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mínimo: $minNeeded jogadores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedCount < minNeeded) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (hasPreviousMatch) {
                Button(
                    onClick = {
                        if (selectedCount >= minNeeded) {
                            onNextRoundClick()
                        } else {
                            onShowSnackbar("Selecione no mínimo $minNeeded jogadores")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Iniciar próximo jogo",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                val canStartAuto = selectedCount >= minNeeded
                Button(
                    onClick = {
                        if (canStartAuto) {
                            onStartAutoClick()
                        } else {
                            onShowSnackbar("Selecione no mínimo $minNeeded jogadores ou altere essas configurações em \"Regras do grupo\"")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canStartAuto) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        },
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    ); Spacer(Modifier.width(8.dp)); Text(
                    "Iniciar jogo",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (selectedCount >= 4) {
                TextButton(onClick = onStartManualClick) {
                    Text(
                        text = "Ou montar times manualmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                TextButton(onClick = {
                    onShowSnackbar("Selecione no mínimo 4 jogadores")
                }) {
                    Text(
                        text = "Ou montar times manualmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
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

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .combinedClickable(onClick = onTogglePresence, onLongClick = { showMenu = true }),
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
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(14.dp),
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
                    val hasToll = player.tollDate == targetDate && player.dailyToll > 0
                    val fontScale = LocalDensity.current.fontScale
                    val isAccessibleMode = fontScale > 1.15f

                    val info = if (actualGames == 0 && !hasToll && !isAccessibleMode) {
                        "Sem jogos recentes"
                    } else if (!(actualGames == 0 || !hasToll || isAccessibleMode)) {
                        "0 jogos"
                    } else {
                        val gamesStr = if (actualGames == 1) "1 jogo" else "$actualGames jogos"
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
                text = { Text("Editar") },
                onClick = { showMenu = false; onEdit() },
                leadingIcon = { Icon(Icons.Default.Edit, null) })
            DropdownMenuItem(
                text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
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
fun WaitingPlayerCard(index: Int, player: Player, showElo: Boolean, onRemove: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .widthIn(min = 120.dp)
            .heightIn(min = 60.dp)
            .combinedClickable(onClick = { }, onLongClick = { showMenu = true })
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Remover da fila", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onRemove()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

@Composable
fun AbsentPlayerGhostCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .widthIn(min = 120.dp)
            .heightIn(min = 60.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Ausentes",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
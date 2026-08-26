package com.bismarck.voleimanager.app.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.components.PlayerNameWithPositionBadges
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.ui.components.TeamCompositionIndicator
import com.bismarck.voleimanager.app.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.app.util.EloCalculator
import com.bismarck.voleimanager.app.util.PositionAssigner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSetupScreen(
    players: List<Player>, // Jogadores do grupo selecionado
    showElo: Boolean, // Passado do ViewModel para respeitar a configuração
    groupType: GroupType = GroupType.RECREATIONAL,
    onConfirm: (List<Player>, List<Player>, List<Player>, Int) -> Unit, // Retorna (TimeA, TimeB, Resto, TeamSize)
    onCancel: () -> Unit
) {
    // Estado para guardar onde cada jogador está alocado
    // Map: ID do Jogador -> "A", "B" ou null (Banco)
    // Usamos rememberSaveable para sobreviver à rotação
    var selectionState by rememberSaveable { mutableStateOf(emptyMap<Int, String>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val errorMsg = stringResource(R.string.select_equal_number)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()

    // Calcula os times em tempo real baseados na seleção
    val teamA = players.filter { selectionState[it.id] == "A" }
    val teamB = players.filter { selectionState[it.id] == "B" }
    val bench = players.filter { selectionState[it.id] == null }

    val canStart = teamA.size == teamB.size && teamA.size in groupType.teamSizeRange

    // Indicador de composição: só faz sentido nos tipos com posições fixas e não bloqueia o início.
    val compositionA = remember(teamA, groupType) {
        if (groupType.usesPositions && teamA.isNotEmpty()) {
            PositionAssigner.describeComposition(teamA, teamA.size)
        } else {
            emptyList()
        }
    }
    val compositionB = remember(teamB, groupType) {
        if (groupType.usesPositions && teamB.isNotEmpty()) {
            PositionAssigner.describeComposition(teamB, teamB.size)
        } else {
            emptyList()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val contentModifier = Modifier
            .padding(padding)
            .fillMaxSize()

        if (isLandscape) {
            LazyColumn(
                state = listState,
                modifier = contentModifier
            ) {
                item {
                    ManualSetupActionBar(
                        canStart = canStart,
                        onCancel = onCancel,
                        onStart = {
                            if (canStart) {
                                onConfirm(teamA, teamB, bench, teamA.size)
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                            }
                        }
                    )
                }
                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    ManualSetupSelectionSummary(teamACount = teamA.size, teamBCount = teamB.size)
                }
                item {
                    ManualSetupCompositionSummary(compositionA, compositionB)
                }
                item {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(players) { player ->
                    PlayerSelectionRow(
                        player = player,
                        currentSelection = selectionState[player.id],
                        showElo = showElo,
                        usesPositions = groupType.usesPositions,
                        onSelect = { selection ->
                            val newState = selectionState.toMutableMap()
                            if (newState[player.id] == selection) {
                                newState.remove(player.id)
                            } else {
                                newState[player.id] = selection
                            }
                            selectionState = newState
                        }
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        } else {
            Column(modifier = contentModifier) {
                ManualSetupActionBar(
                    canStart = canStart,
                    onCancel = onCancel,
                    onStart = {
                        if (canStart) {
                            onConfirm(teamA, teamB, bench, teamA.size)
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(errorMsg) }
                        }
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ManualSetupSelectionSummary(teamACount = teamA.size, teamBCount = teamB.size)
                ManualSetupCompositionSummary(compositionA, compositionB)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(players) { player ->
                            PlayerSelectionRow(
                                player = player,
                                currentSelection = selectionState[player.id],
                                showElo = showElo,
                                usesPositions = groupType.usesPositions,
                                onSelect = { selection ->
                                    val newState = selectionState.toMutableMap()
                                    if (newState[player.id] == selection) {
                                        newState.remove(player.id)
                                    } else {
                                        newState[player.id] = selection
                                    }
                                    selectionState = newState
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualSetupActionBar(
    canStart: Boolean,
    onCancel: () -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
        }

        Text(
            text = stringResource(R.string.assemble_teams),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                contentColor = if (canStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            contentPadding = if (canStart) {
                PaddingValues(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            } else {
                PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(stringResource(R.string.start))
            if (canStart) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Check, null)
            }
        }
    }
}

@Composable
private fun ManualSetupSelectionSummary(
    teamACount: Int,
    teamBCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamCounter(
            stringResource(R.string.team_a),
            teamACount,
            MaterialTheme.colorScheme.primary
        )
        Text(
            stringResource(R.string.vs),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TeamCounter(
            stringResource(R.string.team_b),
            teamBCount,
            LocalExtendedColors.current.anotherPrime.color
        )
    }
}

@Composable
private fun ManualSetupCompositionSummary(
    compositionA: List<PositionAssigner.FilledSlot>,
    compositionB: List<PositionAssigner.FilledSlot>
) {
    if (compositionA.isEmpty() && compositionB.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TeamCompositionIndicator(compositionA, Modifier.weight(1f))
        TeamCompositionIndicator(compositionB, Modifier.weight(1f))
    }
}

@Composable
fun TeamCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, color = color)
        Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PlayerSelectionRow(
    player: Player,
    currentSelection: String?, // "A", "B" ou null
    showElo: Boolean,
    usesPositions: Boolean = false,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone à esquerda + bloco de nome/elo à direita
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            Column {
                PlayerNameWithPositionBadges(
                    player = player,
                    usesPositions = usesPositions,
                    nameStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    nameFontWeight = FontWeight.Medium,
                    nameColor = MaterialTheme.colorScheme.onSurface,
                    isPriority = player.isPriority,
                    priorityIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() },
                    priorityTint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showElo) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            EloCalculator.formatElo(player.elo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Botões de Seleção (Toggle)
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                .padding(2.dp)
        ) {
            // Botão A
            SelectionButton(
                text = "A",
                isSelected = currentSelection == "A",
                activeColor = MaterialTheme.colorScheme.primary,
                onActiveColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onSelect("A") }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Botão B
            SelectionButton(
                text = "B",
                isSelected = currentSelection == "B",
                activeColor = LocalExtendedColors.current.anotherPrime.color,
                onActiveColor = LocalExtendedColors.current.anotherPrime.onColor,
                onClick = { onSelect("B") }
            )
        }
    }
}

@Composable
fun SelectionButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onActiveColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) activeColor else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) onActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.bismarck.voleimanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bismarck.voleimanager.data.model.MatchHistory
import com.bismarck.voleimanager.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// --- TELA DE HISTÓRICO ---
@Composable
fun HistoryScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean, showElo: Boolean) {
    val groupHistory by viewModel.currentGroupHistory.collectAsState()
    val historyDate by viewModel.historyDateFilter.collectAsState()
    val availableDates by viewModel.availableHistoryDates.collectAsState()

    val sortedHistory = remember(groupHistory, historyDate) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        groupHistory.filter {
            (historyDate == null || it.date.startsWith(historyDate!!))
        }.sortedByDescending {
            try { sdf.parse(it.date)?.time ?: 0L } catch (e: Exception) { 0L }
        }
    }

    var expandedDate by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expandedDate = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(historyDate ?: "Todas as datas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = expandedDate, onDismissRequest = { expandedDate = false }) {
                DropdownMenuItem(text = { Text("Todas as datas") }, onClick = { viewModel.setHistoryDateFilter(null); expandedDate = false })
                availableDates.forEach { date ->
                    DropdownMenuItem(text = { Text(date) }, onClick = { viewModel.setHistoryDateFilter(date); expandedDate = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedHistory) { match -> HistoryItem(match, isDarkTheme, showElo) }
            if (sortedHistory.isEmpty()) item { 
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma partida encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(match: MatchHistory, isDarkTheme: Boolean, showElo: Boolean) {
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

    Card(colors = CardDefaults.cardColors(containerColor = cardBgColor, contentColor = contentColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(match.date, style = MaterialTheme.typography.labelMedium)
                if (showElo) {
                    val formattedDelta = remember(match.eloPoints) {
                        NumberFormat.getInstance(Locale.getDefault()).apply {
                            maximumFractionDigits = 2
                            minimumFractionDigits = 0
                        }.format(match.eloPoints)
                    }
                    Text("±$formattedDelta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = contentColor.copy(alpha = 0.3f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Time A", fontWeight = FontWeight.Bold)
                        if (isTeamAWin) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Star, contentDescription = "Vencedor", modifier = Modifier.size(16.dp), tint = starColor)
                        }
                    }
                    if (showElo && match.teamAAverageElo != null) {
                        Spacer(Modifier.height(2.dp))
                        Text("(Elo: ${EloCalculator.formatElo(match.teamAAverageElo)})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = contentColor.copy(alpha = 0.8f))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(teamANames, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                Text("VS", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.titleSmall)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Time B", fontWeight = FontWeight.Bold)
                        if (!isTeamAWin) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Star, contentDescription = "Vencedor", modifier = Modifier.size(16.dp), tint = starColor)
                        }
                    }
                    if (showElo && match.teamBAverageElo != null) {
                        Spacer(Modifier.height(2.dp))
                        Text("(Elo: ${EloCalculator.formatElo(match.teamBAverageElo)})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = contentColor.copy(alpha = 0.8f))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(teamBNames, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = composed {
    val density = LocalDensity.current
    this.size(with(density) { (20*scale).dp })
}

package com.example.voleimanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
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
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(historyDate ?: "Todas as datas")
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(expanded = expandedDate, onDismissRequest = { expandedDate = false }) {
                DropdownMenuItem(text = { Text("Todas as datas") }, onClick = { viewModel.setHistoryDateFilter(null); expandedDate = false })
                availableDates.forEach { date ->
                    DropdownMenuItem(text = { Text(date) }, onClick = { viewModel.setHistoryDateFilter(date); expandedDate = false })
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedHistory) { match -> HistoryItem(match, isDarkTheme, showElo) }
            if (sortedHistory.isEmpty()) item { 
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma partida encontrada.", color = MaterialTheme.colorScheme.secondary) 
                }
            }
        }
    }
}

@Composable
fun HistoryItem(match: MatchHistory, isDarkTheme: Boolean, showElo: Boolean) {
    val winColor = MaterialTheme.colorScheme.primary
    val isTeamAWin = match.winner == "Time A"
    val teamANames = match.teamA.replace(",", ", ")
    val teamBNames = match.teamB.replace(",", ", ")

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(match.date, style = MaterialTheme.typography.labelMedium)
                if (showElo) {
                    Text("±${String.format(Locale.US, "%.2f", match.eloPoints)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
            Divider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time A", fontWeight = FontWeight.Bold, color = if(isTeamAWin) winColor else Color.Unspecified)
                    Text(teamANames, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                Text("VS", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleSmall)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time B", fontWeight = FontWeight.Bold, color = if(!isTeamAWin) winColor else Color.Unspecified)
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
package com.example.voleimanager.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// --- TELA DE RANKING (VISUAL RESTAURADO) ---
@Composable
fun RankingScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val rankingDate by viewModel.rankingDateFilter.collectAsState()
    val availableDates by viewModel.availableRankingDates.collectAsState()
    val displayedPlayers = remember(rankingDate, viewModel.currentGroupEloLogs, viewModel.currentGroupPlayers) {
        viewModel.getRankingListForDate(rankingDate)
    }
    var expandedDate by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expandedDate = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                val dateLabel = rankingDate?.let {
                    try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)!!) } catch(e:Exception){it}
                } ?: "Ranking Atual (Geral)"
                Text(dateLabel)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(expanded = expandedDate, onDismissRequest = { expandedDate = false }) {
                DropdownMenuItem(text = { Text("Atual (Geral)") }, onClick = { viewModel.setRankingDateFilter(null); expandedDate = false })
                availableDates.forEach { date ->
                    val dateStr = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)!!) } catch(e:Exception){date}
                    DropdownMenuItem(text = { Text(dateStr) }, onClick = { viewModel.setRankingDateFilter(date); expandedDate = false })
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(displayedPlayers) { index, player ->
                RankingItem(index + 1, player, viewModel.getPatente(player.elo))
            }
            if (displayedPlayers.isEmpty()) {
                item { 
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum dado para este período.", color = MaterialTheme.colorScheme.secondary) 
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(pos: Int, player: Player, patente: String) {
    val medalColor = when(pos) { 
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.surfaceVariant 
    }
    val textColor = if (pos <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
    val isTop3 = pos <= 3

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isTop3) 4.dp else 1.dp),
        border = if (isTop3) BorderStroke(1.dp, medalColor.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(medalColor), 
                contentAlignment = Alignment.Center
            ) {
                Text("$pos", fontWeight = FontWeight.Bold, color = textColor)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(patente, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${String.format(Locale.US, "%.0f", player.elo)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("Elo", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// --- TELA DE HISTÓRICO ---
@Composable
fun HistoryScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
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
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedHistory) { match -> HistoryItem(match, isDarkTheme) }
            if (sortedHistory.isEmpty()) item { 
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Nenhuma partida encontrada.", color = MaterialTheme.colorScheme.secondary) 
                }
            }
        }
    }
}

@Composable
fun HistoryItem(match: MatchHistory, isDarkTheme: Boolean) {
    val winColor = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
    val isTeamAWin = match.winner == "Time A"
    val teamANames = match.teamA.replace(",", ", ")
    val teamBNames = match.teamB.replace(",", ", ")

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(match.date, style = MaterialTheme.typography.labelMedium)
                Text("±${String.format(Locale.US, "%.2f", match.eloPoints)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
            Divider(Modifier.padding(vertical = 8.dp))
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

// --- TELA DE GRÁFICOS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
    val groupLogs by viewModel.currentGroupEloLogs.collectAsState()
    val selectedIds by viewModel.chartSelectedPlayerIds.collectAsState()
    val dateRange by viewModel.chartDateRange.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setChartDateRange(datePickerState.selectedStartDateMillis, datePickerState.selectedEndDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DateRangePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            // CORREÇÃO: Título menor
            Text("Evolução de Elo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Filtrar Data") }
        }
        
        if (dateRange.first != null && dateRange.second != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Text("Período: ${sdf.format(Date(dateRange.first!!))} - ${sdf.format(Date(dateRange.second!!))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(8.dp))
        
        Text("Selecione para comparar:", style = MaterialTheme.typography.labelSmall)
        LazyColumn(modifier = Modifier.height(100.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), RoundedCornerShape(8.dp))) {
            items(groupPlayers) { player ->
                val isSelected = selectedIds.contains(player.id)
                Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleChartPlayer(player.id) }.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleChartPlayer(player.id) }, modifier = Modifier.scale(0.8f))
                    Text(player.name, modifier = Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    Text("${player.elo.toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            if (selectedIds.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(8.dp))
                        Text("Selecione jogadores na lista acima.", color = MaterialTheme.colorScheme.secondary) 
                    }
                }
            } else {
                val relevantLogs = remember(groupLogs, selectedIds, dateRange) {
                    val sdfLog = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    groupLogs.filter { log ->
                        selectedIds.contains(log.playerId) &&
                        (dateRange.first == null || (sdfLog.parse(log.date)?.time ?: 0L) >= dateRange.first!!) &&
                        (dateRange.second == null || (sdfLog.parse(log.date)?.time ?: 0L) <= dateRange.second!!)
                    }
                    .groupBy { Pair(it.playerId, it.date) } // Agrupa por Jogador e Data
                    .map { (_, logsOfDay) -> 
                        logsOfDay.maxByOrNull { it.id }!! // Pega o último log do dia (maior ID)
                    }
                    .sortedBy { it.date }
                }

                if (relevantLogs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sem dados para este período.", color = MaterialTheme.colorScheme.secondary) }
                } else {
                    EloChart(relevantLogs, selectedIds, groupPlayers, isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun EloChart(logs: List<PlayerEloLog>, selectedIds: Set<Int>, players: List<Player>, isDarkTheme: Boolean) {
    val uniqueDates = logs.map { it.date }.distinct().sorted()
    val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Magenta, Color.Cyan, Color(0xFFFFA000))
    val sortedIds = remember(selectedIds) { selectedIds.sorted() }
    val textPaint = remember(isDarkTheme) { android.graphics.Paint().apply { color = if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK; textSize = 30f; textAlign = android.graphics.Paint.Align.CENTER } }

    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        val width = size.width; val height = size.height
        val bottomMargin = 60f 
        val chartHeight = height - 40f - bottomMargin
        val xStep = if (uniqueDates.size > 1) width / (uniqueDates.size - 1) else width
        
        val minElo = (logs.minOfOrNull { it.elo } ?: 1000.0).toFloat()
        val maxElo = (logs.maxOfOrNull { it.elo } ?: 1400.0).toFloat()
        val eloRange = (maxElo - minElo).coerceAtLeast(10f)

        // Eixo X
        drawLine(Color.Gray.copy(alpha=0.5f), Offset(0f, height-bottomMargin), Offset(width, height-bottomMargin), 2f)
        uniqueDates.forEachIndexed { index, date ->
            val x = index * xStep
            val dateLabel = try { 
                val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d!!)
            } catch(e: Exception) { date }
            
            if (uniqueDates.size < 5 || index % (uniqueDates.size/4) == 0) {
                 drawContext.canvas.nativeCanvas.drawText(dateLabel, x, height - 10f, textPaint)
                 drawLine(Color.Gray.copy(alpha=0.3f), Offset(x, 0f), Offset(x, height-bottomMargin), 1f)
            }
        }

        sortedIds.forEachIndexed { index, playerId ->
            val pLogs = logs.filter { it.playerId == playerId }.sortedBy { it.date }
            if (pLogs.isNotEmpty()) {
                val path = Path()
                val color = colors[index % colors.size]
                pLogs.forEach { log ->
                    val idx = uniqueDates.indexOf(log.date)
                    val x = idx * xStep
                    val y = (height - bottomMargin) - ((log.elo.toFloat() - minElo) / eloRange * chartHeight)
                    if (log == pLogs.first()) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(color, 4.dp.toPx(), Offset(x, y))
                }
                drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = composed {
    val density = LocalDensity.current
    this.size(with(density) { (20*scale).dp })
}

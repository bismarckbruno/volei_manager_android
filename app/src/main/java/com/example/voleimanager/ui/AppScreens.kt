package com.example.voleimanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

// --- TELA DE RANKING ---
@Composable
fun RankingScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val rankingDate by viewModel.rankingDateFilter.collectAsState()
    val availableDates by viewModel.availableRankingDates.collectAsState()
    val displayedPlayers = viewModel.getRankingListForDate(rankingDate)

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
                item { Text("Nenhum dado para este período.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            }
        }
    }
}

@Composable
fun RankingItem(pos: Int, player: Player, patente: String) {
    val medalColor = when(pos) { 1 -> Color(0xFFFFD700); 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> MaterialTheme.colorScheme.surfaceVariant }
    val textColor = if (pos <= 3) Color.Black else MaterialTheme.colorScheme.onSurface
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(medalColor, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                Text("$pos", fontWeight = FontWeight.Bold, color = textColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontWeight = FontWeight.Bold)
                Text(patente, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${player.elo.toInt()}", fontWeight = FontWeight.Bold)
                Text("Elo", style = MaterialTheme.typography.bodySmall)
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
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
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
            if (sortedHistory.isEmpty()) item { Text("Nenhuma partida encontrada.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
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
                Text(match.date, style = MaterialTheme.typography.bodySmall)
                Text("±${"%.1f".format(match.eloPoints)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            Divider(Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time A", fontWeight = FontWeight.Bold, color = if(isTeamAWin) winColor else Color.Unspecified)
                    Text(teamANames, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
                Text("VS", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time B", fontWeight = FontWeight.Bold, color = if(!isTeamAWin) winColor else Color.Unspecified)
                    Text(teamBNames, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// --- TELA DE GRÁFICOS ---
@Composable
fun ChartsScreen(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
    val groupLogs by viewModel.currentGroupEloLogs.collectAsState()
    val selectedIds by viewModel.chartSelectedPlayerIds.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Evolução de Elo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Selecione para comparar:", style = MaterialTheme.typography.bodySmall)

        LazyColumn(modifier = Modifier.height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))) {
            items(groupPlayers) { player ->
                val isSelected = selectedIds.contains(player.id)
                Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleChartPlayer(player.id) }.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleChartPlayer(player.id) })
                    Text(player.name, modifier = Modifier.weight(1f), maxLines = 1)
                    Text("${player.elo.toInt()}", fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            if (selectedIds.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Selecione jogadores acima.") }
            } else {
                val relevantLogs = groupLogs.filter { selectedIds.contains(it.playerId) }.sortedBy { it.date }
                if (relevantLogs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sem histórico suficiente.") }
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
    if (uniqueDates.isEmpty()) return
    val colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Magenta, Color.Cyan, Color(0xFFFFA000))
    val sortedIds = remember(selectedIds) { selectedIds.sorted() }

    val textPaint = remember(isDarkTheme) {
        android.graphics.Paint().apply {
            color = if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. ÁREA DO GRÁFICO
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp)) {
            val width = size.width
            val height = size.height
            // Margem para os textos de Elo e Eixo X
            val topMargin = 40f
            val bottomMargin = 50f
            val chartHeight = height - topMargin - bottomMargin

            val xStep = if (uniqueDates.size > 1) width / (uniqueDates.size - 1) else width

            val minElo = logs.minOfOrNull { it.elo }?.toFloat() ?: 1000f
            val maxElo = logs.maxOfOrNull { it.elo }?.toFloat() ?: 1400f
            val eloRange = (maxElo - minElo).coerceAtLeast(10f)

            // LINHAS DO EIXO X (Datas)
            val lineY = height - bottomMargin
            drawLine(Color.Gray.copy(alpha = 0.5f), Offset(0f, lineY), Offset(width, lineY), 2f)

            // DESENHA AS DATAS NO EIXO X
            uniqueDates.forEachIndexed { index, date ->
                val x = index * xStep
                val shortDate = try { val d = date.split("-"); "${d[2]}/${d[1]}" } catch (e: Exception) { "" }
                drawContext.canvas.nativeCanvas.drawText(shortDate, x, height - 10f, textPaint)
            }

            // DESENHA AS LINHAS DOS JOGADORES
            sortedIds.forEachIndexed { index, playerId ->
                val playerLogs = logs.filter { it.playerId == playerId }.sortedBy { it.date }
                if (playerLogs.isNotEmpty()) {
                    val path = Path()
                    val color = colors[index % colors.size]

                    playerLogs.forEach { log ->
                        val dateIndex = uniqueDates.indexOf(log.date)
                        val x = dateIndex * xStep
                        val normalizedElo = (log.elo.toFloat() - minElo) / eloRange
                        // Y invertido (maior elo em cima) ajustado às margens
                        val y = (height - bottomMargin) - (normalizedElo * chartHeight)

                        if (log == playerLogs.first()) path.moveTo(x, y) else path.lineTo(x, y)

                        drawCircle(color, 5.dp.toPx(), Offset(x, y))
                        // Desenha APENAS o Elo acima do ponto
                        drawContext.canvas.nativeCanvas.drawText(log.elo.toInt().toString(), x, y - 20f, textPaint)
                    }
                    drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
                }
            }
        }

        // 2. LEGENDA
        Divider()
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(sortedIds) { index, id ->
                val player = players.find { it.id == id }
                if (player != null) {
                    val color = colors[index % colors.size]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(4.dp))
                        Text(text = player.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
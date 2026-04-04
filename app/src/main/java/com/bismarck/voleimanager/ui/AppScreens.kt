package com.bismarck.voleimanager.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.data.model.MatchHistory
import com.bismarck.voleimanager.ui.theme.LocalExtendedColors
import com.bismarck.voleimanager.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.util.EloCalculator
import kotlinx.coroutines.launch
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
            try {
                sdf.parse(it.date)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }

    var expandedDate by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sortedHistory) { match -> HistoryItem(match, isDarkTheme, showElo) }
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

    val scoreA = match.teamAScore ?: 0
    val scoreB = match.teamBScore ?: 0
    val hasScore = scoreA > 0 || scoreB > 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor,
            contentColor = contentColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(match.date, style = MaterialTheme.typography.labelMedium)
                if (showElo) {
                    val formattedDelta = remember(match.eloPoints) {
                        NumberFormat.getInstance(Locale.getDefault()).apply {
                            maximumFractionDigits = 2
                            minimumFractionDigits = 0
                        }.format(match.eloPoints)
                    }
                    Text(
                        "±$formattedDelta",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
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
                    if (hasScore){
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
                    if (hasScore) {
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
            .padding(16.dp)
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
            "O que é Mostrar Atraso (Pedágio)?",
            "Quando essa opção está ativa, pessoas que chegam mais tarde na pelada recebem 'partidas fictícias' proporcionais ao que a quadra já jogou. Isso evita que quem chega no fim pule na frente de quem está desde o início esperando para jogar. Se a pessoa chegar no mesmo horário da galera, ela começa com 0 de pedágio."
        )
        FAQItem(
            "Como criar ou gerenciar Grupos?",
            "No menu lateral, você pode criar diferentes 'Grupos'. Isso é útil se você joga em lugares ou com turmas diferentes (ex: Vôlei de Sábado e Vôlei da Empresa). Cada grupo tem seu próprio histórico e lista de jogadores."
        )

        Spacer(Modifier.height(32.dp))
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
            .padding(16.dp)
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
        Spacer(Modifier.height(32.dp))
    }
}

fun Modifier.scale(scale: Float): Modifier = composed {
    val density = LocalDensity.current
    this.size(with(density) { (20 * scale).dp })
}
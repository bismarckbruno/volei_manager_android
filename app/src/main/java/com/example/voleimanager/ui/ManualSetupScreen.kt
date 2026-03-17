package com.example.voleimanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voleimanager.data.model.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSetupScreen(
    players: List<Player>, // Jogadores do grupo selecionado
    showElo: Boolean, // Passado do ViewModel para respeitar a configuração
    onConfirm: (List<Player>, List<Player>, List<Player>, Int) -> Unit, // Retorna (TimeA, TimeB, Resto, TeamSize)
    onCancel: () -> Unit
) {
    // Estado para guardar onde cada jogador está alocado
    // Map: ID do Jogador -> "A", "B" ou null (Banco)
    val selectionState = remember { mutableStateMapOf<Int, String>() }

    // Calcula os times em tempo real baseados na seleção
    val teamA = players.filter { selectionState[it.id] == "A" }
    val teamB = players.filter { selectionState[it.id] == "B" }
    val bench = players.filter { selectionState[it.id] == null }

    val canStart = teamA.size == teamB.size && teamA.size in 2..6

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Montar times") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                actions = {
                    // Só permite iniciar se a qtde nos 2 times for igual e tiver entre 2 e 6 pessoas.
                    Button(
                        onClick = { onConfirm(teamA, teamB, bench, teamA.size) },
                        enabled = canStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Iniciar")
                        if (canStart) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- CABEÇALHO COM CONTAGEM ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamCounter("Time A", teamA.size, Color(0xFF1976D2)) // Azul
                Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TeamCounter("Time B", teamB.size, Color(0xFFD32F2F)) // Vermelho
            }

            if (!canStart) {
                Text(
                    text = "Para iniciar o jogo, selecione um número igual de pessoas em cada time (mín. 2 e máx. 6).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                )
            }

            Divider()

            // --- LISTA DE SELEÇÃO ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(players) { player ->
                    PlayerSelectionRow(
                        player = player,
                        currentSelection = selectionState[player.id],
                        showElo = showElo,
                        onSelect = { selection ->
                            if (selectionState[player.id] == selection) {
                                selectionState.remove(player.id) // Desmarcar (vai pro banco)
                            } else {
                                selectionState[player.id] = selection // Marca A ou B
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TeamCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, color = color)
        Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlayerSelectionRow(
    player: Player,
    currentSelection: String?, // "A", "B" ou null
    showElo: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nome e Elo
        Column(modifier = Modifier.weight(1f)) {
            Text(player.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            if (showElo) {
                Text("${player.elo.toInt()} Elo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        // Botões de Seleção (Toggle)
        Row(
            modifier = Modifier
                .background(Color(0xFFEEEEEE), RoundedCornerShape(50))
                .padding(2.dp)
        ) {
            // Botão A
            SelectionButton(
                text = "A",
                isSelected = currentSelection == "A",
                activeColor = Color(0xFF1976D2), // Azul
                onClick = { onSelect("A") }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Botão B
            SelectionButton(
                text = "B",
                isSelected = currentSelection == "B",
                activeColor = Color(0xFFD32F2F), // Vermelho
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
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
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}
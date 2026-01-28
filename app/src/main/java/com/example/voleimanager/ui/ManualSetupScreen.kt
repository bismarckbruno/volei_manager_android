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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voleimanager.data.model.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualSetupScreen(
    players: List<Player>, // Jogadores do grupo selecionado
    onConfirm: (List<Player>, List<Player>, List<Player>) -> Unit, // Retorna (TimeA, TimeB, Resto)
    onCancel: () -> Unit
) {
    // Estado para guardar onde cada jogador está alocado
    // Map: ID do Jogador -> "A", "B" ou null (Banco)
    val selectionState = remember { mutableStateMapOf<Int, String>() }

    // Calcula os times em tempo real baseados na seleção
    val teamA = players.filter { selectionState[it.id] == "A" }
    val teamB = players.filter { selectionState[it.id] == "B" }
    val bench = players.filter { selectionState[it.id] == null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Montar Times") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                actions = {
                    // Botão de Confirmar (Só ativa se tiver gente nos times)
                    val canStart = teamA.isNotEmpty() && teamB.isNotEmpty()
                    Button(
                        onClick = { onConfirm(teamA, teamB, bench) },
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamCounter("Time A", teamA.size, Color(0xFF1976D2)) // Azul
                TeamCounter("Banco", bench.size, Color.Gray)
                TeamCounter("Time B", teamB.size, Color(0xFFD32F2F)) // Vermelho
            }

            Divider()

            // --- LISTA DE SELEÇÃO ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(players) { player ->
                    PlayerSelectionRow(
                        player = player,
                        currentSelection = selectionState[player.id],
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
            Text("${player.elo.toInt()} Elo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
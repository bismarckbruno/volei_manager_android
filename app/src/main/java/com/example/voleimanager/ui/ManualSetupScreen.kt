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
import com.example.voleimanager.ui.theme.LocalExtendedColors
import com.example.voleimanager.util.EloCalculator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch

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
    // Usamos rememberSaveable para sobreviver à rotação
    var selectionState by rememberSaveable { mutableStateOf(emptyMap<Int, String>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Calcula os times em tempo real baseados na seleção
    val teamA = players.filter { selectionState[it.id] == "A" }
    val teamB = players.filter { selectionState[it.id] == "B" }
    val bench = players.filter { selectionState[it.id] == null }

    val canStart = teamA.size == teamB.size && teamA.size in 2..6

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- 1. CABEÇALHO PERSONALIZADO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp)
                    .padding(vertical = 8.dp)
            ) {
                // Botão Cancelar ancorado na ESQUERDA
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }

                // Título ancorado EXATAMENTE NO CENTRO da tela
                Text(
                    text = "Montar times",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Botão Iniciar ancorado na DIREITA
                Button(
                    onClick = { 
                        if (canStart) {
                            onConfirm(teamA, teamB, bench, teamA.size) 
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Selecione um número igual de pessoas em cada time (mín. 2 e máx. 6)")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canStart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        contentColor = if (canStart) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Iniciar")
                    if (canStart) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            // --- 2. PLACAR COM CONTAGEM ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamCounter("Time A", teamA.size, MaterialTheme.colorScheme.primary)
                Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Usando a cor estendida importada do Theme.kt gerado pelo Figma
                TeamCounter("Time B", teamB.size, LocalExtendedColors.current.anotherPrime.color)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // --- 3. LISTA DE SELEÇÃO ---
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(players) { player ->
                    PlayerSelectionRow(
                        player = player,
                        currentSelection = selectionState[player.id],
                        showElo = showElo,
                        onSelect = { selection ->
                            val newState = selectionState.toMutableMap()

                            if (newState[player.id] == selection) {
                                newState.remove(player.id) // Desmarcar (vai pro banco)
                            } else {
                                newState[player.id] = selection // Marca A ou B
                            }
                            selectionState = newState
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
        Text(text = count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
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
            Text(player.name, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            if (showElo) {
                Text("Elo: ${EloCalculator.formatElo(player.elo)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
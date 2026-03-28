package com.example.voleimanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.voleimanager.data.model.Player
import kotlin.math.roundToInt

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun RenameGroupDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember { mutableStateOf(oldName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear grupo") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Novo nome") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun SubstitutionDialog(
    playerOut: Player,
    waitingList: List<Player>,
    teamA: List<Player>,
    teamB: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: (Player) -> Unit
) {
    val allOptions = remember(waitingList, teamA, teamB, playerOut) {
        val list = mutableListOf<Pair<Player, String>>()
        val isTeamA = teamA.any { it.id == playerOut.id }
        val isTeamB = teamB.any { it.id == playerOut.id }
        waitingList.forEach { list.add(it to "(na espera)") }
        if (isTeamA) teamB.forEach { list.add(it to "(Time B)") }
        else if (isTeamB) teamA.forEach { list.add(it to "(Time A)") }
        else {
            teamA.forEach { list.add(it to "(Time A)") }; teamB.forEach { list.add(it to "(Time B)") }
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Substituir ${playerOut.name}") },
        text = {
            if (allOptions.isEmpty()) {
                Text("Não há jogadores disponíveis para troca.")
            } else {
                val listState = rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier.heightIn(max = 300.dp).simpleScrollbar(listState)) {
                    items(allOptions) { (playerIn, label) ->
                        ListItem(
                            headlineContent = { Text(playerIn.name) },
                            supportingContent = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.clickable { onConfirm(playerIn) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { 
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } 
        }
    )
}

@Composable
fun EditPlayerDialog(player: Player, onDismiss: () -> Unit, onConfirm: (String, Boolean) -> Unit) {
    var newName by remember { mutableStateOf(player.name) }
    var isPriority by remember { mutableStateOf(player.isPriority) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar cadastro") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nome") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text("Prioridade")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newName.isNotBlank()) onConfirm(
                    newName,
                    isPriority
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var eloText by remember { mutableStateOf("1200") }
    var isPriority by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo cadastro") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = eloText,
                    onValueChange = { eloText = it },
                    label = { Text("Elo inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text("Definir como prioridade")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onConfirm(
                    name,
                    eloText.toDoubleOrNull() ?: 1200.0,
                    isPriority
                )
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun GroupConfigDialog(
    groupName: String,
    initialTeamSize: Int,
    initialVictoryLimit: Int,
    initialPriorityEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Boolean) -> Unit
) {
    var teamSize by remember { mutableFloatStateOf(initialTeamSize.toFloat()) }
    var victoryLimit by remember { mutableFloatStateOf(initialVictoryLimit.toFloat()) }
    var priorityEnabled by remember { mutableStateOf(initialPriorityEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Regras do grupo $groupName") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(16.dp))
                Text("Jogadores por time: ${teamSize.roundToInt()}")
                Slider(
                    value = teamSize,
                    onValueChange = { teamSize = it },
                    valueRange = 2f..6f,
                    steps = 3
                )
                Spacer(Modifier.height(16.dp))

                Text("Limite de vitórias: ${victoryLimit.roundToInt()}")
                Slider(
                    value = victoryLimit,
                    onValueChange = { victoryLimit = it },
                    valueRange = 1f..6f,
                    steps = 4
                )
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = priorityEnabled, onCheckedChange = { priorityEnabled = it })
                    Spacer(Modifier.width(16.dp))
                    Text("Mín. 1 prioridade por time", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    teamSize.roundToInt(),
                    victoryLimit.roundToInt(),
                    priorityEnabled
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar novo grupo") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nome do grupo") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank()
            ) { Text("Criar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

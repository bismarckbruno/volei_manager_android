package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bismarck.voleimanager.app.data.model.Player
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
        title = { Text(stringResource(R.string.rename_group)) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(stringResource(R.string.new_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
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
    val waiting_parentheses = stringResource(R.string.waiting_parentheses)
    val team_a_parentheses = stringResource(R.string.team_a_parentheses)
    val team_b_parentheses = stringResource(R.string.team_b_parentheses)
    val allOptions = remember(waitingList, teamA, teamB, playerOut) {
        val list = mutableListOf<Pair<Player, String>>()
        val isTeamA = teamA.any { it.id == playerOut.id }
        val isTeamB = teamB.any { it.id == playerOut.id }
        waitingList.forEach { list.add(it to waiting_parentheses) }
        if (isTeamA) teamB.forEach { list.add(it to team_b_parentheses) }
        else if (isTeamB) teamA.forEach { list.add(it to team_a_parentheses) }
        else {
            teamA.forEach { list.add(it to team_a_parentheses) }; teamB.forEach { list.add(it to team_b_parentheses) }
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.replace_player, playerOut.name)) },
        text = {
            if (allOptions.isEmpty()) {
                Text(stringResource(R.string.no_players_swap))
            } else {
                val listState = rememberLazyListState()
                LazyColumn(state = listState, modifier = Modifier
                    .heightIn(max = 300.dp)
                    .simpleScrollbar(listState)) {
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
            ) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun EditPlayerDialog(player: Player, onDismiss: () -> Unit, onConfirm: (String, Boolean) -> Unit) {
    var newName by remember { mutableStateOf(player.name) }
    var isPriority by remember { mutableStateOf(player.isPriority) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_registration)) },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text(stringResource(R.string.priority))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newName.isNotBlank()) onConfirm(
                    newName,
                    isPriority
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var eloText by remember { mutableStateOf("1200") }
    var isPriority by remember { mutableStateOf(false) }

    // Validação do Elo (mínimo 1100 e máximo 1300)
    val eloValue = eloText.toIntOrNull()
    val isEloValid = eloValue != null && eloValue in 1100..1300

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_registration)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = eloText,
                    onValueChange = { newValue ->
                        // Permite esvaziar o campo e restringe o máximo em 1300
                        if (newValue.isEmpty()) {
                            eloText = newValue
                        } else {
                            val num = newValue.toIntOrNull()
                            if (num != null && num <= 1300) {
                                eloText = newValue
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.initial_elo)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = eloText.isNotEmpty() && !isEloValid // Mostra a borda vermelha se o número for inválido
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text(stringResource(R.string.set_priority))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && isEloValid) {
                        onConfirm(name, eloValue!!.toDouble(), isPriority)
                    }
                },
                enabled = name.isNotBlank() && isEloValid // Desabilita o botão se os dados não estiverem válidos
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun GroupConfigDialog(
    groupName: String,
    initialTeamSize: Int,
    initialVictoryLimit: Int,
    initialPriorityEnabled: Boolean,
    initialScoreEnabled: Boolean = true,
    initialBalancingMode: String = com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Boolean, Boolean, String) -> Unit
) {
    var teamSize by remember { mutableFloatStateOf(initialTeamSize.toFloat()) }
    var victoryLimit by remember { mutableFloatStateOf(initialVictoryLimit.toFloat()) }
    var priorityEnabled by remember { mutableStateOf(initialPriorityEnabled) }
    var scoreEnabled by remember { mutableStateOf(initialScoreEnabled) }
    var balancingMode by remember { mutableStateOf(initialBalancingMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.group_rules, groupName)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.players_per_team, teamSize.roundToInt()), fontWeight = FontWeight.Medium)
                Slider(
                    value = teamSize,
                    onValueChange = { teamSize = it },
                    valueRange = 2f..6f,
                    steps = 3
                )
                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.victory_limit, victoryLimit.roundToInt()), fontWeight = FontWeight.Medium)
                Slider(
                    value = victoryLimit,
                    onValueChange = { victoryLimit = it },
                    valueRange = 1f..6f,
                    steps = 4
                )

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.balance_mode_title), fontWeight = FontWeight.Medium)
                val modes = listOf(
                    com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name to stringResource(R.string.mode_rebalance),
                    com.bismarck.voleimanager.app.data.model.BalancingMode.WINNER_RESTS.name to stringResource(R.string.mode_winner_rests),
                    com.bismarck.voleimanager.app.data.model.BalancingMode.BOTH_REST.name to stringResource(R.string.mode_both_rest)
                )
                Spacer(Modifier.height(8.dp))
                modes.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { balancingMode = value }
                    ) {
                        RadioButton(selected = balancingMode == value, onClick = { balancingMode = value })
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { priorityEnabled = !priorityEnabled }
                ) {
                    Switch(checked = priorityEnabled, onCheckedChange = { priorityEnabled = it })
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.min_priority), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { scoreEnabled = !scoreEnabled }
                ) {
                    Switch(checked = scoreEnabled, onCheckedChange = { scoreEnabled = it })
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.use_score), style = MaterialTheme.typography.bodySmall)
                }

            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    teamSize.roundToInt(),
                    victoryLimit.roundToInt(),
                    priorityEnabled,
                    scoreEnabled,
                    balancingMode
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var balancingMode by remember { mutableStateOf(com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_new_group)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.group_name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.balance_mode_title), fontWeight = FontWeight.Medium)
                val modes = listOf(
                    com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name to stringResource(R.string.mode_rebalance),
                    com.bismarck.voleimanager.app.data.model.BalancingMode.WINNER_RESTS.name to stringResource(R.string.mode_winner_rests),
                    com.bismarck.voleimanager.app.data.model.BalancingMode.BOTH_REST.name to stringResource(R.string.mode_both_rest)
                )
                Spacer(Modifier.height(8.dp))
                modes.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { balancingMode = value }
                    ) {
                        RadioButton(selected = balancingMode == value, onClick = { balancingMode = value })
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text, balancingMode) },
                enabled = text.isNotBlank()
            ) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.bismarck.voleimanager.app.R
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_PLAYER_NAME_LENGTH
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Forces the software keyboard to open on the given [focusRequester] when a dialog appears.
 * AlertDialog hosts its content in a separate window whose softInputMode does not request the
 * IME by default, so we set it explicitly and then request focus after the first frame.
 */
@Composable
private fun DialogKeyboardFocus(focusRequester: FocusRequester) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    LaunchedEffect(Unit) {
        (view.parent as? DialogWindowProvider)?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(56.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
fun RenameGroupDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember { mutableStateOf(oldName) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_group)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= MAX_GROUP_NAME_LENGTH) newName = it },
                    label = { Text(stringResource(R.string.new_name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
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
    usesPositions: Boolean,
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
    var selectedPlayerId by remember(playerOut.id, allOptions) { mutableStateOf<Int?>(null) }
    val selectedOption = remember(selectedPlayerId, allOptions) {
        allOptions.firstOrNull { it.first.id == selectedPlayerId }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.replace_player, playerOut.name)) },
        text = {
            if (allOptions.isEmpty()) {
                Text(stringResource(R.string.no_players_swap))
            } else {
                val listState = rememberLazyListState()
                val itemShape = RoundedCornerShape(12.dp)
                Column {
                    Text(
                        text = selectedOption?.let {
                            stringResource(
                                R.string.substitution_preview,
                                playerOut.name,
                                it.first.name
                            )
                        } ?: stringResource(R.string.substitution_select_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(allOptions, key = { (playerIn, _) -> playerIn.id }) { (playerIn, label) ->
                                val selected = selectedPlayerId == playerIn.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clip(itemShape)
                                        .background(
                                            if (selected) {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable { selectedPlayerId = playerIn.id }
                                ) {
                                    ListItem(
                                        headlineContent = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    playerIn.name,
                                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                                if (usesPositions) {
                                                    PlayerPositionBadges(
                                                        player = playerIn,
                                                        usesPositions = true
                                                    )
                                                } else if (playerIn.isPriority) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() }),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        supportingContent = {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        leadingContent = { Icon(Icons.Default.Person, null, modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() })) },
                                        trailingContent = {
                                            if (selected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        LazyListFastScroller(
                            state = listState,
                            modifier = Modifier.align(Alignment.CenterEnd).offset(x = 14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedOption?.first?.let(onConfirm)
                },
                enabled = selectedOption != null
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(end = 8.dp)
            ) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun EditPlayerDialog(
    player: Player,
    usesPositions: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean, String?, String?) -> Unit
) {
    var newName by remember { mutableStateOf(player.name) }
    var isPriority by remember { mutableStateOf(player.isPriority) }
    var preferredPosition by remember { mutableStateOf(PlayerPosition.fromStoredValue(player.preferredPosition)) }
    var secondaryPosition by remember { mutableStateOf(PlayerPosition.fromStoredValue(player.secondaryPosition)) }
    val focusRequester = remember { FocusRequester() }
    val normalizedName = newName.trim().replace(Regex("\\s+"), " ")

    DialogKeyboardFocus(focusRequester)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_registration)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= MAX_PLAYER_NAME_LENGTH) newName = it },
                    label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(24.dp))
                if (usesPositions) {
                    PositionPreferenceSection(
                        preferredPosition = preferredPosition,
                        secondaryPosition = secondaryPosition,
                        onPreferredChange = {
                            preferredPosition = it
                            if (it == null || it == secondaryPosition) secondaryPosition = null
                        },
                        onSecondaryChange = { secondaryPosition = it }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(40.dp)).clickable { isPriority = !isPriority }) {
                        Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                        Text(stringResource(R.string.priority))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (normalizedName.isNotBlank()) onConfirm(
                    normalizedName,
                    isPriority,
                    preferredPosition?.name,
                    secondaryPosition?.name
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Seletores de posição preferida e segunda posição, com opção "sem preferência" (coringa). */
@Composable
private fun PositionPreferenceSection(
    preferredPosition: PlayerPosition?,
    secondaryPosition: PlayerPosition?,
    onPreferredChange: (PlayerPosition?) -> Unit,
    onSecondaryChange: (PlayerPosition?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PositionDropdown(
            label = stringResource(R.string.preferred_position),
            selected = preferredPosition,
            options = PlayerPosition.entries,
            onSelect = onPreferredChange
        )
        Spacer(Modifier.height(16.dp))
        PositionDropdown(
            label = stringResource(R.string.secondary_position),
            selected = secondaryPosition,
            // A segunda posição nunca repete a primeira.
            options = PlayerPosition.entries.filter { it != preferredPosition },
            enabled = preferredPosition != null,
            onSelect = onSecondaryChange
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.wildcard_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PositionDropdown(
    label: String,
    selected: PlayerPosition?,
    options: List<PlayerPosition>,
    onSelect: (PlayerPosition?) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val noneLabel = stringResource(R.string.position_none)
    val selectedLabel = selected?.let { positionLabel(it) } ?: noneLabel

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(noneLabel) },
                onClick = {
                    expanded = false
                    onSelect(null)
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(positionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
fun AddPlayerDialog(
    usesPositions: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Boolean, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val nameFocusRequester = remember { FocusRequester() }
    val eloLevels = remember { listOf(1100, 1150, 1200, 1250, 1300) }
    val scrollState = rememberScrollState()
    var eloIndex by rememberSaveable { mutableIntStateOf(2) }
    var isPriority by remember { mutableStateOf(false) }
    var preferredPosition by remember { mutableStateOf<PlayerPosition?>(null) }
    var secondaryPosition by remember { mutableStateOf<PlayerPosition?>(null) }
    val normalizedName = name.trim().replace(Regex("\\s+"), " ")
    val eloValue = eloLevels[eloIndex]

    DialogKeyboardFocus(nameFocusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_registration)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_PLAYER_NAME_LENGTH) name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.focusRequester(nameFocusRequester)
                )
                Spacer(Modifier.height(40.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.initial_elo),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Slider(
                    value = eloIndex.toFloat(),
                    onValueChange = { eloIndex = it.roundToInt().coerceIn(0, eloLevels.lastIndex) },
                    valueRange = 0f..eloLevels.lastIndex.toFloat(),
                    steps = eloLevels.size - 2
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    eloLevels.forEachIndexed { index, elo ->
                        val descriptionRes = when (elo) {
                            1100 -> R.string.elo_level_1100_desc
                            1150 -> R.string.elo_level_1150_desc
                            1200 -> R.string.elo_level_1200_desc
                            1250 -> R.string.elo_level_1250_desc
                            else -> R.string.elo_level_1300_desc
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isSelected = index == eloIndex
                            Text(
                                text = elo.toString(),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = stringResource(descriptionRes),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
                if (usesPositions) {
                    PositionPreferenceSection(
                        preferredPosition = preferredPosition,
                        secondaryPosition = secondaryPosition,
                        onPreferredChange = {
                            preferredPosition = it
                            if (it == null || it == secondaryPosition) secondaryPosition = null
                        },
                        onSecondaryChange = { secondaryPosition = it }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp)
                            .border(
                                width = 1.dp,
                                color = if (isPriority) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(40.dp)
                            )
                            .clip(RoundedCornerShape(40.dp))
                            .clickable { isPriority = !isPriority }) {
                        val priorityColor = if (isPriority) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.titleLarge.fontSize.toDp() }),
                            tint = priorityColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.set_priority),
                            color = priorityColor,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (normalizedName.isNotBlank()) {
                        keyboardController?.hide()
                        onConfirm(
                            normalizedName,
                            eloValue.toDouble(),
                            isPriority,
                            preferredPosition?.name,
                            secondaryPosition?.name
                        )
                    }
                },
                enabled = normalizedName.isNotBlank()
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
    initialGroupType: String = GroupType.RECREATIONAL.name,
    initialGuaranteeSetter: Boolean = true,
    isGameInProgress: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Boolean, Boolean, String, String, Boolean) -> Unit
) {
    val initialType = GroupType.fromStoredValue(initialGroupType)
    var groupType by remember { mutableStateOf(initialType) }
    var teamSize by remember { mutableFloatStateOf(initialTeamSize.toFloat()) }
    var victoryLimit by remember { mutableFloatStateOf(initialVictoryLimit.toFloat()) }
    var priorityEnabled by remember { mutableStateOf(initialPriorityEnabled) }
    var scoreEnabled by remember { mutableStateOf(initialScoreEnabled) }
    var balancingMode by remember { mutableStateOf(initialBalancingMode) }
    var guaranteeSetter by remember { mutableStateOf(initialGuaranteeSetter) }
    var showTypeChangeConfirmation by remember { mutableStateOf(false) }

    // De 6 a 7 jogadores por time, garantir levantador é obrigatório (vaga de levantador tem
    // posição específica reservada nessas composições); só é possível desligar de 2 a 5.
    val forcesGuaranteeSetter = groupType.usesPositions && teamSize.roundToInt() >= 6
    LaunchedEffect(forcesGuaranteeSetter) {
        if (forcesGuaranteeSetter) guaranteeSetter = true
    }

    val typeChanged = groupType != initialType
    // Trocar o tipo é destrutivo quando cancela uma partida ou reduz o time além do novo limite.
    val changeIsLossy = typeChanged && (isGameInProgress || initialTeamSize > groupType.maxTeamSize)

    fun save() {
        onConfirm(
            teamSize.roundToInt(),
            victoryLimit.roundToInt(),
            priorityEnabled && groupType.supportsPriority,
            scoreEnabled,
            balancingMode,
            groupType.name,
            guaranteeSetter || forcesGuaranteeSetter
        )
    }

    if (showTypeChangeConfirmation) {
        AlertDialog(
            onDismissRequest = { showTypeChangeConfirmation = false },
            title = { Text(stringResource(R.string.group_type_change_confirm_title)) },
            text = { Text(stringResource(R.string.group_type_change_confirm_message)) },
            confirmButton = {
                Button(onClick = {
                    showTypeChangeConfirmation = false
                    save()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showTypeChangeConfirmation = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.group_rules, groupName)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.group_type_title), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.group_type_long_press_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                GroupType.selectableTypes.forEach { type ->
                    GroupTypeOptionRow(
                        type = type,
                        selected = groupType == type,
                        onSelect = {
                            groupType = type
                            teamSize = type.coerceTeamSize(teamSize.roundToInt()).toFloat()
                            if (!type.supportsPriority) priorityEnabled = false
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Text(stringResource(R.string.balance_mode_title), fontWeight = FontWeight.Medium)
                val modes = listOf(
                    Triple(
                        com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name,
                        stringResource(R.string.mode_rebalance),
                        stringResource(R.string.mode_rebalance_tooltip)
                    ),
                    Triple(
                        com.bismarck.voleimanager.app.data.model.BalancingMode.REST.name,
                        stringResource(R.string.mode_rest),
                        stringResource(R.string.mode_rest_tooltip)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.balance_mode_long_press_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                modes.forEach { (value, label, tooltip) ->
                    BalancingModeOptionRow(
                        label = label,
                        tooltip = tooltip,
                        selected = balancingMode == value,
                        onSelect = { balancingMode = value },
                        iconRes = if (value == com.bismarck.voleimanager.app.data.model.BalancingMode.REBALANCE.name) {
                            R.drawable.arrowsbothsides
                        } else {
                            R.drawable.zzz_rest
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.players_per_team, teamSize.roundToInt()), fontWeight = FontWeight.Medium)
                Slider(
                    value = teamSize,
                    onValueChange = {
                        teamSize = groupType.coerceTeamSize(it.roundToInt()).toFloat()
                    },
                    valueRange = groupType.minTeamSize.toFloat()..groupType.maxTeamSize.toFloat(),
                    steps = (groupType.maxTeamSize - groupType.minTeamSize) - 1
                )
                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.victory_limit, victoryLimit.roundToInt()), fontWeight = FontWeight.Medium)
                Slider(
                    value = victoryLimit,
                    onValueChange = { victoryLimit = it.coerceIn(2f, 6f) },
                    valueRange = 2f..6f,
                    steps = 3
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                if (groupType.supportsPriority) {
                    TooltipToggleRow(
                        label = stringResource(R.string.min_priority),
                        tooltip = stringResource(R.string.min_priority_tooltip),
                        checked = priorityEnabled,
                        onCheckedChange = { priorityEnabled = it }
                    )
                    Spacer(Modifier.height(8.dp))
                } else if (groupType.usesPositions) {
                    TooltipToggleRow(
                        label = stringResource(R.string.guarantee_setter),
                        tooltip = if (forcesGuaranteeSetter) {
                            stringResource(R.string.guarantee_setter_locked_tooltip)
                        } else {
                            stringResource(R.string.guarantee_setter_tooltip)
                        },
                        checked = guaranteeSetter || forcesGuaranteeSetter,
                        onCheckedChange = { guaranteeSetter = it },
                        enabled = !forcesGuaranteeSetter
                    )
                    Spacer(Modifier.height(8.dp))
                }
                TooltipToggleRow(
                    label = stringResource(R.string.use_score),
                    tooltip = stringResource(R.string.use_score_tooltip),
                    checked = scoreEnabled,
                    onCheckedChange = { scoreEnabled = it }
                )

            }
        },
        confirmButton = {
            Button(onClick = {
                if (changeIsLossy) showTypeChangeConfirmation = true else save()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BalancingModeOptionRow(
    label: String,
    tooltip: String,
    selected: Boolean,
    onSelect: () -> Unit,
    iconRes: Int,
    iconSize: Dp = 20.dp
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(
                    text = tooltip,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        state = tooltipState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .combinedClickable(
                    onClick = {
                        tooltipState.dismiss()
                        onSelect()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                )
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    tooltipState.dismiss()
                    onSelect()
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TooltipToggleRow(
    label: String,
    tooltip: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(
                    text = tooltip,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        state = tooltipState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .combinedClickable(
                    enabled = enabled,
                    onClick = {
                        tooltipState.dismiss()
                        onCheckedChange(!checked)
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                )
        ) {
            Switch(checked = checked, enabled = enabled, onCheckedChange = {
                tooltipState.dismiss()
                onCheckedChange(it)
            })
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf(GroupType.RECREATIONAL.name) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_new_group)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= MAX_GROUP_NAME_LENGTH) text = it },
                    label = { Text(stringResource(R.string.group_name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.group_type_title), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.group_type_long_press_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                GroupType.selectableTypes.forEach { type ->
                    GroupTypeOptionRow(
                        type = type,
                        selected = groupType == type.name,
                        onSelect = { groupType = type.name }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text, groupType) },
                enabled = text.isNotBlank()
            ) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Opção de tipo de grupo com título e tooltip (toque longo) explicando o modo. */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupTypeOptionRow(
    type: GroupType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(
                    text = groupTypeDescription(type),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        state = tooltipState
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = {
                        tooltipState.dismiss()
                        onSelect()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                )
                .padding(vertical = 4.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    tooltipState.dismiss()
                    onSelect()
                }
            )
            Spacer(Modifier.width(8.dp))
            Text(text = groupTypeLabel(type), fontWeight = FontWeight.Medium)
        }
    }
}

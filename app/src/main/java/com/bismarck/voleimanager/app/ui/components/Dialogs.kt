package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.automirrored.filled.RotateRight
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import android.view.WindowManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Scoreboard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import java.util.Locale
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_PLAYER_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.TeamAccentColor
import com.bismarck.voleimanager.app.ui.theme.teamAccentColorFamily
import androidx.compose.material.icons.filled.Lock
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

/**
 * Seletor das cores de Time A e Time B (recurso premium). Quando [hasPremiumAccess] é `false`,
 * as opções aparecem esmaecidas com um cadeado e tocar nelas não faz nada além de mostrar
 * [onLockedClick] — a tela real de assinatura ainda não existe (ver todo `cloud-sync-screen`),
 * então por ora isso só serve para o usuário entender que precisa de premium.
 */
@Composable
fun TeamColorPickerSection(
    teamAColor: TeamAccentColor,
    teamBColor: TeamAccentColor,
    hasPremiumAccess: Boolean,
    isDarkTheme: Boolean,
    onColorsSelected: (TeamAccentColor, TeamAccentColor) -> Unit,
    onLockedClick: () -> Unit,
    title: String? = stringResource(R.string.team_colors_title),
    hint: String? = if (hasPremiumAccess) {
        stringResource(R.string.team_colors_hint)
    } else {
        stringResource(R.string.team_colors_locked_hint)
    }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (!hasPremiumAccess) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp, top = 2.dp)
            )
        }
        Text(stringResource(R.string.team_a), style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TeamAccentColor.entries.forEach { accent ->
                ColorSwatch(
                    accent = accent,
                    isDarkTheme = isDarkTheme,
                    selected = accent == teamAColor,
                    enabled = hasPremiumAccess,
                    onClick = {
                        if (hasPremiumAccess) {
                            val newB = if (accent == teamBColor) teamAColor else teamBColor
                            onColorsSelected(accent, newB)
                        } else {
                            onLockedClick()
                        }
                    }
                )
            }
        }
        Text(
            stringResource(R.string.team_b),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TeamAccentColor.entries.forEach { accent ->
                ColorSwatch(
                    accent = accent,
                    isDarkTheme = isDarkTheme,
                    selected = accent == teamBColor,
                    enabled = hasPremiumAccess,
                    onClick = {
                        if (hasPremiumAccess) {
                            val newA = if (accent == teamAColor) teamBColor else teamAColor
                            onColorsSelected(newA, accent)
                        } else {
                            onLockedClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    accent: TeamAccentColor,
    isDarkTheme: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val swatchColor = teamAccentColorFamily(accent, isDarkTheme).color
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(swatchColor.copy(alpha = if (enabled) 1f else 0.35f))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = teamAccentColorFamily(accent, isDarkTheme).onColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TelemetryConsentDialog(onAllow: () -> Unit, onDeny: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = { Text(stringResource(R.string.telemetry_consent_title, stringResource(R.string.app_name))) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.telemetry_consent_body))
            }
        },
        confirmButton = {
            Button(onClick = onAllow) { Text(stringResource(R.string.telemetry_consent_allow)) }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(stringResource(R.string.telemetry_consent_deny), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
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
    teamSize: Int,
    assignedPositions: Map<Int, PlayerPosition>,
    assignedSlotIndices: Map<Int, Int>,
    onDismiss: () -> Unit,
    onConfirm: (Player) -> Unit
) {
    val waiting_parentheses = stringResource(R.string.waiting_parentheses)
    val team_a_parentheses = stringResource(R.string.team_a_parentheses)
    val team_b_parentheses = stringResource(R.string.team_b_parentheses)
    val team_bench_parentheses = stringResource(R.string.team_bench_parentheses)
    val allOptions = remember(waitingList, teamA, teamB, playerOut, usesPositions, teamSize, assignedPositions, assignedSlotIndices) {
        // rank: 0 = topo do diálogo, maior = mais abaixo na lista.
        val list = mutableListOf<Triple<Player, String, Int>>()
        val isTeamA = teamA.any { it.id == playerOut.id }
        val isTeamB = teamB.any { it.id == playerOut.id }
        val activeTeam = when {
            isTeamA -> teamA
            isTeamB -> teamB
            else -> emptyList()
        }
        val opposingTeam = when {
            isTeamA -> teamB
            isTeamB -> teamA
            else -> emptyList()
        }
        val ownTeamLabel = if (isTeamA) team_a_parentheses else team_b_parentheses
        val opposingTeamLabel = if (isTeamA) team_b_parentheses else team_a_parentheses
        // No 7x7, substituir o líbero do banco (ou quem estiver no banco de reserva) prioriza
        // primeiro os próprios colegas de time, depois a fila de espera e por fim o time adversário.
        val isBenchReserve = usesPositions && teamSize == 7 &&
            assignedPositions[playerOut.id] == PlayerPosition.LIBERO
        if (isBenchReserve) {
            activeTeam.filter { it.id != playerOut.id }.forEach { list.add(Triple(it, ownTeamLabel, 0)) }
            waitingList.forEach { list.add(Triple(it, waiting_parentheses, 1)) }
            opposingTeam.forEach { list.add(Triple(it, opposingTeamLabel, 2)) }
        } else {
            // Fora do banco: a substituição especial do líbero (entra sem contar como troca normal)
            // continua aparecendo no topo quando existir, seguida da fila de espera e do time adversário.
            val benchLibero = if (usesPositions && teamSize == 7) {
                activeTeam.firstOrNull { it.id != playerOut.id && assignedPositions[it.id] == PlayerPosition.LIBERO }
            } else {
                null
            }
            benchLibero?.let { list.add(Triple(it, team_bench_parentheses, 0)) }
            waitingList.forEach { list.add(Triple(it, waiting_parentheses, 1)) }
            if (isTeamA) teamB.forEach { list.add(Triple(it, team_b_parentheses, 2)) }
            else if (isTeamB) teamA.forEach { list.add(Triple(it, team_a_parentheses, 2)) }
            else {
                teamA.forEach { list.add(Triple(it, team_a_parentheses, 2)) }
                teamB.forEach { list.add(Triple(it, team_b_parentheses, 2)) }
            }
        }
        list.sortedWith(
            compareBy<Triple<Player, String, Int>>(
                { it.third },
                { assignedSlotIndices[it.first.id] ?: Int.MAX_VALUE },
                { it.first.name.lowercase() }
            )
        ).map { it.first to it.second }
    }
    var selectedPlayerId by remember(playerOut.id, allOptions) {
        mutableStateOf(
            allOptions.firstOrNull { it.second == team_bench_parentheses }?.first?.id
        )
    }
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
                                            PlayerNameWithPositionBadges(
                                                player = playerIn,
                                                usesPositions = usesPositions,
                                                nameFontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                                isPriority = playerIn.isPriority,
                                                priorityIconSize = with(LocalDensity.current) { MaterialTheme.typography.bodyMedium.fontSize.toDp() },
                                                priorityTint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 4.dp)
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
    val normalizedName = newName.trim().replace(Regex("\\s+"), " ")

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
                    singleLine = true
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
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() }),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
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
            trailingIcon = {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded && enabled) 180f else 0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "PositionDropdownRotation"
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.keyboard_arrow_down),
                    modifier = Modifier
                        .rotate(rotation)
                        .size(24.dp)
                )
            },
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
    initialBalancingMode: String = BalancingMode.REBALANCE.name,
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

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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

    // Bloco esquerdo: tipo de grupo + modo de balanceamento (mesma ordem de sempre).
    val groupTypeAndBalancingContent: @Composable ColumnScope.() -> Unit = {
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
                    val previousType = groupType
                    groupType = type
                    teamSize = type.coerceTeamSize(teamSize.roundToInt()).toFloat()
                    victoryLimit = victoryLimit.roundToInt().coerceIn(2, type.maxTeamSize).toFloat()
                    if (!type.supportsPriority) {
                        priorityEnabled = false
                    } else if (!previousType.supportsPriority) {
                        // Volta ao padrão (ativado) ao sair de um tipo sem prioridade.
                        priorityEnabled = true
                    }
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
                BalancingMode.REBALANCE.name,
                stringResource(R.string.mode_rebalance),
                stringResource(R.string.mode_rebalance_tooltip)
            ),
            Triple(
                BalancingMode.REST.name,
                stringResource(R.string.mode_rest),
                stringResource(R.string.mode_rest_tooltip)
            )
        )

        Spacer(Modifier.height(8.dp))
        modes.forEach { (value, label, tooltip) ->
            BalancingModeOptionRow(
                label = label,
                tooltip = tooltip,
                selected = balancingMode == value,
                onSelect = { balancingMode = value },
                iconRes = if (value == BalancingMode.REBALANCE.name) {
                    R.drawable.arrowsbothsides
                } else {
                    R.drawable.zzz_rest
                }
            )
        }
    }

    // Bloco direito: tamanhos/limites (sliders) + interruptores.
    val teamSizeAndTogglesContent: @Composable ColumnScope.() -> Unit = {
        TooltipLabelRow(
            label = stringResource(R.string.players_per_team, teamSize.roundToInt()),
            tooltip = stringResource(R.string.players_per_team_tooltip),
            icon = {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        Slider(
            value = teamSize,
            onValueChange = {
                teamSize = groupType.coerceTeamSize(it.roundToInt()).toFloat()
            },
            valueRange = groupType.minTeamSize.toFloat()..groupType.maxTeamSize.toFloat(),
            steps = (groupType.maxTeamSize - groupType.minTeamSize) - 1
        )
        Spacer(Modifier.height(16.dp))

        TooltipLabelRow(
            label = stringResource(R.string.victory_limit, victoryLimit.roundToInt()),
            tooltip = stringResource(R.string.victory_limit_tooltip),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.crown_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        Slider(
            value = victoryLimit,
            onValueChange = { victoryLimit = it.coerceIn(2f, groupType.maxTeamSize.toFloat()) },
            valueRange = 2f..groupType.maxTeamSize.toFloat(),
            steps = groupType.maxTeamSize - 3
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
                onCheckedChange = { priorityEnabled = it },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                icon = {
                    Icon(
                        painter = painterResource(setterIconRes()),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
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
            onCheckedChange = { scoreEnabled = it },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Scoreboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = if (isLandscape) Modifier.fillMaxWidth(0.95f) else Modifier,
        properties = if (isLandscape) {
            DialogProperties(usePlatformDefaultWidth = false)
        } else {
            DialogProperties()
        },
        title = { Text(stringResource(R.string.group_rules, groupName)) },
        text = {
            if (isLandscape) {
                // Tela horizontal: duas colunas lado a lado para aproveitar o espaço lateral extra.
                Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(Modifier.height(8.dp))
                        groupTypeAndBalancingContent()
                    }
                    VerticalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Spacer(Modifier.height(8.dp))
                        teamSizeAndTogglesContent()
                    }
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(8.dp))
                    groupTypeAndBalancingContent()
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    teamSizeAndTogglesContent()
                }
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
                .height(48.dp)
                .clip(CircleShape)
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
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Rótulo com ícone (usado acima de um Slider) que revela sua explicação via toque longo. */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TooltipLabelRow(
    label: String,
    tooltip: String,
    icon: @Composable () -> Unit
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
                .clip(CircleShape)
                .combinedClickable(
                    onClick = { tooltipState.dismiss() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            tooltipState.show()
                        }
                    }
                )
                // O clip em CircleShape arredonda as pontas de um Row largo, o que corta um
                // ícone encostado bem na borda esquerda; este padding afasta o conteúdo da
                // curva sem reduzir a área de toque/ripple (que continua no bounds do clip).
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            icon()
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.Medium)
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
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
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
                .clip(CircleShape)
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
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = {
                    tooltipState.dismiss()
                    onCheckedChange(it)
                },
                // Default M3 disabled colors use ~12% alpha, which blends almost invisibly
                // into the dialog's surface in both light and dark theme. Bump the alpha a
                // bit so a locked/disabled toggle still reads as a dimmed switch, not a gap.
                colors = SwitchDefaults.colors(
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    disabledCheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            )
            Spacer(Modifier.width(16.dp))
            if (icon != null) {
                icon()
                Spacer(Modifier.width(8.dp))
            }
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
                .height(48.dp)
                .clip(CircleShape)
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
            Icon(
                imageVector = groupTypeIcon(type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = groupTypeLabel(type),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Diálogo de "Entrar em um grupo existente" (Auxiliar/Espectador), acessível pelo menu de troca
 * de grupo (abaixo de "+ Criar novo grupo") e pelo ícone no canto superior direito da tela "Ao
 * vivo". Sem o backend de sincronização (`firestore-sync-engine`), o código apenas define o
 * papel pelo prefixo ("AUX"/"ESP") — ver [com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel.joinGroupWithCode].
 */
@Composable
fun JoinExistingGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, (String?) -> Unit) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.join_existing_group)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.join_existing_group_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it; errorMessage = null },
                    label = { Text(stringResource(R.string.join_existing_group_code_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                errorMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(code) { error ->
                        if (error == null) onDismiss() else errorMessage = error
                    }
                },
                enabled = code.isNotBlank()
            ) { Text(stringResource(R.string.join_existing_group_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo de login com e-mail/senha (Firebase Auth). */
@Composable
fun LoginDialog(
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, (String?) -> Unit) -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.login_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text(stringResource(R.string.email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                errorMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSwitchToSignUp) {
                    Text(stringResource(R.string.login_switch_to_signup))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(email, password) { error -> if (error == null) onDismiss() else errorMessage = error }
                },
                enabled = !inProgress && email.isNotBlank() && password.isNotBlank()
            ) { Text(stringResource(R.string.login_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Insere automaticamente as barras separadoras enquanto o usuário digita uma data de nascimento
 *  (`DD/MM/AAAA`) — mantém apenas dígitos e reformata a cada mudança, inserindo a barra logo após
 *  o 2º dígito do dia e o 2º dígito do mês (sem esperar o dígito seguinte), para evitar erros de
 *  digitação. Opera sobre [TextFieldValue] (não apenas a `String`) para poder recolocar o cursor
 *  explicitamente na posição correta após a reformatação — sem isso, o Compose tende a deixar o
 *  cursor no fim do texto sempre que o valor muda, fazendo os dígitos seguintes serem inseridos no
 *  lugar errado assim que uma barra é adicionada no meio da digitação (ex.: mês/ano trocados). Um
 *  único backspace logo após uma barra recém-inserida remove o dígito e a barra juntos (em vez de
 *  deixar uma "barra presa" que o próximo dígito reformataria de volta). */
private fun autoFormatBirthDate(previous: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    val cursorAfterDeletion = newValue.selection.end
    val isSingleCharDeletion = newValue.selection.collapsed &&
        newValue.text.length == previous.text.length - 1 &&
        cursorAfterDeletion in previous.text.indices &&
        previous.text.removeRange(cursorAfterDeletion, cursorAfterDeletion + 1) == newValue.text

    var workingText = newValue.text
    var cursor = newValue.selection.end
    if (isSingleCharDeletion && previous.text.getOrNull(cursorAfterDeletion) == '/' && cursor > 0) {
        // O usuário apagou uma barra recém-inserida automaticamente — remove também o dígito
        // anterior a ela, como se fosse um único caractere lógico.
        workingText = workingText.removeRange(cursor - 1, cursor)
        cursor -= 1
    }

    val digits = workingText.filter { it.isDigit() }.take(8)
    val digitsBeforeCursor = workingText.take(cursor).count { it.isDigit() }.coerceAtMost(digits.length)

    val sb = StringBuilder()
    var newCursor = 0
    digits.forEachIndexed { i, c ->
        sb.append(c)
        if (i == 1 || i == 3) sb.append('/')
        if (i + 1 == digitsBeforeCursor) newCursor = sb.length
    }
    return TextFieldValue(sb.toString(), TextRange(newCursor.coerceIn(0, sb.length)))
}


private fun parseBirthDateToIso(input: String): String? {
    val parts = input.trim().split("/")
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    if (day !in 1..31 || month !in 1..12 || year < 1900 || year > 2100) return null
    return "%04d-%02d-%02d".format(year, month, day)
}

/** Converte uma data ISO `yyyy-MM-dd` (guardada no perfil) de volta para `DD/MM/AAAA`, para
 *  exibir num campo de texto editável. */
private fun formatBirthDateFromIso(iso: String?): String {
    if (iso == null) return ""
    val parts = iso.split("-")
    if (parts.size != 3) return ""
    return "${parts[2]}/${parts[1]}/${parts[0]}"
}

/** Diálogo de cadastro gratuito com e-mail/senha (Firebase Auth) — direcionado a
 *  Organizador(a)/Auxiliar antes de assinar um pacote premium. Coleta nome completo, apelido
 *  público (exibido no topo do app) e data de nascimento (guardada para uma futura verificação
 *  de elegibilidade de compra premium). */
@Composable
fun SignUpDialog(
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, (String?) -> Unit) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var birthDateValue by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.signup_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = null },
                    label = { Text(stringResource(R.string.full_name_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it; errorMessage = null },
                    label = { Text(stringResource(R.string.nickname_label)) },
                    supportingText = { Text(stringResource(R.string.nickname_hint)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = birthDateValue,
                    onValueChange = { birthDateValue = autoFormatBirthDate(birthDateValue, it); errorMessage = null },
                    label = { Text(stringResource(R.string.birth_date_label)) },
                    placeholder = { Text(stringResource(R.string.birth_date_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text(stringResource(R.string.email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                errorMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSwitchToLogin) {
                    Text(stringResource(R.string.signup_switch_to_login))
                }
            }
        },
        confirmButton = {
            val birthDateHelp = stringResource(R.string.birth_date_invalid)
            Button(
                onClick = {
                    val birthIso = parseBirthDateToIso(birthDateValue.text)
                    if (birthIso == null) {
                        errorMessage = birthDateHelp
                        return@Button
                    }
                    onConfirm(email, password, fullName, nickname, birthIso) { error ->
                        if (error == null) onDismiss() else errorMessage = error
                    }
                },
                enabled = !inProgress && email.isNotBlank() && password.isNotBlank() &&
                    fullName.isNotBlank() && nickname.isNotBlank() && birthDateValue.text.isNotBlank()
            ) { Text(stringResource(R.string.signup_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo de foto de perfil: permite escolher uma nova foto na galeria (a redução/compressão é
 *  feita pelo chamador, ver [com.bismarck.voleimanager.app.util.encodeAvatarBase64]) e, se já
 *  houver uma foto salva, removê-la. */
@Composable
fun EditProfilePhotoDialog(
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (hasPhoto) stringResource(R.string.edit_profile_photo_title)
                else stringResource(R.string.add_profile_photo_title)
            )
        },
        text = {
            Column {
                TextButton(onClick = { onPickPhoto(); onDismiss() }) {
                    Text(stringResource(R.string.profile_photo_choose_from_gallery))
                }
                if (hasPhoto) {
                    TextButton(onClick = { onRemovePhoto(); onDismiss() }) {
                        Text(stringResource(R.string.profile_photo_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo de edição de perfil: nome completo, apelido público e data de nascimento, além da
 *  opção (com confirmação separada) de apagar a conta definitivamente. */
@Composable
fun EditProfileDialog(
    inProgress: Boolean,
    initialFullName: String,
    initialNickname: String,
    initialBirthDateIso: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, (String?) -> Unit) -> Unit,
    onRequestDeleteAccount: () -> Unit
) {
    var fullName by remember { mutableStateOf(initialFullName) }
    var nickname by remember { mutableStateOf(initialNickname) }
    var birthDateValue by remember { mutableStateOf(TextFieldValue(formatBirthDateFromIso(initialBirthDateIso))) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = null },
                    label = { Text(stringResource(R.string.full_name_label)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it; errorMessage = null },
                    label = { Text(stringResource(R.string.nickname_label)) },
                    supportingText = { Text(stringResource(R.string.nickname_hint)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = birthDateValue,
                    onValueChange = { birthDateValue = autoFormatBirthDate(birthDateValue, it); errorMessage = null },
                    label = { Text(stringResource(R.string.birth_date_label)) },
                    placeholder = { Text(stringResource(R.string.birth_date_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                errorMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRequestDeleteAccount) {
                    Text(stringResource(R.string.delete_account_action), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            val birthDateHelp = stringResource(R.string.birth_date_invalid)
            Button(
                onClick = {
                    val birthIso = parseBirthDateToIso(birthDateValue.text)
                    if (birthIso == null) {
                        errorMessage = birthDateHelp
                        return@Button
                    }
                    onConfirm(nickname, fullName, birthIso) { error -> if (error == null) onDismiss() else errorMessage = error }
                },
                enabled = !inProgress && fullName.isNotBlank() && nickname.isNotBlank() && birthDateValue.text.isNotBlank()
            ) { Text(stringResource(R.string.edit_profile_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo de confirmação (segunda etapa, separada da edição de perfil) antes de apagar a conta
 *  definitivamente — ação irreversível. */
@Composable
fun DeleteAccountConfirmDialog(
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account_title)) },
        text = { Text(stringResource(R.string.delete_account_warning)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !inProgress,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.delete_account_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo para o organizador solicitar a transferência de posse de um grupo premium para um(a)
 *  auxiliar (identificado por e-mail). A efetivação real depende do backend de sincronização. */
@Composable
fun TransferGroupOwnershipDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var targetEmail by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    DialogKeyboardFocus(focusRequester)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transfer_ownership_title, groupName)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.transfer_ownership_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetEmail,
                    onValueChange = { targetEmail = it },
                    label = { Text(stringResource(R.string.transfer_ownership_email_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (targetEmail.isNotBlank()) onConfirm(targetEmail) },
                enabled = targetEmail.isNotBlank()
            ) { Text(stringResource(R.string.transfer_ownership_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

/** Diálogo de corte/rotação da foto de perfil escolhida na galeria: exibe [sourceBitmap] num
 *  visor quadrado onde o usuário pode arrastar (pan) e beliscar para dar zoom (pinch), além de um
 *  botão para girar em passos de 90°. Ao confirmar, gera o recorte final (ver
 *  [com.bismarck.voleimanager.app.util.cropAvatarBitmap]) e o repassa via [onConfirm]. */
@Composable
fun AvatarCropDialog(
    sourceBitmap: android.graphics.Bitmap,
    onDismiss: () -> Unit,
    onConfirm: (android.graphics.Bitmap) -> Unit
) {
    var rotationSteps by remember { mutableIntStateOf(0) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewportDp = 240.dp
    val viewportPx = with(density) { viewportDp.toPx() }

    val rotatedBitmap = remember(sourceBitmap, rotationSteps) {
        com.bismarck.voleimanager.app.util.rotateAvatarBitmap(sourceBitmap, rotationSteps * 90)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.avatar_crop_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.avatar_crop_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(viewportDp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .pointerInput(rotatedBitmap) {
                            detectTransformGestures { _, panDelta, zoomDelta, _ ->
                                val newZoom = (zoom * zoomDelta).coerceIn(1f, 4f)
                                val maxPan = com.bismarck.voleimanager.app.util.maxAvatarPan(rotatedBitmap, newZoom, viewportPx)
                                val newPan = androidx.compose.ui.geometry.Offset(
                                    (pan.x + panDelta.x).coerceIn(-maxPan.x, maxPan.x),
                                    (pan.y + panDelta.y).coerceIn(-maxPan.y, maxPan.y)
                                )
                                zoom = newZoom
                                pan = newPan
                            }
                        }
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = rotatedBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = pan.x
                                translationY = pan.y
                            }
                    )
                }
                Spacer(Modifier.height(8.dp))
                IconButton(onClick = {
                    rotationSteps = (rotationSteps + 1) % 4
                    zoom = 1f
                    pan = androidx.compose.ui.geometry.Offset.Zero
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = stringResource(R.string.avatar_crop_rotate)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val result = com.bismarck.voleimanager.app.util.cropAvatarBitmap(rotatedBitmap, zoom, pan, viewportPx)
                onConfirm(result)
            }) { Text(stringResource(R.string.avatar_crop_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}

package com.example.voleimanager

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.voleimanager.data.AppDatabase
import com.example.voleimanager.data.VoleiRepository
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.ui.*
import com.example.voleimanager.ui.ManualSetupScreen
import com.example.voleimanager.ui.viewmodel.Screen
import com.example.voleimanager.ui.viewmodel.ThemeMode
import com.example.voleimanager.ui.viewmodel.CsvType
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import com.example.voleimanager.ui.viewmodel.VoleiViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val repository = VoleiRepository(database.voleiDao())
        val viewModelFactory = VoleiViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VoleiViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when(themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val lightColors = lightColorScheme(primary = Color(0xFF212121), onPrimary = Color.White, primaryContainer = Color(0xFFE0E0E0), onPrimaryContainer = Color(0xFF212121), secondary = Color(0xFF616161), background = Color(0xFFFAFAFA), surface = Color(0xFFFFFFFF))
            val darkColors = darkColorScheme(primary = Color(0xFFEEEEEE), onPrimary = Color.Black, primaryContainer = Color(0xFF424242), onPrimaryContainer = Color.White, secondary = Color(0xFFB0B0B0), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))

            MaterialTheme(colorScheme = if(darkTheme) darkColors else lightColors) { VoleiManagerApp(viewModel, darkTheme) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val showElo by viewModel.showElo.collectAsState()
    val uniqueGroups = remember(allPlayers) { allPlayers.map { it.groupName }.distinct().sorted() }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    // Dialogs
    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showRenameGroupDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteGroupDialog by remember { mutableStateOf<String?>(null) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    // Alerta de Troca de Grupo
    var pendingGroupSwitch by remember { mutableStateOf<String?>(null) }

    // Dialogs de Importação/Exportação
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("volei_data") }
    var pendingImportType by remember { mutableStateOf(CsvType.JOGADORES) }

    val launcherImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importData(it, pendingImportType, context) }
    }

    LaunchedEffect(uniqueGroups) { if (selectedGroup == null && uniqueGroups.isNotEmpty()) selectedGroup = uniqueGroups.first() }
    LaunchedEffect(selectedGroup) { selectedGroup?.let { viewModel.loadGroupConfig(it) } }

    if (pendingGroupSwitch != null) {
        AlertDialog(
            onDismissRequest = { pendingGroupSwitch = null },
            title = { Text("Mudar de grupo?") },
            text = { Text("Existe um jogo em andamento. Se mudar de grupo agora, o progresso da partida atual será perdido.") },
            confirmButton = { Button(onClick = {
                selectedGroup = pendingGroupSwitch
                viewModel.loadGroupConfig(pendingGroupSwitch!!)
                pendingGroupSwitch = null
            }) { Text("Mudar mesmo assim") } },
            dismissButton = { TextButton(onClick = { pendingGroupSwitch = null }) { Text("Cancelar") } }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Dados") },
            text = { Column {
                OutlinedTextField(value = exportFileName, onValueChange = { exportFileName = it }, label = { Text("Nome do arquivo") })
                Spacer(Modifier.height(16.dp))
                Button(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.exportData(context, CsvType.BACKUP_COMPLETO, exportFileName); showExportDialog = false }) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Backup Completo (.json)") }
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Exportar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { viewModel.exportData(context, CsvType.JOGADORES, exportFileName); showExportDialog = false }) { Text("Jogadores") }
                    TextButton(onClick = { viewModel.exportData(context, CsvType.HISTORICO, exportFileName); showExportDialog = false }) { Text("Histórico") }
                    TextButton(onClick = { viewModel.exportData(context, CsvType.ELO_LOGS, exportFileName); showExportDialog = false }) { Text("Logs") }
                }
            }},
            confirmButton = { TextButton(onClick = { showExportDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Dados") },
            text = { Column {
                Button(modifier = Modifier.fillMaxWidth(), onClick = { pendingImportType = CsvType.BACKUP_COMPLETO; launcherImport.launch(arrayOf("application/json", "text/plain")); showImportDialog = false }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Restaurar Backup (.json)") }
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Importar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { pendingImportType = CsvType.JOGADORES; launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv")); showImportDialog = false }) { Text("Jogadores") }
                    TextButton(onClick = { pendingImportType = CsvType.HISTORICO; launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv")); showImportDialog = false }) { Text("Histórico") }
                    TextButton(onClick = { pendingImportType = CsvType.ELO_LOGS; launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv")); showImportDialog = false }) { Text("Logs") }
                }
            }},
            confirmButton = { TextButton(onClick = { showImportDialog = false }) { Text("Cancelar") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Text("Vôlei Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider(Modifier.padding(vertical = 16.dp))

                    Text("Grupo Atual:", style = MaterialTheme.typography.labelMedium)
                    var groupExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = !groupExpanded }) {
                        OutlinedTextField(
                            value = selectedGroup ?: "Selecione",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                            uniqueGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Text(group, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { showRenameGroupDialog = group; groupExpanded = false }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp)) }
                                            IconButton(onClick = { showDeleteGroupDialog = group; groupExpanded = false }) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                                        }
                                    },
                                    onClick = {
                                        groupExpanded = false
                                        if (selectedGroup != group) {
                                            if (viewModel.isGameInProgress()) pendingGroupSwitch = group
                                            else { selectedGroup = group; viewModel.loadGroupConfig(group) }
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(text = { Text("+ Criar Novo Grupo", fontWeight = FontWeight.Bold) }, onClick = { showCreateGroupDialog = true; groupExpanded = false })
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    NavigationDrawerItem(icon = { Icon(Icons.Default.PlayArrow, null) }, label = { Text("Jogo / Partida") }, selected = currentScreen == Screen.GAME, onClick = { viewModel.navigateTo(Screen.GAME); scope.launch { drawerState.close() } })
                    NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Histórico") }, selected = currentScreen == Screen.HISTORY, onClick = { viewModel.navigateTo(Screen.HISTORY); scope.launch { drawerState.close() } })

                    Divider(Modifier.padding(vertical = 16.dp))
                    Text("Configurações", style = MaterialTheme.typography.labelMedium)
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Regras do Grupo") }, selected = false, onClick = { showConfigDialog = true; scope.launch { drawerState.close() } })
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Tema") }, selected = false, onClick = { showThemeDialog = true; scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Star, null) }, 
                        label = { Text("Mostrar Elo") }, 
                        selected = false, 
                        badge = { Switch(checked = showElo, onCheckedChange = null) },
                        onClick = { viewModel.setShowElo(!showElo) }
                    )
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Dados", style = MaterialTheme.typography.labelMedium)
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Share, null) }, label = { Text("Backup / Exportar") }, selected = false, onClick = { showExportDialog = true; scope.launch { drawerState.close() } })
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Add, null) }, label = { Text("Restaurar / Importar") }, selected = false, onClick = { showImportDialog = true; scope.launch { drawerState.close() } })
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    ) {
        if (showConfigDialog) {
            val config by viewModel.currentGroupConfig.collectAsState()
            GroupConfigDialog(
                groupName = selectedGroup ?: "Geral",
                initialTeamSize = config.teamSize,
                initialVictoryLimit = config.victoryLimit,
                initialPriorityEnabled = config.priorityEnabled,
                onDismiss = { showConfigDialog = false },
                onConfirm = { size, limit, prior ->
                    viewModel.updateConfig(size, limit, prior)
                    showConfigDialog = false
                }
            )
        }
        if (showCreateGroupDialog) CreateGroupDialog({ showCreateGroupDialog = false }, { newName -> selectedGroup = newName; viewModel.loadGroupConfig(newName); showCreateGroupDialog = false })
        if (showAddPlayerDialog) AddPlayerDialog({ showAddPlayerDialog = false }, { name, elo, isPriority -> viewModel.addPlayer(name, elo, selectedGroup ?: "Geral", isPriority); showAddPlayerDialog = false })
        if (showThemeDialog) { val mode by viewModel.themeMode.collectAsState(); AlertDialog(onDismissRequest = { showThemeDialog = false }, title = { Text("Tema") }, text = { Column { ThemeOption("Sistema", mode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }; ThemeOption("Claro", mode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }; ThemeOption("Escuro", mode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) } } }, confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Fechar") } }) }
        playerToDelete?.let { player -> AlertDialog(onDismissRequest = { playerToDelete = null }, title = { Text("Excluir ${player.name}?") }, text = { Text("O jogador será removido da lista ativa, mas seu histórico de partidas e registros de Elo SERÃO MANTIDOS.") }, confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = { viewModel.deletePlayer(player); playerToDelete = null }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { playerToDelete = null }) { Text("Cancelar") } }) }

        showRenameGroupDialog?.let { group -> RenameGroupDialog(group, { showRenameGroupDialog = null }, { newName -> viewModel.renameGroup(group, newName); selectedGroup = newName; showRenameGroupDialog = null }) }
        showDeleteGroupDialog?.let { group -> AlertDialog(onDismissRequest = { showDeleteGroupDialog = null }, title = { Text("Excluir grupo '$group'?") }, text = { Text("Tem certeza? Todos os dados desse grupo serão apagados permanentemente.") }, confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = { viewModel.deleteGroup(group); selectedGroup = "Geral"; showDeleteGroupDialog = null }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { showDeleteGroupDialog = null }) { Text("Cancelar") } }) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Column { Text("Vôlei Manager 🏐"); selectedGroup?.let { Text(it, style = MaterialTheme.typography.labelSmall) } } },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) } },
                    actions = {
                        if (currentScreen == Screen.GAME) IconButton(onClick = { showAddPlayerDialog = true }) { Icon(Icons.Default.Add, "Novo Jogador") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500)) },
                    label = "ScreenAnim"
                ) { screen ->
                    when (screen) {
                        Screen.GAME -> GameScreenContent(viewModel, selectedGroup ?: "Geral", isDarkTheme, showElo, Color.Red) { playerToDelete = it }
                        Screen.HISTORY -> HistoryScreen(viewModel, isDarkTheme, showElo)
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreenContent(viewModel: VoleiViewModel, selectedGroup: String, isDarkTheme: Boolean, showElo: Boolean, errorColor: Color, onDeleteRequest: (Player) -> Unit) {
    val sortedPlayers by viewModel.sortedPlayersForPresence.collectAsState()
    val gamesPlayedMap by viewModel.gamesPlayedTodayMap.collectAsState()
    val teamA by viewModel.teamA.collectAsState(); val teamB by viewModel.teamB.collectAsState()
    val waitingList by viewModel.waitingList.collectAsState(); val presentIds by viewModel.presentPlayerIds.collectAsState()
    val hasPrev by viewModel.hasPreviousMatch.collectAsState(); val config by viewModel.currentGroupConfig.collectAsState()
    val streak by viewModel.currentStreak.collectAsState(); val owner by viewModel.streakOwner.collectAsState()
    val winners by viewModel.lastWinners.collectAsState()

    var isSetupMode by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    var subOut by remember { mutableStateOf<Player?>(null) }
    var editP by remember { mutableStateOf<Player?>(null) }

    if (showCancel) AlertDialog(onDismissRequest = { showCancel = false }, title = { Text("Cancelar?") }, text = { Text("O progresso atual será perdido.") }, confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = errorColor), onClick = { viewModel.cancelGame(); showCancel = false }) { Text("Sim") } }, dismissButton = { TextButton(onClick = { showCancel = false }) { Text("Não") } })
    subOut?.let { p -> SubstitutionDialog(p, waitingList, teamA, teamB, { subOut = null }, { viewModel.substitutePlayer(p, it); subOut = null }) }
    editP?.let { p -> EditPlayerDialog(p, { editP = null }, { name, prio -> viewModel.editPlayer(p, name, prio); editP = null }) }

    val presentPlayers = remember(sortedPlayers, presentIds) { sortedPlayers.filter { presentIds.contains(it.id) } }

    if (isSetupMode) { ManualSetupScreen(presentPlayers, showElo, { tA, tB, b -> viewModel.startManualGame(tA, tB, b); isSetupMode = false }, { isSetupMode = false }) } else {
        AnimatedContent(targetState = teamA.isNotEmpty() || teamB.isNotEmpty(), label = "GameActiveAnim") { active ->
            if (active) { ActiveGameView(viewModel, teamA, teamB, waitingList, owner, streak, isDarkTheme, showElo, errorColor, { showCancel = true }, { subOut = it }) } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { EmptyStateCard(presentIds.size, selectedGroup, config.teamSize, { isSetupMode = true }, { if(presentIds.size >= config.teamSize*2) viewModel.startNewAutomaticGame(sortedPlayers, config.teamSize) }, hasPrev, { viewModel.startNextRound() }, winners, owner, streak, config.victoryLimit, errorColor, isDarkTheme) }
                    item { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text("Lista de presença", fontWeight = FontWeight.Bold); val all = sortedPlayers.all { presentIds.contains(it.id) }; TextButton(onClick = { viewModel.setAllPlayersPresence(sortedPlayers, !all) }) { Text(if(all) "Desmarcar todos" else "Marcar todos") } } }
                    items(sortedPlayers) { p -> PlayerCard(p, presentIds.contains(p.id), gamesPlayedMap[p.id], showElo, { viewModel.togglePlayerPresence(p) }, { onDeleteRequest(p) }, { editP = p }) }
                }
            }
        }
    }
}

@Composable
fun ActiveGameView(viewModel: VoleiViewModel, teamA: List<Player>, teamB: List<Player>, waitingList: List<Player>, streakOwner: String?, currentStreak: Int, isDarkTheme: Boolean, showElo: Boolean, errorColor: Color, onCancelRequest: () -> Unit, onSubRequest: (Player) -> Unit) {
    val teamAStreak = if(streakOwner == "A") currentStreak else 0
    val teamBStreak = if(streakOwner == "B") currentStreak else 0
    val cardColorA = if (isDarkTheme) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
    val btnColorA  = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1976D2)
    val cardColorB = if (isDarkTheme) Color(0xFFB71C1C) else Color(0xFFFFEBEE)
    val btnColorB  = if (isDarkTheme) Color(0xFFEF9A9A) else Color(0xFFD32F2F)
    val btnTextColor = if (isDarkTheme) Color.Black else Color.White
    val defaultStreakColor = Color(0xFFFF6F00)
    val yellowStreakColor = Color(0xFFFFD600)
    val streakColorA = defaultStreakColor
    val streakColorB = if (isDarkTheme) yellowStreakColor else defaultStreakColor

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.weight(0.75f).fillMaxHeight()) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { ActiveTeamCard("Time A", teamA, cardColorA, btnColorA, btnTextColor, streakColorA, teamAStreak, showElo, onSubRequest) { viewModel.finishGame("A") } }
                        Box(modifier = Modifier.width(50.dp).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { ActiveTeamCard("Time B", teamB, cardColorB, btnColorB, btnTextColor, streakColorB, teamBStreak, showElo, onSubRequest) { viewModel.finishGame("B") } }
                    }
                    TextButton(onClick = onCancelRequest, modifier = Modifier.align(Alignment.CenterHorizontally).height(24.dp), contentPadding = PaddingValues(0.dp)) { Text("Cancelar partida", color = errorColor, fontSize = 12.sp) }
                }
                Spacer(Modifier.width(16.dp)); Divider(Modifier.fillMaxHeight().width(1.dp).alpha(0.2f)); Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(0.25f).fillMaxHeight()) {
                    Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { itemsIndexed(waitingList) { i, p -> WaitingPlayerCard(i + 1, p, showElo) } }
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { ActiveTeamCard("Time A", teamA, cardColorA, btnColorA, btnTextColor, streakColorA, teamAStreak, showElo, onSubRequest) { viewModel.finishGame("A") } }
                Box(modifier = Modifier.height(40.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) { ActiveTeamCard("Time B", teamB, cardColorB, btnColorB, btnTextColor, streakColorB, teamBStreak, showElo, onSubRequest) { viewModel.finishGame("B") } }
            }
            TextButton(onClick = onCancelRequest, modifier = Modifier.fillMaxWidth().height(36.dp)) { Text("Cancelar partida", color = errorColor) }
            Spacer(Modifier.height(4.dp))
            Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall)
            LazyRow(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { itemsIndexed(waitingList) { i, p -> WaitingPlayerCard(i + 1, p, showElo) } }
        }
    }
}

@Composable
fun ActiveTeamCard(name: String, players: List<Player>, cardColor: Color, buttonColor: Color, buttonTextColor: Color, streakColor: Color, streak: Int, showElo: Boolean, onPlayerClick: (Player) -> Unit, onWin: () -> Unit) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if(cardColor.luminance() < 0.5f) Color.White else Color.Black
    val dividerColor = contentColor.copy(alpha = 0.2f)
    Card(modifier = Modifier.fillMaxSize().padding(4.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                if (showElo) {
                    Spacer(Modifier.width(4.dp))
                    Text("(Méd: ${avgElo.roundToInt()})", style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
                }
                if (streak > 0) { Spacer(Modifier.width(4.dp)); Text("🔥 $streak", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = streakColor) }
            }
            Divider(Modifier.padding(vertical = 4.dp), color = dividerColor)
            Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                players.forEach { p -> 
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable { onPlayerClick(p) }, contentAlignment = Alignment.Center) { 
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = if(showElo) "${p.name} (${p.elo.roundToInt()})" else p.name, 
                                style = MaterialTheme.typography.bodyMedium, 
                                maxLines = 1, overflow = TextOverflow.Ellipsis, color = contentColor
                            ) 
                            if (p.isPriority) {
                                Spacer(Modifier.width(2.dp))
                                Icon(Icons.Default.Star, contentDescription = "Prioridade", modifier = Modifier.size(12.dp), tint = contentColor.copy(alpha = 0.7f))
                            }
                        }
                    } 
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = onWin, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = buttonColor), contentPadding = PaddingValues(0.dp)) { Text("VITÓRIA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = buttonTextColor) }
        }
    }
}

@Composable
fun EmptyStateCard(selectedCount: Int, currentGroup: String, currentTeamSize: Int, onStartManualClick: () -> Unit, onStartAutoClick: () -> Unit, hasPreviousMatch: Boolean = false, onNextRoundClick: () -> Unit = {}, lastWinners: List<Player> = emptyList(), streakOwner: String? = null, currentStreak: Int = 0, victoryLimit: Int = 3, errorColor: Color = Color.Red, isDarkTheme: Boolean = false) {
    val minNeeded = currentTeamSize * 2
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            if (hasPreviousMatch) {
                val limitReached = currentStreak >= victoryLimit
                if (limitReached) {
                    val kingTextColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFFE65100); Text(text = "👑 Rei da quadra atingiu o limite!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = kingTextColor, textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(4.dp)); Text(text = "O time vencedor venceu $currentStreak seguidas e será redistribuído na próxima rodada.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                } else {
                    val teamName = if (streakOwner == "A") "Time A" else if (streakOwner == "B") "Time B" else "Vencedor"
                    val playerNames = lastWinners.joinToString(", ") { it.name }
                    Text(text = "Vitória do $teamName", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "($playerNames)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Aguardando próxima rodada.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text("Grupo: $currentGroup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = if(selectedCount == 0) "Nenhum jogador selecionado" else "$selectedCount jogadores presentes", color = if(selectedCount < minNeeded) errorColor else Color.Unspecified)
                if (selectedCount < minNeeded) { Text("Mínimo: $minNeeded", style = MaterialTheme.typography.bodySmall, color = errorColor) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (hasPreviousMatch) {
                Button(onClick = onNextRoundClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("🔄 Próxima rodada (Automático)", fontSize = 16.sp, color = Color.White) }
            } else {
                Button(onClick = onStartAutoClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if(selectedCount >= minNeeded) Color(0xFF2E7D32) else Color.Gray)) { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Iniciar jogo ($selectedCount selecionados)", fontSize = 16.sp, color = Color.White) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onStartManualClick) { Text(text = "Ou montar times manualmente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textDecoration = TextDecoration.Underline) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(player: Player, isPresent: Boolean, gamesPlayed: Int?, showElo: Boolean, onTogglePresence: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().combinedClickable(onClick = onTogglePresence, onLongClick = { showMenu = true }),
            colors = CardDefaults.cardColors(containerColor = if(isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            border = if(isPresent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() })
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = player.name, fontWeight = FontWeight.Bold)
                        if (player.isPriority) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Star, contentDescription = "Prioridade", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (showElo) {
                        Text(text = "Elo: ${"%.0f".format(player.elo)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val info = if (gamesPlayed != null) { if (gamesPlayed == 1) "1 jogo" else "$gamesPlayed jogos" } else { "Sem jogos recentes" }
                    Text(text = info, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) {
            DropdownMenuItem(text = { Text("Editar") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
            DropdownMenuItem(text = { Text("Excluir", color = Color.Red) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
        }
    }
}

@Composable
fun WaitingPlayerCard(index: Int, player: Player, showElo: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.widthIn(min = 120.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${index}º", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (player.isPriority) {
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Default.Star, contentDescription = "Prioridade", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (showElo) {
                    Text("Elo: ${"%.0f".format(player.elo)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selected, onClick = onClick); Spacer(Modifier.width(8.dp)); Text(text) } }
@Composable fun RenameGroupDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var newName by remember { mutableStateOf(oldName) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Renomear grupo") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Novo nome") }, singleLine = true) }, confirmButton = { Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }

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
        val isTeamA = teamA.any { it.id == playerOut.id }; val isTeamB = teamB.any { it.id == playerOut.id }
        waitingList.forEach { list.add(it to "(na espera)") }
        if (isTeamA) teamB.forEach { list.add(it to "(Time B)") }
        else if (isTeamB) teamA.forEach { list.add(it to "(Time A)") }
        else { teamA.forEach { list.add(it to "(Time A)") }; teamB.forEach { list.add(it to "(Time B)") } }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Substituir/Trocar ${playerOut.name}") },
        text = {
            if (allOptions.isEmpty()) {
                Text("Não há jogadores disponíveis para troca.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(allOptions) { (playerIn, label) ->
                        ListItem(
                            headlineContent = { Text(playerIn.name) },
                            supportingContent = { Text(label, style = MaterialTheme.typography.bodySmall) },
                            leadingContent = { Icon(Icons.Default.Person, null) },
                            modifier = Modifier.clickable { onConfirm(playerIn) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable fun EditPlayerDialog(player: Player, onDismiss: () -> Unit, onConfirm: (String, Boolean) -> Unit) { 
    var newName by remember { mutableStateOf(player.name) }
    var isPriority by remember { mutableStateOf(player.isPriority) }
    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("Editar jogador") }, 
        text = { 
            Column {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nome") }, singleLine = true) 
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text("Prioridade")
                }
            }
        }, 
        confirmButton = { Button(onClick = { if (newName.isNotBlank()) onConfirm(newName, isPriority) }) { Text("Salvar") } }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) 
}

@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var eloText by remember { mutableStateOf("1200") }
    var isPriority by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo jogador") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = eloText, onValueChange = { eloText = it }, label = { Text("Elo inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isPriority = !isPriority }) {
                    Checkbox(checked = isPriority, onCheckedChange = { isPriority = it })
                    Text("Definir como Prioridade")
                }
            }
        },
        confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, eloText.toDoubleOrNull() ?: 1200.0, isPriority) }) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
    var teamSize by remember { mutableStateOf(initialTeamSize.toFloat()) }
    var victoryLimit by remember { mutableStateOf(initialVictoryLimit.toFloat()) }
    var priorityEnabled by remember { mutableStateOf(initialPriorityEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Regras: $groupName") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Jogadores por time: ${teamSize.roundToInt()}")
                Slider(value = teamSize, onValueChange = { teamSize = it }, valueRange = 2f..6f, steps = 3)
                Spacer(Modifier.height(16.dp))
                
                Text("Limite de vitórias: ${victoryLimit.roundToInt()}")
                Slider(value = victoryLimit, onValueChange = { victoryLimit = it }, valueRange = 1f..6f, steps = 4)
                Spacer(Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = priorityEnabled, onCheckedChange = { priorityEnabled = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Mín. 1 prioridade por time", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(teamSize.roundToInt(), victoryLimit.roundToInt(), priorityEnabled) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var text by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Criar novo grupo") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nome do grupo") }, singleLine = true) }, confirmButton = { Button(onClick = { if(text.isNotBlank()) onConfirm(text) }, enabled = text.isNotBlank()) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }

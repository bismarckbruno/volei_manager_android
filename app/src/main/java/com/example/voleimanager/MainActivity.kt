package com.example.voleimanager

import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.Role
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
import com.example.voleimanager.ui.ManualSetupScreen
import com.example.voleimanager.ui.viewmodel.ThemeMode
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

            MaterialTheme(
                colorScheme = if(darkTheme) DarkGrayScheme else LightGrayScheme
            ) {
                VoleiManagerApp(viewModel, darkTheme)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val hasPreviousMatch by viewModel.hasPreviousMatch.collectAsState()
    val waitingList by viewModel.waitingList.collectAsState()
    val currentConfig by viewModel.currentGroupConfig.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val streakOwner by viewModel.streakOwner.collectAsState()
    val presentPlayerIds by viewModel.presentPlayerIds.collectAsState()
    val lastWinners by viewModel.lastWinners.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()

    var isSetupMode by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showCancelGameConfirmation by remember { mutableStateOf(false) }

    var playerToRename by remember { mutableStateOf<Player?>(null) }
    var playerToSubstitute by remember { mutableStateOf<Player?>(null) }

    var showSwitchGroupWarning by remember { mutableStateOf(false) }
    var pendingGroupSwitch by remember { mutableStateOf<String?>(null) }

    var groupToRename by remember { mutableStateOf<String?>(null) }
    var groupToDelete by remember { mutableStateOf<String?>(null) }

    val uniqueGroups = remember(allPlayers) { allPlayers.map { it.groupName }.distinct().sorted() }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uniqueGroups) { if (selectedGroup == null && uniqueGroups.isNotEmpty()) selectedGroup = uniqueGroups.first() }
    LaunchedEffect(selectedGroup) { selectedGroup?.let { viewModel.loadGroupConfig(it) } }

    val filteredPlayers = remember(selectedGroup, allPlayers) { if (selectedGroup == null) allPlayers else allPlayers.filter { it.groupName == selectedGroup } }

    val launcherImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { viewModel.importCsv(it, VoleiViewModel.ImportType.JOGADORES); Toast.makeText(context, "Importando Jogadores... (Os dados atuais foram mantidos)", Toast.LENGTH_LONG).show() } }
    val launcherHistory = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { viewModel.importCsv(it, VoleiViewModel.ImportType.HISTORICO); Toast.makeText(context, "Importando Histórico... (Os dados atuais foram mantidos)", Toast.LENGTH_LONG).show() } }

    val errorTextColor = if (isDarkTheme) Color(0xFFEF9A9A) else Color(0xFFD32F2F)

    // DIÁLOGO DE CONFIRMAÇÃO DE CANCELAMENTO
    if (showCancelGameConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelGameConfirmation = false },
            title = { Text("Cancelar partida em andamento?") }, // Texto atualizado
            text = { Text("Se você cancelar agora, o progresso desta partida será perdido e ela não será salva no histórico.\n\nDeseja realmente cancelar?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = errorTextColor),
                    onClick = {
                        viewModel.cancelGame()
                        showCancelGameConfirmation = false
                    }
                ) { Text("Sim, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelGameConfirmation = false }) { Text("Voltar ao jogo") }
            }
        )
    }

    if (showSwitchGroupWarning) {
        AlertDialog(
            onDismissRequest = { showSwitchGroupWarning = false; pendingGroupSwitch = null },
            title = { Text("Trocar de grupo?") },
            text = { Text("Existe um jogo em andamento. Se você mudar de grupo agora, a partida atual será cancelada.\n\nDeseja continuar?") },
            confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = errorTextColor), onClick = { pendingGroupSwitch?.let { group -> selectedGroup = group }; showSwitchGroupWarning = false; pendingGroupSwitch = null; scope.launch { drawerState.close() } }) { Text("Trocar") } },
            dismissButton = { TextButton(onClick = { showSwitchGroupWarning = false; pendingGroupSwitch = null }) { Text("Cancelar") } }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Aparência") },
            text = {
                Column {
                    ThemeOption(text = "Padrão do sistema", selected = currentThemeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM); showThemeDialog = false }
                    ThemeOption(text = "Claro", selected = currentThemeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT); showThemeDialog = false }
                    ThemeOption(text = "Escuro", selected = currentThemeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK); showThemeDialog = false }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Fechar") } }
        )
    }

    groupToRename?.let { oldName ->
        RenameGroupDialog(oldName = oldName, onDismiss = { groupToRename = null }) { newName ->
            viewModel.renameGroup(oldName, newName)
            if(selectedGroup == oldName) selectedGroup = newName
            groupToRename = null
        }
    }

    groupToDelete?.let { groupName ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Excluir grupo '$groupName'?") },
            text = { Text("ATENÇÃO: Isso apagará TODOS os jogadores e histórico deste grupo. Esta ação não pode ser desfeita.") },
            confirmButton = { Button(colors = ButtonDefaults.buttonColors(containerColor = errorTextColor), onClick = { viewModel.deleteGroup(groupName); if(selectedGroup == groupName) selectedGroup = "Geral"; groupToDelete = null }) { Text("Excluir definitivamente") } },
            dismissButton = { TextButton(onClick = { groupToDelete = null }) { Text("Cancelar") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(16.dp))
                    Text("Vôlei Manager", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider()
                    Text("Grupos", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    uniqueGroups.forEach { group ->
                        var showGroupMenu by remember { mutableStateOf(false) }
                        NavigationDrawerItem(
                            icon = { if(group == selectedGroup) Icon(Icons.Default.Check, null) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(group, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { showGroupMenu = true }) { Icon(Icons.Default.Edit, contentDescription = "Opções", modifier = Modifier.size(16.dp)) }
                                    DropdownMenu(expanded = showGroupMenu, onDismissRequest = { showGroupMenu = false }) {
                                        DropdownMenuItem(text = { Text("Renomear") }, onClick = { showGroupMenu = false; groupToRename = group })
                                        if (group != "Geral") {
                                            DropdownMenuItem(text = { Text("Excluir", color = errorTextColor) }, onClick = { showGroupMenu = false; groupToDelete = group })
                                        }
                                    }
                                }
                            },
                            selected = group == selectedGroup,
                            onClick = {
                                val gameInProgress = teamA.isNotEmpty() || teamB.isNotEmpty()
                                if (gameInProgress && group != selectedGroup) { pendingGroupSwitch = group; showSwitchGroupWarning = true } else { selectedGroup = group; scope.launch { drawerState.close() } }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Add, null) }, label = { Text("Criar novo grupo") }, selected = false, onClick = { showCreateGroupDialog = true; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Divider(Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(icon = { Icon(if(isDarkTheme) Icons.Default.Done else Icons.Default.Info, null) }, label = { Text("Aparência / Tema") }, selected = false, onClick = { showThemeDialog = true; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Divider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Regras do jogo") }, selected = false, onClick = { showConfigDialog = true; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Divider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Importar jogadores") }, selected = false, onClick = { launcherImport.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")); scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Importar histórico") }, selected = false, onClick = { launcherHistory.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")); scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    ) {
        if (showConfigDialog) GroupConfigDialog(selectedGroup ?: "Geral", currentConfig.teamSize, currentConfig.victoryLimit, { showConfigDialog = false }, { size, limit -> viewModel.updateConfig(size, limit); showConfigDialog = false })
        if (showCreateGroupDialog) CreateGroupDialog({ showCreateGroupDialog = false }, { newName -> selectedGroup = newName; viewModel.loadGroupConfig(newName); showCreateGroupDialog = false; Toast.makeText(context, "Grupo '$newName' criado!", Toast.LENGTH_SHORT).show() })
        if (showAddPlayerDialog) AddPlayerDialog({ showAddPlayerDialog = false }, { name, elo -> viewModel.addPlayer(name, elo, selectedGroup ?: "Geral"); showAddPlayerDialog = false; Toast.makeText(context, "$name adicionado!", Toast.LENGTH_SHORT).show() })
        playerToRename?.let { player -> RenamePlayerDialog(player, { playerToRename = null }, { newName -> viewModel.renamePlayer(player, newName); playerToRename = null; Toast.makeText(context, "Renomeado!", Toast.LENGTH_SHORT).show() }) }

        playerToSubstitute?.let { playerOut ->
            SubstitutionDialog(
                playerOut = playerOut,
                waitingList = waitingList,
                onDismiss = { playerToSubstitute = null },
                onConfirm = { playerIn ->
                    viewModel.substitutePlayer(playerOut, playerIn)
                    playerToSubstitute = null
                    Toast.makeText(context, "Substituição realizada!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (isSetupMode) {
            ManualSetupScreen(filteredPlayers, { tA, tB, bench -> viewModel.startManualGame(tA, tB, bench); isSetupMode = false }, { isSetupMode = false })
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Column { Text("Vôlei Manager 🏐"); selectedGroup?.let { Text(it, style = MaterialTheme.typography.labelSmall) } } },
                        navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menu") } },
                        actions = { IconButton(onClick = { showAddPlayerDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Novo jogador") } },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            ) { innerPadding ->
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                // NOVO: Estado para controlar a transição
                val isGameActive = teamA.isNotEmpty() || teamB.isNotEmpty()

                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                    // TRANSITION ANIMATION: Envolvemos a lógica de UI do jogo vs Lista
                    AnimatedContent(
                        targetState = isGameActive,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                        },
                        label = "GameTransition"
                    ) { active ->
                        if (active) {
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

                            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                                if (isLandscape) {
                                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(0.75f).fillMaxHeight()) {
                                            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                    ActiveTeamCard("Time A", teamA, cardColorA, btnColorA, btnTextColor, streakColorA, teamAStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("A") }
                                                }
                                                Box(modifier = Modifier.width(50.dp).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                    ActiveTeamCard("Time B", teamB, cardColorB, btnColorB, btnTextColor, streakColorB, teamBStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("B") }
                                                }
                                            }
                                            // AJUSTE PAISAGEM: Botão "colado" no fundo
                                            TextButton(
                                                onClick = { showCancelGameConfirmation = true },
                                                modifier = Modifier.align(Alignment.CenterHorizontally).height(24.dp), // Altura mínima possível
                                                contentPadding = PaddingValues(0.dp) // Sem padding interno
                                            ) {
                                                Text("Cancelar partida", color = errorTextColor, fontSize = 12.sp) // Texto atualizado
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp)); Divider(Modifier.fillMaxHeight().width(1.dp).alpha(0.2f)); Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(0.25f).fillMaxHeight()) {
                                            Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) // Texto atualizado
                                            Spacer(Modifier.height(8.dp))
                                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { itemsIndexed(waitingList) { i, p -> WaitingPlayerCard(i + 1, p) } }
                                        }
                                    }
                                } else {
                                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            ActiveTeamCard("Time A", teamA, cardColorA, btnColorA, btnTextColor, streakColorA, teamAStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("A") }
                                        }
                                        Box(modifier = Modifier.height(40.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            ActiveTeamCard("Time B", teamB, cardColorB, btnColorB, btnTextColor, streakColorB, teamBStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("B") }
                                        }
                                    }
                                    TextButton(
                                        onClick = { showCancelGameConfirmation = true },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Cancelar partida", color = errorTextColor) // Texto atualizado
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall) // Texto atualizado
                                    LazyRow(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { itemsIndexed(waitingList) { i, p -> WaitingPlayerCard(i + 1, p) } }
                                }
                            }

                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                item {
                                    EmptyStateCard(
                                        selectedCount = presentPlayerIds.size,
                                        currentGroup = selectedGroup ?: "Geral",
                                        currentTeamSize = currentConfig.teamSize,
                                        onStartManualClick = { isSetupMode = true },
                                        onStartAutoClick = {
                                            val minNeeded = currentConfig.teamSize * 2
                                            if(presentPlayerIds.size < minNeeded) {
                                                Toast.makeText(context, "Precisa de $minNeeded jogadores (Atual: ${presentPlayerIds.size})", Toast.LENGTH_LONG).show()
                                            } else {
                                                viewModel.startNewAutomaticGame(filteredPlayers, currentConfig.teamSize)
                                                Toast.makeText(context, "Times sorteados com ${presentPlayerIds.size} jogadores!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        hasPreviousMatch = hasPreviousMatch,
                                        onNextRoundClick = { viewModel.startNextRound() },
                                        lastWinners = lastWinners,
                                        streakOwner = streakOwner,
                                        currentStreak = currentStreak,
                                        victoryLimit = currentConfig.victoryLimit,
                                        errorColor = errorTextColor,
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                                item {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Text("Lista de presença", fontWeight = FontWeight.Bold) // Texto atualizado
                                        val allSelected = filteredPlayers.all { presentPlayerIds.contains(it.id) }
                                        TextButton(onClick = { viewModel.setAllPlayersPresence(filteredPlayers, !allSelected) }) { Text(if(allSelected) "Desmarcar todos" else "Marcar todos") } // Texto atualizado
                                    }
                                }
                                items(filteredPlayers) { player -> PlayerCard(player, presentPlayerIds.contains(player.id), { viewModel.togglePlayerPresence(player) }, { viewModel.deletePlayer(player) }, { playerToRename = player }) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val LightGrayScheme = lightColorScheme(
    primary = Color(0xFF212121),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF212121),
    secondary = Color(0xFF616161),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color.Black,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242)
)

private val DarkGrayScheme = darkColorScheme(
    primary = Color(0xFFEEEEEE),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF424242),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF424242),
    onSecondaryContainer = Color.White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selected, onClick = onClick); Spacer(Modifier.width(8.dp)); Text(text) } }
@Composable fun RenameGroupDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var newName by remember { mutableStateOf(oldName) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Renomear grupo") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Novo nome") }, singleLine = true) }, confirmButton = { Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun WaitingPlayerCard(index: Int, player: Player) { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("${index}º", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp); Spacer(Modifier.width(8.dp)); Text(player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) } } }

@Composable
fun ActiveTeamCard(name: String, players: List<Player>, cardColor: Color, buttonColor: Color, buttonTextColor: Color, streakColor: Color, streak: Int, onPlayerClick: (Player) -> Unit, onWin: () -> Unit) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if(cardColor.luminance() < 0.5f) Color.White else Color.Black
    val subContentColor = contentColor.copy(alpha = 0.8f)
    val dividerColor = contentColor.copy(alpha = 0.2f)

    Card(modifier = Modifier.fillMaxSize().padding(4.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Spacer(Modifier.width(4.dp))
                Text("(Méd: ${avgElo.roundToInt()})", style = MaterialTheme.typography.bodySmall, color = subContentColor)
                if (streak > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text("🔥 $streak", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = streakColor)
                }
            }
            Divider(Modifier.padding(vertical = 4.dp), color = dividerColor)
            Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                players.forEach { p -> Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable { onPlayerClick(p) }, contentAlignment = Alignment.Center) { Text(text = "${p.name} (${p.elo.roundToInt()})", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = contentColor) } }
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = onWin, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = buttonColor), contentPadding = PaddingValues(0.dp)) {
                Text("VITÓRIA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = buttonTextColor)
            }
        }
    }
}

@Composable fun SubstitutionDialog(playerOut: Player, waitingList: List<Player>, onDismiss: () -> Unit, onConfirm: (Player) -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Substituir ${playerOut.name}") }, text = { if (waitingList.isEmpty()) { Text("Não há jogadores na espera.") } else { LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { items(waitingList) { playerIn -> ListItem(headlineContent = { Text(playerIn.name) }, leadingContent = { Icon(Icons.Default.Person, null) }, modifier = Modifier.clickable { onConfirm(playerIn) }) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@OptIn(ExperimentalFoundationApi::class) @Composable fun PlayerCard(player: Player, isPresent: Boolean, onTogglePresence: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) { var showMenu by remember { mutableStateOf(false) }; Box(modifier = Modifier.fillMaxWidth()) { Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().combinedClickable(onClick = onTogglePresence, onLongClick = { showMenu = true }), colors = CardDefaults.cardColors(containerColor = if(isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), border = if(isPresent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() }); Spacer(Modifier.width(8.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = player.name, fontWeight = FontWeight.Bold); Text(text = "Elo: ${"%.0f".format(player.elo)}", style = MaterialTheme.typography.bodySmall) }; Column(horizontalAlignment = Alignment.End) { Text(text = "${player.victories} vitórias", style = MaterialTheme.typography.bodySmall) } } }; DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) { DropdownMenuItem(text = { Text("Renomear") }, onClick = { showMenu = false; onRename() }, leadingIcon = { Icon(Icons.Default.Edit, null) }); DropdownMenuItem(text = { Text("Excluir", color = Color.Red) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }) } } }
@Composable fun RenamePlayerDialog(player: Player, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var newName by remember { mutableStateOf(player.name) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Renomear jogador") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Novo nome") }, singleLine = true) }, confirmButton = { Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }

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
                Button(onClick = onNextRoundClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("🔄 Próxima rodada (Automático)", fontSize = 16.sp, color = Color.White) } // Texto atualizado
            } else {
                Button(onClick = onStartAutoClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if(selectedCount >= minNeeded) Color(0xFF2E7D32) else Color.Gray)) { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Iniciar jogo ($selectedCount selecionados)", fontSize = 16.sp, color = Color.White) } // Texto atualizado
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onStartManualClick) { Text(text = "Ou montar times manualmente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textDecoration = TextDecoration.Underline) }
        }
    }
}
@Composable fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) { var name by remember { mutableStateOf("") }; var eloText by remember { mutableStateOf("1200") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Novo jogador") }, text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = eloText, onValueChange = { eloText = it }, label = { Text("Elo inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) } }, confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, eloText.toDoubleOrNull() ?: 1200.0) }) { Text("Adicionar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun GroupConfigDialog(groupName: String, initialTeamSize: Int, initialVictoryLimit: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) { var teamSize by remember { mutableStateOf(initialTeamSize.toFloat()) }; var victoryLimit by remember { mutableStateOf(initialVictoryLimit.toFloat()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Regras: $groupName") }, text = { Column { Text("Jogadores por time: ${teamSize.roundToInt()}"); Slider(value = teamSize, onValueChange = { teamSize = it }, valueRange = 2f..6f, steps = 3); Spacer(Modifier.height(16.dp)); Text("Limite de vitórias: ${victoryLimit.roundToInt()}"); Slider(value = victoryLimit, onValueChange = { victoryLimit = it }, valueRange = 1f..6f, steps = 4) } }, confirmButton = { Button(onClick = { onConfirm(teamSize.roundToInt(), victoryLimit.roundToInt()) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var text by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Criar novo grupo") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nome do grupo") }, singleLine = true) }, confirmButton = { Button(onClick = { if(text.isNotBlank()) onConfirm(text) }, enabled = text.isNotBlank()) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
package com.example.voleimanager

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.voleimanager.data.AppDatabase
import com.example.voleimanager.data.VoleiRepository
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.ui.ManualSetupScreen
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

        setContent { MaterialTheme { VoleiManagerApp(viewModel) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel) {
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

    var isSetupMode by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var playerToRename by remember { mutableStateOf<Player?>(null) }
    var playerToSubstitute by remember { mutableStateOf<Player?>(null) }

    var showSwitchGroupWarning by remember { mutableStateOf(false) }
    var pendingGroupSwitch by remember { mutableStateOf<String?>(null) }

    val uniqueGroups = remember(allPlayers) { allPlayers.map { it.groupName }.distinct().sorted() }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uniqueGroups) { if (selectedGroup == null && uniqueGroups.isNotEmpty()) selectedGroup = uniqueGroups.first() }
    LaunchedEffect(selectedGroup) { selectedGroup?.let { viewModel.loadGroupConfig(it) } }

    val filteredPlayers = remember(selectedGroup, allPlayers) { if (selectedGroup == null) allPlayers else allPlayers.filter { it.groupName == selectedGroup } }

    val launcherImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { viewModel.importCsv(it, VoleiViewModel.ImportType.JOGADORES); Toast.makeText(context, "Importando...", Toast.LENGTH_SHORT).show() } }
    val launcherHistory = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { viewModel.importCsv(it, VoleiViewModel.ImportType.HISTORICO); Toast.makeText(context, "Importando...", Toast.LENGTH_SHORT).show() } }

    if (showSwitchGroupWarning) {
        AlertDialog(
            onDismissRequest = { showSwitchGroupWarning = false; pendingGroupSwitch = null },
            title = { Text("Trocar de Grupo?") },
            text = { Text("Existe um jogo em andamento. Se você mudar de grupo agora, a partida atual será cancelada e os dados não salvos da rodada serão perdidos.\n\nDeseja continuar?") },
            confirmButton = {
                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.Red), onClick = { pendingGroupSwitch?.let { group -> selectedGroup = group }; showSwitchGroupWarning = false; pendingGroupSwitch = null; scope.launch { drawerState.close() } }) { Text("Trocar Mesmo Assim") }
            },
            dismissButton = { TextButton(onClick = { showSwitchGroupWarning = false; pendingGroupSwitch = null }) { Text("Cancelar") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(16.dp))
                    Text("Vôlei Manager", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Divider()
                    Text("Grupos", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    uniqueGroups.forEach { group ->
                        NavigationDrawerItem(
                            icon = { if(group == selectedGroup) Icon(Icons.Default.Check, null) },
                            label = { Text(group) },
                            selected = group == selectedGroup,
                            onClick = {
                                val gameInProgress = teamA.isNotEmpty() || teamB.isNotEmpty()
                                if (gameInProgress && group != selectedGroup) { pendingGroupSwitch = group; showSwitchGroupWarning = true } else { selectedGroup = group; scope.launch { drawerState.close() } }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Add, null) }, label = { Text("Criar Novo Grupo") }, selected = false, onClick = { showCreateGroupDialog = true; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Divider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Regras do Jogo") }, selected = false, onClick = { showConfigDialog = true; scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    Divider(Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Importar Jogadores") }, selected = false, onClick = { launcherImport.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")); scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                    NavigationDrawerItem(icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Importar Histórico") }, selected = false, onClick = { launcherHistory.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")); scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
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
                        actions = { IconButton(onClick = { showAddPlayerDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Novo Jogador") } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }
            ) { innerPadding ->
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                    if (teamA.isNotEmpty() || teamB.isNotEmpty()) {
                        val teamAStreak = if(streakOwner == "A") currentStreak else 0
                        val teamBStreak = if(streakOwner == "B") currentStreak else 0

                        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                            if (isLandscape) {
                                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(0.7f).fillMaxHeight()) {
                                        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                ActiveTeamCard("Time A", teamA, Color(0xFFE3F2FD), teamAStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("A") }
                                            }
                                            Box(modifier = Modifier.width(50.dp).align(Alignment.CenterVertically), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                ActiveTeamCard("Time B", teamB, Color(0xFFFFEBEE), teamBStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("B") }
                                            }
                                        }
                                        TextButton(onClick = { viewModel.cancelGame() }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)) { Text("Cancelar Partida", color = Color.Red) }
                                    }
                                    Spacer(Modifier.width(16.dp)); Divider(Modifier.fillMaxHeight().width(1.dp).alpha(0.2f)); Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(0.3f).fillMaxHeight()) {
                                        Text("Na Espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { itemsIndexed(waitingList) { i, p -> WaitingPlayerCard(i + 1, p) } }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) { ActiveTeamCard("Time A", teamA, Color(0xFFE3F2FD), teamAStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("A") } }
                                    Box(modifier = Modifier.height(40.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) { ActiveTeamCard("Time B", teamB, Color(0xFFFFEBEE), teamBStreak, onPlayerClick = { playerToSubstitute = it }) { viewModel.finishGame("B") } }
                                }
                                TextButton(onClick = { viewModel.cancelGame() }, modifier = Modifier.fillMaxWidth()) { Text("Cancelar Partida", color = Color.Red) }
                                Spacer(Modifier.height(8.dp))
                                Text("Na Espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall)
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
                                    onNextRoundClick = { viewModel.startNextRound() }
                                )
                            }
                            item {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text("Lista de Presença", fontWeight = FontWeight.Bold)
                                    val allSelected = filteredPlayers.all { presentPlayerIds.contains(it.id) }
                                    TextButton(onClick = { viewModel.setAllPlayersPresence(filteredPlayers, !allSelected) }) { Text(if(allSelected) "Desmarcar Todos" else "Marcar Todos") }
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

// --- COMPONENTES ---

@Composable
fun WaitingPlayerCard(index: Int, player: Player) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${index}º", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(player.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun ActiveTeamCard(name: String, players: List<Player>, color: Color, streak: Int, onPlayerClick: (Player) -> Unit, onWin: () -> Unit) {
    // CÁLCULO DA MÉDIA DO TIME (Atualiza automaticamente)
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0

    Card(modifier = Modifier.fillMaxSize().padding(4.dp), colors = CardDefaults.cardColors(containerColor = color), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // CABEÇALHO DO TIME COM MÉDIA
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                // Mostra a média formatada
                Text("(Méd: ${avgElo.roundToInt()})", style = MaterialTheme.typography.bodySmall)

                if (streak > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text("🔥 $streak", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6F00))
                }
            }
            Divider(Modifier.padding(vertical = 4.dp), color = Color.Black.copy(alpha = 0.1f))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                players.forEach { p ->
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable { onPlayerClick(p) }, contentAlignment = Alignment.Center) {
                        // NOME DO JOGADOR COM ELO INDIVIDUAL
                        Text(
                            text = "${p.name} (${p.elo.roundToInt()})",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(onClick = onWin, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = if(name.contains("A")) Color(0xFF1976D2) else Color(0xFFD32F2F)), contentPadding = PaddingValues(0.dp)) { Text("VITÓRIA", fontWeight = FontWeight.Black, fontSize = 12.sp) }
        }
    }
}

@Composable fun SubstitutionDialog(playerOut: Player, waitingList: List<Player>, onDismiss: () -> Unit, onConfirm: (Player) -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text("Substituir ${playerOut.name}") }, text = { if (waitingList.isEmpty()) { Text("Não há jogadores na espera.") } else { LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) { items(waitingList) { playerIn -> ListItem(headlineContent = { Text(playerIn.name) }, leadingContent = { Icon(Icons.Default.Person, null) }, modifier = Modifier.clickable { onConfirm(playerIn) }) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@OptIn(ExperimentalFoundationApi::class) @Composable fun PlayerCard(player: Player, isPresent: Boolean, onTogglePresence: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) { var showMenu by remember { mutableStateOf(false) }; Box(modifier = Modifier.fillMaxWidth()) { Card(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().combinedClickable(onClick = onTogglePresence, onLongClick = { showMenu = true }), colors = CardDefaults.cardColors(containerColor = if(isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), border = if(isPresent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() }); Spacer(Modifier.width(8.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = player.name, fontWeight = FontWeight.Bold); Text(text = "Elo: ${"%.0f".format(player.elo)}", style = MaterialTheme.typography.bodySmall) }; Column(horizontalAlignment = Alignment.End) { Text(text = "${player.victories} vitórias", style = MaterialTheme.typography.bodySmall) } } }; DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, offset = DpOffset(x = 16.dp, y = 0.dp)) { DropdownMenuItem(text = { Text("Renomear") }, onClick = { showMenu = false; onRename() }, leadingIcon = { Icon(Icons.Default.Edit, null) }); DropdownMenuItem(text = { Text("Excluir", color = Color.Red) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }) } } }
@Composable fun RenamePlayerDialog(player: Player, onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var newName by remember { mutableStateOf(player.name) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Renomear Jogador") }, text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Novo Nome") }, singleLine = true) }, confirmButton = { Button(onClick = { if (newName.isNotBlank()) onConfirm(newName) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun EmptyStateCard(selectedCount: Int, currentGroup: String, currentTeamSize: Int, onStartManualClick: () -> Unit, onStartAutoClick: () -> Unit, hasPreviousMatch: Boolean = false, onNextRoundClick: () -> Unit = {}) {
    val minNeeded = currentTeamSize * 2
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Grupo: $currentGroup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (hasPreviousMatch) {
                Text("Jogo encerrado. Aguardando próxima rodada.")
            } else {
                Text(text = if(selectedCount == 0) "Nenhum jogador selecionado" else "$selectedCount jogadores presentes", color = if(selectedCount < minNeeded) Color.Red else Color.Unspecified)
                if (selectedCount < minNeeded) { Text("Mínimo: $minNeeded", style = MaterialTheme.typography.bodySmall, color = Color.Red) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (hasPreviousMatch) {
                Button(onClick = onNextRoundClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), modifier = Modifier.fillMaxWidth()) { Text("🔄 Próxima Rodada (Automático)") }
            } else {
                Button(onClick = onStartAutoClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if(selectedCount >= minNeeded) MaterialTheme.colorScheme.primary else Color.Gray)) { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Iniciar Jogo ($selectedCount selecionados)") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onStartManualClick, modifier = Modifier.fillMaxWidth()) { Text("Montar Times Manualmente") }
        }
    }
}
@Composable fun AddPlayerDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) { var name by remember { mutableStateOf("") }; var eloText by remember { mutableStateOf("1200") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Novo Jogador") }, text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = eloText, onValueChange = { eloText = it }, label = { Text("Elo Inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) } }, confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name, eloText.toDoubleOrNull() ?: 1200.0) }) { Text("Adicionar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun GroupConfigDialog(groupName: String, initialTeamSize: Int, initialVictoryLimit: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) { var teamSize by remember { mutableStateOf(initialTeamSize.toFloat()) }; var victoryLimit by remember { mutableStateOf(initialVictoryLimit.toFloat()) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Regras: $groupName") }, text = { Column { Text("Jogadores por Time: ${teamSize.roundToInt()}"); Slider(value = teamSize, onValueChange = { teamSize = it }, valueRange = 2f..6f, steps = 3); Spacer(Modifier.height(16.dp)); Text("Limite de Vitórias: ${victoryLimit.roundToInt()}"); Slider(value = victoryLimit, onValueChange = { victoryLimit = it }, valueRange = 1f..6f, steps = 4) } }, confirmButton = { Button(onClick = { onConfirm(teamSize.roundToInt(), victoryLimit.roundToInt()) }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
@Composable fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) { var text by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("Criar Novo Grupo") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Nome do Grupo") }, singleLine = true) }, confirmButton = { Button(onClick = { if(text.isNotBlank()) onConfirm(text) }, enabled = text.isNotBlank()) { Text("Criar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
package com.example.voleimanager

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.voleimanager.ui.theme.AppTheme
import com.example.voleimanager.ui.theme.LocalExtendedColors
import com.example.voleimanager.ui.viewmodel.Screen
import com.example.voleimanager.ui.viewmodel.ThemeMode
import com.example.voleimanager.ui.viewmodel.CsvType
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import com.example.voleimanager.ui.viewmodel.VoleiViewModelFactory
import com.example.voleimanager.util.EloCalculator
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
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppTheme(
                darkTheme = darkTheme
            ) {
                 VoleiManagerApp(
                    viewModel,
                    darkTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val showElo by viewModel.showElo.collectAsState()
    val showToll by viewModel.showToll.collectAsState()
    val uniqueGroups = remember(allPlayers) { allPlayers.map { it.groupName }.distinct().sorted() }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    // Estado de Setup Manual movido para cá para unificar o BackHandler
    var isSetupMode by rememberSaveable { mutableStateOf(false) }

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

    val launcherImport =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.importData(it, pendingImportType, context); Toast.makeText(
                context,
                "Importando...",
                Toast.LENGTH_SHORT
            ).show()
            }
        }

    LaunchedEffect(uniqueGroups) {
        if (selectedGroup == null && uniqueGroups.isNotEmpty()) selectedGroup = uniqueGroups.first()
    }
    LaunchedEffect(selectedGroup) { selectedGroup?.let { viewModel.loadGroupConfig(it) } }

    // Gerenciador Unificado do Botão Voltar do Android
    BackHandler(enabled = drawerState.isOpen || isSetupMode || currentScreen != Screen.GAME) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (isSetupMode) {
            isSetupMode = false
        } else if (currentScreen != Screen.GAME) {
            viewModel.navigateTo(Screen.GAME)
        }
    }

    if (pendingGroupSwitch != null) {
        AlertDialog(
            onDismissRequest = { pendingGroupSwitch = null },
            title = { Text("Mudar de grupo?") },
            text = { Text("Existe um jogo em andamento. Se mudar de grupo agora, o progresso da partida atual será perdido.") },
            confirmButton = {
                Button(onClick = {
                    selectedGroup = pendingGroupSwitch
                    viewModel.loadGroupConfig(pendingGroupSwitch!!)
                    pendingGroupSwitch = null
                }) { Text("Mudar mesmo assim") }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingGroupSwitch = null
                }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar dados") },
            text = {
                Column {
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = it },
                        label = { Text("Nome do arquivo") })
                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.exportData(
                                context,
                                CsvType.BACKUP_COMPLETO,
                                exportFileName
                            )
                            showExportDialog = false
                        }) {
                        Icon(
                            Icons.Default.Share,
                            null
                        ); Spacer(Modifier.width(8.dp)); Text("Backup Completo (.json)")
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Exportar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            viewModel.exportData(
                                context,
                                CsvType.JOGADORES,
                                exportFileName
                            )
                            showExportDialog = false
                        }) { Text("Jogadores") }
                        TextButton(onClick = {
                            viewModel.exportData(
                                context,
                                CsvType.HISTORICO,
                                exportFileName
                            )
                            showExportDialog = false
                        }) { Text("Histórico") }
                        TextButton(onClick = {
                            viewModel.exportData(
                                context,
                                CsvType.ELO_LOGS,
                                exportFileName
                            )
                            showExportDialog = false
                        }) { Text("Logs") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar dados") },
            text = {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pendingImportType = CsvType.BACKUP_COMPLETO; launcherImport.launch(
                            arrayOf("application/json", "text/plain")
                        )
                            showImportDialog = false
                        }) {
                        Icon(
                            Icons.Default.Add,
                            null
                        ); Spacer(Modifier.width(8.dp)); Text("Restaurar Backup (.json)")
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Importar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            pendingImportType = CsvType.JOGADORES; launcherImport.launch(
                            arrayOf(
                                "text/*",
                                "text/csv",
                                "application/csv"
                            )
                        )
                            showImportDialog = false
                        }) { Text("Jogadores") }
                        TextButton(onClick = {
                            pendingImportType = CsvType.HISTORICO; launcherImport.launch(
                            arrayOf(
                                "text/*",
                                "text/csv",
                                "application/csv"
                            )
                        )
                            showImportDialog = false
                        }) { Text("Histórico") }
                        TextButton(onClick = {
                            pendingImportType = CsvType.ELO_LOGS; launcherImport.launch(
                            arrayOf(
                                "text/*",
                                "text/csv",
                                "application/csv"
                            )
                        )
                            showImportDialog = false
                        }) { Text("Logs") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())) {
                    Text(
                        "Vôlei Manager",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))

                    Text("Grupo atual:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    var groupExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = groupExpanded,
                        onExpandedChange = { groupExpanded = !groupExpanded }) {
                        OutlinedTextField(
                            value = selectedGroup ?: "Selecione",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = groupExpanded,
                            onDismissRequest = { groupExpanded = false }) {
                            uniqueGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(group, modifier = Modifier.weight(1f))
                                            IconButton(onClick = {
                                                showRenameGroupDialog = group
                                                groupExpanded = false
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = {
                                                showDeleteGroupDialog = group
                                                groupExpanded = false
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        groupExpanded = false
                                        if (selectedGroup != group) {
                                            if (viewModel.isGameInProgress()) pendingGroupSwitch =
                                                group
                                            else {
                                                selectedGroup = group; viewModel.loadGroupConfig(
                                                    group
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(text = {
                                Text(
                                    "+ Criar novo grupo",
                                    fontWeight = FontWeight.Bold
                                )
                            }, onClick = { showCreateGroupDialog = true
                                groupExpanded = false })
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.PlayCircle, null) },
                        label = { Text("Jogo") },
                        selected = currentScreen == Screen.GAME,
                        onClick = { viewModel.navigateTo(Screen.GAME); scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.DateRange, null) },
                        label = { Text("Histórico") },
                        selected = currentScreen == Screen.HISTORY,
                        onClick = { viewModel.navigateTo(Screen.HISTORY); scope.launch { drawerState.close() } })

                    HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Text("Configurações", style = MaterialTheme.typography.labelMedium)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        label = { Text("Regras do grupo") },
                        selected = false,
                        onClick = { showConfigDialog = true
                            scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Palette, null) },
                        label = { Text("Tema") },
                        selected = false,
                        onClick = { showThemeDialog = true
                            scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, null) },
                        label = { Text("Mostrar Elo") },
                        selected = false,
                        badge = { Switch(checked = showElo, onCheckedChange = null) },
                        onClick = { viewModel.setShowElo(!showElo) }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.AlarmAdd, null) },
                        label = { Text("Mostrar atraso") },
                        selected = false,
                        badge = { Switch(checked = showToll, onCheckedChange = null) },
                        onClick = { viewModel.setShowToll(!showToll) }
                    )
                    HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Text("Dados", style = MaterialTheme.typography.labelMedium)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.FileUpload, null) },
                        label = { Text("Exportar") },
                        selected = false,
                        onClick = { showExportDialog = true
                            scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.FileDownload, null) },
                        label = { Text("Importar") },
                        selected = false,
                        onClick = { showImportDialog = true
                            scope.launch { drawerState.close() } })

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
        if (showCreateGroupDialog) CreateGroupDialog(
            { showCreateGroupDialog = false },
            { newName ->
                selectedGroup = newName; viewModel.loadGroupConfig(newName); showCreateGroupDialog =
                false
            })
        if (showAddPlayerDialog) AddPlayerDialog(
            { showAddPlayerDialog = false },
            { name, elo, isPriority ->
                viewModel.addPlayer(
                    name,
                    elo,
                    selectedGroup ?: "Geral",
                    isPriority
                )
                showAddPlayerDialog = false
            })
        if (showThemeDialog) {
            val mode by viewModel.themeMode.collectAsState(); AlertDialog(
                onDismissRequest = {
                    showThemeDialog = false
                },
                title = { Text("Tema") },
                text = {
                    Column {
                        ThemeOption(
                            "Sistema",
                            mode == ThemeMode.SYSTEM
                        ) { viewModel.setThemeMode(ThemeMode.SYSTEM) }; ThemeOption(
                        "Claro",
                        mode == ThemeMode.LIGHT
                    ) { viewModel.setThemeMode(ThemeMode.LIGHT) }; ThemeOption(
                        "Escuro",
                        mode == ThemeMode.DARK
                    ) { viewModel.setThemeMode(ThemeMode.DARK) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showThemeDialog = false
                    }) { Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                })
        }
        playerToDelete?.let { player ->
            AlertDialog(
                onDismissRequest = { playerToDelete = null },
                title = { Text("Excluir ${player.name}?") },
                text = { Text("A pessoa será removida da lista ativa, mas seu histórico de partidas SERÁ MANTIDO.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { viewModel.deletePlayer(player)
                            playerToDelete = null }) {
                        Text(
                            "Excluir"
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        playerToDelete = null
                    }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                })
        }

        showRenameGroupDialog?.let { group ->
            RenameGroupDialog(
                group,
                { showRenameGroupDialog = null },
                { newName ->
                    viewModel.renameGroup(group, newName)
                    selectedGroup = newName
                    showRenameGroupDialog = null
                })
        }
        showDeleteGroupDialog?.let { group ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteGroupDialog = null
                },
                title = { Text("Excluir grupo '$group'?") },
                text = { Text("Tem certeza? Todos os dados desse grupo serão apagados permanentemente.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteGroup(group); selectedGroup =
                            "Geral"
                            showDeleteGroupDialog = null
                        }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteGroupDialog = null
                    }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                })
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Vôlei Manager"); selectedGroup?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                null
                            )
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.GAME) IconButton(onClick = {
                            showAddPlayerDialog = true
                        }) { Icon(Icons.Default.Add, "Adicionar novo jogador") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier
                .padding(padding)
                .fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                            animationSpec = tween(500)
                        )
                    },
                    label = "ScreenAnim"
                ) { screen ->
                    when (screen) {
                        Screen.GAME -> GameScreenContent(
                            viewModel = viewModel,
                            selectedGroup = selectedGroup ?: "Geral",
                            isDarkTheme = isDarkTheme,
                            showElo = showElo,
                            showToll = showToll,
                            isSetupMode = isSetupMode,
                            onSetupModeChange = { isSetupMode = it },
                            onDeleteRequest = { playerToDelete = it },
                            onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                        )

                        Screen.HISTORY -> HistoryScreen(viewModel, isDarkTheme, showElo)
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreenContent(
    viewModel: VoleiViewModel,
    selectedGroup: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showToll: Boolean,
    isSetupMode: Boolean,
    onSetupModeChange: (Boolean) -> Unit,
    onDeleteRequest: (Player) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val sortedPlayers by viewModel.sortedPlayersForPresence.collectAsState()
    val gamesPlayedMap by viewModel.gamesPlayedTodayMap.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val teamA by viewModel.teamA.collectAsState()
    val teamB by viewModel.teamB.collectAsState()
    val waitingList by viewModel.waitingList.collectAsState()
    val presentIds by viewModel.presentPlayerIds.collectAsState()
    val hasPrev by viewModel.hasPreviousMatch.collectAsState()
    val config by viewModel.currentGroupConfig.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val owner by viewModel.streakOwner.collectAsState()
    val winners by viewModel.lastWinners.collectAsState()
    // Filtra automaticamente quem não está presente
    val absentPlayers = remember(sortedPlayers, presentIds) {
        sortedPlayers.filter { !presentIds.contains(it.id) }
    }
    
    var showAbsentDialog by remember { mutableStateOf(false) }
    var playerToAddFromAbsent by remember { mutableStateOf<Player?>(null) }
    var showCancel by remember { mutableStateOf(false) }
    var subOut by remember { mutableStateOf<Player?>(null) }
    var editP by remember { mutableStateOf<Player?>(null) }
    var confirmWinTeam by remember { mutableStateOf<String?>(null) }

    if (showAbsentDialog) {
        AlertDialog(
            onDismissRequest = { showAbsentDialog = false },
            title = { Text("Selecionar jogadores") },
            text = {
                if (absentPlayers.isEmpty()) {
                    Text("Todos os jogadores cadastrados já estão presentes na quadra.")
                } else {
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState, modifier = Modifier.heightIn(max = 300.dp).simpleScrollbar(listState)) {
                        items(absentPlayers) { p ->
                            ListItem(
                                headlineContent = { Text(p.name, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingContent = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.clickable {
                                    playerToAddFromAbsent = p
                                    showAbsentDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAbsentDialog = false },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    playerToAddFromAbsent?.let { p ->
        val playedToday = (gamesPlayedMap[p.id] ?: 0) > 0
        AlertDialog(
            onDismissRequest = { playerToAddFromAbsent = null },
            title = { Text("Selecionar ${p.name}?") },
            text = { 
                if (playedToday) {
                    Text("A pessoa selecionada irá para o final da fila de espera.")
                } else {
                    Text("A pessoa selecionada irá para o começo da fila de espera por ainda não ter jogado hoje.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Marcar como presente joga a pessoa pra fila no ViewModel
                    viewModel.togglePlayerPresence(p)
                    playerToAddFromAbsent = null
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToAddFromAbsent = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }
    
    if (showCancel) AlertDialog(
        onDismissRequest = { showCancel = false },
        title = { Text("Cancelar partida?") },
        text = { Text("O progresso atual será perdido.") },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { viewModel.cancelGame()
                    showCancel = false }) { Text("Sim") }
        },
        dismissButton = { TextButton(onClick = { showCancel = false }) { Text("Não", color = MaterialTheme.colorScheme.onSurfaceVariant) } })
    subOut?.let { p ->
        SubstitutionDialog(
            p,
            waitingList,
            teamA,
            teamB,
            { subOut = null },
            { viewModel.substitutePlayer(p, it)
                subOut = null })
    }
    editP?.let { p ->
        EditPlayerDialog(
            p,
            { editP = null },
            { name, prio -> viewModel.editPlayer(p, name, prio)
                editP = null })
    }

    confirmWinTeam?.let { team ->
        AlertDialog(
            onDismissRequest = { confirmWinTeam = null },
            title = { Text("Vitória do time $team?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishGame(team)
                        confirmWinTeam = null
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmWinTeam = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    val presentPlayers =
        remember(sortedPlayers, presentIds) { sortedPlayers.filter { presentIds.contains(it.id) } }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isSetupMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                    animationSpec = tween(
                        500
                    )
                )
            },
            label = "SetupModeAnim"
        ) { inSetup ->
            if (inSetup) {
                ManualSetupScreen(
                    presentPlayers,
                    showElo,
                    { tA, tB, b, teamSize ->
                        viewModel.updateConfig(
                            teamSize,
                            config.victoryLimit,
                            config.priorityEnabled
                        )
                        viewModel.startManualGame(tA, tB, b)
                        onSetupModeChange(false)
                    },
                    { onSetupModeChange(false) }
                )
            } else {
                AnimatedContent(
                    targetState = teamA.isNotEmpty() || teamB.isNotEmpty(),
                    label = "GameActiveAnim"
                ) { active ->
                    if (active) {
                        ActiveGameView(
                            viewModel,
                            teamA,
                            teamB,
                            waitingList,
                            owner,
                            streak,
                            isDarkTheme,
                            showElo,
                            { showCancel = true },
                            { subOut = it },
                            { confirmWinTeam = it },
                            onOpenAbsentList = { showAbsentDialog = true })
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    EmptyStateCard(
                                        presentIds.size,
                                        selectedGroup,
                                        config.teamSize,
                                        { onSetupModeChange(true) },
                                        {
                                            if (presentIds.size >= config.teamSize * 2) viewModel.startNewAutomaticGame(
                                                sortedPlayers,
                                                config.teamSize
                                            )
                                        },
                                        hasPrev,
                                        { viewModel.startNextRound() },
                                        winners,
                                        owner,
                                        streak,
                                        config.victoryLimit,
                                        isDarkTheme,
                                        onShowSnackbar = onShowSnackbar
                                    )
                                }
                                
                                if (sortedPlayers.isEmpty()) {
                                    item {
                                        Text(
                                            text = "Para começar, adicione jogadores no botão \"+\" no canto superior direito da tela.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp)
                                        )
                                    }
                                } else {
                                    item {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            Arrangement.SpaceBetween,
                                            Alignment.CenterVertically
                                        ) {
                                            Text("Lista de presença", fontWeight = FontWeight.Bold)
                                            val all =
                                                sortedPlayers.all { presentIds.contains(it.id) }; TextButton(
                                        onClick = {
                                            viewModel.setAllPlayersPresence(
                                                sortedPlayers,
                                                !all
                                            )
                                        }) { Text(if (all) "Desmarcar todos" else "Marcar todos") }
                                    }
                                }
                                items(sortedPlayers) { p ->
                                    PlayerCard(
                                        p,
                                        presentIds.contains(p.id),
                                        gamesPlayedMap[p.id],
                                        targetDate,
                                        showElo,
                                        showToll,
                                        { viewModel.togglePlayerPresence(p) },
                                        { onDeleteRequest(p) },
                                        { editP = p })
                                }
                            }
                        }
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shadowElevation = 8.dp
                            ) {
                                val selCount = presentIds.size
                                val totalCount = sortedPlayers.size
                                val text = if (selCount == 0) {
                                    "Nenhum selecionado ($totalCount cadastrado${if (totalCount > 1) "s)" else ")"}"
                                } else {
                                    "$selCount selecionado${if (selCount > 1) "s" else ""} de $totalCount cadastrado${if (totalCount > 1) "s" else ""}"
                                }
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveGameView(
    viewModel: VoleiViewModel,
    teamA: List<Player>,
    teamB: List<Player>,
    waitingList: List<Player>,
    streakOwner: String?,
    currentStreak: Int,
    isDarkTheme: Boolean,
    showElo: Boolean,
    onCancelRequest: () -> Unit,
    onSubRequest: (Player) -> Unit,
    onWinRequest: (String) -> Unit,
    onOpenAbsentList: () -> Unit
) {
    val teamAStreak = if (streakOwner == "A") currentStreak else 0
    val teamBStreak = if (streakOwner == "B") currentStreak else 0
    
    val cardColorA = MaterialTheme.colorScheme.primaryContainer
    val btnColorA = MaterialTheme.colorScheme.primary
    val btnTextColorA = MaterialTheme.colorScheme.onPrimary
    
    val cardColorB = LocalExtendedColors.current.anotherPrime.colorContainer
    val btnColorB = LocalExtendedColors.current.anotherPrime.color
    val btnTextColorB = LocalExtendedColors.current.anotherPrime.onColor
    
    val defaultStreakColor = Color(0xFFFF6F00)
    val yellowStreakColor = Color(0xFFFFD600)
    val streakColorA = if (isDarkTheme) yellowStreakColor else defaultStreakColor
    val streakColorB = if (isDarkTheme) yellowStreakColor else defaultStreakColor

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        if (isLandscape) {
            Row(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                Column(modifier = Modifier
                    .weight(0.75f)
                    .fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            ActiveTeamCard(
                                "Time A",
                                teamA,
                                cardColorA,
                                btnColorA,
                                btnTextColorA,
                                streakColorA,
                                teamAStreak,
                                showElo,
                                onSubRequest
                            ) { onWinRequest("A") }
                        }
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            ActiveTeamCard(
                                "Time B",
                                teamB,
                                cardColorB,
                                btnColorB,
                                btnTextColorB,
                                streakColorB,
                                teamBStreak,
                                showElo,
                                onSubRequest
                            ) { onWinRequest("B") }
                        }
                    }
                    TextButton(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(24.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("Cancelar partida", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textDecoration = TextDecoration.Underline) }
                }
                Spacer(Modifier.width(16.dp)); HorizontalDivider(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .alpha(0.2f)
            ); Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()) {
                    Text(
                        "Na espera (${waitingList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(
                            waitingList
                        ) { i, p ->
                            WaitingPlayerCard(
                                i + 1,
                                p,
                                showElo
                            ) { viewModel.removePlayerFromWaitingList(p) }
                        }
                        item {
                            AbsentPlayerGhostCard (onClick = onOpenAbsentList)
                        }    
                    }
                }
            }
        } else {
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    ActiveTeamCard(
                        "Time A",
                        teamA,
                        cardColorA,
                        btnColorA,
                        btnTextColorA,
                        streakColorA,
                        teamAStreak,
                        showElo,
                        onSubRequest
                    ) { onWinRequest("A") }
                }
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { Text("VS", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    ActiveTeamCard(
                        "Time B",
                        teamB,
                        cardColorB,
                        btnColorB,
                        btnTextColorB,
                        streakColorB,
                        teamBStreak,
                        showElo,
                        onSubRequest
                    ) { onWinRequest("B") }
                }
            }
            TextButton(
                onClick = onCancelRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) { Text("Cancelar partida", color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.Underline) }
            Spacer(Modifier.height(2.dp))
            Text("Na espera (${waitingList.size})", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(waitingList) { i, p ->
                    WaitingPlayerCard(
                        i + 1,
                        p,
                        showElo
                    ) { viewModel.removePlayerFromWaitingList(p) }
                }
                item {
                    AbsentPlayerGhostCard (onClick = onOpenAbsentList)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveTeamCard(
    name: String,
    players: List<Player>,
    cardColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    streakColor: Color,
    streak: Int,
    showElo: Boolean,
    onPlayerClick: (Player) -> Unit,
    onWin: () -> Unit
) {
    val avgElo = if (players.isNotEmpty()) players.map { it.elo }.average() else 0.0
    val contentColor = if (cardColor.luminance() < 0.5f) Color.White else Color.Black
    val dividerColor = contentColor.copy(alpha = 0.2f)
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (showElo) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "(Média: ${EloCalculator.formatElo(avgElo)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
                if (streak > 0) {
                    Spacer(Modifier.width(4.dp)); Text(
                        "🔥 $streak",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = streakColor
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = dividerColor)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                players.forEach { p ->
                    // Mudança principal aqui, sem box interna consumindo o clique inteiro:
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { onPlayerClick(p) }
                            )
                    ) {
                        Text(
                            text = if (showElo) "${p.name} (${EloCalculator.formatElo(p.elo)})" else p.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, color = contentColor
                        )
                        if (p.isPriority) {
                            Spacer(Modifier.width(2.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(12.dp),
                                tint = contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onWin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "VITÓRIA",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = buttonTextColor
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    selectedCount: Int,
    currentGroup: String,
    currentTeamSize: Int,
    onStartManualClick: () -> Unit,
    onStartAutoClick: () -> Unit,
    hasPreviousMatch: Boolean = false,
    onNextRoundClick: () -> Unit = {},
    lastWinners: List<Player> = emptyList(),
    streakOwner: String? = null,
    currentStreak: Int = 0,
    victoryLimit: Int = 3,
    isDarkTheme: Boolean = false,
    onShowSnackbar: (String) -> Unit
) {
    val minNeeded = currentTeamSize * 2
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = null
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (hasPreviousMatch) {
                val limitReached = currentStreak >= victoryLimit
                if (limitReached) {
                    val kingTextColor = MaterialTheme.colorScheme.tertiary
                    Text(
                        text = "👑 Rei da quadra atingiu o limite!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = kingTextColor,
                        textAlign = TextAlign.Center
                    ); Spacer(modifier = Modifier.height(4.dp)); Text(
                        text = "O time vencedor venceu $currentStreak seguidas e será redistribuído na próxima rodada.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val teamName =
                        if (streakOwner == "A") "Time A" else if (streakOwner == "B") "Time B" else "Vencedor"
                    val playerNames = lastWinners.joinToString(", ") { it.name }
                    Text(
                        text = "Vitória do $teamName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "($playerNames)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text("Grupo: $currentGroup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Mínimo: $minNeeded jogadores",
                    color = if (selectedCount < minNeeded) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (hasPreviousMatch) {
                Button(
                    onClick = onNextRoundClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text("Iniciar próximo jogo", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary) }
            } else {
                Button(
                    onClick = onStartAutoClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedCount >= minNeeded,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    ); Spacer(Modifier.width(8.dp)); Text(
                    "Iniciar jogo",
                    fontSize = 16.sp
                )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (selectedCount >= 4) {
                TextButton(onClick = onStartManualClick) {
                    Text(
                        text = "Ou montar times manualmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.Underline
                    )
                }
            } else {
                TextButton(onClick = {
                    onShowSnackbar("Selecione no mínimo 4 jogadores")
                }) {
                    Text(
                        text = "Ou montar times manualmente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(
    player: Player,
    isPresent: Boolean,
    gamesPlayed: Int?,
    targetDate: String,
    showElo: Boolean,
    showToll: Boolean,
    onTogglePresence: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .combinedClickable(onClick = onTogglePresence, onLongClick = { showMenu = true }),
            colors = CardDefaults.cardColors(containerColor = if (isPresent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            border = if (isPresent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() })
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = player.name, fontWeight = FontWeight.Bold)
                        if (player.isPriority) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Prioridade",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (showElo) {
                        Text(
                            text = "Elo: ${EloCalculator.formatElo(player.elo)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val actualGames = gamesPlayed ?: 0
                    val hasToll = player.tollDate == targetDate && player.dailyToll > 0

                    val info = if (actualGames == 0 && !hasToll) {
                        "Sem jogos recentes"
                    } else {
                        val gamesStr = if (actualGames == 1) "1 jogo" else "$actualGames jogos"
                        if (showToll && hasToll) {
                            "$gamesStr (+${player.dailyToll})"
                        } else {
                            gamesStr
                        }
                    }
                    Text(text = info, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(x = 16.dp, y = 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Editar") },
                onClick = { showMenu = false; onEdit() },
                leadingIcon = { Icon(Icons.Default.Edit, null) })
            DropdownMenuItem(
                text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaitingPlayerCard(index: Int, player: Player, showElo: Boolean, onRemove: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .widthIn(min = 120.dp)
            .combinedClickable(onClick = { }, onLongClick = { showMenu = true })
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${index}º",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player.isPriority) {
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Prioridade",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (showElo) {
                    Text(
                        "Elo: ${EloCalculator.formatElo(player.elo)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Remover da fila", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    onRemove()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

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
        ); Spacer(Modifier.width(8.dp)); Text(text)
    }
}

@Composable
fun RenameGroupDialog(oldName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newName by remember { mutableStateOf(oldName) }; AlertDialog(
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } })
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
        title = { Text("Editar jogador") },
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
        title = { Text("Novo jogador") },
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
        title = { Text("Regras: $groupName") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                    Spacer(Modifier.width(8.dp))
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
    var text by remember { mutableStateOf("") }; AlertDialog(
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) } })
}

@Composable
fun AbsentPlayerGhostCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .widthIn(min = 120.dp)
            .height(60.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Ausentes",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

fun Modifier.simpleScrollbar(state: LazyListState): Modifier = this.drawWithContent {
    drawContent()
    val visibleItems = state.layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return@drawWithContent
    val totalItems = state.layoutInfo.totalItemsCount
    if (totalItems <= visibleItems.size) return@drawWithContent

    val firstItem = visibleItems.first()
    val firstOffset = firstItem.offset.coerceAtMost(0).toFloat() / firstItem.size.coerceAtLeast(1).toFloat()
    val exactIndex = firstItem.index.toFloat() - firstOffset

    val fractionVisible = visibleItems.size.toFloat() / totalItems.toFloat()
    val fractionScrolled = exactIndex / totalItems.toFloat()

    val verticalPadding = 8.dp.toPx() // Distância do topo e rodapé do container
    val availableHeight = size.height - (verticalPadding * 2)

    val scrollbarHeight = availableHeight * fractionVisible
    val scrollbarY = verticalPadding + (availableHeight * fractionScrolled)

    drawRoundRect(
        color = Color.Gray.copy(alpha = 0.5f),
        topLeft = Offset(size.width - 8.dp.toPx(), scrollbarY), // Empurra um pouco mais para a esquerda
        size = Size(4.dp.toPx(), scrollbarHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
}

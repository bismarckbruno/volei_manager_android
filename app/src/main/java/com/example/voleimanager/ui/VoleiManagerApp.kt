package com.example.voleimanager.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.ui.components.*
import com.example.voleimanager.ui.game.GameScreenContent
import com.example.voleimanager.ui.viewmodel.CsvType
import com.example.voleimanager.ui.viewmodel.Screen
import com.example.voleimanager.ui.viewmodel.ThemeMode
import com.example.voleimanager.ui.viewmodel.VoleiViewModel
import kotlinx.coroutines.launch

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

    var isSetupMode by rememberSaveable { mutableStateOf(false) }

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showRenameGroupDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteGroupDialog by remember { mutableStateOf<String?>(null) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    var pendingGroupSwitch by remember { mutableStateOf<String?>(null) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("volei_data") }
    var pendingImportType by remember { mutableStateOf(CsvType.JOGADORES) }

    val launcherImport =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.importData(it, pendingImportType, context)
                Toast.makeText(context, "Importando...", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(uniqueGroups) {
        if (selectedGroup == null && uniqueGroups.isNotEmpty()) selectedGroup = uniqueGroups.first()
    }
    LaunchedEffect(selectedGroup) { selectedGroup?.let { viewModel.loadGroupConfig(it) } }

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
                            viewModel.exportData(context, CsvType.BACKUP_COMPLETO, exportFileName)
                            showExportDialog = false
                        }) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Backup Completo (.json)")
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Exportar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.JOGADORES, exportFileName)
                            showExportDialog = false
                        }) { Text("Jogadores") }
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.HISTORICO, exportFileName)
                            showExportDialog = false
                        }) { Text("Histórico") }
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.ELO_LOGS, exportFileName)
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
                            pendingImportType = CsvType.BACKUP_COMPLETO
                            launcherImport.launch(arrayOf("application/json", "text/plain"))
                            showImportDialog = false
                        }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Restaurar Backup (.json)")
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Importar CSV (Avançado)", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            pendingImportType = CsvType.JOGADORES
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
                            showImportDialog = false
                        }) { Text("Jogadores") }
                        TextButton(onClick = {
                            pendingImportType = CsvType.HISTORICO
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
                            showImportDialog = false
                        }) { Text("Histórico") }
                        TextButton(onClick = {
                            pendingImportType = CsvType.ELO_LOGS
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
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
                                            if (viewModel.isGameInProgress()) pendingGroupSwitch = group
                                            else {
                                                selectedGroup = group; viewModel.loadGroupConfig(group)
                                            }
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(text = { Text("+ Criar novo grupo", fontWeight = FontWeight.Bold) }, onClick = { showCreateGroupDialog = true; groupExpanded = false })
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
                        onClick = { showConfigDialog = true; scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.Palette, null) },
                        label = { Text("Tema") },
                        selected = false,
                        onClick = { showThemeDialog = true; scope.launch { drawerState.close() } })
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
                        onClick = { showExportDialog = true; scope.launch { drawerState.close() } })
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.FileDownload, null) },
                        label = { Text("Importar") },
                        selected = false,
                        onClick = { showImportDialog = true; scope.launch { drawerState.close() } })

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
                selectedGroup = newName; viewModel.loadGroupConfig(newName); showCreateGroupDialog = false
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
            val mode by viewModel.themeMode.collectAsState()
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Tema") },
                text = {
                    Column {
                        ThemeOption("Sistema", mode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                        ThemeOption("Claro", mode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
                        ThemeOption("Escuro", mode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) { Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                        onClick = { viewModel.deletePlayer(player); playerToDelete = null }) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playerToDelete = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                onDismissRequest = { showDeleteGroupDialog = null },
                title = { Text("Excluir grupo '$group'?") },
                text = { Text("Tem certeza? Todos os dados desse grupo serão apagados permanentemente.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteGroup(group); selectedGroup = "Geral"
                            showDeleteGroupDialog = null
                        }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteGroupDialog = null }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                })
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Vôlei Manager")
                            selectedGroup?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.GAME) IconButton(onClick = { showAddPlayerDialog = true }) { 
                            Icon(Icons.Default.Add, "Adicionar novo jogador") 
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
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

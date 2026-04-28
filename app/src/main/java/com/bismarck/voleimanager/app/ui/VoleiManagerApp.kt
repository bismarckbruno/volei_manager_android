package com.bismarck.voleimanager.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.components.*
import com.bismarck.voleimanager.app.ui.game.GameScreenContent
import com.bismarck.voleimanager.app.ui.theme.AppTheme
import com.bismarck.voleimanager.app.ui.theme.voleiManagerBlue
import com.bismarck.voleimanager.app.ui.viewmodel.CsvType
import com.bismarck.voleimanager.app.ui.viewmodel.Screen
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
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
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val showScore = groupConfig.scoreEnabled
    val uniqueGroups = remember(allPlayers) { allPlayers.map { it.groupName }.distinct().sorted() }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    var isSetupMode by rememberSaveable { mutableStateOf(false) }
    var historySelectedTab by rememberSaveable { mutableStateOf(0) }
    var historyPlayerSortMode by rememberSaveable { mutableStateOf(PlayerSortMode.ALPHABETICAL) }
    var historyMatchSortMode by rememberSaveable { mutableStateOf(MatchSortMode.NEWEST) }

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
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsOfUseDialog by remember { mutableStateOf(false) }
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
                    scope.launch { drawerState.close() }
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
                        }) { Text("Elo diário") }
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
                        }) { Text("Elo diário") }
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

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    "Política de Privacidade",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Última atualização: abril de 2026\n\n" +

                        "O Vôlei Manager é um aplicativo gratuito de código aberto para " +
                        "organização de partidas recreativas de vôlei.\n\n" +

                        "1. Dados coletados\n" +
                        "O aplicativo armazena exclusivamente os dados que você fornece " +
                        "manualmente: nomes dos jogadores, grupos, histórico de partidas e " +
                        "pontuação Elo. Nenhum dado pessoal sensível (e-mail, telefone, " +
                        "localização, identificadores de dispositivo) é coletado.\n\n" +

                        "2. Armazenamento local\n" +
                        "Todos os dados são armazenados localmente no seu dispositivo, " +
                        "utilizando o banco de dados interno do aplicativo (Room/SQLite). " +
                        "Nenhum dado é enviado para servidores externos.\n\n" +

                        "3. Compartilhamento de dados\n" +
                        "O aplicativo não compartilha dados com terceiros. As funções de " +
                        "exportação (CSV/JSON) e compartilhamento de imagem de resultado " +
                        "são ações iniciadas exclusivamente por você.\n\n" +

                        "4. Serviços de terceiros\n" +
                        "O aplicativo não utiliza serviços de análise (analytics), " +
                        "rastreamento, publicidade ou qualquer outro serviço que colete " +
                        "dados do usuário.\n\n" +

                        "5. Permissões\n" +
                        "O aplicativo não solicita permissões especiais do dispositivo " +
                        "(câmera, microfone, localização, etc.).\n\n" +

                        "6. Exclusão de dados\n" +
                        "Você pode excluir qualquer dado a qualquer momento diretamente " +
                        "pelo aplicativo (excluir jogadores, limpar histórico) ou " +
                        "desinstalando o aplicativo, o que remove todos os dados " +
                        "armazenados.\n\n" +

                        "7. Crianças\n" +
                        "O aplicativo não é direcionado a menores de 13 anos e não coleta " +
                        "intencionalmente dados de crianças.\n\n" +

                        "8. Alterações\n" +
                        "Esta política pode ser atualizada em versões futuras do aplicativo. " +
                        "Alterações significativas serão comunicadas nas notas de atualização.\n\n" +

                        "9. Contato\n" +
                        "Dúvidas sobre esta política podem ser enviadas para o desenvolvedor " +
                        "através da página do aplicativo na Google Play Store.",

                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, "https://bismarckbruno.github.io/volei_manager_android/PRIVACY_POLICY".toUri())
                    context.startActivity(intent)
                }) {
                    Text("Ver no navegador")
                }
            }
        )
    }

    if (showTermsOfUseDialog) {
        AlertDialog(
            onDismissRequest = { showTermsOfUseDialog = false },
            title = {
                Text(
                    "Termos de Uso",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Última atualização: abril de 2026\n\n" +

                        "Ao utilizar o Vôlei Manager, você concorda com os seguintes termos:\n\n" +

                        "1. Finalidade\n" +
                        "O Vôlei Manager é um aplicativo gratuito destinado à organização " +
                        "de partidas recreativas de vôlei, incluindo sorteio de times, " +
                        "gerenciamento de fila de espera e registro de histórico.\n\n" +

                        "2. Uso permitido\n" +
                        "O aplicativo é fornecido para uso pessoal e recreativo. Você é " +
                        "responsável pelos dados que insere no aplicativo (nomes de " +
                        "jogadores, grupos, etc.).\n\n" +

                        "3. Sem garantias\n" +
                        "O aplicativo é fornecido \"como está\", sem garantias de qualquer " +
                        "tipo. O desenvolvedor não se responsabiliza por perdas de dados " +
                        "decorrentes de falhas do dispositivo, atualizações do sistema " +
                        "operacional ou uso indevido do aplicativo.\n\n" +

                        "4. Backup\n" +
                        "É recomendável utilizar regularmente a função de exportação/backup " +
                        "para proteger seus dados. O desenvolvedor não se responsabiliza " +
                        "pela recuperação de dados não salvos.\n\n" +

                        "5. Disponibilidade\n" +
                        "O aplicativo pode ser atualizado, modificado ou descontinuado a " +
                        "qualquer momento sem aviso prévio.\n\n" +

                        "6. Propriedade intelectual\n" +
                        "O código-fonte, design e conteúdo do Vôlei Manager são de " +
                        "propriedade do desenvolvedor, protegidos pelas leis de direitos " +
                        "autorais aplicáveis.\n\n" +

                        "7. Alterações nos termos\n" +
                        "Estes termos podem ser atualizados em versões futuras. O uso " +
                        "continuado do aplicativo após alterações constitui a aceitação " +
                        "dos novos termos.\n\n" +

                        "8. Contato\n" +
                        "Dúvidas sobre estes termos podem ser enviadas para o desenvolvedor " +
                        "através da página do aplicativo na Google Play Store.",

                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsOfUseDialog = false }) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, "https://bismarckbruno.github.io/volei_manager_android/TERMS_OF_USE".toUri())
                    context.startActivity(intent)
                }) {
                    Text("Ver no navegador")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(modifier = Modifier.safeDrawingPadding()) {
                ModalDrawerSheet(
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                    Text(
                        "Vôlei Manager",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Grupo atual:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    var groupExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = groupExpanded,
                        onExpandedChange = { groupExpanded = !groupExpanded }) {
                        OutlinedTextField(
                            value = selectedGroup ?: "Selecione",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = {
                                val rotation by animateFloatAsState(
                                    targetValue = if (groupExpanded) 180f else 0f,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "GroupMenuRotation"
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.rotate(rotation)
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(56.dp)
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
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                            DropdownMenuItem(text = {
                                Text(
                                    "+ Criar novo grupo",
                                    fontWeight = FontWeight.Bold
                                )
                            }, onClick = { showCreateGroupDialog = true; groupExpanded = false })
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.PlayCircle, null) },
                        label = { Text("Jogo") },
                        selected = currentScreen == Screen.GAME,
                        onClick = { viewModel.navigateTo(Screen.GAME); scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.DateRange, null) },
                        label = { Text("Histórico") },
                        selected = currentScreen == Screen.HISTORY,
                        onClick = { viewModel.navigateTo(Screen.HISTORY); scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null) },
                        label = { Text("Perguntas frequentes") },
                        selected = currentScreen == Screen.FAQ,
                        onClick = { viewModel.navigateTo(Screen.FAQ); scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.Info, null) },
                        label = { Text("Sobre o app") },
                        selected = currentScreen == Screen.ABOUT,
                        onClick = { viewModel.navigateTo(Screen.ABOUT); scope.launch { drawerState.close() } }
                    )

                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text("Configurações", style = MaterialTheme.typography.labelMedium)

                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        label = { Text("Regras do grupo") },
                        selected = false,
                        onClick = { showConfigDialog = true; scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.Palette, null) },
                        label = { Text("Tema") },
                        selected = false,
                        onClick = { showThemeDialog = true; scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, null) },
                        label = { Text("Mostrar Elo") },
                        selected = false,
                        badge = { Switch(checked = showElo, onCheckedChange = null) },
                        tooltipText = "Mostra a pontuação de habilidade de cada jogador e média do time.",
                        onClick = { viewModel.setShowElo(!showElo) }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.AlarmAdd, null) },
                        label = { Text("Mostrar atraso") },
                        selected = false,
                        badge = { Switch(checked = showToll, onCheckedChange = null) },
                        tooltipText = "Mostra a média do número de jogos de quando a pessoa atrasada chegou.",
                        onClick = { viewModel.setShowToll(!showToll) }
                    )
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text("Dados", style = MaterialTheme.typography.labelMedium)

                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.FileUpload, null) },
                        label = { Text("Exportar") },
                        selected = false,
                        onClick = { showExportDialog = true; scope.launch { drawerState.close() } }
                    )
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.FileDownload, null) },
                        label = { Text("Importar") },
                        selected = false,
                        onClick = { showImportDialog = true; scope.launch { drawerState.close() } }
                    )
                    HorizontalDivider(
                        Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text("Privacidade", style = MaterialTheme.typography.labelMedium)

                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.Lock, null) },
                        label = { Text("Política de Privacidade") },
                        selected = false,
                        onClick = {
                            showPrivacyPolicyDialog = true
                            scope.launch { drawerState.close() }
                        })
                    FlexibleDrawerItem(
                        icon = { Icon(Icons.Outlined.Description, null) },
                        label = { Text("Termos de Uso") },
                        selected = false,
                        onClick = {
                            showTermsOfUseDialog = true
                            scope.launch { drawerState.close() }
                        })

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }) {
        if (showConfigDialog) {
            GroupConfigDialog(
                groupName = selectedGroup ?: "Geral",
                initialTeamSize = groupConfig.teamSize,
                initialVictoryLimit = groupConfig.victoryLimit,
                initialPriorityEnabled = groupConfig.priorityEnabled,
                initialScoreEnabled = groupConfig.scoreEnabled,
                onDismiss = { showConfigDialog = false },
                onConfirm = { size, limit, prior, scoreEn ->
                    viewModel.updateConfig(size, limit, prior, scoreEn)
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
            val mode by viewModel.themeMode.collectAsState()
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Tema") },
                text = {
                    Column {
                        ThemeOption(
                            "Sistema",
                            mode == ThemeMode.SYSTEM
                        ) {
                            viewModel.setThemeMode(
                                ThemeMode.SYSTEM
                            )
                        }
                        ThemeOption(
                            "Claro",
                            mode == ThemeMode.LIGHT
                        ) {
                            viewModel.setThemeMode(
                                ThemeMode.LIGHT
                            )
                        }
                        ThemeOption(
                            "Escuro",
                            mode == ThemeMode.DARK
                        ) {
                            viewModel.setThemeMode(
                                ThemeMode.DARK
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(
                            "Fechar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    TextButton(onClick = { playerToDelete = null }) {
                        Text(
                            "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    TextButton(onClick = { showDeleteGroupDialog = null }) {
                        Text(
                            "Cancelar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                })
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                FlexibleTopAppBar(
                    title = {
                        Column {
                            Text("Vôlei Manager")
                            selectedGroup?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu lateral")
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.GAME) {
                            IconButton(onClick = { showAddPlayerDialog = true }) {
                                Icon(Icons.Default.Add, "Adicionar novo jogador")
                            }
                        } else if (currentScreen == Screen.HISTORY) {
                            val view = LocalView.current
                            val historyDate by viewModel.historyDateFilter.collectAsState()
                            val groupHistory by viewModel.currentGroupHistory.collectAsState()
                            val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
                            val eloLogs by viewModel.currentGroupEloLogs.collectAsState()

                            IconButton(onClick = {
                                if (historyDate == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Selecione uma data específica para compartilhar o histórico.") }
                                } else {
                                    Toast.makeText(context, "Gerando imagem...", Toast.LENGTH_SHORT)
                                        .show()

                                    if (historySelectedTab == 0) {
                                        // --- Export matches ---
                                        val sdf = java.text.SimpleDateFormat(
                                            "dd/MM/yyyy HH:mm",
                                            java.util.Locale.getDefault()
                                        )
                                        val filteredMatches = groupHistory.filter {
                                            it.date.startsWith(historyDate!!)
                                        }
                                        val matchesToShare = when (historyMatchSortMode) {
                                            MatchSortMode.NEWEST -> filteredMatches.sortedWith(
                                                compareByDescending<MatchHistory> {
                                                    try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
                                                }.thenByDescending { it.id }
                                            )
                                            MatchSortMode.OLDEST -> filteredMatches.sortedWith(
                                                compareBy<MatchHistory> {
                                                    try { sdf.parse(it.date)?.time ?: 0L } catch (_: Exception) { 0L }
                                                }.thenByDescending { it.id }
                                            )
                                            MatchSortMode.ELO_DELTA -> filteredMatches.sortedWith(
                                                compareByDescending<MatchHistory> { it.eloPoints }
                                                    .thenByDescending { it.id }
                                            )
                                            MatchSortMode.SCORE_DIFF -> filteredMatches.sortedWith(
                                                compareByDescending<MatchHistory> {
                                                    val sa = it.teamAScore ?: 0
                                                    val sb = it.teamBScore ?: 0
                                                    kotlin.math.abs(sa - sb)
                                                }.thenByDescending { it.id }
                                            )
                                        }

                                        val mdm = mutableMapOf<Int, Int>()
                                        matchesToShare.forEach { match ->
                                            if (match.startTimestamp != null && match.endTimestamp != null && match.endTimestamp > match.startTimestamp) {
                                                mdm[match.id] = ((match.endTimestamp - match.startTimestamp) / 60000L).toInt().coerceAtLeast(1)
                                            }
                                        }

                                        val avgDurationText = if (mdm.isNotEmpty()) {
                                            "${mdm.values.average().toInt()} min"
                                        } else null

                                        viewModel.captureHistoryScreenAsImage(
                                            context = context,
                                            view = view,
                                            matches = matchesToShare,
                                            matchSortMode = historyMatchSortMode,
                                            players = null,
                                            playerSortMode = null,
                                            date = historyDate!!,
                                            isDarkTheme = isDarkTheme,
                                            showElo = showElo,
                                            showScore = showScore,
                                            matchDurationsMinutes = mdm,
                                            averagePlayersEloText = null,
                                            averageMatchDurationText = avgDurationText
                                        )
                                    } else {
                                        // --- Export players ---
                                        val filteredMatches = groupHistory.filter {
                                            it.date.startsWith(historyDate!!)
                                        }
                                        val uniquePlayerNames = filteredMatches.flatMap { match ->
                                            (match.teamA.split(",") + match.teamB.split(","))
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                        }.distinct()

                                        // Convert historyDate (dd/MM/yyyy) to elo log date format (yyyy-MM-dd)
                                        val eloDateStr: String? = try {
                                            val parts = historyDate!!.split("/")
                                            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
                                        } catch (_: Exception) {
                                            null
                                        }

                                        val playerDataList = uniquePlayerNames.mapNotNull { name ->
                                            val player = groupPlayers.find { it.name == name }
                                            val logsForPlayer = if (eloDateStr != null) {
                                                if (player != null) eloLogs.filter { it.playerId == player.id && it.date == eloDateStr }
                                                else eloLogs.filter { it.playerNameSnapshot == name && it.date == eloDateStr }
                                            } else {
                                                if (player != null) eloLogs.filter { it.playerId == player.id }
                                                else eloLogs.filter { it.playerNameSnapshot == name }
                                            }
                                            
                                            val games = logsForPlayer.size
                                            val victories = logsForPlayer.count { it.won == true }
                                            val eloForDisplay = logsForPlayer.maxByOrNull { it.id }?.elo ?: (player?.elo ?: 1200.0)

                                            val effectivePlayer = player ?: Player(name = name, groupName = "", elo = 1200.0)

                                            HistoryPlayerInfo(
                                                effectivePlayer,
                                                eloForDisplay,
                                                name,
                                                games,
                                                victories
                                            )
                                        }

                                        fun HistoryPlayerInfo.winRate(): Double =
                                            if (gamesPlayed > 0) victories.toDouble() / gamesPlayed else 0.0

                                        val sortedPlayers = when (historyPlayerSortMode) {
                                            PlayerSortMode.ELO -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.displayElo }
                                                    .thenByDescending { it.winRate() }
                                            )
                                            PlayerSortMode.GAMES -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.gamesPlayed }
                                                    .thenByDescending { it.winRate() }
                                                    .thenByDescending { it.displayElo }
                                            )
                                            PlayerSortMode.VICTORIES -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.victories }
                                                    .thenByDescending { it.winRate() }
                                                    .thenByDescending { it.displayElo }
                                            )
                                            PlayerSortMode.PERCENTAGE -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.winRate() }
                                                    .thenBy { it.gamesPlayed }
                                                    .thenByDescending { it.displayElo }
                                            )
                                            PlayerSortMode.ALPHABETICAL -> playerDataList.sortedWith(
                                                compareBy<HistoryPlayerInfo> { it.player.name.lowercase() }
                                                    .thenByDescending { it.displayElo }
                                            )
                                        }

                                        val avgText = if (sortedPlayers.isNotEmpty()) {
                                            val eloAvg = sortedPlayers.map { it.displayElo }.average()
                                            com.bismarck.voleimanager.app.util.EloCalculator.formatElo(eloAvg)
                                        } else null
                                        viewModel.captureHistoryScreenAsImage(
                                            context = context,
                                            view = view,
                                            matches = null,
                                            matchSortMode = null,
                                            players = sortedPlayers,
                                            playerSortMode = historyPlayerSortMode,
                                            date = historyDate!!,
                                            isDarkTheme = isDarkTheme,
                                            showElo = showElo,
                                            showScore = showScore,
                                            matchDurationsMinutes = null,
                                            averagePlayersEloText = avgText,
                                            averageMatchDurationText = null
                                        )
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Share, "Compartilhar Histórico")
                            }
                        }
                    }
                )
            }
        ) { padding ->
             Box(Modifier
                 .padding(padding)
                 .fillMaxSize()
                 .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
                 AnimatedContent(
                     targetState = currentScreen,
                     transitionSpec = {
                         fadeIn(animationSpec = tween(500)) togetherWith fadeOut(
                             animationSpec = tween(
                                 500
                             )
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
                            showScore = showScore,
                            isSetupMode = isSetupMode,
                            onSetupModeChange = { isSetupMode = it },
                            onDeleteRequest = { playerToDelete = it },
                            onShowSnackbar = { msg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        msg
                                    )
                                }
                            }
                        )

                        Screen.HISTORY -> HistoryScreen(
                            matchSortMode = historyMatchSortMode,
                            onMatchSortModeChanged = { historyMatchSortMode = it },
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            showElo = showElo,
                            showScore = showScore,
                            selectedTab = historySelectedTab,
                            onTabChanged = { historySelectedTab = it },
                            playerSortMode = historyPlayerSortMode,
                            onPlayerSortModeChanged = { historyPlayerSortMode = it }
                        )
                        Screen.FAQ -> FAQScreen()
                        Screen.ABOUT -> AboutScreen()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FlexibleDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    badge: @Composable (() -> Unit)? = null,
    tooltipText: String? = null
) {
    val containerColor =
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = false)

    val itemContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (!tooltipText.isNullOrBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { tooltipState.show() }
                        }
                    }
                )
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(12.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                    label()
                }
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                badge()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        if (tooltipText.isNullOrBlank()) {
            itemContent()
        } else {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(tooltipText)
                    }
                },
                state = tooltipState,
                enableUserInput = false
            ) {
                itemContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlexibleTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                .padding(vertical = 8.dp)
                .heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                navigationIcon()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                    title()
                }
            }
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
    }
}

private fun captureFullHistory(
    context: android.content.Context,
    view: android.view.View,
    matches: List<MatchHistory>,
    date: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    showScore: Boolean,
    onBitmapReady: (android.graphics.Bitmap) -> Unit
) {
    val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
        setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

        setContent {
            AppTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false // Desativa cores baseadas no papel de parede para garantir o azul/amarelo da marca
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .width(400.dp)
                            .padding(horizontal = 16.dp)
                            .padding(
                                top = 32.dp,
                                bottom = 16.dp
                            ), // Padding vertical com safe area pro notch
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header com logo e título do App
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isDarkTheme) com.bismarck.voleimanager.app.R.drawable.bola_de_v_lei_mais_clara_para_fundo_escuro
                                    else com.bismarck.voleimanager.app.R.drawable.ic_launcher_foreground
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Vôlei Manager",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else voleiManagerBlue
                            )
                        }


                        // Título de Histórico
                        Text(
                            text = "Histórico - $date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        matches.forEach { match ->
                            HistoryItem(match = match, isDarkTheme = isDarkTheme, showElo = showElo, showScore = showScore)
                        }
                    }
                }
            }
        }
    }

    val scrollView = android.widget.ScrollView(context).apply {
        addView(composeView)
        alpha = 0f
        isVerticalScrollBarEnabled = false
    }

    val root = view.rootView as? android.view.ViewGroup
    if (root != null) {
        root.addView(
            scrollView, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.postDelayed({
            try {
                // Ao fixarmos uma escala predefinida ao invés de baseada no width nativo da View
                // garantimos que o Bitmap manterá a mesma aparência/escala independente de o 
                // celular do usuário estar de pé (retrato) ou deitado (paisagem).
                val constantWidthPixels = 1440 // Fixo em uma resolução típica em pixels
                val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                    constantWidthPixels,
                    android.view.View.MeasureSpec.EXACTLY
                )
                val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                    0,
                    android.view.View.MeasureSpec.UNSPECIFIED
                )

                composeView.measure(widthSpec, heightSpec)
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

                if (composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        composeView.measuredWidth,
                        composeView.measuredHeight,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    composeView.draw(canvas)
                    onBitmapReady(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                root.removeView(scrollView)
            }
        }, 500)
    }
}

private fun captureFullPlayers(
    context: android.content.Context,
    view: android.view.View,
    players: List<HistoryPlayerInfo>,
    date: String,
    isDarkTheme: Boolean,
    showElo: Boolean,
    sortedByElo: Boolean,
    onBitmapReady: (android.graphics.Bitmap) -> Unit
) {
    val composeView = androidx.compose.ui.platform.ComposeView(context).apply {
        setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())

        setContent {
            AppTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier
                            .width(400.dp)
                            .padding(horizontal = 16.dp)
                            .padding(top = 32.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header com logo e título do App
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isDarkTheme) com.bismarck.voleimanager.app.R.drawable.bola_de_v_lei_mais_clara_para_fundo_escuro
                                    else com.bismarck.voleimanager.app.R.drawable.ic_launcher_foreground
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Vôlei Manager",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.primary else voleiManagerBlue
                            )
                        }

                        // Subtítulo
                        Text(
                            text = "Jogadores - $date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        players.forEachIndexed { index, info ->
                            HistoryPlayerCard(
                                rank = if (sortedByElo) index + 1 else null,
                                player = info.player,
                                displayElo = info.displayElo,
                                showElo = showElo,
                                gamesPlayed = info.gamesPlayed,
                                victories = info.victories
                            )
                        }
                    }
                }
            }
        }
    }

    val scrollView = android.widget.ScrollView(context).apply {
        addView(composeView)
        alpha = 0f
        isVerticalScrollBarEnabled = false
    }

    val root = view.rootView as? android.view.ViewGroup
    if (root != null) {
        root.addView(
            scrollView, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        scrollView.postDelayed({
            try {
                val constantWidthPixels = 1440
                val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                    constantWidthPixels,
                    android.view.View.MeasureSpec.EXACTLY
                )
                val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
                    0,
                    android.view.View.MeasureSpec.UNSPECIFIED
                )

                composeView.measure(widthSpec, heightSpec)
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

                if (composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
                    val bitmap = android.graphics.Bitmap.createBitmap(
                        composeView.measuredWidth,
                        composeView.measuredHeight,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    composeView.draw(canvas)
                    onBitmapReady(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                root.removeView(scrollView)
            }
        }, 500)
    }
}




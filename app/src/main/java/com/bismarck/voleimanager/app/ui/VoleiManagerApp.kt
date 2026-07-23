package com.bismarck.voleimanager.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.bismarck.voleimanager.app.R
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.components.*
import com.bismarck.voleimanager.app.ui.game.GameScreenContent
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.CsvType
import com.bismarck.voleimanager.app.ui.viewmodel.Screen
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_MIN_PLAYERS
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun getDisplayGroupName(groupName: String?): String {
    val defaultName = "Geral" // Sua constante hardcoded
    val translatedDefault = stringResource(R.string.general) // A tradução

    return when {
        groupName == null -> translatedDefault
        groupName == defaultName -> translatedDefault
        else -> groupName // Mostra o nome que o usuário digitou (ex: "Amigos")
    }
}

@Composable
private fun getDisplayBalancingModeName(balancingMode: String): String {
    return when (balancingMode) {
        com.bismarck.voleimanager.app.data.model.BalancingMode.REST.name ->
            stringResource(R.string.mode_rest)
        else -> stringResource(R.string.mode_rebalance)
    }
}

@Composable
private fun getDisplayGroupWithMode(groupName: String?, balancingMode: String, teamSize: Int): String {
    return "${getDisplayGroupName(groupName)} - ${getDisplayBalancingModeName(balancingMode)} - ${teamSize}x${teamSize}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val DEFAULT_GROUP_NAME = "Geral"

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val select_specific_date = stringResource(R.string.select_specific_date)
    val generating_image = stringResource(R.string.generating_image)
    val importing = stringResource(R.string.importing)

    val uiMessage by viewModel.uiMessage.collectAsState()

    val currentScreen by viewModel.currentScreen.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val showElo by viewModel.showElo.collectAsState()
    val showToll by viewModel.showToll.collectAsState()
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val showScore = groupConfig.scoreEnabled
    val groupsSortedByRecent by viewModel.groupsSortedByRecentHistory.collectAsState()
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

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val navBarInsets = WindowInsets.navigationBars

// 2. Verifique os lados (em DP)
    val navBarLeft = with(density) { navBarInsets.getLeft(density, layoutDirection).toDp() }
    val navBarRight = with(density) { navBarInsets.getRight(density, layoutDirection).toDp() }

    val safeDrawingNavBarDirection = WindowInsets.safeDrawing.only(
        when {
            // Se houver barra na esquerda, protege o End (Direita)
            navBarLeft > 0.dp -> WindowInsetsSides.End
            // Se houver barra na direita, protege o Star (Esquerda)
            navBarRight > 0.dp -> WindowInsetsSides.Start
            // Caso padrão (barra embaixo ou gestos), mantém o comportamento padrão
            else -> WindowInsetsSides.Horizontal
        }
    )

    val launcherImport =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.importData(it, pendingImportType, context)
                Toast.makeText(context, importing, Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(groupsSortedByRecent) {
        if (selectedGroup == null && groupsSortedByRecent.isNotEmpty()) {
            selectedGroup = groupsSortedByRecent.first()
        }
    }
    LaunchedEffect(selectedGroup, groupConfig.onboardingStep) {
        val targetGroup = selectedGroup ?: return@LaunchedEffect
        val isOnboardingInProgress = groupConfig.onboardingStep < ONBOARDING_STEP_COMPLETE
        if (targetGroup != groupConfig.groupName && !isOnboardingInProgress) {
            viewModel.loadGroupConfig(targetGroup)
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearUiMessage()
        }
    }

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
            title = { Text(stringResource(R.string.change_group_title)) },
            text = { Text(stringResource(R.string.change_group_text)) },
            confirmButton = {
                Button(onClick = {
                    selectedGroup = pendingGroupSwitch
                    viewModel.loadGroupConfig(pendingGroupSwitch!!)
                    pendingGroupSwitch = null
                    scope.launch { drawerState.close() }
                }) { Text(stringResource(R.string.change_anyway)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingGroupSwitch = null
                }) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_data)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = it },
                        label = { Text(stringResource(R.string.file_name)) })
                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.exportData(context, CsvType.BACKUP_COMPLETO, exportFileName)
                            showExportDialog = false
                        }) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.full_backup))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(stringResource(R.string.export_csv), style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.JOGADORES, exportFileName)
                            showExportDialog = false
                        }) { Text(stringResource(R.string.players_word)) }
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.HISTORICO, exportFileName)
                            showExportDialog = false
                        }) { Text(stringResource(R.string.history)) }
                        TextButton(onClick = {
                            viewModel.exportData(context, CsvType.ELO_LOGS, exportFileName)
                            showExportDialog = false
                        }) { Text(stringResource(R.string.daily_elo)) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportDialog = false
                }) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_data)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            pendingImportType = CsvType.BACKUP_COMPLETO
                            launcherImport.launch(arrayOf("application/json", "text/plain"))
                            showImportDialog = false
                        }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.restore_backup))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text(stringResource(R.string.import_csv), style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = {
                            pendingImportType = CsvType.JOGADORES
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
                            showImportDialog = false
                        }) { Text(stringResource(R.string.players_word)) }
                        TextButton(onClick = {
                            pendingImportType = CsvType.HISTORICO
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
                            showImportDialog = false
                        }) { Text(stringResource(R.string.history)) }
                        TextButton(onClick = {
                            pendingImportType = CsvType.ELO_LOGS
                            launcherImport.launch(arrayOf("text/*", "text/csv", "application/csv"))
                            showImportDialog = false
                        }) { Text(stringResource(R.string.daily_elo)) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                }) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    stringResource(R.string.privacy_policy),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(R.string.privacy_policy_text))
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        privacyPolicyUrl.toUri()
                    )
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.view_in_browser))
                }
            }
        )
    }

    if (showTermsOfUseDialog) {
        val termsOfUseUrl = stringResource(R.string.terms_of_use_url)
        AlertDialog(
            onDismissRequest = { showTermsOfUseDialog = false },
            title = {
                Text(
                    stringResource(R.string.terms_of_use),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(R.string.terms_of_use_text))
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsOfUseDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        termsOfUseUrl.toUri()
                    )
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.view_in_browser))
                }
            }
        )
    }

    ModalNavigationDrawer(
        modifier = Modifier.systemBarsPadding(),
        drawerState = drawerState,
        drawerContent = {
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Vertical))) {
                ModalDrawerSheet {
                    Box(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Start))
                    ) {
                        Column(
                            Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(16.dp))

                            Text(stringResource(R.string.current_group), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(8.dp))
                            var groupExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = groupExpanded,
                                onExpandedChange = { groupExpanded = !groupExpanded }) {
                                OutlinedTextField(
                                    value = selectedGroup?.let { getDisplayGroupName(it) } ?: stringResource(R.string.select),                                    onValueChange = {}, readOnly = true,
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
                                    groupsSortedByRecent.forEach { group ->
                                        val isSelected = selectedGroup == group
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        getDisplayGroupName(group),
                                                        modifier = Modifier.weight(1f),
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
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
                                                        selectedGroup =
                                                            group; viewModel.loadGroupConfig(
                                                            group
                                                        )
                                                    }
                                                }
                                                scope.launch { drawerState.close() }
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(R.string.create_new_group),
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            showCreateGroupDialog = true; groupExpanded = false
                                        })
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.PlayCircle, null) },
                                label = { Text(stringResource(R.string.game_word)) },
                                selected = currentScreen == Screen.GAME,
                                onClick = { viewModel.navigateTo(Screen.GAME); scope.launch { drawerState.close() } }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.DateRange, null) },
                                label = { Text(stringResource(R.string.history)) },
                                selected = currentScreen == Screen.HISTORY,
                                onClick = { viewModel.navigateTo(Screen.HISTORY); scope.launch { drawerState.close() } }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null) },
                                label = { Text(stringResource(R.string.faq)) },
                                selected = currentScreen == Screen.FAQ,
                                onClick = { viewModel.navigateTo(Screen.FAQ); scope.launch { drawerState.close() } }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Info, null) },
                                label = { Text(stringResource(R.string.about_app)) },
                                selected = currentScreen == Screen.ABOUT,
                                onClick = { viewModel.navigateTo(Screen.ABOUT); scope.launch { drawerState.close() } }
                            )

                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(stringResource(R.string.settings), style = MaterialTheme.typography.labelMedium)

                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Settings, null) },
                                label = { Text(stringResource(R.string.group_rules)) },
                                selected = false,
                                onClick = {
                                    showConfigDialog = true; scope.launch { drawerState.close() }
                                }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Palette, null) },
                                label = { Text(stringResource(R.string.theme)) },
                                selected = false,
                                onClick = {
                                    showThemeDialog = true; scope.launch { drawerState.close() }
                                }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Default.WorkspacePremium, null) },
                                label = { Text(stringResource(R.string.show_elo)) },
                                selected = false,
                                badge = { Switch(checked = showElo, onCheckedChange = null) },
                                tooltipText = stringResource(R.string.show_elo_tooltip),
                                onClick = { viewModel.setShowElo(!showElo) }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.AlarmAdd, null) },
                                label = { Text(stringResource(R.string.show_lateness)) },
                                selected = false,
                                badge = { Switch(checked = showToll, onCheckedChange = null) },
                                tooltipText = stringResource(R.string.show_lateness_tooltip),
                                onClick = { viewModel.setShowToll(!showToll) }
                            )
                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(stringResource(R.string.data), style = MaterialTheme.typography.labelMedium)

                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.FileUpload, null) },
                                label = { Text(stringResource(R.string.export)) },
                                selected = false,
                                onClick = {
                                    showExportDialog = true; scope.launch { drawerState.close() }
                                }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.FileDownload, null) },
                                label = { Text(stringResource(R.string.import_text)) },
                                selected = false,
                                onClick = {
                                    showImportDialog = true; scope.launch { drawerState.close() }
                                }
                            )
                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(stringResource(R.string.privacy), style = MaterialTheme.typography.labelMedium)

                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Lock, null) },
                                label = { Text(stringResource(R.string.privacy_policy)) },
                                selected = false,
                                onClick = {
                                    showPrivacyPolicyDialog = true
                                    scope.launch { drawerState.close() }
                                })
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Description, null) },
                                label = { Text(stringResource(R.string.terms_of_use)) },
                                selected = false,
                                onClick = {
                                    showTermsOfUseDialog = true
                                    scope.launch { drawerState.close() }
                                })

                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

    ) {
        if (showConfigDialog) {
            GroupConfigDialog(
                groupName = selectedGroup ?: DEFAULT_GROUP_NAME,
                initialTeamSize = groupConfig.teamSize,
                initialVictoryLimit = groupConfig.victoryLimit,
                initialPriorityEnabled = groupConfig.priorityEnabled,
                initialScoreEnabled = groupConfig.scoreEnabled,
                initialBalancingMode = groupConfig.balancingMode,
                onDismiss = { showConfigDialog = false },
                onConfirm = { size, limit, prior, scoreEn, balancingMode ->
                    viewModel.updateConfig(size, limit, prior, scoreEn, balancingMode)
                    showConfigDialog = false
                }
            )
        }
        if (showCreateGroupDialog) CreateGroupDialog(
            { showCreateGroupDialog = false },
            { newName, balancingMode ->
                val normalizedGroupName = newName.trim().replace(Regex("\\s+"), " ").take(MAX_GROUP_NAME_LENGTH)
                selectedGroup = normalizedGroupName
                viewModel.createGroup(normalizedGroupName, balancingMode)
                showCreateGroupDialog = false
                scope.launch { drawerState.close() }
            })
        if (showAddPlayerDialog) AddPlayerDialog(
            { showAddPlayerDialog = false },
            { name, elo, isPriority ->
                viewModel.addPlayer(
                    name,
                    elo,
                    selectedGroup ?: DEFAULT_GROUP_NAME,
                    isPriority
                )
                showAddPlayerDialog = false
            })
        if (showThemeDialog) {
            val mode by viewModel.themeMode.collectAsState()
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text(stringResource(R.string.theme)) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        ThemeOption(
                            stringResource(R.string.theme_system),
                            mode == ThemeMode.SYSTEM
                        ) {
                            viewModel.setThemeMode(
                                ThemeMode.SYSTEM
                            )
                        }
                        ThemeOption(
                            stringResource(R.string.theme_light),
                            mode == ThemeMode.LIGHT
                        ) {
                            viewModel.setThemeMode(
                                ThemeMode.LIGHT
                            )
                        }
                        ThemeOption(
                            stringResource(R.string.theme_dark),
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
                            stringResource(R.string.close),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                })
        }
        playerToDelete?.let { player ->
            AlertDialog(
                onDismissRequest = { playerToDelete = null },
                title = { Text(stringResource(R.string.delete_player_title, player.name)) },
                text = { Text(stringResource(R.string.delete_player_text)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { viewModel.deletePlayer(player); playerToDelete = null }) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playerToDelete = null }) {
                        Text(
                            stringResource(R.string.cancel),
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
                    val normalizedName = newName.trim().replace(Regex("\\s+"), " ").take(MAX_GROUP_NAME_LENGTH)
                    scope.launch {
                        viewModel.renameGroup(group, normalizedName)
                        selectedGroup = normalizedName
                        showRenameGroupDialog = null
                    }
                })
        }
        showDeleteGroupDialog?.let { group ->
            AlertDialog(
                onDismissRequest = { showDeleteGroupDialog = null },
                title = { Text(stringResource(R.string.delete_group_title, getDisplayGroupName(group))) },                text = { Text(stringResource(R.string.delete_group_text)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.deleteGroup(group); selectedGroup = DEFAULT_GROUP_NAME
                            showDeleteGroupDialog = null
                        }) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteGroupDialog = null }) {
                        Text(
                            stringResource(R.string.cancel),
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
                            Text(stringResource(R.string.app_name))
                            selectedGroup?.let {
                                Text(
                                    getDisplayGroupWithMode(groupConfig.groupName, groupConfig.balancingMode, groupConfig.teamSize),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, stringResource(R.string.side_menu))
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.GAME) {
                            val groupConfig by viewModel.currentGroupConfig.collectAsState()
                            val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
                            val showAddPulse = groupConfig.onboardingStep == ONBOARDING_STEP_MIN_PLAYERS && groupPlayers.isEmpty()
                            
                            val scale by animateFloatAsState(
                                targetValue = if (showAddPulse) 1.25f else 1f,
                                animationSpec = if (showAddPulse) 
                                    infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                else tween(200),
                                label = "AddButtonPulse"
                            )
                            
                            val iconColor by animateColorAsState(
                                targetValue = if (showAddPulse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = if (showAddPulse)
                                    infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                else tween(200),
                                label = "AddButtonColor"
                            )
                            
                            IconButton(
                                onClick = { showAddPlayerDialog = true },
                                modifier = Modifier.scale(scale)
                            ) {
                                Icon(Icons.Default.Add, stringResource(R.string.add_new_player), tint = iconColor)
                            }
                        } else if (currentScreen == Screen.HISTORY) {
                            val view = LocalView.current
                            val historyDate by viewModel.historyDateFilter.collectAsState()
                            val groupHistory by viewModel.currentGroupHistory.collectAsState()
                            val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
                            val eloLogs by viewModel.currentGroupEloLogs.collectAsState()

                            IconButton(onClick = {
                                if (historyDate == null) {
                                    scope.launch { snackbarHostState.showSnackbar(select_specific_date)}
                                } else {
                                    Toast.makeText(context, generating_image, Toast.LENGTH_SHORT)
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

                                        data class PlayerIdentifier(val id: Int?, val name: String)
                                        val identifiers = mutableSetOf<PlayerIdentifier>()
                                        filteredMatches.forEach { match ->
                                            val namesA = match.teamA.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                            val idsA = match.teamAIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                                            namesA.forEachIndexed { index, name ->
                                                val id = idsA.getOrNull(index)
                                                identifiers.add(PlayerIdentifier(id, name))
                                            }
                                            
                                            val namesB = match.teamB.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                            val idsB = match.teamBIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                                            namesB.forEachIndexed { index, name ->
                                                val id = idsB.getOrNull(index)
                                                identifiers.add(PlayerIdentifier(id, name))
                                            }
                                        }

                                        val deduplicated = mutableListOf<PlayerIdentifier>()
                                        identifiers.forEach { identifier ->
                                            if (identifier.id != null && deduplicated.any { it.id == identifier.id }) return@forEach
                                            
                                            val existingByName = deduplicated.find { it.name == identifier.name }
                                            if (existingByName != null) {
                                                if (existingByName.id == null && identifier.id != null) {
                                                    deduplicated.remove(existingByName)
                                                    deduplicated.add(identifier)
                                                }
                                            } else {
                                                deduplicated.add(identifier)
                                            }
                                        }
                                        val uniquePlayerIdentifiers = deduplicated.toList()

                                        // Convert historyDate (dd/MM/yyyy) to elo log date format (yyyy-MM-dd)
                                        val eloDateStr: String? = try {
                                            val parts = historyDate!!.split("/")
                                            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else null
                                        } catch (_: Exception) {
                                            null
                                        }

                                        val matchDurationById = filteredMatches.associate { match ->
                                            val duration = if (
                                                match.startTimestamp != null &&
                                                match.endTimestamp != null &&
                                                match.endTimestamp > match.startTimestamp
                                            ) {
                                                ((match.endTimestamp - match.startTimestamp) / 60000L).toInt().coerceAtLeast(1)
                                            } else {
                                                0
                                            }
                                            match.id to duration
                                        }

                                        fun playerAppearsInMatch(match: MatchHistory, identifier: PlayerIdentifier): Boolean {
                                            val namesA = match.teamA.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                            val namesB = match.teamB.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                            val idsA = match.teamAIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                                            val idsB = match.teamBIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                                            return if (identifier.id != null) {
                                                idsA.contains(identifier.id) || idsB.contains(identifier.id) ||
                                                    ((idsA.isEmpty() && idsB.isEmpty()) && (namesA.contains(identifier.name) || namesB.contains(identifier.name)))
                                            } else {
                                                namesA.contains(identifier.name) || namesB.contains(identifier.name)
                                            }
                                        }

                                        val playerDataList = uniquePlayerIdentifiers.mapNotNull { identifier ->
                                            val player = groupPlayers.find { 
                                                if (identifier.id != null) it.id == identifier.id 
                                                else it.name == identifier.name 
                                            }
                                            val logsForPlayer = if (eloDateStr != null) {
                                                if (player != null) eloLogs.filter { it.playerId == player.id && it.date == eloDateStr }
                                                else eloLogs.filter { it.playerNameSnapshot == identifier.name && it.date == eloDateStr }
                                            } else {
                                                if (player != null) eloLogs.filter { it.playerId == player.id }
                                                else eloLogs.filter { it.playerNameSnapshot == identifier.name }
                                            }
                                            
                                            val games = logsForPlayer.size
                                            val victories = logsForPlayer.count { it.won == true }
                                            val eloForDisplay = logsForPlayer.maxByOrNull { it.id }?.elo ?: (player?.elo ?: 1200.0)
                                            val playedMinutes = filteredMatches.sumOf { match ->
                                                if (playerAppearsInMatch(match, identifier)) matchDurationById[match.id] ?: 0 else 0
                                            }

                                            val effectivePlayer = player ?: Player(name = identifier.name, groupName = "", elo = 1200.0)

                                            HistoryPlayerInfo(
                                                effectivePlayer,
                                                eloForDisplay,
                                                player?.name ?: identifier.name,
                                                games,
                                                victories,
                                                playedMinutes
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
                                            PlayerSortMode.PLAYED_TIME -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.playedMinutes }
                                                    .thenByDescending { it.gamesPlayed }
                                                    .thenByDescending { it.winRate() }
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
                                Icon(Icons.Default.Share, stringResource(R.string.share_history))
                            }
                        }
                    }
                )
            }
        )

        { padding ->
             Box(Modifier
                 .padding(padding)
                 .fillMaxSize()
                 .windowInsetsPadding(safeDrawingNavBarDirection)
             ) {
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
                            selectedGroup = selectedGroup ?: DEFAULT_GROUP_NAME,
                            onSelectedGroupChange = { selectedGroup = it },
                            isDarkTheme = isDarkTheme,
                            showElo = showElo,
                            showToll = showToll,
                            showScore = showScore,
                            isSetupMode = isSetupMode,
                            onSetupModeChange = { isSetupMode = it },
                            onDeleteRequest = { playerToDelete = it },
                            onShowSnackbar = { msg, actionLabel, onAction ->
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = msg,
                                        actionLabel = actionLabel,
                                        withDismissAction = actionLabel != null,
                                        duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onAction?.invoke()
                                    }
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
    val tooltipState = rememberTooltipState(isPersistent = true)

    val itemContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        tooltipState.dismiss()
                        onClick()
                    },
                    onLongClick = {
                        if (!tooltipText.isNullOrBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                tooltipState.show()
                            }
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

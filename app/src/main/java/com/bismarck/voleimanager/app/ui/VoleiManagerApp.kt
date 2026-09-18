package com.bismarck.voleimanager.app.ui

import android.widget.Toast
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.BuildConfig
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.components.*
import com.bismarck.voleimanager.app.ui.game.GameScreenContent
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_EXPORT_FILE_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.MAX_GROUP_NAME_LENGTH
import com.bismarck.voleimanager.app.ui.viewmodel.CsvType
import com.bismarck.voleimanager.app.ui.viewmodel.Screen
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType
import com.bismarck.voleimanager.app.ui.viewmodel.PostProfileOnboardingStage
import com.bismarck.voleimanager.app.util.AppAuthUser
import com.bismarck.voleimanager.app.util.decodeAvatarBase64
import com.bismarck.voleimanager.app.util.loadBitmapForAvatarEditing
import com.bismarck.voleimanager.app.util.compressAvatarBitmap
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_MIN_PLAYERS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun getDisplayGroupName(groupName: String?): String {
    return groupName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.general)
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
private fun userProfileTypeLabel(type: UserProfileType): String = when (type) {
    UserProfileType.ORGANIZADOR -> stringResource(R.string.user_profile_organizer)
    UserProfileType.AUXILIAR -> stringResource(R.string.user_profile_assistant)
    UserProfileType.ESPECTADOR -> stringResource(R.string.user_profile_viewer)
}

/**
 * Deriva o papel do usuário *no grupo ativo* a partir de [GroupConfig.remoteRole]: `null`
 * significa que este dispositivo criou o grupo localmente (Organizador/a dele), enquanto
 * "AUXILIAR"/"ESPECTADOR" refletem o papel obtido ao resgatar um código de convite. Substitui o
 * antigo rótulo estático baseado só na resposta do onboarding (que não mudava por grupo).
 */
private fun activeGroupProfileType(remoteRole: String?): UserProfileType = when (remoteRole) {
    UserProfileType.AUXILIAR.name -> UserProfileType.AUXILIAR
    UserProfileType.ESPECTADOR.name -> UserProfileType.ESPECTADOR
    else -> UserProfileType.ORGANIZADOR
}

/**
 * Cabeçalho do menu lateral: substitui o nome estático do app por um avatar (foto de perfil
 * quando logado e definida, placeholder caso contrário), o apelido público do usuário logado (ou
 * o nome do app, se deslogado), o papel deste dispositivo *no grupo ativo* (Organizador(a) se
 * criado localmente, Auxiliar/Espectador(a) se obtido por código de convite — ver
 * [activeGroupProfileType] —, com sufixo "Premium" para um(a) Espectador(a) premium) e um selo ao
 * lado do nome quando é assinante. Tocar no avatar abre um menu de login/cadastro (deslogado) ou
 * logout + edição de conta/foto (logado).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerAccountHeader(
    currentUser: AppAuthUser?,
    userProfileType: UserProfileType?,
    hasPremiumAccess: Boolean,
    menuExpanded: Boolean,
    onAvatarClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditPhotoClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onResendVerificationClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box {
            val avatarBitmap = remember(currentUser?.photoBase64) {
                currentUser?.photoBase64?.let { decodeAvatarBase64(it) }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.account_avatar_content_description),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = stringResource(R.string.account_avatar_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = onDismissMenu,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                offset = DpOffset(x = 0.dp, y = 4.dp)
            ) {
                if (currentUser == null) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.login_title)) }, onClick = onLoginClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.signup_title)) }, onClick = onSignUpClick)
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (currentUser.photoBase64 != null) stringResource(R.string.edit_profile_photo_title)
                                else stringResource(R.string.add_profile_photo_title)
                            )
                        },
                        onClick = onEditPhotoClick
                    )
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit_profile_title)) }, onClick = onEditProfileClick)
                    if (currentUser.email != null && !currentUser.emailVerified) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.resend_verification_email)) },
                            onClick = onResendVerificationClick
                        )
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.logout)) }, onClick = onLogoutClick)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    currentUser?.nickname?.takeIf { it.isNotBlank() }
                        ?: currentUser?.email
                        ?: stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (currentUser != null && hasPremiumAccess) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.premium_icon),
                        contentDescription = stringResource(R.string.premium_subscriber_badge),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(with(LocalDensity.current) { MaterialTheme.typography.headlineMedium.fontSize.toDp() })
                    )
                }
            }
            if (userProfileType != null) {
                val roleLabel = userProfileTypeLabel(userProfileType)
                val statusText = if (userProfileType == UserProfileType.ESPECTADOR && hasPremiumAccess) {
                    stringResource(R.string.drawer_status_premium_suffix, roleLabel)
                } else {
                    roleLabel
                }
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Ícone que representa o modo de balanceamento no cabeçalho, no lugar do nome por extenso. */
private fun balancingModeIconRes(balancingMode: String): Int =
    if (balancingMode == com.bismarck.voleimanager.app.data.model.BalancingMode.REST.name) {
        R.drawable.zzz_rest
    } else {
        R.drawable.arrowsbothsides
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoleiManagerApp(viewModel: VoleiViewModel, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val select_specific_date = stringResource(R.string.select_specific_date)
    val generating_image = stringResource(R.string.generating_image)
    val importing = stringResource(R.string.importing)

    val uiMessage by viewModel.uiMessage.collectAsState()

    val currentScreen by viewModel.currentScreen.collectAsState()
    val isGroupDataLoading by viewModel.isGroupDataLoading.collectAsState()
    val isAwaitingInitialRemoteSync by viewModel.isAwaitingInitialRemoteSync.collectAsState()
    val allPlayers by viewModel.players.collectAsState()
    val showEloPreference by viewModel.showElo.collectAsState()
    val showToll by viewModel.showToll.collectAsState()
    val telemetryEnabled by viewModel.telemetryEnabled.collectAsState()
    val showTelemetryConsentPrompt by viewModel.showTelemetryConsentPrompt.collectAsState()
    val showUserProfileOnboarding by viewModel.showUserProfileOnboarding.collectAsState()
    val postProfileOnboardingStage by viewModel.postProfileOnboardingStage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val hasPremiumAccessGlobal by viewModel.hasPremiumAccess.collectAsState()
    val authInProgress by viewModel.authInProgress.collectAsState()
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val isSpectatorOfCurrentGroup by viewModel.isSpectatorOfCurrentGroup.collectAsState()
    // Espectador nunca vê Elo se o Administrador/Auxiliar desativou "Mostrar Elo aos
    // espectadores" (GroupConfig.showEloToObservers) — mesmo que a preferência local do
    // dispositivo esteja ligada (ver "elo-observer-toggle-rename-hardgate").
    val showElo = if (isSpectatorOfCurrentGroup) {
        showEloPreference && groupConfig.showEloToObservers
    } else {
        showEloPreference
    }
    val showScore = groupConfig.scoreEnabled
    val eloBlockedForSpectatorMessage = stringResource(R.string.elo_blocked_for_spectator_message)
    // Papel deste dispositivo no grupo ativo (não a resposta global do onboarding) — ver
    // activeGroupProfileType. null enquanto nenhum grupo foi criado/carregado ainda.
    val activeGroupRole = if (groupConfig.groupName.isNotBlank()) {
        activeGroupProfileType(groupConfig.remoteRole)
    } else {
        null
    }
    val groupsSortedByRecent by viewModel.groupsSortedByRecentHistory.collectAsState()
    val allGroupConfigsList by viewModel.allGroupConfigs.collectAsState()
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    var isSetupMode by rememberSaveable { mutableStateOf(false) }
    var historySelectedTab by rememberSaveable { mutableStateOf(0) }
    var historyPlayerSortMode by rememberSaveable { mutableStateOf(PlayerSortMode.ALPHABETICAL) }
    var historyMatchSortMode by rememberSaveable { mutableStateOf(MatchSortMode.NEWEST) }

    // Incremented on every double-tap on the app header while on the game screen, so
    // GameScreenContent can react by recentering (portrait) or scrolling to top (landscape).
    var headerDoubleTapTick by remember { mutableIntStateOf(0) }
    val headerTooltipTeamA by viewModel.teamA.collectAsState()
    val headerTooltipTeamB by viewModel.teamB.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showRenameGroupDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteGroupDialog by remember { mutableStateOf<String?>(null) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTelemetryConsentDialog by remember { mutableStateOf(false) }
    var showRateAppDialog by remember { mutableStateOf(false) }
    var showSendQuestionDialog by remember { mutableStateOf(false) }
    var showJoinGroupDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var showEditProfilePhotoDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }

    var pendingGroupSwitch by remember { mutableStateOf<String?>(null) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsOfUseDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("volei_data") }
    var pendingImportType by remember { mutableStateOf(CsvType.JOGADORES) }
    var pendingDrawerCloseScreen by remember { mutableStateOf<Screen?>(null) }
    var showExportCsvAdvanced by remember { mutableStateOf(false) }
    var showImportCsvAdvanced by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    val launcherImport =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                viewModel.importData(it, pendingImportType, context)
                Toast.makeText(context, importing, Toast.LENGTH_SHORT).show()
            }
        }
    var pendingAvatarCropBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val avatarPhotoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { loadBitmapForAvatarEditing(context, uri) }
                if (bitmap != null) {
                    pendingAvatarCropBitmap = bitmap
                }
            }
        }
    }
    val csvImportMimeTypes = arrayOf(
        "text/*",
        "text/csv",
        "application/csv",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-excel" // .xls (legacy binary format; parsing not supported, shows a clear error)
    )

    // Recarrega o usuário logado ao voltar ao primeiro plano (ex.: após confirmar o e-mail pelo
    // link recebido, num navegador ou app de e-mail à parte), fazendo o botão "Reenviar e-mail de
    // confirmação" sumir automaticamente assim que a confirmação for detectada.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCurrentUser()
                viewModel.refreshPurchases()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(isSpectatorOfCurrentGroup) {
        if (isSpectatorOfCurrentGroup) {
            showAddPlayerDialog = false
            showExportDialog = false
            showImportDialog = false
            showConfigDialog = false
        }
    }

    LaunchedEffect(groupsSortedByRecent, groupConfig.groupName, isGroupDataLoading, selectedGroup) {
        if (isGroupDataLoading) return@LaunchedEffect
        val validGroups = (groupsSortedByRecent + groupConfig.groupName).toSet()
        if (selectedGroup != null && !validGroups.contains(selectedGroup)) {
            selectedGroup = null
        }
        if (selectedGroup == null) {
            selectedGroup = groupsSortedByRecent.firstOrNull() ?: groupConfig.groupName
        }
    }
    // Mantém a seleção da UI em sincronia sempre que o grupo ativo do ViewModel muda por conta
    // própria (ex.: troca automática após importar um backup), sem esperar o usuário escolher
    // manualmente no menu de grupos.
    LaunchedEffect(groupConfig.groupName, isGroupDataLoading) {
        if (isGroupDataLoading) return@LaunchedEffect
        if (selectedGroup != groupConfig.groupName) {
            selectedGroup = groupConfig.groupName
        }
    }
    LaunchedEffect(selectedGroup, groupConfig.onboardingStep, isGroupDataLoading) {
        if (isGroupDataLoading) return@LaunchedEffect
        val targetGroup = selectedGroup ?: return@LaunchedEffect
        val isOnboardingInProgress = groupConfig.onboardingStep < ONBOARDING_STEP_COMPLETE
        if (targetGroup != groupConfig.groupName && !isOnboardingInProgress) {
            viewModel.loadGroupConfig(targetGroup)
        }
    }

    val shouldRequestReview by viewModel.shouldRequestReview.collectAsState()
    LaunchedEffect(uiMessage) {
        uiMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearUiMessage()
        }
    }

    LaunchedEffect(shouldRequestReview) {
        if (shouldRequestReview) {
            (context as? android.app.Activity)?.let { activity ->
                com.bismarck.voleimanager.app.util.InAppReviewHelper.requestReview(activity)
            }
            viewModel.onReviewRequestHandled()
        }
    }

    fun requestScreenSwitch(screen: Screen) {
        if (currentScreen == screen) {
            scope.launch { drawerState.close() }
            return
        }
        pendingDrawerCloseScreen = screen
        viewModel.navigateTo(screen)
    }

    LaunchedEffect(currentScreen, pendingDrawerCloseScreen, isGroupDataLoading) {
        val shouldCloseNow = pendingDrawerCloseScreen == currentScreen &&
            currentScreen != Screen.HISTORY &&
            !isGroupDataLoading &&
            drawerState.isOpen
        if (shouldCloseNow) {
            drawerState.close()
            pendingDrawerCloseScreen = null
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

    if (showExportDialog && !isSpectatorOfCurrentGroup) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_data)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = { exportFileName = it.take(MAX_EXPORT_FILE_NAME_LENGTH) },
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
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    val exportAdvancedRotation by animateFloatAsState(
                        targetValue = if (showExportCsvAdvanced) 180f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "ExportAdvancedRotation"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showExportCsvAdvanced = !showExportCsvAdvanced }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.export_csv),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(exportAdvancedRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = showExportCsvAdvanced,
                        enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
                        exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
                    ) {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            onClick = {
                                viewModel.exportData(context, CsvType.JOGADORES, exportFileName)
                                showExportDialog = false
                            }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.players_word),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            onClick = {
                                viewModel.exportData(context, CsvType.HISTORICO, exportFileName)
                                showExportDialog = false
                            }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.history),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            onClick = {
                                viewModel.exportData(context, CsvType.ELO_LOGS, exportFileName)
                                showExportDialog = false
                            }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.daily_elo),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
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

    if (showImportDialog && !isSpectatorOfCurrentGroup) {
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
                            launcherImport.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                            showImportDialog = false
                        }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.restore_backup))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    val importAdvancedRotation by animateFloatAsState(
                        targetValue = if (showImportCsvAdvanced) 180f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "ImportAdvancedRotation"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showImportCsvAdvanced = !showImportCsvAdvanced }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.import_csv),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(importAdvancedRotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = showImportCsvAdvanced,
                        enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
                        exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
                    ) {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                onClick = {
                                pendingImportType = CsvType.JOGADORES
                                launcherImport.launch(csvImportMimeTypes)
                                showImportDialog = false
                            }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = stringResource(R.string.players_word),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.exportPlayersTemplate(context) }) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = stringResource(R.string.download_players_template)
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            onClick = {
                                pendingImportType = CsvType.HISTORICO
                                launcherImport.launch(csvImportMimeTypes)
                                showImportDialog = false
                            }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.history),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            onClick = {
                                pendingImportType = CsvType.ELO_LOGS
                                launcherImport.launch(csvImportMimeTypes)
                                showImportDialog = false
                            }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.daily_elo),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
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

    val pendingMergeImport by viewModel.pendingMergeImport.collectAsState()
    pendingMergeImport?.let { pending ->
        val groupList = pending.overlappingGroups.joinToString(", ")
        val duplicateSummary = if (pending.duplicatePlayerNames.isNotEmpty()) {
            val preview = pending.duplicatePlayerNames.take(8).joinToString(", ")
            "\n\n${stringResource(R.string.import_duplicate_names_detected, preview)}${if (pending.duplicatePlayerNames.size > 8) "..." else ""}"
        } else ""
        val dialogText = if (groupList.isBlank()) {
            "${stringResource(R.string.import_duplicate_name_dialog_text)}${duplicateSummary}"
        } else {
            "${stringResource(R.string.import_merge_text, groupList)}${duplicateSummary}"
        }
        AlertDialog(
            onDismissRequest = { viewModel.cancelMergeImport() },
            title = { Text(stringResource(R.string.import_merge_title)) },
            text = { Text(dialogText) },
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.confirmMergeImport(false) }
                    ) { Text(stringResource(R.string.import_keep_first_and_skip)) }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.confirmMergeImport(true) }
                    ) { Text(stringResource(R.string.import_rename_duplicates)) }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.cancelMergeImport() }
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }

    val pendingExternalImportUri by viewModel.pendingExternalImportUri.collectAsState()
    pendingExternalImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelExternalImport() },
            title = { Text(stringResource(R.string.external_import_title)) },
            text = { Text(stringResource(R.string.external_import_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importData(uri, CsvType.BACKUP_COMPLETO, context)
                    Toast.makeText(context, importing, Toast.LENGTH_SHORT).show()
                    viewModel.cancelExternalImport()
                }) { Text(stringResource(R.string.import_word)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelExternalImport() }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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

    if (showUserProfileOnboarding) {
        UserProfileOnboardingScreen(
            onProfileSelected = { viewModel.setUserProfileType(it) }
        )
        return
    }

    // Diálogos de conta/entrada em grupo — declarados fora do ModalNavigationDrawer (e antes dos
    // "return" abaixo) para que também funcionem nas telas de roteamento por perfil
    // (Organizador/Auxiliar/Espectador), que ainda não chegam ao conteúdo principal do app.
    if (showJoinGroupDialog) JoinExistingGroupDialog(
        onDismiss = { showJoinGroupDialog = false },
        onConfirm = { code, onResult ->
            viewModel.joinGroupWithCode(code) { error ->
                if (error == null) {
                    scope.launch { drawerState.close() }
                    if (postProfileOnboardingStage == PostProfileOnboardingStage.SPECTATOR_JOIN_SUGGESTION) {
                        viewModel.onSpectatorJoinStepDone()
                    }
                }
                onResult(error)
            }
        }
    )
    if (showLoginDialog) LoginDialog(
        inProgress = authInProgress,
        onDismiss = { showLoginDialog = false },
        onConfirm = { email, password, onResult -> viewModel.signInWithEmail(email, password, onResult) },
        onGoogleClick = { onResult -> viewModel.signInWithGoogle(context, onResult) },
        onForgotPasswordClick = { email, onResult -> viewModel.sendPasswordResetEmail(email, onResult) },
        onSwitchToSignUp = { showLoginDialog = false; showSignUpDialog = true }
    )
    if (showSignUpDialog) SignUpDialog(
        inProgress = authInProgress,
        onDismiss = { showSignUpDialog = false },
        onConfirm = { email, password, fullName, nickname, birthDate, onResult ->
            viewModel.signUpWithEmail(email, password, fullName, nickname, birthDate, onResult)
        },
        onGoogleClick = { onResult -> viewModel.signInWithGoogle(context, onResult) },
        onSwitchToLogin = { showSignUpDialog = false; showLoginDialog = true }
    )

    // Roteamento por perfil: avança automaticamente assim que o login/cadastro é concluído,
    // enquanto uma dessas etapas está visível (ver VoleiViewModel.PostProfileOnboardingStage).
    LaunchedEffect(currentUser, postProfileOnboardingStage) {
        if (currentUser != null) {
            when (postProfileOnboardingStage) {
                PostProfileOnboardingStage.AUTH_REQUIRED -> viewModel.onAuthGatePassed()
                PostProfileOnboardingStage.SPECTATOR_AUTH_SUGGESTION -> viewModel.onSpectatorAuthStepDone()
                else -> {}
            }
        }
    }

    if (postProfileOnboardingStage == PostProfileOnboardingStage.AUTH_REQUIRED) {
        BackHandler { viewModel.returnToProfileSelection() }
        MandatoryAccountGateScreen(
            onLoginClick = { showLoginDialog = true },
            onSignUpClick = { showSignUpDialog = true },
            onBackClick = { viewModel.returnToProfileSelection() }
        )
        return
    }
    if (postProfileOnboardingStage == PostProfileOnboardingStage.SPECTATOR_AUTH_SUGGESTION) {
        BackHandler { viewModel.returnToProfileSelection() }
        SpectatorAuthSuggestionScreen(
            onLoginClick = { showLoginDialog = true },
            onSignUpClick = { showSignUpDialog = true },
            onSkip = { viewModel.onSpectatorAuthStepDone() },
            onBackClick = { viewModel.returnToProfileSelection() }
        )
        return
    }
    if (postProfileOnboardingStage == PostProfileOnboardingStage.SPECTATOR_JOIN_SUGGESTION) {
        BackHandler { viewModel.returnToProfileSelection() }
        SpectatorJoinSuggestionScreen(
            onJoinClick = { showJoinGroupDialog = true },
            onSkip = { viewModel.onSpectatorJoinStepDone() },
            onBackClick = { viewModel.returnToProfileSelection() }
        )
        return
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
                            var accountMenuExpanded by remember { mutableStateOf(false) }
                            val verificationEmailSentMessage = stringResource(R.string.verification_email_sent)
                            DrawerAccountHeader(
                                currentUser = currentUser,
                                userProfileType = activeGroupRole,
                                hasPremiumAccess = hasPremiumAccessGlobal,
                                menuExpanded = accountMenuExpanded,
                                onAvatarClick = { accountMenuExpanded = true },
                                onDismissMenu = { accountMenuExpanded = false },
                                onLoginClick = { accountMenuExpanded = false; showLoginDialog = true },
                                onSignUpClick = { accountMenuExpanded = false; showSignUpDialog = true },
                                onLogoutClick = { accountMenuExpanded = false; showLogoutConfirmDialog = true },
                                onEditPhotoClick = { accountMenuExpanded = false; showEditProfilePhotoDialog = true },
                                onEditProfileClick = { accountMenuExpanded = false; showEditProfileDialog = true },
                                onResendVerificationClick = {
                                    accountMenuExpanded = false
                                    viewModel.resendVerificationEmail { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                error ?: verificationEmailSentMessage
                                            )
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.height(16.dp))

                            var groupExpanded by remember { mutableStateOf(false) }
                            var groupAnchorWidth by remember { mutableStateOf(280.dp) }
                            val groupConfiguration = LocalConfiguration.current
                            val groupHeightFraction = if (groupConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                0.27f
                            } else {
                                0.57f
                            }
                            val groupContainerHeightDp = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
                            val groupMaxMenuHeight = groupContainerHeightDp * groupHeightFraction
                            ExposedDropdownMenuBox(
                                expanded = groupExpanded,
                                onExpandedChange = { groupExpanded = !groupExpanded }) {
                                OutlinedTextField(
                                    value = selectedGroup?.let { getDisplayGroupName(it) } ?: stringResource(R.string.select_word),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        val rotation by animateFloatAsState(
                                            targetValue = if (groupExpanded) 180f else 0f,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "GroupMenuRotation"
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
                                        .menuAnchor(
                                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        )
                                        .onGloballyPositioned { coordinates ->
                                            groupAnchorWidth = with(density) { coordinates.size.width.toDp() }
                                        }
                                        .fillMaxWidth(),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(56.dp)
                                )
                                DropdownMenu(
                                    expanded = groupExpanded,
                                    onDismissRequest = { groupExpanded = false },
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    offset = DpOffset(x = 0.dp, y = 4.dp),
                                    modifier = Modifier
                                        .heightIn(max = groupMaxMenuHeight)
                                        .widthIn(min = groupAnchorWidth)
                                ) {
                                    groupsSortedByRecent.forEach { group ->
                                        val isSelected = selectedGroup == group
                                        val remoteRole = allGroupConfigsList.firstOrNull { it.groupName == group }?.remoteRole
                                        val isRemoteGroup = remoteRole != null
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    if (isRemoteGroup) {
                                                        Icon(
                                                            Icons.Filled.Podcasts,
                                                            contentDescription = stringResource(R.string.remote_group_content_description),
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                    }
                                                    Text(
                                                        getDisplayGroupName(group),
                                                        modifier = Modifier.weight(1f),
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (!isRemoteGroup) {
                                                        IconButton(
                                                            onClick = {
                                                                showRenameGroupDialog = group
                                                                groupExpanded = false
                                                            },
                                                            modifier = Modifier.minimumInteractiveComponentSize()
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Edit,
                                                                contentDescription = stringResource(R.string.rename_group),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            if (isRemoteGroup) {
                                                                viewModel.leaveRemoteGroup(group)
                                                                groupExpanded = false
                                                            } else {
                                                                showDeleteGroupDialog = group
                                                                groupExpanded = false
                                                            }
                                                        },
                                                        modifier = Modifier.minimumInteractiveComponentSize()
                                                    ) {
                                                        Icon(
                                                            if (isRemoteGroup) Icons.AutoMirrored.Filled.Logout else Icons.Default.Delete,
                                                            contentDescription = stringResource(
                                                                if (isRemoteGroup) R.string.leave_group else R.string.delete
                                                            ),
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(24.dp)
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
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(R.string.join_existing_group),
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            showJoinGroupDialog = true; groupExpanded = false
                                        })
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.PlayCircle, null) },
                                label = {
                                    Text(
                                        stringResource(
                                            if (isSpectatorOfCurrentGroup) {
                                                R.string.game_word_spectator
                                            } else {
                                                R.string.game_word
                                            }
                                        )
                                    )
                                },
                                selected = currentScreen == Screen.GAME,
                                onClick = { requestScreenSwitch(Screen.GAME) }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.DateRange, null) },
                                label = { Text(stringResource(R.string.history)) },
                                selected = currentScreen == Screen.HISTORY,
                                onClick = { requestScreenSwitch(Screen.HISTORY) }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(painter = painterResource(R.drawable.premium_icon), null) },
                                label = { Text(stringResource(R.string.cloud_sync)) },
                                selected = currentScreen == Screen.CLOUD_SYNC,
                                onClick = { requestScreenSwitch(Screen.CLOUD_SYNC) }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null) },
                                label = { Text(stringResource(R.string.faq)) },
                                selected = currentScreen == Screen.FAQ,
                                onClick = { requestScreenSwitch(Screen.FAQ) }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Info, null) },
                                label = { Text(stringResource(R.string.about_app)) },
                                selected = currentScreen == Screen.ABOUT,
                                onClick = { requestScreenSwitch(Screen.ABOUT) }
                            )

                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(text = stringResource(R.string.settings), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))

                            if (!isSpectatorOfCurrentGroup) {
                                FlexibleDrawerItem(
                                    icon = { Icon(Icons.Outlined.Settings, null) },
                                    label = { Text(stringResource(R.string.group_rules)) },
                                    selected = false,
                                    onClick = {
                                        showConfigDialog = true; scope.launch { drawerState.close() }
                                    }
                                )
                            }
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
                                onClick = {
                                    if (isSpectatorOfCurrentGroup && !showEloPreference && !groupConfig.showEloToObservers) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(eloBlockedForSpectatorMessage)
                                        }
                                    } else {
                                        viewModel.setShowElo(!showElo)
                                    }
                                }
                            )
                            FlexibleDrawerItem(
                                icon = { Icon(painter = painterResource(R.drawable.volei_manager_icon), null) },
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
                            Text(text = stringResource(R.string.data), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))

                            if (!isSpectatorOfCurrentGroup) {
                                FlexibleDrawerItem(
                                    icon = { Icon(Icons.Outlined.FileUpload, null) },
                                    label = { Text(stringResource(R.string.export)) },
                                    selected = false,
                                    onClick = {
                                        showExportCsvAdvanced = false
                                        showExportDialog = true; scope.launch { drawerState.close() }
                                    }
                                )
                                FlexibleDrawerItem(
                                    icon = { Icon(Icons.Outlined.FileDownload, null) },
                                    label = { Text(stringResource(R.string.import_text)) },
                                    selected = false,
                                    onClick = {
                                        showImportCsvAdvanced = false
                                        showImportDialog = true; scope.launch { drawerState.close() }
                                    }
                                )
                            }
                            FlexibleDrawerItem(
                                icon = { Icon(Icons.Outlined.Info, null) },
                                label = { Text(stringResource(R.string.telemetry_consent_menu_item)) },
                                selected = false,
                                badge = { Switch(checked = telemetryEnabled, onCheckedChange = null) },
                                tooltipText = stringResource(R.string.telemetry_consent_menu_tooltip),
                                onClick = {
                                    if (telemetryEnabled) {
                                        viewModel.setTelemetryEnabled(false)
                                    } else {
                                        showTelemetryConsentDialog = true
                                        scope.launch { drawerState.close() }
                                    }
                                }
                            )
                            HorizontalDivider(
                                Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(text = stringResource(R.string.privacy), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))

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
        if (showConfigDialog && !isSpectatorOfCurrentGroup) {
            GroupConfigDialog(
                groupName = selectedGroup ?: groupConfig.groupName,
                initialTeamSize = groupConfig.teamSize,
                initialVictoryLimit = groupConfig.victoryLimit,
                initialPriorityEnabled = groupConfig.priorityEnabled,
                initialScoreEnabled = groupConfig.scoreEnabled,
                initialBalancingMode = groupConfig.balancingMode,
                initialGroupType = groupConfig.groupType,
                initialGuaranteeSetter = groupConfig.guaranteeSetter,
                isGameInProgress = viewModel.isGameInProgress(),
                onDismiss = { showConfigDialog = false },
                onConfirm = { size, limit, prior, scoreEn, balancingMode, groupType, guaranteeSetter ->
                    viewModel.updateConfig(size, limit, prior, scoreEn, balancingMode, groupType, guaranteeSetter)
                    showConfigDialog = false
                }
            )
        }
        if (showCreateGroupDialog) CreateGroupDialog(
            { showCreateGroupDialog = false },
            { newName, groupType ->
                val normalizedGroupName = newName.trim().replace(Regex("\\s+"), " ").take(MAX_GROUP_NAME_LENGTH)
                selectedGroup = normalizedGroupName
                viewModel.createGroup(normalizedGroupName, groupType = groupType)
                showCreateGroupDialog = false
                scope.launch { drawerState.close() }
            })
        if (showEditProfilePhotoDialog) EditProfilePhotoDialog(
            hasPhoto = currentUser?.photoBase64 != null,
            onDismiss = { showEditProfilePhotoDialog = false },
            onPickPhoto = {
                avatarPhotoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = { viewModel.updateProfilePhoto(null) { } }
        )
        pendingAvatarCropBitmap?.let { bitmap ->
            AvatarCropDialog(
                sourceBitmap = bitmap,
                onDismiss = { pendingAvatarCropBitmap = null },
                onConfirm = { cropped ->
                    val base64 = compressAvatarBitmap(cropped)
                    viewModel.updateProfilePhoto(base64) { }
                    pendingAvatarCropBitmap = null
                }
            )
        }
        if (showEditProfileDialog) EditProfileDialog(
            inProgress = authInProgress,
            initialFullName = currentUser?.fullName.orEmpty(),
            initialNickname = currentUser?.nickname.orEmpty(),
            initialBirthDateIso = currentUser?.birthDate,
            hasPasswordProvider = currentUser?.hasPasswordProvider == true,
            onChangeEmailClick = { showEditProfileDialog = false; showChangeEmailDialog = true },
            onChangePasswordClick = { showEditProfileDialog = false; showChangePasswordDialog = true },
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { nickname, fullName, birthDate, onResult ->
                viewModel.updateUserProfile(nickname, fullName, birthDate, onResult)
            },
            onRequestDeleteAccount = { showEditProfileDialog = false; showDeleteAccountConfirmDialog = true }
        )
        if (showLogoutConfirmDialog) LogoutConfirmDialog(
            onDismiss = { showLogoutConfirmDialog = false },
            onConfirm = { showLogoutConfirmDialog = false; viewModel.signOut() }
        )
        if (showChangeEmailDialog) ChangeEmailDialog(
            inProgress = authInProgress,
            onDismiss = { showChangeEmailDialog = false },
            onConfirm = { currentPassword, newEmail, onResult ->
                viewModel.changeEmail(currentPassword, newEmail, onResult)
            }
        )
        if (showChangePasswordDialog) ChangePasswordDialog(
            inProgress = authInProgress,
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { currentPassword, newPassword, onResult ->
                viewModel.changePassword(currentPassword, newPassword, onResult)
            }
        )
        if (showDeleteAccountConfirmDialog) DeleteAccountConfirmDialog(
            inProgress = authInProgress,
            onDismiss = { showDeleteAccountConfirmDialog = false },
            onConfirm = {
                viewModel.deleteAccount { error ->
                    showDeleteAccountConfirmDialog = false
                    if (error != null) {
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
        if (showAddPlayerDialog && !isSpectatorOfCurrentGroup) AddPlayerDialog(
            usesPositions = groupConfig.type.usesPositions,
            onDismiss = { showAddPlayerDialog = false },
            onConfirm = { name, elo, isPriority, preferred, secondary ->
                viewModel.addPlayer(
                    name,
                    elo,
                    selectedGroup ?: groupConfig.groupName,
                    isPriority,
                    preferred,
                    secondary
                )
                showAddPlayerDialog = false
            })
        if (showThemeDialog) {
            val mode by viewModel.themeMode.collectAsState()
            val groupTeamAColor by viewModel.groupTeamAColor.collectAsState()
            val groupTeamBColor by viewModel.groupTeamBColor.collectAsState()
            val personalOverrideEnabled by viewModel.personalTeamColorOverrideEnabled.collectAsState()
            val personalTeamAColor by viewModel.personalTeamAColor.collectAsState()
            val personalTeamBColor by viewModel.personalTeamBColor.collectAsState()
            val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
            val debugPremiumOverride by viewModel.debugPremiumOverride.collectAsState()
            val teamColorsLockedMessage = stringResource(R.string.team_colors_locked_hint)
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

                        // Cores oficiais do grupo atual: valem para todo mundo que o visualiza
                        // (inclusive observadores sem premium), definidas apenas pelo organizador
                        // — Espectador (mesmo premium) nunca edita as cores oficiais do grupo, só
                        // a sobreposição pessoal abaixo (ver [VoleiViewModel.setGroupTeamColors]).
                        if (!isSpectatorOfCurrentGroup) {
                            HorizontalDivider(
                                Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            TeamColorPickerSection(
                                title = stringResource(R.string.team_colors_group_title),
                                hint = if (hasPremiumAccess) {
                                    stringResource(R.string.team_colors_group_hint)
                                } else {
                                    stringResource(R.string.team_colors_locked_hint)
                                },
                                teamAColor = groupTeamAColor,
                                teamBColor = groupTeamBColor,
                                hasPremiumAccess = hasPremiumAccess,
                                isDarkTheme = isDarkTheme,
                                onColorsSelected = { a, b -> viewModel.setGroupTeamColors(a, b) },
                                onLockedClick = {
                                    viewModel.showMessage(teamColorsLockedMessage)
                                }
                            )
                        }

                        HorizontalDivider(
                            Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        // Sobreposição só para este usuário/dispositivo, sem alterar o que os
                        // demais membros do grupo enxergam — só disponível para quem é premium.
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.team_colors_personal_override_title),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    stringResource(R.string.team_colors_personal_override_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = personalOverrideEnabled,
                                enabled = hasPremiumAccess,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        viewModel.setPersonalTeamColorOverride(
                                            personalTeamAColor,
                                            personalTeamBColor
                                        )
                                    } else {
                                        viewModel.clearPersonalTeamColorOverride()
                                    }
                                }
                            )
                        }
                        if (personalOverrideEnabled) {
                            Spacer(Modifier.height(8.dp))
                            TeamColorPickerSection(
                                title = null,
                                hint = null,
                                teamAColor = personalTeamAColor,
                                teamBColor = personalTeamBColor,
                                hasPremiumAccess = hasPremiumAccess,
                                isDarkTheme = isDarkTheme,
                                onColorsSelected = { a, b ->
                                    viewModel.setPersonalTeamColorOverride(a, b)
                                },
                                onLockedClick = {
                                    viewModel.showMessage(teamColorsLockedMessage)
                                }
                            )
                        }

                        if (BuildConfig.DEBUG) {
                            HorizontalDivider(
                                Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.debug_simulate_premium),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        stringResource(R.string.debug_simulate_premium_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = debugPremiumOverride,
                                    onCheckedChange = { viewModel.setDebugPremiumOverride(it) }
                                )
                            }
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
        if (showTelemetryConsentDialog || showTelemetryConsentPrompt) {
            TelemetryConsentDialog(
                onAllow = {
                    viewModel.setTelemetryEnabled(true)
                    showTelemetryConsentDialog = false
                },
                onDeny = {
                    viewModel.setTelemetryEnabled(false)
                    showTelemetryConsentDialog = false
                }
            )
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
                            viewModel.deleteGroup(group); selectedGroup = null
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

        if (showRateAppDialog) {
            AlertDialog(
                onDismissRequest = { showRateAppDialog = false },
                icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                title = { Text(stringResource(R.string.rate_app_dialog_title)) },
                text = { Text(stringResource(R.string.rate_app_dialog_text)) },
                confirmButton = {
                    Button(onClick = {
                        showRateAppDialog = false
                        val packageName = context.packageName
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
                            )
                        } catch (_: ActivityNotFoundException) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "https://play.google.com/store/apps/details?id=$packageName".toUri()
                                )
                            )
                        }
                    }) { Text(stringResource(R.string.rate_app_dialog_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showRateAppDialog = false }) {
                        Text(
                            stringResource(R.string.rate_app_dialog_dismiss),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                })
        }

        if (showSendQuestionDialog) {
            AlertDialog(
                onDismissRequest = { showSendQuestionDialog = false },
                icon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null) },
                title = { Text(stringResource(R.string.send_question_dialog_title)) },
                text = { Text(stringResource(R.string.send_question_dialog_text)) },
                confirmButton = {
                    Button(onClick = {
                        showSendQuestionDialog = false
                        val feedbackFormUrl = context.getString(R.string.feedback_form_url)
                        val intent = Intent(Intent.ACTION_VIEW, feedbackFormUrl.toUri())
                        context.startActivity(intent)
                    }) { Text(stringResource(R.string.send_question_dialog_confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showSendQuestionDialog = false }) {
                        Text(
                            stringResource(R.string.rate_app_dialog_dismiss),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                })
        }

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                val headerTooltipState = rememberTooltipState(isPersistent = true)
                val headerInfoTooltipState = rememberTooltipState(isPersistent = true)
                val headerTooltipScope = rememberCoroutineScope()
                val headerTooltipGameInProgress = headerTooltipTeamA.isNotEmpty() || headerTooltipTeamB.isNotEmpty()
                LaunchedEffect(currentScreen, groupConfig.groupName, headerTooltipGameInProgress) {
                    if (currentScreen == Screen.GAME &&
                        groupConfig.groupName.isNotBlank() &&
                        headerTooltipGameInProgress &&
                        !viewModel.hasSeenHeaderScrollTooltip(groupConfig.groupName)
                    ) {
                        viewModel.markHeaderScrollTooltipSeen(groupConfig.groupName)
                        delay(600)
                        headerTooltipState.show()
                        delay(4_000)
                        headerTooltipState.dismiss()
                    }
                }
                FlexibleTopAppBar(
                    onTap = {
                        headerTooltipScope.launch {
                            headerTooltipState.dismiss()
                            headerInfoTooltipState.dismiss()
                        }
                    },
                    onDoubleTap = if (currentScreen == Screen.GAME) {
                        { headerDoubleTapTick++ }
                    } else {
                        null
                    },
                    onLongPress = if (selectedGroup != null) {
                        {
                            headerTooltipScope.launch {
                                headerTooltipState.dismiss()
                                headerInfoTooltipState.show()
                                delay(4_000)
                                headerInfoTooltipState.dismiss()
                            }
                        }
                    } else {
                        null
                    },
                    title = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        stringResource(R.string.header_scroll_tooltip),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            state = headerTooltipState,
                            enableUserInput = false
                        ) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                                tooltip = {
                                    PlainTooltip {
                                        val groupType = groupConfig.type
                                        val infoText = if (groupType.supportsBalancingMode) {
                                            stringResource(
                                                R.string.header_info_tooltip,
                                                getDisplayGroupName(groupConfig.groupName),
                                                groupTypeLabel(groupType),
                                                getDisplayBalancingModeName(groupConfig.balancingMode),
                                                "${groupConfig.teamSize}x${groupConfig.teamSize}"
                                            )
                                        } else {
                                            stringResource(
                                                R.string.header_info_tooltip_no_mode,
                                                getDisplayGroupName(groupConfig.groupName),
                                                groupTypeLabel(groupType),
                                                "${groupConfig.teamSize}x${groupConfig.teamSize}"
                                            )
                                        }
                                        Text(infoText, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                state = headerInfoTooltipState,
                                enableUserInput = false
                            ) {
                                Column {
                                    Text(stringResource(R.string.app_name))
                                    selectedGroup?.let {
                                        val groupType = groupConfig.type
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                getDisplayGroupName(groupConfig.groupName),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                imageVector = groupTypeIcon(groupType),
                                                contentDescription = groupTypeLabel(groupType),
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (groupType.supportsBalancingMode) {
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    painter = painterResource(balancingModeIconRes(groupConfig.balancingMode)),
                                                    contentDescription = getDisplayBalancingModeName(groupConfig.balancingMode),
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "${groupConfig.teamSize}x${groupConfig.teamSize}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(
                                Icons.Default.Menu,
                                stringResource(R.string.side_menu),
                                modifier = Modifier.size(24.dp))
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.GAME) {
                            val groupConfig by viewModel.currentGroupConfig.collectAsState()
                            val groupPlayers by viewModel.currentGroupPlayers.collectAsState()
                            val minimumPlayersNeeded = groupConfig.teamSize * 2
                            val showAddPulse = groupConfig.onboardingStep == ONBOARDING_STEP_MIN_PLAYERS &&
                                groupPlayers.size < minimumPlayersNeeded
                            
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
                            val iconRotation by animateFloatAsState(
                                targetValue = if (showAddPulse) 90f else 0f,
                                animationSpec = if (showAddPulse)
                                    infiniteRepeatable(
                                        animation = tween(2000),
                                        repeatMode = RepeatMode.Restart
                                    )
                                else tween(200),
                                label = "AddButtonRotation"
                            )
                            
                            if (!isSpectatorOfCurrentGroup) {
                                IconButton(
                                    onClick = { showAddPlayerDialog = true },
                                    modifier = Modifier
                                        .scale(scale)
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        stringResource(R.string.add_new_player),
                                        tint = iconColor,
                                        modifier = Modifier.rotate(iconRotation).size(24.dp)
                                    )
                                }
                            }
                        } else if (currentScreen == Screen.HISTORY) {
                            val view = LocalView.current
                            val historyDate by viewModel.historyDateFilter.collectAsState()
                            val isRemoteHistoryGroup = groupConfig.remoteRole != null
                            val localGroupHistoryForExport by viewModel.currentGroupHistory.collectAsState()
                            val localGroupPlayersForExport by viewModel.currentGroupPlayers.collectAsState()
                            val localEloLogsForExport by viewModel.currentGroupEloLogs.collectAsState()
                            val remoteHistoryForExport by viewModel.remoteHistory.collectAsState()
                            val remoteEloLogsForExport by viewModel.remoteEloLogs.collectAsState()
                            // Grupo remoto (Auxiliar/Espectador): reaproveita os mesmos dados
                            // espelhados do Firestore que alimentam a tela de Histórico (ver
                            // HistoryScreen.toMatchHistory/toPlayerEloLogs em AppScreens.kt), já
                            // filtrados por visibilidade (remoteHistory/remoteEloLogs).
                            val groupHistory = if (isRemoteHistoryGroup) {
                                remoteHistoryForExport.map { it.toMatchHistory(groupConfig.groupName) }
                            } else {
                                localGroupHistoryForExport
                            }
                            val groupPlayers = if (isRemoteHistoryGroup) emptyList() else localGroupPlayersForExport
                            val eloLogs = if (isRemoteHistoryGroup) remoteEloLogsForExport.toPlayerEloLogs(groupConfig.groupName) else localEloLogsForExport

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
                                            "${mdm.values.average().toInt()}min"
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
                                                    .thenByDescending { it.gamesPlayed }
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
                                                    .thenByDescending { it.gamesPlayed }
                                                    .thenByDescending { it.displayElo }
                                            )
                                            PlayerSortMode.PLAYED_TIME -> playerDataList.sortedWith(
                                                compareByDescending<HistoryPlayerInfo> { it.playedMinutes }
                                                    .thenByDescending { it.winRate() }
                                                    .thenByDescending { it.gamesPlayed }
                                                    .thenByDescending { it.displayElo }
                                            )
                                            PlayerSortMode.ALPHABETICAL -> playerDataList.sortedWith(
                                                compareBy<HistoryPlayerInfo> { it.player.name.lowercase() }
                                                    .thenByDescending { it.winRate() }
                                                    .thenByDescending { it.gamesPlayed }
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
                            }, modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    stringResource(R.string.share_history),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (currentScreen == Screen.ABOUT) {
                            IconButton(
                                onClick = { showRateAppDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.Default.Storefront,
                                    stringResource(R.string.rate_app),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (currentScreen == Screen.FAQ) {
                            IconButton(
                                onClick = { showSendQuestionDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.HelpOutline,
                                    stringResource(R.string.send_question),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else if (currentScreen == Screen.CLOUD_SYNC) {
                            IconButton(
                                onClick = { showJoinGroupDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    Icons.Default.Groups,
                                    stringResource(R.string.join_existing_group),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
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
             ) {
                 if (isGroupDataLoading || isAwaitingInitialRemoteSync) {
                     Box(
                         modifier = Modifier.fillMaxSize(),
                         contentAlignment = Alignment.Center
                     ) {
                         CircularProgressIndicator()
                     }
                 } else {
                     AnimatedContent(
                         targetState = currentScreen,
                         transitionSpec = {
                             EnterTransition.None togetherWith ExitTransition.None
                         },
                         label = "ScreenAnim"
                     ) { screen ->
                        when (screen) {
                            Screen.GAME -> GameScreenContent(
                                viewModel = viewModel,
                                selectedGroup = selectedGroup ?: groupConfig.groupName,
                                onSelectedGroupChange = { selectedGroup = it },
                                isDarkTheme = isDarkTheme,
                                showElo = showElo,
                                showToll = showToll,
                                showScore = showScore,
                                isSetupMode = isSetupMode,
                                onSetupModeChange = { isSetupMode = it },
                                onDeleteRequest = { playerToDelete = it },
                                headerDoubleTapTick = headerDoubleTapTick,
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
                                onPlayerSortModeChanged = { historyPlayerSortMode = it },
                                onContentReady = {
                                    if (pendingDrawerCloseScreen == Screen.HISTORY && drawerState.isOpen) {
                                        scope.launch {
                                            drawerState.close()
                                            pendingDrawerCloseScreen = null
                                        }
                                    }
                                }
                            )
                            Screen.FAQ -> FAQScreen(
                                viewModel = viewModel,
                                onSendQuestionClick = { showSendQuestionDialog = true }
                            )
                            Screen.ABOUT -> AboutScreen()
                            Screen.CLOUD_SYNC -> CloudSyncScreen(viewModel = viewModel)
                        }
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
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
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
    actions: @Composable RowScope.() -> Unit = {},
    onTap: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                .padding(vertical = 8.dp)
                .heightIn(min = 64.dp)
                .then(
                    if (onTap != null || onDoubleTap != null || onLongPress != null) {
                        Modifier.pointerInput(onTap, onDoubleTap, onLongPress) {
                            detectTapGestures(
                                onTap = { onTap?.invoke() },
                                onDoubleTap = { onDoubleTap?.invoke() },
                                onLongPress = { onLongPress?.invoke() }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
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

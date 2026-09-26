package com.bismarck.voleimanager.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.DpOffset
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.ui.viewmodel.CloudPlanTier
import com.bismarck.voleimanager.app.ui.viewmodel.PremiumScreenPersona
import com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.BillingProductIds
import com.bismarck.voleimanager.app.util.SubscriptionOffer
import com.bismarck.voleimanager.app.util.JoinRole
import com.bismarck.voleimanager.app.util.LiveGameState
import com.bismarck.voleimanager.app.util.QrCodeGenerator
import com.bismarck.voleimanager.app.util.RegeneratedSpectatorCode
import com.bismarck.voleimanager.app.util.RemoteEloLogEntry
import com.bismarck.voleimanager.app.util.RemoteHistoryEntry
import com.bismarck.voleimanager.app.util.RemotePlayerSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import java.text.NumberFormat
import java.util.Currency

/**
 * Tela "Premium": ponto único de sincronização em nuvem premium. O conteúdo é dividido por
 * perfil do usuário ([UserProfileType]):
 * - Sem perfil definido (usuário pulou/nunca respondeu o onboarding de perfil): mostra as três
 *   opções para escolher agora (mesmo componente do onboarding).
 * - Organizador(a)/Auxiliar: entrada de código de convite em destaque, oferta de assinatura,
 *   gestão de conta e lista de todos os grupos com o papel do usuário em cada um (dono,
 *   auxiliar ou espectador) — se premium, também o painel de gerenciamento (grupos
 *   sincronizados, geração de códigos, visibilidade para espectadores, transferência de posse).
 * - Espectador(a): visão ao vivo (placar, times, fila) do grupo que ele entrou via código, mais
 *   histórico/ranking de Elo quando o organizador/auxiliar habilitar os toggles de visibilidade
 *   (ver [CloudSyncManager][com.bismarck.voleimanager.app.util.CloudSyncManager]).
 *
 * Cadastro/login real (Firebase Auth), a sincronização em tempo real (Firestore) e a compra real
 * de assinatura (Google Play Billing, ver [com.bismarck.voleimanager.app.util.BillingManager]) já
 * funcionam; enquanto os planos não existirem no Play Console (ou em builds de debug sem uma
 * compra real), [VoleiViewModel.debugPremiumOverride]/[VoleiViewModel.debugPremiumPlanTier]
 * seguem disponíveis como simulação apenas de desenvolvimento.
 */
@Composable
fun CloudSyncScreen(viewModel: VoleiViewModel) {
    val userProfileType by viewModel.userProfileType.collectAsState()

    if (userProfileType == null) {
        UserProfileOnboardingScreen(onProfileSelected = { viewModel.setUserProfileType(it) })
        return
    }

    val persona by viewModel.premiumScreenPersona.collectAsState()

    // O segmented button rola junto com o conteúdo (é o primeiro item de cada Column com
    // verticalScroll abaixo) em vez de ficar fixo no topo — ver `premium-persona-toggle-scrolls`.
    when (persona) {
        PremiumScreenPersona.ESPECTADOR -> SpectatorLiveScreen(viewModel, persona)
        PremiumScreenPersona.ADMIN -> OrganizerAssistantCloudScreen(viewModel, persona)
    }
}

/**
 * Quem administra um grupo pode também ser Auxiliar/Espectador de grupos de outras pessoas (e
 * vice-versa) — ver [PremiumScreenPersona]. As duas versões da tela ficam sempre disponíveis,
 * iniciando na que corresponde à resposta do onboarding, mas o usuário pode trocar livremente
 * depois; a escolha é persistida (ver [VoleiViewModel.setPremiumScreenPersona]).
 */
@Composable
private fun PersonaSegmentedButtonRow(viewModel: VoleiViewModel, persona: PremiumScreenPersona) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = persona == PremiumScreenPersona.ADMIN,
            onClick = { viewModel.setPremiumScreenPersona(PremiumScreenPersona.ADMIN) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {}
        ) {
            Text(stringResource(R.string.premium_screen_persona_admin))
        }
        SegmentedButton(
            selected = persona == PremiumScreenPersona.ESPECTADOR,
            onClick = { viewModel.setPremiumScreenPersona(PremiumScreenPersona.ESPECTADOR) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {}
        ) {
            Text(stringResource(R.string.premium_screen_persona_espectador))
        }
    }
}

@Composable
private fun SpectatorLiveScreen(viewModel: VoleiViewModel, persona: PremiumScreenPersona) {
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    // Grupos onde o usuário é Espectador, ainda que ele também seja Administrador de outro grupo
    // (essa outra parte fica só na versão Admin da tela — ver [OrganizerAssistantCloudScreen]).
    val espectadorGroups = remember(allGroups) {
        allGroups.filter { it.remoteRole == UserProfileType.ESPECTADOR.name }.sortedBy { it.groupName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PersonaSegmentedButtonRow(viewModel, persona)
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.live_screen_spectator_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (espectadorGroups.isEmpty()) {
                Text(
                    stringResource(R.string.live_screen_spectator_no_group),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    pluralStringResource(
                        R.plurals.live_screen_spectator_groups_count,
                        espectadorGroups.size,
                        espectadorGroups.size
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                espectadorGroups.forEach { group ->
                    Text(group.groupName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // O placar, a fila de espera e o histórico de partidas já aparecem com muito mais detalhe
        // e qualidade nas telas "Jogo (Ao vivo)" e "Histórico" (que reaproveitam a mesma base
        // visual do organizador — ver `spectator-reuse-game-screen`/`spectator-reuse-history-screen`).
        // Esta tela não repete essas visualizações simplificadas; foca só no apoio ao projeto.
        // Quem já é assinante não precisa ver o pedido de apoio de novo — a seção some por
        // completo (o gerenciamento/cancelamento da assinatura continua acessível na versão Admin
        // da tela, que sempre mostra PremiumPlansSection independente de grupos próprios).
        if (!hasPremiumAccess) {
            PremiumPlansSection(
                viewModel = viewModel,
                title = stringResource(R.string.cloud_sync_spectator_support_title),
                description = stringResource(R.string.cloud_sync_spectator_support_description),
                collapsible = false
            )
        }
    }
}

@Composable
internal fun LiveScoreboard(state: LiveGameState, swapSides: Boolean = false) {
    val (leftTeam, rightTeam) = if (swapSides) state.teamB to state.teamA else state.teamA to state.teamB
    val (leftScore, rightScore) = if (swapSides) state.scoreB to state.scoreA else state.scoreA to state.scoreB
    val leftLabelRes = if (swapSides) R.string.live_screen_team_b_label else R.string.live_screen_team_a_label
    val rightLabelRes = if (swapSides) R.string.live_screen_team_a_label else R.string.live_screen_team_b_label
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(
            "$leftScore x $rightScore",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(leftLabelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            leftTeam.forEach { player -> Text(player.name, style = MaterialTheme.typography.bodyMedium) }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(rightLabelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            rightTeam.forEach { player -> Text(player.name, style = MaterialTheme.typography.bodyMedium) }
        }
    }
    HorizontalDivider(
        Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    )
    Text(
        stringResource(R.string.live_screen_waiting_list_label),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
    if (state.waitingList.isEmpty()) {
        Text(
            stringResource(R.string.live_screen_waiting_list_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        state.waitingList.forEach { player: RemotePlayerSnapshot ->
            Text(player.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun RemoteHistoryRow(entry: RemoteHistoryEntry) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(entry.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${entry.teamA}  ${entry.teamAScore ?: "-"} x ${entry.teamBScore ?: "-"}  ${entry.teamB}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun RemoteEloRow(entry: RemoteEloLogEntry) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(entry.playerNameSnapshot, style = MaterialTheme.typography.bodyMedium)
        Text("%.0f".format(entry.elo), style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun OrganizerAssistantCloudScreen(viewModel: VoleiViewModel, persona: PremiumScreenPersona) {
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val syncedGroupNames by viewModel.cloudSyncedGroupNames.collectAsState()
    val activeGroupName = viewModel.currentGroupConfig.collectAsState().value.groupName

    // Transferência de posse do grupo temporariamente oculta (ver `hide-ownership-transfer-temporarily`)
    // — a funcionalidade em si (VoleiViewModel.requestGroupOwnershipTransfer/cancelGroupOwnershipTransfer)
    // continua implementada; só a UI de acesso fica escondida por enquanto para revisitar depois.

    // Geração manual de código (AUXILIAR/ESPECTADOR de 30 min) fica de fora desta tela desde
    // `spectator-code-client`: o código de Espectador agora é permanente e gerado automaticamente
    // na primeira ativação da sincronização (ver [SpectatorCodeSection] abaixo); o papel Auxiliar
    // continua oculto (`hide-auxiliar-role-temporarily`). [GenerateJoinCodeDialog] segue definido
    // em Dialogs.kt, sem uso, para quando o Auxiliar for reativado.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PersonaSegmentedButtonRow(viewModel, persona)

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.premium_icon3),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.cloud_sync_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                stringResource(R.string.cloud_sync_screen_intro),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Entrar em um grupo (código de Auxiliar/Espectador) e gestão de Conta (login/logout) já
        // ficam disponíveis em outros pontos do app (menu do avatar/gaveta de navegação — ver
        // [VoleiManagerApp]), então não são repetidos nesta tela para evitar redundância.

        // ========== GRUPOS SINCRONIZADOS ==========
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_groups_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (hasPremiumAccess) {
                    pluralStringResource(
                        R.plurals.cloud_sync_groups_limit_label,
                        effectivePlanTier.maxSyncedGroups,
                        syncedGroupNames.size,
                        effectivePlanTier.maxSyncedGroups
                    )
                } else {
                    stringResource(R.string.cloud_sync_groups_locked_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // Só os grupos criados/administrados pelo próprio usuário (remoteRole == null)
            // aparecem aqui — grupos onde ele é Espectador ficam na seção seguinte. Um dropdown
            // mostra um grupo por vez (em vez de listar todos empilhados), ordenado com os já
            // sincronizados primeiro e, dentro de cada grupo, em ordem alfabética.
            val ownGroups = allGroups.filter { it.remoteRole == null }
            if (ownGroups.isEmpty()) {
                Text(
                    stringResource(R.string.cloud_sync_groups_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val sortedGroups = remember(ownGroups) {
                    ownGroups.sortedWith(
                        compareByDescending<GroupConfig> { it.isCloudSynced }.thenBy { it.groupName }
                    )
                }
                // Padrão: o grupo já selecionado na gaveta de navegação, quando ele estiver entre
                // os grupos administrados por este usuário — evita que o organizador precise
                // reselecionar manualmente o grupo que já estava usando ao entrar nesta tela.
                var selectedGroupName by rememberSaveable { mutableStateOf<String?>(null) }
                val selectedGroup = sortedGroups.firstOrNull { it.groupName == selectedGroupName }
                    ?: sortedGroups.firstOrNull { it.groupName == activeGroupName }
                    ?: sortedGroups.first()
                LaunchedEffect(selectedGroup.groupName) {
                    selectedGroupName = selectedGroup.groupName
                }

                var expanded by remember { mutableStateOf(false) }
                var groupAnchorWidth by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedGroup.groupName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.cloud_sync_groups_selector_label)) },
                        trailingIcon = {
                            val rotation by animateFloatAsState(
                                targetValue = if (expanded) 180f else 0f,
                                animationSpec = tween(durationMillis = 200),
                                label = "CloudSyncGroupMenuRotation"
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
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .onGloballyPositioned { coordinates ->
                                groupAnchorWidth = with(density) { coordinates.size.width.toDp() }
                            }
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(56.dp)
                    )
                    // DropdownMenu comum em vez de ExposedDropdownMenu: só o primeiro aceita o
                    // parâmetro `offset`, necessário para manter o mesmo afastamento de 4dp do
                    // padrão do app (ver o seletor de grupo da gaveta de navegação em
                    // VoleiManagerApp.kt). A largura do popup é travada na largura do campo âncora
                    // via `onGloballyPositioned` acima, para não perder o alinhamento.
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        offset = DpOffset(x = 0.dp, y = 4.dp),
                        modifier = Modifier.widthIn(min = groupAnchorWidth)
                    ) {
                        sortedGroups.forEach { group ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(group.groupName, modifier = Modifier.weight(1f))
                                        if (group.isCloudSynced) {
                                            Icon(
                                                Icons.Outlined.CloudDone,
                                                contentDescription = stringResource(R.string.cloud_sync_groups_synced_badge),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedGroupName = group.groupName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    CloudSyncToggleRow(
                        label = stringResource(R.string.cloud_sync_groups_activate_label),
                        tooltip = stringResource(R.string.cloud_sync_groups_activate_tooltip),
                        checked = selectedGroup.isCloudSynced,
                        enabled = hasPremiumAccess,
                        onCheckedChange = { checked ->
                            viewModel.setGroupCloudSynced(selectedGroup.groupName, checked)
                        },
                        colors = SwitchDefaults.colors(
                            disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            disabledCheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                    if (selectedGroup.isCloudSynced) {
                        val cloudGroupId = selectedGroup.cloudGroupId
                        // `spectator-code-listener-refresh`: às vezes o listener do Firestore fica
                        // "parado" sem receber a atualização quando o código permanente é gerado
                        // pelo backend logo após a ativação (observado em teste real: o código só
                        // aparecia depois de trocar de grupo no seletor e voltar, o que forçava
                        // este bloco a sair e voltar à composição, recriando a assinatura do zero).
                        // Em vez de depender do usuário perceber isso, recria a assinatura sozinho
                        // a cada poucos segundos enquanto o código ainda não chegou.
                        var listenerRefreshTick by remember(cloudGroupId) { mutableStateOf(0) }
                        val cloudMeta by remember(cloudGroupId, listenerRefreshTick) {
                            if (cloudGroupId == null) flowOf(null) else viewModel.observeGroupCloudMeta(cloudGroupId)
                        }.collectAsState(initial = null)
                        val viewerCount by remember(cloudGroupId) {
                            if (cloudGroupId == null) flowOf(0) else viewModel.observeLiveViewerCount(cloudGroupId)
                        }.collectAsState(initial = 0)
                        val activationInProgressGroups by viewModel.cloudActivationInProgress.collectAsState()
                        val isActivationInProgress = selectedGroup.groupName in activationInProgressGroups

                        LaunchedEffect(cloudGroupId, cloudMeta?.spectatorCode) {
                            if (cloudGroupId != null && cloudMeta?.spectatorCode == null) {
                                delay(4_000)
                                listenerRefreshTick++
                            }
                        }

                        if (cloudGroupId != null && !viewModel.isThisDeviceTheActiveAdmin(cloudMeta?.activeAdminDeviceId)) {
                            Spacer(Modifier.height(12.dp))
                            AdminSessionTransferSection(
                                onTransfer = { viewModel.transferAdminSession(selectedGroup.groupName) }
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        SpectatorCodeSection(
                            spectatorCode = cloudMeta?.spectatorCode,
                            viewerCount = viewerCount,
                            activationInProgress = isActivationInProgress,
                            onRegenerate = { onResult -> viewModel.regenerateSpectatorCode(selectedGroup.groupName, onResult) },
                            onRetry = { viewModel.retryCloudGroupActivation(selectedGroup.groupName) }
                        )

                        Spacer(Modifier.height(12.dp))
                        GroupVisibilityToggles(group = selectedGroup, onChange = { shareHistory, showElo, shareOnlyToday ->
                            viewModel.setGroupVisibility(selectedGroup.groupName, shareHistory, showElo, shareOnlyToday)
                        })
                        // Transferência de posse oculta por enquanto — ver comentário acima.
                    }
                }
            }
        }

        // ========== ASSINATURA E PLANOS ==========
        // Abaixo dos grupos sincronizados e recolhida por padrão para quem já é premium (ver
        // PremiumPlansSection/collapsible): uma vez assinante, o foco do Administrador passa a
        // ser configurar cada grupo, não a oferta de planos em si.
        PremiumPlansSection(viewModel = viewModel)

        // ========== GRUPOS ADMINISTRADOS COMO AUXILIAR ==========
        // Papel Auxiliar temporariamente oculto/desativado para o lançamento (a sincronização
        // Admin<->Auxiliar ainda não está estável) — ver `hide-auxiliar-role-temporarily`. Novos
        // códigos de Auxiliar não são mais gerados nem resgatáveis; esta seção fica escondida
        // mesmo que algum dispositivo de teste ainda tenha um grupo remoto com esse papel salvo.

        // Grupos acompanhados como Espectador agora aparecem só na versão "Espectador" da tela
        // (ver [SpectatorLiveScreen]/[PremiumScreenPersona]), já que essa persona fica sempre
        // acessível pelo segmented button no topo, independente de o usuário também administrar
        // outro grupo por aqui.

        Spacer(Modifier.height(8.dp))
    }
}

/**
 * `spectator-code-client`: código permanente de convite de Espectador, sempre visível para o
 * organizador enquanto o grupo estiver sincronizado (gerado automaticamente no backend na
 * primeira ativação, ver [ensureSpectatorCode][com.bismarck.voleimanager.app.util.CloudSyncManager]
 * em `volei_manager_backend`). Mostra a contagem de espectadores assistindo ao vivo agora (RTDB,
 * ver `rtdb-presence-client`), um botão de compartilhar (texto simples: código + link da Play
 * Store, fase 1 de `spectator-code-client` — sem auto-abrir o app, ver Firebase Dynamic Links
 * descontinuado), um QR code equivalente, e "Atualizar código" (revoga de verdade o acesso de
 * quem já entrou, ver [VoleiViewModel.regenerateSpectatorCode]).
 */
@Composable
private fun SpectatorCodeSection(
    spectatorCode: String?,
    viewerCount: Int,
    activationInProgress: Boolean,
    onRegenerate: ((RegeneratedSpectatorCode?, String?) -> Unit) -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var showQrDialog by remember { mutableStateOf(false) }
    var showRegenerateConfirm by remember { mutableStateOf(false) }
    var regenerating by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    if (showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showRegenerateConfirm = false },
            title = { Text(stringResource(R.string.spectator_code_regenerate_confirm_title)) },
            text = { Text(stringResource(R.string.spectator_code_regenerate_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRegenerateConfirm = false
                    regenerating = true
                    onRegenerate { result, error ->
                        regenerating = false
                        feedbackMessage = if (result != null) {
                            context.getString(R.string.spectator_code_regenerated_message, result.revokedCount)
                        } else {
                            error ?: context.getString(R.string.spectator_code_regenerate_error)
                        }
                    }
                }) {
                    Text(stringResource(R.string.spectator_code_regenerate_confirm_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showQrDialog && spectatorCode != null) {
        QrCodeDialog(text = spectatorCode, onDismiss = { showQrDialog = false })
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.spectator_code_section_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.spectator_code_section_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (spectatorCode == null) {
            var showRetry by remember { mutableStateOf(false) }
            LaunchedEffect(activationInProgress) {
                // Depois de ~8s sem o código chegar, é sinal de que a ativação do grupo falhou
                // silenciosamente no backend (ver `activateCloudGroupBackend` em VoleiViewModel) —
                // oferece um jeito de tentar de novo em vez de deixar girando pra sempre. Mas
                // enquanto a ativação (chamada ao backend + envio do histórico pré-existente)
                // ainda está rodando de verdade, não faz sentido oferecer o retry: mostra só a
                // mensagem de status (ver `retry-vs-inflight-activation`).
                showRetry = false
                if (!activationInProgress) {
                    delay(8_000)
                    showRetry = true
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(
                        if (activationInProgress) {
                            R.string.spectator_code_syncing_history
                        } else {
                            R.string.spectator_code_loading
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showRetry && !activationInProgress) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    showRetry = false
                    onRetry()
                }) {
                    Text(stringResource(R.string.spectator_code_retry_button))
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        spectatorCode,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { showRegenerateConfirm = true }, enabled = !regenerating) {
                        if (regenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.spectator_code_regenerate_button)
                            )
                        }
                    }
                }
                if (viewerCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.spectator_code_live_viewers, viewerCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (activationInProgress) {
                    // O código já está disponível para compartilhar mesmo com o histórico antigo
                    // ainda subindo em segundo plano (ver `history-backfill`) — só avisa que a
                    // sincronização inicial ainda está rolando, sem bloquear o compartilhamento.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.spectator_code_syncing_history),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(spectatorCode))
                        feedbackMessage = context.getString(R.string.spectator_code_copied)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.spectator_code_copy))
                }
                OutlinedButton(onClick = { showQrDialog = true }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.spectator_code_qr_button))
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val shareText = context.getString(
                        R.string.spectator_code_share_text,
                        spectatorCode,
                        "https://play.google.com/store/apps/details?id=${context.packageName}"
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.spectator_code_share_button))
            }
            feedbackMessage?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Diálogo com o QR code equivalente ao código de convite de Espectador — mesma informação do
 *  botão "Compartilhar código" (fase 1, texto simples), em formato para leitura por câmera. */
@Composable
private fun QrCodeDialog(text: String, onDismiss: () -> Unit) {
    val bitmap = remember(text) { QrCodeGenerator.generate(text) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.spectator_code_qr_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.spectator_code_qr_title),
                        modifier = Modifier.size(220.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.spectator_code_qr_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

/**
 * `admin-session-transfer`: aviso + botão mostrados quando outro aparelho é quem está publicando
 * os dados ao vivo deste grupo (ver [VoleiViewModel.isThisDeviceTheActiveAdmin]) — normalmente
 * porque o organizador restaurou um backup em um novo aparelho. "Assumir sessão" transfere a
 * publicação para este aparelho, exigindo confirmação explícita por ser uma ação com efeito
 * colateral em outro dispositivo.
 */
@Composable
private fun AdminSessionTransferSection(onTransfer: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.admin_session_transfer_confirm_title)) },
            text = { Text(stringResource(R.string.admin_session_transfer_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    onTransfer()
                }) {
                    Text(stringResource(R.string.admin_session_transfer_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.admin_session_transfer_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { showConfirm = true }) {
            Text(stringResource(R.string.admin_session_transfer_button))
        }
    }
}

/** Toggles de `observer-visibility-controls`: compartilhar histórico e ranking de Elo com
 *  espectadores, e o alcance do histórico compartilhado (completo ou só hoje). Mostrar Elo e a
 *  escolha de alcance exigem compartilhar histórico também (ver [VoleiViewModel.setGroupVisibility]). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun GroupVisibilityToggles(
    group: GroupConfig,
    onChange: (shareHistory: Boolean, showElo: Boolean, shareOnlyToday: Boolean) -> Unit
) {
    // Mesmas cores usadas para um Switch desabilitado em "Regras do grupo" (ver TooltipToggleRow em
    // Dialogs.kt): o alpha padrão do Material3 (~12%) some no fundo do card nesta tela.
    val lockedSwitchColors = SwitchDefaults.colors(
        disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        disabledCheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    )
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        CloudSyncToggleRow(
            label = stringResource(R.string.cloud_sync_visibility_share_history),
            tooltip = stringResource(R.string.cloud_sync_visibility_share_history_tooltip),
            checked = group.shareHistoryWithObservers,
            onCheckedChange = { checked ->
                onChange(checked, group.showEloToObservers && checked, group.shareOnlyTodayHistory && checked)
            },
            colors = lockedSwitchColors
        )
        if (group.shareHistoryWithObservers) {
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !group.shareOnlyTodayHistory,
                    onClick = { onChange(true, group.showEloToObservers, false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    icon = {}
                ) {
                    Text(
                        stringResource(R.string.cloud_sync_visibility_share_full_history),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                SegmentedButton(
                    selected = group.shareOnlyTodayHistory,
                    onClick = { onChange(true, group.showEloToObservers, true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    icon = {}
                ) {
                    Text(
                        stringResource(R.string.cloud_sync_visibility_share_today_history),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // "Mostrar Elo" só faz sentido (e só fica visível) com o histórico compartilhado
            // ativo — sem histórico compartilhado não há nada para o espectador ver o Elo junto.
            CloudSyncToggleRow(
                label = stringResource(R.string.cloud_sync_visibility_show_elo),
                tooltip = stringResource(R.string.cloud_sync_visibility_show_elo_tooltip),
                checked = group.showEloToObservers,
                onCheckedChange = { checked -> onChange(group.shareHistoryWithObservers, checked, group.shareOnlyTodayHistory) },
                colors = lockedSwitchColors
            )
        }
    }
}

/** Row com Switch reutilizada nos toggles de sincronização/visibilidade desta tela: o texto e o
 *  switch inteiros são clicáveis (não só o switch) e um toque longo revela um [PlainTooltip]
 *  explicando o que o toggle faz — quando desabilitado, [tooltip] deve explicar o motivo (ex.:
 *  "Mostrar Elo" exige "Compartilhar histórico" ativo primeiro). Mesmo padrão do TooltipToggleRow
 *  em Dialogs.kt, mas com o texto à esquerda ocupando o espaço e o Switch à direita, layout já
 *  usado nesta tela. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CloudSyncToggleRow(
    label: String,
    tooltip: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: SwitchColors,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val haptic = LocalHapticFeedback.current

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(text = tooltip, style = MaterialTheme.typography.bodySmall)
            }
        },
        state = tooltipState
    ) {
        Row(
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
                        scope.launch { tooltipState.show() }
                    }
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = {
                    tooltipState.dismiss()
                    onCheckedChange(it)
                },
                colors = colors
            )
        }
    }
}

/**
 * Card "Assinatura" reaproveitado tanto pelo Organizador/Auxiliar (para sincronizar grupo(s) em
 * nuvem) quanto pelo Espectador (para apoiar financeiramente o projeto). Os dois pacotes de
 * assinatura são exatamente os mesmos ([BillingProductIds.SINGLE_GROUP]/[BillingProductIds.MULTI_GROUP])
 * — na prática tanto faz assinar para de fato sincronizar um grupo quanto assinar só para apoiar o
 * desenvolvimento, já que todo assinante ganha os mesmos benefícios: personalização de cores de
 * time (local para qualquer assinante; também sincronizada em grupo para o organizador) e o limite
 * de grupos sincronizados do pacote escolhido (1 ou até 5), mesmo que um Espectador normalmente não
 * tenha motivo para sincronizar um grupo próprio. [title]/[description] permitem customizar a
 * chamada conforme o perfil (ver [SpectatorLiveScreen]/[OrganizerAssistantCloudScreen]).
 */
@Composable
internal fun PremiumPlansSection(
    viewModel: VoleiViewModel,
    title: String = stringResource(R.string.cloud_sync_status_title),
    description: String? = null,
    collapsible: Boolean = true
) {
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val debugPremiumOverride by viewModel.debugPremiumOverride.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val subscriptionOffers by viewModel.subscriptionOffers.collectAsState()
    val canPurchasePremium by viewModel.canPurchasePremium.collectAsState()
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current

    // Só recolhe de fato quando já é premium: quem ainda não assinou precisa ver os planos
    // (a "chamada" da seção), enquanto quem já assina tem o foco nas configurações premium de
    // cada grupo, não mais na oferta em si — ver TooltipToggleRow em FAQItem para o mesmo padrão
    // de seta giratória usado no FAQ.
    val effectivelyCollapsible = collapsible && hasPremiumAccess
    var expanded by rememberSaveable(hasPremiumAccess) { mutableStateOf(!hasPremiumAccess) }
    val isExpanded = !effectivelyCollapsible || expanded
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "PremiumPlansArrowRotation"
    )

    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (effectivelyCollapsible) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expanded = !expanded }
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (effectivelyCollapsible) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }
        Text(
            if (hasPremiumAccess) {
                val tierLabel = stringResource(
                    if (effectivePlanTier == CloudPlanTier.MULTI) {
                        R.string.cloud_sync_tier_multi
                    } else {
                        R.string.cloud_sync_tier_single
                    }
                )
                stringResource(R.string.cloud_sync_status_active, tierLabel)
            } else {
                stringResource(R.string.cloud_sync_status_inactive)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasPremiumAccess) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
        ) {
            Column {
                HorizontalDivider(
                    Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )

                if (!hasPremiumAccess && !canPurchasePremium) {
                    Text(
                        stringResource(R.string.premium_purchase_gating_banner),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                val singleMonthlyOffer = subscriptionOffers.firstOrNull {
                    it.productId == BillingProductIds.SINGLE_GROUP && it.basePlanId == BillingProductIds.BASE_PLAN_MONTHLY
                }
                val singleAnnualOffer = subscriptionOffers.firstOrNull {
                    it.productId == BillingProductIds.SINGLE_GROUP && it.basePlanId == BillingProductIds.BASE_PLAN_ANNUAL
                }
                val multiMonthlyOffer = subscriptionOffers.firstOrNull {
                    it.productId == BillingProductIds.MULTI_GROUP && it.basePlanId == BillingProductIds.BASE_PLAN_MONTHLY
                }
                val multiAnnualOffer = subscriptionOffers.firstOrNull {
                    it.productId == BillingProductIds.MULTI_GROUP && it.basePlanId == BillingProductIds.BASE_PLAN_ANNUAL
                }

                PlanOptionRow(
                    title = stringResource(R.string.cloud_sync_plan_single_title),
                    monthlyPrice = singleMonthlyOffer?.let { stringResource(R.string.cloud_sync_price_per_month, it.formattedPrice) }
                        ?: stringResource(R.string.cloud_sync_plan_single_price),
                    selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.SINGLE,
                    onSubscribeClick = activity?.let { act ->
                        { viewModel.purchasePremiumPlan(act, CloudPlanTier.SINGLE, annual = false) }
                    },
                    annualPrice = singleAnnualOffer?.let {
                        stringResource(
                            R.string.cloud_sync_plan_annual_price,
                            it.formattedPrice,
                            monthlyEquivalentFormatted(it)
                        )
                    } ?: stringResource(R.string.cloud_sync_plan_single_annual_price),
                    onSubscribeAnnualClick = activity?.let { act ->
                        { viewModel.purchasePremiumPlan(act, CloudPlanTier.SINGLE, annual = true) }
                    }
                )
                Spacer(Modifier.height(16.dp))
                PlanOptionRow(
                    title = stringResource(R.string.cloud_sync_plan_multi_title),
                    monthlyPrice = multiMonthlyOffer?.let { stringResource(R.string.cloud_sync_price_per_month, it.formattedPrice) }
                        ?: stringResource(R.string.cloud_sync_plan_multi_price),
                    selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.MULTI,
                    onSubscribeClick = activity?.let { act ->
                        { viewModel.purchasePremiumPlan(act, CloudPlanTier.MULTI, annual = false) }
                    },
                    annualPrice = multiAnnualOffer?.let {
                        stringResource(
                            R.string.cloud_sync_plan_annual_price,
                            it.formattedPrice,
                            monthlyEquivalentFormatted(it)
                        )
                    } ?: stringResource(R.string.cloud_sync_plan_multi_annual_price),
                    onSubscribeAnnualClick = activity?.let { act ->
                        { viewModel.purchasePremiumPlan(act, CloudPlanTier.MULTI, annual = true) }
                    }
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.cloud_sync_plan_benefits_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.cloud_sync_plan_benefits),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Cancelamento real de assinatura é sempre feito pela própria Play Store (não há
                // API pública de cliente para cancelar do lado do app) — abre a tela de gestão de
                // assinaturas do Google Play já filtrada pelo produto ativo. Só faz sentido para
                // quem tem uma assinatura de verdade (não a simulação de debug).
                if (hasPremiumAccess && !debugPremiumOverride) {
                    Spacer(Modifier.height(16.dp))
                    val activeProductId = if (effectivePlanTier == CloudPlanTier.MULTI) {
                        BillingProductIds.MULTI_GROUP
                    } else {
                        BillingProductIds.SINGLE_GROUP
                    }
                    OutlinedButton(onClick = {
                        val uri =
                            "https://play.google.com/store/account/subscriptions?sku=$activeProductId&package=${context.packageName}".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) {
                        Text(stringResource(R.string.cloud_sync_cancel_subscription))
                    }
                }

                if (BuildConfig.DEBUG) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Text(
                        stringResource(R.string.cloud_sync_debug_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        TextButton(onClick = {
                            viewModel.setDebugPremiumPlanTier(CloudPlanTier.SINGLE)
                            viewModel.setDebugPremiumOverride(true)
                        }) {
                            Text(stringResource(R.string.cloud_sync_debug_simulate_single))
                        }
                        TextButton(onClick = {
                            viewModel.setDebugPremiumPlanTier(CloudPlanTier.MULTI)
                            viewModel.setDebugPremiumOverride(true)
                        }) {
                            Text(stringResource(R.string.cloud_sync_debug_simulate_multi))
                        }
                    }
                    if (debugPremiumOverride) {
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.setDebugPremiumOverride(false) }) {
                            Text(stringResource(R.string.cloud_sync_debug_cancel_simulation))
                        }
                    }
                }
            }
        }
    }
}

/** Preço anual dividido por 12, formatado na moeda do próprio plano — dá pro usuário perceber, na
 *  sua própria moeda local (respeitando o preço regional do Play Console), quanto economiza por
 *  mês ao assinar o plano anual em vez do mensal. Cai para [SubscriptionOffer.formattedPrice] se o
 *  código de moeda vier inválido/ausente (nunca deveria acontecer com dados reais da Play Store). */
private fun monthlyEquivalentFormatted(offer: SubscriptionOffer): String = try {
    val currency = Currency.getInstance(offer.priceCurrencyCode)
    val format = NumberFormat.getCurrencyInstance().apply { this.currency = currency }
    format.format(offer.priceAmountMicros / 12.0 / 1_000_000.0)
} catch (e: IllegalArgumentException) {
    offer.formattedPrice
}

@Composable
private fun PlanOptionRow(
    title: String,
    monthlyPrice: String,
    annualPrice: String,
    selected: Boolean,
    onSubscribeClick: (() -> Unit)?,
    onSubscribeAnnualClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text(
                stringResource(R.string.cloud_sync_plan_active_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            monthlyPrice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            annualPrice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onSubscribeClick != null) {
            OutlinedButton(
                onClick = onSubscribeClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cloud_sync_plan_subscribe))
            }
        }
        if (onSubscribeAnnualClick != null) {
            // Anual recebe mais destaque (botão preenchido) porque é a opção mais vantajosa por
            // mês, incentivando quem já decidiu assinar a preferi-la em vez do mensal.
            Button(
                onClick = onSubscribeAnnualClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cloud_sync_plan_subscribe_annual))
            }
        }
    }
}

@Composable
internal fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

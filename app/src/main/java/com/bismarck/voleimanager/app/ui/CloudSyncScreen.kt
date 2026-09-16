package com.bismarck.voleimanager.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.app.Activity
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.ui.components.JoinExistingGroupDialog
import com.bismarck.voleimanager.app.ui.components.GenerateJoinCodeDialog
import com.bismarck.voleimanager.app.ui.components.TransferGroupOwnershipDialog
import com.bismarck.voleimanager.app.ui.viewmodel.CloudPlanTier
import com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.BillingProductIds
import com.bismarck.voleimanager.app.util.JoinRole
import com.bismarck.voleimanager.app.util.LiveGameState
import com.bismarck.voleimanager.app.util.RemoteEloLogEntry
import com.bismarck.voleimanager.app.util.RemoteHistoryEntry
import com.bismarck.voleimanager.app.util.RemotePlayerSnapshot

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

    var showJoinDialog by rememberSaveable { mutableStateOf(false) }
    if (showJoinDialog) {
        JoinExistingGroupDialog(
            onDismiss = { showJoinDialog = false },
            onConfirm = { code, onResult -> viewModel.joinGroupWithCode(code, onResult) }
        )
    }

    when (userProfileType) {
        UserProfileType.ESPECTADOR -> SpectatorLiveScreen(viewModel)
        else -> OrganizerAssistantCloudScreen(viewModel, onJoinGroupClick = { showJoinDialog = true })
    }
}

@Composable
private fun SpectatorLiveScreen(viewModel: VoleiViewModel) {
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val remoteSpectatorGroup = allGroups.firstOrNull { it.remoteRole == UserProfileType.ESPECTADOR.name }
    val liveState by viewModel.remoteLiveGameState.collectAsState()
    val remoteHistory by viewModel.remoteHistory.collectAsState()
    val remoteEloLogs by viewModel.remoteEloLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(0.dp))
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
            if (remoteSpectatorGroup == null) {
                Text(
                    stringResource(R.string.live_screen_spectator_no_group),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    stringResource(R.string.live_screen_spectator_placeholder, remoteSpectatorGroup.groupName),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Perfil "Espectador" é escolhido uma única vez no primeiro onboarding do app; quem
            // errou a resposta (ou mudou de ideia depois) não tinha, até aqui, nenhum jeito de
            // corrigir sem reinstalar. Reaproveita o mesmo retorno usado pelo botão "voltar" do
            // onboarding (ver [VoleiViewModel.returnToProfileSelection]) para reabrir a pergunta de
            // perfil e permitir escolher Organizador(a)/Auxiliar.
            TextButton(onClick = { viewModel.returnToProfileSelection() }) {
                Text(stringResource(R.string.live_screen_not_spectator_hint))
            }
        }

        if (remoteSpectatorGroup != null) {
            SectionCard {
                if (liveState == null) {
                    Text(
                        stringResource(R.string.live_screen_no_live_state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LiveScoreboard(liveState!!)
                }
            }

            if (remoteSpectatorGroup.shareHistoryWithObservers) {
                SectionCard {
                    Text(
                        stringResource(R.string.live_screen_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (remoteHistory.isEmpty()) {
                        Text(
                            stringResource(R.string.live_screen_history_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        remoteHistory.take(20).forEach { entry -> RemoteHistoryRow(entry) }
                    }
                }

                if (remoteSpectatorGroup.showEloToObservers) {
                    SectionCard {
                        Text(
                            stringResource(R.string.live_screen_elo_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (remoteEloLogs.isEmpty()) {
                            Text(
                                stringResource(R.string.live_screen_elo_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            remoteEloLogs.take(20).forEach { entry -> RemoteEloRow(entry) }
                        }
                    }
                }
            } else {
                SectionCard {
                    Text(
                        stringResource(R.string.live_screen_history_hidden),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

@Composable
private fun OrganizerAssistantCloudScreen(viewModel: VoleiViewModel, onJoinGroupClick: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val debugPremiumOverride by viewModel.debugPremiumOverride.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val syncedGroupNames by viewModel.cloudSyncedGroupNames.collectAsState()
    val subscriptionOffers by viewModel.subscriptionOffers.collectAsState()
    val activity = LocalContext.current as? Activity

    var transferDialogFor by remember { mutableStateOf<String?>(null) }
    transferDialogFor?.let { groupName ->
        TransferGroupOwnershipDialog(
            groupName = groupName,
            onDismiss = { transferDialogFor = null },
            onConfirm = { email ->
                viewModel.requestGroupOwnershipTransfer(groupName, email)
                transferDialogFor = null
            }
        )
    }

    var generateCodeDialogFor by remember { mutableStateOf<String?>(null) }
    generateCodeDialogFor?.let { groupName ->
        GenerateJoinCodeDialog(
            groupName = groupName,
            onDismiss = { generateCodeDialogFor = null },
            onGenerateAuxiliar = { onResult ->
                viewModel.generateJoinCode(groupName, JoinRole.AUXILIAR, onResult)
            },
            onGenerateEspectador = { onResult ->
                viewModel.generateJoinCode(groupName, JoinRole.ESPECTADOR, onResult)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(0.dp))

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.WorkspacePremium,
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

        // ========== ENTRAR EM UM GRUPO (código de Auxiliar/Espectador) ==========
        // Colocado logo no topo: é o ponto de entrada mais comum para quem chega nesta tela com
        // um código em mãos (recebido de um organizador/auxiliar), sem precisar rolar a tela.
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_join_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.cloud_sync_join_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onJoinGroupClick) {
                Text(stringResource(R.string.join_existing_group))
            }
        }

        // ========== CONTA (Firebase Auth e-mail/senha) ==========
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_account_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (currentUser != null) {
                Text(
                    currentUser?.nickname?.takeIf { it.isNotBlank() }
                        ?: currentUser?.email.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { viewModel.signOut() }) {
                    Text(stringResource(R.string.logout))
                }
            } else {
                Text(
                    stringResource(R.string.cloud_sync_account_signed_out_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ========== ASSINATURA E PLANOS ==========
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_status_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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

            HorizontalDivider(
                Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )

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
                price = singleMonthlyOffer?.formattedPrice
                    ?: stringResource(R.string.cloud_sync_plan_single_price),
                selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.SINGLE,
                onSubscribeClick = activity?.let { act ->
                    { viewModel.purchasePremiumPlan(act, CloudPlanTier.SINGLE, annual = false) }
                },
                annualPrice = singleAnnualOffer?.formattedPrice,
                onSubscribeAnnualClick = activity?.takeIf { singleAnnualOffer != null }?.let { act ->
                    { viewModel.purchasePremiumPlan(act, CloudPlanTier.SINGLE, annual = true) }
                }
            )
            Spacer(Modifier.height(8.dp))
            PlanOptionRow(
                title = stringResource(R.string.cloud_sync_plan_multi_title),
                price = multiMonthlyOffer?.formattedPrice
                    ?: stringResource(R.string.cloud_sync_plan_multi_price),
                selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.MULTI,
                onSubscribeClick = activity?.let { act ->
                    { viewModel.purchasePremiumPlan(act, CloudPlanTier.MULTI, annual = false) }
                },
                annualPrice = multiAnnualOffer?.formattedPrice,
                onSubscribeAnnualClick = activity?.takeIf { multiAnnualOffer != null }?.let { act ->
                    { viewModel.purchasePremiumPlan(act, CloudPlanTier.MULTI, annual = true) }
                }
            )
            Text(
                stringResource(R.string.cloud_sync_plan_annual_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (BuildConfig.DEBUG) {
                HorizontalDivider(
                    Modifier.padding(vertical = 12.dp),
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
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.setDebugPremiumOverride(false) }) {
                        Text(stringResource(R.string.cloud_sync_debug_cancel_simulation))
                    }
                }
            } else if (!hasPremiumAccess) {
                Text(
                    stringResource(R.string.cloud_sync_plan_coming_soon),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // ========== GRUPOS SINCRONIZADOS ==========
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_groups_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (hasPremiumAccess) {
                    stringResource(
                        R.string.cloud_sync_groups_limit_label,
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

            val ownGroups = allGroups.filter { it.remoteRole == null }
            if (ownGroups.isEmpty()) {
                Text(
                    stringResource(R.string.cloud_sync_groups_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ownGroups.sortedBy { it.groupName }.forEach { group: GroupConfig ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                group.groupName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = group.isCloudSynced,
                                enabled = hasPremiumAccess,
                                onCheckedChange = { checked ->
                                    viewModel.setGroupCloudSynced(group.groupName, checked)
                                }
                            )
                        }
                        if (group.isCloudSynced) {
                            TextButton(onClick = { generateCodeDialogFor = group.groupName }) {
                                Text(stringResource(R.string.generate_join_code_menu_item))
                            }
                            GroupVisibilityToggles(group = group, onChange = { shareHistory, showElo ->
                                viewModel.setGroupVisibility(group.groupName, shareHistory, showElo)
                            })
                            if (group.pendingOwnershipTransferTo != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stringResource(
                                            R.string.transfer_ownership_pending_label,
                                            group.pendingOwnershipTransferTo.orEmpty()
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.cancelGroupOwnershipTransfer(group.groupName) }) {
                                        Text(stringResource(R.string.transfer_ownership_cancel))
                                    }
                                }
                            } else {
                                TextButton(onClick = { transferDialogFor = group.groupName }) {
                                    Text(stringResource(R.string.transfer_ownership_menu_item))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ========== GRUPOS ADMINISTRADOS COMO AUXILIAR ==========
        val auxiliarGroups = allGroups.filter { it.remoteRole == UserProfileType.AUXILIAR.name }
        if (auxiliarGroups.isNotEmpty()) {
            SectionCard {
                Text(
                    stringResource(R.string.cloud_sync_auxiliar_groups_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                auxiliarGroups.sortedBy { it.groupName }.forEach { group ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(group.groupName, style = MaterialTheme.typography.bodyMedium)
                        GroupVisibilityToggles(group = group, onChange = { shareHistory, showElo ->
                            viewModel.setGroupVisibility(group.groupName, shareHistory, showElo)
                        })
                    }
                }
            }
        }

        // ========== GRUPOS ACOMPANHADOS COMO ESPECTADOR ==========
        // Um mesmo dispositivo/conta pode ser Auxiliar de um grupo e Espectador de outro ao mesmo
        // tempo — listar aqui também, ainda que a visualização ao vivo em si só apareça quando o
        // perfil global escolhido no onboarding for Espectador (ver [SpectatorLiveScreen]).
        val espectadorGroups = allGroups.filter { it.remoteRole == UserProfileType.ESPECTADOR.name }
        if (espectadorGroups.isNotEmpty()) {
            SectionCard {
                Text(
                    stringResource(R.string.cloud_sync_espectador_groups_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                espectadorGroups.sortedBy { it.groupName }.forEach { group ->
                    Text(group.groupName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

/** Toggles de `observer-visibility-controls`: compartilhar histórico e ranking de Elo com
 *  espectadores. Mostrar Elo exige compartilhar histórico também (ver [VoleiViewModel.setGroupVisibility]). */
@Composable
private fun GroupVisibilityToggles(group: GroupConfig, onChange: (shareHistory: Boolean, showElo: Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(
            stringResource(R.string.cloud_sync_visibility_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cloud_sync_visibility_share_history),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = group.shareHistoryWithObservers,
                onCheckedChange = { checked -> onChange(checked, group.showEloToObservers && checked) }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cloud_sync_visibility_show_elo),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = group.showEloToObservers,
                enabled = group.shareHistoryWithObservers,
                onCheckedChange = { checked -> onChange(group.shareHistoryWithObservers, checked) }
            )
        }
    }
}

@Composable
private fun PlanOptionRow(
    title: String,
    price: String,
    selected: Boolean,
    onSubscribeClick: (() -> Unit)?,
    annualPrice: String? = null,
    onSubscribeAnnualClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(price, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) {
            Text(
                stringResource(R.string.cloud_sync_plan_active_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        if (onSubscribeClick != null) {
            OutlinedButton(
                onClick = onSubscribeClick,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(stringResource(R.string.cloud_sync_plan_subscribe))
            }
        }
    }
    if (onSubscribeAnnualClick != null && annualPrice != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.cloud_sync_plan_annual_price, annualPrice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            TextButton(onClick = onSubscribeAnnualClick) {
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

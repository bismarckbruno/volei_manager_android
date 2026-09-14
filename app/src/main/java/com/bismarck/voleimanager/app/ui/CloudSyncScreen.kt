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
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.ui.components.JoinExistingGroupDialog
import com.bismarck.voleimanager.app.ui.components.GenerateJoinCodeDialog
import com.bismarck.voleimanager.app.ui.components.TransferGroupOwnershipDialog
import com.bismarck.voleimanager.app.ui.viewmodel.CloudPlanTier
import com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import com.bismarck.voleimanager.app.util.JoinRole

/**
 * Tela "Ao vivo": ponto único de sincronização em nuvem premium. O conteúdo é dividido por
 * perfil do usuário ([UserProfileType]):
 * - Sem perfil definido (usuário pulou/nunca respondeu o onboarding de perfil): mostra as três
 *   opções para escolher agora (mesmo componente do onboarding).
 * - Organizador(a)/Auxiliar: gestão de conta, assinatura/planos e grupos sincronizados (o que já
 *   existia nesta tela).
 * - Espectador(a): visão ao vivo (placar, times, fila) do grupo que ele entrou via código —
 *   ainda um stub, já que a engine de sincronização de fato (`firestore-sync-engine`) não existe.
 *
 * Cadastro/login real (Firebase Auth) já funciona (e-mail/senha); a engine de sincronização de
 * fato (Firestore) e o pagamento chegam em fases seguintes (`firestore-sync-engine`,
 * `billing-integration`) — por ora, planos e grupo(s) sincronizado(s) usam
 * [VoleiViewModel.debugPremiumOverride]/[VoleiViewModel.debugPremiumPlanTier] (apenas em debug).
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
        }
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
                    Icons.Outlined.Cloud,
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

            PlanOptionRow(
                title = stringResource(R.string.cloud_sync_plan_single_title),
                price = stringResource(R.string.cloud_sync_plan_single_price),
                selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.SINGLE,
                onSubscribeClick = {
                    viewModel.setDebugPremiumPlanTier(CloudPlanTier.SINGLE)
                    viewModel.setDebugPremiumOverride(true)
                }.takeIf { BuildConfig.DEBUG }
            )
            Spacer(Modifier.height(8.dp))
            PlanOptionRow(
                title = stringResource(R.string.cloud_sync_plan_multi_title),
                price = stringResource(R.string.cloud_sync_plan_multi_price),
                selected = hasPremiumAccess && effectivePlanTier == CloudPlanTier.MULTI,
                onSubscribeClick = {
                    viewModel.setDebugPremiumPlanTier(CloudPlanTier.MULTI)
                    viewModel.setDebugPremiumOverride(true)
                }.takeIf { BuildConfig.DEBUG }
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

        // ========== ENTRAR EM UM GRUPO (código de Auxiliar/Espectador) ==========
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

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun PlanOptionRow(
    title: String,
    price: String,
    selected: Boolean,
    onSubscribeClick: (() -> Unit)?
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
}

@Composable
private fun SectionCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
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

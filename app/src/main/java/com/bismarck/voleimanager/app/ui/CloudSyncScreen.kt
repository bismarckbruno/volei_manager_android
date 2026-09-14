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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.ui.viewmodel.CloudPlanTier
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel

/**
 * Tela "Nuvem": ponto único para o organizador/auxiliar assinar a sincronização em nuvem, escolher
 * qual(is) grupo(s) local(is) ficam sincronizados, e (fase futura) entrar em um grupo de outra
 * pessoa via código/QR. Cadastro/login real (Firebase Auth) e a engine de sincronização de fato
 * (Firestore) chegam em fases seguintes (`auth-account-flow`, `firestore-sync-engine`) — por ora
 * esta tela cobre o que já é testável localmente via [VoleiViewModel.debugPremiumOverride] /
 * [VoleiViewModel.debugPremiumPlanTier] (apenas em build de debug).
 */
@Composable
fun CloudSyncScreen(viewModel: VoleiViewModel) {
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val debugPremiumOverride by viewModel.debugPremiumOverride.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val debugPlanTier by viewModel.debugPremiumPlanTier.collectAsState()
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val syncedGroupNames by viewModel.cloudSyncedGroupNames.collectAsState()

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

        // ========== CONTA (cadastro/login chegam em `auth-account-flow`) ==========
        SectionCard {
            Text(
                stringResource(R.string.cloud_sync_account_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.cloud_sync_account_coming_soon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

            if (allGroups.isEmpty()) {
                Text(
                    stringResource(R.string.cloud_sync_groups_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                allGroups.sortedBy { it.groupName }.forEach { group: GroupConfig ->
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
                }
            }
        }

        // ========== ENTRAR EM UM GRUPO (código/QR — fase futura) ==========
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

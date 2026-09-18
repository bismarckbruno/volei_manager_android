package com.bismarck.voleimanager.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.app.Activity
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.ui.components.GenerateJoinCodeDialog
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

    when (userProfileType) {
        UserProfileType.ESPECTADOR -> SpectatorLiveScreen(viewModel)
        else -> OrganizerAssistantCloudScreen(viewModel)
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
            // Perfil "Espectador" é escolhido uma única vez no primeiro onboarding do app; quem
            // errou a resposta (ou mudou de ideia depois) não tinha, até aqui, nenhum jeito de
            // corrigir sem reinstalar. Reaproveita o mesmo retorno usado pelo botão "voltar" do
            // onboarding (ver [VoleiViewModel.returnToProfileSelection]) para reabrir a pergunta de
            // perfil e permitir escolher Organizador(a)/Auxiliar.
            TextButton(onClick = { viewModel.returnToProfileSelection() }) {
                Text(stringResource(R.string.live_screen_not_spectator_hint))
            }
        }

        // O placar, a fila de espera e o histórico de partidas já aparecem com muito mais detalhe
        // e qualidade nas telas "Jogo (Ao vivo)" e "Histórico" (que reaproveitam a mesma base
        // visual do organizador — ver `spectator-reuse-game-screen`/`spectator-reuse-history-screen`).
        // Esta tela não repete essas visualizações simplificadas; foca só no apoio ao projeto.
        PremiumPlansSection(
            viewModel = viewModel,
            title = stringResource(R.string.cloud_sync_spectator_support_title),
            description = stringResource(R.string.cloud_sync_spectator_support_description)
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganizerAssistantCloudScreen(viewModel: VoleiViewModel) {
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val allGroups by viewModel.allGroupConfigs.collectAsState()
    val syncedGroupNames by viewModel.cloudSyncedGroupNames.collectAsState()

    // Transferência de posse do grupo temporariamente oculta (ver `hide-ownership-transfer-temporarily`)
    // — a funcionalidade em si (VoleiViewModel.requestGroupOwnershipTransfer/cancelGroupOwnershipTransfer)
    // continua implementada; só a UI de acesso fica escondida por enquanto para revisitar depois.

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
                    painter = painterResource(R.drawable.premium_icon),
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

        // ========== ASSINATURA E PLANOS ==========
        PremiumPlansSection(viewModel = viewModel)

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
                var selectedGroupName by rememberSaveable { mutableStateOf<String?>(null) }
                val selectedGroup = sortedGroups.firstOrNull { it.groupName == selectedGroupName }
                    ?: sortedGroups.first()
                LaunchedEffect(selectedGroup.groupName) {
                    selectedGroupName = selectedGroup.groupName
                }

                var expanded by remember { mutableStateOf(false) }
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
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(56.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.offset(x = 0.dp, y = 4.dp)) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.cloud_sync_groups_activate_label),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
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
                    }
                    if (selectedGroup.isCloudSynced) {
                        TextButton(onClick = { generateCodeDialogFor = selectedGroup.groupName }) {
                            Text(stringResource(R.string.generate_join_code_menu_item))
                        }
                        GroupVisibilityToggles(group = selectedGroup, onChange = { shareHistory, showElo, shareOnlyToday ->
                            viewModel.setGroupVisibility(selectedGroup.groupName, shareHistory, showElo, shareOnlyToday)
                        })
                        // Transferência de posse oculta por enquanto — ver comentário acima.
                    }
                }
            }
        }

        // ========== GRUPOS ADMINISTRADOS COMO AUXILIAR ==========
        // Papel Auxiliar temporariamente oculto/desativado para o lançamento (a sincronização
        // Admin<->Auxiliar ainda não está estável) — ver `hide-auxiliar-role-temporarily`. Novos
        // códigos de Auxiliar não são mais gerados nem resgatáveis; esta seção fica escondida
        // mesmo que algum dispositivo de teste ainda tenha um grupo remoto com esse papel salvo.

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
 *  espectadores, e o alcance do histórico compartilhado (completo ou só hoje). Mostrar Elo e a
 *  escolha de alcance exigem compartilhar histórico também (ver [VoleiViewModel.setGroupVisibility]). */
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
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cloud_sync_visibility_share_history),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = group.shareHistoryWithObservers,
                onCheckedChange = { checked ->
                    onChange(checked, group.showEloToObservers && checked, group.shareOnlyTodayHistory && checked)
                },
                colors = lockedSwitchColors
            )
        }
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
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.cloud_sync_visibility_show_elo),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = group.showEloToObservers,
                enabled = group.shareHistoryWithObservers,
                onCheckedChange = { checked -> onChange(group.shareHistoryWithObservers, checked, group.shareOnlyTodayHistory) },
                colors = lockedSwitchColors
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
    description: String? = null
) {
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()
    val debugPremiumOverride by viewModel.debugPremiumOverride.collectAsState()
    val effectivePlanTier by viewModel.effectivePremiumPlanTier.collectAsState()
    val subscriptionOffers by viewModel.subscriptionOffers.collectAsState()
    val activity = LocalContext.current as? Activity

    SectionCard {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
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

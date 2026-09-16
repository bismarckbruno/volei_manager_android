package com.bismarck.voleimanager.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel

/**
 * Substitui a tela "Jogo (Ao vivo)" quando o grupo ativo é remoto (entrado via código de
 * Auxiliar/Espectador, [com.bismarck.voleimanager.app.data.model.GroupConfig.remoteRole] != null):
 * este dispositivo nunca cria jogadores/partidas locais para um grupo de outra pessoa (ver
 * [VoleiViewModel.joinRemoteGroup]), então a tela local de jogo sempre apareceria vazia. Em vez
 * disso, lê diretamente [VoleiViewModel.remoteLiveGameState] (placar, times, fila), espelhado em
 * tempo real do Firestore por quem organiza o grupo. Suporta trocar os lados dos cards (botão
 * "VS"), como a tela local, mas é somente leitura — sem placar, sem decretar vitória, sem fila
 * editável, pois isso é feito no aparelho do organizador/auxiliar de verdade.
 */
@Composable
fun RemoteGameScreen(viewModel: VoleiViewModel) {
    val liveState by viewModel.remoteLiveGameState.collectAsState()
    var swapSides by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (liveState == null) {
            SectionCard {
                Text(
                    stringResource(R.string.spectator_waiting_game_start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.live_screen_no_live_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { swapSides = !swapSides }) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = stringResource(R.string.swap_team_sides))
                    }
                }
                LiveScoreboard(liveState!!, swapSides = swapSides)
            }
        }
    }
}

/**
 * Substitui a tela "Histórico" quando o grupo ativo é remoto — lê [VoleiViewModel.remoteHistory]
 * e [VoleiViewModel.remoteEloLogs] (Firestore), que só vêm preenchidos quando o organizador/
 * auxiliar do grupo ligou, respectivamente,
 * [com.bismarck.voleimanager.app.data.model.GroupConfig.shareHistoryWithObservers] e
 * [com.bismarck.voleimanager.app.data.model.GroupConfig.showEloToObservers].
 */
@Composable
fun RemoteHistoryScreen(viewModel: VoleiViewModel) {
    val groupConfig by viewModel.currentGroupConfig.collectAsState()
    val remoteHistory by viewModel.remoteHistory.collectAsState()
    val remoteEloLogs by viewModel.remoteEloLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val isAuxiliar = groupConfig.remoteRole == com.bismarck.voleimanager.app.ui.viewmodel.UserProfileType.AUXILIAR.name
        if (!isAuxiliar && !groupConfig.shareHistoryWithObservers) {
            SectionCard {
                Text(
                    stringResource(R.string.live_screen_history_hidden),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

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
                remoteHistory.forEach { entry -> RemoteHistoryRow(entry) }
            }
        }

        if (isAuxiliar || groupConfig.showEloToObservers) {
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
                    remoteEloLogs.forEach { entry -> RemoteEloRow(entry) }
                }
            }
        }
    }
}

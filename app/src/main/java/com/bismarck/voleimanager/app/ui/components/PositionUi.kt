package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.PositionRole
import com.bismarck.voleimanager.app.data.model.TeamSlot
import com.bismarck.voleimanager.app.util.PositionAssigner

/** Nome completo da posição, no idioma do app. */
@Composable
fun positionLabel(position: PlayerPosition): String = stringResource(
    when (position) {
        PlayerPosition.SETTER -> R.string.position_setter
        PlayerPosition.OUTSIDE_HITTER -> R.string.position_outside_hitter
        PlayerPosition.MIDDLE_BLOCKER -> R.string.position_middle_blocker
        PlayerPosition.OPPOSITE -> R.string.position_opposite
        PlayerPosition.LIBERO -> R.string.position_libero
    }
)

/** Abreviação da posição, usada nos cards de time. */
@Composable
fun positionShortLabel(position: PlayerPosition): String = stringResource(
    when (position) {
        PlayerPosition.SETTER -> R.string.position_short_setter
        PlayerPosition.OUTSIDE_HITTER -> R.string.position_short_outside_hitter
        PlayerPosition.MIDDLE_BLOCKER -> R.string.position_short_middle_blocker
        PlayerPosition.OPPOSITE -> R.string.position_short_opposite
        PlayerPosition.LIBERO -> R.string.position_short_libero
    }
)

/**
 * Rótulo de uma vaga. Vagas específicas mostram a posição exigida; vagas por papel mostram uma
 * posição representativa do papel (armador, ataque ou defesa).
 */
@Composable
fun slotLabel(slot: TeamSlot): String {
    val position = slot.position ?: when (slot.role) {
        PositionRole.PLAYMAKER -> PlayerPosition.SETTER
        PositionRole.ATTACK -> PlayerPosition.OUTSIDE_HITTER
        PositionRole.DEFENSE -> PlayerPosition.MIDDLE_BLOCKER
    }
    return positionLabel(position)
}

/** Nome do tipo de grupo. */
@Composable
fun groupTypeLabel(type: GroupType): String = stringResource(
    when (type) {
        GroupType.RECREATIONAL -> R.string.group_type_recreational
        GroupType.FIXED_POSITIONS -> R.string.group_type_fixed_positions
        GroupType.TOURNAMENT_RECREATIONAL, GroupType.TOURNAMENT_PRO -> R.string.group_type_fixed_positions
    }
)

/** Descrição curta do tipo de grupo. */
@Composable
fun groupTypeDescription(type: GroupType): String = stringResource(
    when (type) {
        GroupType.RECREATIONAL -> R.string.group_type_recreational_desc
        GroupType.FIXED_POSITIONS -> R.string.group_type_fixed_positions_desc
        GroupType.TOURNAMENT_RECREATIONAL, GroupType.TOURNAMENT_PRO -> R.string.group_type_fixed_positions_desc
    }
)

/** Selo com a abreviação da posição ocupada pelo jogador na partida. */
@Composable
fun PositionBadge(
    position: PlayerPosition,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = positionShortLabel(position),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(contentColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/**
 * Indicador de composição de um time: lista cada vaga exigida com o jogador alocado ou o aviso de
 * vaga vazia. Não bloqueia nada — serve para orientar a montagem manual.
 */
@Composable
fun TeamCompositionIndicator(
    slots: List<PositionAssigner.FilledSlot>,
    modifier: Modifier = Modifier
) {
    if (slots.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.composition_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        slots.forEach { filled ->
            val isMissing = filled.player == null
            val color = when {
                isMissing -> MaterialTheme.colorScheme.error
                filled.isImprovised -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = slotLabel(filled.slot),
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = filled.player?.name ?: stringResource(R.string.composition_slot_empty),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isMissing) FontWeight.Normal else FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}

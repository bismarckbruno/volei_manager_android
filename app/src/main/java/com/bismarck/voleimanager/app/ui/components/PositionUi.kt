package com.bismarck.voleimanager.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.data.model.GroupType
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.PositionRole
import com.bismarck.voleimanager.app.data.model.TeamComposition
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

/** Ênfase visual do selo: a segunda posição preferida aparece esmaecida. */
enum class BadgeEmphasis { PRIMARY, SECONDARY }

/**
 * Borda do selo na partida ativa:
 * - [NONE]: o jogador está na sua posição preferida;
 * - [DASHED]: está na segunda posição preferida;
 * - [SOLID]: está improvisando (posição que não escolheu, ou não cadastrou nenhuma).
 */
enum class BadgeBorder { NONE, DASHED, SOLID }

/** Selo com a abreviação da posição ocupada pelo jogador na partida. */
@Composable
fun PositionBadge(
    position: PlayerPosition,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    emphasis: BadgeEmphasis = BadgeEmphasis.PRIMARY,
    border: BadgeBorder = BadgeBorder.NONE
) {
    val shape = RoundedCornerShape(8.dp)
    val secondary = emphasis == BadgeEmphasis.SECONDARY
    val textColor = if (secondary) contentColor.copy(alpha = 0.6f) else contentColor
    val backgroundAlpha = if (secondary) 0.06f else 0.12f

    Text(
        text = positionShortLabel(position),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = modifier
            .clip(shape)
            .background(contentColor.copy(alpha = backgroundAlpha))
            .positionBadgeBorder(border, textColor, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** Contorno contínuo ou tracejado do selo, desenhado sobre o fundo. */
private fun Modifier.positionBadgeBorder(
    border: BadgeBorder,
    color: Color,
    shape: RoundedCornerShape
): Modifier = when (border) {
    BadgeBorder.NONE -> this
    BadgeBorder.SOLID -> this.border(BorderStroke(1.dp, color), shape)
    BadgeBorder.DASHED -> this.drawBehind {
        val strokeWidth = 1.dp.toPx()
        val radius = CornerRadius(8.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = radius,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))
            )
        )
    }
}

/**
 * Posições cadastradas do jogador, lado a lado: primeiro a preferida, depois a segunda
 * (com menos ênfase). Não desenha nada fora dos tipos de grupo com posições fixas.
 */
@Composable
fun PlayerPositionBadges(
    player: Player,
    usesPositions: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (!usesPositions) return
    val preferred = PlayerPosition.fromStoredValue(player.preferredPosition)
    val secondary = PlayerPosition.fromStoredValue(player.secondaryPosition)
    if (preferred == null && secondary == null) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        preferred?.let { PositionBadge(position = it, contentColor = contentColor) }
        secondary?.let {
            PositionBadge(
                position = it,
                contentColor = contentColor,
                emphasis = BadgeEmphasis.SECONDARY
            )
        }
    }
}

/** Borda correspondente ao encaixe entre a posição escalada e as preferências do jogador. */
fun assignedPositionBorder(player: Player, assigned: PlayerPosition, teamSize: Int): BadgeBorder {
    val preferred = TeamComposition.effectivePosition(
        PlayerPosition.fromStoredValue(player.preferredPosition),
        teamSize
    )
    if (preferred == assigned) return BadgeBorder.NONE
    val secondary = TeamComposition.effectivePosition(
        PlayerPosition.fromStoredValue(player.secondaryPosition),
        teamSize
    )
    if (secondary == assigned) return BadgeBorder.DASHED
    return BadgeBorder.SOLID
}

/**
 * Texto explicativo da borda do selo, ou `null` quando o jogador está na posição preferida
 * (nesse caso não há nada a explicar e nenhuma tooltip deve ser aberta).
 */
@Composable
fun assignedPositionTooltip(player: Player, assigned: PlayerPosition, teamSize: Int): String? =
    when (assignedPositionBorder(player, assigned, teamSize)) {
        BadgeBorder.NONE -> null
        BadgeBorder.DASHED -> stringResource(
            R.string.position_badge_secondary_tooltip,
            positionLabel(assigned)
        )
        BadgeBorder.SOLID -> stringResource(
            R.string.position_badge_improvised_tooltip,
            positionLabel(assigned)
        )
    }

/** Selo da posição para a qual o jogador foi escalado, com a borda indicando o encaixe. */
@Composable
fun AssignedPositionBadge(
    player: Player,
    position: PlayerPosition,
    teamSize: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    PositionBadge(
        position = position,
        modifier = modifier,
        contentColor = contentColor,
        border = assignedPositionBorder(player, position, teamSize)
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

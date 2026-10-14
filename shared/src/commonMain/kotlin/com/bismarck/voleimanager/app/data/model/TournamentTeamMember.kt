package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vínculo entre um jogador e um time de campeonato.
 *
 * Substituições preservam o histórico: o vínculo antigo recebe [isActive] falso e [leftAt].
 */
@Entity(
    tableName = "tournament_team_members",
    indices = [
        Index(value = ["groupName", "teamId"]),
        Index(value = ["groupName", "playerId"])
    ]
)
data class TournamentTeamMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val teamId: Int,
    val playerId: Int,
    /** Posição ocupada no time ([PlayerPosition]); nulo nos campeonatos sem posições fixas. */
    val position: String? = null,
    val isActive: Boolean = true,
    val joinedAt: Long = 0L,
    val leftAt: Long? = null
)

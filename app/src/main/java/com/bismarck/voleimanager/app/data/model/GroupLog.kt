package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tipo de evento registrado no log do grupo. */
enum class GroupLogType {
    SUBSTITUTION,
    TEAM_EDIT,
    PLAYER_MOVED,
    TEAM_CREATED,
    TEAM_REMOVED;

    companion object {
        fun fromStoredValue(value: String?): GroupLogType? =
            entries.firstOrNull { it.name == value }
    }
}

/**
 * Registro persistente de eventos do grupo com data e horário.
 *
 * Usado principalmente pelos tipos de campeonato, onde substituições e trocas entre times podem
 * ocorrer a qualquer momento e precisam ficar auditáveis.
 */
@Entity(
    tableName = "group_logs",
    indices = [Index(value = ["groupName", "timestamp"])]
)
data class GroupLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val timestamp: Long,
    /** Data no formato yyyy-MM-dd, para agrupamento por dia. */
    val date: String,
    val type: String,
    val message: String,
    val playerId: Int? = null,
    val teamId: Int? = null,
    val relatedTeamId: Int? = null,
    val metadata: String? = null
)

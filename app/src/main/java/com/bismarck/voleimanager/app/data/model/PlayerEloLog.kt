package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "elo_logs",
    indices = [
        Index(value = ["playerId"]),
        Index(value = ["groupName", "date"]),
        Index(value = ["groupName", "playerId", "date"])
    ]
)
data class PlayerEloLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val playerNameSnapshot: String,
    val date: String,
    val elo: Double,
    val groupName: String,
    val won: Boolean? = null,
    /** Id da [com.bismarck.voleimanager.app.data.model.MatchHistory] que gerou este log — nulo
     *  em registros anteriores a essa coluna. Permite localizar com precisão os logs de uma
     *  partida específica (ex.: para "Desfazer última vitória"), já que [date] só guarda o dia,
     *  insuficiente quando várias partidas acontecem no mesmo dia. */
    val matchHistoryId: Int? = null
)

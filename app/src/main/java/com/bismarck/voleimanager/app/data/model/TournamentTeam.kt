package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Time nomeado dos tipos de campeonato.
 *
 * [teamKey] guarda apenas o sufixo gerado em ordem alfabética ("A", "B", "C"...), sem a palavra
 * "Time". [customName] sobrepõe o nome padrão quando o usuário personaliza o time.
 */
@Entity(
    tableName = "tournament_teams",
    indices = [Index(value = ["groupName", "teamKey"], unique = true)]
)
data class TournamentTeam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val teamKey: String,
    val customName: String? = null,
    val seed: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = 0L
)

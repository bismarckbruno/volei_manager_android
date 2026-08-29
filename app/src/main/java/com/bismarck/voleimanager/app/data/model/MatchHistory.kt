package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_history",
    indices = [Index(value = ["groupName", "id"])]
)
data class MatchHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val teamA: String,
    val teamB: String,
    val teamAIds: String = "",
    val teamBIds: String = "",
    val winner: String,
    val eloPoints: Double,
    val groupName: String,
    val teamAAverageElo: Double? = null,
    val teamBAverageElo: Double? = null,
    val teamAScore: Int? = null,
    val teamBScore: Int? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    /** Id do time de campeonato (tournament_teams) que jogou como Time A, quando aplicável. */
    val teamAId: Int? = null,
    /** Id do time de campeonato (tournament_teams) que jogou como Time B, quando aplicável. */
    val teamBId: Int? = null,
    /** Nome exibido do time A no momento da partida (snapshot). */
    val teamALabel: String? = null,
    /** Nome exibido do time B no momento da partida (snapshot). */
    val teamBLabel: String? = null,
    /**
     * Formato de disputa desta partida ([MatchFormat] serializado). Nulo em partidas antigas
     * (equivalentes a BO1). Preparação para BO3/BO5 — ainda não usado pela engine do jogo.
     */
    val matchFormat: String? = null,
    /** Placar de cada set, serializado como "25-22;23-25;15-12" (nulo fora do fluxo de sets). */
    val setScores: String? = null,
    /** Sets vencidos pelo Time A (nulo fora do fluxo de sets; BO1 continua usando teamAScore). */
    val teamASetsWon: Int? = null,
    /** Sets vencidos pelo Time B (nulo fora do fluxo de sets; BO1 continua usando teamBScore). */
    val teamBSetsWon: Int? = null
)

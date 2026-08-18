package com.bismarck.voleimanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Fase do chaveamento. */
enum class TournamentPhase {
    GROUP_STAGE,
    KNOCKOUT,
    ROUND_ROBIN;

    companion object {
        fun fromStoredValue(value: String?): TournamentPhase? =
            entries.firstOrNull { it.name == value }
    }
}

/** Situação de uma partida do chaveamento. */
enum class TournamentMatchStatus {
    PENDING,
    IN_PROGRESS,
    FINISHED,
    BYE;

    companion object {
        fun fromStoredValue(value: String?): TournamentMatchStatus =
            entries.firstOrNull { it.name == value } ?: PENDING
    }
}

/**
 * Partida do chaveamento de um grupo de campeonato.
 *
 * A estrutura é genérica (fase / rodada / ordem) para suportar mata-mata, pontos corridos e fase
 * de grupos. [homeSourceMatchId] e [awaySourceMatchId] ligam a partida ao confronto anterior cujo
 * vencedor a alimenta, permitindo montar o chaveamento antes de os times serem conhecidos.
 */
@Entity(
    tableName = "tournament_matches",
    indices = [Index(value = ["groupName", "phase", "roundIndex", "orderInRound"])]
)
data class TournamentMatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val phase: String = TournamentPhase.KNOCKOUT.name,
    /** Rótulo livre da fase: "Quartas", "Semifinal", "Grupo A"... */
    val phaseLabel: String? = null,
    val roundIndex: Int = 0,
    val orderInRound: Int = 0,
    val homeTeamId: Int? = null,
    val awayTeamId: Int? = null,
    val homeSourceMatchId: Int? = null,
    val awaySourceMatchId: Int? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winnerTeamId: Int? = null,
    val status: String = TournamentMatchStatus.PENDING.name,
    /** Id da partida correspondente em match_history, quando concluída. */
    val matchHistoryId: Int? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null
)

package com.bismarck.voleimanager.app.ui.viewmodel

import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.GroupLog
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import com.bismarck.voleimanager.app.data.model.TournamentMatch
import com.bismarck.voleimanager.app.data.model.TournamentTeam
import com.bismarck.voleimanager.app.data.model.TournamentTeamMember
import kotlinx.serialization.Serializable

// Estruturas persistidas como JSON (backup .vlz exportado e estado salvo nas preferências),
// serializadas com [com.bismarck.voleimanager.app.util.AppJson]. Não renomear propriedades: os
// nomes fazem parte do formato já gravado nos aparelhos e nos backups dos usuários.

/** Conteúdo de um backup completo (.vlz). */
@Serializable
data class BackupData(
    val version: Int = 1,
    val date: String,
    val players: List<Player>,
    val history: List<MatchHistory>,
    val logs: List<PlayerEloLog>,
    /** Configuração do grupo exportado (inclui o tipo de grupo). Nulo em backups antigos. */
    val groupConfig: GroupConfig? = null,
    val tournamentTeams: List<TournamentTeam>? = null,
    val tournamentTeamMembers: List<TournamentTeamMember>? = null,
    val tournamentMatches: List<TournamentMatch>? = null,
    val groupLogs: List<GroupLog>? = null
)

@Serializable
data class GameStateSnapshot(
    val groupName: String,
    val teamA: List<Player>,
    val teamB: List<Player>,
    val waitingList: List<Player>,
    val presentPlayerIds: List<Int>,
    val scoreA: Int,
    val scoreB: Int,
    val currentStreak: Int,
    val streakOwner: String?,
    val hasPreviousMatch: Boolean,
    val lastWinners: List<Player>,
    val lastLosers: List<Player>,
    val currentMatchStartTimestamp: Long? = null,
    val roundCounter: Int = 0,
    val restingPlayers: Map<Int, Int> = emptyMap(),
    val rebalancedPlayerIds: List<Int> = emptyList(),
    val autoSelectedLoserPlayerIds: List<Int> = emptyList(),
    val guaranteedNextMatchPlayerIds: List<Int> = emptyList(),
    val lastScoringTeam: String? = null,
    val rotationRequiredForTeam: String? = null,
    /** Posição ocupada por cada jogador na partida (Modo Posições Fixas). playerId -> nome do enum. */
    val assignedPositions: Map<Int, String> = emptyMap(),
    /** Índice da vaga ocupada por cada jogador dentro do próprio time (0 = topo do card base). */
    val assignedSlotIndices: Map<Int, Int> = emptyMap(),
    val compositionIncomplete: Boolean = false
)

/**
 * `undo-last-match`: guardado por até [UNDO_MATCH_WINDOW_MILLIS] após [VoleiViewModel.finishGame]
 * para permitir desfazer a partida com fidelidade total — tanto o estado local de sequência de
 * vitórias de antes da partida (que não é persistido em nenhuma outra tabela) quanto os ids dos
 * documentos remotos criados (para apagá-los junto, se o grupo for sincronizado em nuvem). Só é
 * válido enquanto [matchId] ainda for o id da última [MatchHistory] do grupo e [expiresAt] não
 * tiver passado — ver [VoleiViewModel.refreshCanUndoLastMatch] / [VoleiViewModel.undoLastMatch].
 */
@Serializable
data class UndoMatchSnapshot(
    val groupName: String,
    val matchId: Int,
    val previousStreak: Int,
    val previousStreakOwner: String?,
    val previousLastWinnerIds: List<Int>,
    val previousLastLoserIds: List<Int>,
    val previousHasPreviousMatch: Boolean,
    val cloudGroupId: String? = null,
    val cloudHistoryDocId: String? = null,
    val cloudEloLogDocIds: List<String> = emptyList(),
    val expiresAt: Long
)

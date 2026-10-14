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
    val createdAt: Long = 0L,
    /**
     * Agregados de classificação (fase de grupos / pontos corridos), atualizados a cada partida
     * finalizada — mesmo padrão de [Player.matchesPlayed]/[Player.victories]. Preparação de
     * terreno para a futura tabela de classificação (sistema FIVB de 3 pontos); ainda não
     * calculados/atualizados por nenhuma lógica hoje.
     */
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    /** Sets vencidos/perdidos somados de todas as partidas — usado no critério "set average". */
    val setsWon: Int = 0,
    val setsLost: Int = 0,
    /**
     * Pontos feitos/sofridos somados de todas as partidas. Usado tanto no "point average"
     * (torneios BO3/BO5) quanto no saldo de pontos (torneios amadores de set único, BO1).
     */
    val pointsWon: Int = 0,
    val pointsLost: Int = 0,
    /** Pontos de classificação já acumulados (3/2/1/0 no sistema FIVB, ou 3/0 em set único). */
    val tournamentPoints: Int = 0
)

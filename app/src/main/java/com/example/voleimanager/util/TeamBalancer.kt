package com.example.voleimanager.util

import com.example.voleimanager.data.model.Player

object TeamBalancer {

    data class BalancedResult(
        val teamA: List<Player>,
        val teamB: List<Player>
    )

    /**
     * Distribui os jogadores tentando equilibrar a soma total dos Elos.
     * Prioriza alocar Levantadores primeiro, depois os demais.
     */
    fun createBalancedTeams(
        availablePlayers: List<Player>, // Todos os presentes
        teamSize: Int,
        preFilledA: List<Player> = emptyList(), // Para caso de Streak (Reis da quadra)
        preFilledB: List<Player> = emptyList()
    ): BalancedResult {

        // Listas mutáveis para ir montando os times
        val teamA = preFilledA.toMutableList()
        val teamB = preFilledB.toMutableList()

        // Remove do pool quem já está pré-alocado
        val playersToDistribute = availablePlayers.filter { player ->
            player !in teamA && player !in teamB
        }

        // Separa levantadores e outros, ordenando por Elo (Do maior para o menor)
        val (setters, others) = playersToDistribute.partition { it.isSetter }
        val sortedSetters = setters.sortedByDescending { it.elo }
        val sortedOthers = others.sortedByDescending { it.elo }

        // Função auxiliar para decidir em qual time o jogador entra
        fun allocatePlayer(player: Player) {
            val isAFull = teamA.size >= teamSize
            val isBFull = teamB.size >= teamSize

            when {
                // Se ambos têm vaga, vai para o time mais "fraco" (menor Elo somado)
                !isAFull && !isBFull -> {
                    val sumA = teamA.sumOf { it.elo }
                    val sumB = teamB.sumOf { it.elo }
                    if (sumA <= sumB) teamA.add(player) else teamB.add(player)
                }
                // Se só A tem vaga
                !isAFull -> teamA.add(player)
                // Se só B tem vaga
                !isBFull -> teamB.add(player)
                // Se ambos cheios (sobra na fila de espera, ignorado aqui)
                else -> { /* Não faz nada, fica na fila */ }
            }
        }

        // 1. Distribui Levantadores primeiro (para garantir 1 em cada lado se possível)
        sortedSetters.forEach { allocatePlayer(it) }

        // 2. Distribui o resto do pessoal
        sortedOthers.forEach { allocatePlayer(it) }

        return BalancedResult(teamA, teamB)
    }
}
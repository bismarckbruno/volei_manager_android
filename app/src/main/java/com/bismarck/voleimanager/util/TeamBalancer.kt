package com.bismarck.voleimanager.util

import com.bismarck.voleimanager.data.model.Player

object TeamBalancer {

    data class BalancedResult(
        val teamA: List<Player>,
        val teamB: List<Player>
    )

    /**
     * Distribui os jogadores tentando equilibrar a soma total dos Elos.
     * Alocar prioridades e depois os demais.
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

        // Separa prioridades e outros, ordenando por Elo (Do maior para o menor)
        val (setters, others) = playersToDistribute.partition { it.isPriority }
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

        // 1. Distribui prioridades primeiro (para garantir 1 em cada lado, se possível)
        sortedSetters.forEach { allocatePlayer(it) }

        // 2. Distribui o restante do pessoal
        sortedOthers.forEach { allocatePlayer(it) }

        return BalancedResult(teamA, teamB)
    }

    /**
     * Intercala jogadores por Elo em zigzag (alto-baixo-alto-baixo...).
     *
     * Garante que qualquer prefixo da lista resultante contenha
     * uma mistura equilibrada de jogadores fortes e fracos.
     *
     * Algoritmo: ordena por Elo decrescente, depois pega alternadamente
     * da frente (mais forte) e de trás (mais fraco).
     */
    fun interleaveByElo(players: List<Player>): List<Player> {
        if (players.size <= 1) return players
        val sorted = players.sortedByDescending { it.elo }
        val result = mutableListOf<Player>()
        var lo = 0
        var hi = sorted.lastIndex
        var takeHigh = true
        while (lo <= hi) {
            if (takeHigh) result.add(sorted[lo++]) else result.add(sorted[hi--])
            takeHigh = !takeHigh
        }
        return result
    }

    /**
     * Agrupa jogadores pela chave fornecida (ex: effectiveGames),
     * ordena os grupos em ordem crescente da chave,
     * e dentro de cada grupo aplica [interleaveByElo].
     *
     * Resultado: lista onde jogadores com menor chave vêm primeiro (prioridade de jogo),
     * e dentro de cada faixa a diversidade de Elo é garantida.
     */
    fun groupAndInterleave(players: List<Player>, keySelector: (Player) -> Int): List<Player> {
        return players
            .groupBy { keySelector(it) }
            .toSortedMap()
            .flatMap { (_, tier) -> interleaveByElo(tier) }
    }
}
package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import kotlin.math.roundToInt

/**
 * Lógica pura de cálculo de pedágio (toll).
 *
 * O pedágio é a média de jogos efetivos dos jogadores já presentes,
 * atribuída a um jogador que chega atrasado no dia de jogo.
 * Ele "reseta" diariamente (comparando [tollDate] com a data atual).
 */
object TollCalculator {

    /**
     * Retorna o número efetivo de jogos de um jogador:
     *   jogos reais disputados hoje  +  pedágio (se [Player.tollDate] == [today]).
     *
     * Se a data do pedágio não bate com hoje, o pedágio é descartado (reset diário).
     */
    fun getEffectiveGames(player: Player, actualGamesToday: Int, today: String): Int {
        val toll = if (player.tollDate == today) player.dailyToll else 0
        return actualGamesToday + toll
    }

    /**
     * Calcula o pedágio para um jogador que acabou de chegar.
     *
     * @param presentPlayers  jogadores já presentes no grupo (incluindo o próprio, se quiser excluí-lo).
     * @param usageMap         mapa [playerId → jogos disputados hoje] (obtido dos EloLogs do dia).
     * @param today            data atual no formato "yyyy-MM-dd".
     * @param excludePlayerId  id do jogador que está chegando (excluído da média).
     *
     * @return média arredondada dos jogos efetivos dos presentes, ou 0 se não houver presentes.
     */
    fun calculateToll(
        presentPlayers: List<Player>,
        usageMap: Map<Int, Int>,
        today: String,
        excludePlayerId: Int? = null
    ): Int {
        val filtered = if (excludePlayerId != null) {
            presentPlayers.filter { it.id != excludePlayerId }
        } else {
            presentPlayers
        }
        if (filtered.isEmpty()) return 0

        val sum = filtered.sumOf { p ->
            getEffectiveGames(p, usageMap[p.id] ?: 0, today)
        }
        return (sum.toDouble() / filtered.size).roundToInt()
    }

    /**
     * Verifica se o pedágio precisa ser (re)calculado para o jogador
     * (ou seja, se ele ainda não tem pedágio do dia de hoje).
     */
    fun shouldApplyToll(player: Player, today: String): Boolean {
        return player.tollDate != today
    }

    /**
     * Retorna uma cópia do jogador com o pedágio atualizado para hoje,
     * ou o próprio jogador inalterado se já tiver pedágio do dia.
     */
    fun applyToll(
        player: Player,
        presentPlayers: List<Player>,
        usageMap: Map<Int, Int>,
        today: String
    ): Player {
        if (!shouldApplyToll(player, today)) return player
        val toll = calculateToll(presentPlayers, usageMap, today, excludePlayerId = player.id)
        return player.copy(dailyToll = toll, tollDate = today)
    }
}

package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

object EloCalculator {

    private const val K_FACTOR = 32.0

    /**
     * Calcula quantos pontos o vencedor deve ganhar (e o perdedor perder).
     */
    fun calculateEloChange(winnerAvgElo: Double, loserAvgElo: Double): Double {
        // 1. Calcular a expectativa de vitória (0.0 a 1.0)
        // Fórmula: 1 / (1 + 10 ^ ((EloPerdedor - EloVencedor) / 400))
        val exponent = (loserAvgElo - winnerAvgElo) / 400.0
        val expectedScore = 1.0 / (1.0 + 10.0.pow(exponent))

        // 2. Calcular o Delta (Variação)
        // O vencedor "ganhou" (score = 1), então a fórmula é K * (1 - expectativa)
        val delta = K_FACTOR * (1.0 - expectedScore)

        return delta
    }

    /**
     * Calcula o delta Elo individual de um jogador contra a média do time adversário.
     */
    fun calculateIndividualEloChange(
        playerElo: Double,
        opponentTeamAvgElo: Double,
        won: Boolean
    ): Double {
        val exponent = (opponentTeamAvgElo - playerElo) / 400.0
        val expectedScore = 1.0 / (1.0 + 10.0.pow(exponent))
        val actualScore = if (won) 1.0 else 0.0
        return K_FACTOR * (actualScore - expectedScore)
    }

    /**
     * Calcula deltas individuais normalizados para preservar o total (zero-sum).
     * sum(abs(deltas)) == flatDelta × players.size
     */
    fun calculateNormalizedDeltas(
        players: List<Player>,
        opponentTeamAvgElo: Double,
        won: Boolean,
        flatDelta: Double
    ): List<Double> {
        val rawDeltas = players.map { calculateIndividualEloChange(it.elo, opponentTeamAvgElo, won) }
        val rawAbsSum = rawDeltas.sumOf { abs(it) }
        val targetAbsSum = flatDelta * players.size
        val scale = if (rawAbsSum > 0.0) targetAbsSum / rawAbsSum else 1.0
        return rawDeltas.map { it * scale }
    }

    /**
     * Formata o Elo para exibição com separador de milhar.
     */
    fun formatElo(elo: Double): String {
        return NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(elo)
    }
}

package com.example.voleimanager.util

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
}
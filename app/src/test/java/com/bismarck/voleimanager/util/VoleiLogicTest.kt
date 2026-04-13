package com.bismarck.voleimanager.util

import com.bismarck.voleimanager.data.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoleiLogicTest {

    // --- TESTES DE ELO ---
    @Test
    fun eloCalculation_equalTeams_returnsHalfK() {
        // Se times são iguais, chance é 50%. Ganho deve ser K * (1 - 0.5) = 16
        // K-Factor default é 32.0
        val delta = EloCalculator.calculateEloChange(1200.0, 1200.0)
        assertEquals(16.0, delta, 0.01)
    }

    @Test
    fun eloCalculation_strongWinner_returnsSmallDelta() {
        // Time forte (2000) ganha de fraco (1000) -> Ganha pouco
        val delta = EloCalculator.calculateEloChange(2000.0, 1000.0)
        // Expectativa do forte ganhar é quase 100%. Delta deve ser pequeno.
        assertTrue("Delta deve ser pequeno para vitória óbvia", delta < 2.0)
    }

    @Test
    fun eloCalculation_weakWinner_returnsBigDelta() {
        // Time fraco (1000) ganha de forte (2000) -> Ganha muito
        val delta = EloCalculator.calculateEloChange(1000.0, 2000.0)
        // Expectativa do fraco ganhar é quase 0%. Delta deve ser perto de 32.
        assertTrue("Delta deve ser grande para zebra", delta > 30.0)
    }

    // --- TESTES DE BALANCEAMENTO ---
    @Test
    fun teamBalancer_distributesEvenly() {
        val p1 = Player(id=1, name="Fraco 1", elo=1000.0, groupName="G")
        val p2 = Player(id=2, name="Forte 1", elo=2000.0, groupName="G")
        val p3 = Player(id=3, name="Fraco 2", elo=1000.0, groupName="G")
        val p4 = Player(id=4, name="Forte 2", elo=2000.0, groupName="G")
        
        val pool = listOf(p1, p2, p3, p4)
        // Times de 2 jogadores
        val result = TeamBalancer.createBalancedTeams(pool, 2)
        
        // Deve colocar um forte em cada time para equilibrar (1000+2000)/2 = 1500
        val avgA = result.teamA.map { it.elo }.average()
        val avgB = result.teamB.map { it.elo }.average()
        
        assertEquals("Média do Time A deve ser 1500", 1500.0, avgA, 0.1)
        assertEquals("Média do Time B deve ser 1500", 1500.0, avgB, 0.1)
        assertEquals("Time A deve ter 2 jogadores", 2, result.teamA.size)
        assertEquals("Time B deve ter 2 jogadores", 2, result.teamB.size)
    }

    @Test
    fun teamBalancer_respectsTeamSize() {
        val players = (1..10).map { Player(id=it, name="P$it", elo=1200.0, groupName="G") }
        // Pede times de 3
        val result = TeamBalancer.createBalancedTeams(players, 3)
        
        assertEquals(3, result.teamA.size)
        assertEquals(3, result.teamB.size)
        // Sobram 4 (10 - 6)
    }

    // --- TESTES DE ELO INDIVIDUAL ---
    @Test
    fun individualElo_strongPlayerWins_getsSmallDelta() {
        val delta = EloCalculator.calculateIndividualEloChange(1400.0, 1200.0, true)
        assertTrue("Jogador forte deve ganhar pouco", delta < 10.0)
        assertTrue("Delta deve ser positivo", delta > 0.0)
    }

    @Test
    fun individualElo_weakPlayerWins_getsBigDelta() {
        val delta = EloCalculator.calculateIndividualEloChange(1000.0, 1200.0, true)
        assertTrue("Jogador fraco deve ganhar muito", delta > 20.0)
    }

    @Test
    fun individualElo_strongPlayerLoses_losesMore() {
        val delta = EloCalculator.calculateIndividualEloChange(1400.0, 1200.0, false)
        assertTrue("Jogador forte deve perder muito", delta < -20.0)
    }

    @Test
    fun individualElo_weakPlayerLoses_losesLess() {
        val delta = EloCalculator.calculateIndividualEloChange(1000.0, 1200.0, false)
        assertTrue("Jogador fraco deve perder pouco", delta > -10.0)
        assertTrue("Delta deve ser negativo", delta < 0.0)
    }

    @Test
    fun normalizedDeltas_sumMatchesFlatTotal() {
        val players = listOf(
            Player(id=1, name="Forte", elo=1400.0, groupName="G"),
            Player(id=2, name="Médio", elo=1200.0, groupName="G"),
            Player(id=3, name="Fraco", elo=1000.0, groupName="G")
        )
        val flatDelta = 16.0
        val opponentAvg = 1200.0

        val winDeltas = EloCalculator.calculateNormalizedDeltas(players, opponentAvg, true, flatDelta)
        assertEquals("Soma dos ganhos deve ser flatDelta × tamanho", flatDelta * 3, winDeltas.sum(), 0.01)

        val loseDeltas = EloCalculator.calculateNormalizedDeltas(players, opponentAvg, false, flatDelta)
        assertEquals("Soma das perdas deve ser -flatDelta × tamanho", -flatDelta * 3, loseDeltas.sum(), 0.01)
    }

    @Test
    fun normalizedDeltas_equalPlayersGetEqualDeltas() {
        val players = listOf(
            Player(id=1, name="P1", elo=1200.0, groupName="G"),
            Player(id=2, name="P2", elo=1200.0, groupName="G")
        )
        val flatDelta = 16.0
        val deltas = EloCalculator.calculateNormalizedDeltas(players, 1200.0, true, flatDelta)
        assertEquals(16.0, deltas[0], 0.01)
        assertEquals(16.0, deltas[1], 0.01)
    }
}
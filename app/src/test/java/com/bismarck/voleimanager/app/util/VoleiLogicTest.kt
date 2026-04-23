package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
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

    // --- TESTES DE INTERCALAÇÃO POR ELO ---

    @Test
    fun interleaveByElo_zigzagOrder() {
        // 6 jogadores com Elos distintos
        val players = listOf(
            Player(id=1, name="P1", elo=1433.0, groupName="G"),
            Player(id=2, name="P2", elo=1326.0, groupName="G"),
            Player(id=3, name="P3", elo=1314.0, groupName="G"),
            Player(id=4, name="P4", elo=1265.0, groupName="G"),
            Player(id=5, name="P5", elo=1074.0, groupName="G"),
            Player(id=6, name="P6", elo=1024.0, groupName="G")
        )
        val result = TeamBalancer.interleaveByElo(players)

        // Deve alternar: mais forte, mais fraco, 2° mais forte, 2° mais fraco...
        assertEquals("Posição 0 deve ser o mais forte", 1433.0, result[0].elo, 0.01)
        assertEquals("Posição 1 deve ser o mais fraco", 1024.0, result[1].elo, 0.01)
        assertEquals("Posição 2 deve ser o 2° mais forte", 1326.0, result[2].elo, 0.01)
        assertEquals("Posição 3 deve ser o 2° mais fraco", 1074.0, result[3].elo, 0.01)
        assertEquals("Posição 4 deve ser o 3° mais forte", 1314.0, result[4].elo, 0.01)
        assertEquals("Posição 5 deve ser o restante", 1265.0, result[5].elo, 0.01)
    }

    @Test
    fun interleaveByElo_preservesAllPlayers() {
        val players = (1..7).map { Player(id=it, name="P$it", elo=1000.0 + it * 100.0, groupName="G") }
        val result = TeamBalancer.interleaveByElo(players)

        assertEquals("Deve manter todos os jogadores", players.size, result.size)
        assertEquals(
            "Deve conter os mesmos IDs",
            players.map { it.id }.toSet(),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun interleaveByElo_singlePlayer_returnsSame() {
        val player = Player(id=1, name="Solo", elo=1200.0, groupName="G")
        val result = TeamBalancer.interleaveByElo(listOf(player))
        assertEquals(1, result.size)
        assertEquals(1200.0, result[0].elo, 0.01)
    }

    @Test
    fun interleaveByElo_emptyList_returnsEmpty() {
        val result = TeamBalancer.interleaveByElo(emptyList())
        assertTrue("Lista vazia deve retornar vazia", result.isEmpty())
    }

    @Test
    fun interleaveByElo_twoPlayers_highThenLow() {
        val players = listOf(
            Player(id=1, name="Fraco", elo=1000.0, groupName="G"),
            Player(id=2, name="Forte", elo=1400.0, groupName="G")
        )
        val result = TeamBalancer.interleaveByElo(players)
        assertEquals("Primeiro deve ser o forte", 1400.0, result[0].elo, 0.01)
        assertEquals("Segundo deve ser o fraco", 1000.0, result[1].elo, 0.01)
    }

    @Test
    fun interleaveByElo_anyPrefixHasBalancedAverage() {
        // Propriedade fundamental: qualquer prefixo de tamanho >= 2 deve ter
        // média próxima da média total (dentro de margem razoável)
        val players = listOf(
            Player(id=1, name="P1", elo=1433.0, groupName="G"),
            Player(id=2, name="P2", elo=1326.0, groupName="G"),
            Player(id=3, name="P3", elo=1314.0, groupName="G"),
            Player(id=4, name="P4", elo=1265.0, groupName="G"),
            Player(id=5, name="P5", elo=1074.0, groupName="G"),
            Player(id=6, name="P6", elo=1024.0, groupName="G")
        )
        val totalAvg = players.map { it.elo }.average()  // ~1239.3
        val result = TeamBalancer.interleaveByElo(players)

        // Cada prefixo de tamanho 2+ deve ter média dentro de ±15% da média total
        for (n in 2..result.size) {
            val prefixAvg = result.take(n).map { it.elo }.average()
            val deviation = kotlin.math.abs(prefixAvg - totalAvg) / totalAvg
            assertTrue(
                "Prefixo de tamanho $n (avg=${"%.1f".format(prefixAvg)}) deve estar próximo da média total (${"%.1f".format(totalAvg)}). Desvio: ${"%.1f".format(deviation * 100)}%",
                deviation < 0.15
            )
        }
    }

    @Test
    fun groupAndInterleave_respectsTierOrder() {
        // Jogadores com effectiveGames diferentes
        // Tier 2: P1(1400), P2(1000)
        // Tier 4: P3(1350), P4(1050), P5(1200)
        val p1 = Player(id=1, name="P1", elo=1400.0, groupName="G")
        val p2 = Player(id=2, name="P2", elo=1000.0, groupName="G")
        val p3 = Player(id=3, name="P3", elo=1350.0, groupName="G")
        val p4 = Player(id=4, name="P4", elo=1050.0, groupName="G")
        val p5 = Player(id=5, name="P5", elo=1200.0, groupName="G")

        // Simula effectiveGames via mapa
        val gamesMap = mapOf(1 to 2, 2 to 2, 3 to 4, 4 to 4, 5 to 4)

        val result = TeamBalancer.groupAndInterleave(listOf(p1, p2, p3, p4, p5)) { gamesMap[it.id] ?: 0 }

        // Tier 2 deve vir antes de Tier 4
        val tier2Ids = setOf(1, 2)
        val firstTier2Index = result.indexOfFirst { it.id in tier2Ids }
        val lastTier2Index = result.indexOfLast { it.id in tier2Ids }
        val firstTier4Index = result.indexOfFirst { it.id !in tier2Ids }

        assertTrue("Tier com menos jogos deve vir primeiro", lastTier2Index < firstTier4Index)

        // Dentro de tier 2: intercalado (forte, fraco)
        assertEquals("Tier 2 pos 0 deve ser o forte", 1400.0, result[0].elo, 0.01)
        assertEquals("Tier 2 pos 1 deve ser o fraco", 1000.0, result[1].elo, 0.01)

        // Dentro de tier 4: intercalado (forte, fraco, médio)
        assertEquals("Tier 4 pos 0 deve ser o mais forte", 1350.0, result[2].elo, 0.01)
        assertEquals("Tier 4 pos 1 deve ser o mais fraco", 1050.0, result[3].elo, 0.01)
        assertEquals("Tier 4 pos 2 deve ser o restante", 1200.0, result[4].elo, 0.01)
    }

    @Test
    fun groupAndInterleave_singleTier_sameAsInterleave() {
        // Se todos têm o mesmo effectiveGames, deve ser igual a interleaveByElo
        val players = listOf(
            Player(id=1, name="P1", elo=1400.0, groupName="G"),
            Player(id=2, name="P2", elo=1200.0, groupName="G"),
            Player(id=3, name="P3", elo=1000.0, groupName="G")
        )
        val grouped = TeamBalancer.groupAndInterleave(players) { 3 } // todos com games=3
        val interleaved = TeamBalancer.interleaveByElo(players)

        assertEquals(
            "Se tier único, groupAndInterleave == interleaveByElo",
            interleaved.map { it.id },
            grouped.map { it.id }
        )
    }

    @Test
    fun groupAndInterleave_emptyList_returnsEmpty() {
        val result = TeamBalancer.groupAndInterleave(emptyList()) { 0 }
        assertTrue(result.isEmpty())
    }
}



package com.bismarck.voleimanager.util

import com.bismarck.voleimanager.data.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para a lógica de pedágio (toll) do Vôlei Manager.
 *
 * O pedágio é a penalidade atribuída a jogadores que chegam atrasados:
 * eles recebem como "jogos extras" a média de jogos efetivos dos presentes,
 * de forma que entrem na fila de espera como se já tivessem jogado.
 *
 * Roda com:  ./gradlew test
 */
class TollCalculatorTest {

    private val today = "2026-04-12"
    private val yesterday = "2026-04-11"

    // ──────────────────────────────────────────────────────────────────────
    // Helper: cria jogador com valores padrão
    // ──────────────────────────────────────────────────────────────────────
    private fun player(
        id: Int,
        name: String = "P$id",
        dailyToll: Int = 0,
        tollDate: String = ""
    ) = Player(
        id = id,
        name = name,
        elo = 1200.0,
        groupName = "Geral",
        dailyToll = dailyToll,
        tollDate = tollDate
    )

    // ======================================================================
    //  1. getEffectiveGames
    // ======================================================================

    @Test
    fun effectiveGames_noToll_returnsActualGamesOnly() {
        val p = player(1)
        assertEquals(3, TollCalculator.getEffectiveGames(p, actualGamesToday = 3, today))
    }

    @Test
    fun effectiveGames_withTollToday_addsToll() {
        val p = player(1, dailyToll = 4, tollDate = today)
        // 2 jogos reais + 4 de pedágio = 6
        assertEquals(6, TollCalculator.getEffectiveGames(p, actualGamesToday = 2, today))
    }

    @Test
    fun effectiveGames_withTollFromYesterday_ignoresToll() {
        val p = player(1, dailyToll = 4, tollDate = yesterday)
        // Pedágio de ontem não conta → apenas jogos reais
        assertEquals(2, TollCalculator.getEffectiveGames(p, actualGamesToday = 2, today))
    }

    @Test
    fun effectiveGames_zeroActualZeroToll_returnsZero() {
        val p = player(1)
        assertEquals(0, TollCalculator.getEffectiveGames(p, actualGamesToday = 0, today))
    }

    @Test
    fun effectiveGames_tollDateEmpty_ignoresToll() {
        val p = player(1, dailyToll = 5, tollDate = "")
        assertEquals(3, TollCalculator.getEffectiveGames(p, actualGamesToday = 3, today))
    }

    // ======================================================================
    //  2. calculateToll
    // ======================================================================

    @Test
    fun calculateToll_noPresentPlayers_returnsZero() {
        val result = TollCalculator.calculateToll(
            presentPlayers = emptyList(),
            usageMap = emptyMap(),
            today = today
        )
        assertEquals(0, result)
    }

    @Test
    fun calculateToll_singlePresentWithZeroGames_returnsZero() {
        val present = listOf(player(1))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 0), today)
        assertEquals(0, result)
    }

    @Test
    fun calculateToll_singlePresentWith3Games_returns3() {
        val present = listOf(player(1))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 3), today)
        assertEquals(3, result)
    }

    @Test
    fun calculateToll_twoPresentDifferentGames_returnsAverage() {
        // Jogador 1: 2 jogos | Jogador 2: 4 jogos → média = 3
        val present = listOf(player(1), player(2))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 2, 2 to 4), today)
        assertEquals(3, result)
    }

    @Test
    fun calculateToll_roundsUpFromHalf() {
        // 3 jogadores: 1, 2, 3 jogos → soma 6, média 2.0 (exata)
        val present = listOf(player(1), player(2), player(3))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 1, 2 to 2, 3 to 3), today)
        assertEquals(2, result)
    }

    @Test
    fun calculateToll_roundsCorrectly_fractionalAboveHalf() {
        // 3 jogadores: 1, 2, 4 jogos → soma 7, média ≈ 2.33 → arredonda para 2
        val present = listOf(player(1), player(2), player(3))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 1, 2 to 2, 3 to 4), today)
        assertEquals(2, result)
    }

    @Test
    fun calculateToll_roundsCorrectly_fractionalBelowHalf() {
        // 3 jogadores: 2, 3, 4 jogos → soma 9, média 3.0
        val present = listOf(player(1), player(2), player(3))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 2, 2 to 3, 3 to 4), today)
        assertEquals(3, result)
    }

    @Test
    fun calculateToll_roundsHalfUp() {
        // 2 jogadores: 1 e 2 jogos → soma 3, média 1.5 → arredonda para 2
        val present = listOf(player(1), player(2))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 1, 2 to 2), today)
        assertEquals(2, result)
    }

    @Test
    fun calculateToll_excludesSelf() {
        // Jogadores presentes: 1 (3 jogos), 2 (5 jogos), 10 (o que chega)
        // Excluindo 10, média = (3 + 5) / 2 = 4
        val present = listOf(player(1), player(2), player(10))
        val usageMap = mapOf(1 to 3, 2 to 5, 10 to 0)
        val result = TollCalculator.calculateToll(present, usageMap, today, excludePlayerId = 10)
        assertEquals(4, result)
    }

    @Test
    fun calculateToll_excludeSelf_allOthersRemoved_returnsZero() {
        // Apenas 1 jogador presente e ele é excluído
        val present = listOf(player(1))
        val result = TollCalculator.calculateToll(present, mapOf(1 to 5), today, excludePlayerId = 1)
        assertEquals(0, result)
    }

    @Test
    fun calculateToll_considersExistingTollOfPresentPlayers() {
        // Jogador 1: 2 jogos reais + pedágio 3 (de hoje) = 5 efetivos
        // Jogador 2: 1 jogo real, sem pedágio = 1 efetivo
        // Média = (5 + 1) / 2 = 3
        val present = listOf(
            player(1, dailyToll = 3, tollDate = today),
            player(2)
        )
        val usageMap = mapOf(1 to 2, 2 to 1)
        val result = TollCalculator.calculateToll(present, usageMap, today)
        assertEquals(3, result)
    }

    @Test
    fun calculateToll_ignoresOldTollOfPresentPlayers() {
        // Jogador 1 tem pedágio de ontem → deve ser ignorado
        // Jogador 1: 2 jogos (pedágio ignorado) | Jogador 2: 4 jogos → média = 3
        val present = listOf(
            player(1, dailyToll = 10, tollDate = yesterday),
            player(2)
        )
        val usageMap = mapOf(1 to 2, 2 to 4)
        val result = TollCalculator.calculateToll(present, usageMap, today)
        assertEquals(3, result)
    }

    @Test
    fun calculateToll_missingUsageMapEntry_treatedAsZeroGames() {
        // Jogador 3 não aparece no usageMap (não jogou ainda)
        val present = listOf(player(1), player(3))
        val usageMap = mapOf(1 to 4) // 3 não tem entrada
        // média = (4 + 0) / 2 = 2
        val result = TollCalculator.calculateToll(present, usageMap, today)
        assertEquals(2, result)
    }

    @Test
    fun calculateToll_largeLobby_averagesCorrectly() {
        // 6 jogadores, jogos: 3, 3, 4, 4, 5, 5 → soma 24, média 4.0
        val present = (1..6).map { player(it) }
        val usageMap = mapOf(1 to 3, 2 to 3, 3 to 4, 4 to 4, 5 to 5, 6 to 5)
        val result = TollCalculator.calculateToll(present, usageMap, today)
        assertEquals(4, result)
    }

    // ======================================================================
    //  3. shouldApplyToll
    // ======================================================================

    @Test
    fun shouldApplyToll_noTollDate_returnsTrue() {
        val p = player(1)
        assertTrue(TollCalculator.shouldApplyToll(p, today))
    }

    @Test
    fun shouldApplyToll_tollFromYesterday_returnsTrue() {
        val p = player(1, dailyToll = 3, tollDate = yesterday)
        assertTrue(TollCalculator.shouldApplyToll(p, today))
    }

    @Test
    fun shouldApplyToll_tollFromToday_returnsFalse() {
        val p = player(1, dailyToll = 3, tollDate = today)
        assertFalse(TollCalculator.shouldApplyToll(p, today))
    }

    // ======================================================================
    //  4. applyToll (integração das partes)
    // ======================================================================

    @Test
    fun applyToll_newPlayerGetsAverageToll() {
        // Presentes: P1 (3 jogos), P2 (5 jogos). Média = 4
        // P10 chega agora (sem pedágio) → deve receber toll = 4
        val newPlayer = player(10)
        val present = listOf(player(1), player(2), newPlayer)
        val usageMap = mapOf(1 to 3, 2 to 5, 10 to 0)

        val result = TollCalculator.applyToll(newPlayer, present, usageMap, today)

        assertEquals(4, result.dailyToll)
        assertEquals(today, result.tollDate)
        assertEquals(10, result.id) // id preservado
    }

    @Test
    fun applyToll_playerAlreadyHasTollToday_noChange() {
        val p = player(1, dailyToll = 3, tollDate = today)
        val present = listOf(p, player(2))
        val usageMap = mapOf(1 to 0, 2 to 5)

        val result = TollCalculator.applyToll(p, present, usageMap, today)

        // Deve retornar o mesmo objeto sem alterações
        assertEquals(3, result.dailyToll)
        assertEquals(today, result.tollDate)
        assertTrue("Deve ser o mesmo objeto (sem recalcular)", result === p)
    }

    @Test
    fun applyToll_playerHasOldToll_recalculatesToday() {
        // P1 tinha pedágio de ontem (valor 10). Deve recalcular para hoje.
        val p1 = player(1, dailyToll = 10, tollDate = yesterday)
        val p2 = player(2)
        // P2 jogou 4 vezes hoje. P1 é excluído do cálculo.
        val present = listOf(p1, p2)
        val usageMap = mapOf(1 to 0, 2 to 4)

        val result = TollCalculator.applyToll(p1, present, usageMap, today)

        assertEquals(4, result.dailyToll) // média dos outros (só P2: 4)
        assertEquals(today, result.tollDate)
    }

    @Test
    fun applyToll_noPresentOthers_tollIsZero() {
        // Único jogador presente é o próprio → pedágio = 0
        val p = player(1)
        val present = listOf(p)
        val usageMap = mapOf(1 to 0)

        val result = TollCalculator.applyToll(p, present, usageMap, today)

        assertEquals(0, result.dailyToll)
        assertEquals(today, result.tollDate)
    }

    @Test
    fun applyToll_preservesPlayerFields() {
        val p = Player(
            id = 42,
            name = "Carlos",
            elo = 1350.0,
            matchesPlayed = 10,
            victories = 6,
            isPriority = true,
            groupName = "Sexta",
            dailyToll = 0,
            tollDate = ""
        )
        val present = listOf(p, player(2))
        val usageMap = mapOf(2 to 4, 42 to 0)

        val result = TollCalculator.applyToll(p, present, usageMap, today)

        // Campos originais preservados
        assertEquals(42, result.id)
        assertEquals("Carlos", result.name)
        assertEquals(1350.0, result.elo, 0.001)
        assertEquals(10, result.matchesPlayed)
        assertEquals(6, result.victories)
        assertTrue(result.isPriority)
        assertEquals("Sexta", result.groupName)
        // Campos de toll atualizados
        assertEquals(4, result.dailyToll)
        assertEquals(today, result.tollDate)
    }

    // ======================================================================
    //  5. Cenários de jogo realistas
    // ======================================================================

    @Test
    fun scenario_firstPlayerOfTheDay_noToll() {
        // Ninguém jogou ainda. Primeiro jogador a marcar presença.
        val p = player(1)
        val present = listOf(p)
        val usageMap = emptyMap<Int, Int>()

        val result = TollCalculator.applyToll(p, present, usageMap, today)

        assertEquals(0, result.dailyToll)
    }

    @Test
    fun scenario_lateArrival_midSession() {
        // 4 jogadores presentes. Cada um jogou 3 partidas.
        // P5 chega atrasado → pedágio = 3
        val present = (1..4).map { player(it) } + player(5)
        val usageMap = mapOf(1 to 3, 2 to 3, 3 to 3, 4 to 3, 5 to 0)

        val result = TollCalculator.applyToll(player(5), present, usageMap, today)

        assertEquals(3, result.dailyToll)
    }

    @Test
    fun scenario_lateArrival_unevenGames() {
        // P1: 4 jogos, P2: 3 jogos, P3: 5 jogos → média (excluindo P10) = 4.0
        val present = listOf(player(1), player(2), player(3), player(10))
        val usageMap = mapOf(1 to 4, 2 to 3, 3 to 5, 10 to 0)

        val result = TollCalculator.applyToll(player(10), present, usageMap, today)

        assertEquals(4, result.dailyToll)
    }

    @Test
    fun scenario_secondLateArrival_firstAlreadyHasToll() {
        // P1: 3 jogos, P2: 3 jogos (presentes desde o início)
        // P3 chegou atrasado antes: toll=3, tollDate=hoje, 0 jogos reais → efetivo 3
        // P4 chega agora → média de P1(3), P2(3), P3(0+3=3) = 3
        val present = listOf(
            player(1),
            player(2),
            player(3, dailyToll = 3, tollDate = today),
            player(4)
        )
        val usageMap = mapOf(1 to 3, 2 to 3, 3 to 0, 4 to 0)

        val result = TollCalculator.applyToll(player(4), present, usageMap, today)

        assertEquals(3, result.dailyToll)
    }

    @Test
    fun scenario_effectiveGames_ordering_for_team_sort() {
        // Simula a ordenação de um time: jogadores com menos jogos efetivos primeiro
        val p1 = player(1, dailyToll = 0, tollDate = "")
        val p2 = player(2, dailyToll = 2, tollDate = today)
        val p3 = player(3, dailyToll = 0, tollDate = "")
        val usageMap = mapOf(1 to 3, 2 to 1, 3 to 1)

        val team = listOf(p1, p2, p3)
        val sorted = team.sortedBy { p ->
            TollCalculator.getEffectiveGames(p, usageMap[p.id] ?: 0, today)
        }

        // P3: 1 efetivo | P1: 3 efetivos | P2: 1+2=3 efetivos
        assertEquals(3, sorted[0].id) // 1 jogo efetivo
        // P1 e P2 empatam com 3 — stable sort mantém a ordem original (P1 vem antes de P2)
        assertEquals(1, sorted[1].id) // 3 jogos reais
        assertEquals(2, sorted[2].id) // 1 + 2 pedágio = 3
    }

    @Test
    fun scenario_dailyReset_nextDay() {
        // Jogador tinha pedágio ontem. Hoje é um novo dia.
        val p = player(1, dailyToll = 5, tollDate = yesterday)

        // getEffectiveGames ignora o pedágio antigo
        assertEquals(2, TollCalculator.getEffectiveGames(p, actualGamesToday = 2, today))

        // shouldApplyToll quer recalcular
        assertTrue(TollCalculator.shouldApplyToll(p, today))

        // applyToll recalcula com base nos presentes de hoje
        val present = listOf(p, player(2))
        val usageMap = mapOf(1 to 0, 2 to 3)

        val result = TollCalculator.applyToll(p, present, usageMap, today)
        assertEquals(3, result.dailyToll) // média dos outros: P2 com 3
        assertEquals(today, result.tollDate) // data atualizada
    }
}


package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bismarck.voleimanager.app.data.VoleiDao
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.GroupLog
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_COMPLETE
import com.bismarck.voleimanager.app.data.model.ONBOARDING_STEP_GROUP_NAME
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
import com.bismarck.voleimanager.app.data.model.PlayerPosition
import com.bismarck.voleimanager.app.data.model.TournamentMatch
import com.bismarck.voleimanager.app.data.model.TournamentTeam
import com.bismarck.voleimanager.app.data.model.TournamentTeamMember
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class VoleiViewModelRestingIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun finishGame_thenStartNextRound_rest_withOneFullTeamInQueue_winnerContinuesAndQueueTeamStepsIn() = runBlocking {
        val env = createViewModel(BalancingMode.REST)
        val vm = env.vm

        val allPlayers = listOf(
            player(1, "A1"), player(2, "A2"),
            player(3, "B1"), player(4, "B2"),
            player(5, "C1"), player(6, "C2")
        )

        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        val queue = listOf(byName.getValue("C1"), byName.getValue("C2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(champs, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)

        vm.startNextRound()
        assertEquals(champs.map { it.id }.toSet(), vm.teamA.value.map { it.id }.toSet())
        assertEquals(queue.map { it.id }.toSet(), vm.teamB.value.map { it.id }.toSet())

        val waitingIds = vm.waitingList.value.map { it.id }.toSet()
        assertTrue("Losers must move to the waiting list", waitingIds.containsAll(losers.map { it.id }))
    }

    @Test
    fun finishGame_thenStartNextRound_fixedPositions_splitsWinnersByEloAndFillsFromQueueInFairOrder() = runBlocking {
        val env = createViewModel(
            mode = BalancingMode.REBALANCE,
            groupType = com.bismarck.voleimanager.app.data.model.GroupType.FIXED_POSITIONS.name,
            teamSize = 2,
            guaranteeSetter = true
        )
        val vm = env.vm

        val allPlayers = listOf(
            player(1, "A1", elo = 1500.0, preferredPosition = PlayerPosition.SETTER.name),
            player(2, "A2", elo = 1400.0, preferredPosition = PlayerPosition.OUTSIDE_HITTER.name),
            player(3, "B1", elo = 1000.0, preferredPosition = PlayerPosition.OPPOSITE.name),
            player(4, "B2", elo = 900.0, preferredPosition = PlayerPosition.MIDDLE_BLOCKER.name),
            player(5, "C1", elo = 1300.0, preferredPosition = PlayerPosition.SETTER.name),
            player(6, "C2", elo = 1250.0, preferredPosition = PlayerPosition.OUTSIDE_HITTER.name)
        )

        val byName = insertPlayers(env, allPlayers)
        val winners = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        val queue = listOf(byName.getValue("C1"), byName.getValue("C2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(winners, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)

        // Força a quebra de sequência para acionar o rebalanceamento (dividir vencedores por Elo
        // e completar as vagas de posição faltantes com o topo da fila de espera).
        forceStreak(vm, streak = 2, owner = "A")
        vm.startNextRound()

        val a1 = byName.getValue("A1")
        val a2 = byName.getValue("A2")
        val b1 = byName.getValue("B1")
        val b2 = byName.getValue("B2")
        val c1 = byName.getValue("C1")
        val c2 = byName.getValue("C2")

        // Para uma única vaga em aberto por time, a garantia de justiça mínima (metade
        // arredondada pra cima = 1) cobre 100% da vaga: o topo "de verdade" da fila de espera
        // entra estritamente por ordem de chegada, mesmo que outro jogador mais atrás encaixasse
        // melhor na posição. Por isso C1 (topo da fila) completa o time A, e C2 completa o time B.
        assertEquals(setOf(a1.id, c1.id), vm.teamA.value.map { it.id }.toSet())
        assertEquals(setOf(a2.id, c2.id), vm.teamB.value.map { it.id }.toSet())

        // Os perdedores vão para a fila de espera.
        val waitingIds = vm.waitingList.value.map { it.id }.toSet()
        assertEquals(setOf(b1.id, b2.id), waitingIds)

        // A1 mantém a vaga de armador; C1 (também levantador) preenche a vaga de ataque restante
        // do time A. No time B, ninguém encaixa de fato na vaga de armador (A2 e C2 são ambos
        // ponteiros); o encaixe final escala C2 na vaga de ataque e força A2 na vaga de armador
        // como último recurso, deixando a composição do time B incompleta.
        assertEquals(PlayerPosition.SETTER, vm.assignedPositions.value[a1.id])
        assertEquals(PlayerPosition.OUTSIDE_HITTER, vm.assignedPositions.value[c2.id])
        assertEquals(PlayerPosition.SETTER, vm.assignedPositions.value[a2.id])
        assertTrue(vm.compositionIncomplete.value)
    }

    @Test
    fun scoreIndicator_tracksLatestScorerAndRotation_onScoreIncrease() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm

        assertNull(vm.lastScoringTeam.value)
        assertNull(vm.rotationRequiredForTeam.value)

        vm.incrementScoreA()
        assertEquals("A", vm.lastScoringTeam.value)
        assertNull(vm.rotationRequiredForTeam.value)

        vm.incrementScoreA()
        assertEquals("A", vm.lastScoringTeam.value)
        assertNull(vm.rotationRequiredForTeam.value)

        vm.incrementScoreB()
        assertEquals("B", vm.lastScoringTeam.value)
        assertEquals("B", vm.rotationRequiredForTeam.value)
    }

    @Test
    fun scoreIndicator_clearsOnScoreDecreaseAndGameReset() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm

        vm.incrementScoreA()
        vm.incrementScoreB()
        assertEquals("B", vm.lastScoringTeam.value)
        assertEquals("B", vm.rotationRequiredForTeam.value)

        vm.decrementScoreB()
        assertNull(vm.lastScoringTeam.value)
        assertNull(vm.rotationRequiredForTeam.value)

        vm.incrementScoreA()
        assertEquals("A", vm.lastScoringTeam.value)

        vm.cancelGame()
        assertEquals(0, vm.scoreA.value)
        assertEquals(0, vm.scoreB.value)
        assertNull(vm.lastScoringTeam.value)
        assertNull(vm.rotationRequiredForTeam.value)
    }

    @Test
    fun finishGame_thenStartNextRound_rest_withTwoFullTeamsInQueue_winnerRestAndTopTeamsPlay() = runBlocking {
        val env = createViewModel(BalancingMode.REST)
        val vm = env.vm

        val allPlayers = listOf(
            player(11, "A1"), player(12, "A2"),
            player(13, "B1"), player(14, "B2"),
            player(15, "C1"), player(16, "C2"),
            player(17, "D1"), player(18, "D2")
        )

        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        val queue = listOf(byName.getValue("C1"), byName.getValue("C2"))
        val queue2 = listOf(byName.getValue("D1"), byName.getValue("D2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(champs, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)
        forceStreak(vm, 2, "A")

        vm.startNextRound()
        assertEquals(queue.map { it.id }.toSet(), vm.teamA.value.map { it.id }.toSet())
        assertEquals(queue2.map { it.id }.toSet(), vm.teamB.value.map { it.id }.toSet())

        val waitingIds = vm.waitingList.value.map { it.id }.toSet()
        assertTrue("Winning team must rest", waitingIds.containsAll(champs.map { it.id }))
        assertTrue("Losers must go to the waiting list", waitingIds.containsAll(losers.map { it.id }))
    }

    @Test
    fun returningRestTeam_withUncheckedPlayer_usesNextWaitingPlayerAsSubstitute() = runBlocking {
        val env = createViewModel(BalancingMode.REST)
        val vm = env.vm

        val allPlayers = listOf(
            player(31, "A1"), player(32, "A2"),
            player(33, "B1"), player(34, "B2"),
            player(35, "C1"), player(36, "C2"),
            player(37, "D1"), player(38, "D2")
        )

        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        val queue = listOf(byName.getValue("C1"), byName.getValue("C2"))
        val queue2 = listOf(byName.getValue("D1"), byName.getValue("D2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        setPrivateFlow(vm, "_currentStreak", 2)
        setPrivateFlow(vm, "_streakOwner", "A")
        setPrivateFlow(vm, "_waitingList", listOf(byName.getValue("B1"), byName.getValue("B2"), byName.getValue("C1"), byName.getValue("C2"), byName.getValue("D1"), byName.getValue("D2")))
        setPrivateFlow(vm, "_presentPlayerIds", setOf(
            byName.getValue("A2").id,
            byName.getValue("B1").id,
            byName.getValue("B2").id,
            byName.getValue("C1").id,
            byName.getValue("C2").id,
            byName.getValue("D1").id,
            byName.getValue("D2").id
        ))
        setPrivateFlow(vm, "_restingPlayers", mapOf(
            byName.getValue("A1").id to 0,
            byName.getValue("A2").id to 0
        ))
        setPrivateFlow(vm, "_roundCounter", 1)
        setPrivateFlow(vm, "_lastWinners", listOf(byName.getValue("A1"), byName.getValue("A2")))
        setPrivateVar(vm, "lastLosers", listOf(byName.getValue("B1"), byName.getValue("B2")))

        vm.startNextRound()

        val allTeamIds = (vm.teamA.value + vm.teamB.value).map { it.id }.toSet()
        assertFalse("Unchecked resting player must not be auto-added back to the match", allTeamIds.contains(byName.getValue("A1").id))
        assertTrue("Resting team should still return using the next waiting player as substitute", allTeamIds.contains(byName.getValue("A2").id))
        val loserIds = losers.map { it.id }.toSet()
        assertTrue("A waiting player should replace the missing resting teammate", allTeamIds.any { loserIds.contains(it) })
    }

    @Test
    fun finishGame_thenStartNextRound_rest_withNoWaitingTeams_keepsCurrentMatch() = runBlocking {
        val env = createViewModel(BalancingMode.REST)
        val vm = env.vm

        val allPlayers = listOf(
            player(21, "A1"), player(22, "A2"),
            player(23, "B1"), player(24, "B2")
        )

        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(champs, losers, emptyList())
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)

        vm.startNextRound()
        assertEquals(champs.map { it.id }.toSet(), vm.teamA.value.map { it.id }.toSet())
        assertEquals(losers.map { it.id }.toSet(), vm.teamB.value.map { it.id }.toSet())
        assertTrue(vm.waitingList.value.isEmpty())
    }

    @Test
    fun startNextRound_rebalance_withMissingRecentWinner_resetsStreakAfterAutomaticReplacement() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 2,
            l = 3,
            priorityP = false,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(41, "A1"), player(42, "A2"),
            player(43, "B1"), player(44, "B2"),
            player(45, "C1"), player(46, "C2")
        )
        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("A1"), byName.getValue("A2"))
        val losers = listOf(byName.getValue("B1"), byName.getValue("B2"))
        val queue = listOf(byName.getValue("C1"), byName.getValue("C2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(champs, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)
        assertEquals(1, vm.currentStreak.value)
        assertEquals("A", vm.streakOwner.value)

        vm.togglePlayerPresence(byName.getValue("A1"))
        vm.startNextRound()

        val nextRoundIds = (vm.teamA.value + vm.teamB.value).map { it.id }.toSet()
        assertFalse(nextRoundIds.contains(byName.getValue("A1").id))
        assertTrue(nextRoundIds.contains(byName.getValue("A2").id))
        assertEquals(0, vm.currentStreak.value)
        assertEquals(null, vm.streakOwner.value)
    }

    @Test
    fun startNextRound_rebalance_belowVictoryLimit_keepsWinnerTeamIntactEvenWithoutPriority() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 2,
            l = 3,
            priorityP = true,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(61, "W1"),
            player(62, "W2"),
            player(63, "L1", isPriority = true),
            player(64, "L2"),
            player(65, "Q1", isPriority = true),
            player(66, "Q2")
        )
        val byName = insertPlayers(env, allPlayers)
        val champs = listOf(byName.getValue("W1"), byName.getValue("W2"))
        val losers = listOf(byName.getValue("L1"), byName.getValue("L2"))
        val queue = listOf(byName.getValue("Q1"), byName.getValue("Q2"))
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(champs, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)
        vm.startNextRound()

        assertEquals(champs.map { it.id }.toSet(), vm.teamA.value.map { it.id }.toSet())
        assertTrue(vm.teamB.value.any { it.isPriority })
        assertFalse(vm.teamB.value.any { it.id == byName.getValue("W1").id || it.id == byName.getValue("W2").id })
    }

    @Test
    fun startNextRound_rebalance_prioritizesGuaranteedByLowerUsageAfterPriorityAllocation() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 2,
            l = 3,
            priorityP = true,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(71, "W1"),
            player(72, "W2"),
            player(73, "L1", isPriority = true),
            player(74, "L2"),
            player(75, "P2", isPriority = true),
            player(76, "GLOW"),
            player(77, "GHIGH"),
            player(78, "NQ")
        )
        val byName = insertPlayers(env, allPlayers)
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(
            tA = listOf(byName.getValue("GHIGH"), byName.getValue("NQ")),
            tB = listOf(byName.getValue("L2"), byName.getValue("P2")),
            rem = listOf(byName.getValue("W1"), byName.getValue("W2"), byName.getValue("L1"), byName.getValue("GLOW"))
        )
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)

        vm.startManualGame(
            tA = listOf(byName.getValue("W1"), byName.getValue("W2")),
            tB = listOf(byName.getValue("L1"), byName.getValue("L2")),
            rem = listOf(byName.getValue("GLOW"), byName.getValue("GHIGH"), byName.getValue("P2"), byName.getValue("NQ"))
        )
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)

        vm.toggleGuaranteedNextMatchPlayer(byName.getValue("GLOW"))
        vm.toggleGuaranteedNextMatchPlayer(byName.getValue("GHIGH"))

        vm.startNextRound()

        assertEquals(setOf(byName.getValue("W1").id, byName.getValue("W2").id), vm.teamA.value.map { it.id }.toSet())
        val challengerIds = vm.teamB.value.map { it.id }.toSet()
        assertTrue(challengerIds.contains(byName.getValue("GLOW").id))
        assertFalse(challengerIds.contains(byName.getValue("GHIGH").id))
        assertTrue(vm.teamB.value.any { it.isPriority })
        assertTrue(vm.guaranteedNextMatchPlayerIds.value.isEmpty())
    }

    @Test
    fun startNewAutomaticGame_withOnlyOnePriority_doesNotForcePriorityRule() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 2,
            l = 3,
            priorityP = true,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(51, "N1", elo = 1300.0),
            player(52, "N2", elo = 1290.0),
            player(53, "N3", elo = 1280.0),
            player(54, "P1", elo = 1270.0, isPriority = true),
            player(55, "N4", elo = 1260.0),
            player(56, "N5", elo = 1250.0)
        )
        val byName = insertPlayers(env, allPlayers)
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startNewAutomaticGame(byName.values.toList(), size = 2)

        val selectedIds = (vm.teamA.value + vm.teamB.value).map { it.id }.toSet()
        assertFalse(
            "Single priority player should not be force-selected when priority mode is enabled",
            selectedIds.contains(byName.getValue("P1").id)
        )
    }

    @Test
    fun startNextRound_rebalance_atVictoryLimit_splitsEvenlyBeforeFillingFromQueue() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 4,
            l = 2,
            priorityP = false,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(101, "W1", elo = 1400.0),
            player(102, "W2", elo = 1350.0),
            player(103, "W3", elo = 1300.0),
            player(104, "W4", elo = 1250.0),
            player(105, "L1", elo = 1200.0),
            player(106, "L2", elo = 1180.0),
            player(107, "L3", elo = 1160.0),
            player(108, "L4", elo = 1140.0),
            player(109, "Q1", elo = 1220.0),
            player(110, "Q2", elo = 1100.0)
        )
        val byName = insertPlayers(env, allPlayers)
        val winners = listOf("W1", "W2", "W3", "W4").map { byName.getValue(it) }
        val losers = listOf("L1", "L2", "L3", "L4").map { byName.getValue(it) }
        val queue = listOf("Q1", "Q2").map { byName.getValue(it) }
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.startManualGame(winners, losers, queue)
        vm.finishGame("A")
        awaitFinishGamePersistence(vm)
        forceStreak(vm, 2, "A")
        vm.startNextRound()

        assertEquals(4, vm.teamA.value.size)
        assertEquals(4, vm.teamB.value.size)

        val winnerIds = winners.map { it.id }.toSet()
        val winnersInA = vm.teamA.value.count { winnerIds.contains(it.id) }
        val winnersInB = vm.teamB.value.count { winnerIds.contains(it.id) }
        assertEquals(2, winnersInA)
        assertEquals(2, winnersInB)
    }

    @Test
    fun startNewAutomaticGame_withGuaranteedPlayers_includesThemAndClearsTemporaryMarks() = runBlocking {
        val env = createViewModel(BalancingMode.REBALANCE)
        val vm = env.vm
        vm.updateConfig(
            s = 2,
            l = 3,
            priorityP = true,
            scoreEnabled = true,
            balancingMode = BalancingMode.REBALANCE.name
        )

        val allPlayers = listOf(
            player(121, "G1", elo = 1000.0, isPriority = true),
            player(122, "G2", elo = 1010.0, isPriority = true),
            player(123, "N1", elo = 1400.0),
            player(124, "N2", elo = 1380.0),
            player(125, "N3", elo = 1360.0),
            player(126, "N4", elo = 1340.0)
        )
        val byName = insertPlayers(env, allPlayers)
        vm.setAllPlayersPresence(byName.values.toList(), present = true)

        vm.toggleGuaranteedNextMatchPlayer(byName.getValue("G1"))
        vm.toggleGuaranteedNextMatchPlayer(byName.getValue("G2"))
        assertEquals(2, vm.guaranteedNextMatchPlayerIds.value.size)

        vm.startNewAutomaticGame(byName.values.toList(), size = 2)

        val selectedIds = (vm.teamA.value + vm.teamB.value).map { it.id }.toSet()
        assertTrue(selectedIds.contains(byName.getValue("G1").id))
        assertTrue(selectedIds.contains(byName.getValue("G2").id))
        assertEquals(0, vm.guaranteedNextMatchPlayerIds.value.size)
        assertTrue(vm.teamA.value.any { it.isPriority })
        assertTrue(vm.teamB.value.any { it.isPriority })
    }

    @Test
    fun init_withExistingNonDefaultGroup_doesNotCreateDefaultGroup() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        repo.saveGroupConfig(
            GroupConfig(
                groupName = "Amigos",
                onboardingStep = ONBOARDING_STEP_COMPLETE
            )
        )

        val vm = VoleiViewModel(app, repo)
        withTimeout(3_000) {
            vm.currentGroupConfig.first { it.groupName == "Amigos" }
        }

        val groupNames = repo.getAllGroupNames()
        assertTrue(groupNames.contains("Amigos"))
        assertTrue(!groupNames.contains(DEFAULT_GROUP_NAME))
    }

    @Test
    fun init_withNoExistingGroups_doesNotPersistAnyGroupUntilNameConfirmed() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)

        val vm = VoleiViewModel(app, repo)
        withTimeout(3_000) {
            vm.currentGroupConfig.first { it.onboardingStep == ONBOARDING_STEP_GROUP_NAME }
        }

        // Fresh install: nothing should be written to the database yet.
        assertTrue(repo.getAllGroupNames().isEmpty())
        assertEquals("", vm.currentGroupConfig.value.groupName)

        // Only after the user confirms a valid name should a GroupConfig be persisted.
        vm.continueCurrentGroupOnboardingWithGroupName("Amigos")
        withTimeout(3_000) {
            while (repo.getGroupConfig("Amigos") == null) {
                delay(20)
            }
        }

        val groupNames = repo.getAllGroupNames()
        assertTrue(groupNames.contains("Amigos"))
        assertTrue(!groupNames.contains(DEFAULT_GROUP_NAME))
    }

    @Test
    fun init_withLegacyGroupDataWithoutConfig_marksOnboardingAsComplete() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        repo.insertPlayer(Player(name = "Bruno", groupName = "Amigos", elo = 1200.0))

        val vm = VoleiViewModel(app, repo)
        val loaded = withTimeout(3_000) {
            vm.currentGroupConfig.first { it.groupName == "Amigos" }
        }

        assertEquals(ONBOARDING_STEP_COMPLETE, loaded.onboardingStep)
        val persisted = repo.getGroupConfig("Amigos")
        assertEquals(ONBOARDING_STEP_COMPLETE, persisted?.onboardingStep)
    }

    @Test
    fun groupsSortedByRecentHistory_includesGroupsWithoutHistoryAtEnd() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)

        repo.saveGroupConfig(GroupConfig(groupName = "SemHistorico", onboardingStep = ONBOARDING_STEP_COMPLETE))
        repo.insertMatch(
            MatchHistory(
                date = "01/01/2026 10:00",
                teamA = "A1, A2",
                teamB = "B1, B2",
                winner = "A",
                eloPoints = 16.0,
                groupName = "MaisAntigo"
            )
        )
        repo.insertMatch(
            MatchHistory(
                date = "02/01/2026 10:00",
                teamA = "C1, C2",
                teamB = "D1, D2",
                winner = "B",
                eloPoints = 16.0,
                groupName = "MaisRecente"
            )
        )

        val vm = VoleiViewModel(app, repo)
        val groups = withTimeout(3_000) {
            vm.groupsSortedByRecentHistory.first {
                it.contains("MaisRecente") && it.contains("MaisAntigo") && it.contains("SemHistorico")
            }
        }

        assertEquals(listOf("MaisRecente", "MaisAntigo"), groups.take(2))
        assertEquals("SemHistorico", groups.last())
    }

    @Test
    fun init_withSavedGameStateAndLastMatchOlderThan12Hours_clearsCurrentGameState() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences("volei", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        repo.saveGroupConfig(
            GroupConfig(
                groupName = DEFAULT_GROUP_NAME,
                onboardingStep = ONBOARDING_STEP_COMPLETE
            )
        )
        repo.insertMatch(
            MatchHistory(
                date = "01/01/2026 10:00",
                teamA = "A1",
                teamB = "B1",
                winner = "A",
                eloPoints = 16.0,
                groupName = DEFAULT_GROUP_NAME,
                endTimestamp = System.currentTimeMillis() - (13L * 60L * 60L * 1000L)
            )
        )

        val savedSnapshot = GameStateSnapshot(
            groupName = DEFAULT_GROUP_NAME,
            teamA = listOf(player(301, "A1")),
            teamB = listOf(player(302, "B1")),
            waitingList = emptyList(),
            presentPlayerIds = listOf(301, 302),
            scoreA = 11,
            scoreB = 9,
            currentStreak = 1,
            streakOwner = "A",
            hasPreviousMatch = true,
            lastWinners = listOf(player(301, "A1")),
            lastLosers = listOf(player(302, "B1"))
        )
        prefs.edit()
            .putString("game_state_$DEFAULT_GROUP_NAME", Gson().toJson(savedSnapshot))
            .apply()

        val vm = VoleiViewModel(app, repo)
        withTimeout(3_000) {
            vm.isGroupDataLoading.first { !it }
        }

        assertTrue(vm.teamA.value.isEmpty())
        assertTrue(vm.teamB.value.isEmpty())
        assertTrue(vm.presentPlayerIds.value.isEmpty())
        assertNull(prefs.getString("game_state_$DEFAULT_GROUP_NAME", null))
    }

    @Test
    fun init_withSavedGameStateAndRecentLastMatch_restoresCurrentGameState() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences("volei", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        repo.saveGroupConfig(
            GroupConfig(
                groupName = DEFAULT_GROUP_NAME,
                onboardingStep = ONBOARDING_STEP_COMPLETE
            )
        )
        repo.insertMatch(
            MatchHistory(
                date = "01/01/2026 10:00",
                teamA = "A1",
                teamB = "B1",
                winner = "A",
                eloPoints = 16.0,
                groupName = DEFAULT_GROUP_NAME,
                endTimestamp = System.currentTimeMillis() - (2L * 60L * 60L * 1000L)
            )
        )

        val savedSnapshot = GameStateSnapshot(
            groupName = DEFAULT_GROUP_NAME,
            teamA = listOf(player(401, "A1")),
            teamB = listOf(player(402, "B1")),
            waitingList = listOf(player(403, "W1")),
            presentPlayerIds = listOf(401, 402, 403),
            scoreA = 7,
            scoreB = 6,
            currentStreak = 1,
            streakOwner = "A",
            hasPreviousMatch = true,
            lastWinners = listOf(player(401, "A1")),
            lastLosers = listOf(player(402, "B1"))
        )
        prefs.edit()
            .putString("game_state_$DEFAULT_GROUP_NAME", Gson().toJson(savedSnapshot))
            .apply()

        val vm = VoleiViewModel(app, repo)
        withTimeout(3_000) {
            vm.isGroupDataLoading.first { !it }
        }

        assertEquals(setOf(401), vm.teamA.value.map { it.id }.toSet())
        assertEquals(setOf(402), vm.teamB.value.map { it.id }.toSet())
        assertEquals(setOf(401, 402, 403), vm.presentPlayerIds.value)
        assertEquals(7, vm.scoreA.value)
        assertEquals(6, vm.scoreB.value)
        assertTrue(prefs.getString("game_state_$DEFAULT_GROUP_NAME", null) != null)
    }

    private fun forceStreak(vm: VoleiViewModel, streak: Int, owner: String?) {
        val streakField = VoleiViewModel::class.java.getDeclaredField("_currentStreak")
        val ownerField = VoleiViewModel::class.java.getDeclaredField("_streakOwner")
        streakField.isAccessible = true
        ownerField.isAccessible = true
        (streakField.get(vm) as MutableStateFlow<Int>).value = streak
        (ownerField.get(vm) as MutableStateFlow<String?>).value = owner
    }

    private fun setPrivateFlow(vm: VoleiViewModel, fieldName: String, value: Any?) {
        val field = VoleiViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(vm) as MutableStateFlow<Any?>
        flow.value = value
    }

    private fun setPrivateVar(vm: VoleiViewModel, fieldName: String, value: Any?) {
        val field = VoleiViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(vm, value)
    }

    private suspend fun insertPlayers(env: TestEnv, players: List<Player>): Map<String, Player> {
        env.repo.insertPlayers(players)
        val loaded = withTimeout(3_000) {
            env.vm.currentGroupPlayers.first { it.size >= players.size }
        }
        return loaded.associateBy { it.name }
    }

    private fun createViewModel(
        mode: BalancingMode,
        groupType: String = com.bismarck.voleimanager.app.data.model.GroupType.RECREATIONAL.name,
        teamSize: Int = 2,
        guaranteeSetter: Boolean = true
    ): TestEnv {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        val vm = VoleiViewModel(app, repo)

        runBlocking {
            repo.saveGroupConfig(
                GroupConfig(
                    groupName = DEFAULT_GROUP_NAME,
                    teamSize = teamSize,
                    victoryLimit = 2,
                    priorityEnabled = false,
                    scoreEnabled = true,
                    balancingMode = mode.name,
                    groupType = groupType,
                    guaranteeSetter = guaranteeSetter
                )
            )
        }
        vm.loadGroupConfig(DEFAULT_GROUP_NAME)
        vm.updateConfig(
            s = teamSize,
            l = 2,
            priorityP = false,
            scoreEnabled = true,
            balancingMode = mode.name,
            groupType = groupType,
            guaranteeSetter = guaranteeSetter
        )
        return TestEnv(vm, repo)
    }

    private suspend fun awaitFinishGamePersistence(vm: VoleiViewModel) {
        withTimeout(5_000) {
            while (vm.teamA.value.isNotEmpty() || vm.teamB.value.isNotEmpty()) {
                delay(20)
            }
        }
    }

    private fun player(
        id: Int,
        name: String,
        elo: Double = 1200.0,
        isPriority: Boolean = false,
        preferredPosition: String? = null,
        secondaryPosition: String? = null
    ): Player = Player(
        id = id,
        name = name,
        elo = elo,
        isPriority = isPriority,
        groupName = DEFAULT_GROUP_NAME,
        preferredPosition = preferredPosition,
        secondaryPosition = secondaryPosition
    )
}

private data class TestEnv(
    val vm: VoleiViewModel,
    val repo: VoleiRepository
)

private class FakeVoleiDao : VoleiDao {
    private val players = mutableListOf<Player>()
    private val history = mutableListOf<MatchHistory>()
    private val eloLogs = mutableListOf<PlayerEloLog>()
    private val configs = mutableMapOf<String, GroupConfig>()
    private val tournamentTeams = mutableListOf<TournamentTeam>()
    private val tournamentMembers = mutableListOf<TournamentTeamMember>()
    private val tournamentMatches = mutableListOf<TournamentMatch>()
    private val groupLogs = mutableListOf<GroupLog>()

    private val playersFlow = MutableStateFlow<List<Player>>(emptyList())
    private val historyFlow = MutableStateFlow<List<MatchHistory>>(emptyList())
    private val eloLogsFlow = MutableStateFlow<List<PlayerEloLog>>(emptyList())
    private val configsFlow = MutableStateFlow<List<GroupConfig>>(emptyList())
    private val tournamentTeamsFlow = MutableStateFlow<List<TournamentTeam>>(emptyList())
    private val tournamentMembersFlow = MutableStateFlow<List<TournamentTeamMember>>(emptyList())
    private val tournamentMatchesFlow = MutableStateFlow<List<TournamentMatch>>(emptyList())
    private val groupLogsFlow = MutableStateFlow<List<GroupLog>>(emptyList())

    private var nextPlayerId = 1
    private var nextMatchId = 1
    private var nextLogId = 1
    private var nextTournamentTeamId = 1
    private var nextTournamentMemberId = 1
    private var nextTournamentMatchId = 1
    private var nextGroupLogId = 1

    override fun getAllPlayers(): Flow<List<Player>> = playersFlow.asStateFlow()
    override fun getPlayersByGroup(groupName: String): Flow<List<Player>> =
        playersFlow.asStateFlow().map { list -> list.filter { it.groupName == groupName } }

    override suspend fun insertPlayer(player: Player): Long {
        val id = if (player.id == 0) nextPlayerId++ else player.id
        players.removeAll { it.id == id }
        players.add(player.copy(id = id))
        emitPlayers()
        return id.toLong()
    }

    override suspend fun insertPlayers(players: List<Player>) {
        players.forEach { p ->
            val id = if (p.id == 0) nextPlayerId++ else p.id
            if (this.players.none { it.id == id }) {
                this.players.add(p.copy(id = id))
            }
        }
        emitPlayers()
    }

    override suspend fun updatePlayers(players: List<Player>) {
        players.forEach { updatePlayer(it) }
    }

    override suspend fun updatePlayer(player: Player) {
        this.players.removeAll { it.id == player.id }
        this.players.add(player)
        emitPlayers()
    }

    override suspend fun deletePlayer(player: Player) {
        players.removeAll { it.id == player.id }
        emitPlayers()
    }

    override fun getHistory(): Flow<List<MatchHistory>> = historyFlow.asStateFlow()
    override fun getHistoryByGroup(groupName: String): Flow<List<MatchHistory>> =
        historyFlow.asStateFlow().map { list -> list.filter { it.groupName == groupName } }

    override suspend fun insertMatch(match: MatchHistory) {
        val id = if (match.id == 0) nextMatchId++ else match.id
        history.add(0, match.copy(id = id))
        historyFlow.value = history.toList()
    }

    override suspend fun insertHistoryList(history: List<MatchHistory>) {
        history.forEach { insertMatch(it) }
    }

    override suspend fun updateMatchHistories(history: List<MatchHistory>) {
        history.forEach { updated ->
            this.history.replaceAll { current -> if (current.id == updated.id) updated else current }
        }
        historyFlow.value = this.history.toList()
    }

    override suspend fun getAllHistorySync(): List<MatchHistory> = history.toList()

    override suspend fun insertEloLog(log: PlayerEloLog) {
        val id = if (log.id == 0) nextLogId++ else log.id
        eloLogs.add(log.copy(id = id))
        eloLogsFlow.value = eloLogs.sortedBy { it.date }
    }

    override suspend fun insertEloLogs(logs: List<PlayerEloLog>) {
        logs.forEach { insertEloLog(it) }
    }

    override suspend fun getPlayersByGroupSync(groupName: String): List<Player> =
        players.filter { it.groupName == groupName }

    override suspend fun getHistoryByGroupSync(groupName: String): List<MatchHistory> =
        history.filter { it.groupName == groupName }

    override suspend fun getEloLogsByGroupSync(groupName: String): List<PlayerEloLog> =
        eloLogs.filter { it.groupName == groupName }

    override fun getAllEloLogs(): Flow<List<PlayerEloLog>> = eloLogsFlow.asStateFlow()
    override fun getEloLogsByGroup(groupName: String): Flow<List<PlayerEloLog>> =
        eloLogsFlow.asStateFlow().map { list -> list.filter { it.groupName == groupName } }

    override suspend fun getAllEloLogsSync(): List<PlayerEloLog> = eloLogs.toList()

    override suspend fun updatePlayerEloLogs(logs: List<PlayerEloLog>) {
        logs.forEach { updated ->
            eloLogs.replaceAll { current -> if (current.id == updated.id) updated else current }
        }
        eloLogsFlow.value = eloLogs.sortedBy { it.date }
    }

    override suspend fun getGroupConfig(groupName: String): GroupConfig? = configs[groupName]

    override suspend fun getAllGroupConfigs(): List<GroupConfig> = configs.values.toList()
    override fun getAllGroupConfigsFlow(): Flow<List<GroupConfig>> = configsFlow.asStateFlow()

    override suspend fun getAllGroupNames(): List<String> {
        val groups = mutableSetOf<String>()
        groups.addAll(configs.keys)
        groups.addAll(players.map { it.groupName })
        groups.addAll(history.map { it.groupName })
        groups.addAll(eloLogs.map { it.groupName })
        return groups.sortedBy { it.lowercase() }
    }

    override suspend fun saveGroupConfig(config: GroupConfig) {
        configs[config.groupName] = config
        emitConfigs()
    }

    override suspend fun updatePlayerGroupNames(oldName: String, newName: String) {
        players.replaceAll { p -> if (p.groupName == oldName) p.copy(groupName = newName) else p }
        emitPlayers()
    }

    override suspend fun updateHistoryGroupNames(oldName: String, newName: String) {
        history.replaceAll { h -> if (h.groupName == oldName) h.copy(groupName = newName) else h }
        historyFlow.value = history.toList()
    }

    override suspend fun updateConfigGroupNames(oldName: String, newName: String) {
        val old = configs.remove(oldName) ?: return
        configs[newName] = old.copy(groupName = newName)
        emitConfigs()
    }

    override suspend fun updateEloLogGroupNames(oldName: String, newName: String) {
        eloLogs.replaceAll { l -> if (l.groupName == oldName) l.copy(groupName = newName) else l }
        eloLogsFlow.value = eloLogs.sortedBy { it.date }
    }

    override suspend fun deletePlayersByGroup(groupName: String) {
        players.removeAll { it.groupName == groupName }
        emitPlayers()
    }

    override suspend fun deleteHistoryByGroup(groupName: String) {
        history.removeAll { it.groupName == groupName }
        historyFlow.value = history.toList()
    }

    override suspend fun deleteConfigByGroup(groupName: String) {
        configs.remove(groupName)
        emitConfigs()
    }

    override suspend fun deleteEloLogsByGroup(groupName: String) {
        eloLogs.removeAll { it.groupName == groupName }
        eloLogsFlow.value = eloLogs.sortedBy { it.date }
    }

    // --- Tournament & group logs ---
    override fun getTournamentTeamsByGroup(groupName: String): Flow<List<TournamentTeam>> =
        tournamentTeamsFlow.map { list -> list.filter { it.groupName == groupName } }

    override suspend fun getTournamentTeamsByGroupSync(groupName: String): List<TournamentTeam> =
        tournamentTeams.filter { it.groupName == groupName }

    override suspend fun insertTournamentTeam(team: TournamentTeam): Long {
        val id = if (team.id == 0) nextTournamentTeamId++ else team.id
        tournamentTeams.removeAll { it.id == id }
        tournamentTeams.add(team.copy(id = id))
        emitTournamentTeams()
        return id.toLong()
    }

    override suspend fun insertTournamentTeams(teams: List<TournamentTeam>) {
        teams.forEach { insertTournamentTeam(it) }
    }

    override suspend fun updateTournamentTeam(team: TournamentTeam) {
        val index = tournamentTeams.indexOfFirst { it.id == team.id }
        if (index >= 0) tournamentTeams[index] = team
        emitTournamentTeams()
    }

    override suspend fun deleteTournamentTeam(team: TournamentTeam) {
        tournamentTeams.removeAll { it.id == team.id }
        emitTournamentTeams()
    }

    override fun getTournamentTeamMembersByGroup(groupName: String): Flow<List<TournamentTeamMember>> =
        tournamentMembersFlow.map { list -> list.filter { it.groupName == groupName } }

    override suspend fun getTournamentTeamMembersByGroupSync(groupName: String): List<TournamentTeamMember> =
        tournamentMembers.filter { it.groupName == groupName }

    override suspend fun insertTournamentTeamMember(member: TournamentTeamMember): Long {
        val id = if (member.id == 0) nextTournamentMemberId++ else member.id
        tournamentMembers.removeAll { it.id == id }
        tournamentMembers.add(member.copy(id = id))
        emitTournamentMembers()
        return id.toLong()
    }

    override suspend fun insertTournamentTeamMembers(members: List<TournamentTeamMember>) {
        members.forEach { insertTournamentTeamMember(it) }
    }

    override suspend fun updateTournamentTeamMembers(members: List<TournamentTeamMember>) {
        members.forEach { member ->
            val index = tournamentMembers.indexOfFirst { it.id == member.id }
            if (index >= 0) tournamentMembers[index] = member
        }
        emitTournamentMembers()
    }

    override suspend fun deleteTournamentTeamMember(member: TournamentTeamMember) {
        tournamentMembers.removeAll { it.id == member.id }
        emitTournamentMembers()
    }

    override fun getTournamentMatchesByGroup(groupName: String): Flow<List<TournamentMatch>> =
        tournamentMatchesFlow.map { list -> list.filter { it.groupName == groupName } }

    override suspend fun getTournamentMatchesByGroupSync(groupName: String): List<TournamentMatch> =
        tournamentMatches.filter { it.groupName == groupName }

    override suspend fun insertTournamentMatch(match: TournamentMatch): Long {
        val id = if (match.id == 0) nextTournamentMatchId++ else match.id
        tournamentMatches.removeAll { it.id == id }
        tournamentMatches.add(match.copy(id = id))
        emitTournamentMatches()
        return id.toLong()
    }

    override suspend fun insertTournamentMatches(matches: List<TournamentMatch>) {
        matches.forEach { insertTournamentMatch(it) }
    }

    override suspend fun updateTournamentMatch(match: TournamentMatch) {
        val index = tournamentMatches.indexOfFirst { it.id == match.id }
        if (index >= 0) tournamentMatches[index] = match
        emitTournamentMatches()
    }

    override suspend fun updateTournamentMatches(matches: List<TournamentMatch>) {
        matches.forEach { updateTournamentMatch(it) }
    }

    override fun getGroupLogsByGroup(groupName: String): Flow<List<GroupLog>> =
        groupLogsFlow.map { list -> list.filter { it.groupName == groupName } }

    override suspend fun getGroupLogsByGroupSync(groupName: String): List<GroupLog> =
        groupLogs.filter { it.groupName == groupName }

    override suspend fun insertGroupLog(log: GroupLog): Long {
        val id = if (log.id == 0) nextGroupLogId++ else log.id
        groupLogs.removeAll { it.id == id }
        groupLogs.add(log.copy(id = id))
        emitGroupLogs()
        return id.toLong()
    }

    override suspend fun insertGroupLogs(logs: List<GroupLog>) {
        logs.forEach { insertGroupLog(it) }
    }

    override suspend fun updateTournamentTeamGroupNames(oldName: String, newName: String) {
        tournamentTeams.replaceAll { if (it.groupName == oldName) it.copy(groupName = newName) else it }
        emitTournamentTeams()
    }

    override suspend fun updateTournamentTeamMemberGroupNames(oldName: String, newName: String) {
        tournamentMembers.replaceAll { if (it.groupName == oldName) it.copy(groupName = newName) else it }
        emitTournamentMembers()
    }

    override suspend fun updateTournamentMatchGroupNames(oldName: String, newName: String) {
        tournamentMatches.replaceAll { if (it.groupName == oldName) it.copy(groupName = newName) else it }
        emitTournamentMatches()
    }

    override suspend fun updateGroupLogGroupNames(oldName: String, newName: String) {
        groupLogs.replaceAll { if (it.groupName == oldName) it.copy(groupName = newName) else it }
        emitGroupLogs()
    }

    override suspend fun deleteTournamentTeamsByGroup(groupName: String) {
        tournamentTeams.removeAll { it.groupName == groupName }
        emitTournamentTeams()
    }

    override suspend fun deleteTournamentTeamMembersByGroup(groupName: String) {
        tournamentMembers.removeAll { it.groupName == groupName }
        emitTournamentMembers()
    }

    override suspend fun deleteTournamentMatchesByGroup(groupName: String) {
        tournamentMatches.removeAll { it.groupName == groupName }
        emitTournamentMatches()
    }

    override suspend fun deleteGroupLogsByGroup(groupName: String) {
        groupLogs.removeAll { it.groupName == groupName }
        emitGroupLogs()
    }

    private fun emitTournamentTeams() {
        tournamentTeamsFlow.value = tournamentTeams.sortedBy { it.teamKey }
    }

    private fun emitTournamentMembers() {
        tournamentMembersFlow.value = tournamentMembers.sortedBy { it.id }
    }

    private fun emitTournamentMatches() {
        tournamentMatchesFlow.value = tournamentMatches.sortedWith(
            compareBy({ it.roundIndex }, { it.orderInRound })
        )
    }

    private fun emitGroupLogs() {
        groupLogsFlow.value = groupLogs.sortedByDescending { it.timestamp }
    }

    private fun emitPlayers() {
        playersFlow.value = players.sortedByDescending { it.elo }
    }

    private fun emitConfigs() {
        configsFlow.value = configs.values.sortedBy { it.groupName.lowercase(Locale.getDefault()) }
    }
}

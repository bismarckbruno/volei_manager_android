package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.bismarck.voleimanager.app.data.VoleiDao
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.data.model.BalancingMode
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

        vm.startNextRound()
        assertEquals(queue.map { it.id }.toSet(), vm.teamA.value.map { it.id }.toSet())
        assertEquals(queue2.map { it.id }.toSet(), vm.teamB.value.map { it.id }.toSet())

        val waitingIds = vm.waitingList.value.map { it.id }.toSet()
        assertTrue("Winning team must rest", waitingIds.containsAll(champs.map { it.id }))
        assertTrue("Losers must go to the waiting list", waitingIds.containsAll(losers.map { it.id }))
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

    private suspend fun insertPlayers(env: TestEnv, players: List<Player>): Map<String, Player> {
        env.repo.insertPlayers(players)
        val loaded = withTimeout(3_000) {
            env.vm.currentGroupPlayers.first { it.size >= players.size }
        }
        return loaded.associateBy { it.name }
    }

    private fun createViewModel(mode: BalancingMode): TestEnv {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("volei", Context.MODE_PRIVATE).edit().clear().apply()

        val dao = FakeVoleiDao()
        val repo = VoleiRepository(dao)
        val vm = VoleiViewModel(app, repo)

        runBlocking {
            repo.saveGroupConfig(
                GroupConfig(
                    groupName = DEFAULT_GROUP_NAME,
                    teamSize = 2,
                    victoryLimit = 1,
                    priorityEnabled = false,
                    scoreEnabled = true,
                    balancingMode = mode.name
                )
            )
        }
        vm.loadGroupConfig(DEFAULT_GROUP_NAME)
        vm.updateConfig(s = 2, l = 1, priorityP = false, scoreEnabled = true, balancingMode = mode.name)
        return TestEnv(vm, repo)
    }

    private suspend fun awaitFinishGamePersistence(vm: VoleiViewModel) {
        withTimeout(5_000) {
            while (vm.teamA.value.isNotEmpty() || vm.teamB.value.isNotEmpty()) {
                delay(20)
            }
        }
    }

    private fun player(id: Int, name: String): Player =
        Player(id = id, name = name, elo = 1200.0, groupName = DEFAULT_GROUP_NAME)
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

    private val playersFlow = MutableStateFlow<List<Player>>(emptyList())
    private val historyFlow = MutableStateFlow<List<MatchHistory>>(emptyList())
    private val eloLogsFlow = MutableStateFlow<List<PlayerEloLog>>(emptyList())

    private var nextPlayerId = 1
    private var nextMatchId = 1
    private var nextLogId = 1

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

    override suspend fun saveGroupConfig(config: GroupConfig) {
        configs[config.groupName] = config
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
    }

    override suspend fun deleteEloLogsByGroup(groupName: String) {
        eloLogs.removeAll { it.groupName == groupName }
        eloLogsFlow.value = eloLogs.sortedBy { it.date }
    }

    private fun emitPlayers() {
        playersFlow.value = players.sortedByDescending { it.elo }
    }
}




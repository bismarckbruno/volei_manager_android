package com.bismarck.voleimanager.app.ui

import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.util.RemoteEloLogEntry
import com.bismarck.voleimanager.app.util.RemoteHistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Reproduz o pipeline exato usado pela tela de Histórico para grupos remotos (Auxiliar/
 * Espectador): [RemoteHistoryEntry]/[RemoteEloLogEntry] (vindos do Firestore) convertidos via
 * [toMatchHistory]/[toPlayerEloLogs], jogadores via um [Player] "sintético" (mesmo formato de
 * [com.bismarck.voleimanager.app.ui.viewmodel.toSyntheticPlayer]), e então processados por
 * [computeHistoryComputation] — para confirmar (ou refutar) que jogos/vitórias/aproveitamento
 * batem mesmo quando playerId sempre chega como 0 nos logs remotos.
 */
class RemoteHistoryStatsTest {

    @Test
    fun remotePipeline_countsGamesAndVictoriesForMatchedPlayer() {
        val groupName = "Grupo Teste"
        val history = listOf(
            RemoteHistoryEntry(
                id = "m1",
                date = "18/04/2026 20:00",
                teamA = "Mari, Agnes",
                teamB = "Bismarck, Fernando",
                winner = "A",
                endTimestamp = 1L
            ),
            RemoteHistoryEntry(
                id = "m2",
                date = "18/04/2026 20:30",
                teamA = "Bismarck, Agnes",
                teamB = "Mari, Fernando",
                winner = "B",
                endTimestamp = 2L
            )
        ).map { it.toMatchHistory(groupName) }

        val eloEntries = listOf(
            RemoteEloLogEntry(playerNameSnapshot = "Mari", date = "2026-04-18", elo = 1210.0, won = true, endTimestamp = 1L),
            RemoteEloLogEntry(playerNameSnapshot = "Mari", date = "2026-04-18", elo = 1195.0, won = false, endTimestamp = 2L),
            RemoteEloLogEntry(playerNameSnapshot = "Agnes", date = "2026-04-18", elo = 1210.0, won = true, endTimestamp = 1L),
            RemoteEloLogEntry(playerNameSnapshot = "Agnes", date = "2026-04-18", elo = 1220.0, won = true, endTimestamp = 2L),
            RemoteEloLogEntry(playerNameSnapshot = "Bismarck", date = "2026-04-18", elo = 1190.0, won = false, endTimestamp = 1L),
            RemoteEloLogEntry(playerNameSnapshot = "Bismarck", date = "2026-04-18", elo = 1205.0, won = true, endTimestamp = 2L),
            RemoteEloLogEntry(playerNameSnapshot = "Fernando", date = "2026-04-18", elo = 1190.0, won = false, endTimestamp = 1L),
            RemoteEloLogEntry(playerNameSnapshot = "Fernando", date = "2026-04-18", elo = 1180.0, won = false, endTimestamp = 2L)
        ).toPlayerEloLogs(groupName)

        val groupPlayers = listOf(
            Player(id = "mari".hashCode(), name = "Mari", groupName = groupName, elo = 1195.0),
            Player(id = "agnes".hashCode(), name = "Agnes", groupName = groupName, elo = 1220.0),
            Player(id = "bismarck".hashCode(), name = "Bismarck", groupName = groupName, elo = 1205.0),
            Player(id = "fernando".hashCode(), name = "Fernando", groupName = groupName, elo = 1180.0)
        )

        val result = computeHistoryComputation(
            groupHistory = history,
            historyDate = null,
            historyPlayerFilter = null,
            restrictToDates = null,
            matchSortMode = MatchSortMode.NEWEST,
            groupPlayers = groupPlayers,
            eloLogs = eloEntries,
            playerSortMode = PlayerSortMode.ALPHABETICAL
        )

        val byName = result.historyPlayerList.associateBy { it.name }
        assertEquals(4, byName.size)

        assertEquals(2, byName.getValue("Mari").gamesPlayed)
        assertEquals(1, byName.getValue("Mari").victories)

        assertEquals(2, byName.getValue("Agnes").gamesPlayed)
        assertEquals(2, byName.getValue("Agnes").victories)

        assertEquals(2, byName.getValue("Bismarck").gamesPlayed)
        assertEquals(1, byName.getValue("Bismarck").victories)

        assertEquals(2, byName.getValue("Fernando").gamesPlayed)
        assertEquals(0, byName.getValue("Fernando").victories)
    }
}

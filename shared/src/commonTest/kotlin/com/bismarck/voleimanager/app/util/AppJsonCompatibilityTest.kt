package com.bismarck.voleimanager.app.util

import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.ui.viewmodel.BackupData
import com.bismarck.voleimanager.app.ui.viewmodel.GameStateSnapshot
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * O formato JSON (backup .vlz e estado do jogo salvo) era produzido pelo Gson; estes testes garantem
 * que o [AppJson] continua lendo esses arquivos e gravando no mesmo formato.
 */
class AppJsonCompatibilityTest {

    // Backup antigo no formato do Gson: sem publicId/posições, nulos omitidos, Elo inteiro e uma
    // chave desconhecida (de uma versão futura).
    private val legacyGsonBackup = """
        {"version":1,"date":"01/02/2024 20:00",
         "players":[{"id":3,"name":"Ana","elo":1250,"matchesPlayed":4,"victories":2,"isPriority":true,
                     "groupName":"Quinta","dailyToll":0,"tollDate":""}],
         "history":[{"id":7,"date":"01/02/2024","teamA":"Ana","teamB":"Bia","winner":"A",
                     "eloPoints":16.0,"groupName":"Quinta","teamAScore":25,"teamBScore":20}],
         "logs":[{"id":1,"playerId":3,"playerNameSnapshot":"Ana","date":"2024-02-01","elo":1250.0,
                  "groupName":"Quinta","won":true}],
         "futureField":{"x":1}}
    """.trimIndent()

    @Test
    fun decodesLegacyGsonBackup() {
        val backup = AppJson.decodeFromString(BackupData.serializer(), legacyGsonBackup)
        val player = backup.players.single()
        assertEquals("Ana", player.name)
        assertEquals(1250.0, player.elo)
        assertTrue(player.isPriority)
        assertNull(player.preferredPosition)
        assertTrue(player.publicId.isNotBlank(), "publicId ausente recebe um UUID novo")
        val match = backup.history.single()
        assertEquals("", match.teamAIds)
        assertEquals(25, match.teamAScore)
        assertNull(match.startTimestamp)
        assertEquals(true, backup.logs.single().won)
        assertNull(backup.groupConfig)
    }

    @Test
    fun encodesLikeGson_defaultsWrittenAndNullsOmitted() {
        val backup = BackupData(
            date = "d",
            players = listOf(Player(id = 1, name = "Ana", groupName = "G", publicId = "p1")),
            history = emptyList(),
            logs = emptyList()
        )
        val json = AppJson.parseToJsonElement(AppJson.encodeToString(BackupData.serializer(), backup)).jsonObject
        assertEquals("1", json["version"].toString())
        assertFalse("groupConfig" in json, "nulos são omitidos, como no Gson")
        val player = json["players"].toString()
        assertTrue("\"elo\":1200.0" in player, player)
        assertTrue("\"isPriority\":false" in player, player)
        assertFalse("preferredPosition" in player, player)
    }

    @Test
    fun decodesGameStateSavedByGson() {
        // Gson grava Map<Int, *> com chaves string e omite campos nulos (ex.: streakOwner).
        val json = """
            {"groupName":"G","teamA":[],"teamB":[],"waitingList":[],"presentPlayerIds":[1,2],
             "scoreA":3,"scoreB":5,"currentStreak":0,"hasPreviousMatch":false,
             "lastWinners":[],"lastLosers":[],"restingPlayers":{"1":2},"assignedPositions":{"2":"SETTER"}}
        """.trimIndent()
        val snapshot = AppJson.decodeFromString(GameStateSnapshot.serializer(), json)
        assertNull(snapshot.streakOwner)
        assertEquals(mapOf(1 to 2), snapshot.restingPlayers)
        assertEquals(mapOf(2 to "SETTER"), snapshot.assignedPositions)
        assertEquals(emptyMap(), snapshot.assignedSlotIndices)
        assertEquals(listOf(1, 2), snapshot.presentPlayerIds)

        val roundTrip = AppJson.decodeFromString(
            GameStateSnapshot.serializer(),
            AppJson.encodeToString(GameStateSnapshot.serializer(), snapshot)
        )
        assertEquals(snapshot, roundTrip)
    }
}

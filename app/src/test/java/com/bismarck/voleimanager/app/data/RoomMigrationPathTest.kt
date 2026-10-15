package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_ROOM_PATH = "room_migration_path_test_db"

/**
 * Os demais testes aplicam as migrações manualmente; este abre um banco legado (v6) direto com o
 * Room usando a mesma configuração de produção ([buildAppDatabase]), garantindo que o próprio Room
 * executa as migrações multiplataforma (`migrate(SQLiteConnection)`) sem cair no fallback
 * destrutivo — os dados existentes precisam sobreviver.
 */
@RunWith(RobolectricTestRunner::class)
class RoomMigrationPathTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_ROOM_PATH)
    }

    @Test
    fun roomUpgradesVersion6DatabaseKeepingData() {
        val legacyDb = createVersion6Database(context, TEST_DB_ROOM_PATH)
        legacyDb.execSQL(
            "INSERT INTO players (id, name, elo, matchesPlayed, victories, isPriority, groupName, dailyToll, tollDate) " +
                "VALUES (1, 'Ana', 1300.0, 2, 1, 0, 'Grupo', 0, '')"
        )
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 5)"
        )
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_ROOM_PATH)
            .buildAppDatabase()
        try {
            val players = runBlocking { room.voleiDao().getPlayersByGroupSync("Grupo") }
            assertEquals(listOf("Ana"), players.map { it.name })
            assertEquals(1300.0, players.single().elo, 0.0)
            assertTrue(players.single().publicId.isNotBlank())

            val config = runBlocking { room.voleiDao().getGroupConfig("Grupo") }
            assertTrue(config != null && config.publicId.isNotBlank())
            assertEquals(17, room.openHelper.writableDatabase.version)
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_ROOM_PATH)
        }
    }
}

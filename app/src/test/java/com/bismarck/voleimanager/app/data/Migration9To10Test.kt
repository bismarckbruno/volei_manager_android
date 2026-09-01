package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_9_10 = "migration_9_10_test_db"

/**
 * Chega até a versão 9 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_9_10] e abre
 * com Room na versão 10 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre o backfill de `publicId` (UUID estável) em `players` e `group_configs`,
 * preparação de terreno para uma futura identidade "de nuvem" independente do id local/groupName.
 */
@RunWith(RobolectricTestRunner::class)
class Migration9To10Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_9_10)
    }

    @Test
    fun migrate9To10_backfillsDistinctPublicIdsForExistingRows() {
        val legacyDb = createVersion6Database(context, TEST_DB_9_10)
        legacyDb.execSQL(
            "INSERT INTO players (id, name, elo, matchesPlayed, victories, isPriority, groupName, dailyToll, tollDate) " +
                "VALUES (1, 'Ana', 1200.0, 0, 0, 0, 'Grupo', 0, '')"
        )
        legacyDb.execSQL(
            "INSERT INTO players (id, name, elo, matchesPlayed, victories, isPriority, groupName, dailyToll, tollDate) " +
                "VALUES (2, 'Bia', 1200.0, 0, 0, 0, 'Grupo', 0, '')"
        )
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 5)"
        )
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Outro Grupo', 6, 3, 1, 1, 'REBALANCE', 5)"
        )
        AppDatabase.MIGRATION_6_7.migrate(legacyDb)
        AppDatabase.MIGRATION_7_8.migrate(legacyDb)
        AppDatabase.MIGRATION_8_9.migrate(legacyDb)

        AppDatabase.MIGRATION_9_10.migrate(legacyDb)
        legacyDb.version = 10
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_9_10)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(10, migratedDb.version)

            val playerPublicIds = mutableListOf<String>()
            migratedDb.query("SELECT publicId FROM players ORDER BY id").use {
                while (it.moveToNext()) {
                    val publicId = it.getString(0)
                    assertTrue(publicId.isNotBlank())
                    playerPublicIds.add(publicId)
                }
            }
            assertEquals(2, playerPublicIds.size)
            assertNotEquals(playerPublicIds[0], playerPublicIds[1])

            val groupPublicIds = mutableListOf<String>()
            migratedDb.query("SELECT publicId FROM group_configs ORDER BY groupName").use {
                while (it.moveToNext()) {
                    val publicId = it.getString(0)
                    assertTrue(publicId.isNotBlank())
                    groupPublicIds.add(publicId)
                }
            }
            assertEquals(2, groupPublicIds.size)
            assertNotEquals(groupPublicIds[0], groupPublicIds[1])
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_9_10)
        }
    }
}

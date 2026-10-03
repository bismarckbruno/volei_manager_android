package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_16_17 = "migration_16_17_test_db"

/**
 * Chega até a versão 16 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_16_17] e
 * abre com Room na versão 17 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre a nova coluna `matchHistoryId` em `elo_logs` (`undo-last-match`), nula por
 * padrão para logs já existentes.
 */
@RunWith(RobolectricTestRunner::class)
class Migration16To17Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_16_17)
    }

    @Test
    fun migrate16To17_addsMatchHistoryIdColumnWithNullDefault() {
        val legacyDb = createVersion6Database(context, TEST_DB_16_17)
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 5)"
        )
        AppDatabase.MIGRATION_6_7.migrate(legacyDb)
        AppDatabase.MIGRATION_7_8.migrate(legacyDb)
        AppDatabase.MIGRATION_8_9.migrate(legacyDb)
        AppDatabase.MIGRATION_9_10.migrate(legacyDb)
        AppDatabase.MIGRATION_10_11.migrate(legacyDb)
        AppDatabase.MIGRATION_11_12.migrate(legacyDb)
        AppDatabase.MIGRATION_12_13.migrate(legacyDb)
        AppDatabase.MIGRATION_13_14.migrate(legacyDb)
        AppDatabase.MIGRATION_14_15.migrate(legacyDb)
        AppDatabase.MIGRATION_15_16.migrate(legacyDb)

        legacyDb.execSQL(
            "INSERT INTO elo_logs (playerId, playerNameSnapshot, date, elo, groupName) " +
                "VALUES (1, 'Jogador', '2024-01-01', 1200.0, 'Grupo')"
        )

        AppDatabase.MIGRATION_16_17.migrate(legacyDb)
        legacyDb.version = 17
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_16_17)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16,
                AppDatabase.MIGRATION_16_17
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(17, migratedDb.version)

            migratedDb.query("SELECT matchHistoryId FROM elo_logs WHERE groupName = 'Grupo'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertTrue(cursor.isNull(0))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_16_17)
        }
    }
}

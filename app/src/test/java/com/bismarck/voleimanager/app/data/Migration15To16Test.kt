package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_15_16 = "migration_15_16_test_db"

/**
 * Chega até a versão 15 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_15_16] e
 * abre com Room na versão 16 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre as novas colunas `historyBackfilledAt` (`history-backfill`) e
 * `activeAdminDeviceId`/`activeAdminSince` (`admin-session-transfer`), todas nulas por padrão
 * para grupos já existentes (nenhum backfill rodou ainda, nenhuma restrição de aparelho ativa).
 */
@RunWith(RobolectricTestRunner::class)
class Migration15To16Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_15_16)
    }

    @Test
    fun migrate15To16_addsBackfillAndAdminSessionColumnsWithNullDefaults() {
        val legacyDb = createVersion6Database(context, TEST_DB_15_16)
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
        legacyDb.version = 16
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_15_16)
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
                AppDatabase.MIGRATION_15_16
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(16, migratedDb.version)

            migratedDb.query(
                "SELECT historyBackfilledAt, activeAdminDeviceId, activeAdminSince FROM group_configs WHERE groupName = 'Grupo'"
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals(true, cursor.isNull(0))
                assertNull(cursor.getString(1))
                assertEquals(true, cursor.isNull(2))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_15_16)
        }
    }
}

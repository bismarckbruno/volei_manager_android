package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_10_11 = "migration_10_11_test_db"

/**
 * Chega até a versão 10 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_10_11] e
 * abre com Room na versão 11 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre os novos campos de sincronização premium em `group_configs`
 * (isCloudSynced, cloudGroupId, lastPremiumSwitchAt), todos com defaults seguros para grupos
 * já existentes (nenhum grupo pré-existente vira sincronizado automaticamente).
 */
@RunWith(RobolectricTestRunner::class)
class Migration10To11Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_10_11)
    }

    @Test
    fun migrate10To11_addsCloudSyncColumnsWithSafeDefaults() {
        val legacyDb = createVersion6Database(context, TEST_DB_10_11)
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
        legacyDb.version = 12
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_10_11)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(12, migratedDb.version)

            migratedDb.query(
                "SELECT isCloudSynced, cloudGroupId, lastPremiumSwitchAt FROM group_configs WHERE groupName = 'Grupo'"
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertFalse(cursor.getInt(0) == 1)
                assertNull(cursor.getString(1))
                assertEquals(true, cursor.isNull(2))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_10_11)
        }
    }
}

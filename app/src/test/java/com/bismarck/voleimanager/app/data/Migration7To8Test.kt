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

private const val TEST_DB_7_8 = "migration_7_8_test_db"

/**
 * Chega até a versão 7 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_7_8] e abre
 * com Room na versão 8 — a abertura falha se o esquema migrado divergir do esperado pelas entidades.
 */
@RunWith(RobolectricTestRunner::class)
class Migration7To8Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_7_8)
    }

    @Test
    fun migrate7To8_addsGuaranteeSetterEnabledByDefault() {
        val legacyDb = createVersion6Database(context, TEST_DB_7_8)
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 3)"
        )
        AppDatabase.MIGRATION_6_7.migrate(legacyDb)
        legacyDb.execSQL("UPDATE group_configs SET groupType = 'FIXED_POSITIONS' WHERE groupName = 'Grupo'")

        AppDatabase.MIGRATION_7_8.migrate(legacyDb)
        // A migração seguinte é aplicada para que o Room possa abrir o banco na versão atual.
        AppDatabase.MIGRATION_8_9.migrate(legacyDb)
        legacyDb.version = 9
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_7_8)
            .addMigrations(AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(10, migratedDb.version)

            migratedDb.query(
                "SELECT guaranteeSetter, groupType, teamSize, victoryLimit FROM group_configs WHERE groupName = 'Grupo'"
            ).use {
                assertTrue(it.moveToFirst())
                // Grupos existentes passam a ter a garantia de levantador ligada.
                assertEquals(1, it.getInt(0))
                assertEquals("FIXED_POSITIONS", it.getString(1))
                assertEquals(6, it.getInt(2))
                assertEquals(3, it.getInt(3))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_7_8)
        }
    }
}

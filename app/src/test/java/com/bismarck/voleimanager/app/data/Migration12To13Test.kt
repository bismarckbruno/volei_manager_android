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

private const val TEST_DB_12_13 = "migration_12_13_test_db"

/**
 * Chega até a versão 12 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_12_13] e
 * abre com Room na versão 13 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre as novas colunas `remoteRole` (grupo de outra pessoa, entrado via código) e
 * `pendingOwnershipTransferTo` (pedido de transferência de posse), ambas nulas por padrão
 * (grupos existentes continuam sendo locais/próprios).
 */
@RunWith(RobolectricTestRunner::class)
class Migration12To13Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_12_13)
    }

    @Test
    fun migrate12To13_addsRemoteGroupColumnsWithSafeDefaults() {
        val legacyDb = createVersion6Database(context, TEST_DB_12_13)
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
        legacyDb.version = 15
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_12_13)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(15, migratedDb.version)

            migratedDb.query(
                "SELECT remoteRole, pendingOwnershipTransferTo FROM group_configs WHERE groupName = 'Grupo'"
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertNull(cursor.getString(0))
                assertNull(cursor.getString(1))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_12_13)
        }
    }
}

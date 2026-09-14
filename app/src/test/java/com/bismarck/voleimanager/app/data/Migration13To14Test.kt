package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB_13_14 = "migration_13_14_test_db"

/**
 * Chega até a versão 13 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_13_14] e
 * abre com Room na versão 14 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre as novas colunas `shareHistoryWithObservers` e `showEloToObservers` (espelho
 * local de `cloudGroups/{id}.visibility.*` no Firestore, ver `CloudSyncManager`), ambas `false`
 * por padrão (grupos existentes continuam sem compartilhar histórico/Elo com observadores).
 */
@RunWith(RobolectricTestRunner::class)
class Migration13To14Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_13_14)
    }

    @Test
    fun migrate13To14_addsVisibilityColumnsWithSafeDefaults() {
        val legacyDb = createVersion6Database(context, TEST_DB_13_14)
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
        legacyDb.version = 14
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_13_14)
            .addMigrations(
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
                AppDatabase.MIGRATION_8_9,
                AppDatabase.MIGRATION_9_10,
                AppDatabase.MIGRATION_10_11,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_13_14
            )
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(14, migratedDb.version)

            migratedDb.query(
                "SELECT shareHistoryWithObservers, showEloToObservers FROM group_configs WHERE groupName = 'Grupo'"
            ).use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
                assertEquals(0, cursor.getInt(1))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_13_14)
        }
    }
}

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

private const val TEST_DB_8_9 = "migration_8_9_test_db"

/**
 * Chega até a versão 8 pelo caminho real de atualização, aplica [AppDatabase.MIGRATION_8_9] e abre
 * com Room na versão 9 — a abertura falha se o esquema migrado divergir do esperado pelas
 * entidades. Cobre o terreno preparado para partidas com sets (BO1/BO3/BO5) e para a futura
 * classificação de torneios (sistema FIVB de 3 pontos).
 */
@RunWith(RobolectricTestRunner::class)
class Migration8To9Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB_8_9)
    }

    @Test
    fun migrate8To9_addsSetAndTournamentStandingsColumnsWithSafeDefaults() {
        val legacyDb = createVersion6Database(context, TEST_DB_8_9)
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 3)"
        )
        legacyDb.execSQL(
            "INSERT INTO match_history (date, teamA, teamB, teamAIds, teamBIds, winner, eloPoints, groupName, teamAScore, teamBScore) " +
                "VALUES ('2024-01-01', 'Ana, Bia', 'Caio, Dani', '1,2', '3,4', 'Ana, Bia', 16.0, 'Grupo', 25, 20)"
        )
        AppDatabase.MIGRATION_6_7.migrate(legacyDb)
        legacyDb.execSQL(
            "INSERT INTO tournament_teams (groupName, teamKey, isActive, createdAt) VALUES ('Grupo', 'A', 1, 0)"
        )
        legacyDb.execSQL(
            "INSERT INTO tournament_matches (groupName, phase, roundIndex, orderInRound, status) " +
                "VALUES ('Grupo', 'KNOCKOUT', 0, 0, 'PENDING')"
        )
        AppDatabase.MIGRATION_7_8.migrate(legacyDb)

        AppDatabase.MIGRATION_8_9.migrate(legacyDb)
        legacyDb.version = 9
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB_8_9)
            .addMigrations(AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .build()

        try {
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(10, migratedDb.version)

            migratedDb.query(
                "SELECT matchFormat, regularSetPoints, tiebreakSetPoints, winByTwo FROM group_configs WHERE groupName = 'Grupo'"
            ).use {
                assertTrue(it.moveToFirst())
                assertEquals("BO1", it.getString(0))
                assertEquals(25, it.getInt(1))
                assertEquals(15, it.getInt(2))
                assertEquals(1, it.getInt(3))
            }

            migratedDb.query(
                "SELECT matchFormat, setScores, teamASetsWon, teamBSetsWon, teamAScore, teamBScore FROM match_history WHERE groupName = 'Grupo'"
            ).use {
                assertTrue(it.moveToFirst())
                assertTrue(it.isNull(0))
                assertTrue(it.isNull(1))
                assertTrue(it.isNull(2))
                assertTrue(it.isNull(3))
                // Placar legado (BO1) preservado sem alteração de significado.
                assertEquals(25, it.getInt(4))
                assertEquals(20, it.getInt(5))
            }

            migratedDb.query(
                "SELECT matchesPlayed, wins, losses, setsWon, setsLost, pointsWon, pointsLost, tournamentPoints " +
                    "FROM tournament_teams WHERE groupName = 'Grupo' AND teamKey = 'A'"
            ).use {
                assertTrue(it.moveToFirst())
                for (col in 0..7) {
                    assertEquals(0, it.getInt(col))
                }
            }

            migratedDb.query(
                "SELECT matchFormat, homeSetsWon, awaySetsWon, setScores FROM tournament_matches WHERE groupName = 'Grupo'"
            ).use {
                assertTrue(it.moveToFirst())
                assertTrue(it.isNull(0))
                assertTrue(it.isNull(1))
                assertTrue(it.isNull(2))
                assertTrue(it.isNull(3))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB_8_9)
        }
    }
}

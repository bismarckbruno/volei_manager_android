package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val TEST_DB = "migration_test_db"

/**
 * Cria o banco no esquema da versão 6, aplica [AppDatabase.MIGRATION_6_7] e abre com Room na
 * versão 7 — a abertura falha se o esquema migrado divergir do esperado pelas entidades.
 */
@RunWith(RobolectricTestRunner::class)
class Migration6To7Test {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate6To7_preservesDataAndMatchesRoomSchema() {
        val legacyDb = createVersion6Database()
        legacyDb.execSQL(
            "INSERT INTO players (id, name, elo, matchesPlayed, victories, isPriority, groupName, dailyToll, tollDate) " +
                "VALUES (1, 'Ana', 1300.0, 4, 2, 1, 'Grupo', 0, '')"
        )
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Grupo', 6, 3, 1, 1, 'REBALANCE', 3)"
        )
        legacyDb.execSQL(
            "INSERT INTO group_configs (groupName, teamSize, victoryLimit, priorityEnabled, scoreEnabled, balancingMode, onboardingStep) " +
                "VALUES ('Novo', 6, 3, 1, 1, 'REBALANCE', 0)"
        )
        AppDatabase.MIGRATION_6_7.migrate(legacyDb)
        legacyDb.version = 7
        legacyDb.close()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(AppDatabase.MIGRATION_6_7)
            .build()

        try {
            // Abre o banco: o Room valida aqui o esquema resultante da migração.
            val dao = room.voleiDao()
            val migratedDb = room.openHelper.writableDatabase
            assertEquals(7, migratedDb.version)

            migratedDb.query(
                "SELECT groupType, tournamentFormat, tournamentStarted, onboardingStep FROM group_configs WHERE groupName = 'Grupo'"
            ).use {
                assertTrue(it.moveToFirst())
                assertEquals("RECREATIONAL", it.getString(0))
                assertTrue(it.isNull(1))
                assertEquals(0, it.getInt(2))
                // Passo 3 (TEAM_SIZE antigo) vira 4 com o novo passo de tipo de grupo.
                assertEquals(4, it.getInt(3))
            }

            // Quem estava no passo do nome do grupo (0) não é deslocado.
            migratedDb.query("SELECT onboardingStep FROM group_configs WHERE groupName = 'Novo'").use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }

            migratedDb.query("SELECT name, preferredPosition, secondaryPosition FROM players WHERE id = 1").use {
                assertTrue(it.moveToFirst())
                assertEquals("Ana", it.getString(0))
                assertTrue(it.isNull(1))
                assertTrue(it.isNull(2))
            }

            // As novas tabelas existem e são consultáveis pelo DAO gerado.
            runBlocking {
                assertEquals(emptyList<Any>(), dao.getTournamentTeamsByGroupSync("Grupo"))
                assertEquals(emptyList<Any>(), dao.getTournamentTeamMembersByGroupSync("Grupo"))
                assertEquals(emptyList<Any>(), dao.getTournamentMatchesByGroupSync("Grupo"))
                assertEquals(emptyList<Any>(), dao.getGroupLogsByGroupSync("Grupo"))
            }
        } finally {
            room.close()
            context.deleteDatabase(TEST_DB)
        }
    }

    private fun createVersion6Database(): SupportSQLiteDatabase {
        val callback = object : SupportSQLiteOpenHelper.Callback(6) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `players` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `elo` REAL NOT NULL,
                        `matchesPlayed` INTEGER NOT NULL,
                        `victories` INTEGER NOT NULL,
                        `isPriority` INTEGER NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `dailyToll` INTEGER NOT NULL,
                        `tollDate` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_players_groupName_elo` ON `players` (`groupName`, `elo`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `match_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `teamA` TEXT NOT NULL,
                        `teamB` TEXT NOT NULL,
                        `teamAIds` TEXT NOT NULL,
                        `teamBIds` TEXT NOT NULL,
                        `winner` TEXT NOT NULL,
                        `eloPoints` REAL NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `teamAAverageElo` REAL,
                        `teamBAverageElo` REAL,
                        `teamAScore` INTEGER,
                        `teamBScore` INTEGER,
                        `startTimestamp` INTEGER,
                        `endTimestamp` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_match_history_groupName_id` ON `match_history` (`groupName`, `id`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `group_configs` (
                        `groupName` TEXT NOT NULL,
                        `teamSize` INTEGER NOT NULL,
                        `victoryLimit` INTEGER NOT NULL,
                        `priorityEnabled` INTEGER NOT NULL,
                        `scoreEnabled` INTEGER NOT NULL,
                        `balancingMode` TEXT NOT NULL,
                        `onboardingStep` INTEGER NOT NULL,
                        PRIMARY KEY(`groupName`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `elo_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `playerId` INTEGER NOT NULL,
                        `playerNameSnapshot` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `elo` REAL NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `won` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_playerId` ON `elo_logs` (`playerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_groupName_date` ON `elo_logs` (`groupName`, `date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_elo_logs_groupName_playerId_date` ON `elo_logs` (`groupName`, `playerId`, `date`)")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }

        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DB)
            .callback(callback)
            .build()

        return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
    }
}

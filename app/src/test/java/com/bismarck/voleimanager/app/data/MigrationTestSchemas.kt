package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

/**
 * Esquema legado da versão 6 do banco, compartilhado pelos testes de migração.
 *
 * Migrações posteriores são aplicadas em cima deste esquema para reproduzir o caminho real de
 * atualização de um usuário antigo.
 */
internal fun createVersion6Database(context: Context, dbName: String): SupportSQLiteDatabase {
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
        .name(dbName)
        .callback(callback)
        .build()

    return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
}

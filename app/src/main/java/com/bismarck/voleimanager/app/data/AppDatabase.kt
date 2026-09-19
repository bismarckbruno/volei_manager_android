package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bismarck.voleimanager.app.data.model.GroupConfig
import com.bismarck.voleimanager.app.data.model.MatchHistory
import com.bismarck.voleimanager.app.data.model.Player
import com.bismarck.voleimanager.app.data.model.PlayerEloLog

@Database(
    entities = [
        com.bismarck.voleimanager.app.data.model.Player::class,
        com.bismarck.voleimanager.app.data.model.MatchHistory::class,
        com.bismarck.voleimanager.app.data.model.GroupConfig::class,
        com.bismarck.voleimanager.app.data.model.PlayerEloLog::class,
        com.bismarck.voleimanager.app.data.model.TournamentTeam::class,
        com.bismarck.voleimanager.app.data.model.TournamentTeamMember::class,
        com.bismarck.voleimanager.app.data.model.TournamentMatch::class,
        com.bismarck.voleimanager.app.data.model.GroupLog::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voleiDao(): com.bismarck.voleimanager.app.data.VoleiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE group_configs ADD COLUMN balancingMode TEXT NOT NULL DEFAULT 'REBALANCE'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE group_configs ADD COLUMN onboardingStep INTEGER NOT NULL DEFAULT 2")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE group_configs SET onboardingStep = onboardingStep + 2")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamAIds TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBIds TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_players_groupName_elo ON players(groupName, elo)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_match_history_groupName_id ON match_history(groupName, id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elo_logs_groupName_date ON elo_logs(groupName, date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_elo_logs_groupName_playerId_date ON elo_logs(groupName, playerId, date)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Posições preferidas dos jogadores (modos com posições fixas)
                db.execSQL("ALTER TABLE players ADD COLUMN preferredPosition TEXT")
                db.execSQL("ALTER TABLE players ADD COLUMN secondaryPosition TEXT")

                // Tipo de grupo e dados de campeonato
                db.execSQL("ALTER TABLE group_configs ADD COLUMN groupType TEXT NOT NULL DEFAULT 'RECREATIONAL'")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN tournamentFormat TEXT")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN tournamentStarted INTEGER NOT NULL DEFAULT 0")

                // Novo passo de onboarding (tipo do grupo) inserido logo após o nome do grupo
                db.execSQL("UPDATE group_configs SET onboardingStep = onboardingStep + 1 WHERE onboardingStep >= 1")

                // Identidade dos times nas partidas de campeonato
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamAId INTEGER")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBId INTEGER")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamALabel TEXT")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBLabel TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tournament_teams` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `teamKey` TEXT NOT NULL,
                        `customName` TEXT,
                        `seed` INTEGER,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tournament_teams_groupName_teamKey` ON `tournament_teams` (`groupName`, `teamKey`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tournament_team_members` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `teamId` INTEGER NOT NULL,
                        `playerId` INTEGER NOT NULL,
                        `position` TEXT,
                        `isActive` INTEGER NOT NULL,
                        `joinedAt` INTEGER NOT NULL,
                        `leftAt` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_teamId` ON `tournament_team_members` (`groupName`, `teamId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_playerId` ON `tournament_team_members` (`groupName`, `playerId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tournament_matches` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `phase` TEXT NOT NULL,
                        `phaseLabel` TEXT,
                        `roundIndex` INTEGER NOT NULL,
                        `orderInRound` INTEGER NOT NULL,
                        `homeTeamId` INTEGER,
                        `awayTeamId` INTEGER,
                        `homeSourceMatchId` INTEGER,
                        `awaySourceMatchId` INTEGER,
                        `homeScore` INTEGER,
                        `awayScore` INTEGER,
                        `winnerTeamId` INTEGER,
                        `status` TEXT NOT NULL,
                        `matchHistoryId` INTEGER,
                        `startTimestamp` INTEGER,
                        `endTimestamp` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_matches_groupName_phase_roundIndex_orderInRound` ON `tournament_matches` (`groupName`, `phase`, `roundIndex`, `orderInRound`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `group_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `groupName` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `playerId` INTEGER,
                        `teamId` INTEGER,
                        `relatedTeamId` INTEGER,
                        `metadata` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_group_logs_groupName_timestamp` ON `group_logs` (`groupName`, `timestamp`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Garantia de levantador nos tipos com posições fixas (ligada por padrão)
                db.execSQL("ALTER TABLE group_configs ADD COLUMN guaranteeSetter INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Terreno para partidas com múltiplos sets (BO1/BO3/BO5); Elo continua por partida.
                db.execSQL("ALTER TABLE group_configs ADD COLUMN matchFormat TEXT NOT NULL DEFAULT 'BO1'")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN regularSetPoints INTEGER NOT NULL DEFAULT 25")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN tiebreakSetPoints INTEGER NOT NULL DEFAULT 15")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN winByTwo INTEGER NOT NULL DEFAULT 1")

                db.execSQL("ALTER TABLE match_history ADD COLUMN matchFormat TEXT")
                db.execSQL("ALTER TABLE match_history ADD COLUMN setScores TEXT")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamASetsWon INTEGER")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBSetsWon INTEGER")

                // Agregados de classificação de torneio (sistema FIVB de 3 pontos)
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN matchesPlayed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN wins INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN losses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN setsWon INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN setsLost INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN pointsWon INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN pointsLost INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tournament_teams ADD COLUMN tournamentPoints INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE tournament_matches ADD COLUMN matchFormat TEXT")
                db.execSQL("ALTER TABLE tournament_matches ADD COLUMN homeSetsWon INTEGER")
                db.execSQL("ALTER TABLE tournament_matches ADD COLUMN awaySetsWon INTEGER")
                db.execSQL("ALTER TABLE tournament_matches ADD COLUMN setScores TEXT")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Identidade estável (UUID) de jogadores e grupos, independente do id local
                // autoGenerate ou do groupName (editável) — preparação de terreno para uma futura
                // sincronização em nuvem (ex.: acompanhamento "ao vivo" de um grupo).
                db.execSQL("ALTER TABLE players ADD COLUMN publicId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN publicId TEXT NOT NULL DEFAULT ''")

                // Backfill linha a linha: cada registro existente precisa de um UUID distinto,
                // o que não é possível fazer com um único UPDATE em massa.
                db.query("SELECT id FROM players").use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(0)
                        db.execSQL(
                            "UPDATE players SET publicId = ? WHERE id = ?",
                            arrayOf<Any>(java.util.UUID.randomUUID().toString(), id)
                        )
                    }
                }
                db.query("SELECT groupName FROM group_configs").use { cursor ->
                    while (cursor.moveToNext()) {
                        val groupName = cursor.getString(0)
                        db.execSQL(
                            "UPDATE group_configs SET publicId = ? WHERE groupName = ?",
                            arrayOf(java.util.UUID.randomUUID().toString(), groupName)
                        )
                    }
                }

                // Índices únicos criados só depois do backfill, para não colidir com o default ''.
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_players_publicId ON players(publicId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_group_configs_publicId ON group_configs(publicId)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Terreno para a sincronização premium em nuvem: flag local de "grupo premium
                // sincronizado", o id do documento em nuvem (igual ao publicId quando ativo) e o
                // timestamp da última troca de grupo premium (espelha o valor validado no
                // backend, usado só para bloqueio otimista na UI).
                db.execSQL("ALTER TABLE group_configs ADD COLUMN isCloudSynced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN cloudGroupId TEXT")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN lastPremiumSwitchAt INTEGER")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Cores de time A/B definidas pelo organizador/auxiliar do grupo (nulo = padrão
                // azul/amarelo) — aplicam-se a todos que visualizam o grupo, com possibilidade de
                // sobreposição só-local para um visualizador premium (guardada nas preferências,
                // não no banco).
                db.execSQL("ALTER TABLE group_configs ADD COLUMN teamAColorName TEXT")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN teamBColorName TEXT")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Terreno para "grupo de outra pessoa" (entrado via código de auxiliar/espectador)
                // e para o pedido de transferência de posse de um grupo premium.
                db.execSQL("ALTER TABLE group_configs ADD COLUMN remoteRole TEXT")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN pendingOwnershipTransferTo TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Espelho local dos toggles de visibilidade do grupo para observadores
                // (`cloudGroups/{id}.visibility.*` no Firestore, ver CloudSyncManager) —
                // desligados por padrão, igual ao valor inicial criado pela Cloud Function
                // `switchPremiumGroup`.
                db.execSQL("ALTER TABLE group_configs ADD COLUMN shareHistoryWithObservers INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN showEloToObservers INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Quando `shareHistoryWithObservers` está ligado, o organizador/auxiliar pode
                // escolher entre expor o histórico completo do grupo aos observadores ou só as
                // partidas de hoje (`false` = completo, mantém o comportamento anterior).
                db.execSQL("ALTER TABLE group_configs ADD COLUMN shareOnlyTodayHistory INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // `history-backfill`: registra quando o histórico/Elo pré-existentes já subiram
                // para o Firestore, para não repetir o envio em lote a cada reativação do toggle.
                db.execSQL("ALTER TABLE group_configs ADD COLUMN historyBackfilledAt INTEGER")
                // `admin-session-transfer`: qual aparelho está autorizado a escrever como
                // organizador deste grupo em nuvem agora (null = sem restrição ainda).
                db.execSQL("ALTER TABLE group_configs ADD COLUMN activeAdminDeviceId TEXT")
                db.execSQL("ALTER TABLE group_configs ADD COLUMN activeAdminSince INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volei_manager_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

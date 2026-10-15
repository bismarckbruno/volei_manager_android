package com.bismarck.voleimanager.app.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.uuid.Uuid

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
    version = 17,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voleiDao(): com.bismarck.voleimanager.app.data.VoleiDao

    companion object {
        const val DATABASE_NAME = "volei_manager_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN balancingMode TEXT NOT NULL DEFAULT 'REBALANCE'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN onboardingStep INTEGER NOT NULL DEFAULT 2")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("UPDATE group_configs SET onboardingStep = onboardingStep + 2")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamAIds TEXT NOT NULL DEFAULT ''")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamBIds TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_players_groupName_elo ON players(groupName, elo)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_match_history_groupName_id ON match_history(groupName, id)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_elo_logs_groupName_date ON elo_logs(groupName, date)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_elo_logs_groupName_playerId_date ON elo_logs(groupName, playerId, date)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(connection: SQLiteConnection) {
                // Posições preferidas dos jogadores (modos com posições fixas)
                connection.execSQL("ALTER TABLE players ADD COLUMN preferredPosition TEXT")
                connection.execSQL("ALTER TABLE players ADD COLUMN secondaryPosition TEXT")

                // Tipo de grupo e dados de campeonato
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN groupType TEXT NOT NULL DEFAULT 'RECREATIONAL'")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN tournamentFormat TEXT")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN tournamentStarted INTEGER NOT NULL DEFAULT 0")

                // Novo passo de onboarding (tipo do grupo) inserido logo após o nome do grupo
                connection.execSQL("UPDATE group_configs SET onboardingStep = onboardingStep + 1 WHERE onboardingStep >= 1")

                // Identidade dos times nas partidas de campeonato
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamAId INTEGER")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamBId INTEGER")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamALabel TEXT")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamBLabel TEXT")

                connection.execSQL(
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
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tournament_teams_groupName_teamKey` ON `tournament_teams` (`groupName`, `teamKey`)")

                connection.execSQL(
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
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_teamId` ON `tournament_team_members` (`groupName`, `teamId`)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_team_members_groupName_playerId` ON `tournament_team_members` (`groupName`, `playerId`)")

                connection.execSQL(
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
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_tournament_matches_groupName_phase_roundIndex_orderInRound` ON `tournament_matches` (`groupName`, `phase`, `roundIndex`, `orderInRound`)")

                connection.execSQL(
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
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_group_logs_groupName_timestamp` ON `group_logs` (`groupName`, `timestamp`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                // Garantia de levantador nos tipos com posições fixas (ligada por padrão)
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN guaranteeSetter INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(connection: SQLiteConnection) {
                // Terreno para partidas com múltiplos sets (BO1/BO3/BO5); Elo continua por partida.
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN matchFormat TEXT NOT NULL DEFAULT 'BO1'")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN regularSetPoints INTEGER NOT NULL DEFAULT 25")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN tiebreakSetPoints INTEGER NOT NULL DEFAULT 15")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN winByTwo INTEGER NOT NULL DEFAULT 1")

                connection.execSQL("ALTER TABLE match_history ADD COLUMN matchFormat TEXT")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN setScores TEXT")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamASetsWon INTEGER")
                connection.execSQL("ALTER TABLE match_history ADD COLUMN teamBSetsWon INTEGER")

                // Agregados de classificação de torneio (sistema FIVB de 3 pontos)
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN matchesPlayed INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN wins INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN losses INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN setsWon INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN setsLost INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN pointsWon INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN pointsLost INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE tournament_teams ADD COLUMN tournamentPoints INTEGER NOT NULL DEFAULT 0")

                connection.execSQL("ALTER TABLE tournament_matches ADD COLUMN matchFormat TEXT")
                connection.execSQL("ALTER TABLE tournament_matches ADD COLUMN homeSetsWon INTEGER")
                connection.execSQL("ALTER TABLE tournament_matches ADD COLUMN awaySetsWon INTEGER")
                connection.execSQL("ALTER TABLE tournament_matches ADD COLUMN setScores TEXT")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(connection: SQLiteConnection) {
                // Identidade estável (UUID) de jogadores e grupos, independente do id local
                // autoGenerate ou do groupName (editável) — preparação de terreno para uma futura
                // sincronização em nuvem (ex.: acompanhamento "ao vivo" de um grupo).
                connection.execSQL("ALTER TABLE players ADD COLUMN publicId TEXT NOT NULL DEFAULT ''")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN publicId TEXT NOT NULL DEFAULT ''")

                // Backfill linha a linha: cada registro existente precisa de um UUID distinto,
                // o que não é possível fazer com um único UPDATE em massa.
                val playerIds = mutableListOf<Long>()
                connection.prepare("SELECT id FROM players").use { statement ->
                    while (statement.step()) playerIds += statement.getLong(0)
                }
                playerIds.forEach { id ->
                    connection.prepare("UPDATE players SET publicId = ? WHERE id = ?").use { statement ->
                        statement.bindText(1, Uuid.random().toString())
                        statement.bindLong(2, id)
                        statement.step()
                    }
                }
                val groupNames = mutableListOf<String>()
                connection.prepare("SELECT groupName FROM group_configs").use { statement ->
                    while (statement.step()) groupNames += statement.getText(0)
                }
                groupNames.forEach { groupName ->
                    connection.prepare("UPDATE group_configs SET publicId = ? WHERE groupName = ?").use { statement ->
                        statement.bindText(1, Uuid.random().toString())
                        statement.bindText(2, groupName)
                        statement.step()
                    }
                }

                // Índices únicos criados só depois do backfill, para não colidir com o default ''.
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_players_publicId ON players(publicId)")
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_group_configs_publicId ON group_configs(publicId)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(connection: SQLiteConnection) {
                // Terreno para a sincronização premium em nuvem: flag local de "grupo premium
                // sincronizado", o id do documento em nuvem (igual ao publicId quando ativo) e o
                // timestamp da última troca de grupo premium (espelha o valor validado no
                // backend, usado só para bloqueio otimista na UI).
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN isCloudSynced INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN cloudGroupId TEXT")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN lastPremiumSwitchAt INTEGER")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(connection: SQLiteConnection) {
                // Cores de time A/B definidas pelo organizador/auxiliar do grupo (nulo = padrão
                // azul/amarelo) — aplicam-se a todos que visualizam o grupo, com possibilidade de
                // sobreposição só-local para um visualizador premium (guardada nas preferências,
                // não no banco).
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN teamAColorName TEXT")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN teamBColorName TEXT")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(connection: SQLiteConnection) {
                // Terreno para "grupo de outra pessoa" (entrado via código de auxiliar/espectador)
                // e para o pedido de transferência de posse de um grupo premium.
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN remoteRole TEXT")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN pendingOwnershipTransferTo TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(connection: SQLiteConnection) {
                // Espelho local dos toggles de visibilidade do grupo para observadores
                // (`cloudGroups/{id}.visibility.*` no Firestore, ver CloudSyncManager) —
                // desligados por padrão, igual ao valor inicial criado pela Cloud Function
                // `switchPremiumGroup`.
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN shareHistoryWithObservers INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN showEloToObservers INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(connection: SQLiteConnection) {
                // Quando `shareHistoryWithObservers` está ligado, o organizador/auxiliar pode
                // escolher entre expor o histórico completo do grupo aos observadores ou só as
                // partidas de hoje (`false` = completo, mantém o comportamento anterior).
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN shareOnlyTodayHistory INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(connection: SQLiteConnection) {
                // `history-backfill`: registra quando o histórico/Elo pré-existentes já subiram
                // para o Firestore, para não repetir o envio em lote a cada reativação do toggle.
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN historyBackfilledAt INTEGER")
                // `admin-session-transfer`: qual aparelho está autorizado a escrever como
                // organizador deste grupo em nuvem agora (null = sem restrição ainda).
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN activeAdminDeviceId TEXT")
                connection.execSQL("ALTER TABLE group_configs ADD COLUMN activeAdminSince INTEGER")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(connection: SQLiteConnection) {
                // `undo-last-match`: vincula cada log de elo à partida que o gerou, permitindo
                // localizar com precisão os logs de uma partida específica para desfazê-la (ver
                // VoleiViewModel.undoLastMatch) mesmo havendo várias partidas no mesmo dia.
                connection.execSQL("ALTER TABLE elo_logs ADD COLUMN matchHistoryId INTEGER")
            }
        }

        /** Todas as migrações, em ordem — usadas pelo builder de cada plataforma. */
        val ALL_MIGRATIONS: Array<Migration>
            get() = arrayOf(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
                MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15,
                MIGRATION_15_16, MIGRATION_16_17
            )
    }
}

// Implementado pelo compilador do Room (KSP) em cada plataforma.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Configuração comum do banco: migrações + fallback destrutivo (rede de segurança). Cada plataforma
 * cria o [RoomDatabase.Builder] (com o caminho do arquivo e, no iOS, o driver SQLite) e chama isto.
 */
fun RoomDatabase.Builder<AppDatabase>.buildAppDatabase(): AppDatabase =
    addMigrations(*AppDatabase.ALL_MIGRATIONS)
        .fallbackToDestructiveMigration(true)
        .build()

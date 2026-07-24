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
    entities = [com.bismarck.voleimanager.app.data.model.Player::class, com.bismarck.voleimanager.app.data.model.MatchHistory::class, com.bismarck.voleimanager.app.data.model.GroupConfig::class, com.bismarck.voleimanager.app.data.model.PlayerEloLog::class],
    version = 6,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volei_manager_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

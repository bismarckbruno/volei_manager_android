package com.bismarck.voleimanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bismarck.voleimanager.data.model.GroupConfig
import com.bismarck.voleimanager.data.model.MatchHistory
import com.bismarck.voleimanager.data.model.Player
import com.bismarck.voleimanager.data.model.PlayerEloLog

@Database(
    entities = [Player::class, MatchHistory::class, GroupConfig::class, PlayerEloLog::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voleiDao(): VoleiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamAAverageElo REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBAverageElo REAL DEFAULT NULL")
            }
        }
        
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamAScore INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE match_history ADD COLUMN teamBScore INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE group_configs ADD COLUMN scoreEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volei_manager_db"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
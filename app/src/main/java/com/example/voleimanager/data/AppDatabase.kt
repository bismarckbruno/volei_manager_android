package com.example.voleimanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player
import com.example.voleimanager.data.model.PlayerEloLog

// ATUALIZAÇÃO: Adicionamos PlayerEloLog e subimos a versão para 4
@Database(
    entities = [Player::class, MatchHistory::class, GroupConfig::class, PlayerEloLog::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voleiDao(): VoleiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volei_manager_db"
                )
                    .fallbackToDestructiveMigration() // Recria o banco se houver conflito de versão
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
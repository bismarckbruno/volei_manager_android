package com.example.voleimanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voleimanager.data.model.GroupConfig
import com.example.voleimanager.data.model.MatchHistory
import com.example.voleimanager.data.model.Player

// Adicionamos GroupConfig na lista e mudamos a version para 3
@Database(entities = [Player::class, MatchHistory::class, GroupConfig::class], version = 3, exportSchema = false)
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
                    .fallbackToDestructiveMigration() // Vai limpar o banco antigo para criar o novo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.bismarck.voleimanager.app.data

import android.content.Context
import androidx.room.Room

@Volatile
private var INSTANCE: AppDatabase? = null

/**
 * Banco do app no Android. Sem `setDriver`: mantém o SQLite do framework (SupportSQLite), o mesmo
 * motor usado desde sempre, para não alterar o comportamento do arquivo já existente nos aparelhos.
 */
fun AppDatabase.Companion.getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(AppDatabase::class) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).buildAppDatabase().also { INSTANCE = it }
    }
}

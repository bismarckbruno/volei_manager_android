package com.bismarck.voleimanager.app.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private var INSTANCE: AppDatabase? = null

/** Banco do app no iOS: arquivo em Application Support, com o SQLite embarcado (bundled). */
@OptIn(ExperimentalForeignApi::class)
fun createAppDatabase(): AppDatabase {
    INSTANCE?.let { return it }
    val directory = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    NSFileManager.defaultManager.createDirectoryAtPath(
        directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null
    )
    return Room.databaseBuilder<AppDatabase>(name = "$directory/${AppDatabase.DATABASE_NAME}")
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .buildAppDatabase()
        .also { INSTANCE = it }
}

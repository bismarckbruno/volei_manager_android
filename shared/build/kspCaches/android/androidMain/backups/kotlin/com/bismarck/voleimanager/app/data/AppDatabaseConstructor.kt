package com.bismarck.voleimanager.app.`data`

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  actual override fun initialize(): AppDatabase = com.bismarck.voleimanager.app.`data`.AppDatabase_Impl()
}

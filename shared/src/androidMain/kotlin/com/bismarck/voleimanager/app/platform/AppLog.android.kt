package com.bismarck.voleimanager.app.platform

import android.util.Log

internal actual fun platformLog(level: LogLevel, tag: String, message: String) {
    when (level) {
        LogLevel.DEBUG -> Log.d(tag, message)
        LogLevel.ERROR -> Log.e(tag, message)
    }
}

package com.bismarck.voleimanager.app.platform

/** Log multiplataforma (Logcat no Android, console no iOS). */
object AppLog {
    fun d(tag: String, message: String) = platformLog(LogLevel.DEBUG, tag, message)
    fun e(tag: String, message: String) = platformLog(LogLevel.ERROR, tag, message)
}

enum class LogLevel { DEBUG, ERROR }

internal expect fun platformLog(level: LogLevel, tag: String, message: String)

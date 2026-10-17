package com.bismarck.voleimanager.app.platform

internal actual fun platformLog(level: LogLevel, tag: String, message: String) {
    println("[${level.name}] $tag: $message")
}

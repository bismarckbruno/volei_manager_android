package com.bismarck.voleimanager.app.platform

import kotlinx.coroutines.runBlocking

internal actual fun <T> runBlockingCompat(block: suspend () -> T): T = runBlocking { block() }

package com.bismarck.voleimanager.app.platform

import kotlinx.coroutines.CoroutineDispatcher

/** Dispatcher para trabalho de disco/rede (Dispatchers.IO não é exposto no código comum). */
expect val ioDispatcher: CoroutineDispatcher

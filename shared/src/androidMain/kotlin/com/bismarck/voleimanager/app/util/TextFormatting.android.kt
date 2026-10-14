package com.bismarck.voleimanager.app.util

import java.text.NumberFormat
import java.text.Normalizer
import java.util.Locale

internal actual fun decomposeNfd(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)

internal actual fun formatWholeNumber(value: Double): String =
    NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(value)

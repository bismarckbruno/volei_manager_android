package com.bismarck.voleimanager.app.util

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSString
import platform.Foundation.currentLocale
import platform.Foundation.decomposedStringWithCanonicalMapping

@Suppress("CAST_NEVER_SUCCEEDS")
internal actual fun decomposeNfd(text: String): String =
    (text as NSString).decomposedStringWithCanonicalMapping

internal actual fun formatWholeNumber(value: Double): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        locale = NSLocale.currentLocale
        maximumFractionDigits = 0u
        minimumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toLong().toString()
}

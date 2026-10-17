package com.bismarck.voleimanager.app.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

// No iOS o gesto "voltar" é tratado pela navegação própria do app; efeitos de janela de diálogo
// específicos do Android não se aplicam. Pickers nativos entram em ios-actuals.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

@Composable
actual fun rememberDocumentPicker(onPicked: (String) -> Unit): (List<String>) -> Unit = {}

@Composable
actual fun rememberImagePicker(onPicked: (String) -> Unit): () -> Unit = {}

@Composable
actual fun DialogKeyboardAlwaysVisibleEffect() {
}

@Composable
actual fun DialogSystemBarsContrastEffect() {
}

actual fun currentLanguage(): String = NSLocale.currentLocale.languageCode

actual fun formatDecimal(value: Double): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = 2u
        minimumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}

actual fun formatInteger(value: Long): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(double = value.toDouble())) ?: value.toString()
}

actual fun formatCurrency(amount: Double, currencyCode: String): String? {
    if (currencyCode.length != 3) return null
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        this.currencyCode = currencyCode
    }
    return formatter.stringFromNumber(NSNumber(double = amount))
}

@Composable
actual fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? = null

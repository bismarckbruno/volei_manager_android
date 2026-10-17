package com.bismarck.voleimanager.app.ui.platform

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberDocumentPicker(onPicked: (String) -> Unit): (List<String>) -> Unit {
    val currentOnPicked = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { currentOnPicked.value(it.toString()) }
    }
    return { mimeTypes -> launcher.launch(mimeTypes.toTypedArray()) }
}

@Composable
actual fun rememberImagePicker(onPicked: (String) -> Unit): () -> Unit {
    val currentOnPicked = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { currentOnPicked.value(it.toString()) }
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

@Composable
actual fun DialogKeyboardAlwaysVisibleEffect() {
    val view = LocalView.current
    LaunchedEffect(view) {
        // AlertDialog hospeda o conteúdo numa janela cujo softInputMode não pede o IME por padrão.
        (view.parent as? DialogWindowProvider)?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )
    }
}

@Composable
actual fun DialogSystemBarsContrastEffect() {
    val view = LocalView.current
    LaunchedEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
    }
}

actual fun currentLanguage(): String = Locale.getDefault().language

actual fun formatDecimal(value: Double): String =
    NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }.format(value)

actual fun formatInteger(value: Long): String =
    NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

actual fun formatCurrency(amount: Double, currencyCode: String): String? = try {
    NumberFormat.getCurrencyInstance().apply { currency = Currency.getInstance(currencyCode) }.format(amount)
} catch (e: IllegalArgumentException) {
    null
}

@Composable
actual fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context: Context = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}

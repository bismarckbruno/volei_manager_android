package com.bismarck.voleimanager.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.data.createAppDatabase
import com.bismarck.voleimanager.app.platform.AppStrings
import com.bismarck.voleimanager.app.platform.FileRef
import com.bismarck.voleimanager.app.platform.IosDeviceInfo
import com.bismarck.voleimanager.app.platform.IosFileService
import com.bismarck.voleimanager.app.platform.IosSpreadsheetCodec
import com.bismarck.voleimanager.app.platform.NoOpTelemetry
import com.bismarck.voleimanager.app.platform.UnavailableAuthService
import com.bismarck.voleimanager.app.platform.UnavailableBillingService
import com.bismarck.voleimanager.app.platform.UnavailableCloudFunctionsService
import com.bismarck.voleimanager.app.platform.UnavailableCloudSyncService
import com.bismarck.voleimanager.app.platform.UnavailableLivePresenceService
import com.bismarck.voleimanager.app.platform.UnsupportedGoogleSignInService
import com.bismarck.voleimanager.app.platform.UserDefaultsPreferencesStore
import com.bismarck.voleimanager.app.ui.VoleiManagerApp
import com.bismarck.voleimanager.app.ui.platform.IosPlatformActions
import com.bismarck.voleimanager.app.ui.platform.LocalPlatformActions
import com.bismarck.voleimanager.app.ui.theme.AppTheme
import com.bismarck.voleimanager.app.ui.viewmodel.ThemeMode
import com.bismarck.voleimanager.app.ui.viewmodel.VoleiViewModel
import platform.UIKit.UIViewController

/** ViewModel único do app iOS (equivalente ao `ViewModelProvider` do `MainActivity`). */
private val iosViewModel: VoleiViewModel by lazy {
    VoleiViewModel(
        repository = VoleiRepository(createAppDatabase().voleiDao()),
        preferences = UserDefaultsPreferencesStore(),
        telemetry = NoOpTelemetry,
        fileService = IosFileService(),
        deviceInfo = IosDeviceInfo(),
        googleSignIn = UnsupportedGoogleSignInService,
        purchaseLauncher = { _, _ -> false },
        strings = AppStrings,
        auth = UnavailableAuthService,
        billing = UnavailableBillingService,
        cloudFunctions = UnavailableCloudFunctionsService,
        livePresence = UnavailableLivePresenceService,
        cloudSync = UnavailableCloudSyncService,
        spreadsheet = IosSpreadsheetCodec()
    )
}

/** Chamado pelo Swift quando o sistema abre um arquivo (.vlz) no app. */
fun handleOpenedFile(url: String) {
    iosViewModel.onExternalFileOpened(FileRef(url))
}

/** Ponto de entrada da UI no iOS: `MainViewControllerKt.MainViewController()` no Swift. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val actions = remember { IosPlatformActions() }
    val themeMode by iosViewModel.themeMode.collectAsState()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalPlatformActions provides actions) {
        AppTheme(darkTheme = darkTheme, dynamicColor = false) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                VoleiManagerApp(iosViewModel, darkTheme)
            }
        }
    }
}

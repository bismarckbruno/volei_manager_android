package com.bismarck.voleimanager.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bismarck.voleimanager.app.data.VoleiRepository
import com.bismarck.voleimanager.app.platform.AppStrings
import com.bismarck.voleimanager.app.platform.SharedPreferencesStore
import com.bismarck.voleimanager.app.platform.StringProvider
import com.bismarck.voleimanager.app.util.AndroidDeviceInfo
import com.bismarck.voleimanager.app.util.AndroidFileService
import com.bismarck.voleimanager.app.util.AndroidGoogleSignInService
import com.bismarck.voleimanager.app.util.AndroidSpreadsheetCodec
import com.bismarck.voleimanager.app.util.AuthManager
import com.bismarck.voleimanager.app.util.BillingManager
import com.bismarck.voleimanager.app.util.BillingPurchaseLauncher
import com.bismarck.voleimanager.app.util.CloudFunctionsManager
import com.bismarck.voleimanager.app.util.CloudSyncManager
import com.bismarck.voleimanager.app.util.FirebaseTelemetry
import com.bismarck.voleimanager.app.util.LivePresenceManager

/** Monta o [VoleiViewModel] com as implementações Android (Firebase, Play Billing, arquivos...). */
fun createVoleiViewModel(
    application: Application,
    repository: VoleiRepository,
    strings: StringProvider = AppStrings
): VoleiViewModel = VoleiViewModel(
    repository = repository,
    preferences = SharedPreferencesStore.create(application),
    telemetry = FirebaseTelemetry(application),
    fileService = AndroidFileService(application),
    deviceInfo = AndroidDeviceInfo(application),
    googleSignIn = AndroidGoogleSignInService(),
    purchaseLauncher = BillingPurchaseLauncher(),
    strings = strings,
    auth = AuthManager,
    billing = BillingManager,
    cloudFunctions = CloudFunctionsManager,
    livePresence = LivePresenceManager,
    cloudSync = CloudSyncManager,
    spreadsheet = AndroidSpreadsheetCodec(),
    onPlatformInit = {
        AuthManager.init(application)
        BillingManager.init(application)
    }
)

class VoleiViewModelFactory(
    private val application: Application,
    private val repository: VoleiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(VoleiViewModel::class.java)) {
            return createVoleiViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

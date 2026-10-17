package com.bismarck.voleimanager.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import com.bismarck.voleimanager.app.BuildConfig
import com.bismarck.voleimanager.app.R
import com.bismarck.voleimanager.app.platform.PurchaseLauncher
import com.bismarck.voleimanager.app.platform.DeviceInfo
import com.bismarck.voleimanager.app.platform.GoogleSignInService
import com.bismarck.voleimanager.app.platform.PlatformHost

/** [PlatformHost] Android: encapsula o contexto (de Activity) usado para exibir UIs do sistema. */
class AndroidPlatformHost(val context: Context) : PlatformHost {
    val activity: Activity?
        get() {
            var current: Context? = context
            while (current is ContextWrapper) {
                if (current is Activity) return current
                current = current.baseContext
            }
            return null
        }
}

/** `Settings.Secure.ANDROID_ID`, com fallback aleatório se indisponível. */
class AndroidDeviceInfo(context: Context) : DeviceInfo {
    override val isDebugBuild: Boolean = BuildConfig.DEBUG

    private val appContext = context.applicationContext

    override val deviceId: String by lazy {
        try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
                .takeUnless { it.isNullOrBlank() } ?: java.util.UUID.randomUUID().toString()
        } catch (e: Exception) {
            java.util.UUID.randomUUID().toString()
        }
    }
}

/** Credential Manager + One Tap (ver [GoogleSignInHelper]); exige um [AndroidPlatformHost] de Activity. */
class AndroidGoogleSignInService : GoogleSignInService {
    override suspend fun getIdToken(host: PlatformHost): Result<String> {
        val context = (host as AndroidPlatformHost).context
        return GoogleSignInHelper.getIdToken(context, context.getString(R.string.google_web_client_id))
    }
}

/** Abre o fluxo de compra de uma assinatura; `true` se foi iniciado. */
class BillingPurchaseLauncher : PurchaseLauncher {
    override fun launch(host: PlatformHost, offer: SubscriptionOffer): Boolean {
        val activity = (host as AndroidPlatformHost).activity ?: return false
        return BillingManager.launchPurchaseFlow(activity, offer)
    }
}

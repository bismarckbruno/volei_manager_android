package com.bismarck.voleimanager.app.util

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics

/**
 * Fachada única sobre Firebase Analytics + Crashlytics para a telemetria anônima e opcional
 * descrita na seção 2 da PRIVACY_POLICY.md.
 *
 * Regras que este objeto garante, independentemente da tela que o chame:
 * - Nada é coletado antes do usuário dar consentimento explícito ([applyConsent] com `granted =
 *   true`). O manifest já define `firebase_*_collection_enabled = false` por padrão; este objeto
 *   é quem liga/desliga a coleta em runtime a partir da preferência persistida.
 * - Nenhum evento aqui aceita texto livre (nome de jogador/grupo, conteúdo de partida): apenas
 *   enums e contagens, para nunca violar a promessa de anonimato da política de privacidade.
 * - Se o app não tiver um google-services.json válido (ambiente de desenvolvimento sem Firebase
 *   configurado), o Firebase simplesmente não inicializa; todas as chamadas abaixo são no-op
 *   seguro (try/catch), nunca derrubando o app.
 *
 * Ao adicionar um novo evento de negócio, crie um método tipado aqui (não espalhe
 * `FirebaseAnalytics.getInstance(...).logEvent("string solta", ...)` pela ViewModel).
 */
object TelemetryManager {

    private const val TAG = "TelemetryManager"
    const val PREF_KEY_TELEMETRY_ENABLED = "telemetry_enabled"

    private var consentGranted = false

    private fun analyticsOrNull(context: Context): FirebaseAnalytics? = try {
        Firebase.analytics
    } catch (e: Exception) {
        Log.d(TAG, "Firebase Analytics indisponível: ${e.message}")
        null
    }

    private fun crashlyticsOrNull(): FirebaseCrashlytics? = try {
        Firebase.crashlytics
    } catch (e: Exception) {
        Log.d(TAG, "Firebase Crashlytics indisponível: ${e.message}")
        null
    }

    /** Chamado uma vez na inicialização do app para restaurar o consentimento já persistido. */
    fun init(context: Context, consentGranted: Boolean) {
        applyConsent(context, consentGranted)
    }

    /**
     * Liga ou desliga a coleta de telemetria. Deve ser chamado sempre que o usuário mudar sua
     * escolha na [com.bismarck.voleimanager.app.ui.components.TelemetryConsentDialog] ou no menu.
     * A persistência da preferência em si (SharedPreferences) é responsabilidade da ViewModel,
     * no mesmo padrão de `show_elo`/`is_supporter`.
     */
    fun applyConsent(context: Context, granted: Boolean) {
        consentGranted = granted
        analyticsOrNull(context)?.setAnalyticsCollectionEnabled(granted)
        crashlyticsOrNull()?.setCrashlyticsCollectionEnabled(granted)
        if (!granted) {
            analyticsOrNull(context)?.resetAnalyticsData()
        }
    }

    private fun logEvent(context: Context, name: String, params: Map<String, Any> = emptyMap()) {
        if (!consentGranted) return
        val analytics = analyticsOrNull(context) ?: return
        val bundle = android.os.Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Boolean -> bundle.putString(key, value.toString())
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics.logEvent(name, bundle)
    }

    fun logGroupCreated(context: Context, groupType: String, balancingMode: String) {
        logEvent(context, "group_created", mapOf("group_type" to groupType, "balancing_mode" to balancingMode))
    }

    fun logMatchFinished(context: Context, groupType: String, teamSize: Int, streakBroken: Boolean) {
        logEvent(
            context,
            "match_finished",
            mapOf("group_type" to groupType, "team_size" to teamSize, "streak_broken" to streakBroken)
        )
    }

    fun logTeamsRebalanced(context: Context, groupType: String, balancingMode: String) {
        logEvent(context, "teams_rebalanced", mapOf("group_type" to groupType, "balancing_mode" to balancingMode))
    }

    fun logStreakBreakRebalance(context: Context, groupType: String) {
        logEvent(context, "streak_break_rebalance", mapOf("group_type" to groupType))
    }

    fun logBackupExported(context: Context) {
        logEvent(context, "backup_exported")
    }

    fun logBackupImported(context: Context) {
        logEvent(context, "backup_imported")
    }

    fun logCsvExported(context: Context, csvType: String) {
        logEvent(context, "csv_exported", mapOf("csv_type" to csvType))
    }

    fun logCsvImported(context: Context, csvType: String) {
        logEvent(context, "csv_imported", mapOf("csv_type" to csvType))
    }

    /** Encaminha exceções não fatais relevantes (ex.: falha de import/export) ao Crashlytics. */
    fun recordException(throwable: Throwable) {
        if (!consentGranted) return
        crashlyticsOrNull()?.recordException(throwable)
    }
}

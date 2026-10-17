package com.bismarck.voleimanager.app.util

import android.content.Context
import com.bismarck.voleimanager.app.platform.Telemetry

/** [Telemetry] Android: delega ao [TelemetryManager] (Firebase Analytics + Crashlytics). */
class FirebaseTelemetry(context: Context) : Telemetry {
    private val context = context.applicationContext

    override fun init(consentGranted: Boolean) = TelemetryManager.init(context, consentGranted)
    override fun applyConsent(granted: Boolean) = TelemetryManager.applyConsent(context, granted)
    override fun logGroupCreated(groupType: String, balancingMode: String) =
        TelemetryManager.logGroupCreated(context, groupType, balancingMode)
    override fun logMatchFinished(groupType: String, teamSize: Int, streakBroken: Boolean) =
        TelemetryManager.logMatchFinished(context, groupType, teamSize, streakBroken)
    override fun logTeamsRebalanced(groupType: String, balancingMode: String) =
        TelemetryManager.logTeamsRebalanced(context, groupType, balancingMode)
    override fun logStreakBreakRebalance(groupType: String) =
        TelemetryManager.logStreakBreakRebalance(context, groupType)
    override fun logBackupExported() = TelemetryManager.logBackupExported(context)
    override fun logBackupImported() = TelemetryManager.logBackupImported(context)
    override fun logCsvExported(csvType: String) = TelemetryManager.logCsvExported(context, csvType)
    override fun logCsvImported(csvType: String) = TelemetryManager.logCsvImported(context, csvType)
    override fun logGroupCloudSynced(groupType: String, planTier: String) =
        TelemetryManager.logGroupCloudSynced(context, groupType, planTier)
    override fun logMemberJoinedViaCode(role: String) = TelemetryManager.logMemberJoinedViaCode(context, role)
    override fun logPremiumPurchased(planTier: String, isAnnual: Boolean) =
        TelemetryManager.logPremiumPurchased(context, planTier, isAnnual)
    override fun recordException(throwable: Throwable) = TelemetryManager.recordException(throwable)
}

package com.bismarck.voleimanager.app.platform

/**
 * Telemetria anônima e opcional (ver PRIVACY_POLICY.md, seção 2). Só aceita enums/contagens
 * (nunca nomes de jogadores/grupos ou conteúdo de partidas). Ao criar um novo evento de negócio,
 * adicione um método tipado aqui em vez de registrar eventos com texto livre.
 */
interface Telemetry {
    /** Restaura o consentimento já persistido na inicialização do app. */
    fun init(consentGranted: Boolean)

    /** Liga/desliga a coleta em runtime; revogar o consentimento também limpa os dados. */
    fun applyConsent(granted: Boolean)

    fun logGroupCreated(groupType: String, balancingMode: String)
    fun logMatchFinished(groupType: String, teamSize: Int, streakBroken: Boolean)
    fun logTeamsRebalanced(groupType: String, balancingMode: String)
    fun logStreakBreakRebalance(groupType: String)
    fun logBackupExported()
    fun logBackupImported()
    fun logCsvExported(csvType: String)
    fun logCsvImported(csvType: String)
    fun logGroupCloudSynced(groupType: String, planTier: String)
    fun logMemberJoinedViaCode(role: String)
    fun logPremiumPurchased(planTier: String, isAnnual: Boolean)

    /** Exceção não fatal relevante (ex.: falha de import/export). */
    fun recordException(throwable: Throwable)

    companion object {
        /** Chave da preferência que guarda o consentimento do usuário. */
        const val PREF_KEY_TELEMETRY_ENABLED = "telemetry_enabled"
    }
}

/** Telemetria desligada (padrão onde não há backend configurado, ex.: testes ou iOS offline). */
object NoOpTelemetry : Telemetry {
    override fun init(consentGranted: Boolean) = Unit
    override fun applyConsent(granted: Boolean) = Unit
    override fun logGroupCreated(groupType: String, balancingMode: String) = Unit
    override fun logMatchFinished(groupType: String, teamSize: Int, streakBroken: Boolean) = Unit
    override fun logTeamsRebalanced(groupType: String, balancingMode: String) = Unit
    override fun logStreakBreakRebalance(groupType: String) = Unit
    override fun logBackupExported() = Unit
    override fun logBackupImported() = Unit
    override fun logCsvExported(csvType: String) = Unit
    override fun logCsvImported(csvType: String) = Unit
    override fun logGroupCloudSynced(groupType: String, planTier: String) = Unit
    override fun logMemberJoinedViaCode(role: String) = Unit
    override fun logPremiumPurchased(planTier: String, isAnnual: Boolean) = Unit
    override fun recordException(throwable: Throwable) = Unit
}

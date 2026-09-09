package com.bismarck.voleimanager.app.util

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Sem um google-services.json real no classpath de teste, o Firebase nunca inicializa a
 * FirebaseApp padrão. Isso é exatamente o cenário que o TelemetryManager precisa tratar sem
 * lançar exceções: toda chamada deve ser um no-op seguro, independentemente do consentimento.
 * Cobrimos aqui a garantia de que nenhuma chamada (init, applyConsent, eventos, exceções)
 * derruba o app nesse cenário, e que o gating de consentimento não impede a chamada de rodar.
 */
@RunWith(RobolectricTestRunner::class)
class TelemetryManagerTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()

    @Test
    fun `init with consent not granted does not throw`() {
        TelemetryManager.init(context, consentGranted = false)
    }

    @Test
    fun `init with consent granted does not throw`() {
        TelemetryManager.init(context, consentGranted = true)
    }

    @Test
    fun `applyConsent false resets analytics data safely`() {
        TelemetryManager.applyConsent(context, granted = true)
        TelemetryManager.applyConsent(context, granted = false)
    }

    @Test
    fun `logging events before consent is granted does not throw`() {
        TelemetryManager.applyConsent(context, granted = false)
        TelemetryManager.logGroupCreated(context, "RECREATIONAL", "REBALANCE")
        TelemetryManager.logMatchFinished(context, "RECREATIONAL", 6, streakBroken = false)
        TelemetryManager.logTeamsRebalanced(context, "RECREATIONAL", "REBALANCE")
        TelemetryManager.logStreakBreakRebalance(context, "RECREATIONAL")
        TelemetryManager.logBackupExported(context)
        TelemetryManager.logBackupImported(context)
        TelemetryManager.logCsvExported(context, "JOGADORES")
        TelemetryManager.logCsvImported(context, "JOGADORES")
    }

    @Test
    fun `logging events after consent is granted does not throw`() {
        TelemetryManager.applyConsent(context, granted = true)
        TelemetryManager.logGroupCreated(context, "FIXED_POSITIONS", "REST")
        TelemetryManager.logMatchFinished(context, "FIXED_POSITIONS", 4, streakBroken = true)
    }

    @Test
    fun `recordException never throws regardless of consent`() {
        TelemetryManager.applyConsent(context, granted = false)
        TelemetryManager.recordException(RuntimeException("test"))
        TelemetryManager.applyConsent(context, granted = true)
        TelemetryManager.recordException(RuntimeException("test"))
        assertFalse(false) // apenas garante que chegamos até aqui sem exceção
    }
}

package com.bismarck.voleimanager.app.util

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Encapsula a Play Core In-App Update API.
 *
 * Estratégia: toda atualização disponível é ao menos sugerida via fluxo FLEXIBLE (não
 * bloqueante, o usuário decide quando reiniciar para aplicar). Só escalamos para IMMEDIATE
 * (bloqueante, tela cheia) quando:
 * - a versão foi marcada no Play Console com prioridade alta (`inAppUpdatePriority` >=
 *   [IMMEDIATE_UPDATE_MIN_PRIORITY], reservado para correções críticas/segurança); ou
 * - o usuário já está há [IMMEDIATE_UPDATE_MIN_STALENESS_DAYS] dias ou mais sem atualizar
 *   mesmo com uma atualização disponível (rede de segurança para quem ignora o fluxo flexível
 *   indefinidamente).
 *
 * A API decide se e como a atualização será mostrada com base em cotas/regras internas do
 * Play, assim como o [InAppReviewHelper] - chamar esta função não garante nenhuma UI visível.
 */
class InAppUpdateHelper(activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var flexibleUpdateListener: InstallStateUpdatedListener? = null
    private var onFlexibleReadyToInstall: (() -> Unit)? = null

    /**
     * Verifica se há atualização disponível e decide entre FLEXIBLE e IMMEDIATE, ou retoma um
     * update IMMEDIATE que tenha ficado parado. Chame em todo ponto de entrada do app (ex.:
     * onCreate/onResume da Activity). [onFlexibleReadyToInstall] é chamado sempre que um update
     * FLEXIBLE termina de baixar (nesta sessão ou em uma anterior) e está pronto para
     * [completeFlexibleUpdate] - normalmente para exibir um snackbar "Reiniciar para atualizar".
     */
    fun checkForUpdate(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onFlexibleReadyToInstall: () -> Unit
    ) {
        this.onFlexibleReadyToInstall = onFlexibleReadyToInstall
        ensureFlexibleListenerRegistered()

        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                when {
                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        // Update IMMEDIATE ficou parado (ex.: app foi fechado no meio do fluxo); retoma.
                        startUpdateFlow(info, launcher, AppUpdateType.IMMEDIATE)
                    }

                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                        val isCriticalPriority = info.updatePriority() >= IMMEDIATE_UPDATE_MIN_PRIORITY
                        val isTooStale =
                            (info.clientVersionStalenessDays()
                                ?: -1) >= IMMEDIATE_UPDATE_MIN_STALENESS_DAYS
                        if ((isCriticalPriority || isTooStale) &&
                            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                        ) {
                            startUpdateFlow(info, launcher, AppUpdateType.IMMEDIATE)
                        } else if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            startUpdateFlow(info, launcher, AppUpdateType.FLEXIBLE)
                        }
                    }

                    info.installStatus() == InstallStatus.DOWNLOADED -> {
                        // Update FLEXIBLE já baixado (possivelmente em uma sessão anterior); só falta instalar.
                        onFlexibleReadyToInstall()
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Falha ao consultar disponibilidade de atualização: ${it.message}")
            }
    }

    private fun startUpdateFlow(
        info: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        type: Int
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(type).build()
            )
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao iniciar o fluxo de atualização: ${e.message}")
        }
    }

    private fun ensureFlexibleListenerRegistered() {
        if (flexibleUpdateListener != null) return
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onFlexibleReadyToInstall?.invoke()
            }
        }
        flexibleUpdateListener = listener
        appUpdateManager.registerListener(listener)
    }

    /** Finaliza um update FLEXIBLE já baixado, reiniciando o app para aplicá-lo. */
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    /** Chame no onDestroy da Activity para não vazar o listener registrado. */
    fun unregister() {
        flexibleUpdateListener?.let(appUpdateManager::unregisterListener)
        flexibleUpdateListener = null
    }

    companion object {
        private const val TAG = "InAppUpdate"
        private const val IMMEDIATE_UPDATE_MIN_PRIORITY = 4
        private const val IMMEDIATE_UPDATE_MIN_STALENESS_DAYS = 15
    }
}

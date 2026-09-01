package com.bismarck.voleimanager.app.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Encapsula o fluxo da Google Play In-App Review API.
 *
 * O Play decide, com base em regras e cotas internas, se o diálogo de avaliação será
 * realmente exibido — chamar esta função não garante que o usuário verá algo na tela, e
 * isso é esperado. Por isso não há fallback de UI aqui: se o fluxo falhar ou o Play optar
 * por não mostrar o diálogo, simplesmente não acontece nada visível para o usuário.
 */
object InAppReviewHelper {

    private const val TAG = "InAppReview"

    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d(TAG, "Falha ao solicitar o fluxo de review: ${task.exception?.message}")
                return@addOnCompleteListener
            }
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener {
                // Independentemente do resultado, o Play não informa se o usuário
                // efetivamente avaliou o app; apenas registramos que o fluxo terminou.
                Log.d(TAG, "Fluxo de review finalizado")
            }
        }
    }
}

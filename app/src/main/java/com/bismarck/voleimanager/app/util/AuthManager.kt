package com.bismarck.voleimanager.app.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Usuário autenticado, já convertido para um tipo simples (sem depender do SDK do Firebase fora
 *  desta camada) — nome, e-mail e foto de perfil exibidos no cabeçalho do menu lateral. */
data class AppAuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)

private fun FirebaseUser.toAppAuthUser() = AppAuthUser(
    uid = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl?.toString()
)

/**
 * Fachada única sobre o Firebase Authentication (e-mail/senha por enquanto — login com Google
 * fica para uma fase seguinte). Segue o mesmo cuidado do [TelemetryManager]: se o app não tiver
 * um `google-services.json` válido, todas as chamadas abaixo falham de forma segura (never
 * derruba o app) e [currentUser] simplesmente nunca emite um usuário logado.
 */
object AuthManager {
    private const val TAG = "AuthManager"

    private fun authOrNull(): FirebaseAuth? = try {
        Firebase.auth
    } catch (e: Exception) {
        Log.d(TAG, "Firebase Auth indisponível: ${e.message}")
        null
    }

    /** Emite o usuário logado (ou `null`) sempre que o estado de autenticação mudar. */
    val currentUser: Flow<AppAuthUser?> = callbackFlow {
        val auth = authOrNull()
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAppAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /** Cria uma conta gratuita com e-mail/senha e define [displayName] como apelido exibido no
     *  app. Retorna uma mensagem de erro amigável em caso de falha, ou `null` em caso de sucesso. */
    suspend fun signUp(email: String, password: String, displayName: String): String? {
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            val result = suspendCancellableCoroutine<Result<FirebaseUser?>> { cont ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(task.result?.user))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao criar conta")))
                        }
                    }
            }
            result.getOrThrow()?.let { user ->
                if (displayName.isNotBlank()) {
                    awaitProfileUpdate(user, displayName)
                }
            }
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível criar a conta."
        }
    }

    /** Autentica com e-mail/senha. Retorna uma mensagem de erro amigável em caso de falha, ou
     *  `null` em caso de sucesso. */
    suspend fun signIn(email: String, password: String): String? {
        val auth = authOrNull() ?: return "Serviço de conta indisponível no momento."
        return try {
            val result = suspendCancellableCoroutine<Result<Unit>> { cont ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            cont.resume(Result.success(Unit))
                        } else {
                            cont.resume(Result.failure(task.exception ?: Exception("Falha ao entrar")))
                        }
                    }
            }
            result.getOrThrow()
            null
        } catch (e: Exception) {
            e.message ?: "Não foi possível entrar. Verifique seu e-mail e senha."
        }
    }

    fun signOut() {
        authOrNull()?.signOut()
    }

    private suspend fun awaitProfileUpdate(user: FirebaseUser, displayName: String) {
        suspendCancellableCoroutine<Unit> { cont ->
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(request).addOnCompleteListener {
                cont.resume(Unit)
            }
        }
    }
}

package com.bismarck.voleimanager.app.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Fachada sobre o Jetpack Credential Manager + One Tap do Google, usada para obter um ID token do
 * Google associado à conta que o usuário escolher no seletor de contas do sistema. Esse ID token é
 * então trocado por uma credencial do Firebase Auth em [AuthManager.signInWithGoogleIdToken] —
 * este arquivo não conhece Firebase, só o fluxo de obtenção do token.
 *
 * Requer:
 *  - "Google" habilitado como provedor em Firebase Console > Authentication > Sign-in method
 *    (isso gera automaticamente um "Web client ID" no Google Cloud, usado como [webClientId]);
 *  - o SHA-1 (e, para builds de release, o SHA-256) do certificado de assinatura do app cadastrado
 *    em Firebase Console > Configurações do projeto > seu app Android > "Add fingerprint" — sem
 *    isso o seletor de contas do Google recusa a solicitação (erro de credencial inválida).
 *
 * [context] precisa ser um contexto de Activity (não o `applicationContext`), pois o Credential
 * Manager exibe uma UI do sistema (bottom sheet/diálogo) sobre a tela atual.
 */
object GoogleSignInHelper {
    private const val TAG = "GoogleSignInHelper"

    /**
     * Solicita um ID token do Google. Primeiro tenta apenas contas já usadas anteriormente com
     * este app ([FILTER_BY_AUTHORIZED_ACCOUNTS] = true, fluxo mais rápido e discreto, ideal para
     * "Entrar"); se isso falhar (ex.: nenhuma conta autorizada ainda, caso mais comum no
     * cadastro), tenta de novo sem filtro, mostrando todas as contas Google do aparelho.
     */
    suspend fun getIdToken(context: Context, webClientId: String): Result<String> {
        val filtered = requestIdToken(context, webClientId, filterByAuthorizedAccounts = true)
        if (filtered.isSuccess) return filtered
        return requestIdToken(context, webClientId, filterByAuthorizedAccounts = false)
    }

    private suspend fun requestIdToken(
        context: Context,
        webClientId: String,
        filterByAuthorizedAccounts: Boolean
    ): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val response = CredentialManager.create(context).getCredential(request = request, context = context)
            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Result.failure(IllegalStateException("Credencial inesperada retornada pelo Google."))
            }
        } catch (e: GetCredentialException) {
            Log.d(TAG, "Falha ao obter credencial do Google (filterByAuthorizedAccounts=$filterByAuthorizedAccounts): ${e.message}")
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.d(TAG, "Falha ao interpretar o ID token do Google: ${e.message}")
            Result.failure(e)
        }
    }
}

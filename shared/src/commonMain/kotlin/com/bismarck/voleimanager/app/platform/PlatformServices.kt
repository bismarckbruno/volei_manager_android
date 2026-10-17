package com.bismarck.voleimanager.app.platform

/**
 * Anfitrião de UI do sistema (no Android, a Activity atual; no iOS, o UIViewController raiz).
 * Necessário para fluxos que exibem telas nativas por cima do app, como o seletor de contas Google
 * e a folha de compra da loja.
 */
interface PlatformHost

/** Identificador estável deste aparelho (só para `admin-session-transfer`; nunca é PII). */
interface DeviceInfo {
    val deviceId: String
}

/** Obtém um ID token do Google para trocar por uma credencial do Firebase Auth. */
interface GoogleSignInService {
    suspend fun getIdToken(host: PlatformHost): Result<String>
}

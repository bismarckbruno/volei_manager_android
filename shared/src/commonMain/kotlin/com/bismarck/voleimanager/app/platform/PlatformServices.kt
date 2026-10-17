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

    /** `true` em builds de debug (libera overrides de premium e logs extras). */
    val isDebugBuild: Boolean
}

/** Obtém um ID token do Google para trocar por uma credencial do Firebase Auth. */
interface GoogleSignInService {
    suspend fun getIdToken(host: PlatformHost): Result<String>
}

/** Inicia o fluxo nativo de compra de uma assinatura; devolve `false` se n?o puder abrir. */
fun interface PurchaseLauncher {
    fun launch(host: PlatformHost, offer: com.bismarck.voleimanager.app.util.SubscriptionOffer): Boolean
}

/** Leitura/escrita de planilhas .xlsx (zip + XML), implementada por plataforma. */
interface SpreadsheetCodec {
    fun buildXlsx(headers: List<String>, rows: List<List<String>>): ByteArray
    fun readXlsxRows(bytes: ByteArray): List<List<String>>
}

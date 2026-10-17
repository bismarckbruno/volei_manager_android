package com.bismarck.voleimanager.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import com.bismarck.voleimanager.app.platform.PlatformHost

/**
 * Ações de UI que dependem do sistema (abrir URL, compartilhar, toast, loja, orientação...).
 * A UI comum as obtém via [LocalPlatformActions]; cada plataforma fornece sua implementação
 * (Android: `AndroidPlatformActions` no :app; iOS: no app shell).
 */
interface PlatformActions {
    /** Anfitrião de UI nativa (Activity / UIViewController) para login Google e compra. */
    val host: PlatformHost

    /** `true` quando nuvem (login, grupos live) está disponível nesta plataforma. */
    val supportsCloud: Boolean

    /** `true` quando assinaturas Premium podem ser compradas nesta plataforma. */
    val supportsBilling: Boolean

    /** `true` em builds de debug (libera atalhos e seções de teste na UI). */
    val isDebugBuild: Boolean

    /** Link público do app na loja (Play Store/App Store), usado em textos de convite. */
    val storeUrl: String

    fun openUrl(url: String)
    fun shareText(text: String)
    fun showToast(message: String, long: Boolean = false)

    /** Dispara o fluxo nativo de avaliação in-app, se houver. */
    fun requestReview()

    /** Abre a página do app na loja para avaliação manual. */
    fun openStoreListing()

    /** Abre a tela de gerenciamento de assinaturas da loja. */
    fun openSubscriptionManagement(productId: String? = null)

    /** Trava (ou libera) a tela em paisagem. */
    fun setLandscapeLocked(locked: Boolean)

    /** Lê a imagem de [uri] (vinda do image picker) reduzida para edição/corte, já na orientação correta. */
    suspend fun loadAvatarForEditing(uri: String): ImageBitmap?

    /**
     * Renderiza [content] fora da tela com [widthDp] de largura, converte em PNG e entrega a
     * [onPng]. [onPng] não é chamado se a captura falhar.
     */
    fun captureComposableAsPng(
        widthDp: Int,
        content: @Composable () -> Unit,
        onPng: (ByteArray) -> Unit
    )
}

val LocalPlatformActions = staticCompositionLocalOf<PlatformActions> {
    error("PlatformActions não fornecido: envolva a UI em CompositionLocalProvider(LocalPlatformActions provides ...)")
}

/** Trata o gesto/botão "voltar" do sistema enquanto [enabled]. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/** Retorna uma função que abre o seletor de documentos; o resultado (uri) chega em [onPicked]. */
@Composable
expect fun rememberDocumentPicker(onPicked: (String) -> Unit): (mimeTypes: List<String>) -> Unit

/** Retorna uma função que abre o seletor de imagens da galeria; o resultado chega em [onPicked]. */
@Composable
expect fun rememberImagePicker(onPicked: (String) -> Unit): () -> Unit

/** Dentro de um diálogo: força o teclado a abrir ao mostrar (comportamento do Android). */
@Composable
expect fun DialogKeyboardAlwaysVisibleEffect()

/** Dentro de um bottom sheet modal: remove o scrim de contraste das barras do sistema. */
@Composable
expect fun DialogSystemBarsContrastEffect()

/** Idioma atual (código ISO 639-1, ex.: "pt"). */
expect fun currentLanguage(): String

/** Formata [value] no formato local com no máximo 2 casas decimais (mínimo 0). */
expect fun formatDecimal(value: Double): String

/** Formata um inteiro com separador de milhar local. */
expect fun formatInteger(value: Long): String

/** Formata [amount] na moeda [currencyCode] (ISO 4217); `null` se o código for inválido. */
expect fun formatCurrency(amount: Double, currencyCode: String): String?

/** Material You (Android 12+): devolve o esquema dinâmico, ou `null` se indisponível. */
@Composable
expect fun dynamicColorSchemeOrNull(darkTheme: Boolean): androidx.compose.material3.ColorScheme?

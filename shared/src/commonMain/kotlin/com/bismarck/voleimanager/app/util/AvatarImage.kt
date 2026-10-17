package com.bismarck.voleimanager.app.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

/** Lado (em pixels) da miniatura final salva como foto de perfil. */
const val AVATAR_OUTPUT_PX = 320

/** Fator de escala "cobrir" (igual ao `ContentScale.Crop`) que faz [bitmap] preencher totalmente
 *  um quadro de [viewportPx] pixels de lado, mantendo a proporção original. */
fun avatarCoverScale(bitmap: ImageBitmap, viewportPx: Float): Float =
    maxOf(viewportPx / bitmap.width, viewportPx / bitmap.height)

/** Deslocamento máximo (em px, no espaço do viewport de corte) que ainda mantém [bitmap]
 *  cobrindo totalmente o quadro de [viewportPx] de lado, dado o [zoom] atual. */
fun maxAvatarPan(bitmap: ImageBitmap, zoom: Float, viewportPx: Float): Offset {
    val scale = avatarCoverScale(bitmap, viewportPx) * zoom
    val scaledW = bitmap.width * scale
    val scaledH = bitmap.height * scale
    val maxX = maxOf(0f, (scaledW - viewportPx) / 2f)
    val maxY = maxOf(0f, (scaledH - viewportPx) / 2f)
    return Offset(maxX, maxY)
}

/** Menor zoom (relativo ao recorte "cobrir" de zoom=1) que ainda deixa [bitmap] inteiro visível
 *  dentro do quadro de corte, sem cortar nenhuma borda. */
fun minAvatarZoom(bitmap: ImageBitmap): Float {
    val longestSide = maxOf(bitmap.width, bitmap.height).toFloat()
    val shortestSide = minOf(bitmap.width, bitmap.height).toFloat()
    if (longestSide <= 0f) return 1f
    return (shortestSide / longestSide).coerceAtMost(1f)
}

/** Retorna uma cópia de [source] rotacionada em [degrees] graus (múltiplos de 90), ou o próprio
 *  [source] se [degrees] for 0 (módulo 360). */
expect fun rotateAvatarBitmap(source: ImageBitmap, degrees: Int): ImageBitmap

/** Recorta [bitmap] para um quadrado de [outputPx] pixels de lado, reproduzindo o enquadramento
 *  visto no diálogo de corte (ver [avatarCoverScale]), multiplicado pelo [zoom] e deslocado por
 *  [pan] (medido num viewport de [viewportPx] pixels). Margens descobertas saem brancas. */
expect fun cropAvatarBitmap(
    bitmap: ImageBitmap,
    zoom: Float,
    pan: Offset,
    viewportPx: Float,
    outputPx: Int = AVATAR_OUTPUT_PX
): ImageBitmap

/** Comprime [bitmap] em JPEG e retorna o resultado em Base64 (sem quebras de linha). */
expect fun compressAvatarBitmap(bitmap: ImageBitmap): String

/** Decodifica uma foto salva por [compressAvatarBitmap]; `null` se inválida. */
expect fun decodeAvatarBase64(base64: String): ImageBitmap?

/** Gera um QR code preto/branco para [text]; `null` se o texto for vazio ou a geração falhar. */
expect fun generateQrCode(text: String, sizePx: Int = 512): ImageBitmap?

package com.bismarck.voleimanager.app.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val AVATAR_JPEG_QUALITY = 85

actual fun rotateAvatarBitmap(source: ImageBitmap, degrees: Int): ImageBitmap {
    val normalized = ((degrees % 360) + 360) % 360
    if (normalized == 0) return source
    val swap = normalized == 90 || normalized == 270
    val output = ImageBitmap(if (swap) source.height else source.width, if (swap) source.width else source.height)
    val canvas = Canvas(output)
    canvas.translate(output.width / 2f, output.height / 2f)
    canvas.rotate(normalized.toFloat())
    canvas.translate(-source.width / 2f, -source.height / 2f)
    canvas.drawImage(source, Offset.Zero, Paint())
    return output
}

actual fun cropAvatarBitmap(
    bitmap: ImageBitmap,
    zoom: Float,
    pan: Offset,
    viewportPx: Float,
    outputPx: Int
): ImageBitmap {
    val baseScale = maxOf(outputPx / bitmap.width.toFloat(), outputPx / bitmap.height.toFloat())
    val totalScale = baseScale * zoom
    val scaleRatio = outputPx / viewportPx
    val dx = outputPx / 2f - bitmap.width * totalScale / 2f + pan.x * scaleRatio
    val dy = outputPx / 2f - bitmap.height * totalScale / 2f + pan.y * scaleRatio
    val output = ImageBitmap(outputPx, outputPx)
    val canvas = Canvas(output)
    // Fundo branco: margens descobertas viram preto ao comprimir em JPEG (sem canal alfa).
    canvas.drawRect(0f, 0f, outputPx.toFloat(), outputPx.toFloat(), Paint().apply { color = Color.White })
    canvas.translate(dx, dy)
    canvas.scale(totalScale, totalScale)
    canvas.drawImage(bitmap, Offset.Zero, Paint().apply { filterQuality = FilterQuality.High })
    return output
}

@OptIn(ExperimentalEncodingApi::class)
actual fun compressAvatarBitmap(bitmap: ImageBitmap): String {
    val data = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.JPEG, AVATAR_JPEG_QUALITY)
        ?: return ""
    return Base64.encode(data.bytes)
}

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeAvatarBase64(base64: String): ImageBitmap? = try {
    Image.makeFromEncoded(Base64.decode(base64)).toComposeImageBitmap()
} catch (e: Exception) {
    null
}

// QR code no iOS depende de uma biblioteca KMP (ex.: ZXing port); só é usado na tela de nuvem,
// desligada na 1ª versão iOS.
actual fun generateQrCode(text: String, sizePx: Int): ImageBitmap? = null

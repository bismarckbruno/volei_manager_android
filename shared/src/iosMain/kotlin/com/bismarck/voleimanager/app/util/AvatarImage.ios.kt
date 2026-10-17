package com.bismarck.voleimanager.app.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

// Operações de bitmap do avatar e geração de QR no iOS ainda não foram implementadas (ios-actuals):
// as funções abaixo falham de forma controlada (null/erro) em vez de quebrar a compilação.

actual fun rotateAvatarBitmap(source: ImageBitmap, degrees: Int): ImageBitmap = source

actual fun cropAvatarBitmap(
    bitmap: ImageBitmap,
    zoom: Float,
    pan: Offset,
    viewportPx: Float,
    outputPx: Int
): ImageBitmap = bitmap

actual fun compressAvatarBitmap(bitmap: ImageBitmap): String = ""

actual fun decodeAvatarBase64(base64: String): ImageBitmap? = null

actual fun generateQrCode(text: String, sizePx: Int): ImageBitmap? = null

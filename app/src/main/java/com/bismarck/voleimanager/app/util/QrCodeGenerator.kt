package com.bismarck.voleimanager.app.util

import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

private const val TAG = "QrCodeGenerator"

/**
 * Gera um [Bitmap] de QR code preto/branco para [text] usando ZXing (`com.google.zxing:core`,
 * ver `spectator-code-client`). Usado pela tela Premium para exibir o código de convite de
 * espectador como QR, além do texto simples — mesma informação, forma alternativa de
 * compartilhar (ler com a câmera em vez de digitar).
 */
object QrCodeGenerator {
    fun generate(text: String, sizePx: Int = 512): Bitmap? {
        if (text.isBlank()) return null
        return try {
            val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
            val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.d(TAG, "Falha ao gerar QR code: ${e.message}")
            null
        }
    }
}

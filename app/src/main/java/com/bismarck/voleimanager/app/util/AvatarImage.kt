package com.bismarck.voleimanager.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.geometry.Offset
import java.io.ByteArrayOutputStream

private const val TAG = "AvatarImage"

/** Lado máximo (em pixels) da imagem carregada para edição (corte/rotação) — grande o bastante
 *  para o usuário ajustar o enquadramento sem perda perceptível, sem estourar a memória. */
private const val EDIT_MAX_DIMENSION_PX = 1024

/** Lado (em pixels) da miniatura final salva como foto de perfil — suficiente para ficar nítida
 *  num avatar de tela (círculo de até ~96dp em telas densas), sem gastar espaço à toa no
 *  documento Firestore do usuário. */
private const val AVATAR_OUTPUT_PX = 320
private const val AVATAR_JPEG_QUALITY = 85

/** Lê a imagem apontada por [uri] já reduzida para no máximo [EDIT_MAX_DIMENSION_PX] pixels de
 *  lado (mantendo a proporção original), pronta para ser exibida no diálogo de corte/rotação
 *  ([com.bismarck.voleimanager.app.ui.components.AvatarCropDialog]). Retorna `null` se a imagem
 *  não puder ser lida/decodificada. */
fun loadBitmapForAvatarEditing(context: Context, uri: Uri): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, boundsOptions) }
        var sampleSize = 1
        val longestSide = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
        while (longestSide / sampleSize > EDIT_MAX_DIMENSION_PX * 2) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
    } catch (e: Exception) {
        Log.w(TAG, "Falha ao carregar foto para edição: ${e.message}")
        null
    }
}

/** Retorna uma cópia de [source] rotacionada em [degrees] graus (múltiplos de 90), ou o próprio
 *  [source] se [degrees] for 0 (módulo 360). */
fun rotateAvatarBitmap(source: Bitmap, degrees: Int): Bitmap {
    val normalized = ((degrees % 360) + 360) % 360
    if (normalized == 0) return source
    val matrix = Matrix().apply { postRotate(normalized.toFloat()) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

/** Deslocamento máximo (em px, no espaço do viewport de corte) que ainda mantém [bitmap]
 *  cobrindo totalmente o quadro de [viewportPx] de lado, dado o [zoom] atual — usado para travar
 *  o arraste do usuário sem revelar bordas vazias. */
fun maxAvatarPan(bitmap: Bitmap, zoom: Float, viewportPx: Float): Offset {
    val baseScale = maxOf(viewportPx / bitmap.width, viewportPx / bitmap.height)
    val scale = baseScale * zoom
    val scaledW = bitmap.width * scale
    val scaledH = bitmap.height * scale
    val maxX = maxOf(0f, (scaledW - viewportPx) / 2f)
    val maxY = maxOf(0f, (scaledH - viewportPx) / 2f)
    return Offset(maxX, maxY)
}

/**
 * Recorta [bitmap] para um quadrado de [outputPx] pixels de lado, reproduzindo exatamente o
 * enquadramento visto no diálogo de corte: mesmo ajuste "cobrir" (`ContentScale.Crop`) usado na
 * prévia, multiplicado pelo [zoom] do usuário e deslocado por [pan] — ambos medidos no espaço do
 * viewport de prévia ([viewportPx] pixels de lado — convertido proporcionalmente para o espaço de
 * saída, que pode ter um tamanho diferente).
 */
fun cropAvatarBitmap(bitmap: Bitmap, zoom: Float, pan: Offset, viewportPx: Float, outputPx: Int = AVATAR_OUTPUT_PX): Bitmap {
    val baseScale = maxOf(outputPx / bitmap.width.toFloat(), outputPx / bitmap.height.toFloat())
    val totalScale = baseScale * zoom
    val scaleRatio = outputPx / viewportPx
    val panOutX = pan.x * scaleRatio
    val panOutY = pan.y * scaleRatio
    val scaledW = bitmap.width * totalScale
    val scaledH = bitmap.height * totalScale
    val dx = outputPx / 2f - scaledW / 2f + panOutX
    val dy = outputPx / 2f - scaledH / 2f + panOutY
    val matrix = Matrix().apply {
        postScale(totalScale, totalScale)
        postTranslate(dx, dy)
    }
    val output = Bitmap.createBitmap(outputPx, outputPx, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    Canvas(output).drawBitmap(bitmap, matrix, paint)
    return output
}

/** Comprime [bitmap] (já no tamanho final, ver [cropAvatarBitmap]) em JPEG e retorna o resultado
 *  codificado em Base64 (sem quebras de linha) — pronto para ser salvo no documento de perfil do
 *  usuário no Firestore. */
fun compressAvatarBitmap(bitmap: Bitmap): String {
    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, output)
    return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
}

/** Decodifica uma foto de perfil previamente salva por [compressAvatarBitmap] de volta para um
 *  [Bitmap], para exibição em `Image`/`Icon` compostos manualmente com `BitmapFactory`. */
fun decodeAvatarBase64(base64: String): Bitmap? = try {
    val bytes = Base64.decode(base64, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: Exception) {
    Log.w(TAG, "Falha ao decodificar foto de perfil: ${e.message}")
    null
}

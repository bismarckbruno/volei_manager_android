package com.bismarck.voleimanager.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bismarck.voleimanager.app.platform.FileRef
import com.bismarck.voleimanager.app.platform.FileService
import java.io.File

/** [FileService] Android: `ContentResolver` para leitura e `FileProvider` + `ACTION_SEND` para compartilhar. */
class AndroidFileService(context: Context) : FileService {
    private val context = context.applicationContext

    override fun readBytes(file: FileRef): ByteArray? =
        context.contentResolver.openInputStream(file.uri.toUri())?.use { it.readBytes() }

    override fun shareFile(fileName: String, bytes: ByteArray, mimeType: String, chooserTitle: String) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    }
}

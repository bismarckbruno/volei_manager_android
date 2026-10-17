package com.bismarck.voleimanager.app.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIDevice
import platform.posix.memcpy
import platform.Foundation.NSUUID
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/** [PreferencesStore] sobre `NSUserDefaults` (equivalente iOS do `SharedPreferences("volei")`). */
class UserDefaultsPreferencesStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : PreferencesStore {
    override fun contains(key: String) = defaults.objectForKey(key) != null
    override fun getString(key: String, defaultValue: String?) = defaults.stringForKey(key) ?: defaultValue
    override fun getInt(key: String, defaultValue: Int) =
        if (contains(key)) defaults.integerForKey(key).toInt() else defaultValue
    override fun getLong(key: String, defaultValue: Long) =
        if (contains(key)) defaults.integerForKey(key) else defaultValue
    override fun getBoolean(key: String, defaultValue: Boolean) =
        if (contains(key)) defaults.boolForKey(key) else defaultValue

    override fun edit(): PreferencesStore.Editor = object : PreferencesStore.Editor {
        private val pending = mutableListOf<(NSUserDefaults) -> Unit>()

        private fun add(op: (NSUserDefaults) -> Unit) = apply { pending += op }
        override fun putString(key: String, value: String?) =
            add { if (value == null) it.removeObjectForKey(key) else it.setObject(value, forKey = key) }
        override fun putInt(key: String, value: Int) = add { it.setInteger(value.toLong(), forKey = key) }
        override fun putLong(key: String, value: Long) = add { it.setInteger(value, forKey = key) }
        override fun putBoolean(key: String, value: Boolean) = add { it.setBool(value, forKey = key) }
        override fun remove(key: String) = add { it.removeObjectForKey(key) }
        override fun clear() = add { d ->
            d.dictionaryRepresentation().keys.forEach { k -> (k as? String)?.let { d.removeObjectForKey(it) } }
        }

        override fun apply() {
            pending.forEach { it(defaults) }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) {
        result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    }
    return result
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }
}

/** Lê arquivos escolhidos no document picker e compartilha via `UIActivityViewController`. */
class IosFileService : FileService {
    override fun readBytes(file: FileRef): ByteArray? {
        val url = NSURL.URLWithString(file.uri) ?: NSURL.fileURLWithPath(file.uri)
        val scoped = url.startAccessingSecurityScopedResource()
        return try {
            NSData.dataWithContentsOfURL(url)?.toByteArray()
        } finally {
            if (scoped) url.stopAccessingSecurityScopedResource()
        }
    }

    override fun shareFile(fileName: String, bytes: ByteArray, mimeType: String, chooserTitle: String) {
        val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
        check(bytes.toNSData().writeToFile(path, atomically = true)) { "Não foi possível gravar $fileName" }
        val controller = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null
        )
        presentOnTop(controller)
    }
}

@OptIn(ExperimentalNativeApi::class)
class IosDeviceInfo : DeviceInfo {
    override val isDebugBuild: Boolean = Platform.isDebugBinary
    override val deviceId: String =
        UIDevice.currentDevice.identifierForVendor?.UUIDString ?: NSUUID().UUIDString
}

/** Planilhas .xlsx (zip + XML) ainda não têm implementação no iOS; CSV continua disponível. */
class IosSpreadsheetCodec : SpreadsheetCodec {
    override fun buildXlsx(headers: List<String>, rows: List<List<String>>): ByteArray =
        throw UnsupportedOperationException("Exportação .xlsx indisponível no iOS")

    override fun readXlsxRows(bytes: ByteArray): List<List<String>> =
        throw UnsupportedOperationException("Importação .xlsx indisponível no iOS")
}

/** Login Google não existe na 1ª versão iOS (offline). */
object UnsupportedGoogleSignInService : GoogleSignInService {
    override suspend fun getIdToken(host: PlatformHost): Result<String> =
        Result.failure(UnsupportedOperationException("Login Google indisponível no iOS"))
}

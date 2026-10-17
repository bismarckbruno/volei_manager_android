package com.bismarck.voleimanager.app.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.bismarck.voleimanager.app.platform.presentOnTop
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.writeToFile
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject

// No iOS o gesto "voltar" é tratado pela navegação própria do app; efeitos de janela de diálogo
// específicos do Android não se aplicam.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

private class DocumentPickerDelegate(var onPicked: (String) -> Unit) :
    NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.absoluteString?.let(onPicked)
    }
}

@Composable
actual fun rememberDocumentPicker(onPicked: (String) -> Unit): (mimeTypes: List<String>) -> Unit {
    val delegate = remember { DocumentPickerDelegate(onPicked) }
    delegate.onPicked = rememberUpdatedState(onPicked).value
    return {
        // O iOS filtra por UTI, não por MIME; o .vlz é um tipo próprio, então aceita qualquer item
        // e a importação valida o conteúdo.
        val types = listOfNotNull(UTType.typeWithIdentifier("public.item"))
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = types, asCopy = true)
        picker.delegate = delegate
        presentOnTop(picker)
    }
}

private class ImagePickerDelegate(var onPicked: (String) -> Unit) :
    NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage ?: return
        val data = normalizedJpeg(image) ?: return
        val path = NSTemporaryDirectory().trimEnd('/') + "/avatar_pick.jpg"
        if (data.writeToFile(path, atomically = true)) {
            NSURL.fileURLWithPath(path).absoluteString?.let(onPicked)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

/** Redesenha [image] (aplicando a orientação EXIF) com no máximo 1024px de lado e codifica em JPEG. */
@OptIn(ExperimentalForeignApi::class)
private fun normalizedJpeg(image: UIImage, maxSide: Double = 1024.0) = run {
    val (w, h) = image.size.useContents { width to height }
    val scale = minOf(1.0, maxSide / maxOf(w, h))
    val targetW = w * scale
    val targetH = h * scale
    val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(targetW, targetH), format = format)
    val redrawn = renderer.imageWithActions { image.drawInRect(CGRectMake(0.0, 0.0, targetW, targetH)) }
    UIImageJPEGRepresentation(redrawn, 0.9)
}

@Composable
actual fun rememberImagePicker(onPicked: (String) -> Unit): () -> Unit {
    val delegate = remember { ImagePickerDelegate(onPicked) }
    delegate.onPicked = rememberUpdatedState(onPicked).value
    return {
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.delegate = delegate
        presentOnTop(picker)
    }
}

@Composable
actual fun DialogKeyboardAlwaysVisibleEffect() {
}

@Composable
actual fun DialogSystemBarsContrastEffect() {
}

actual fun currentLanguage(): String = NSLocale.currentLocale.languageCode

actual fun formatDecimal(value: Double): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = 2u
        minimumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}

actual fun formatInteger(value: Long): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(double = value.toDouble())) ?: value.toString()
}

actual fun formatCurrency(amount: Double, currencyCode: String): String? {
    if (currencyCode.length != 3) return null
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        this.currencyCode = currencyCode
    }
    return formatter.stringFromNumber(NSNumber(double = amount))
}

@Composable
actual fun dynamicColorSchemeOrNull(darkTheme: Boolean): ColorScheme? = null

package com.bismarck.voleimanager.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.bismarck.voleimanager.app.platform.PlatformHost
import com.bismarck.voleimanager.app.platform.IosFileService
import com.bismarck.voleimanager.app.platform.presentOnTop
import com.bismarck.voleimanager.app.platform.topViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.CoreGraphics.CGSizeMake
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

private object IosHost : PlatformHost

/**
 * [PlatformActions] do iOS. Nuvem e assinaturas ficam desligadas na 1Âª versÃ£o (offline); captura
 * da imagem do histÃ³rico e travar paisagem ainda nÃ£o foram implementadas.
 */
class IosPlatformActions : PlatformActions {
    override val host: PlatformHost = IosHost
    override val supportsCloud: Boolean = false
    override val supportsBilling: Boolean = false
    override val isDebugBuild: Boolean = false
    override val storeUrl: String = "https://apps.apple.com/app/voleizin"

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    override fun shareText(text: String) {
        presentOnTop(UIActivityViewController(activityItems = listOf(text), applicationActivities = null))
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun showToast(message: String, long: Boolean) {
        val host = topViewController()?.view ?: return
        val maxWidth = host.bounds.useContents { size.width } * 0.85
        val (hostWidth, hostHeight) = host.bounds.useContents { size.width to size.height }
        val label = UILabel().apply {
            text = message
            textColor = UIColor.whiteColor
            font = UIFont.systemFontOfSize(14.0)
            textAlignment = NSTextAlignmentCenter
            numberOfLines = 0
        }
        val textSize = label.sizeThatFits(CGSizeMake(maxWidth - 24.0, 1000.0)).useContents { width to height }
        val boxWidth = textSize.first + 24.0
        val boxHeight = textSize.second + 16.0
        label.setFrame(CGRectMake(12.0, 8.0, textSize.first, textSize.second))
        val box = UIView(frame = CGRectMake((hostWidth - boxWidth) / 2, hostHeight - boxHeight - 96.0, boxWidth, boxHeight)).apply {
            backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.85)
            layer.cornerRadius = 12.0
            layer.masksToBounds = true
            addSubview(label)
        }
        host.addSubview(box)
        val delayNanos = if (long) 3_000_000_000L else 1_800_000_000L
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayNanos), dispatch_get_main_queue()) {
            UIView.animateWithDuration(
                duration = 0.25,
                animations = { box.alpha = 0.0 },
                completion = { _ -> box.removeFromSuperview() }
            )
        }
    }

    override fun requestReview() {
        SKStoreReviewController.requestReview()
    }

    override fun openStoreListing() = openUrl("$storeUrl?action=write-review")

    override fun openSubscriptionManagement(productId: String?) =
        openUrl("https://apps.apple.com/account/subscriptions")

    override fun setLandscapeLocked(locked: Boolean) {
        // Sem suporte ainda: exigiria o AppDelegate liberar a orientaÃ§Ã£o em tempo de execuÃ§Ã£o.
    }

    override suspend fun loadAvatarForEditing(uri: String): ImageBitmap? {
        val bytes = IosFileService().readBytes(com.bismarck.voleimanager.app.platform.FileRef(uri)) ?: return null
        return try {
            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    override fun captureComposableAsPng(
        widthDp: Int,
        content: @Composable () -> Unit,
        onPng: (ByteArray) -> Unit
    ) {
        // Pendente (ios-actuals): renderizar com ImageComposeScene e codificar em PNG.
    }
}

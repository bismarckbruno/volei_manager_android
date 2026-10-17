package com.bismarck.voleimanager.app.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bismarck.voleimanager.app.platform.PlatformHost
import com.bismarck.voleimanager.app.ui.platform.PlatformActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** [PlatformActions] do Android, vinculado à [activity] que hospeda a UI. */
class AndroidPlatformActions(private val activity: ComponentActivity) : PlatformActions {
    override val host: PlatformHost = AndroidPlatformHost(activity)
    override val supportsCloud: Boolean = true
    override val supportsBilling: Boolean = true
    override val isDebugBuild: Boolean = com.bismarck.voleimanager.app.BuildConfig.DEBUG
    override val storeUrl: String = "https://play.google.com/store/apps/details?id=${activity.packageName}"

    override fun openUrl(url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(sendIntent, null))
    }

    override fun showToast(message: String, long: Boolean) {
        Toast.makeText(activity, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    override fun requestReview() {
        InAppReviewHelper.requestReview(activity)
    }

    override fun openStoreListing() {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=${activity.packageName}".toUri()))
        } catch (_: ActivityNotFoundException) {
            openUrl(storeUrl)
        }
    }

    override fun openSubscriptionManagement(productId: String?) {
        val sku = productId?.let { "sku=$it&" }.orEmpty()
        openUrl("https://play.google.com/store/account/subscriptions?${sku}package=${activity.packageName}")
    }

    override fun setLandscapeLocked(locked: Boolean) {
        activity.requestedOrientation = if (locked) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override suspend fun loadAvatarForEditing(uri: String): ImageBitmap? =
        withContext(Dispatchers.IO) { loadBitmapForAvatarEditing(activity, uri.toUri()) }

    override fun captureComposableAsPng(
        widthDp: Int,
        content: @Composable () -> Unit,
        onPng: (ByteArray) -> Unit
    ) {
        val view = activity.window.decorView
        val composeView = ComposeView(activity).apply {
            setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
            setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
            setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())
            setContent(content)
        }
        val scrollView = ScrollView(activity).apply {
            addView(composeView)
            alpha = 0f
            isVerticalScrollBarEnabled = false
        }
        val root = view.rootView as? ViewGroup ?: return
        root.addView(scrollView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        scrollView.postDelayed({
            try {
                // Usa a densidade real do aparelho: a largura fixa em dp do conteúdo exportado
                // deve virar o mesmo tamanho proporcional em qualquer tela.
                val exportWidthPx = (widthDp * activity.resources.displayMetrics.density).toInt().coerceAtLeast(1)
                composeView.measure(
                    View.MeasureSpec.makeMeasureSpec(exportWidthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
                if (composeView.measuredWidth > 0 && composeView.measuredHeight > 0) {
                    val bitmap = Bitmap.createBitmap(composeView.measuredWidth, composeView.measuredHeight, Bitmap.Config.ARGB_8888)
                    composeView.draw(Canvas(bitmap))
                    val png = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
                    onPng(png)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                root.removeView(scrollView)
            }
        }, 500)
    }
}

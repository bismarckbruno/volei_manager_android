package com.bismarck.voleimanager.app.platform

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/** `UIViewController` visÃ­vel no topo da hierarquia, de onde apresentar sheets e alertas. */
internal fun topViewController(): UIViewController? {
    val scene = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
    val window = scene?.windows?.firstOrNull() as? UIWindow
    var top = window?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}

internal fun presentOnTop(controller: UIViewController) {
    topViewController()?.presentViewController(controller, animated = true, completion = null)
}


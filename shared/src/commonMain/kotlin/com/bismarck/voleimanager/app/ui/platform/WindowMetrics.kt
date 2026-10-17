package com.bismarck.voleimanager.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** `true` quando a janela está em paisagem (largura maior que a altura). */
@Composable
fun rememberIsLandscape(): Boolean {
    val size = LocalWindowInfo.current.containerSize
    return size.width > size.height
}

/** Largura atual da janela em dp. */
@Composable
fun rememberScreenWidthDp(): Dp {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return with(density) { size.width.toDp() }
}

/** Altura atual da janela em dp. */
@Composable
fun rememberScreenHeightDp(): Dp {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return with(density) { size.height.toDp() }
}

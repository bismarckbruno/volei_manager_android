package com.bismarck.voleimanager.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.bismarck.voleimanager.app.ui.viewmodel.TeamAccentColor

@Immutable
data class ExtendedColorScheme(
    val anotherPrime: ColorFamily,
)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val mediumContrastLightColorScheme = lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
    surfaceDim = surfaceDimLightMediumContrast,
    surfaceBright = surfaceBrightLightMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestLightMediumContrast,
    surfaceContainerLow = surfaceContainerLowLightMediumContrast,
    surfaceContainer = surfaceContainerLightMediumContrast,
    surfaceContainerHigh = surfaceContainerHighLightMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestLightMediumContrast,
)

private val highContrastLightColorScheme = lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
    surfaceDim = surfaceDimLightHighContrast,
    surfaceBright = surfaceBrightLightHighContrast,
    surfaceContainerLowest = surfaceContainerLowestLightHighContrast,
    surfaceContainerLow = surfaceContainerLowLightHighContrast,
    surfaceContainer = surfaceContainerLightHighContrast,
    surfaceContainerHigh = surfaceContainerHighLightHighContrast,
    surfaceContainerHighest = surfaceContainerHighestLightHighContrast,
)

private val mediumContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
    surfaceDim = surfaceDimDarkMediumContrast,
    surfaceBright = surfaceBrightDarkMediumContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkMediumContrast,
    surfaceContainerLow = surfaceContainerLowDarkMediumContrast,
    surfaceContainer = surfaceContainerDarkMediumContrast,
    surfaceContainerHigh = surfaceContainerHighDarkMediumContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkMediumContrast,
)

private val highContrastDarkColorScheme = darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
    surfaceDim = surfaceDimDarkHighContrast,
    surfaceBright = surfaceBrightDarkHighContrast,
    surfaceContainerLowest = surfaceContainerLowestDarkHighContrast,
    surfaceContainerLow = surfaceContainerLowDarkHighContrast,
    surfaceContainer = surfaceContainerDarkHighContrast,
    surfaceContainerHigh = surfaceContainerHighDarkHighContrast,
    surfaceContainerHighest = surfaceContainerHighestDarkHighContrast,
)

val extendedLight = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeLight,
        onAnotherPrimeLight,
        anotherPrimeContainerLight,
        onAnotherPrimeContainerLight,
    ),
)

val extendedDark = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeDark,
        onAnotherPrimeDark,
        anotherPrimeContainerDark,
        onAnotherPrimeContainerDark,
    ),
)

val extendedLightMediumContrast = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeLightMediumContrast,
        onAnotherPrimeLightMediumContrast,
        anotherPrimeContainerLightMediumContrast,
        onAnotherPrimeContainerLightMediumContrast,
    ),
)

val extendedLightHighContrast = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeLightHighContrast,
        onAnotherPrimeLightHighContrast,
        anotherPrimeContainerLightHighContrast,
        onAnotherPrimeContainerLightHighContrast,
    ),
)

val extendedDarkMediumContrast = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeDarkMediumContrast,
        onAnotherPrimeDarkMediumContrast,
        anotherPrimeContainerDarkMediumContrast,
        onAnotherPrimeContainerDarkMediumContrast,
    ),
)

val extendedDarkHighContrast = ExtendedColorScheme(
    anotherPrime = ColorFamily(
        anotherPrimeDarkHighContrast,
        onAnotherPrimeDarkHighContrast,
        anotherPrimeContainerDarkHighContrast,
        onAnotherPrimeContainerDarkHighContrast,
    ),
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

/**
 * Deriva a [ColorFamily] (cor/onCor/container/onContainer) de uma [TeamAccentColor] a partir da
 * sua cor semente, sem precisar de uma paleta tonal completa gerada pelo Material Theme Builder
 * (que já existe só para o azul padrão [primaryLight] e o amarelo padrão [anotherPrimeLight]).
 * Usada pelos times A/B quando o usuário premium escolhe vermelho, verde ou roxo.
 */
fun teamAccentColorFamily(accent: TeamAccentColor, darkTheme: Boolean): ColorFamily = when (accent) {
    TeamAccentColor.BLUE -> primaryContainerFamily(darkTheme)
    TeamAccentColor.YELLOW -> if (darkTheme) extendedDark.anotherPrime else extendedLight.anotherPrime
    TeamAccentColor.RED -> seedColorFamily(teamRedSeed, darkTheme)
    TeamAccentColor.GREEN -> seedColorFamily(teamGreenSeed, darkTheme)
    TeamAccentColor.PURPLE -> seedColorFamily(teamPurpleSeed, darkTheme)
}

/** Time A no padrão azul reaproveita o esquema de cores primário já usado hoje pelo Time A. */
private fun primaryContainerFamily(darkTheme: Boolean): ColorFamily = if (darkTheme) {
    ColorFamily(primaryDark, onPrimaryDark, primaryContainerDark, onPrimaryContainerDark)
} else {
    ColorFamily(primaryLight, onPrimaryLight, primaryContainerLight, onPrimaryContainerLight)
}

private fun lerp(from: Color, to: Color, fraction: Float): Color = Color(
    red = from.red + (to.red - from.red) * fraction,
    green = from.green + (to.green - from.green) * fraction,
    blue = from.blue + (to.blue - from.blue) * fraction,
    alpha = 1f,
)

/** Luminância relativa (peso perceptual padrão) de uma cor, em 0f..1f. */
private fun luminance(color: Color): Float =
    0.299f * color.red + 0.587f * color.green + 0.114f * color.blue

/**
 * Cores neutras de referência para o "peso visual" (luminância) do container dos times azul/amarelo
 * já em uso hoje: aproximadamente #444444 no tema escuro e #E2E2E2 no tema claro (ver
 * [primaryContainerDark]/[primaryContainerLight], cuja luminância já bate quase exatamente com esses
 * tons). Vermelho/verde/roxo usam essas mesmas luminâncias-alvo para o container, para que todas as
 * cores tenham o mesmo contraste/peso visual com os demais elementos da tela, independentemente do
 * matiz escolhido.
 */
private val teamAccentContainerLuminanceDark = luminance(Color(0xFF444444))
private val teamAccentContainerLuminanceLight = luminance(Color(0xFFE2E2E2))

/**
 * Luminância-alvo da cor de destaque ("color": nome do time, botões, badges) no tema escuro, igual
 * à do azul ([primaryDark]) — um azul claro/pastel usado sobre o container escuro. Vermelho, verde
 * e roxo usam essa mesma luminância-alvo para que o contraste entre o nome do time e o fundo do
 * card fique equivalente ao do card azul, em vez de ficarem visivelmente mais "apagados".
 */
private val teamAccentColorLuminanceDark = luminance(primaryDark)

/**
 * Luminância-alvo do texto/ícone (crown) sobre o botão de vitória no tema escuro, igual à do azul
 * ([onPrimaryDark]) — um tom escuro e saturado da própria cor, não preto puro. Assim vermelho, verde
 * e roxo ficam com o mesmo contraste "tom escuro da cor" que o azul já usa nesses itens.
 */
private val teamAccentOnColorLuminanceDark = luminance(onPrimaryDark)

/**
 * Luminância-alvo do texto principal sobre o card do time ("onColorContainer": nome dos jogadores,
 * placar, duração etc. nos cards de Histórico), igual à do azul em cada tema
 * ([onPrimaryContainerDark]/[onPrimaryContainerLight]). Vermelho, verde e roxo usam essas mesmas
 * luminâncias-alvo para que o texto sobre o card fique com o mesmo "peso visual"/contraste do
 * card azul, tanto no tema escuro quanto no claro — consistente com [teamAccentColorLuminanceDark]
 * e [teamAccentOnColorLuminanceDark] acima, usados nas telas de placar.
 */
private val teamAccentOnContainerLuminanceDark = luminance(onPrimaryContainerDark)
private val teamAccentOnContainerLuminanceLight = luminance(onPrimaryContainerLight)

/**
 * Mistura [seed] com [towards] na fração exata necessária para que a luminância resultante seja
 * [targetLuminance] (a luminância varia linearmente com a mistura, então a fração é resolvida
 * analiticamente). Preserva o matiz de [seed] enquanto ajusta seu "tom sem saturação" para bater com
 * o alvo — é assim que vermelho/verde/roxo ficam com o mesmo peso visual do azul.
 */
private fun blendToLuminance(seed: Color, towards: Color, targetLuminance: Float): Color {
    val seedLuminance = luminance(seed)
    val towardsLuminance = luminance(towards)
    if (towardsLuminance == seedLuminance) return seed
    val fraction = ((targetLuminance - seedLuminance) / (towardsLuminance - seedLuminance))
        .coerceIn(0f, 1f)
    return lerp(seed, towards, fraction)
}

/** Aproxima os 4 papéis de cor M3 (cor/onCor/container/onContainer) a partir de uma única seed. */
private fun seedColorFamily(seed: Color, darkTheme: Boolean): ColorFamily = if (darkTheme) {
    ColorFamily(
        color = blendToLuminance(seed, Color.White, teamAccentColorLuminanceDark),
        onColor = blendToLuminance(seed, Color.Black, teamAccentOnColorLuminanceDark),
        colorContainer = blendToLuminance(seed, Color.Black, teamAccentContainerLuminanceDark),
        onColorContainer = blendToLuminance(seed, Color.White, teamAccentOnContainerLuminanceDark),
    )
} else {
    ColorFamily(
        color = seed,
        onColor = Color.White,
        colorContainer = blendToLuminance(seed, Color.White, teamAccentContainerLuminanceLight),
        onColorContainer = blendToLuminance(seed, Color.Black, teamAccentOnContainerLuminanceLight),
    )
}

val LocalExtendedColors = staticCompositionLocalOf { extendedLight }

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val extendedColors = if (darkTheme) extendedDark else extendedLight

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

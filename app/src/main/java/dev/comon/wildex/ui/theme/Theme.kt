package dev.comon.wildex.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = WildexPalette.Primary,
    onPrimary = WildexPalette.OnPrimary,
    primaryContainer = WildexPalette.PrimaryContainer,
    onPrimaryContainer = WildexPalette.OnPrimaryContainer,
    secondary = WildexPalette.SecondarySurface,
    onSecondary = WildexPalette.OnSecondarySurface,
    secondaryContainer = WildexPalette.SurfaceContainerLow,
    onSecondaryContainer = WildexPalette.OnSurface,
    tertiary = WildexPalette.SecondaryMuted,
    onTertiary = WildexPalette.OnPrimary,
    tertiaryContainer = WildexPalette.SurfaceContainerHighest,
    onTertiaryContainer = WildexPalette.OnSurface,
    error = WildexPalette.Error,
    onError = WildexPalette.OnError,
    errorContainer = WildexPalette.PrimaryContainer,
    onErrorContainer = WildexPalette.OnSurface,
    background = WildexPalette.Surface,
    onBackground = WildexPalette.OnSurface,
    surface = WildexPalette.Surface,
    onSurface = WildexPalette.OnSurface,
    surfaceVariant = WildexPalette.SurfaceContainerLow,
    onSurfaceVariant = WildexPalette.SecondaryMuted,
    outline = WildexPalette.OnSurface,
    outlineVariant = WildexPalette.OutlineVariant,
    scrim = WildexPalette.OnSurface,
    inverseSurface = WildexPalette.OnSurface,
    inverseOnSurface = WildexPalette.Surface,
    inversePrimary = WildexPalette.PrimaryContainer,
    surfaceDim = WildexPalette.SurfaceContainerLow,
    surfaceBright = WildexPalette.SurfaceContainerLowest,
    surfaceContainerLowest = WildexPalette.SurfaceContainerLowest,
    surfaceContainerLow = WildexPalette.SurfaceContainerLow,
    surfaceContainer = WildexPalette.SurfaceContainerLow,
    surfaceContainerHigh = WildexPalette.SurfaceContainerHighest,
    surfaceContainerHighest = WildexPalette.SurfaceContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
    primary = WildexPalette.Primary,
    onPrimary = WildexPalette.OnPrimary,
    primaryContainer = WildexPalette.PrimaryContainer,
    onPrimaryContainer = WildexPalette.DarkOnSurface,
    secondary = WildexPalette.DarkSecondarySurface,
    onSecondary = WildexPalette.DarkOnSecondarySurface,
    secondaryContainer = WildexPalette.DarkSurfaceVariant,
    onSecondaryContainer = WildexPalette.DarkOnSurface,
    tertiary = WildexPalette.SecondaryMuted,
    onTertiary = WildexPalette.DarkOnSurface,
    tertiaryContainer = WildexPalette.DarkSurfaceVariant,
    onTertiaryContainer = WildexPalette.DarkOnSurface,
    error = WildexPalette.Error,
    onError = WildexPalette.OnError,
    errorContainer = WildexPalette.PrimaryContainer,
    onErrorContainer = WildexPalette.DarkOnSurface,
    background = WildexPalette.DarkBackground,
    onBackground = WildexPalette.DarkOnSurface,
    surface = WildexPalette.DarkSurface,
    onSurface = WildexPalette.DarkOnSurface,
    surfaceVariant = WildexPalette.DarkSurfaceVariant,
    onSurfaceVariant = WildexPalette.SecondaryMuted,
    outline = WildexPalette.DarkOutline,
    outlineVariant = WildexPalette.OutlineVariant,
    scrim = WildexPalette.DarkBackground,
    inverseSurface = WildexPalette.DarkOnSurface,
    inverseOnSurface = WildexPalette.DarkBackground,
    inversePrimary = WildexPalette.Primary,
    surfaceDim = WildexPalette.DarkBackground,
    surfaceBright = WildexPalette.DarkSurfaceVariant,
    surfaceContainerLowest = WildexPalette.DarkBackground,
    surfaceContainerLow = WildexPalette.DarkSurfaceVariant,
    surfaceContainer = WildexPalette.DarkSurface,
    surfaceContainerHigh = WildexPalette.DarkSurfaceVariant,
    surfaceContainerHighest = WildexPalette.SurfaceContainerHighest,
)

object WildexTheme {
    val extraColors: WildexExtraColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalWildexExtraColors.current
}

@Composable
fun WildexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** 브랜드 팔레트를 쓰려면 false (기본). 시스템 다이나믹 컬러는 디자인 시스템과 충돌할 수 있습니다. */
    dynamicColor: Boolean = false,
    /** DESIGN.md 기본은 [WildexShapes.cartridge]. 스펙 시트형 둥근 UI는 [WildexShapes.specSheet]. */
    shapes: Shapes = WildexShapes.cartridge,
    content: @Composable () -> Unit,
) {
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extra = if (darkTheme) darkWildexExtraColorScheme() else lightWildexExtraColorScheme()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalWildexExtraColors provides extra) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WildexTypography,
            shapes = shapes,
            content = content,
        )
    }
}

package dev.comon.wildex.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Material [androidx.compose.material3.ColorScheme]에 없는 카트리지 전용 토큰.
 */
@Immutable
data class WildexExtraColorScheme(
    val surfaceContainerLowest: Color,
    val surfaceContainerHighest: Color,
    /** 하드 오프셋 "질량" 섀도우 */
    val shadowMass: Color,
    /** 스펙 시트 Inverted 버튼 (#333) */
    val invertedButtonBackground: Color,
    val invertedButtonContent: Color,
    val searchBarBorderFocused: Color,
)

fun lightWildexExtraColorScheme(): WildexExtraColorScheme = WildexExtraColorScheme(
    surfaceContainerLowest = WildexPalette.SurfaceContainerLowest,
    surfaceContainerHighest = WildexPalette.SurfaceContainerHighest,
    shadowMass = WildexPalette.SecondaryMuted,
    invertedButtonBackground = WildexPalette.OnSecondarySurface,
    invertedButtonContent = WildexPalette.Neutral,
    searchBarBorderFocused = WildexPalette.Primary,
)

fun darkWildexExtraColorScheme(): WildexExtraColorScheme = WildexExtraColorScheme(
    surfaceContainerLowest = WildexPalette.DarkSurface,
    surfaceContainerHighest = WildexPalette.SurfaceContainerHighest,
    shadowMass = WildexPalette.SecondaryMuted,
    invertedButtonBackground = WildexPalette.SurfaceContainerHighest,
    invertedButtonContent = WildexPalette.DarkOnSurface,
    searchBarBorderFocused = WildexPalette.Primary,
)

val LocalWildexExtraColors = staticCompositionLocalOf<WildexExtraColorScheme> {
    error("WildexTheme로 루트를 감싸야 WildexTheme.extraColors를 사용할 수 있습니다.")
}

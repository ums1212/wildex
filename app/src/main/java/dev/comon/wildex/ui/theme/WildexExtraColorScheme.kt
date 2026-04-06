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
    /** 두꺼운 카트리지 외곽선 (다크: DESIGN.md `on-surface` #E2E2E2) */
    val cartridgeOutline: Color,
    /** 하드 오프셋 블록 (다크: DESIGN.md §5 불투명 블랙 #000000) */
    val cartridgeHardShadow: Color,
    /** 스펙 시트 Inverted 버튼 (#333) */
    val invertedButtonBackground: Color,
    val invertedButtonContent: Color,
    val searchBarBorderFocused: Color,
)

fun lightWildexExtraColorScheme(): WildexExtraColorScheme = WildexExtraColorScheme(
    surfaceContainerLowest = WildexPalette.SurfaceContainerLowest,
    surfaceContainerHighest = WildexPalette.SurfaceContainerHighest,
    shadowMass = WildexPalette.SecondaryMuted,
    cartridgeOutline = WildexPalette.OnSurface,
    cartridgeHardShadow = WildexPalette.OnSurface,
    invertedButtonBackground = WildexPalette.OnSecondarySurface,
    invertedButtonContent = WildexPalette.Neutral,
    searchBarBorderFocused = WildexPalette.Primary,
)

fun darkWildexExtraColorScheme(): WildexExtraColorScheme = WildexExtraColorScheme(
    surfaceContainerLowest = WildexPalette.NightVoid,
    surfaceContainerHighest = WildexPalette.NightSurfaceModuleHighest,
    shadowMass = Color.Black,
    cartridgeOutline = WildexPalette.NightOnSurface,
    cartridgeHardShadow = Color.Black,
    invertedButtonBackground = WildexPalette.NightSurfaceModuleHighest,
    invertedButtonContent = WildexPalette.NightOnSurface,
    searchBarBorderFocused = WildexPalette.NightPrimaryMuted,
)

val LocalWildexExtraColors = staticCompositionLocalOf<WildexExtraColorScheme> {
    error("WildexTheme로 루트를 감싸야 WildexTheme.extraColors를 사용할 수 있습니다.")
}

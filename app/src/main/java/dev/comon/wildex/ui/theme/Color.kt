package dev.comon.wildex.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Wildex 디자인 토큰.
 * UI는 [androidx.compose.material3.MaterialTheme.colorScheme], [WildexTheme.extraColors], [WildexColorRoles]를 우선 사용합니다.
 *
 * 라이트: 카트리지 / 스펙 시트. 다크: DESIGN.md **Night Mission** §3.
 */
object WildexPalette {
    // —— Light ——
    /** 스펙 시트의 #FF0000. 브랜드 레드는 라이트에서 [Primary]. */
    val SpecSheetPureRed = Color(0xFFFF0000)
    val Primary = Color(0xFFBC0100)
    val OnPrimary = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF9F9F9)
    val SurfaceContainerLow = Color(0xFFF3F3F4)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerHighest = Color(0xFFE2E2E2)
    val OnSurface = Color(0xFF1A1C1C)
    /** 플레이스홀더, 보조 텍스트, 라이트 테마 하드 섀도 질감 */
    val SecondaryMuted = Color(0xFF5D5F5F)
    val OutlineVariant = Color(0xFFEBBBB4)
    val SecondarySurface = Color(0xFFF0F0F0)
    val OnSecondarySurface = Color(0xFF333333)
    val Neutral = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFD6D6)
    val OnPrimaryContainer = OnSurface
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)

    // —— Night Mission (DESIGN.md §3) ——
    /** `background` — 깊은 베이스 */
    val NightVoid = Color(0xFF121414)
    /** `surface-container-low` — 그리드/보조 면 */
    val NightSurfaceContainerLow = Color(0xFF1A1C1C)
    /** `surface-container` — 일반 컴포넌트 면 */
    val NightSurfaceContainer = Color(0xFF1E2020)
    /** `surface-container-high` — 카드·모듈 */
    val NightSurfaceContainerHigh = Color(0xFF282A2A)
    /** 라벨/태그용 상위 모듈 면 (Typography §4) */
    val NightSurfaceModuleHighest = Color(0xFF323636)
    /** `primary` — 뮤트 기능 레드 */
    val NightPrimaryMuted = Color(0xFFFFB4AB)
    /** `primary-container` — 네온 CTA */
    val NightNeonCta = Color(0xFFFF544B)
    /** `on-surface` — 본문·구조적 보더 */
    val NightOnSurface = Color(0xFFE2E2E2)
    /** `on-surface-variant` — 보조 본문 */
    val NightOnSurfaceVariant = Color(0xFFE7BCB8)
    val NightOnPrimaryMuted = Color(0xFF1A1C1C)
    val NightOnNeonCta = Color(0xFFFFFFFF)
    /** 카드 `outline-variant` 등 */
    val NightOutlineVariant = Color(0xFF4A5050)
    val NightErrorContainer = Color(0xFF93000A)

    // —— Theme.kt 다크 스킴 연동용 별칭 (기존 이름 유지) ——
    val DarkBackground = NightVoid
    val DarkSurface = NightSurfaceContainer
    val DarkSurfaceContainerLow = NightSurfaceContainerLow
    val DarkSurfaceVariant = NightSurfaceContainerHigh
    val DarkOnSurface = NightOnSurface
    val DarkOnSurfaceVariant = NightOnSurfaceVariant
    val DarkOutline = NightOnSurface
    val DarkSecondarySurface = NightSurfaceModuleHighest
}

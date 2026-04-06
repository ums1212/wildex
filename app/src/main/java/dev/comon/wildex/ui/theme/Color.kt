package dev.comon.wildex.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Wildex 디자인 토큰 (DESIGN.md + 스펙 시트 색상).
 * UI에서는 하드코딩 대신 [androidx.compose.material3.MaterialTheme.colorScheme] 및 [WildexTheme.extraColors]를 사용합니다.
 */
object WildexPalette {
    // —— DESIGN.md: The Tactile Cartridge ——
    /** 스펙 시트의 #FF0000. 브랜드 레드는 DESIGN.md 기준 [Primary]를 사용합니다. */
    val SpecSheetPureRed = Color(0xFFFF0000)
    val Primary = Color(0xFFBC0100)
    val OnPrimary = Color(0xFFFFFFFF)
    val Surface = Color(0xFFF9F9F9)
    val SurfaceContainerLow = Color(0xFFF3F3F4)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerHighest = Color(0xFFE2E2E2)
    val OnSurface = Color(0xFF1A1C1C)
    /** 플레이스홀더, 보조 텍스트, 하드 섀도우 블록(DESIGN.md secondary) */
    val SecondaryMuted = Color(0xFF5D5F5F)
    val OutlineVariant = Color(0xFFEBBBB4)

    // —— 스펙 시트: 세컨더리/터셔리/뉴트럴 ——
    /** 스펙 시트 Secondary 버튼 면 */
    val SecondarySurface = Color(0xFFF0F0F0)
    /** 스펙 시트 Tertiary(본문/아이콘 대비) */
    val OnSecondarySurface = Color(0xFF333333)
    val Neutral = Color(0xFFFFFFFF)

    /** primary_container / 강조 구역용 연한 톤 */
    val PrimaryContainer = Color(0xFFFFD6D6)
    val OnPrimaryContainer = OnSurface

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)

    // —— Dark: "Midnight Version" ——
    val DarkBackground = Color(0xFF1A1C1C)
    val DarkSurface = Color(0xFF252828)
    val DarkOnSurface = Color(0xFFF9F9F9)
    val DarkSurfaceVariant = Color(0xFF2E3030)
    /** 다크 테마에서의 에칭 보더 */
    val DarkOutline = SurfaceContainerHighest
    val DarkSecondarySurface = Color(0xFF3A3C3C)
    val DarkOnSecondarySurface = Color(0xFFF0F0F0)
}

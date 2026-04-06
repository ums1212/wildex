package dev.comon.wildex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * DESIGN.md Night Mission: 깊은 배경(`NightVoid` 계열)에서는 CTA 면을 [ColorScheme.primaryContainer](네온),
 * 라이트에서는 [ColorScheme.primary](브랜드 레드)로 둡니다.
 */
object WildexColorRoles {
    /** sRGB 채널 평균으로 깊은 다크 배경 여부 (Night Mission `background` #121414 등). */
    private fun Color.isDeepDarkBackground(): Boolean {
        val avg = (red + green + blue) / 3f
        return avg < 0.1f
    }

    @Composable
    @ReadOnlyComposable
    fun missionCtaBackground(): Color {
        val scheme = MaterialTheme.colorScheme
        return if (scheme.background.isDeepDarkBackground()) {
            scheme.primaryContainer
        } else {
            scheme.primary
        }
    }

    @Composable
    @ReadOnlyComposable
    fun missionCtaForeground(): Color {
        val scheme = MaterialTheme.colorScheme
        return if (scheme.background.isDeepDarkBackground()) {
            scheme.onPrimaryContainer
        } else {
            scheme.onPrimary
        }
    }

    /** 아이콘 웰 위 액센트(다크: 뮤트 프라이머리 #FFB4AB). */
    @Composable
    @ReadOnlyComposable
    fun missionCtaIconAccent(): Color = MaterialTheme.colorScheme.primary
}

package dev.comon.wildex.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md 기본: 0 라디우(직각).
 * 스펙 시트용 둥근 쉐이프는 [specSheet]를 사용합니다.
 */
object WildexShapes {
    val cartridge: Shapes = Shapes(
        extraSmall = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(0.dp),
        medium = RoundedCornerShape(0.dp),
        large = RoundedCornerShape(0.dp),
        extraLarge = RoundedCornerShape(0.dp),
    )

    val specSheet: Shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(WildexDimens.radiusSmallSoft),
        medium = RoundedCornerShape(WildexDimens.radiusComponentSoft),
        large = RoundedCornerShape(WildexDimens.radiusComponentSoft),
        extraLarge = RoundedCornerShape(16.dp),
    )
}

package dev.comon.wildex.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md: 4px 픽셀 스트로크, 하드 오프셋 섀도우, 블록 그리드.
 */
object WildexDimens {
    /** 보더·디바이더에 사용하는 두께 (1dp 라인 금지) */
    val borderStrokeChunky: Dp = 4.dp

    /** "Hard Offset" 섀도우/비벨 시뮬레이션 시 이동량 */
    val shadowOffsetHard: Dp = 4.dp

    val gridStep: Dp = 4.dp

    /**
     * 스펙 시트(검색바·버튼·네비)의 둥근 형태와의 호환용.
     * 카트리지(0라디우)가 기본이며, 필요 시 컴포넌트에만 이 값을 사용합니다.
     */
    val radiusComponentSoft: Dp = 12.dp

    val radiusSmallSoft: Dp = 8.dp
}

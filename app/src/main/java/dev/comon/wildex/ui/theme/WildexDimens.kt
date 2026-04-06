package dev.comon.wildex.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DESIGN.md: 구조선·하드 오프셋, 16px 기술 그리드(§7).
 */
object WildexDimens {
    /** UI 정렬용 메이저 그리드 (DESIGN.md 16px) */
    val gridMajor: Dp = 16.dp

    /** 보더·디바이더에 사용하는 두께 (1dp 라인 금지; §3은 2–4px 프레임) */
    val borderStrokeChunky: Dp = 4.dp

    /** "Hard Offset" / 스프라이트 레이어링 — DESIGN.md §5 (4px·8px) */
    val shadowOffsetHard: Dp = 4.dp

    val gridStep: Dp = 4.dp

    /**
     * 스펙 시트(검색바·버튼·네비)의 둥근 형태와의 호환용.
     * 카트리지(0라디우)가 기본이며, 필요 시 컴포넌트에만 이 값을 사용합니다.
     */
    val radiusComponentSoft: Dp = 12.dp

    val radiusSmallSoft: Dp = 8.dp
}

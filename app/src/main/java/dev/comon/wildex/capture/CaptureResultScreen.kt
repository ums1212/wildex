package dev.comon.wildex.capture

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.comon.wildex.ui.WildexMainTabEmptyScreen

/**
 * 사진 분석 완료 후 표시되는 결과 화면.
 * 현재는 API 미구현 상태이므로 플레이스홀더를 보여준다.
 * 실제 API 연결 후 조류 도감 상세 화면(BirdInfoScreen)으로 교체 예정.
 */
@Composable
fun CaptureResultScreen(
    speciesId: String,
    modifier: Modifier = Modifier,
) {
    WildexMainTabEmptyScreen(
        title = "ANALYSIS COMPLETE",
        bodyText = "분석이 완료되었습니다.\n종 식별 ID: $speciesId\n\n상세 정보는 추후 제공됩니다.",
        modifier = modifier,
    )
}

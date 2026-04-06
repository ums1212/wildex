package dev.comon.wildex.ui.capture

/** Capture 화면 MVI 계약: UI 상태는 StateFlow, 일회성 피드백은 Channel(→ Flow)로 전달합니다. */
data class CaptureUiState(
    val hasCameraPermission: Boolean = false,
    val flashOn: Boolean = false,
    val zoomRatio: Float = 1f,
    val zoomMin: Float = 1f,
    val zoomMax: Float = 4f,
    val isoAuto: Boolean = true,
    val wbDay: Boolean = true,
)

/** 사용자 액션·카메라 계층에서 VM으로 보내는 입력 */
sealed interface CaptureIntent {
    data class PermissionResult(val granted: Boolean) : CaptureIntent
    data object FlashOn : CaptureIntent
    data object FlashOff : CaptureIntent
    data object ZoomIn : CaptureIntent
    data object ZoomOut : CaptureIntent
    data object ToggleIso : CaptureIntent
    data object ToggleWb : CaptureIntent
    /** 셔터 탭 — VM이 [CaptureUiEvent.TakePicture]를 방출 */
    data object CaptureClicked : CaptureIntent
    data class ZoomBoundsUpdated(val min: Float, val max: Float) : CaptureIntent
    data class CameraBindFailed(val error: Throwable) : CaptureIntent
    data object CaptureSucceeded : CaptureIntent
    data class CaptureFailed(val error: Throwable) : CaptureIntent
}

/** UI가 한 번만 처리하면 되는 이벤트 (토스트, 실제 촬영 트리거 등) */
sealed interface CaptureUiEvent {
    data object TakePicture : CaptureUiEvent
    data class ShowError(val message: String) : CaptureUiEvent
}

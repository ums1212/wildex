package dev.comon.wildex.capture

/** Capture 화면 MVI 계약: UI 상태는 StateFlow, 일회성 피드백은 Channel(→ Flow)로 전달합니다. */
data class CaptureUiState(
    val hasCameraPermission: Boolean = false,
    val flashOn: Boolean = false,
    val zoomRatio: Float = 1f,
    val zoomMin: Float = 1f,
    val zoomMax: Float = 4f,
    val isoAuto: Boolean = true,
    val wbDay: Boolean = true,
    /** 서버 분석 요청 진행 중 여부 — true 일 때 오버레이 표시 및 셔터 잠금 */
    val isAnalyzing: Boolean = false,
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
    /** 촬영 성공 — JPEG 바이트와 화면 회전값을 함께 전달 */
    data class CaptureSucceeded(val imageBytes: ByteArray, val rotationDegrees: Int) : CaptureIntent {
        // ByteArray는 구조 동등성을 직접 비교해야 함
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CaptureSucceeded) return false
            return rotationDegrees == other.rotationDegrees &&
                imageBytes.contentEquals(other.imageBytes)
        }
        override fun hashCode(): Int = 31 * imageBytes.contentHashCode() + rotationDegrees
    }
    data class CaptureFailed(val error: Throwable) : CaptureIntent
}

/** UI가 한 번만 처리하면 되는 이벤트 (스낵바, 실제 촬영 트리거, 화면 이동 등) */
sealed interface CaptureUiEvent {
    data object TakePicture : CaptureUiEvent
    data class ShowSnackbar(val message: String) : CaptureUiEvent
    data class NavigateToBirdInfo(val speciesId: String) : CaptureUiEvent
}

package dev.comon.wildex.capture

/** 서버 분석 과정의 단계별 상태 */
sealed interface AnalysisState {
    data object Analyzing : AnalysisState
    data class Success(val speciesId: String) : AnalysisState
    data class Error(val error: AnalysisError) : AnalysisState
}

/** 분석 실패 케이스별 유저 친화적 메시지 */
enum class AnalysisError(val userMessage: String) {
    NETWORK_UNAVAILABLE("네트워크에 연결할 수 없습니다. 연결 상태를 확인해주세요."),
    SERVER_ERROR("서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
    TIMEOUT("분석 요청 시간이 초과되었습니다. 다시 시도해주세요."),
    INVALID_IMAGE("이미지를 분석할 수 없습니다. 다시 촬영해주세요."),
    CAMERA_NOT_READY("카메라가 준비되지 않았습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN("알 수 없는 오류가 발생했습니다. 다시 시도해주세요."),
}

package dev.comon.wildex.capture

/** 조류 이미지 인식 과정의 단계별 상태 (AI SDK 종류에 무관) */
sealed interface BirdRecognitionState {
    data object Analyzing : BirdRecognitionState
    data class Recognized(val birdName: String, val category: String?) : BirdRecognitionState
    data class Error(val error: BirdRecognitionError) : BirdRecognitionState
}

/** 인식 실패 케이스별 유저 친화적 메시지 */
enum class BirdRecognitionError(val userMessage: String) {
    NETWORK_UNAVAILABLE("네트워크에 연결할 수 없습니다. 연결 상태를 확인해주세요."),
    TIMEOUT("분석 시간이 초과되었습니다. 다시 시도해주세요."),
    NOT_A_BIRD("대상을 인식할 수 없습니다. 다시 촬영해주세요."),
    API_KEY_INVALID("AI 서비스 인증에 실패했습니다. 관리자에게 문의해주세요."),
    UNKNOWN("알 수 없는 오류가 발생했습니다. 다시 시도해주세요."),
}

package dev.comon.wildex.data

import dev.comon.wildex.capture.BirdRecognitionState
import kotlinx.coroutines.flow.Flow

/**
 * 조류 이미지 인식 인터페이스.
 * 특정 AI SDK에 종속되지 않으므로 구현체(Gemini, OpenAI 등)를 자유롭게 교체할 수 있다.
 */
interface BirdImageAnalyzer {
    /**
     * [imageBytes] JPEG 바이트를 분석하여 조류 이름을 추출한다.
     * [rotationDegrees] 가 0이 아니면 구현체 내부에서 이미지를 회전한 뒤 분석한다.
     *
     * Flow 방출 순서: [BirdRecognitionState.Analyzing] → [BirdRecognitionState.Recognized] 또는 [BirdRecognitionState.Error]
     */
    fun recognizeBird(imageBytes: ByteArray, rotationDegrees: Int = 0): Flow<BirdRecognitionState>
}

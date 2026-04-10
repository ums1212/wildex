package dev.comon.wildex.data

import dev.comon.wildex.domain.model.BirdPrediction

/**
 * 가장 최근 분석 결과를 화면 간 공유하기 위한 인메모리 저장소.
 * 네비게이션 라우트에 복잡한 객체를 직렬화하지 않고도 CaptureResultScreen에서 전체 예측 목록을 읽을 수 있다.
 */
object AnalysisResultStore {

    var latestPredictions: List<BirdPrediction> = emptyList()
        private set

    fun save(predictions: List<BirdPrediction>) {
        latestPredictions = predictions
    }
}

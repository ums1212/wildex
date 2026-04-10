package dev.comon.wildex.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import dev.comon.wildex.capture.AnalysisState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.ByteArrayOutputStream

class AnalysisRepository {

    /**
     * 이미지를 서버에 전송하고 분석 결과를 [Flow]로 반환한다.
     *
     * [rotationDegrees]가 0이 아니면 비트맵을 물리적으로 회전한 뒤 전송한다.
     * (세로 모드에서 카메라 센서가 가로로 캡처하므로 서버가 올바른 방향으로 받을 수 있도록 보정)
     *
     * 현재는 Mock 구현으로 2초 딜레이 후 가짜 speciesId를 반환한다.
     * 실제 API 구현 완료 후 [delay] 부분을 API 호출로 교체하면 된다.
     */
    fun analyzeImage(imageBytes: ByteArray, rotationDegrees: Int): Flow<AnalysisState> = flow {
        emit(AnalysisState.Analyzing)

        // 회전이 필요한 경우에만 비트맵 처리 (가로 모드는 0도이므로 원본 그대로)
        val bytesToSend = if (rotationDegrees != 0) {
            rotateBitmap(imageBytes, rotationDegrees)
        } else {
            imageBytes
        }

        // TODO: 실제 API 구현 시 아래 delay를 API 호출로 교체 (bytesToSend 전달)
        @Suppress("UNUSED_VARIABLE")
        val unused = bytesToSend
        delay(2000L)
        emit(AnalysisState.Success("MOCK_SPECIES_001"))
    }.flowOn(Dispatchers.IO)

    private fun rotateBitmap(imageBytes: ByteArray, degrees: Int): ByteArray {
        val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        original.recycle()
        return ByteArrayOutputStream().use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 95, out)
            rotated.recycle()
            out.toByteArray()
        }
    }
}

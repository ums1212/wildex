package dev.comon.wildex.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import dev.comon.wildex.capture.BirdRecognitionError
import dev.comon.wildex.capture.BirdRecognitionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "GeminiBirdAnalyzer"
private const val MODEL_NAME = "gemini-3-flash-preview"

/**
 * Firebase AI Logic를 사용한 [BirdImageAnalyzer] 구현체.
 * 이미지를 Gemini 비전 모델에 전달하여 조류 한국어 이름을 추출한다.
 */
class GeminiBirdImageAnalyzer : BirdImageAnalyzer {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(MODEL_NAME)

    override fun recognizeBird(imageBytes: ByteArray, rotationDegrees: Int): Flow<BirdRecognitionState> = flow {
        emit(BirdRecognitionState.Analyzing)

        val bitmap = decodeBitmap(imageBytes, rotationDegrees)
            ?: run {
                emit(BirdRecognitionState.Error(BirdRecognitionError.UNKNOWN))
                return@flow
            }

        try {
            Log.d(TAG, "Gemini 요청 전송 (model=$MODEL_NAME, rotation=$rotationDegrees°)")
            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(RECOGNITION_PROMPT)
                },
            )
            bitmap.recycle()

            val text = response.text?.trim().orEmpty()
            Log.d(TAG, "Gemini 원본 응답: $text")

            // 마크다운 코드 블록이 포함된 경우 제거
            val json = text
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            Log.d(TAG, "파싱된 JSON: $json")

            val found = Regex("\"found\"\\s*:\\s*true").containsMatchIn(json)
            if (!found) {
                Log.d(TAG, "조류 미인식 → NOT_A_BIRD")
                emit(BirdRecognitionState.Error(BirdRecognitionError.NOT_A_BIRD))
                return@flow
            }

            val nameMatch = Regex("\"koreanName\"\\s*:\\s*\"([^\"]+)\"").find(json)
            val birdName = nameMatch?.groupValues?.getOrNull(1)?.trim()
            if (birdName.isNullOrBlank()) {
                Log.d(TAG, "koreanName 파싱 실패 → NOT_A_BIRD")
                emit(BirdRecognitionState.Error(BirdRecognitionError.NOT_A_BIRD))
                return@flow
            }

            val categoryMatch = Regex("\"category\"\\s*:\\s*\"([^\"]+)\"").find(json)
            val category = categoryMatch?.groupValues?.getOrNull(1)?.trim()
                ?.ifBlank { null } ?: "조류"

            Log.d(TAG, "인식 성공 → birdName=$birdName, category=$category")
            emit(BirdRecognitionState.Recognized(birdName, category))
        } catch (e: Exception) {
            bitmap.recycle()
            val error = mapException(e)
            Log.e(TAG, "Gemini 호출 실패 → $error", e)
            emit(BirdRecognitionState.Error(error))
        }
    }.flowOn(Dispatchers.IO)

    private fun decodeBitmap(imageBytes: ByteArray, rotationDegrees: Int): Bitmap? {
        val original = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null
        if (rotationDegrees == 0) return original
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            .also { original.recycle() }
    }

    private fun mapException(e: Throwable): BirdRecognitionError {
        if (e is UnknownHostException) return BirdRecognitionError.NETWORK_UNAVAILABLE
        if (e is SocketTimeoutException) return BirdRecognitionError.TIMEOUT
        if (e is kotlinx.coroutines.TimeoutCancellationException) return BirdRecognitionError.TIMEOUT
        val msg = e.message?.lowercase().orEmpty()
        return when {
            msg.contains("api key") || msg.contains("api_key") ||
                msg.contains("invalid") && msg.contains("key") ||
                msg.contains("403") -> BirdRecognitionError.API_KEY_INVALID
            msg.contains("network") || msg.contains("connect") ||
                msg.contains("unreachable") -> BirdRecognitionError.NETWORK_UNAVAILABLE
            else -> BirdRecognitionError.UNKNOWN
        }
    }

    companion object {
        private val RECOGNITION_PROMPT = """
이 이미지에 조류(새)가 있는지 분석해주세요.
반드시 아래 JSON 형식으로만 응답하세요. 마크다운이나 다른 텍스트는 포함하지 마세요.

조류가 있는 경우:
{"found": true, "koreanName": "조류의 한국어 이름", "category": "조류"}

조류가 없는 경우:
{"found": false}
""".trimIndent()
    }
}

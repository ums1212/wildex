package dev.comon.wildex.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import dev.comon.wildex.capture.AnalysisError
import dev.comon.wildex.capture.AnalysisState
import dev.comon.wildex.domain.model.BirdPrediction
import dev.comon.wildex.network.WildexApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

class AnalysisRepository {

    /**
     * 이미지를 `/bird-identify/`에 multipart/form-data로 업로드하고,
     * iNaturalist 기반 예측 결과(최대 3개)를 [Flow]로 반환한다.
     *
     * [rotationDegrees]가 0이 아니면 비트맵을 물리적으로 회전한 뒤 전송한다.
     */
    fun analyzeImage(imageBytes: ByteArray, rotationDegrees: Int): Flow<AnalysisState> = flow {
        emit(AnalysisState.Analyzing)

        val bytesToSend = if (rotationDegrees != 0) {
            rotateBitmap(imageBytes, rotationDegrees)
        } else {
            imageBytes
        }

        try {
            val requestBody = bytesToSend.toRequestBody("image/jpeg".toMediaType())
            val imagePart = MultipartBody.Part.createFormData("image", "capture.jpg", requestBody)

            val response = WildexApiClient.wildexApiService.identifyBird(imagePart)

            if (!response.success || response.predictions.isEmpty()) {
                emit(AnalysisState.Error(AnalysisError.INVALID_IMAGE))
                return@flow
            }

            val predictions = response.predictions.map { dto ->
                BirdPrediction(
                    taxonId = dto.taxon.id,
                    scientificName = dto.taxon.name,
                    commonName = dto.taxon.preferredCommonName,
                    combinedScore = dto.combinedScore,
                    visionScore = dto.visionScore,
                )
            }
            emit(AnalysisState.Success(predictions))
        } catch (e: HttpException) {
            val error = when (e.code()) {
                400 -> AnalysisError.INVALID_IMAGE
                502 -> AnalysisError.SERVER_ERROR
                else -> AnalysisError.UNKNOWN
            }
            emit(AnalysisState.Error(error))
        }
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

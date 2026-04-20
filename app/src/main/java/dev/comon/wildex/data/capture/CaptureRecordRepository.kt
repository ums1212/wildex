package dev.comon.wildex.data.capture

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "CaptureRecordRepo"

class CaptureRecordRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = WildexDatabase.getInstance(appContext).captureRecordDao()

    suspend fun saveCapture(
        imageBytes: ByteArray,
        rotationDegrees: Int,
        hasLocationPermission: Boolean,
    ): Long? = withContext(Dispatchers.IO) {
        runCatching {
            val uri = MediaStoreImageSaver.save(appContext, imageBytes, rotationDegrees)
            val capturedAt = System.currentTimeMillis()

            val (latitude, longitude, address) = if (hasLocationPermission) {
                val location = LocationResolver.currentLocation(appContext)
                if (location != null) {
                    val addr = LocationResolver.reverseGeocode(appContext, location.latitude, location.longitude)
                        ?: "%.4f, %.4f".format(location.latitude, location.longitude)
                    Triple(location.latitude, location.longitude, addr)
                } else {
                    Triple(null, null, "위치 확인 불가")
                }
            } else {
                Triple<Double?, Double?, String>(null, null, "권한 거절")
            }

            dao.insert(
                CaptureRecordEntity(
                    imageUri = uri.toString(),
                    capturedAt = capturedAt,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                ),
            )
        }.getOrElse {
            Log.e(TAG, "저장 실패", it)
            null
        }
    }

    suspend fun updateRecognition(id: Long, name: String, category: String) {
        runCatching { dao.updateRecognition(id, name, category) }
            .onFailure { Log.e(TAG, "updateRecognition 실패", it) }
    }
}

package dev.comon.wildex.data.capture

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val TAG = "CaptureRecordRepo"

class CaptureRecordRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = WildexDatabase.getInstance(appContext).captureRecordDao()

    suspend fun saveImageToMediaStore(imageBytes: ByteArray, rotationDegrees: Int): Uri? =
        withContext(Dispatchers.IO) {
            runCatching {
                MediaStoreImageSaver.save(appContext, imageBytes, rotationDegrees)
            }.getOrElse {
                Log.e(TAG, "미디어 저장 실패", it)
                null
            }
        }

    suspend fun insertCaptureRecord(imageUri: Uri, hasLocationPermission: Boolean): Long =
        withContext(Dispatchers.IO) {
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
                    imageUri = imageUri.toString(),
                    capturedAt = capturedAt,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                ),
            )
        }

    suspend fun updateRecognition(id: Long, name: String, category: String?) {
        runCatching { dao.updateRecognition(id, name, category) }
            .onFailure { Log.e(TAG, "updateRecognition 실패", it) }
    }

    suspend fun updateMemo(id: Long, memo: String?) = withContext(Dispatchers.IO) {
        runCatching { dao.updateMemo(id, memo?.takeIf { it.isNotBlank() }) }
            .onFailure { Log.e(TAG, "updateMemo 실패", it) }
    }

    suspend fun updateName(id: Long, name: String?) = withContext(Dispatchers.IO) {
        runCatching { dao.updateName(id, name) }
            .onFailure { Log.e(TAG, "updateName 실패", it) }
    }

    suspend fun updateCategory(id: Long, category: String?) = withContext(Dispatchers.IO) {
        runCatching { dao.updateCategory(id, category) }
            .onFailure { Log.e(TAG, "updateCategory 실패", it) }
    }

    suspend fun updateAddress(id: Long, address: String) = withContext(Dispatchers.IO) {
        runCatching { dao.updateAddress(id, address) }
            .onFailure { Log.e(TAG, "updateAddress 실패", it) }
    }

    fun observeRecord(id: Long): Flow<CaptureRecordEntity?> = dao.observeById(id)

    suspend fun deleteRecord(id: Long) = withContext(Dispatchers.IO) {
        runCatching { dao.deleteById(id) }
            .onFailure { Log.e(TAG, "deleteRecord 실패", it) }
    }

    suspend fun deleteRecords(ids: List<Long>) = withContext(Dispatchers.IO) {
        runCatching { dao.deleteByIds(ids) }
            .onFailure { Log.e(TAG, "deleteRecords 실패", it) }
    }

    fun recordsPager(pageSize: Int = 10): Flow<PagingData<CaptureRecordEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { dao.pagingSource() },
        ).flow

    fun filteredRecordsPager(
        pageSize: Int = 10,
        startMillis: Long?,
        endMillis: Long?,
        category: String,
        query: String?,
        ascending: Boolean,
    ): Flow<PagingData<CaptureRecordEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                dao.filteredPagingSource(startMillis, endMillis, category, query, ascending)
            },
        ).flow
}

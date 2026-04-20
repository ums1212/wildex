package dev.comon.wildex.data.capture

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureRecordDao {
    @Insert
    suspend fun insert(record: CaptureRecordEntity): Long

    @Query("UPDATE capture_record SET name = :name, category = :category WHERE id = :id")
    suspend fun updateRecognition(id: Long, name: String, category: String)

    @Query("SELECT * FROM capture_record ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<CaptureRecordEntity>>
}

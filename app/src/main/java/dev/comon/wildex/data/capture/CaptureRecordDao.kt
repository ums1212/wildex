package dev.comon.wildex.data.capture

import androidx.paging.PagingSource
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

    @Query("SELECT * FROM capture_record ORDER BY capturedAt DESC")
    fun pagingSource(): PagingSource<Int, CaptureRecordEntity>

    @Query("SELECT * FROM capture_record WHERE id = :id")
    fun observeById(id: Long): Flow<CaptureRecordEntity?>

    @Query("DELETE FROM capture_record WHERE id = :id")
    suspend fun deleteById(id: Long)
}

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

    @Query("UPDATE capture_record SET memo = :memo WHERE id = :id")
    suspend fun updateMemo(id: Long, memo: String?)

    @Query("SELECT * FROM capture_record ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<CaptureRecordEntity>>

    @Query("SELECT * FROM capture_record ORDER BY capturedAt DESC")
    fun pagingSource(): PagingSource<Int, CaptureRecordEntity>

    @Query(
        """
        SELECT * FROM capture_record
        WHERE (:startMillis IS NULL OR capturedAt >= :startMillis)
          AND (:endMillis IS NULL OR capturedAt <= :endMillis)
          AND (
              :query IS NULL
              OR (:category = 'NAME'     AND name     LIKE '%' || :query || '%')
              OR (:category = 'CATEGORY' AND category LIKE '%' || :query || '%')
              OR (:category = 'LOCATION' AND address  LIKE '%' || :query || '%')
              OR (:category = 'MEMO'     AND memo     LIKE '%' || :query || '%')
          )
        ORDER BY
            CASE WHEN :ascending = 1 THEN capturedAt END ASC,
            CASE WHEN :ascending = 0 THEN capturedAt END DESC
        """,
    )
    fun filteredPagingSource(
        startMillis: Long?,
        endMillis: Long?,
        category: String,
        query: String?,
        ascending: Boolean,
    ): PagingSource<Int, CaptureRecordEntity>

    @Query("SELECT * FROM capture_record WHERE id = :id")
    fun observeById(id: Long): Flow<CaptureRecordEntity?>

    @Query("DELETE FROM capture_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM capture_record WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

package dev.comon.wildex.data.capture

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capture_record")
data class CaptureRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val capturedAt: Long,
    val latitude: Double?,
    val longitude: Double?,
    val address: String,
    val name: String? = null,
    val category: String? = null,
)

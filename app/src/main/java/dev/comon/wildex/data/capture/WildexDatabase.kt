package dev.comon.wildex.data.capture

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CaptureRecordEntity::class], version = 1, exportSchema = true)
abstract class WildexDatabase : RoomDatabase() {
    abstract fun captureRecordDao(): CaptureRecordDao

    companion object {
        @Volatile
        private var instance: WildexDatabase? = null

        fun getInstance(context: Context): WildexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WildexDatabase::class.java,
                    "wildex.db",
                ).build().also { instance = it }
            }
    }
}

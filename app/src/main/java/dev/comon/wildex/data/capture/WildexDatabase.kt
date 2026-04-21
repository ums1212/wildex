package dev.comon.wildex.data.capture

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CaptureRecordEntity::class], version = 2, exportSchema = true)
abstract class WildexDatabase : RoomDatabase() {
    abstract fun captureRecordDao(): CaptureRecordDao

    companion object {
        @Volatile
        private var instance: WildexDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE capture_record ADD COLUMN memo TEXT")
            }
        }

        fun getInstance(context: Context): WildexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WildexDatabase::class.java,
                    "wildex.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

package com.shaikh.storentry.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shaikh.storentry.data.local.dao.HistoryDao
import com.shaikh.storentry.data.local.dao.ProductDao
import com.shaikh.storentry.data.local.dao.ReminderDao
import com.shaikh.storentry.data.local.entity.HistoryEntity
import com.shaikh.storentry.data.local.entity.ProductEntity
import com.shaikh.storentry.data.local.entity.ReminderEntity

/**
 * Room App Database.
 */
@Database(
    entities = [ProductEntity::class, HistoryEntity::class, ReminderEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val historyDao: HistoryDao
    abstract val reminderDao: ReminderDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `actionType` TEXT NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `metadata` TEXT)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `scheduledTime` INTEGER NOT NULL, `status` TEXT NOT NULL, `workId` TEXT NOT NULL)"
                )
            }
        }
    }
}

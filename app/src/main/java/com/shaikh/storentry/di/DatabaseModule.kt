package com.shaikh.storentry.di

import android.content.Context
import androidx.room.Room
import com.shaikh.storentry.data.local.AppDatabase
import com.shaikh.storentry.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(db: AppDatabase) = db.productDao

    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase) = db.historyDao

    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase) = db.reminderDao
}

package com.shaikh.storentry.di

import com.shaikh.storentry.data.repository.HistoryRepositoryImpl
import com.shaikh.storentry.data.repository.ProductRepositoryImpl
import com.shaikh.storentry.domain.repository.HistoryRepository
import com.shaikh.storentry.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(
        impl: com.shaikh.storentry.data.repository.PreferenceRepositoryImpl
    ): com.shaikh.storentry.domain.repository.PreferenceRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: com.shaikh.storentry.data.repository.ReminderRepositoryImpl
    ): com.shaikh.storentry.domain.repository.ReminderRepository
}

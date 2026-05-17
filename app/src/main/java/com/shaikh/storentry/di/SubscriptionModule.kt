package com.shaikh.storentry.di

import com.shaikh.storentry.data.repository.SubscriptionRepositoryImpl
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * SubscriptionModule — Hilt Module for exposing clean dependency bindings
 * for billing and entitlement services.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SubscriptionModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository
}

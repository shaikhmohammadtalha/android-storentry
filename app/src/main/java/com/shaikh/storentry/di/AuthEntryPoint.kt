package com.shaikh.storentry.di

import com.shaikh.storentry.domain.repository.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * AuthEntryPoint — Hilt EntryPoint interface to access AuthRepository
 * from Composable functions outside of ViewModels (e.g. AppNavGraph paywall gating).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthEntryPoint {
    fun authRepository(): AuthRepository
}

package com.shaikh.storentry.di

import com.shaikh.storentry.util.analytics.AnalyticsManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * AnalyticsEntryPoint — Hilt EntryPoint interface to inject AnalyticsManager
 * directly inside non-Hilt classes or Composable functions.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun analyticsManager(): AnalyticsManager
}

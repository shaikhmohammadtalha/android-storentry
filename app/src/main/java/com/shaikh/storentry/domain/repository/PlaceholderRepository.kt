package com.shaikh.storentry.domain.repository

import com.shaikh.storentry.domain.model.PlaceholderModel
import kotlinx.coroutines.flow.Flow

/**
 * Placeholder Repository Interface.
 */
interface PlaceholderRepository {
    fun getPlaceholders(): Flow<List<PlaceholderModel>>
    suspend fun addPlaceholder(name: String)
}

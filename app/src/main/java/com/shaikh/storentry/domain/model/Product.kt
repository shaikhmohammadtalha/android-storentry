package com.shaikh.storentry.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a Product.
 */
@Serializable
data class Product(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val lowStockThreshold: Int
)

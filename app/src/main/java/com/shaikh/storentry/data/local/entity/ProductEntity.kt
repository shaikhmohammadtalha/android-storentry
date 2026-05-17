package com.shaikh.storentry.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shaikh.storentry.domain.model.Product

/**
 * Room entity for Product.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val quantity: Int,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val lowStockThreshold: Int
)

/**
 * Extension to convert entity to domain model.
 */
fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    category = category,
    quantity = quantity,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    lowStockThreshold = lowStockThreshold
)

/**
 * Extension to convert domain model to entity.
 */
fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    category = category,
    quantity = quantity,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    lowStockThreshold = lowStockThreshold
)

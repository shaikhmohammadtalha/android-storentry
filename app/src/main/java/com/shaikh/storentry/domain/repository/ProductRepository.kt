package com.shaikh.storentry.domain.repository

import com.shaikh.storentry.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing products.
 */
interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    
    suspend fun getProductById(id: String): Product?
    
    suspend fun addProduct(product: Product)
    
    suspend fun updateProduct(product: Product)
    
    suspend fun deleteProduct(product: Product)
    
    suspend fun updateStock(productId: String, newQuantity: Int)
    
    fun getLowStockProducts(): Flow<List<Product>>
    
    fun getProductCount(): Flow<Int>
    
    fun getTotalInventoryValue(): Flow<Double>
    
    fun getLowStockCount(): Flow<Int>
    
    fun getCategories(): Flow<List<String>>
}

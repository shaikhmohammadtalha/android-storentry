package com.shaikh.storentry.data.local.dao

import androidx.room.*
import com.shaikh.storentry.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for products.
 */
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET quantity = :newQuantity WHERE id = :productId")
    suspend fun updateStock(productId: String, newQuantity: Int)

    @Query("SELECT * FROM products WHERE quantity <= lowStockThreshold")
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("SELECT SUM(quantity * sellingPrice) FROM products")
    fun getTotalInventoryValue(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM products WHERE quantity <= lowStockThreshold")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getDistinctCategories(): Flow<List<String>>
}

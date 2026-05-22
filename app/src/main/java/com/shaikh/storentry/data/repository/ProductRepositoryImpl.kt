package com.shaikh.storentry.data.repository

import com.shaikh.storentry.data.local.dao.ProductDao
import com.shaikh.storentry.data.local.entity.toDomain
import com.shaikh.storentry.data.local.entity.toEntity
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProductRepository using Room.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val historyRepository: com.shaikh.storentry.domain.repository.HistoryRepository,
    private val cloudSyncManager: com.shaikh.storentry.data.sync.CloudSyncManager,
    private val notificationHelper: com.shaikh.storentry.utils.NotificationHelper
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
        cloudSyncManager.uploadProduct(product)
        historyRepository.addRecord(
            com.shaikh.storentry.domain.model.HistoryRecord(
                productId = product.id,
                productName = product.name,
                actionType = com.shaikh.storentry.domain.model.HistoryActionType.PRODUCT_ADDED,
                description = "New product added: ${product.category}",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateProduct(product: Product) {
        val oldProduct = getProductById(product.id)
        productDao.updateProduct(product.toEntity())
        cloudSyncManager.uploadProduct(product)
        
        if (oldProduct != null) {
            val record = when {
                oldProduct.sellingPrice != product.sellingPrice -> {
                    com.shaikh.storentry.domain.model.HistoryRecord(
                        productId = product.id,
                        productName = product.name,
                        actionType = com.shaikh.storentry.domain.model.HistoryActionType.PRICE_CHANGED,
                        description = "Price updated: ₹${oldProduct.sellingPrice} → ₹${product.sellingPrice}",
                        timestamp = System.currentTimeMillis()
                    )
                }
                oldProduct.category != product.category -> {
                    com.shaikh.storentry.domain.model.HistoryRecord(
                        productId = product.id,
                        productName = product.name,
                        actionType = com.shaikh.storentry.domain.model.HistoryActionType.CATEGORY_CHANGED,
                        description = "Product category changed to \"${product.category}\"",
                        timestamp = System.currentTimeMillis()
                    )
                }
                else -> {
                    com.shaikh.storentry.domain.model.HistoryRecord(
                        productId = product.id,
                        productName = product.name,
                        actionType = com.shaikh.storentry.domain.model.HistoryActionType.PRODUCT_UPDATED,
                        description = "Product details updated",
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
            historyRepository.addRecord(record)
        }

        // Trigger real-time low-stock notification if quantity falls below or equal to threshold
        if (product.quantity <= product.lowStockThreshold && (oldProduct == null || oldProduct.quantity != product.quantity)) {
            notificationHelper.showLowStockAlert(product.id, product.name, product.quantity)
        }
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
        cloudSyncManager.deleteProduct(product.id)
        val serializedProduct = kotlinx.serialization.json.Json.encodeToString(product)
        historyRepository.addRecord(
            com.shaikh.storentry.domain.model.HistoryRecord(
                productId = product.id,
                productName = product.name,
                actionType = com.shaikh.storentry.domain.model.HistoryActionType.PRODUCT_DELETED,
                description = "Product deleted from inventory",
                timestamp = System.currentTimeMillis(),
                metadata = serializedProduct
            )
        )
    }

    override suspend fun updateStock(productId: String, newQuantity: Int) {
        val product = getProductById(productId) ?: return
        val oldQuantity = product.quantity
        val diff = newQuantity - oldQuantity
        
        productDao.updateStock(productId, newQuantity)
        cloudSyncManager.updateStock(productId, newQuantity)
        
        if (diff != 0) {
            val actionType = if (diff > 0) 
                com.shaikh.storentry.domain.model.HistoryActionType.STOCK_ADDED 
            else 
                com.shaikh.storentry.domain.model.HistoryActionType.STOCK_REMOVED
            
            val description = if (diff > 0) 
                "Stock increased by $diff units" 
            else 
                "Stock reduced by ${-diff} units"
            
            historyRepository.addRecord(
                com.shaikh.storentry.domain.model.HistoryRecord(
                    productId = productId,
                    productName = product.name,
                    actionType = actionType,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    metadata = "Total: $newQuantity units${if (newQuantity <= product.lowStockThreshold) " · Low Stock Alert triggered" else ""}"
                )
            )

            // Trigger real-time low-stock notification if quantity falls below or equal to threshold
            if (newQuantity <= product.lowStockThreshold) {
                notificationHelper.showLowStockAlert(productId, product.name, newQuantity)
            }
        }
    }

    override fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProductCount(): Flow<Int> {
        return productDao.getProductCount()
    }

    override fun getTotalInventoryValue(): Flow<Double> {
        return productDao.getTotalInventoryValue().map { it ?: 0.0 }
    }

    override fun getLowStockCount(): Flow<Int> {
        return productDao.getLowStockCount()
    }

    override fun getCategories(): Flow<List<String>> {
        return productDao.getDistinctCategories()
    }
}

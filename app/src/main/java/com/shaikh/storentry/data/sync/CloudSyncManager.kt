package com.shaikh.storentry.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shaikh.storentry.data.local.AppDatabase
import com.shaikh.storentry.data.local.entity.ProductEntity
import com.shaikh.storentry.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CloudSyncManager — Coordinates seamless offline-first synchronization
 * between Android's Room DB and Firebase Firestore.
 */
@Singleton
class CloudSyncManager @Inject constructor(
    private val db: AppDatabase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferenceRepository: com.shaikh.storentry.domain.repository.PreferenceRepository,
    private val subscriptionRepository: com.shaikh.storentry.domain.repository.SubscriptionRepository
) {
    private val productDao = db.productDao
    
    private suspend fun shouldAutoSync(): Boolean {
        val isPremium = subscriptionRepository.isPremium()
        return isPremium && preferenceRepository.isAutoSyncEnabled().first()
    }

    private fun getUserId(): String? = auth.currentUser?.uid

    /**
     * Uploads/updates a single product to Firestore.
     */
    suspend fun uploadProduct(product: Product) {
        if (!shouldAutoSync()) return
        val userId = getUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(product.id)
                    .set(product)
                    .await()
                Timber.d("Successfully uploaded product ${product.id} to Firestore")
            } catch (e: Exception) {
                Timber.e(e, "Failed to upload product ${product.id} to Firestore")
            }
        }
    }

    /**
     * Deletes a single product from Firestore.
     */
    suspend fun deleteProduct(productId: String) {
        if (!shouldAutoSync()) return
        val userId = getUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(productId)
                    .delete()
                    .await()
                Timber.d("Successfully deleted product $productId from Firestore")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete product $productId from Firestore")
            }
        }
    }

    /**
     * Updates the quantity of a product in Firestore.
     */
    suspend fun updateStock(productId: String, newQuantity: Int) {
        if (!shouldAutoSync()) return
        val userId = getUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(productId)
                    .update("quantity", newQuantity)
                    .await()
                Timber.d("Successfully updated stock in Firestore for product $productId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to update stock in Firestore for product $productId")
            }
        }
    }

    /**
     * Performs a two-way sync on User Sign-in:
     * 1. Uploads guest session local data to Firestore to prevent data loss.
     * 2. Pulls all user cloud data and merges it into the local Room database.
     */
    suspend fun syncOnSignIn() {
        val userId = getUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                // 1. Back up any offline/guest products created before signing in
                val localEntities = productDao.getAllProducts().first()
                for (entity in localEntities) {
                    val product = Product(
                        id = entity.id,
                        name = entity.name,
                        category = entity.category,
                        quantity = entity.quantity,
                        purchasePrice = entity.purchasePrice,
                        sellingPrice = entity.sellingPrice,
                        lowStockThreshold = entity.lowStockThreshold
                    )
                    firestore.collection("users")
                        .document(userId)
                        .collection("products")
                        .document(product.id)
                        .set(product)
                        .await()
                }

                // 2. Fetch full remote collection from Firestore
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: continue
                    val category = doc.getString("category") ?: ""
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    val purchasePrice = doc.getDouble("purchasePrice") ?: 0.0
                    val sellingPrice = doc.getDouble("sellingPrice") ?: 0.0
                    val lowStockThreshold = doc.getLong("lowStockThreshold")?.toInt() ?: 5

                    val entity = ProductEntity(
                        id = doc.id,
                        name = name,
                        category = category,
                        quantity = quantity,
                        purchasePrice = purchasePrice,
                        sellingPrice = sellingPrice,
                        lowStockThreshold = lowStockThreshold
                    )
                    productDao.insertProduct(entity)
                }
                Timber.d("Firestore to Room sync completed successfully for user: $userId")
            } catch (e: Exception) {
                Timber.e(e, "Firestore sync failed on sign-in")
            }
        }
    }

    suspend fun syncOnSignOut() {
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                if (userId != null && shouldAutoSync()) {
                    val localEntities = productDao.getAllProducts().first()
                    for (entity in localEntities) {
                        val product = Product(
                            id = entity.id,
                            name = entity.name,
                            category = entity.category,
                            quantity = entity.quantity,
                            purchasePrice = entity.purchasePrice,
                            sellingPrice = entity.sellingPrice,
                            lowStockThreshold = entity.lowStockThreshold
                        )
                        firestore.collection("users")
                            .document(userId)
                            .collection("products")
                            .document(product.id)
                            .set(product)
                            .await()
                    }
                    Timber.d("Successfully pushed final local sync before wiping")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to perform final sync push on sign-out")
            }

            try {
                db.clearAllTables()
                Timber.d("Local database wiped successfully on sign-out")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear database on sign-out")
            }

            try {
                preferenceRepository.setPremium(false)
                preferenceRepository.setPremiumExpiry(0L)
                preferenceRepository.setActiveEntitlements(emptyList())
                preferenceRepository.setAutoSyncEnabled(false)
                preferenceRepository.setDebugForcePremium(false)
                Timber.d("Local subscription preferences cleared on sign-out")
            } catch (e: Exception) {
                Timber.e(e, "Failed to clear subscription preferences on sign-out")
            }
        }
    }

    /**
     * Triggers a manual sync.
     */
    suspend fun manualSync() {
        syncOnSignIn()
    }

    /**
     * Gets count of local products from Room.
     */
    suspend fun getLocalProductCount(): Int = withContext(Dispatchers.IO) {
        try {
            productDao.getProductCount().first()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get local product count")
            0
        }
    }

    /**
     * Gets count of remote products from Firestore.
     */
    suspend fun getRemoteProductCount(): Int = withContext(Dispatchers.IO) {
        val userId = getUserId() ?: return@withContext 0
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("products")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get remote product count")
            0
        }
    }

    /**
     * Resolves sync conflicts using the specified strategy.
     */
    suspend fun resolveConflictAndSync(resolution: SyncConflictResolution) {
        val userId = getUserId() ?: return
        withContext(Dispatchers.IO) {
            try {
                // Fetch local entities
                val localEntities = productDao.getAllProducts().first()
                val localMap = localEntities.associateBy { it.id }

                // Fetch remote documents
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .get()
                    .await()
                val remoteMap = snapshot.documents.associateBy { it.id }

                when (resolution) {
                    SyncConflictResolution.MERGE_CLOUD_MAIN -> {
                        // 1. Process all local products
                        for (localEntity in localEntities) {
                            val remoteDoc = remoteMap[localEntity.id]
                            if (remoteDoc != null) {
                                // Conflict! Cloud is main. Update local database to match Cloud.
                                val name = remoteDoc.getString("name") ?: localEntity.name
                                val category = remoteDoc.getString("category") ?: localEntity.category
                                val quantity = remoteDoc.getLong("quantity")?.toInt() ?: localEntity.quantity
                                val purchasePrice = remoteDoc.getDouble("purchasePrice") ?: localEntity.purchasePrice
                                val sellingPrice = remoteDoc.getDouble("sellingPrice") ?: localEntity.sellingPrice
                                val lowStockThreshold = remoteDoc.getLong("lowStockThreshold")?.toInt() ?: localEntity.lowStockThreshold

                                val updatedEntity = localEntity.copy(
                                    name = name,
                                    category = category,
                                    quantity = quantity,
                                    purchasePrice = purchasePrice,
                                    sellingPrice = sellingPrice,
                                    lowStockThreshold = lowStockThreshold
                                )
                                productDao.insertProduct(updatedEntity)
                            } else {
                                // Exists only locally. Upload to Firestore.
                                val product = Product(
                                    id = localEntity.id,
                                    name = localEntity.name,
                                    category = localEntity.category,
                                    quantity = localEntity.quantity,
                                    purchasePrice = localEntity.purchasePrice,
                                    sellingPrice = localEntity.sellingPrice,
                                    lowStockThreshold = localEntity.lowStockThreshold
                                )
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("products")
                                    .document(product.id)
                                    .set(product)
                                    .await()
                            }
                        }

                        // 2. Process all remote products that don't exist locally, and save them to local DB
                        for (remoteDoc in remoteMap.values) {
                            if (!localMap.containsKey(remoteDoc.id)) {
                                val name = remoteDoc.getString("name") ?: continue
                                val category = remoteDoc.getString("category") ?: ""
                                val quantity = remoteDoc.getLong("quantity")?.toInt() ?: 0
                                val purchasePrice = remoteDoc.getDouble("purchasePrice") ?: 0.0
                                val sellingPrice = remoteDoc.getDouble("sellingPrice") ?: 0.0
                                val lowStockThreshold = remoteDoc.getLong("lowStockThreshold")?.toInt() ?: 5

                                val entity = ProductEntity(
                                    id = remoteDoc.id,
                                    name = name,
                                    category = category,
                                    quantity = quantity,
                                    purchasePrice = purchasePrice,
                                    sellingPrice = sellingPrice,
                                    lowStockThreshold = lowStockThreshold
                                )
                                productDao.insertProduct(entity)
                            }
                        }
                    }

                    SyncConflictResolution.MERGE_LOCAL_MAIN -> {
                        // 1. Process all local products
                        for (localEntity in localEntities) {
                            val remoteDoc = remoteMap[localEntity.id]
                            if (remoteDoc != null) {
                                // Conflict! Local is main. Update Cloud to match Local.
                                val product = Product(
                                    id = localEntity.id,
                                    name = localEntity.name,
                                    category = localEntity.category,
                                    quantity = localEntity.quantity,
                                    purchasePrice = localEntity.purchasePrice,
                                    sellingPrice = localEntity.sellingPrice,
                                    lowStockThreshold = localEntity.lowStockThreshold
                                )
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("products")
                                    .document(product.id)
                                    .set(product)
                                    .await()
                            } else {
                                // Exists only locally. Upload to Cloud.
                                val product = Product(
                                    id = localEntity.id,
                                    name = localEntity.name,
                                    category = localEntity.category,
                                    quantity = localEntity.quantity,
                                    purchasePrice = localEntity.purchasePrice,
                                    sellingPrice = localEntity.sellingPrice,
                                    lowStockThreshold = localEntity.lowStockThreshold
                                )
                                firestore.collection("users")
                                    .document(userId)
                                    .collection("products")
                                    .document(product.id)
                                    .set(product)
                                    .await()
                            }
                        }

                        // 2. Process all remote products that don't exist locally. Save to local.
                        for (remoteDoc in remoteMap.values) {
                            if (!localMap.containsKey(remoteDoc.id)) {
                                val name = remoteDoc.getString("name") ?: continue
                                val category = remoteDoc.getString("category") ?: ""
                                val quantity = remoteDoc.getLong("quantity")?.toInt() ?: 0
                                val purchasePrice = remoteDoc.getDouble("purchasePrice") ?: 0.0
                                val sellingPrice = remoteDoc.getDouble("sellingPrice") ?: 0.0
                                val lowStockThreshold = remoteDoc.getLong("lowStockThreshold")?.toInt() ?: 5

                                val entity = ProductEntity(
                                    id = remoteDoc.id,
                                    name = name,
                                    category = category,
                                    quantity = quantity,
                                    purchasePrice = purchasePrice,
                                    sellingPrice = sellingPrice,
                                    lowStockThreshold = lowStockThreshold
                                )
                                productDao.insertProduct(entity)
                            }
                        }
                    }

                    SyncConflictResolution.OVERWRITE_LOCAL_BY_CLOUD -> {
                        // Wipe Local completely, then insert all Cloud items
                        productDao.deleteAllProducts()
                        
                        for (remoteDoc in remoteMap.values) {
                            val name = remoteDoc.getString("name") ?: continue
                            val category = remoteDoc.getString("category") ?: ""
                            val quantity = remoteDoc.getLong("quantity")?.toInt() ?: 0
                            val purchasePrice = remoteDoc.getDouble("purchasePrice") ?: 0.0
                            val sellingPrice = remoteDoc.getDouble("sellingPrice") ?: 0.0
                            val lowStockThreshold = remoteDoc.getLong("lowStockThreshold")?.toInt() ?: 5

                            val entity = ProductEntity(
                                id = remoteDoc.id,
                                name = name,
                                category = category,
                                quantity = quantity,
                                purchasePrice = purchasePrice,
                                sellingPrice = sellingPrice,
                                lowStockThreshold = lowStockThreshold
                            )
                            productDao.insertProduct(entity)
                        }
                    }

                    SyncConflictResolution.OVERWRITE_CLOUD_BY_LOCAL -> {
                        // 1. Wipe remote collection in Firestore
                        for (remoteDoc in remoteMap.values) {
                            firestore.collection("users")
                                .document(userId)
                                .collection("products")
                                .document(remoteDoc.id)
                                .delete()
                                .await()
                        }

                        // 2. Upload all local items to Firestore
                        for (localEntity in localEntities) {
                            val product = Product(
                                id = localEntity.id,
                                name = localEntity.name,
                                category = localEntity.category,
                                quantity = localEntity.quantity,
                                purchasePrice = localEntity.purchasePrice,
                                sellingPrice = localEntity.sellingPrice,
                                lowStockThreshold = localEntity.lowStockThreshold
                            )
                            firestore.collection("users")
                                .document(userId)
                                .collection("products")
                                .document(product.id)
                                .set(product)
                                .await()
                        }
                    }
                }
                Timber.d("Sync conflict resolution successfully executed for: $resolution")
            } catch (e: Exception) {
                Timber.e(e, "resolveConflictAndSync failed")
                throw e
            }
        }
    }
}

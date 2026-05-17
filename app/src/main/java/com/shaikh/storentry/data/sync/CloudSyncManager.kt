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

    /**
     * Deletes a single product from Firestore.
     */
    suspend fun deleteProduct(productId: String) {
        if (!shouldAutoSync()) return
        val userId = getUserId() ?: return
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

    /**
     * Updates the quantity of a product in Firestore.
     */
    suspend fun updateStock(productId: String, newQuantity: Int) {
        if (!shouldAutoSync()) return
        val userId = getUserId() ?: return
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

    /**
     * Performs a two-way sync on User Sign-in:
     * 1. Uploads guest session local data to Firestore to prevent data loss.
     * 2. Pulls all user cloud data and merges it into the local Room database.
     */
    suspend fun syncOnSignIn() {
        val userId = getUserId() ?: return
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
        }
    }

    /**
     * Triggers a manual sync.
     */
    suspend fun manualSync() {
        syncOnSignIn()
    }
}

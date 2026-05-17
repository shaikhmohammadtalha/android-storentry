package com.shaikh.storentry.presentation.screens.add_product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import com.shaikh.storentry.utils.SubscriptionConfig
import com.shaikh.storentry.utils.UiState
import com.shaikh.storentry.util.analytics.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AddProductViewModel — Manages adding new products, validating fields,
 * and gating addition if the free inventory limit is reached.
 */
@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Gated save function. Checks subscription status and database size before permitting the save operation.
     */
    fun saveProduct(
        name: String,
        category: String,
        quantity: String,
        purchasePrice: String,
        sellingPrice: String,
        threshold: String
    ) {
        if (name.isBlank() || category.isBlank() || quantity.isBlank() || 
            purchasePrice.isBlank() || sellingPrice.isBlank() || threshold.isBlank()) {
            _uiState.value = UiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // Gating verification: Free limits
                val isPremium = subscriptionRepository.isPremium()
                val currentProductCount = repository.getProductCount().first()

                if (!isPremium && currentProductCount >= SubscriptionConfig.FREE_PRODUCT_LIMIT) {
                    val bundle = android.os.Bundle().apply {
                        putString(AnalyticsManager.Params.FEATURE_NAME, "add_product")
                        putString(AnalyticsManager.Params.SOURCE, "limit_reached")
                    }
                    analyticsManager.logEvent(AnalyticsManager.Events.FEATURES_GATED, bundle)
                    _uiState.value = UiState.Error("LIMIT_REACHED")
                    return@launch
                }

                val product = Product(
                    name = name,
                    category = category,
                    quantity = quantity.toIntOrNull() ?: 0,
                    purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                    lowStockThreshold = threshold.toIntOrNull() ?: 0
                )
                repository.addProduct(product)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to save product")
            }
        }
    }

    /**
     * Resets visual overlays or errors back to idle.
     */
    fun resetState() {
        _uiState.value = UiState.Idle
    }
}

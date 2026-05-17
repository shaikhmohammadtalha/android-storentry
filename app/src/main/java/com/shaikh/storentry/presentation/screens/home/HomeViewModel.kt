package com.shaikh.storentry.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.util.analytics.AnalyticsManager
import com.shaikh.storentry.util.crashlytics.CrashlyticsManager
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

data class HomeData(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalValue: Double = 0.0,
    val recentProducts: List<Product> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val analyticsManager: AnalyticsManager,
    private val crashlyticsManager: CrashlyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    val hasAlerts: StateFlow<Boolean> = repository.getLowStockCount()
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        analyticsManager.trackScreenView("home_screen")
        fetchHomeData()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            combine(
                repository.getProductCount(),
                repository.getLowStockCount(),
                repository.getTotalInventoryValue(),
                repository.getAllProducts()
            ) { count, lowStockCount, totalValue, allProducts ->
                HomeData(
                    totalProducts = count,
                    lowStockCount = lowStockCount,
                    totalValue = totalValue,
                    recentProducts = allProducts.take(5) // Just take top 5 for "recent"
                )
            }.catch { e ->
                Timber.e(e, "Error fetching home data")
                crashlyticsManager.recordException(e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }

    fun updateStock(product: Product, increment: Int) {
        viewModelScope.launch {
            try {
                val newQuantity = (product.quantity + increment).coerceAtLeast(0)
                repository.updateProduct(product.copy(quantity = newQuantity))
                
                // Analytics
                analyticsManager.logEvent(AnalyticsManager.Events.BUTTON_CLICKED, android.os.Bundle().apply {
                    putString(AnalyticsManager.Params.BUTTON_NAME, if (increment > 0) "stock_increment" else "stock_decrement")
                    putString(AnalyticsManager.Params.PRODUCT_ID, product.id)
                })
            } catch (e: Exception) {
                Timber.e(e, "Failed to update stock")
                crashlyticsManager.recordException(e)
            }
        }
    }
}

package com.shaikh.storentry.presentation.screens.low_stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Low Stock Alerts screen.
 */
@HiltViewModel
class LowStockAlertsViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    val hasAlerts: StateFlow<Boolean> = repository.getLowStockCount()
        .map { it > 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    init {
        fetchLowStockProducts()
    }

    fun fetchLowStockProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getLowStockProducts()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { products ->
                    if (products.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(products)
                    }
                }
        }
    }
}

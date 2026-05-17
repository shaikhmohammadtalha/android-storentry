package com.shaikh.storentry.presentation.screens.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    val hasAlerts: StateFlow<Boolean> = repository.getLowStockCount()
        .map { it > 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    init {
        observeProducts()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            combine(
                repository.getAllProducts(),
                _searchQuery
            ) { products: List<Product>, query: String ->
                if (query.isEmpty()) {
                    products
                } else {
                    products.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                    }
                }
            }.catch { e ->
                _uiState.value = UiState.Error(e.message ?: "An error occurred")
            }.collect { filteredProducts ->
                if (filteredProducts.isEmpty()) {
                    if (_searchQuery.value.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(emptyList())
                    }
                } else {
                    _uiState.value = UiState.Success(filteredProducts)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun fetchProducts() {
        // Redundant with observeProducts but kept for compatibility if called externally
        observeProducts()
    }
}

package com.shaikh.storentry.presentation.screens.edit_product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the EditProductScreen.
 * Handles fetching product details, updating the product, and managing category suggestions.
 */
@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Product>>(UiState.Idle)
    val uiState: StateFlow<UiState<Product>> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _saveStatus = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val saveStatus: StateFlow<UiState<Unit>> = _saveStatus.asStateFlow()

    init {
        fetchCategories()
    }

    /**
     * Fetches the product details for editing.
     */
    fun fetchProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val product = repository.getProductById(productId)
                if (product != null) {
                    _uiState.value = UiState.Success(product)
                } else {
                    _uiState.value = UiState.Error("Product not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to fetch product")
            }
        }
    }

    /**
     * Fetches unique categories from the repository.
     */
    private fun fetchCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onEach { _categories.value = it }
                .launchIn(viewModelScope)
        }
    }

    /**
     * Updates an existing product.
     */
    fun updateProduct(
        id: String,
        name: String,
        category: String,
        quantity: String,
        purchasePrice: String,
        sellingPrice: String,
        lowStockThreshold: String
    ) {
        if (name.isBlank() || category.isBlank() || quantity.isBlank()) {
            _saveStatus.value = UiState.Error("Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _saveStatus.value = UiState.Loading
            try {
                val updatedProduct = Product(
                    id = id,
                    name = name,
                    category = category,
                    quantity = quantity.toIntOrNull() ?: 0,
                    purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                    lowStockThreshold = lowStockThreshold.toIntOrNull() ?: 5
                )
                repository.updateProduct(updatedProduct)
                _saveStatus.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _saveStatus.value = UiState.Error(e.message ?: "Failed to update product")
            }
        }
    }

    private val _productDeleted = MutableSharedFlow<Unit>()
    val productDeleted = _productDeleted.asSharedFlow()

    /**
     * Deletes the product.
     */
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _saveStatus.value = UiState.Loading
            try {
                repository.deleteProduct(product)
                _saveStatus.value = UiState.Idle
                _productDeleted.emit(Unit)
            } catch (e: Exception) {
                _saveStatus.value = UiState.Error(e.message ?: "Failed to delete product")
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = UiState.Idle
    }
}

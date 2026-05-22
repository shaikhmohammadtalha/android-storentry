package com.shaikh.storentry.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.Product
import com.shaikh.storentry.domain.repository.ProductRepository
import com.shaikh.storentry.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class ReportsData(
    val totalProducts: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val inStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val lowStockCount: Int = 0,
    val topValuedProducts: List<Product> = emptyList(),
    val categoryDistribution: Map<String, Double> = emptyMap() // Category to Total Value
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ReportsData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ReportsData>> = _uiState.asStateFlow()

    init {
        fetchReportsData()
    }

    private fun fetchReportsData() {
        viewModelScope.launch {
            repository.getAllProducts()
                .map { allProducts ->
                    val count = allProducts.size
                    val totalValue = allProducts.sumOf { it.sellingPrice * it.quantity }
                    val inStock = allProducts.count { it.quantity > it.lowStockThreshold }
                    val outOfStock = allProducts.count { it.quantity == 0 }
                    val lowStock = allProducts.count { it.quantity in 1..it.lowStockThreshold }
                    
                    val topValued = allProducts
                        .sortedByDescending { it.sellingPrice * it.quantity }
                        .take(5)

                    val categoryDist = allProducts
                        .groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.sellingPrice * it.quantity } }
                        .toList()
                        .sortedByDescending { it.second }
                        .toMap()

                    ReportsData(
                        totalProducts = count,
                        totalInventoryValue = totalValue,
                        inStockCount = inStock,
                        outOfStockCount = outOfStock,
                        lowStockCount = lowStock,
                        topValuedProducts = topValued,
                        categoryDistribution = categoryDist
                    )
                }
                .flowOn(Dispatchers.Default)
                .collect { data ->
                    if (data.totalProducts == 0) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(data)
                    }
                }
        }
    }
}

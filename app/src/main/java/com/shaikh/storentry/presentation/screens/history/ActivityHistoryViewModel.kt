package com.shaikh.storentry.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.HistoryActionType
import com.shaikh.storentry.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.shaikh.storentry.domain.repository.ProductRepository
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState: StateFlow<ActivityHistoryUiState> = _uiState.asStateFlow()

    private val _undoEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val undoEvent = _undoEvent.asSharedFlow()

    fun undoDelete(record: com.shaikh.storentry.domain.model.HistoryRecord) {
        viewModelScope.launch {
            try {
                val jsonStr = record.metadata ?: return@launch
                val product = kotlinx.serialization.json.Json.decodeFromString<com.shaikh.storentry.domain.model.Product>(jsonStr)
                productRepository.addProduct(product)
                // Delete history record from the DB
                historyRepository.deleteHistoryById(record.id)
                _undoEvent.emit(product.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            historyRepository.getAllHistory()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { records ->
                    _uiState.update { 
                        it.copy(
                            records = records,
                            filteredRecords = filterRecords(records, it.selectedFilter),
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    fun onFilterSelected(filter: HistoryFilter) {
        _uiState.update { 
            it.copy(
                selectedFilter = filter,
                filteredRecords = filterRecords(it.records, filter)
            ) 
        }
    }

    private fun filterRecords(records: List<com.shaikh.storentry.domain.model.HistoryRecord>, filter: HistoryFilter): List<com.shaikh.storentry.domain.model.HistoryRecord> {
        if (filter == HistoryFilter.ALL) return records
        return records.filter { it.actionType.toFilter() == filter }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}

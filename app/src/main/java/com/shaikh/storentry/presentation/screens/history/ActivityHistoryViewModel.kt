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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

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
                    viewModelScope.launch(Dispatchers.Default) {
                        val filtered = filterRecords(records, _uiState.value.selectedFilter)
                        val grouped = groupRecordsByDate(filtered)
                        _uiState.update { 
                            it.copy(
                                records = records,
                                filteredRecords = filtered,
                                groupedRecords = grouped,
                                isLoading = false
                            ) 
                        }
                    }
                }
        }
    }

    fun onFilterSelected(filter: HistoryFilter) {
        viewModelScope.launch(Dispatchers.Default) {
            val filtered = filterRecords(_uiState.value.records, filter)
            val grouped = groupRecordsByDate(filtered)
            _uiState.update { 
                it.copy(
                    selectedFilter = filter,
                    filteredRecords = filtered,
                    groupedRecords = grouped
                ) 
            }
        }
    }

    private fun filterRecords(records: List<com.shaikh.storentry.domain.model.HistoryRecord>, filter: HistoryFilter): List<com.shaikh.storentry.domain.model.HistoryRecord> {
        if (filter == HistoryFilter.ALL) return records
        return records.filter { it.actionType.toFilter() == filter }
    }

    private fun groupRecordsByDate(records: List<com.shaikh.storentry.domain.model.HistoryRecord>): Map<String, List<com.shaikh.storentry.domain.model.HistoryRecord>> {
        val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val yesterday = today - 86400000
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        return records.groupBy { record ->
            when {
                record.timestamp >= today -> "Today"
                record.timestamp >= yesterday -> "Yesterday"
                else -> sdf.format(Date(record.timestamp))
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}

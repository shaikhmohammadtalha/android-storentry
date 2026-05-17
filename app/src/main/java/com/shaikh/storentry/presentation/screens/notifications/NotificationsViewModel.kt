package com.shaikh.storentry.presentation.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        observeReminders()
    }

    private fun observeReminders() {
        combine(
            reminderRepository.getUpcomingReminders(),
            reminderRepository.getAllReminders()
        ) { upcoming, all ->
            val history = all.filter { it.status != "SCHEDULED" }
            NotificationsUiState(
                upcoming = upcoming,
                history = history,
                isLoading = false
            )
        }.onEach {
            _uiState.value = it
        }.launchIn(viewModelScope)
    }

    fun cancelReminder(workId: String) {
        viewModelScope.launch {
            reminderRepository.cancelReminder(workId)
        }
    }
}

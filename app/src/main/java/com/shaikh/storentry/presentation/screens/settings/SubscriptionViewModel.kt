package com.shaikh.storentry.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.SubscriptionState
import com.shaikh.storentry.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SubscriptionViewModel — Shared ViewModel bound at the root AppNavGraph layer
 * to feed subscription entitlement states uniformly across all Compose screens.
 */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /**
     * Hot StateFlow exposing the user's active entitlement state.
     * Starts with a default cached state, optimistically fallbacks to DataStore, and fetches RevenueCat update.
     */
    val subscriptionState: StateFlow<SubscriptionState> =
        subscriptionRepository.observeSubscriptionState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SubscriptionState(false, emptyList(), null)
            )

    init {
        refresh()
    }

    /**
     * Triggers active background billing validation check.
     */
    fun refresh() {
        viewModelScope.launch {
            subscriptionRepository.refreshSubscriptionStatus()
        }
    }
}

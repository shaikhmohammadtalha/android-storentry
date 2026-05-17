package com.shaikh.storentry.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.model.SubscriptionStatus
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
     * Hot StateFlow exposing the user's active entitlement tier.
     * Starts with a Loading state, optimistically fallback to DataStore, and fetches RevenueCat update.
     */
    val subscriptionStatus: StateFlow<SubscriptionStatus> =
        subscriptionRepository.observeSubscriptionStatus()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SubscriptionStatus.Loading
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

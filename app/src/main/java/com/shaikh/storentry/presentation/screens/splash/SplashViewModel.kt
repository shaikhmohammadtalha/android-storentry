package com.shaikh.storentry.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the SplashScreen.
 * Checks if onboarding is completed to determine the next destination.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            // Get the first value from the flow
            val completed = preferenceRepository.isOnboardingCompleted().first()
            _onboardingCompleted.value = completed
        }
    }
}

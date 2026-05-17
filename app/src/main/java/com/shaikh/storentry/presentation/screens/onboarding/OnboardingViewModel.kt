package com.shaikh.storentry.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaikh.storentry.domain.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the OnboardingScreen.
 * Handles marking onboarding as completed.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    /**
     * Marks the onboarding as completed in preferences.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            preferenceRepository.setOnboardingCompleted(true)
        }
    }
}

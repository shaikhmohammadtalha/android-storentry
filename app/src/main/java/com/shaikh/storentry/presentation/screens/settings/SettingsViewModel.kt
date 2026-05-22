package com.shaikh.storentry.presentation.screens.settings

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.shaikh.storentry.R
import com.shaikh.storentry.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cloudSyncManager: com.shaikh.storentry.data.sync.CloudSyncManager,
    private val preferenceRepository: com.shaikh.storentry.domain.repository.PreferenceRepository,
    private val productRepository: com.shaikh.storentry.domain.repository.ProductRepository,
    private val subscriptionRepository: com.shaikh.storentry.domain.repository.SubscriptionRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val autoSyncEnabled = preferenceRepository.isAutoSyncEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hasAlerts: StateFlow<Boolean> = productRepository.getLowStockCount()
        .map { it > 0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _syncConflictState = MutableStateFlow<com.shaikh.storentry.data.sync.SyncConflictState>(com.shaikh.storentry.data.sync.SyncConflictState.Idle)
    val syncConflictState = _syncConflictState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Hashing a raw nonce for Google ID Option
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val signInResult = authRepository.signInWithGoogle(googleIdTokenCredential.idToken)
                    if (signInResult.isSuccess) {
                        val localCount = cloudSyncManager.getLocalProductCount()
                        val remoteCount = cloudSyncManager.getRemoteProductCount()
                        if (localCount > 0 && remoteCount > 0) {
                            _syncConflictState.value = com.shaikh.storentry.data.sync.SyncConflictState.Conflict(localCount, remoteCount)
                        } else {
                            cloudSyncManager.syncOnSignIn()
                        }
                    }
                } else {
                    Timber.e("Unexpected credential type: ${credential.type}")
                }

            } catch (e: GetCredentialCancellationException) {
                Timber.d("User cancelled sign in")
            } catch (e: Exception) {
                Timber.e(e, "Error during Google Sign In")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resolveConflict(resolution: com.shaikh.storentry.data.sync.SyncConflictResolution) {
        viewModelScope.launch {
            _syncConflictState.value = com.shaikh.storentry.data.sync.SyncConflictState.Resolving
            _isSyncing.value = true
            try {
                cloudSyncManager.resolveConflictAndSync(resolution)
            } catch (e: Exception) {
                Timber.e(e, "Conflict resolution failed")
            } finally {
                _syncConflictState.value = com.shaikh.storentry.data.sync.SyncConflictState.Idle
                _isSyncing.value = false
            }
        }
    }

    fun dismissConflictDialog() {
        _syncConflictState.value = com.shaikh.storentry.data.sync.SyncConflictState.Idle
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setAutoSyncEnabled(enabled)
            if (enabled) {
                triggerManualSync()
            }
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                cloudSyncManager.manualSync()
            } catch (e: Exception) {
                Timber.e(e, "Manual sync failed")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                cloudSyncManager.syncOnSignOut()
                authRepository.signOut()
            }
        }
    }

    val isDebugForcePremiumEnabled = if (com.shaikh.storentry.BuildConfig.DEBUG) {
        preferenceRepository.isDebugForcePremium()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    } else {
        MutableStateFlow(false)
    }

    fun setDebugForcePremium(enabled: Boolean) {
        if (com.shaikh.storentry.BuildConfig.DEBUG) {
            viewModelScope.launch {
                preferenceRepository.setDebugForcePremium(enabled)
                subscriptionRepository.refreshSubscriptionStatus()
            }
        }
    }
}

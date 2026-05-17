package com.shaikh.storentry.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.shaikh.storentry.domain.repository.PreferenceRepository
import com.shaikh.storentry.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PreferenceRepository using DataStore.
 */
@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceRepository {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey(Constants.PREF_ONBOARDING_COMPLETED)
        val IS_PREMIUM = booleanPreferencesKey(Constants.PREF_IS_PREMIUM)
        val PREMIUM_EXPIRY = longPreferencesKey(Constants.PREF_PREMIUM_EXPIRY)
        val AUTO_SYNC = booleanPreferencesKey(Constants.PREF_AUTO_SYNC)
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
            }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override fun isPremium(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.IS_PREMIUM] ?: false
            }
    }

    override suspend fun setPremium(isPremium: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREMIUM] = isPremium
        }
    }

    override fun getPremiumExpiry(): Flow<Long> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.PREMIUM_EXPIRY] ?: 0L
            }
    }

    override suspend fun setPremiumExpiry(expiry: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREMIUM_EXPIRY] = expiry
        }
    }

    override fun isAutoSyncEnabled(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PreferencesKeys.AUTO_SYNC] ?: false
            }
    }

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SYNC] = enabled
        }
    }
}

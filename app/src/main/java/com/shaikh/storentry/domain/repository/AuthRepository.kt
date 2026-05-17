package com.shaikh.storentry.domain.repository

import com.shaikh.storentry.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    /**
     * Flow of the currently authenticated user. Null if not logged in.
     */
    val currentUser: StateFlow<User?>

    /**
     * Authenticates with Google using the provided ID token from Credential Manager.
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Signs the user out.
     */
    suspend fun signOut()
}

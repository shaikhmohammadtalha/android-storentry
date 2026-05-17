package com.shaikh.storentry.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shaikh.storentry.domain.model.User
import com.shaikh.storentry.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _currentUser = MutableStateFlow(auth.currentUser?.toDomainUser())
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser?.toDomainUser()
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user?.toDomainUser() 
                ?: return Result.failure(Exception("Failed to map Firebase user"))
            
            Timber.d("Successfully signed in with Google: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Google Sign-In failed")
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toDomainUser(): User {
        return User(
            id = uid,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl?.toString()
        )
    }
}

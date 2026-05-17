package com.shaikh.storentry.di

import com.google.firebase.auth.FirebaseAuth
import com.shaikh.storentry.data.repository.AuthRepositoryImpl
import com.shaikh.storentry.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore {
        return com.google.firebase.firestore.FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }
}

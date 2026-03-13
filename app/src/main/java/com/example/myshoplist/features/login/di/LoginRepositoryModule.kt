package com.example.myshoplist.features.login.di

import com.example.myshoplist.features.login.data.repository.AuthRepositoryImpl
import com.example.myshoplist.features.login.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class LoginRepositoryModule {
    @Binds
    abstract fun bindLoginRepository(
        loginRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
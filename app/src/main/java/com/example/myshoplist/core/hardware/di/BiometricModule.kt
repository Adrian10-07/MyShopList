package com.example.myshoplist.core.hardware.di

import android.content.Context
import com.example.myshoplist.core.hardware.data.BiometricManagerImpl
import com.example.myshoplist.core.hardware.domain.BiometricManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindBiometricManager(
        biometricManagerImpl: BiometricManagerImpl
    ): BiometricManager
}
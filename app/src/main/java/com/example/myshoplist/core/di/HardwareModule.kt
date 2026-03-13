package com.example.myshoplist.core.di

import com.example.myshoplist.core.hardware.location.FusedLocationClientImpl
import com.example.myshoplist.core.hardware.location.LocationClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        fusedLocationClientImpl: FusedLocationClientImpl
    ): LocationClient
}
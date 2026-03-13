package com.example.myshoplist.features.register.di

import com.example.myshoplist.core.di.MyShopListRetrofit
import com.example.myshoplist.features.register.data.datasource.remote.api.RegisterApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RegisterNetworkModule {

    @Provides
    @Singleton
    fun provideRegisterApiService(@MyShopListRetrofit retrofit: Retrofit): RegisterApi {
        return retrofit.create(RegisterApi::class.java)
    }
}
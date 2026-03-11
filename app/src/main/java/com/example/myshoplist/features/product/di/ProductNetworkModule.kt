package com.example.myshoplist.features.product.di

import com.example.myshoplist.core.di.MyShopListRetrofit
import com.example.myshoplist.features.product.data.datasource.remote.api.ProductApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductNetworkModule {

    @Provides
    @Singleton
    fun provideProductApiService(@MyShopListRetrofit retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }
}
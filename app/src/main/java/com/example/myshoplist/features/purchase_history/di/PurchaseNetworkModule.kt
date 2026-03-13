package com.example.myshoplist.features.purchase_history.di

import com.example.myshoplist.core.di.MyShopListRetrofit
import com.example.myshoplist.features.purchase_history.data.datasource.remote.api.PurchaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PurchaseNetworkModule {

    @Provides
    @Singleton
    fun providePurchaseApi(@MyShopListRetrofit retrofit: Retrofit): PurchaseApi {
        return retrofit.create(PurchaseApi::class.java)
    }
}
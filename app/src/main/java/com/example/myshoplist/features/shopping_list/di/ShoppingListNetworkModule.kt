package com.example.myshoplist.features.shopping_list.di

import com.example.myshoplist.core.di.MyShopListRetrofit
import com.example.myshoplist.features.shopping_list.data.remote.api.ShoppingListApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShoppingListNetworkModule {
    @Provides
    @Singleton
    fun provideShoppingListApiService(@MyShopListRetrofit retrofit: Retrofit): ShoppingListApi {
        return retrofit.create(ShoppingListApi::class.java)
    }
}
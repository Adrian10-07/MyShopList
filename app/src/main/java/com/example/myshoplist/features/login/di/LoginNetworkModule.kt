package com.example.myshoplist.features.login.di

import com.example.myshoplist.core.di.MyShopListRetrofit
import com.example.myshoplist.features.login.data.datasource.remote.api.LoginApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginNetworkModule {
    @Provides
    @Singleton
    fun provideLoginApiService(@MyShopListRetrofit retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }
}
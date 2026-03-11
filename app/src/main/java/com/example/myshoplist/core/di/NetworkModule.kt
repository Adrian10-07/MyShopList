package com.example.myshoplist.core.di

import com.example.myshoplist.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @AuthClient
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .build()
    }
    @Provides
    @Singleton
    @AuthClient
    fun provideRetrofit(
        @AuthClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
/*
    @Provides
    @Singleton
    fun provideAuthApiService(@AuthClient retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    */
}
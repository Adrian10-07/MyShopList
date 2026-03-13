package com.example.myshoplist.core.di

import com.example.myshoplist.BuildConfig
import com.example.myshoplist.core.network.AuthInterceptor
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

    // 1. Le enseñamos a Hilt cómo crear tu interceptor
    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    // 2. Inyectamos el interceptor en el OkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // <- Aquí recuperamos la funcionalidad
            .build()
    }

    // 3. El Retrofit ya recibe el OkHttpClient configurado
    @Provides
    @Singleton
    @MyShopListRetrofit
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
/*
    @Provides
    @Singleton
    fun provideAuthApiService(@AuthClient retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    */

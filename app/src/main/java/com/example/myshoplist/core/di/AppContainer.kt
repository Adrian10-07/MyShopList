package com.example.myshoplist.core.di

import android.content.Context
import com.example.myshoplist.BuildConfig
import com.example.myshoplist.core.network.AuthApiService
import com.example.myshoplist.core.network.AuthInterceptor
import com.example.myshoplist.features.add_product.data.repository.AddProductRepositoryImpl
import com.example.myshoplist.features.add_product.domain.repository.AddProductRepository
import com.example.myshoplist.features.login.data.repository.AuthRepositoryImpl
import com.example.myshoplist.features.login.domain.repository.AuthRepository
import com.example.myshoplist.features.register.data.repository.RegisterRepositoryImpl
import com.example.myshoplist.features.register.domain.repository.RegisterRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authRetrofit = createRetrofit(BuildConfig.BASE_URL, okHttpClient)

    val authApi: AuthApiService by lazy {
        authRetrofit.create(AuthApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApi)
    }
    val registerRepository: RegisterRepository by lazy {
        RegisterRepositoryImpl(authApi)
    }

    val addProductRepository: AddProductRepository by lazy {
        AddProductRepositoryImpl(authApi)
    }
}
package com.example.myshoplist.features.purchase_history.data.di

import com.example.myshoplist.features.purchase_history.data.datasource.remote.api.PurchaseApi
import com.example.myshoplist.features.purchase_history.data.repositories.PurchaseRepositoryImpl
import com.example.myshoplist.features.purchase_history.domain.repositories.PurchaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PurchaseModule {

    @Provides
    @Singleton
    fun providePurchaseApi(retrofit: Retrofit): PurchaseApi {
        // Asume que ya tienes un módulo general de Red que provee 'Retrofit'
        // con tu interceptor para el Bearer Token (requerido por tu authMiddleware)
        return retrofit.create(PurchaseApi::class.java)
    }

    @Provides
    @Singleton
    fun providePurchaseRepository(
        api: PurchaseApi
    ): PurchaseRepository {
        return PurchaseRepositoryImpl(api)
    }
}
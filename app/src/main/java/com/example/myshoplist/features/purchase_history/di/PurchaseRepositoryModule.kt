package com.example.myshoplist.features.purchase_history.di

import com.example.myshoplist.features.purchase_history.data.repositories.PurchaseRepositoryImpl
import com.example.myshoplist.features.purchase_history.domain.repositories.PurchaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PurchaseRepositoryModule {

    @Binds
    abstract fun bindPurchaseRepository(
        purchaseRepositoryImpl: PurchaseRepositoryImpl
    ): PurchaseRepository
}
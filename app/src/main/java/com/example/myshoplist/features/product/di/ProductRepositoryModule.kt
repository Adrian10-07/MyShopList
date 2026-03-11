package com.example.myshoplist.features.product.di

import com.example.myshoplist.features.product.data.repository.AddProductRepositoryImpl
import com.example.myshoplist.features.product.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductRepositoryModule {
    @Binds
    abstract fun bindProductRepository(
        productRepositoryImpl: AddProductRepositoryImpl
    ): ProductRepository

}
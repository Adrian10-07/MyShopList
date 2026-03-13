package com.example.myshoplist.features.shopping_list.di

import com.example.myshoplist.features.shopping_list.data.repository.ShoppingListRepositoryImpl
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ShoppingListRepositoryModule {
    @Binds
    abstract fun bindShoppingListRepository(
        shoppingListRepositoryImpl: ShoppingListRepositoryImpl
    ): ShoppingListRepository
}
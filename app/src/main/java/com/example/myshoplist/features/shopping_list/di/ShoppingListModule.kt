package com.example.myshoplist.features.shopping_list.di

import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModelFactory

class ShoppingListModule(
    private val appContainer: AppContainer
) {
    private fun provideShoppingListUseCase(): ShoppingListUseCase {
        return ShoppingListUseCase(appContainer.shoppingListRepository)
    }

    fun provideShoppingListViewModelFactory(): ShoppingListViewModelFactory {
        return ShoppingListViewModelFactory(shoppingListUseCase = provideShoppingListUseCase())
    }
}
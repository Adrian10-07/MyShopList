package com.example.myshoplist.features.shopping_list.di

import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.features.shopping_list.domain.use_case.DeleteProductUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModelFactory

class ShoppingListModule(
    private val appContainer: AppContainer
) {

    private fun provideGetShoppingListUseCase(): ShoppingListUseCase {
        return ShoppingListUseCase(appContainer.shoppingListRepository)
    }

    private fun provideDeleteProductUseCase(): DeleteProductUseCase {
        return DeleteProductUseCase(appContainer.shoppingListRepository)
    }
    private fun provideUpdateProductUseCase(): UpdateProductUseCase {
        return UpdateProductUseCase(appContainer.shoppingListRepository)
    }
    fun provideShoppingListViewModelFactory(): ShoppingListViewModelFactory {
        return ShoppingListViewModelFactory(
            getShoppingListUseCase = provideGetShoppingListUseCase(),
            deleteProductUseCase = provideDeleteProductUseCase(),
            updateProductUseCase = provideUpdateProductUseCase()
        )
    }
}

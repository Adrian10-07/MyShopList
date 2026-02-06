package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.DeleteProductUseCase
import com.example.myshoplist.features.shopping_list.domain.use_case.UpdateProductUseCase


@Suppress("UNCHECKED_CAST")
class ShoppingListViewModelFactory(
    private val getShoppingListUseCase: ShoppingListUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            return ShoppingListViewModel(
                getShoppingListUseCase,
                deleteProductUseCase,
                updateProductUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
    }
}

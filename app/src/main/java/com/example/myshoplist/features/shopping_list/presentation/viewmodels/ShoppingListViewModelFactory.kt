package com.example.myshoplist.features.shopping_list.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myshoplist.features.shopping_list.domain.use_case.ShoppingListUseCase

@Suppress("UNCHECKED_LIST")
class ShoppingListViewModelFactory(
    private val shoppingListUseCase: ShoppingListUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java
        )) {
            return ShoppingListViewModel(shoppingListUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class: ${modelClass.name}")
    }
}
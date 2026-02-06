package com.example.myshoplist.features.shopping_list.presentation.screens

import com.example.myshoplist.features.add_product.domain.entities.Product

sealed interface ShoppingListUiState {
    object Loading: ShoppingListUiState
    object Empty: ShoppingListUiState
    data class Success(val items: List<Product>) : ShoppingListUiState
    data class Error(val message: String): ShoppingListUiState
}
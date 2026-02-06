package com.example.myshoplist.features.add_product.presentation.screens

import com.example.myshoplist.features.add_product.domain.entities.Product

sealed interface AddProductUiState {
    object Idle : AddProductUiState
    object Loading : AddProductUiState
    data class Success(val product: Product) : AddProductUiState
    data class Error(val message: String) : AddProductUiState
}
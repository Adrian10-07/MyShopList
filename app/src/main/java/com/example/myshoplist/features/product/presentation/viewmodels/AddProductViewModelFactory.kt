package com.example.myshoplist.features.product.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myshoplist.features.product.domain.use_case.AddProductUseCase

class AddProductViewModelFactory(
    private val addProductUseCase: AddProductUseCase
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProductViewModel::class.java)) {
            return AddProductViewModel(addProductUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}
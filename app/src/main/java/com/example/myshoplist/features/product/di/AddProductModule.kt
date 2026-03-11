package com.example.myshoplist.features.product.di

import com.example.myshoplist.features.product.domain.use_case.AddProductUseCase
import com.example.myshoplist.features.product.presentation.viewmodels.AddProductViewModelFactory

class AddProductModule(
    private val appContainer: AppContainer
) {
    private fun provideAddProductUseCase(): AddProductUseCase {
        return AddProductUseCase(appContainer.addProductRepository)
    }

    fun provideAddProductViewModelFactory(): AddProductViewModelFactory
    {
        return AddProductViewModelFactory(addProductUseCase = provideAddProductUseCase())
    }
}
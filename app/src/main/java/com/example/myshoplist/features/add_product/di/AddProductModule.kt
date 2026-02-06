package com.example.myshoplist.features.add_product.di

import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.features.add_product.domain.use_case.AddProductUseCase
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModelFactory

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
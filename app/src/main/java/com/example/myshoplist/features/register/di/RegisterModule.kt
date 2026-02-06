package com.example.myshoplist.features.register.di

import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.features.register.domain.use_case.RegisterUseCase
import com.example.myshoplist.features.register.presentation.viewmodels.RegisterViewModelFactory

class RegisterModule(
    private val appContainer: AppContainer
) {

    private fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(appContainer.registerRepository)
    }

    fun provideRegisterViewModelFactory(): RegisterViewModelFactory {
        return RegisterViewModelFactory(
            registerUseCase = provideRegisterUseCase()
        )
    }
}
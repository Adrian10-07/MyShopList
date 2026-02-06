package com.example.myshoplist.features.login.di

import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.features.login.domain.use_case.LoginUseCase
import com.example.myshoplist.features.login.presentation.viewmodels.LoginViewModelFactory


class LoginModule(
    private val appContainer: AppContainer
) {

    private fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(appContainer.authRepository)
    }

    fun provideLoginViewModelFactory(): LoginViewModelFactory {
        return LoginViewModelFactory(
            loginUseCase = provideLoginUseCase()
        )
    }
}
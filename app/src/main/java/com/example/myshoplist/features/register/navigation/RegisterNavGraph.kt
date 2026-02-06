package com.example.myshoplist.features.register.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.Login
import com.example.myshoplist.core.navigation.Register
import com.example.myshoplist.features.register.di.RegisterModule
import com.example.myshoplist.features.register.presentation.screens.RegisterScreen
import com.example.myshoplist.features.register.presentation.viewmodels.RegisterViewModel

class RegisterNavGraph(
    private val registerModule: RegisterModule
) : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<Register> {
            val viewModel: RegisterViewModel = viewModel(
                factory = registerModule.provideRegisterViewModelFactory()
            )

            RegisterScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                    navController.navigate(Login)
                },
                onLoginClick = {
                    navController.navigate(Login)
                }
            )
        }
    }
}
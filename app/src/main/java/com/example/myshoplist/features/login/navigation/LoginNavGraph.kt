package com.example.myshoplist.features.login.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.Home
import com.example.myshoplist.core.navigation.Login
import com.example.myshoplist.features.login.di.LoginModule
import com.example.myshoplist.features.login.presentation.screens.LoginScreen
import com.example.myshoplist.features.login.presentation.viewmodel.LoginViewModel

class LoginNavGraph(
    private val loginModule: LoginModule
) : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {

        navGraphBuilder.composable<Login> {

            val viewModel: LoginViewModel = viewModel(
                factory = loginModule.provideLoginViewModelFactory()
            )

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Login) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

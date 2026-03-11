package com.example.myshoplist.features.login.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.ShopList
import com.example.myshoplist.core.navigation.Login
import com.example.myshoplist.core.navigation.Register
import com.example.myshoplist.features.login.presentation.screens.LoginScreen
import com.example.myshoplist.features.login.presentation.viewmodel.LoginViewModel

class LoginNavGraph() : FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {

        navGraphBuilder.composable<Login> {

            val viewModel: LoginViewModel = viewModel()

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(ShopList) {
                        popUpTo(ShopList) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Register)
                }
            )
        }
    }
}

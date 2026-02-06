package com.example.myshoplist.features.add_product.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.Home
import com.example.myshoplist.features.add_product.di.AddProductModule
import com.example.myshoplist.features.add_product.presentation.screens.AddProductScreen
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModel

class AddProductNavGraph(
    private val productModule: AddProductModule
): FeatureNavGraph {
    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<Home> { 
            val viewModel: AddProductViewModel = viewModel(
                factory = productModule.provideAddProductViewModelFactory()
            )

            AddProductScreen(
                viewModel = viewModel,
                onNavigateBack = {
                },
                onProductAdded = {
                }
            )
        }
    }
}
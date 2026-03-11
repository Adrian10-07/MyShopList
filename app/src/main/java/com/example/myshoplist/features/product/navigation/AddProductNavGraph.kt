package com.example.myshoplist.features.product.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.AddProduct
import com.example.myshoplist.core.navigation.ShopList
import com.example.myshoplist.features.product.di.AddProductModule
import com.example.myshoplist.features.product.presentation.screens.AddProductScreen
import com.example.myshoplist.features.product.presentation.viewmodels.AddProductViewModel

class AddProductNavGraph(
    private val productModule: AddProductModule
): FeatureNavGraph {
    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<AddProduct> {
            val viewModel: AddProductViewModel = viewModel(
                factory = productModule.provideAddProductViewModelFactory()
            )

            AddProductScreen(
                viewModel = viewModel,
                onProductAdded = {
                    navController.navigate(ShopList)
                }
            )
        }
    }
}
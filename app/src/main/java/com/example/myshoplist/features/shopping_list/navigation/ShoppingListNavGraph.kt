package com.example.myshoplist.features.shopping_list.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.features.shopping_list.di.ShoppingListModule
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListScreen
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModel
import com.example.myshoplist.core.navigation.ShopList

class ShoppingListNavGraph(
    private val shoppingListModule: ShoppingListModule
): FeatureNavGraph {

    override fun registerGraph(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable<ShopList> {
            val viewModel: ShoppingListViewModel = viewModel(
                factory = shoppingListModule.provideShoppingListViewModelFactory()
            )

            ShoppingListScreen(
                shoppingListViewModel = viewModel,
                userName = "Diego",
                onNavigateToHistory = {
                },
                onNavigateToPurchases = {
                },
                onLogout = {
                    //navController.popBackStack()
                }
            )
        }
    }
}
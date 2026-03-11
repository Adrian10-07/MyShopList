package com.example.myshoplist.features.shopping_list.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListScreen
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModel
import com.example.myshoplist.features.product.presentation.viewmodels.AddProductViewModel
import com.example.myshoplist.core.navigation.ShopList

class ShoppingListNavGraph(): FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {

        navGraphBuilder.composable<ShopList> {

            val shoppingListViewModel: ShoppingListViewModel = hiltViewModel()

            val addProductViewModel: AddProductViewModel = hiltViewModel()

            ShoppingListScreen(
                shoppingListViewModel = shoppingListViewModel,
                addProductViewModel = addProductViewModel,
                userName = "Diego",
                onNavigateToHistory = {},
                onNavigateToPurchases = {},
                onLogout = {
                    navController.popBackStack()
                }
            )
        }
    }
}

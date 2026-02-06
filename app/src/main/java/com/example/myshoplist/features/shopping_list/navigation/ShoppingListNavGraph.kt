package com.example.myshoplist.features.shopping_list.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.features.shopping_list.di.ShoppingListModule
import com.example.myshoplist.features.add_product.di.AddProductModule
import com.example.myshoplist.features.shopping_list.presentation.screens.ShoppingListScreen
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.ShoppingListViewModel
import com.example.myshoplist.features.add_product.presentation.viewmodels.AddProductViewModel
import com.example.myshoplist.core.navigation.ShopList

class ShoppingListNavGraph(
    private val shoppingListModule: ShoppingListModule,
    private val addProductModule: AddProductModule
): FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {

        navGraphBuilder.composable<ShopList> {

            val shoppingListViewModel: ShoppingListViewModel = viewModel(
                factory = shoppingListModule.provideShoppingListViewModelFactory()
            )

            val addProductViewModel: AddProductViewModel = viewModel(
                factory = addProductModule.provideAddProductViewModelFactory()
            )

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

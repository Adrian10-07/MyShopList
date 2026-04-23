package com.example.myshoplist.features.shopping_list.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.SharedList
import com.example.myshoplist.features.shopping_list.presentation.screens.SharedListScreen
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.SharedListViewModel

class SharedListNavGraph : FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<SharedList> { backStackEntry ->
            val route: SharedList = backStackEntry.toRoute()
            val viewModel: SharedListViewModel = hiltViewModel()

            SharedListScreen(
                viewModel = viewModel,
                listId = route.listId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
package com.example.myshoplist.features.purchase_history.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.PurchaseHistory
import com.tuspaquetes.features.purchase_history.presentation.screen.PurchaseHistoryScreen
import com.tuspaquetes.features.purchase_history.presentation.viewmodels.PurchaseHistoryViewModel

class PurchaseHistoryNavGraph(): FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<PurchaseHistory> {

            val purchaseHistoryViewModel: PurchaseHistoryViewModel = hiltViewModel()

            PurchaseHistoryScreen(
                viewModel = purchaseHistoryViewModel,
                onNavigateToBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
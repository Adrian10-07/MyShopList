package com.example.myshoplist.features.shopping_list.navigation

import android.provider.Settings
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.SharedList
import com.example.myshoplist.features.shopping_list.presentation.screens.SharedListScreen
import com.example.myshoplist.features.shopping_list.presentation.viewmodels.SharedListViewModel
import androidx.compose.ui.platform.LocalContext

class SharedListNavGraph : FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<SharedList> { backStackEntry ->
            val route: SharedList = backStackEntry.toRoute()
            val viewModel: SharedListViewModel = hiltViewModel()
            val context = LocalContext.current

            // Si el listId que viene del nav es el placeholder, genera uno
            // único por dispositivo usando el Android ID.
            // Así cada usuario tiene su propio documento en Firestore y
            // solo comparte su lista cuando el otro pega el link.
            val listId = if (route.listId == "mi-lista-principal") {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            } else {
                route.listId
            }

            SharedListScreen(
                viewModel = viewModel,
                listId = listId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
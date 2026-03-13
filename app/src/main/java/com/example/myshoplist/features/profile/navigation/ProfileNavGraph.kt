package com.example.myshoplist.features.profile.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.myshoplist.core.navigation.FeatureNavGraph
import com.example.myshoplist.core.navigation.Profile
import com.example.myshoplist.features.profile.presentation.screen.ProfileScreen
import com.example.myshoplist.features.profile.presentation.viewmodels.ProfileViewModel

class ProfileNavGraph(): FeatureNavGraph {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<Profile> {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateToBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
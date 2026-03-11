package com.example.myshoplist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myshoplist.core.navigation.NavigationWrapper
import com.example.myshoplist.core.ui.theme.MyShopListTheme
import com.example.myshoplist.features.product.navigation.AddProductNavGraph
import com.example.myshoplist.features.login.navigation.LoginNavGraph
import com.example.myshoplist.features.register.navigation.RegisterNavGraph
import com.example.myshoplist.features.shopping_list.di.ShoppingListModule
import com.example.myshoplist.features.shopping_list.navigation.ShoppingListNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navGraphs = listOf(
            LoginNavGraph(),
            RegisterNavGraph(),
            AddProductNavGraph(),

        )

        setContent {
            MyShopListTheme {
                NavigationWrapper(
                    navGraphs = navGraphs
                )
            }
        }
    }
}
package com.example.myshoplist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.core.navigation.NavigationWrapper
import com.example.myshoplist.core.ui.theme.MyShopListTheme
import com.example.myshoplist.features.product.di.AddProductModule
import com.example.myshoplist.features.product.navigation.AddProductNavGraph
import com.example.myshoplist.features.login.di.LoginModule
import com.example.myshoplist.features.register.di.RegisterModule
import com.example.myshoplist.features.login.navigation.LoginNavGraph
import com.example.myshoplist.features.register.navigation.RegisterNavGraph
import com.example.myshoplist.features.shopping_list.di.ShoppingListModule
import com.example.myshoplist.features.shopping_list.navigation.ShoppingListNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this)

        val loginModule = LoginModule(appContainer)
        val registerModule = RegisterModule(appContainer)
        val addProductModule = AddProductModule(appContainer)
        val shoppingListModule = ShoppingListModule(appContainer)


        val navGraphs = listOf(
            LoginNavGraph(loginModule),
            RegisterNavGraph(registerModule),
            AddProductNavGraph(addProductModule),
            ShoppingListNavGraph(shoppingListModule, addProductModule)

        )

        enableEdgeToEdge()

        setContent {
            MyShopListTheme {
                NavigationWrapper(navGraphs)
            }
        }
    }
}
package com.example.myshoplist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myshoplist.core.di.AppContainer
import com.example.myshoplist.core.navigation.NavigationWrapper
import com.example.myshoplist.core.ui.theme.MyShopListTheme
import com.example.myshoplist.features.add_product.di.AddProductModule
import com.example.myshoplist.features.add_product.navigation.AddProductNavGraph
import com.example.myshoplist.features.login.di.LoginModule
import com.example.myshoplist.features.register.di.RegisterModule
import com.example.myshoplist.features.login.navigation.LoginNavGraph
import com.example.myshoplist.features.register.navigation.RegisterNavGraph

class MainActivity : ComponentActivity() {
    lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(this)

        val loginModule = LoginModule(appContainer)
        val registerModule = RegisterModule(appContainer)
        val addProductModule = AddProductModule(appContainer)


        val navGraphs = listOf(
            LoginNavGraph(loginModule),
            RegisterNavGraph(registerModule),
            AddProductNavGraph(addProductModule)

        )

        Log.d("Main", "AppContainer y Módulos cargados correctamente")

        enableEdgeToEdge()

        setContent {
            MyShopListTheme {
                NavigationWrapper(navGraphs)
            }
        }
    }
}
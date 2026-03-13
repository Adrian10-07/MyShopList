package com.example.myshoplist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.myshoplist.core.navigation.NavigationWrapper
import com.example.myshoplist.core.ui.theme.MyShopListTheme
import com.example.myshoplist.features.product.navigation.AddProductNavGraph
import com.example.myshoplist.features.login.navigation.LoginNavGraph
import com.example.myshoplist.features.profile.navigation.ProfileNavGraph
import com.example.myshoplist.features.purchase_history.navigation.PurchaseHistoryNavGraph
import com.example.myshoplist.features.register.navigation.RegisterNavGraph
import com.example.myshoplist.features.shopping_list.navigation.ShoppingListNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navGraphs = listOf(
            LoginNavGraph(),
            RegisterNavGraph(),
            AddProductNavGraph(),
            ShoppingListNavGraph(),
            PurchaseHistoryNavGraph(),
            ProfileNavGraph()
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
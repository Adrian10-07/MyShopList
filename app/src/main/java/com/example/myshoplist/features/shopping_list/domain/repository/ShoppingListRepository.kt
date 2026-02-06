package com.example.myshoplist.features.shopping_list.domain.repository

import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListDto

interface ShoppingListRepository {
    suspend fun getProducts(): Result<List<ShoppingListDto>>
}
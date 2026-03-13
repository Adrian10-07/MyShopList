package com.example.myshoplist.features.shopping_list.domain.repository

import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest

interface ShoppingListRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun deleteProduct(id: String): Result<Unit>
    suspend fun updateProduct(id: String): Result<Unit>
    suspend fun createPurchase(request: CreatePurchaseRequest): Result<String>
}
package com.example.myshoplist.features.shopping_list.domain.repository

import com.example.myshoplist.features.product.domain.entities.Product
import kotlinx.coroutines.flow.Flow

interface RemoteListDataSource {

    suspend fun syncListToCloud(listId: String, data: Map<String, Any>): Result<Boolean>

    suspend fun syncProductsToCloud(listId: String, products: List<Product>): Result<Boolean>

    suspend fun toggleProductInCloud(
        listId: String,
        productId: String,
        isPurchased: Boolean
    ): Result<Boolean>
    suspend fun deleteProductInCloud(listId: String, productId: String): Result<Boolean>

    suspend fun finalizeSharedPurchase(listId: String, purchasedProductIds: List<String>): Result<Boolean>
    fun observeSharedList(listId: String): Flow<Map<String, Any>>

    suspend fun saveFcmToken(userId: String, token: String): Result<Boolean>

    suspend fun generateShareLink(listId: String): Result<String>
}
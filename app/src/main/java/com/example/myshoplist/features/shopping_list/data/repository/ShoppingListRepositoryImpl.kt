package com.example.myshoplist.features.shopping_list.data.repository

import com.example.myshoplist.core.network.AuthApiService
import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.add_product.domain.repository.ProductRepository
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListDto
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository

class ShoppingListRepositoryImpl(
    private val apiService: AuthApiService
) : ShoppingListRepository {

    override suspend fun getProducts(): Result<List<ShoppingListDto>> {
        return try {
            val response = apiService.getProducts()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(
                        Exception("Respuesta inválida del servidor")
                    )
                }
            } else {
                Result.failure(
                    Exception("Error HTTP ${response.code()}")
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

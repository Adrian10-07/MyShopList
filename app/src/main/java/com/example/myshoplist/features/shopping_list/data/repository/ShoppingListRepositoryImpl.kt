package com.example.myshoplist.features.shopping_list.data.repository

import com.example.myshoplist.core.network.AuthApiService
import com.example.myshoplist.features.product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.product.domain.entities.Product
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
    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(id)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar producto"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun updateProduct(id: String): Result<Product> {
        return try {
            val response = apiService.updateProduct(id)
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                Result.success(body.data!!.toDomain())
            } else {
                Result.failure(Exception("No se pudo obtener el producto actualizado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.myshoplist.features.product.data.repository

import com.example.myshoplist.features.product.data.datasource.remote.api.ProductApi
import com.example.myshoplist.features.product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.product.domain.repository.ProductRepository
import java.io.IOException
import javax.inject.Inject

class AddProductRepositoryImpl @Inject constructor(
    private val apiService: ProductApi) : ProductRepository {

    override suspend fun addProduct(
        name: String,
        category: String,
        estimatedPrice: Double
    ): Result<Product> {
        return try {
            val request = AddProductRequest(
                name = name,
                category = category,
                estimatedPrice = estimatedPrice
            )

            val response = apiService.addProduct(request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.success && body.data != null) {
                    val product = body.data.toDomain()
                    Result.success(product)
                } else {
                    Result.failure(Exception(body?.message ?: "Error desconocido en el servidor"))
                }
            } else {
                Result.failure(Exception("Error en la petición: ${response.code()} ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet. Verifica tu red."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
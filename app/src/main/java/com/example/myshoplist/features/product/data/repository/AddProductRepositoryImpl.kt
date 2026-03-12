package com.example.myshoplist.features.product.data.repository

import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.features.product.data.datasource.local.mapper.toEntity
import com.example.myshoplist.features.product.data.datasource.remote.api.ProductApi
import com.example.myshoplist.features.product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.product.domain.repository.ProductRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class AddProductRepositoryImpl @Inject constructor(
    private val apiService: ProductApi,
    private val productDao: ProductDao,          // ← nuevo
) : ProductRepository {

    override suspend fun addProduct(
        name: String,
        category: String,
        estimatedPrice: Double,
    ): Result<Product> {
        return try {
            // 1. Intenta la API
            val response = apiService.addProduct(
                AddProductRequest(name, category, estimatedPrice)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val product = body.data.toDomain()
                    // 2a. Éxito → persiste en Room como caché
                    productDao.insertProduct(product.toEntity())
                    Result.success(product)
                } else {
                    Result.failure(Exception(body?.message ?: "Error desconocido en el servidor"))
                }
            } else {
                Result.failure(Exception("Error en la petición: ${response.code()} ${response.message()}"))
            }

        } catch (e: IOException) {
            // 2b. Sin conexión → guarda localmente con id temporal
            val localProduct = Product(
                id             = "local_${UUID.randomUUID()}",
                userId         = "local_user",
                name           = name,
                category       = category,
                estimatedPrice = estimatedPrice,
                isPurchased    = 0,
                createdAt      = System.currentTimeMillis().toString(),
            )
            productDao.insertProduct(localProduct.toEntity())
            // Devolvemos éxito para no bloquear al usuario
            Result.success(localProduct)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
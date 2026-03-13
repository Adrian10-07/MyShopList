package com.example.myshoplist.features.product.data.repository

import android.util.Log
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.sync.SyncScheduler
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
    private val productDao: ProductDao,
    private val syncScheduler: SyncScheduler,
) : ProductRepository {

    override suspend fun addProduct(
        name: String,
        category: String,
        estimatedPrice: Double,
    ): Result<Product> {
        return try {
            val response = apiService.addProduct(AddProductRequest(name, category, estimatedPrice))

            if (response.isSuccessful && response.body()?.success == true) {
                val product = response.body()!!.data!!.toDomain()
                productDao.insertProduct(product.toEntity(pendingSync = false))
                Result.success(product)
            } else {
                val msg = response.body()?.message ?: "Error ${response.code()}"
                Result.failure(Exception(msg))
            }

        } catch (e: IOException) {
            // Sin red → guarda con pendingSync=true para el worker
            val local = Product(
                id             = "local_${UUID.randomUUID()}",
                userId         = "local_user",
                name           = name,
                category       = category,
                estimatedPrice = estimatedPrice,
                isPurchased    = 0,
                createdAt      = System.currentTimeMillis().toString(),
            )
            productDao.insertProduct(local.toEntity(pendingSync = true))
            syncScheduler.schedule()
            Result.success(local)

        } catch (e: IOException) {
        Log.d("SYNC", "Sin red, guardando local: $name")  // ← AQUÍ

        val local = Product(
            id             = "local_${UUID.randomUUID()}",
            userId         = "local_user",
            name           = name,
            category       = category,
            estimatedPrice = estimatedPrice,
            isPurchased    = 0,
            createdAt      = System.currentTimeMillis().toString(),
        )

        productDao.insertProduct(local.toEntity(pendingSync = true))
        Log.d("SYNC", "Insertado en Room: ${local.id}")   // ← Y AQUÍ

        syncScheduler.schedule()
        Result.success(local)

    } catch (e: Exception) {
        Log.e("SYNC", "Error inesperado: ${e.stackTraceToString()}")  // ← Y AQUÍ
        Result.failure(e)
    }
    }
}
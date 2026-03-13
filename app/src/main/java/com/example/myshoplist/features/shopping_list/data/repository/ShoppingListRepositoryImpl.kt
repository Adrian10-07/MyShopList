package com.example.myshoplist.features.shopping_list.data.repository

import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.sync.SyncScheduler
import com.example.myshoplist.features.product.data.datasource.local.mapper.toDomain
import com.example.myshoplist.features.product.data.datasource.local.mapper.toEntity
import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.data.remote.api.ShoppingListApi
import com.example.myshoplist.features.shopping_list.data.remote.mapper.toDomain
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import java.io.IOException
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val apiService: ShoppingListApi,
    private val productDao: ProductDao,
    private val syncScheduler: SyncScheduler,
) : ShoppingListRepository {


    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()

            if (response.isSuccessful && response.body()?.success == true) {
                val remote = response.body()!!.data.toDomain()
                // Refresca Room con los datos del servidor
                remote.forEach { productDao.insertProduct(it.toEntity()) }
                Result.success(remote)
            } else {
                Result.success(productDao.getProductsOnce().map { it.toDomain() })
            }

        } catch (e: IOException) {
            // Sin internet → sirve desde Room
            Result.success(productDao.getProductsOnce().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(id)

            if (response.isSuccessful) {
                productDao.deleteProduct(id)        // borra físicamente
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar: ${response.code()}"))
            }

        } catch (e: IOException) {
            productDao.markForDeletion(id)
            syncScheduler.schedule()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.updateProduct(id)

            if (response.isSuccessful) {
                productDao.toggleIsPurchased(id, pending = false)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar: ${response.code()}"))
            }

        } catch (e: IOException) {
            productDao.toggleIsPurchased(id, pending = true)
            syncScheduler.schedule()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPurchase(request: CreatePurchaseRequest): Result<String> {
        return try {
            val response = apiService.createPurchase(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data.id)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                android.util.Log.e("BackendError", "El servidor rechazó la compra: $errorMsg")
                Result.failure(Exception(errorMsg))            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
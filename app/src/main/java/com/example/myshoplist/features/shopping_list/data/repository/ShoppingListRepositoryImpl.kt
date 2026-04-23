package com.example.myshoplist.features.shopping_list.data.repository

import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.session.SessionManager
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

    /** ID del usuario activo; cadena vacía si aún no hay sesión (no mostrará nada). */
    private val currentUserId get() = SessionManager.userId ?: ""

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()

            if (response.isSuccessful && response.body()?.success == true) {
                val remote = response.body()!!.data.toDomain()

                // Proteger productos con operaciones offline pendientes:
                // insertProduct con REPLACE resetearía sus flags a false.
                val pendingIds = productDao.getAllPendingIds().toSet()
                remote
                    .filter { it.id !in pendingIds }
                    .forEach { productDao.insertProduct(it.toEntity()) }

                // Retornar desde Room filtrando por usuario activo.
                // Incluye local_UUID pendientes del usuario y excluye pendingDelete=1.
                Result.success(productDao.getProductsOnce(currentUserId).map { it.toDomain() })
            } else {
                Result.success(productDao.getProductsOnce(currentUserId).map { it.toDomain() })
            }

        } catch (e: IOException) {
            // Sin internet → sirve desde Room
            Result.success(productDao.getProductsOnce(currentUserId).map { it.toDomain() })
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
        // El toggle de selección es una operación PURAMENTE LOCAL en Room.
        // El servidor tiene una validación que rechaza CREATE PURCHASE si
        // isPurchased=1 ya está seteado vía su propio endpoint PATCH/toggle.
        // Por eso NO llamamos a la API aquí: el servidor se entera del cambio
        // únicamente cuando se envía el POST /purchases (finalizePurchase).
        productDao.toggleIsPurchased(id, pending = false)
        return Result.success(Unit)
    }

    override suspend fun createPurchase(request: CreatePurchaseRequest): Result<String> {
        return try {
            val response = apiService.createPurchase(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data.id)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                android.util.Log.e("BackendError", "El servidor rechazó la compra: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            // Re-lanzamos IOException para que CreatePurchaseUseCase la capture
            // y ejecute el guardado offline.
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
package com.example.myshoplist.features.shopping_list.data.repository

import com.example.myshoplist.core.database.product.dao.ProductDao
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
    private val productDao: ProductDao,         // ← nuevo
) : ShoppingListRepository {

    // ------------------------------------------------------------------ //
    //  GET — API primero, Room como fallback                               //
    // ------------------------------------------------------------------ //

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()

            if (response.isSuccessful && response.body()?.success == true) {
                val products = response.body()!!.data.toDomain()

                // Refresca Room con los datos del servidor
                products.forEach { product ->
                    productDao.insertProduct(product.toEntity())
                }

                Result.success(products)
            } else {
                // API respondió pero con error → lee Room
                val local = productDao.getProductsOnce().map { it.toDomain() }
                Result.success(local)
            }

        } catch (e: IOException) {
            // Sin internet → sirve desde Room
            val local = productDao.getProductsOnce().map { it.toDomain() }
            Result.success(local)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------ //
    //  DELETE — API primero, Room como fallback                            //
    // ------------------------------------------------------------------ //

    override suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(id)

            if (response.isSuccessful) {
                productDao.deleteProduct(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar: ${response.code()}"))
            }

        } catch (e: IOException) {
            // Sin internet → elimina solo en Room
            productDao.deleteProduct(id)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------ //
    //  TOGGLE — API primero, Room como fallback                            //
    // ------------------------------------------------------------------ //

    override suspend fun updateProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.updateProduct(id)

            if (response.isSuccessful) {
                productDao.toggleIsPurchased(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar: ${response.code()}"))
            }

        } catch (e: IOException) {
            // Sin internet → alterna en Room localmente
            productDao.toggleIsPurchased(id)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------ //
    //  CREATE PURCHASE — solo API (requiere conexión)                      //
    // ------------------------------------------------------------------ //

    override suspend fun createPurchase(request: CreatePurchaseRequest): Result<String> {
        return try {
            val response = apiService.createPurchase(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data.id)
            } else {
                Result.failure(Exception("Error al registrar la compra en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
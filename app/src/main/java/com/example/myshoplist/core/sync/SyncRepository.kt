package com.example.myshoplist.core.sync

import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.features.product.data.datasource.local.mapper.toEntity
import com.example.myshoplist.features.product.data.datasource.remote.api.ProductApi
import com.example.myshoplist.features.product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.shopping_list.data.remote.api.ShoppingListApi
import com.example.myshoplist.features.shopping_list.data.remote.mapper.toDomain
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val productDao: ProductDao,
    private val productApi: ProductApi,
    private val shoppingListApi: ShoppingListApi,
) {
    /**
     * Sube al servidor todos los cambios pendientes y luego refresca Room.
     * Retorna [Result.success] si al menos el proceso corrió sin errores de red.
     */
    suspend fun sync(): Result<Unit> {
        return try {

            // 1. Subir inserciones offline (addProduct sin internet)
            productDao.getPendingInserts().forEach { entity ->
                val response = productApi.addProduct(
                    AddProductRequest(entity.name, entity.category, entity.estimatedPrice)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val remote = response.body()!!.data!!.toDomain()
                    productDao.deleteProduct(entity.id)             // borra id local
                    productDao.insertProduct(remote.toEntity())      // inserta id real
                }
                // Si falla un item, se omite y se reintentará en el próximo sync
            }

            // 2. Subir eliminaciones offline (deleteProduct sin internet)
            productDao.getPendingDeletions().forEach { entity ->
                val response = shoppingListApi.deleteProduct(entity.id)
                if (response.isSuccessful) {
                    productDao.deleteProduct(entity.id)
                }
            }

            // 3. Subir toggles offline (updateProduct sin internet)
            productDao.getPendingToggles().forEach { entity ->
                val response = shoppingListApi.updateProduct(entity.id)
                if (response.isSuccessful) {
                    productDao.clearPendingToggle(entity.id)
                }
            }

            // 4. Refrescar lista completa desde el servidor
            val listResponse = shoppingListApi.getProducts()
            if (listResponse.isSuccessful && listResponse.body()?.success == true) {
                val remote = listResponse.body()!!.data
                    .toDomain()
                    .map { it.toEntity() }
                productDao.replaceAllSynced(remote)
            }

            Result.success(Unit)

        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión durante el sync."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
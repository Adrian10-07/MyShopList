package com.example.myshoplist.core.sync

import android.util.Log
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseItemEntity
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.features.product.data.datasource.local.mapper.toEntity
import com.example.myshoplist.features.product.data.datasource.remote.api.ProductApi
import com.example.myshoplist.features.product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.product.data.datasource.remote.model.AddProductRequest
import com.example.myshoplist.features.shopping_list.data.remote.api.ShoppingListApi
import com.example.myshoplist.features.shopping_list.data.remote.mapper.toDomain
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.data.remote.model.PurchaseProductRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val productDao: ProductDao,
    private val productApi: ProductApi,
    private val shoppingListApi: ShoppingListApi,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val purchaseLocationDao: PurchaseLocationDao,
) {

    suspend fun sync(): Result<Unit> {
        return try {

            // Subir compras offline pendientes //
            purchaseHistoryDao.getPendingPurchases().forEach { entity ->
                val items = purchaseHistoryDao.getItemsForPurchase(entity.id)
                val request = CreatePurchaseRequest(
                    totalAmount  = entity.totalAmount,
                    purchaseDate = entity.purchaseDate,
                    products     = items.map { item ->
                        PurchaseProductRequest(
                            productId   = item.productId,
                            productName = item.productName,
                            category    = item.category,
                            price       = item.price,
                        )
                    },
                )
                val response = shoppingListApi.createPurchase(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val serverId = response.body()!!.data.id
                    Log.d("SYNC", "Compra offline sincronizada: ${entity.id} → $serverId")

                    // Actualizar el purchaseId de la ubicación GPS si existe
                    purchaseLocationDao.updatePurchaseId(entity.id, serverId)

                    // Reemplazar la entidad local con la del servidor
                    purchaseHistoryDao.replacePendingWithServer(
                        localId        = entity.id,
                        serverPurchase = PurchaseEntity(
                            id           = serverId,
                            totalAmount  = entity.totalAmount,
                            purchaseDate = entity.purchaseDate,
                            itemCount    = entity.itemCount,
                            pendingSync  = false,
                        ),
                        serverItems = items.map { item ->
                            PurchaseItemEntity(
                                purchaseId  = serverId,
                                productId   = item.productId,
                                productName = item.productName,
                                category    = item.category,
                                price       = item.price,
                            )
                        },
                    )
                }
            }

            //  Subir inserciones offline //
            val pendingInserts = productDao.getPendingInserts()

            // Productos creados Y borrados offline antes de sincronizarse:
            // nunca llegaron al servidor, así que simplemente los eliminamos de Room.
            pendingInserts
                .filter { it.pendingDelete }
                .forEach { entity ->
                    Log.d("SYNC", "Descartando producto local nunca subido: ${entity.id}")
                    productDao.deleteProduct(entity.id)
                }

            // Productos creados offline y aún vigentes: subirlos al servidor.
            pendingInserts
                .filter { !it.pendingDelete }
                .forEach { entity ->
                    val response = productApi.addProduct(
                        AddProductRequest(entity.name, entity.category, entity.estimatedPrice)
                    )
                    if (response.isSuccessful && response.body()?.success == true) {
                        val remote = response.body()!!.data!!.toDomain()
                        productDao.deleteProduct(entity.id)        // borra id local
                        productDao.insertProduct(remote.toEntity()) // inserta id real
                        Log.d("SYNC", "Producto sincronizado: ${entity.id} → ${remote.id}")
                    }
                    // Si falla un item, se omite y se reintentará en el próximo sync
                }

            //  Subir eliminaciones offline  //
            productDao.getPendingDeletions().forEach { entity ->
                val response = shoppingListApi.deleteProduct(entity.id)
                // Tratamos 404 como éxito: el producto ya no existe en el servidor
                // (quizás fue borrado por otra sesión), así que lo eliminamos de Room.
                if (response.isSuccessful || response.code() == 404) {
                    productDao.deleteProduct(entity.id)
                    Log.d("SYNC", "Eliminación sincronizada: ${entity.id} (HTTP ${response.code()})")
                }
            }
            //  Refrescar lista completa desde el servidor  //
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

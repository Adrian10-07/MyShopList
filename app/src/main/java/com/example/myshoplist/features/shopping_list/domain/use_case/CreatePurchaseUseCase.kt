package com.example.myshoplist.features.shopping_list.domain.use_case

import android.util.Log
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseItemEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseLocationEntity
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.hardware.location.LocationClient
import com.example.myshoplist.core.sync.SyncScheduler
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class CreatePurchaseUseCase @Inject constructor(
    private val apiRepository: ShoppingListRepository,
    private val localDao: PurchaseLocationDao,
    private val locationClient: LocationClient,
    private val productDao: ProductDao,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val syncScheduler: SyncScheduler,
) {
    suspend operator fun invoke(request: CreatePurchaseRequest): Result<Unit> {
        // Obtenemos la ubicación antes del intento de red para tenerla disponible
        // tanto en el path online como offline.
        val location = try { locationClient.getCurrentLocation() } catch (_: Exception) { null }

        return try {
            val apiResult = apiRepository.createPurchase(request)

            if (apiResult.isSuccess) {
                val purchaseId = apiResult.getOrNull()!!

                // Guardar ubicación GPS asociada a la compra
                if (location != null) {
                    localDao.insertLocation(
                        PurchaseLocationEntity(
                            purchaseId = purchaseId,
                            latitude   = location.latitude,
                            longitude  = location.longitude,
                        )
                    )
                }

                // Eliminar los productos comprados de Room para que desaparezcan
                // de la lista principal de inmediato.
                val purchasedIds = request.products.map { it.productId }
                productDao.deleteProductsByIds(purchasedIds)

                Result.success(Unit)
            } else {
                Result.failure(apiResult.exceptionOrNull() ?: Exception("Unknown error"))
            }

        } catch (e: IOException) {
            // ── Sin internet: guardamos la compra localmente ─────────── //
            Log.d("SYNC", "Sin red, guardando compra offline")
            val localPurchaseId = "local_purchase_${UUID.randomUUID()}"

            purchaseHistoryDao.savePurchaseWithItems(
                purchase = PurchaseEntity(
                    id           = localPurchaseId,
                    totalAmount  = request.totalAmount,
                    purchaseDate = request.purchaseDate,
                    itemCount    = request.products.size,
                    pendingSync  = true,
                ),
                items = request.products.map { product ->
                    PurchaseItemEntity(
                        purchaseId  = localPurchaseId,
                        productId   = product.productId,
                        productName = product.productName,
                        category    = product.category,
                        price       = product.price,
                    )
                },
            )

            // Guardar GPS si está disponible
            if (location != null) {
                localDao.insertLocation(
                    PurchaseLocationEntity(
                        purchaseId = localPurchaseId,
                        latitude   = location.latitude,
                        longitude  = location.longitude,
                    )
                )
            }

            // Eliminar optimistamente los productos de la lista de compras
            val purchasedIds = request.products.map { it.productId }
            productDao.deleteProductsByIds(purchasedIds)

            syncScheduler.schedule()
            Log.d("SYNC", "Compra offline guardada: $localPurchaseId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SYNC", "Error inesperado al crear compra: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}

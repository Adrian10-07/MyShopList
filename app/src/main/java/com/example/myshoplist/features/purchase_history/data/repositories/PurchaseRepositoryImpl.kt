package com.example.myshoplist.features.purchase_history.data.repositories

import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.features.purchase_history.data.datasource.remote.api.PurchaseApi
import com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper.toEntity
import com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper.toItemEntity
import com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.purchase_history.domain.entities.Purchase
import com.example.myshoplist.features.purchase_history.domain.repositories.PurchaseRepository
import java.io.IOException
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val api: PurchaseApi,
    private val purchaseHistoryDao: PurchaseHistoryDao,
    private val purchaseLocationDao: PurchaseLocationDao,
) : PurchaseRepository {

    override suspend fun getPurchases(): Result<List<Purchase>> {
        return try {
            val response = api.getPurchaseHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data

                // Cachear en Room
                dtos.forEach { dto ->
                    purchaseHistoryDao.savePurchaseWithItems(
                        purchase = dto.toEntity(),
                        items    = dto.products?.map { it.toItemEntity(dto.id) } ?: emptyList(),
                    )
                }

                Result.success(buildPurchaseList())
            } else {
                // Si la API falla, servimos desde Room
                Result.success(buildPurchaseList())
            }

        } catch (e: IOException) {
            // Sin internet → fallback Room
            Result.success(buildPurchaseList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Construye la lista de dominio combinando Room + ubicaciones GPS. */
    private suspend fun buildPurchaseList(): List<Purchase> {
        return purchaseHistoryDao.getAllPurchases().map { entity ->
            val items    = purchaseHistoryDao.getItemsForPurchase(entity.id)
            val location = purchaseLocationDao.getLocationForPurchase(entity.id)
            entity.toDomain(
                items     = items,
                latitude  = location?.latitude,
                longitude = location?.longitude,
            )
        }
    }
}
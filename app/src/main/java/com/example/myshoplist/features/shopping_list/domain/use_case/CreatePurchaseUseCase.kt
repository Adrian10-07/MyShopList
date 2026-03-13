package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseLocationEntity
import com.example.myshoplist.core.hardware.location.LocationClient
import com.example.myshoplist.features.shopping_list.data.remote.model.CreatePurchaseRequest
import com.example.myshoplist.features.shopping_list.domain.repository.ShoppingListRepository
import javax.inject.Inject

class CreatePurchaseUseCase @Inject constructor(
    private val apiRepository: ShoppingListRepository,
    private val localDao: PurchaseLocationDao, // Inyectamos Room
    private val locationClient: LocationClient // Inyectamos Hardware GPS
) {
    suspend operator fun invoke(request: CreatePurchaseRequest): Result<Unit> {
        return try {
            val location = locationClient.getCurrentLocation()

            val apiResult = apiRepository.createPurchase(request)

            apiResult.onSuccess { purchaseId ->
                if (location != null) {
                    val locationEntity = PurchaseLocationEntity(
                        purchaseId = purchaseId,
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    localDao.insertLocation(locationEntity)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
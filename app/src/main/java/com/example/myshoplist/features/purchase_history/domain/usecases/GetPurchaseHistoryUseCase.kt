package com.tuspaquetes.features.purchase_history.domain.usecases

import com.example.myshoplist.features.purchase_history.domain.entities.Purchase
import com.example.myshoplist.features.purchase_history.domain.repositories.PurchaseRepository
import javax.inject.Inject

class GetPurchaseHistoryUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(): Result<List<Purchase>> {
        return try {
            val purchases = repository.getPurchases()
            Result.success(purchases.getOrThrow())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
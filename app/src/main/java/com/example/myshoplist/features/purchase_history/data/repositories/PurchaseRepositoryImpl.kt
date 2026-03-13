package com.example.myshoplist.features.purchase_history.data.repositories

import com.example.myshoplist.features.purchase_history.data.datasource.remote.api.PurchaseApi
import com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.purchase_history.domain.repositories.PurchaseRepository
import com.example.myshoplist.features.purchase_history.domain.entities.Purchase
import javax.inject.Inject

class PurchaseRepositoryImpl @Inject constructor(
    private val api: PurchaseApi
) : PurchaseRepository {

    override suspend fun getPurchases(): Result<List<Purchase>> {
        return try {
            val response = api.getPurchaseHistory()
            if (response.isSuccessful && response.body()?.success == true) {
                val domainPurchases = response.body()!!.data.map { it.toDomain() }
                Result.success(domainPurchases)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
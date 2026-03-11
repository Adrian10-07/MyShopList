package com.example.myshoplist.features.purchase_history.domain.repositories

import com.example.myshoplist.features.purchase_history.domain.entities.Purchase

interface PurchaseRepository {
    suspend fun getPurchases(): Result<List<Purchase>>
}
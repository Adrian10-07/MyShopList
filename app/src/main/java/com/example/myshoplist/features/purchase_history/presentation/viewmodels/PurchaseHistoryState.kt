package com.tuspaquetes.features.purchase_history.presentation.viewmodels

import com.example.myshoplist.features.purchase_history.domain.entities.Purchase


data class PurchaseHistoryState(
    val purchases: List<Purchase> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

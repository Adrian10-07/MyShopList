package com.example.myshoplist.features.purchase_history.domain.entities

data class Purchase(
    val id: String,
    val totalAmount: Double,
    val purchaseDate: String,
    val itemCount: Int,
    val products: List<PurchaseProduct> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
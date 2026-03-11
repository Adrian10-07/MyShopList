package com.example.myshoplist.features.shopping_list.data.remote.model

data class CreatePurchaseRequest(
    val totalAmount: Double,
    val purchaseDate: String,
    val products: List<PurchaseProductRequest>
)

data class PurchaseProductRequest(
    val productId: String,
    val productName: String,
    val category: String,
    val price: Double
)
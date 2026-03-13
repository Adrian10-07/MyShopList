package com.example.myshoplist.features.purchase_history.data.datasource.remote.models

import com.google.gson.annotations.SerializedName

data class PurchaseResponseDto(
    val success: Boolean,
    val data: List<PurchaseDto>,
    val count: Int?
)

data class PurchaseDto(
    val id: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("purchase_date") val purchaseDate: String,
    val itemCount: Int,
    val products: List<PurchaseProductDto>?
)

data class PurchaseProductDto(
    @SerializedName("product_name") val productName: String?,
    val category: String?,
    val price: Double?
)
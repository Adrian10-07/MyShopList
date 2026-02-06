package com.example.myshoplist.features.add_product.data.datasource.remote.model

import com.google.gson.annotations.SerializedName


data class AddProductRequest(
    val name: String,
    val category: String,
    val estimatedPrice: Double
)


data class AddProductResponse(
    val success: Boolean,
    val message: String,
    val data: ProductDto?
)

data class  ProductDto(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    val name: String,
    val category: String,
    @SerializedName("estimated_price")
    val estimatedPrice: Double,
    @SerializedName("is_purchased")
    val isPurchased: Int,
    @SerializedName("created_at")
    val createdAt: String
)
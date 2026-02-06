package com.example.myshoplist.features.add_product.data.datasource.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AddProductRequest(
    val name: String,
    val category: String,
    val estimatedPrice: Double
)

@Serializable
data class AddProductResponse(
    val success: Boolean,
    val message: String,
    val data: ProductDto?
)

@Serializable
data class  ProductDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val category: String,
    @SerialName("estimated_price")
    val estimatedPrice: Double,
    @SerialName("is_purchased")
    val isPurchased: Boolean,
    @SerialName("created_at")
    val createdAt: String
)
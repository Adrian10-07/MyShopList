package com.example.myshoplist.features.shopping_list.data.remote.model

import com.google.gson.annotations.SerializedName

data class ShoppingListRequest(
    val success: Boolean,
    val count: Int,
    val data: List<ShoppingListDto>
)

data class ShoppingListDto(
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
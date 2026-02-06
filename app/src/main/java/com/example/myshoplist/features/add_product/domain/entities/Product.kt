package com.example.myshoplist.features.add_product.domain.entities

data class Product(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val estimatedPrice: Double,
    val isPurchased: Int = 0,
    val createdAt: String
)
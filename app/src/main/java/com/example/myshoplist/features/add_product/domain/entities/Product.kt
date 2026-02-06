package com.example.myshoplist.features.add_product.domain.entities

data class Product(
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val estimatedPrice: Double,
    val isPurchased: Boolean = false,
    val createdAt: String
)
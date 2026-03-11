package com.example.myshoplist.features.product.domain.repository

import com.example.myshoplist.features.product.domain.entities.Product

interface ProductRepository {
    suspend fun addProduct(
        name: String,
        category: String,
        estimatedPrice: Double
    ): Result<Product>
}
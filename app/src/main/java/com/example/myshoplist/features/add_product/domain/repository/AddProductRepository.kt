package com.example.myshoplist.features.add_product.domain.repository

import com.example.myshoplist.features.add_product.domain.entities.Product

interface AddProductRepository {
    suspend fun addProduct(
        name: String,
        category: String,
        estimatedPrice: Double
    ): Result<Product>
}
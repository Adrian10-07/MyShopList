package com.example.myshoplist.features.product.data.datasource.remote.mapper

import com.example.myshoplist.features.product.data.datasource.remote.model.ProductDto
import com.example.myshoplist.features.product.domain.entities.Product

fun ProductDto.toDomain(): Product {
    return Product(
        id = this.id,
        userId = this.userId,
        name = this.name,
        category = this.category,
        estimatedPrice = this.estimatedPrice,
        isPurchased = this.isPurchased,
        createdAt = this.createdAt
    )
}
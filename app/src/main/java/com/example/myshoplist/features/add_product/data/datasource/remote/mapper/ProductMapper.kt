package com.example.myshoplist.features.add_product.data.datasource.remote.mapper

import com.example.myshoplist.features.add_product.data.datasource.remote.model.ProductDto
import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListDto

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
package com.example.myshoplist.features.shopping_list.data.remote.mapper

import com.example.myshoplist.features.add_product.data.datasource.remote.mapper.toDomain
import com.example.myshoplist.features.add_product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.data.remote.model.ShoppingListDto

fun ShoppingListDto.toDomain(): Product {
    return Product(
        id = id,
        userId = userId,
        name = name,
        category = category,
        estimatedPrice = estimatedPrice,
        isPurchased = isPurchased,
        createdAt = createdAt
    )
}

fun List<ShoppingListDto>.toDomain(): List<Product> {
    return this.map { it.toDomain() }
}

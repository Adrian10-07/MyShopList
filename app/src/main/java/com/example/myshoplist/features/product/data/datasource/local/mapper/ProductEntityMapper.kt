package com.example.myshoplist.features.product.data.datasource.local.mapper

import com.example.myshoplist.core.database.product.entities.ProductEntity
import com.example.myshoplist.features.product.domain.entities.Product

// Domain → Room Entity
fun Product.toEntity(): ProductEntity = ProductEntity(
    id             = id,
    userId         = userId,
    name           = name,
    category       = category,
    estimatedPrice = estimatedPrice,
    isPurchased    = isPurchased,
    createdAt      = createdAt,
)

// Room Entity → Domain
fun ProductEntity.toDomain(): Product = Product(
    id             = id,
    userId         = userId,
    name           = name,
    category       = category,
    estimatedPrice = estimatedPrice,
    isPurchased    = isPurchased,
    createdAt      = createdAt,
)
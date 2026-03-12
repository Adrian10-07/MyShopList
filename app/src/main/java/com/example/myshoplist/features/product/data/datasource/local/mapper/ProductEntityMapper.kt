package com.example.myshoplist.features.product.data.datasource.local.mapper

import com.example.myshoplist.core.database.product.entities.ProductEntity
import com.example.myshoplist.features.product.domain.entities.Product

fun Product.toEntity(
    pendingSync: Boolean = false,
    pendingDelete: Boolean = false,
    pendingToggle: Boolean = false,
): ProductEntity = ProductEntity(
    id             = id,
    userId         = userId,
    name           = name,
    category       = category,
    estimatedPrice = estimatedPrice,
    isPurchased    = isPurchased,
    createdAt      = createdAt,
    pendingSync    = pendingSync,
    pendingDelete  = pendingDelete,
    pendingToggle  = pendingToggle,
)

fun ProductEntity.toDomain(): Product = Product(
    id             = id,
    userId         = userId,
    name           = name,
    category       = category,
    estimatedPrice = estimatedPrice,
    isPurchased    = isPurchased,
    createdAt      = createdAt,
)
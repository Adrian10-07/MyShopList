package com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper

import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseItemEntity
import com.example.myshoplist.features.purchase_history.data.datasource.remote.models.PurchaseDto
import com.example.myshoplist.features.purchase_history.data.datasource.remote.models.PurchaseProductDto
import com.example.myshoplist.features.purchase_history.domain.entities.Purchase
import com.example.myshoplist.features.purchase_history.domain.entities.PurchaseProduct

// ── DTO → Room Entity ──────────────────────────────────────────── //

fun PurchaseDto.toEntity(): PurchaseEntity = PurchaseEntity(
    id           = id,
    totalAmount  = totalAmount,
    purchaseDate = purchaseDate,
    itemCount    = itemCount,
)

fun PurchaseProductDto.toItemEntity(purchaseId: String): PurchaseItemEntity = PurchaseItemEntity(
    purchaseId  = purchaseId,
    productId   = productId   ?: "",
    productName = productName ?: "",
    category    = category    ?: "",
    price       = price       ?: 0.0,
)

// ── Room Entity → Domain ───────────────────────────────────────── //

fun PurchaseEntity.toDomain(
    items: List<PurchaseItemEntity> = emptyList(),
    latitude: Double?  = null,
    longitude: Double? = null,
): Purchase = Purchase(
    id           = id,
    totalAmount  = totalAmount,
    purchaseDate = purchaseDate,
    itemCount    = itemCount,
    products     = items.map { it.toDomain() },
    latitude     = latitude,
    longitude    = longitude,
)

fun PurchaseItemEntity.toDomain(): PurchaseProduct = PurchaseProduct(
    name     = productName,
    category = category,
    price    = price,
)

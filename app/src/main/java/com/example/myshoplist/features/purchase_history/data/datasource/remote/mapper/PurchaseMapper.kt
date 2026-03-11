package com.example.myshoplist.features.purchase_history.data.datasource.remote.mapper

import com.example.myshoplist.features.purchase_history.data.datasource.remote.models.PurchaseDto
import com.example.myshoplist.features.purchase_history.domain.entities.Purchase

fun PurchaseDto.toDomain(): Purchase {
    return Purchase(
        id = this.id,
        totalAmount = this.totalAmount,
        purchaseDate = this.purchaseDate,
        itemCount = this.itemCount
    )
}
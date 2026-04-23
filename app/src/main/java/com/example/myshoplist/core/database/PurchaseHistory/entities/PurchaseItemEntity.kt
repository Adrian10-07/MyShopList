package com.example.myshoplist.core.database.PurchaseHistory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val purchaseId: String,
    /** ID del producto en el servidor (necesario para reconstruir la petición durante sync offline). */
    val productId: String = "",
    val productName: String,
    val category: String,
    val price: Double,
)

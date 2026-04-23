package com.example.myshoplist.core.database.PurchaseHistory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val purchaseId: String,
    val productId: String = "",
    val productName: String,
    val category: String,
    val price: Double,
)

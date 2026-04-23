package com.example.myshoplist.core.database.PurchaseHistory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val totalAmount: Double,
    val purchaseDate: String,
    val itemCount: Int,
    val pendingSync: Boolean = false,
)

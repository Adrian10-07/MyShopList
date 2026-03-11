package com.example.myshoplist.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_locations")
data class PurchaseLocationEntity(
    @PrimaryKey
    val purchaseId: String,
    val latitude: Double,
    val longitude: Double,
    val dateSaved: Long = System.currentTimeMillis()
)

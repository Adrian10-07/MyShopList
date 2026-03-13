package com.example.myshoplist.core.database.product.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(

    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val category: String,
    val estimatedPrice: Double,
    val isPurchased: Int,
    val createdAt: String,

    val pendingSync: Boolean = false,
    val pendingDelete: Boolean = false,
    val pendingToggle: Boolean = false,
)
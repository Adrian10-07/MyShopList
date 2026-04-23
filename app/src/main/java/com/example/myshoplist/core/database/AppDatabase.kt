package com.example.myshoplist.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseHistoryDao
import com.example.myshoplist.core.database.PurchaseHistory.dao.PurchaseLocationDao
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.database.product.entities.ProductEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseItemEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseLocationEntity
import com.example.myshoplist.core.database.profile.dao.UserProfileDao
import com.example.myshoplist.core.database.profile.entities.UserProfileEntity

@Database(
    entities = [
        ProductEntity::class,
        PurchaseLocationEntity::class,
        UserProfileEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun purchaseLocationDao(): PurchaseLocationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun purchaseHistoryDao(): PurchaseHistoryDao
}
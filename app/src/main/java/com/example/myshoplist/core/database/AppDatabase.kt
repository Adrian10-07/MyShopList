package com.example.myshoplist.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myshoplist.core.database.product.dao.ProductDao
import com.example.myshoplist.core.database.product.entities.ProductEntity

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

}
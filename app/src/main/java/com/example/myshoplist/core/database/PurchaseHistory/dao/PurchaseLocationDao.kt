package com.example.myshoplist.core.database.PurchaseHistory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseLocationEntity

@Dao
interface PurchaseLocationDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLocation(location: PurchaseLocationEntity)

    @Query("SELECT * FROM purchase_locations WHERE purchaseId = :purchaseId")
    suspend fun getLocationForPurchase(purchaseId: String): PurchaseLocationEntity?
}
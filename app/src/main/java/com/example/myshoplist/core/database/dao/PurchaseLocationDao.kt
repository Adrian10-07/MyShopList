package com.example.myshoplist.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myshoplist.core.database.entities.PurchaseLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: PurchaseLocationEntity)

    @Query("SELECT * FROM purchase_locations WHERE purchaseId = :purchaseId")
    suspend fun getLocationForPurchase(purchaseId: String): PurchaseLocationEntity?
}
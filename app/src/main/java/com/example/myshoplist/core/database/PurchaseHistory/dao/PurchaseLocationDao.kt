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

    /** Actualiza el purchaseId de una ubicación al hacer sync de compra offline. */
    @Query("UPDATE purchase_locations SET purchaseId = :newId WHERE purchaseId = :oldId")
    suspend fun updatePurchaseId(oldId: String, newId: String)

    /** Elimina todas las ubicaciones GPS (usado al cambiar de usuario). */
    @Query("DELETE FROM purchase_locations")
    suspend fun deleteAllLocations()
}
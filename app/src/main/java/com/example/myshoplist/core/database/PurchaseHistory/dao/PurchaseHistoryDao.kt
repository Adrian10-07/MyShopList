package com.example.myshoplist.core.database.PurchaseHistory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseEntity
import com.example.myshoplist.core.database.PurchaseHistory.entities.PurchaseItemEntity

@Dao
interface PurchaseHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    suspend fun getAllPurchases(): List<PurchaseEntity>

    @Query("SELECT * FROM purchases WHERE pendingSync = 1")
    suspend fun getPendingPurchases(): List<PurchaseEntity>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchase(purchaseId: String): List<PurchaseItemEntity>

    /** Elimina los ítems de una compra (previene duplicados al re-cachear). */
    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsForPurchase(purchaseId: String)

    /** Elimina físicamente una compra por su ID. */
    @Query("DELETE FROM purchases WHERE id = :purchaseId")
    suspend fun deletePurchase(purchaseId: String)

    /**
     * Guarda una compra con sus ítems en una sola transacción.
     * Borra los ítems previos antes de insertar para evitar duplicados
     * cuando se re-cachea la misma compra desde la API.
     */
    @Transaction
    suspend fun savePurchaseWithItems(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>,
    ) {
        insertPurchase(purchase)
        deleteItemsForPurchase(purchase.id)   // ← limpia antes de reinsertar
        if (items.isNotEmpty()) {
            insertPurchaseItems(items)
        }
    }

    /** Elimina todos los ítems de compras (usado al cambiar de usuario). */
    @Query("DELETE FROM purchase_items")
    suspend fun deleteAllPurchaseItems()

    /** Elimina todas las compras (usado al cambiar de usuario). */
    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()

    /** Limpia el historial completo en una sola transacción (cambio de usuario). */
    @Transaction
    suspend fun deleteAllPurchasesAndItems() {
        deleteAllPurchaseItems()   // primero los ítems (evita registros huérfanos)
        deleteAllPurchases()
    }

    /**
     * Reemplaza una compra local pendiente (id local) por la versión del servidor.
     * Usado al sincronizar compras realizadas sin internet.
     */
    @Transaction
    suspend fun replacePendingWithServer(
        localId: String,
        serverPurchase: PurchaseEntity,
        serverItems: List<PurchaseItemEntity>,
    ) {
        deleteItemsForPurchase(localId)
        deletePurchase(localId)
        insertPurchase(serverPurchase)
        if (serverItems.isNotEmpty()) {
            insertPurchaseItems(serverItems)
        }
    }
}

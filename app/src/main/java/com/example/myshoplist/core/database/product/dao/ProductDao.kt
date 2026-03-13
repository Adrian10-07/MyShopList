package com.example.myshoplist.core.database.product.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myshoplist.core.database.product.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE pendingDelete = 0 ORDER BY createdAt DESC")
    fun getProducts(): Flow<List<ProductEntity>>

    /** Lectura única para sync y fallback offline. */
    @Query("SELECT * FROM products WHERE pendingDelete = 0 ORDER BY createdAt DESC")
    suspend fun getProductsOnce(): List<ProductEntity>

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    /** Marca para borrar offline en lugar de eliminar físicamente. */
    @Query("UPDATE products SET pendingDelete = 1 WHERE id = :id")
    suspend fun markForDeletion(id: String)

    /** Alterna isPurchased y marca pendingToggle si aplica. */
    @Query("""
        UPDATE products
        SET isPurchased   = CASE WHEN isPurchased = 0 THEN 1 ELSE 0 END,
            pendingToggle = :pending
        WHERE id = :id
    """)
    suspend fun toggleIsPurchased(id: String, pending: Boolean = false)

    // ── Queries de sync ────────────────────────────────────────────────── //

    @Query("SELECT * FROM products WHERE pendingSync = 1")
    suspend fun getPendingInserts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE pendingDelete = 1")
    suspend fun getPendingDeletions(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE pendingToggle = 1")
    suspend fun getPendingToggles(): List<ProductEntity>

    @Query("UPDATE products SET pendingToggle = 0 WHERE id = :id")
    suspend fun clearPendingToggle(id: String)

    /** Reemplaza todos los productos sincronizados con los del servidor. */
    @Transaction
    suspend fun replaceAllSynced(remote: List<ProductEntity>) {
        deleteAllSynced()
        remote.forEach { insertProduct(it) }
    }

    @Query("""
        DELETE FROM products
        WHERE pendingSync = 0 AND pendingDelete = 0 AND pendingToggle = 0
    """)
    suspend fun deleteAllSynced()
}
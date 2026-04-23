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

    @Query("SELECT * FROM products WHERE pendingDelete = 0 AND userId = :userId ORDER BY createdAt DESC")
    fun getProducts(userId: String): Flow<List<ProductEntity>>

    /** Lectura única para sync y fallback offline. Solo devuelve productos del usuario indicado. */
    @Query("SELECT * FROM products WHERE pendingDelete = 0 AND userId = :userId ORDER BY createdAt DESC")
    suspend fun getProductsOnce(userId: String): List<ProductEntity>

    /** Elimina TODOS los productos de Room (usado al cambiar de usuario). */
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    /** Elimina físicamente varios productos de una vez (p.ej. después de finalizar compra). */
    @Query("DELETE FROM products WHERE id IN (:ids)")
    suspend fun deleteProductsByIds(ids: List<String>)

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

    /** IDs de todos los productos con alguna operación offline pendiente. */
    @Query("SELECT id FROM products WHERE pendingDelete = 1 OR pendingSync = 1 OR pendingToggle = 1")
    suspend fun getAllPendingIds(): List<String>

    /**
     * Reemplaza los productos sincronizados con los del servidor,
     * sin tocar los que aún tienen operaciones offline pendientes.
     */
    @Transaction
    suspend fun replaceAllSynced(remote: List<ProductEntity>) {
        val pendingIds = getAllPendingIds().toSet()
        deleteAllSynced()
        remote
            .filter { it.id !in pendingIds }
            .forEach { insertProduct(it) }
    }

    @Query("""
        DELETE FROM products
        WHERE pendingSync = 0 AND pendingDelete = 0 AND pendingToggle = 0
    """)
    suspend fun deleteAllSynced()
}
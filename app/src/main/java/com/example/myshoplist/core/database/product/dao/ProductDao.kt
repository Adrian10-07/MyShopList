package com.example.myshoplist.core.database.product.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myshoplist.core.database.product.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    suspend fun getProductsOnce(): List<ProductEntity>

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    /** Alterna isPurchased (0→1 ó 1→0) directamente en Room. */
    @Query("""
        UPDATE products
        SET isPurchased = CASE WHEN isPurchased = 0 THEN 1 ELSE 0 END
        WHERE id = :id
    """)
    suspend fun toggleIsPurchased(id: String)
}
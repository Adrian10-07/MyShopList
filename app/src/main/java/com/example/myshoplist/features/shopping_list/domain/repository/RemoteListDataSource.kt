package com.example.myshoplist.features.shopping_list.domain.repository

import com.example.myshoplist.features.product.domain.entities.Product
import kotlinx.coroutines.flow.Flow

interface RemoteListDataSource {

    /** Sube o actualiza campos sueltos de la lista (merge) */
    suspend fun syncListToCloud(listId: String, data: Map<String, Any>): Result<Boolean>

    /**
     * Sube la lista completa de productos de Room a Firestore.
     * Cada producto se guarda como un mapa bajo la clave "items/{productId}".
     * Se usa SetOptions.merge para no sobreescribir campos como isPurchased
     * que otro usuario pudo haber cambiado.
     */
    suspend fun syncProductsToCloud(listId: String, products: List<Product>): Result<Boolean>

    /**
     * Cambia el estado isPurchased de un producto específico en Firestore.
     * Solo actualiza ESE campo, no toca el resto del documento.
     */
    suspend fun toggleProductInCloud(
        listId: String,
        productId: String,
        isPurchased: Boolean
    ): Result<Boolean>

    /** Escucha cambios en tiempo real. Emite cada vez que Firestore cambia. */
    fun observeSharedList(listId: String): Flow<Map<String, Any>>

    /** Guarda el token FCM del dispositivo en Firestore */
    suspend fun saveFcmToken(userId: String, token: String): Result<Boolean>

    /** Genera un deep link de invitación para compartir la lista */
    suspend fun generateShareLink(listId: String): Result<String>
}
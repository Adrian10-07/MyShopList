package com.example.myshoplist.features.shopping_list.framework.fcm

import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreListDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteListDataSource {

    private val sharedListsCollection = firestore.collection("shared_lists")
    private val userTokensCollection  = firestore.collection("user_tokens")

    override suspend fun syncListToCloud(listId: String, data: Map<String, Any>): Result<Boolean> {
        return try {
            sharedListsCollection.document(listId)
                .set(data, SetOptions.merge())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube los productos de Room a Firestore PRESERVANDO el estado isPurchased
     * que ya exista en la nube. Así al salir y volver a entrar los checkmarks
     * no se resetean.
     *
     * Estrategia:
     * 1. Lee el documento actual de Firestore
     * 2. Para cada producto local, preserva el isPurchased que ya esté en Firestore
     * 3. Solo escribe los productos que sean NUEVOS (no existen en Firestore aún)
     */
    override suspend fun syncProductsToCloud(
        listId: String,
        products: List<Product>
    ): Result<Boolean> {
        return try {
            // Paso 1: Leer estado actual de Firestore
            val existingDoc = sharedListsCollection.document(listId).get().await()

            @Suppress("UNCHECKED_CAST")
            val existingItems = (existingDoc.data?.get("items") as? Map<String, Any>) ?: emptyMap()

            // Paso 2: Construir mapa preservando isPurchased existente
            val itemsMap = products.associate { product ->
                val key = product.id ?: return Result.failure(Exception("Producto sin ID"))

                // Si el producto ya existe en Firestore, usa su isPurchased actual
                val existingItem  = existingItems[key] as? Map<String, Any>
                val isPurchased   = existingItem?.get("isPurchased") as? Boolean
                    ?: (product.isPurchased == 1)

                key to mapOf(
                    "name"           to product.name,
                    "category"       to product.category,
                    "estimatedPrice" to product.estimatedPrice,
                    "isPurchased"    to isPurchased
                )
            }

            sharedListsCollection.document(listId)
                .set(
                    mapOf("items" to itemsMap, "updatedAt" to System.currentTimeMillis()),
                    SetOptions.merge()
                )
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cambia isPurchased de un producto individual usando dot-notation.
     * Solo toca ese campo, nada más del documento.
     */
    override suspend fun toggleProductInCloud(
        listId: String,
        productId: String,
        isPurchased: Boolean
    ): Result<Boolean> {
        return try {
            sharedListsCollection.document(listId)
                .update("items.$productId.isPurchased", isPurchased)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finalizeSharedPurchase(listId: String): Result<Boolean> {
        return try {
            val doc = sharedListsCollection.document(listId).get().await()

            @Suppress("UNCHECKED_CAST")
            val existingItems = (doc.data?.get("items") as? Map<String, Any>) ?: emptyMap()

            // Construye el mapa con todos los isPurchased en false
            val resetItems = existingItems.mapValues { (_, value) ->
                val item = (value as? Map<String, Any>)?.toMutableMap() ?: mutableMapOf()
                item["isPurchased"] = false
                item
            }

            sharedListsCollection.document(listId)
                .update("items", resetItems)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeSharedList(listId: String): Flow<Map<String, Any>> = callbackFlow {
        val listener = sharedListsCollection.document(listId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data ?: emptyMap())
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveFcmToken(userId: String, token: String): Result<Boolean> {
        return try {
            userTokensCollection.document(userId)
                .set(mapOf("fcmToken" to token, "updatedAt" to System.currentTimeMillis()),
                    SetOptions.merge())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateShareLink(listId: String): Result<String> {
        return try {
            sharedListsCollection.document(listId)
                .set(mapOf("isPublic" to true, "sharedAt" to System.currentTimeMillis()),
                    SetOptions.merge())
                .await()
            Result.success("myshoplist://shared?listId=$listId")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
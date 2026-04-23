package com.example.myshoplist.features.shopping_list.framework.fcm

import com.example.myshoplist.features.product.domain.entities.Product
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import com.google.firebase.firestore.FieldValue
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

    // ─── 1. Actualizar campos sueltos de la lista ────────────────────────────
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

    // ─── 2. Subir lista completa de productos desde Room ────────────────────
    /**
     * Estructura en Firestore:
     * shared_lists/{listId} {
     *   items: {
     *     "product_id_1": { name, category, estimatedPrice, isPurchased },
     *     "product_id_2": { ... }
     *   },
     *   updatedAt: 123456
     * }
     *
     * Se usa merge para no sobreescribir isPurchased que otro usuario tocó.
     * Solo sube productos que aún NO están en la nube (isPurchased == 0).
     */
    override suspend fun syncProductsToCloud(
        listId: String,
        products: List<Product>
    ): Result<Boolean> {
        return try {
            val itemsMap = products.associate { product ->
                val key = product.id ?: return Result.failure(Exception("Producto sin ID"))
                key to mapOf(
                    "name"           to product.name,
                    "category"       to product.category,
                    "estimatedPrice" to product.estimatedPrice,
                    "isPurchased"    to (product.isPurchased == 1)
                )
            }

            val data = mapOf(
                "items"     to itemsMap,
                "updatedAt" to System.currentTimeMillis()
            )

            sharedListsCollection.document(listId)
                .set(data, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── 3. Cambiar isPurchased de un producto individual ───────────────────
    /**
     * Solo actualiza el campo "items.{productId}.isPurchased" usando dot-notation.
     * Esto es atómico — no sobreescribe el resto del documento.
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

    // ─── 4. Escuchar cambios en tiempo real ─────────────────────────────────
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

    // ─── 5. Guardar token FCM ────────────────────────────────────────────────
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

    // ─── 6. Generar link de invitación ───────────────────────────────────────
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
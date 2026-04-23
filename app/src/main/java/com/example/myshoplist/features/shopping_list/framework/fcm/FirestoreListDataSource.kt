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

    override suspend fun syncProductsToCloud(
        listId: String,
        products: List<Product>
    ): Result<Boolean> {
        return try {
            val existingDoc = sharedListsCollection.document(listId).get().await()

            @Suppress("UNCHECKED_CAST")
            val existingItems = (existingDoc.data?.get("items") as? Map<String, Any>) ?: emptyMap()

            val itemsMap = products.associate { product ->
                val key = product.id ?: return Result.failure(Exception("Producto sin ID"))

                val existingItem  = existingItems[key] as? Map<String, Any>
                val rawIsPurchased = existingItem?.get("isPurchased")

                // Parseo robusto del booleano
                val isPurchased = when(rawIsPurchased) {
                    is Boolean -> rawIsPurchased
                    is Number -> rawIsPurchased.toInt() == 1
                    is String -> rawIsPurchased.toBooleanStrictOrNull() ?: (product.isPurchased == 1)
                    else -> product.isPurchased == 1
                }

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

    // NUEVO: Eliminar un solo producto usando dot-notation y FieldValue.delete()
    override suspend fun deleteProductInCloud(listId: String, productId: String): Result<Boolean> {
        return try {
            sharedListsCollection.document(listId)
                .update("items.$productId", FieldValue.delete())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // MODIFICADO: Ahora limpia todos los productos en lugar de solo ponerlos en false
    override suspend fun finalizeSharedPurchase(listId: String, purchasedProductIds: List<String>): Result<Boolean> {
        return try {
            // Si no hay productos comprados, no hacemos nada y devolvemos éxito
            if (purchasedProductIds.isEmpty()) return Result.success(true)

            // Preparamos un mapa con las instrucciones de borrado para Firestore
            val updates = mutableMapOf<String, Any>()
            purchasedProductIds.forEach { id ->
                updates["items.$id"] = FieldValue.delete()
            }

            sharedListsCollection.document(listId)
                .update(updates)
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

    override suspend fun registerJoin(listId: String): Result<Boolean> {
        return try {
            sharedListsCollection.document(listId)
                .set(mapOf("lastJoinedAt" to System.currentTimeMillis()), SetOptions.merge())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
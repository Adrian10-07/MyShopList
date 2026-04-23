package com.example.myshoplist.features.shopping_list.domain.use_case

import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// Caso de Uso 1: Guardar y sincronizar lista en Firestore
// ─────────────────────────────────────────────────────────────────────────────
class SaveAndSyncListUseCase @Inject constructor(
    private val remoteDataSource: RemoteListDataSource
) {
    suspend operator fun invoke(listId: String, data: Map<String, Any>): Result<Boolean> {
        val enrichedData = data.toMutableMap().apply {
            put("updatedAt", System.currentTimeMillis())
        }
        return remoteDataSource.syncListToCloud(listId, enrichedData)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Caso de Uso 2: Observar cambios en tiempo real de una lista compartida
// ─────────────────────────────────────────────────────────────────────────────
class ObserveSharedListUseCase @Inject constructor(
    private val remoteDataSource: RemoteListDataSource
) {
    operator fun invoke(listId: String): Flow<Map<String, Any>> {
        return remoteDataSource.observeSharedList(listId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Caso de Uso 3: Guardar el token FCM del dispositivo en Firestore
// ─────────────────────────────────────────────────────────────────────────────
class SaveFcmTokenUseCase @Inject constructor(
    private val remoteDataSource: RemoteListDataSource
) {
    suspend operator fun invoke(userId: String, token: String): Result<Boolean> {
        return remoteDataSource.saveFcmToken(userId, token)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Caso de Uso 4: Generar link de invitación para compartir una lista
// ─────────────────────────────────────────────────────────────────────────────
class GenerateShareLinkUseCase @Inject constructor(
    private val remoteDataSource: RemoteListDataSource
) {
    suspend operator fun invoke(listId: String): Result<String> {
        return remoteDataSource.generateShareLink(listId)
    }
}
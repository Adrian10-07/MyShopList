package com.example.myshoplist.features.shopping_list.framework.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myshoplist.features.shopping_list.domain.repository.RemoteListDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker que sincroniza las listas pendientes con Firestore cuando hay conexión.
 * Cumple el Requisito 9: Uso de WorkManager basado en estrategias.
 *
 * Estrategia aplicada:
 *  - Solo se ejecuta si hay red disponible (NetworkType.CONNECTED)
 *  - Si falla, reintenta con BackoffPolicy.EXPONENTIAL (15s, 30s, 60s, ...)
 *  - Puede usarse en modo OneTime (cuando el usuario guarda) o Periodic (cada N minutos)
 */
@HiltWorker
class SyncListWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteDataSource: RemoteListDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val listId   = inputData.getString(KEY_LIST_ID)   ?: return Result.failure()
        val listName = inputData.getString(KEY_LIST_NAME) ?: return Result.failure()

        val data = mapOf(
            "nombre"    to listName,
            "syncedAt"  to System.currentTimeMillis()
        )

        val result = remoteDataSource.syncListToCloud(listId, data)

        return if (result.isSuccess) {
            Result.success()
        } else {
            // Si falla (sin red, error de Firestore), reintenta según BackoffPolicy
            Result.retry()
        }
    }

    companion object {
        const val KEY_LIST_ID   = "LIST_ID"
        const val KEY_LIST_NAME = "LIST_NAME"

        /**
         * Uso: Enqueue cuando el usuario guarda una lista.
         * Garantiza que aunque no haya red en ese momento, se sincronizará después.
         */
        fun buildOneTimeRequest(listId: String, listName: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SyncListWorker>()
                .setInputData(
                    workDataOf(
                        KEY_LIST_ID   to listId,
                        KEY_LIST_NAME to listName
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.SECONDS
                )
                .build()
        }

        /**
         * Uso: Sincronización periódica de fondo (cada 15 minutos como mínimo).
         * Enqueue al iniciar la app con ExistingPeriodicWorkPolicy.KEEP para no duplicar.
         */
        fun buildPeriodicRequest(): androidx.work.PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncListWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
}
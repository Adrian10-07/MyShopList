package com.example.myshoplist.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return syncRepository.sync().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },   // WorkManager reintenta automáticamente
        )
    }

    companion object {
        const val WORK_NAME = "myshoplist_sync"
    }
}
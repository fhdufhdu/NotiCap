package com.fhdufhdu.noticap.notification.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val TAG = "periodic_service"
object WorkManager {
    fun schedulePeriodicWork(context: Context) {
        // 15분마다 실행 (최소 간격)
        val periodicWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequest
                .Builder(PeriodicWorker::class.java, 30, TimeUnit.MINUTES)
                .setInitialDelay(0, TimeUnit.MILLISECONDS)
                .addTag(TAG)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "periodic_service",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
    }

    fun cancelPeriodicWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
    }
}

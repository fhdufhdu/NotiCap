package com.fhdufhdu.noticap.notification.worker

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.fhdufhdu.noticap.notification.manager.CustomNotificationListenerService

class PeriodicWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val intent = Intent(
            applicationContext,
            CustomNotificationListenerService::class.java
        )
        applicationContext.startForegroundService(intent)
        return Result.success()
    }
}

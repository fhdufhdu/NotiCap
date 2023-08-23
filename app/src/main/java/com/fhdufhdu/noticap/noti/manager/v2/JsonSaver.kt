package com.fhdufhdu.noticap.noti.manager.v2

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class JsonSaver(private val appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val kndManager = KakaoNotificationDataManager.getInstance()
        kndManager.saveJson(appContext)
        return Result.success()
    }
}
package com.fhdufhdu.noticap.noti.manager.v2

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class JsonSaver(private val appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val kndManager = KakaoNotificationDataManager.getInstance()
        Log.d("worker", "do run")
        kndManager.saveJson(appContext)
        return Result.success()
    }
}
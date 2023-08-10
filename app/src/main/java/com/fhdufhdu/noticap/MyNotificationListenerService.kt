package com.fhdufhdu.noticap

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fhdufhdu.noticap.noti.manager.v1.KtNotificationCaptureManagerV1

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val packageName: String = sbn.packageName
        if (packageName == applicationContext.packageName) {
            val ktNotificationCaptureManager = KtNotificationCaptureManagerV1.getInstance(this)
            val id: Int? = ktNotificationCaptureManager.remove(sbn)
            if (id != null){
                ktNotificationCaptureManager.clearNotification(id)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val ktNotificationCaptureManager = KtNotificationCaptureManagerV1.getInstance(this)
        var isEnableKtNotification = ktNotificationCaptureManager.getSharedPreferences().getBoolean("enable_kakao_notification", true)

        try {
            ktNotificationCaptureManager.add(sbn)
        } catch (error: KtNotificationCaptureManagerV1.NotKtNotification) { }

        if (sbn.packageName == "com.kakao.talk" && !isEnableKtNotification){
            cancelNotification(sbn.key)
//            Handler(Looper.myLooper()!!).postDelayed({
//                cancelNotification(sbn.key)
//            }, 600)
        }
    }
}
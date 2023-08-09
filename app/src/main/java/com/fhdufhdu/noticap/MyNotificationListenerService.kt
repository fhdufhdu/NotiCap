package com.fhdufhdu.noticap

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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
        var isCancleNotification = false
        var isEnableKakaoNotification = ktNotificationCaptureManager.getSharedPreferences().getBoolean("enable_kakao_notification", true)

        try {
            ktNotificationCaptureManager.add(sbn)
            isCancleNotification = true
        } catch (error: KtNotificationCaptureManagerV1.NotKtNotification) {
            if (error.type == KtNotificationCaptureManagerV1.NotKtNotification.NOT_HAVE_CONTENT) {
                isCancleNotification = true
            }
        }
        if (!isEnableKakaoNotification && isCancleNotification){
            cancelNotification(sbn.key)
        }
    }
}
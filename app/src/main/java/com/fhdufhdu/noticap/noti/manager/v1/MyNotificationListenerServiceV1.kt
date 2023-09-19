package com.fhdufhdu.noticap.noti.manager.v1

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MyNotificationListenerServiceV1 : NotificationListenerService() {

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

//        val packageName: String = sbn.packageName
//        if (packageName == applicationContext.packageName) {
//            val ktNotificationCaptureManager = KtNotificationCaptureManagerV1.getInstance(this)
//            val id: Int? = ktNotificationCaptureManager.remove(sbn)
//            if (id != null){
//                ktNotificationCaptureManager.clearNotification(id)
//            }
//        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
//        val ktNotificationCaptureManager = KtNotificationCaptureManagerV1.getInstance(this)
//
//        try {
//            ktNotificationCaptureManager.add(sbn)
//        } catch (error: KtNotificationCaptureManagerV1.NotKtNotification) { }

//        if(sbn.packageName == "com.kakao.talk"){
//            cancelNotification(sbn.key)
//        }
        if (sbn.packageName == "com.kakao.talk") {
            Log.d("number", sbn.notification.number.toString())
            val extras = sbn.notification.extras
            val title: String = extras.getString(Notification.EXTRA_TITLE) ?: "null"
            val text: String = extras.getString(Notification.EXTRA_TEXT) ?: "null"
            val subText: String = extras.getString(Notification.EXTRA_SUB_TEXT) ?: "null"
            Log.d("extra", "$title\n$text\n$subText")
        }
    }
}
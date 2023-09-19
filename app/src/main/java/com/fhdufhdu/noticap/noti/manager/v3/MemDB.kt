package com.fhdufhdu.noticap.noti.manager.v3

import android.app.PendingIntent

class MemDB {
    lateinit var pendingIntentMap: HashMap<String, PendingIntent>
    lateinit var postLastNotificationData: KakaoNotification
    lateinit var removeLastNotificationData: KakaoNotification

    companion object {
        private var instance: MemDB? = null

        fun getInstance(): MemDB {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance = MemDB().also {
                        it.pendingIntentMap = HashMap()
                        it.postLastNotificationData = KakaoNotification(
                            "empty",
                            "empty",
                            "empty",
                            "empty",
                            0,
                        )
                        it.removeLastNotificationData = KakaoNotification(
                            "empty",
                            "empty",
                            "empty",
                            "empty",
                            0,
                        )
                    }
                }
                return instance!!
            }
        }
    }
}
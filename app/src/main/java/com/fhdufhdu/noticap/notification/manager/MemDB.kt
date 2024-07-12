package com.fhdufhdu.noticap.notification.manager

import android.app.PendingIntent

class MemDB {
    lateinit var pendingIntentMap: HashMap<String, PendingIntent>

    companion object {
        private var instance: MemDB? = null

        fun getInstance(): MemDB {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance = MemDB().also {
                        it.pendingIntentMap = HashMap()
                    }
                }
                return instance!!
            }
        }
    }
}
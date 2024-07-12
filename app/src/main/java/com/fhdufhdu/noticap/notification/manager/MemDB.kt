package com.fhdufhdu.noticap.notification.manager

import android.app.PendingIntent
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

class MemDB {
    lateinit var pendingIntentMap: HashMap<String, PendingIntent>
    lateinit var personMap: HashMap<String, Person>

    companion object {
        private var instance: MemDB? = null

        fun getInstance(): MemDB {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance = MemDB().also {
                        it.pendingIntentMap = HashMap()
                        it.personMap = HashMap()
                    }
                }
                return instance!!
            }
        }
    }
}
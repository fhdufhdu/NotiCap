package com.fhdufhdu.noticap.notification.manager

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.util.IconConverter

class MemDB {
    lateinit var pendingIntentMap: HashMap<String, PendingIntent>
    lateinit var iconCompatMap: HashMap<String, IconCompat>

    companion object {
        private var instance: MemDB? = null

        fun getInstance(): MemDB {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance = MemDB().also {
                        it.pendingIntentMap = HashMap()
                        it.iconCompatMap = HashMap()
                    }
                }
                return instance!!
            }
        }
    }

    fun getIconCompat(context: Context, personKey: String?, personIcon: String?): IconCompat? {
        var icon: IconCompat? = null
        if (personKey != null) {
            if (iconCompatMap.containsKey(personKey))
                icon = iconCompatMap[personKey]
            else if (personIcon != null) {
                icon = IconCompat.createFromIcon(
                    context,
                    IconConverter.stringToIcon(personIcon)!!
                )
                iconCompatMap[personKey] = icon!!
            }
        }
        return icon
    }
}
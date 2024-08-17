package com.fhdufhdu.noticap.notification.manager

import android.app.PendingIntent
import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import com.fhdufhdu.noticap.util.IconConverter

class MemDB {
    lateinit var pendingIntentMap: HashMap<String, PendingIntent>
    lateinit var iconCompatMap: HashMap<String, Pair<String, IconCompat>>

    companion object {
        private var instance: MemDB? = null

        fun getInstance(): MemDB {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance =
                        MemDB().also {
                            it.pendingIntentMap = HashMap()
                            it.iconCompatMap = HashMap()
                        }
                }
                return instance!!
            }
        }
    }

    private fun saveIconCompat(
        context: Context,
        personKey: String,
        personIcon: String,
    ): Pair<String, IconCompat> {
        val icon =
            IconCompat.createFromIcon(
                context,
                IconConverter.stringToIcon(personIcon)!!,
            )
        val pair = Pair(personIcon, icon!!)
        iconCompatMap[personKey] = pair
        return pair
    }

    fun getOrSaveIconCompat(
        context: Context,
        personKey: String?,
        personIcon: String?,
    ): IconCompat? {
        if (personKey == null || personIcon == null) return null

        // 이미 캐시되어 있다면
        if (iconCompatMap.containsKey(personKey)) {
            val pair = iconCompatMap[personKey]!!
            val cachedPersonIcon = pair.first
            // 캐시된 값이 현재 값이랑 다르다면
            return if (cachedPersonIcon != personIcon) {
                saveIconCompat(context, personKey, personIcon).second
            } else {
                pair.second
            }
        }

        return saveIconCompat(context, personKey, personIcon).second
    }
}

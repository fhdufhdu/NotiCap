package com.fhdufhdu.noticap.noti.manager.v2;

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.preference.PreferenceManager
import com.fhdufhdu.noticap.IconBitmapConverter
import com.fhdufhdu.noticap.noti.manager.v1.KtNotificationCaptureManagerV1
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.util.PriorityQueue
import java.util.SortedMap
import java.util.SortedSet
import java.util.TreeMap
import java.util.TreeSet
import kotlin.random.Random

class NotificationDataManager {
    private val usedId: Array<Boolean> = Array(100000) { false }
    lateinit var notificationMap: HashMap<String, NotificationDataList>
    val NOTI_JSON = "NOTI_JSON"

    companion object {
        private var instance: NotificationDataManager? = null
        fun getInstance(): NotificationDataManager {
            return instance ?: synchronized(this) {
                instance ?: NotificationDataManager().also {
                    it.notificationMap = HashMap()
                    instance = it
                }
            }
        }
    }

    fun add(chatroomName: String, notificationData: NotificationData) {
        if (!notificationMap.containsKey(chatroomName)) {
            var id = 0
            do {
                id = (Math.random() * 100000).toInt()
                var existId = usedId[id]
            } while (existId)
            usedId[id] = true
            val notificationDataList = NotificationDataList(id)
            notificationMap[chatroomName] = notificationDataList
        }
        notificationMap[chatroomName]!!.addFirst(notificationData)
    }

    fun read(chatroomName: String) {
        if (notificationMap.containsKey(chatroomName)) {
            val notificationDataList = notificationMap[chatroomName]!!
            notificationDataList.unreadCount = 0
            for (it in notificationDataList) {
                if (!it.unread) break
                it.unread = false
            }
        }
    }

    fun getUnreadCount(chatroomName: String): Int {
        var cnt = 0
        if (notificationMap.containsKey(chatroomName)) {
            cnt = notificationMap[chatroomName]!!.unreadCount
        }
        return cnt
    }

    fun getUnreadChatroomNames(): ArrayList<String> {
        val result = ArrayList<String>()

        val chatroomNames = getSortedChatroomNames()
        val dataLists = getSortedDataLists()
        for (idx in 0 until notificationMap.size) {
            if (dataLists[idx].unreadCount != 0) {
                result.add("${chatroomNames[idx]}(${dataLists[idx].unreadCount})")
            }
        }
        return result
    }

    fun get(chatroomName: String, position: Int): NotificationData? {
        if (notificationMap.containsKey(chatroomName)) {
            val notificationDataList = notificationMap[chatroomName]!!
            return notificationDataList[position]

        }
        return null
    }

    fun getSortedDataLists(): ArrayList<NotificationDataList> {
        return ArrayList(notificationMap.values.sortedWith { a, b ->
            (b.lastNotificationTime - a.lastNotificationTime).toInt()
        })
    }

    fun getSortedChatroomNames(): ArrayList<String> {
        return ArrayList(notificationMap.keys.sortedWith { a, b ->
            (notificationMap[b]!!.lastNotificationTime - notificationMap[a]!!.lastNotificationTime).toInt()
        })
    }

    fun deleteChatroom(context: Context, chatroomName: String) {
        notificationMap.remove(chatroomName)
        var broadcastIntent = Intent()
        broadcastIntent.action = MyNotificationListenerServiceV2.ACTION_NAME
        context.sendBroadcast(broadcastIntent)
    }

    @SuppressLint("CommitPrefEdits")
    fun saveJson(context: Context){
        val jsonMap = JSONObject()
        notificationMap.forEach {
            val chatroomName = it.key
            val notificationDataList = it.value

            val jsonListData = JSONObject()
            jsonListData.put("id", notificationDataList.id)
            jsonListData.put("lastNotificationTime", notificationDataList.lastNotificationTime)
            jsonListData.put("unreadCount", notificationDataList.unreadCount)

            val jsonList = JSONArray()
            notificationDataList.forEach { iit ->
                val jsonData = JSONObject()
                jsonData.put("title", iit.title)
                jsonData.put("text", iit.text)
                jsonData.put("subText", iit.subText)
                if (iit.personIcon != null) {
                    jsonData.put(
                        "personIcon",
                        IconBitmapConverter.iconToString(iit.personIcon, context)
                    )
                }
                jsonData.put("time", iit.time)
                jsonData.put("pendingIntent", null)
                jsonData.put("unread", iit.unread)
                jsonData.put("doRunAnimation", iit.doRunAnimation)
                jsonList.put(jsonData)
            }

            jsonListData.put("list", jsonList)
            jsonMap.put(chatroomName, jsonListData)
        }

        Log.d("json", jsonMap.toString())

        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(NOTI_JSON, jsonMap.toString())
        editor.apply()
    }

    fun loadJson(context: Context){
        if(notificationMap.size == 0){
            val json =
                PreferenceManager.getDefaultSharedPreferences(context).getString(NOTI_JSON, null)
                    ?: return
            val jsonMap = JSONObject(json)
            jsonMap.keys().forEach {
                val jsonListData = jsonMap.getJSONObject(it)
                val jsonList = jsonListData.getJSONArray("list")

                val notificationDataList = NotificationDataList(jsonListData.getInt("id"))
                notificationDataList.lastNotificationTime = jsonListData.getLong("lastNotificationTime")
                notificationDataList.unreadCount = jsonListData.getInt("unreadCount")

                for(idx in 0 until jsonList.length()){
                    val jsonData = jsonList.getJSONObject(idx)
                    val notificationData = NotificationData(
                        jsonData.getString("title"),
                        jsonData.getString("text"),
                        jsonData.optString("subText", null),
                        IconBitmapConverter.stringToIcon(jsonData.optString("personIcon", null)),
                        jsonData.getLong("time"),
                        null
                    )
                    notificationData.unread = jsonData.getBoolean("unread")
                    notificationData.doRunAnimation = jsonData.getBoolean("doRunAnimation")
                    notificationDataList.add(notificationData)
                }

                notificationMap[it] = notificationDataList
            }
        }
    }
}

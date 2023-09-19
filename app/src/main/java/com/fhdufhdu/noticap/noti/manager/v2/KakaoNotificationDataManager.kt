package com.fhdufhdu.noticap.noti.manager.v2;

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fhdufhdu.noticap.util.IconConverter
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.PriorityQueue
import java.util.concurrent.TimeUnit

class KakaoNotificationDataManager {
    private val usedId: Array<Boolean> = Array(100000) { false }
    lateinit var notificationMap: HashMap<String, KakaoNotificationDataList>
    val NOTI_JSON = "NOTI_JSON"
    val JSON_FILE_NAME = "kakao-noti.json"

    companion object {
        private var instance: KakaoNotificationDataManager? = null
        fun getInstance(): KakaoNotificationDataManager {
            return instance ?: synchronized(this) {
                instance ?: KakaoNotificationDataManager().also {
                    it.notificationMap = HashMap()
                    instance = it
                }
            }
        }
    }

    fun add(chatroomName: String, kakaoNotificationData: KakaoNotificationData) {
        if (!notificationMap.containsKey(chatroomName)) {
            var id = 0
            do {
                id = (Math.random() * 100000).toInt()
                var existId = usedId[id]
            } while (existId)
            usedId[id] = true
            val kakaoNotificationDataList = KakaoNotificationDataList(id)
            notificationMap[chatroomName] = kakaoNotificationDataList
        }
        notificationMap[chatroomName]!!.addFirst(kakaoNotificationData)
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

    fun get(chatroomName: String, position: Int): KakaoNotificationData? {
        if (notificationMap.containsKey(chatroomName)) {
            val notificationDataList = notificationMap[chatroomName]!!
            return notificationDataList[position]
        }
        return null
    }

    fun getSortedDataLists(): ArrayList<KakaoNotificationDataList> {
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
        broadcastIntent.action = CustomNotificationListenerService.ACTION_NAME
        context.sendBroadcast(broadcastIntent)
        saveJson(context)
    }

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
                        IconConverter.iconToString(iit.personIcon, context)
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

        context.openFileOutput(JSON_FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(jsonMap.toString().toByteArray())
        }

        Log.d("save json", jsonMap.toString())
    }

    fun loadJson(context: Context){
        if(notificationMap.size == 0){
            try {
                val json = context.openFileInput(JSON_FILE_NAME).bufferedReader().useLines {
                    it.fold(""){a, b -> "$a\n$b"}
                }
                Log.d("load json", json)
                val jsonMap = JSONObject(json)
                jsonMap.keys().forEach {
                    val jsonListData = jsonMap.getJSONObject(it)
                    val jsonList = jsonListData.getJSONArray("list")

                    val kakaoNotificationDataList = KakaoNotificationDataList(jsonListData.getInt("id"))
                    kakaoNotificationDataList.lastNotificationTime = jsonListData.getLong("lastNotificationTime")
                    kakaoNotificationDataList.unreadCount = jsonListData.getInt("unreadCount")

                    for(idx in 0 until jsonList.length()){
                        val jsonData = jsonList.getJSONObject(idx)
                        val kakaoNotificationData = KakaoNotificationData(
                            jsonData.getString("title"),
                            jsonData.getString("text"),
                            jsonData.optString("subText", null),
                            IconConverter.stringToIcon(jsonData.optString("personIcon", null)),
                            jsonData.getLong("time"),
                            null
                        )
                        kakaoNotificationData.unread = jsonData.getBoolean("unread")
                        kakaoNotificationData.doRunAnimation = jsonData.getBoolean("doRunAnimation")
                        kakaoNotificationDataList.add(kakaoNotificationData)
                    }

                    notificationMap[it] = kakaoNotificationDataList
                }
                Log.d("load json", json)
            }
            catch (_: FileNotFoundException){ }
        }
    }
}

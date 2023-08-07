package com.example.noticap;

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Queue
import java.util.Stack
import kotlin.collections.ArrayList

class KakaoTalkNotiManager{
    private var notiIdList: Array<Boolean> = Array(100000) { false }
    var notiMap: HashMap<String, KakaoTalkNoti> = HashMap()

    companion object {
        private var instance: KakaoTalkNotiManager? = null

        fun getInstance(): KakaoTalkNotiManager {
            return instance ?: synchronized(this) {
                instance ?: KakaoTalkNotiManager().also {
                    instance = it
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun addNoti(chatroomName: String, sender: String, text: String){
        if (notiMap[chatroomName] == null) {
            var id: Int = 0
            do {
                id = (Math.random() * 100000).toInt()
                var existId = notiIdList[id]
            } while (existId)
            notiIdList[id] = true
            notiMap[chatroomName] = KakaoTalkNoti(id, chatroomName)
        }

        notiMap[chatroomName]!!.addText(sender, text)
    }

    public fun getNotiId(chatroomName: String): Int?{
        if (notiMap[chatroomName] == null) {
            return null
        }
        return notiMap[chatroomName]!!.id
    }
    public fun removeNoti(chatroomName: String){
        if (notiMap[chatroomName] == null) {
           return
        }
        notiMap[chatroomName]!!.textList.clear()
    }
}

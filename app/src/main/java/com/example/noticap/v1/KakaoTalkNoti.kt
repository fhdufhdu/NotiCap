package com.example.noticap.v1;

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class KakaoTalkNoti {
    class KakaoTalkNotiText constructor(subTitle: String, text: String) {
        val time: String = SimpleDateFormat("HH:mm").format(System.currentTimeMillis())
        var sender: String = subTitle
        var text: String = text
    }

    val id: Int
    val chatroomName: String
    val textList: ArrayDeque<KakaoTalkNotiText> = ArrayDeque()

    constructor(id: Int, chatroomName: String) {
        this.id = id
        this.chatroomName = chatroomName
    }

    public fun addText(sender: String, text: String, maxSize: Int) {
        while (textList.size > 15) {
            textList.removeLast()
        }
        textList.addFirst(KakaoTalkNotiText(sender, text))
    }
}
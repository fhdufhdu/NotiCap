package com.example.noticap;

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class KakaoTalkNoti {
    class KakaoTalkNotiText constructor(subTitle: String, text: String) {
        @RequiresApi(Build.VERSION_CODES.O)
        val time: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        var sender: String = subTitle
        var text: String = text
    }
    val id: Int
    val chatroomName: String
    val textList: ArrayDeque<KakaoTalkNotiText> = ArrayDeque()

    constructor(id: Int, chatroomName: String){
        this.id = id
        this.chatroomName = chatroomName
    }

    public fun addText(sender:String, text: String){
        while(textList.size > 15){
            textList.removeLast()
        }
        textList.addFirst(KakaoTalkNotiText(sender, text))
    }
}
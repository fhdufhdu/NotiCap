package com.fhdufhdu.noticap.noti.manager.v1;

import java.text.SimpleDateFormat

class KtNotificationDataV1 {
    class KtNotificationText constructor(subTitle: String, text: String) {
        val time: Long = System.currentTimeMillis()
        val timeString: String = SimpleDateFormat("HH:mm").format(time)
        var sender: String = subTitle
        var text: String = text
    }

    var isQuiet: Boolean = false
    val id: Int
    val chatroomName: String
    val textList: ArrayDeque<KtNotificationText> = ArrayDeque()

    constructor(id: Int, chatroomName: String) {
        this.id = id
        this.chatroomName = chatroomName
    }

    fun addText(sender: String, text: String, maxSize: Int) {
        while (textList.size > maxSize) {
            textList.removeLast()
        }
        textList.addFirst(KtNotificationText(sender, text))
    }
}
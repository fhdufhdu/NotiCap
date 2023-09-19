package com.fhdufhdu.noticap.noti.manager.v2

import java.util.LinkedList

class KakaoNotificationDataList(var id: Int) : LinkedList<KakaoNotificationData>() {
    var lastNotificationTime: Long = System.currentTimeMillis()
    var unreadCount: Int = 0

    override fun addFirst(element: KakaoNotificationData) {
        lastNotificationTime = element.time
        unreadCount += 1
        super.addFirst(element)
    }
}
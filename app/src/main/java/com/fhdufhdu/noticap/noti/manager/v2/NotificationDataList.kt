package com.fhdufhdu.noticap.noti.manager.v2;

import android.graphics.drawable.Icon
import android.os.Parcel
import android.os.Parcelable
import com.fhdufhdu.noticap.noti.manager.v1.KtNotificationCaptureManagerV1
import java.util.LinkedList
import java.util.Queue

class NotificationDataList(var id: Int) : LinkedList<NotificationData>(){
    var lastNotificationTime: Long = System.currentTimeMillis()
    var unreadCount: Int = 0

    override fun addFirst(element: NotificationData) {
        lastNotificationTime = element.time
        unreadCount += 1
        super.addFirst(element)
    }
}
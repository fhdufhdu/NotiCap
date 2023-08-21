package com.fhdufhdu.noticap.noti.manager.v2;

import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.os.Parcel
import android.os.Parcelable

class NotificationData(
    val title: String,
    val text: String,
    val subText: String?,
    val personIcon: Icon?,
    val time: Long,
    val pendingIntent: PendingIntent?,
) {
    var unread: Boolean = true
    var doRunAnimation: Boolean = true

    fun getChatroomName(): String {
        return subText ?: title
    }
}
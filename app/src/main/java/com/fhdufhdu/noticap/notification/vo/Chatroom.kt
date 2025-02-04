package com.fhdufhdu.noticap.notification.vo

import android.app.Notification
import android.os.Bundle
import com.fhdufhdu.noticap.notification.UnusableNotificationException
import kotlinx.coroutines.channels.Channel

@JvmInline
value class Chatroom(val name: String) {
    companion object {
        fun from(notificationExtras: Bundle): Chatroom {
            val title = notificationExtras.getString(Notification.EXTRA_TITLE)
            val subText = notificationExtras.getString(Notification.EXTRA_SUB_TEXT)
            return Chatroom(subText ?: title ?: throw UnusableNotificationException("채팅방 이름 확인 불가"))
        }
    }

    override fun toString(): String = name
}

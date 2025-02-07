package com.fhdufhdu.catchtalk.notification.vo

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.fhdufhdu.catchtalk.notification.UnusableNotificationException
import com.fhdufhdu.catchtalk.util.ExtensionFunctions.isQuiet
import java.io.Serializable

data class Chatroom(val name: String, val notificationKey: String, val quiet: Boolean) : Serializable {
    companion object {
        fun from(sbn: StatusBarNotification): Chatroom {
            val notification = sbn.notification
            val title = notification.extras.getString(Notification.EXTRA_TITLE)
            val subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT)
            return Chatroom(subText ?: title ?: throw UnusableNotificationException("채팅방 이름 확인 불가"), sbn.key, notification.isQuiet())
        }
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (other is Chatroom) {
            return this.name == other.name
        }
        return false
    }

    override fun hashCode(): Int = this.name.hashCode()
}

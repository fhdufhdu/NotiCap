package com.fhdufhdu.catchtalk.notification.vo

import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.fhdufhdu.catchtalk.notification.UnusableNotificationException

data class Conversation(
    val chatroom: Chatroom,
    val sender: Sender,
    val content: Content,
    val time: Time,
    val intent: PendingIntent,
) {
    companion object {
        fun fromKakao(notification: Notification): Conversation {
            val extras = notification.extras
            val person =
                NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
                    ?.messages
                    ?.get(0)
                    ?.person ?: throw UnusableNotificationException("발신자 없는 알림")

            val chatroom = Chatroom.from(extras)
            val sender = Sender.from(person)
            val content = Content.from(extras)
            val time = Time.from(notification)
            val intent = notification.contentIntent

            return Conversation(chatroom, sender, content, time, intent)
        }
    }
}

package com.fhdufhdu.catchtalk.notification.strategy

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fhdufhdu.catchtalk.notification.CatchTalkNotificationManager
import com.fhdufhdu.catchtalk.notification.ConversationRepository
import com.fhdufhdu.catchtalk.notification.UnusableNotificationException
import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import com.fhdufhdu.catchtalk.notification.vo.Conversation
import com.fhdufhdu.catchtalk.util.Const

class RemoveKakaoTalkNotificationStrategy(
    private val service: NotificationListenerService,
) : NotificationProcessingStrategy {
    private val catchTalkNotificationManager = CatchTalkNotificationManager(service)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == Const.KAKAOTALK_PACKAGE_NAME) {
                val conversation = Conversation.fromKakao(sbn)
                ConversationRepository.add(conversation)

                catchTalkNotificationManager.sendNotification(conversation.chatroom)
                // 카카오톡 알림은 제거
                service.cancelNotification(sbn.key)
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == service.packageName) {
                val chatroom = Chatroom.from(sbn)
                ConversationRepository.clear(chatroom)
                catchTalkNotificationManager.sendNotification(chatroom)
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }
}

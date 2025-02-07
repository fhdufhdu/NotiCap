package com.fhdufhdu.catchtalk.notification.strategy

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fhdufhdu.catchtalk.notification.CatchTalkNotificationManager
import com.fhdufhdu.catchtalk.notification.ConversationRepository
import com.fhdufhdu.catchtalk.notification.LastChatroomRepository
import com.fhdufhdu.catchtalk.notification.UnusableNotificationException
import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import com.fhdufhdu.catchtalk.notification.vo.Conversation
import com.fhdufhdu.catchtalk.util.Const

class NormalNotificationProcessingStrategy(
    private val service: NotificationListenerService,
) : NotificationProcessingStrategy {
    private val catchTalkNotificationManager = CatchTalkNotificationManager(service)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == Const.KAKAOTALK_PACKAGE_NAME) {
                val conversation = Conversation.fromKakao(sbn)
                ConversationRepository.add(conversation)
                LastChatroomRepository.add(conversation.chatroom)

                catchTalkNotificationManager.sendNotification(conversation.chatroom)
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == Const.KAKAOTALK_PACKAGE_NAME) {
                // 카카오톡 알림이 제거되는 경우 Noticap 알림도 제거한다.
                val chatroom = Chatroom.from(sbn)

                ConversationRepository.clear(chatroom)

                catchTalkNotificationManager.sendNotification(chatroom)
            } else if (sbn.packageName == service.packageName) {
                // CatchTalk 알림만 제거되는 경우 카카오톡 알림도 지운다.
                val chatroom = Chatroom.from(sbn)
                val lastChatroom = LastChatroomRepository.get(chatroom)
//                // CatchTalk 알림만 제거되는 경우 다시 알림을 살린다.
//                catchTalkNotificationManager.sendNotification(chatroom)

                if (lastChatroom != null) {
                    service.cancelNotification(lastChatroom.notificationKey)
                }
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }
}

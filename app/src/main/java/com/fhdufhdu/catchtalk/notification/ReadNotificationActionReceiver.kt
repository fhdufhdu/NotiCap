package com.fhdufhdu.catchtalk.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fhdufhdu.catchtalk.notification.vo.Chatroom

private const val DEFAULT_NOTIFICATION_ID = -10000

class ReadNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == READ_NOTIFICATION_ACTION) {
            val chatroom =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(CHATROOM_KEY, Chatroom::class.java)
                } else {
                    intent.getSerializableExtra(CHATROOM_KEY) as Chatroom
                }
            val notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, DEFAULT_NOTIFICATION_ID)

            if (chatroom == null || notificationId == DEFAULT_NOTIFICATION_ID) return

            ConversationRepository.clear(chatroom)

            val manager = CatchTalkNotificationManager(context)
            manager.sendNotification(chatroom)
        }
    }

    companion object {
        const val READ_NOTIFICATION_ACTION = "READ_NOTIFICATION"
        const val NOTIFICATION_ID_KEY = "NOTIFICATION_ID"
        const val CHATROOM_KEY = "CHATROOM"
    }
}

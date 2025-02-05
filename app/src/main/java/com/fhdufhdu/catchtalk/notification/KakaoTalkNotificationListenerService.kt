package com.fhdufhdu.catchtalk.notification

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import com.fhdufhdu.catchtalk.notification.vo.Conversation

private const val KAKAOTALK_PACKAGE_NAME = "com.kakao.talk"
private const val FOREGROUND_NOTIFICATION_ID = 100000000

class KakaoTalkNotificationListenerService : NotificationListenerService() {
    private var kakaoTalkNotificationManager: KakaoTalkNotificationManager? = null

    private fun initNotificationSender() {
        if (kakaoTalkNotificationManager == null) {
            kakaoTalkNotificationManager = KakaoTalkNotificationManager(this)
        }
    }

    override fun onCreate() {
        initNotificationSender()
        startForeground()
        super.onCreate()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        return START_STICKY
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val notification = sbn.notification
        val extras = sbn.notification.extras

        // foreground 알림인 경우 다시 생성한다.
        if (notification.channelId == KakaoTalkNotificationManager.FOREGROUND_CHANNEL_ID) {
            startForeground()
            return
        }

        try {
            if (nPackageName == KAKAOTALK_PACKAGE_NAME) {
                // 카카오톡 알림이 제거되는 경우 Noticap 알림도 제거한다.
                val chatroom = Chatroom.from(extras)

                ConversationRepository.clear(chatroom)

                kakaoTalkNotificationManager?.sendNotification(chatroom)
            } else if (nPackageName == packageName) {
                // Noticap 알림만 제거되는 경우 다시 알림을 살린다.
                val chatroom = Chatroom.from(extras)
                kakaoTalkNotificationManager?.sendNotification(chatroom)
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val notification = sbn.notification

        try {
            if (nPackageName == KAKAOTALK_PACKAGE_NAME) {
                val conversation = Conversation.fromKakao(notification)
                ConversationRepository.add(conversation)

                kakaoTalkNotificationManager?.sendNotification(conversation.chatroom)
            }
        } catch (exception: UnusableNotificationException) {
            Log.d(this.javaClass.name, exception.toString())
        }
    }

    private fun startForeground() {
        kakaoTalkNotificationManager?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    FOREGROUND_NOTIFICATION_ID,
                    it.getForegroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(FOREGROUND_NOTIFICATION_ID, it.getForegroundNotification())
            }
        }
    }
}

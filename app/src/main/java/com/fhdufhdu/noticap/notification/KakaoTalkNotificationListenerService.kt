package com.fhdufhdu.noticap.notification

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.notification.vo.Chatroom
import com.fhdufhdu.noticap.notification.vo.Conversation
import com.fhdufhdu.noticap.notification.vo.NotificationTitle

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
        val extras = sbn.notification.extras

        try {
            if (nPackageName == KAKAOTALK_PACKAGE_NAME) {
                val notificationTitle = NotificationTitle.from(extras)
                val chatroom = Chatroom.from(notificationTitle)

                ConversationRepository.clear(chatroom)

                kakaoTalkNotificationManager?.sendNotification(chatroom)
            } else if (nPackageName == packageName) {
                val notificationTitle = NotificationTitle.from(extras)
                val foregroundTitle =
                    NotificationTitle(getString(R.string.foreground_notification_title))

                if (notificationTitle == foregroundTitle) {
                    startForeground()
                    return
                }

                val chatroom = Chatroom.from(notificationTitle)
                ConversationRepository.clear(chatroom)
            }
        } catch (exception: UnusableNotificationException) {
            Log.e("사용할 수 없는 알림", exception.toString())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val notification = sbn.notification

        if (nPackageName == KAKAOTALK_PACKAGE_NAME) {
            try {
                val conversation = Conversation.fromKakao(notification)
                ConversationRepository.add(conversation)

                kakaoTalkNotificationManager?.sendNotification(conversation.chatroom)
            } catch (exception: UnusableNotificationException) {
                Log.e("사용할 수 없는 알림", exception.toString())
            }
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

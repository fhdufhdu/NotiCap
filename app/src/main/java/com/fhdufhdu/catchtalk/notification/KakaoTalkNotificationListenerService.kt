package com.fhdufhdu.catchtalk.notification

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.fhdufhdu.catchtalk.notification.strategy.NormalNotificationProcessingStrategy
import com.fhdufhdu.catchtalk.notification.strategy.NotificationProcessingStrategy
import com.fhdufhdu.catchtalk.notification.strategy.RemoveKakaoTalkNotificationStrategy
import com.fhdufhdu.catchtalk.util.SharedPreferenceManager

private const val KAKAOTALK_PACKAGE_NAME = "com.kakao.talk"
private const val FOREGROUND_NOTIFICATION_ID = 100000000

class KakaoTalkNotificationListenerService : NotificationListenerService() {
    private var initFlag = false
    private lateinit var catchTalkNotificationManager: CatchTalkNotificationManager
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private fun init() {
        if (!initFlag) {
            catchTalkNotificationManager = CatchTalkNotificationManager(this)
            sharedPreferenceManager = SharedPreferenceManager(this)
            initFlag = true
        }
    }

    override fun onCreate() {
        init()
        startForeground()
        super.onCreate()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        startForeground()
        return START_STICKY
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        // foreground 알림인 경우 다시 생성한다.
        if (sbn.notification.channelId == CatchTalkNotificationManager.FOREGROUND_CHANNEL_ID) {
            startForeground()
            return
        }

        val strategy = getProcessingStrategy()
        strategy.onNotificationRemoved(sbn)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val strategy = getProcessingStrategy()
        strategy.onNotificationPosted(sbn)
    }

    private fun startForeground() {
        catchTalkNotificationManager.let {
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

    private fun getProcessingStrategy(): NotificationProcessingStrategy {
        return when {
            sharedPreferenceManager.isRemoveKakaoNoti() -> RemoveKakaoTalkNotificationStrategy(this)
            else -> NormalNotificationProcessingStrategy(this)
        }
    }
}

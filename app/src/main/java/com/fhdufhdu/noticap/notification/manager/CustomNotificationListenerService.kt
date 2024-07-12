package com.fhdufhdu.noticap.notification.manager

import android.app.Notification
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.util.CoroutineManager
import com.fhdufhdu.noticap.util.IconConverter


class CustomNotificationListenerService : NotificationListenerService() {
    private var kakaoNotificationSender: KakaoNotificationSender? = null

    private fun initNotificationSender() {
        kakaoNotificationSender = kakaoNotificationSender ?: KakaoNotificationSender(this)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras

        if (nPackageName == "com.kakao.talk") {
            val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return
            val chatroomName = notificationInfo.first

            CoroutineManager.run {
                kakaoNotificationSender?.updateReadStatusAndSendNotification(chatroomName)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras

        Log.d("package", nPackageName)
        if (nPackageName == "com.kakao.talk") {
            val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return
            val chatroomName = notificationInfo.first
            val notificationData = notificationInfo.second

            MemDB.getInstance().pendingIntentMap[chatroomName] = sbn.notification.contentIntent

            CoroutineManager.run {
                kakaoNotificationSender?.addAndSendNotification(notificationData)
            }
        }
    }

    private fun getNotificationInfo(
        notification: Notification,
        extras: Bundle
    ): Pair<String, KakaoNotificationEntity>? {
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return null
        val text = extras.getString(Notification.EXTRA_TEXT) ?: return null
        val subText = extras.getString(Notification.EXTRA_SUB_TEXT)
        val personList =
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)?.messages
        var personIcon: Icon? = null
        if (personList != null && personList.size > 0) {
            personIcon = personList[0].person?.icon?.toIcon(this)
        }
        val time = notification.`when`
        val pendingIntent = notification.contentIntent
        val chatroomName = subText ?: title

        val pendingIntentMap = MemDB.getInstance().pendingIntentMap
        pendingIntentMap[chatroomName] = pendingIntent

        val kakaoNotification = KakaoNotificationEntity(
            "${chatroomName}_${time}",
            chatroomName,
            title,
            text,
            IconConverter.iconToString(personIcon, this),
            time,
        )

        return Pair(chatroomName, kakaoNotification)
    }
}
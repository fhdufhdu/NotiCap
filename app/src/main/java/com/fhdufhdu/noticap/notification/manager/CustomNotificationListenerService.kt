package com.fhdufhdu.noticap.notification.manager

import android.app.Notification
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
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
            val chatroomName = getChatroomName(extras) ?: return

            CoroutineManager.run {
                kakaoNotificationSender?.updateReadStatusAndSendNotification(chatroomName)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        initNotificationSender()

        val nPackageName = sbn.packageName
        val notification = sbn.notification
        val extras = sbn.notification.extras

        if (nPackageName == "com.kakao.talk") {
            val chatroomName = getChatroomName(extras)?:return
            val kakaoNotificationEntity = getKakaoNotificationEntity(notification, extras)?:return

            val memDB = MemDB.getInstance()
            if (!memDB.pendingIntentMap.containsKey(chatroomName))
                memDB.pendingIntentMap[chatroomName] = notification.contentIntent

            CoroutineManager.run {
                kakaoNotificationSender?.addAndSendNotification(kakaoNotificationEntity)
            }
        }
    }

    private fun getKakaoNotificationEntity(
        notification: Notification,
        extras: Bundle
    ): KakaoNotificationEntity? {
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return null
        val text = extras.getString(Notification.EXTRA_TEXT) ?: return null
        val person = getPerson(notification)
        val personKey: String? = person?.key
        val personIcon: String? = IconConverter.iconToString(person?.icon?.toIcon(this), this)
        val time = notification.`when`
        val pendingIntent = notification.contentIntent
        val chatroomName = getChatroomName(extras)?: return null

        val pendingIntentMap = MemDB.getInstance().pendingIntentMap
        pendingIntentMap[chatroomName] = pendingIntent

        return KakaoNotificationEntity(
            "${chatroomName}_${time}",
            chatroomName,
            title,
            text,
            personKey,
            personIcon,
            time,
        )
    }

    private fun getChatroomName(extras: Bundle):String? {
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return null
        val subText = extras.getString(Notification.EXTRA_SUB_TEXT)
        return subText ?: title
    }

    private fun getPerson(notification: Notification,): Person? {
        val personList =
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)?.messages

        return personList?.get(0)?.person
    }
}
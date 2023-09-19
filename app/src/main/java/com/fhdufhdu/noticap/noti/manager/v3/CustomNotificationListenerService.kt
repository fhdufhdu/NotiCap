package com.fhdufhdu.noticap.noti.manager.v3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.ui.main.MainActivity
import com.fhdufhdu.noticap.util.CoroutineManager
import com.fhdufhdu.noticap.util.IconConverter


class CustomNotificationListenerService : NotificationListenerService() {
    companion object {
        val CHANNEL_ID = "CAPTURE"
        val ACTION_NAME = "UPDATE_NOTI_CAP"
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras

        val memDB = MemDB.getInstance()
        if (nPackageName == "com.kakao.talk") {
            synchronized(memDB.removeLastNotificationData) {
                val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return

                val chatroomName = notificationInfo.first
                val notificationData = notificationInfo.second
                val dao =
                    KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

                if (
                    memDB.removeLastNotificationData.chatroomName == notificationData.chatroomName
                    && memDB.removeLastNotificationData.content == notificationData.content
                    && memDB.removeLastNotificationData.time == notificationData.time
                ) {
                    return
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("CHATROOM_NAME", chatroomName)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    this, 1026, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val notificationManager = createNotificationChannel()
                CoroutineManager.run {
                    dao.updateRead(chatroomName)
                    val unreadChatroomNames = dao.selectUnreadChatrooms().map {
                        "${it.chatroomName}(${it.unreadCount})"
                    }
                    if (unreadChatroomNames.isEmpty()) {
                        notificationManager.cancel(1026)
                    } else {
                        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(
                                IconCompat.createWithResource(
                                    this,
                                    R.drawable.ic_notification
                                )
                            )
                            .setContentTitle("새로운 카카오톡 알림")
                            .setContentText(
                                unreadChatroomNames.joinToString(", ")
                            )
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)

                        notificationManager.notify(1026, builder.build())
                    }
                }
                memDB.removeLastNotificationData = notificationData
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras

        val memDB = MemDB.getInstance()
        if (nPackageName == "com.kakao.talk") {
            synchronized(memDB.postLastNotificationData) {
                val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return

                val chatroomName = notificationInfo.first
                val notificationData = notificationInfo.second
                val dao =
                    KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

                if (
                    memDB.postLastNotificationData.chatroomName == notificationData.chatroomName
                    && memDB.postLastNotificationData.content == notificationData.content
                    && memDB.postLastNotificationData.time == notificationData.time
                ) {
                    return
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("CHATROOM_NAME", chatroomName)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    this, 1026, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val notificationManager = createNotificationChannel()

                CoroutineManager.run {
                    dao.insertNotification(notificationData)
                    val unreadChatroomNames = dao.selectUnreadChatrooms().map {
                        "${it.chatroomName}(${it.unreadCount})"
                    }
                    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                            IconCompat.createWithResource(
                                this,
                                R.drawable.ic_notification
                            )
                        )
                        .setContentTitle("새로운 카카오톡 알림")
                        .setContentText(unreadChatroomNames.joinToString(", "))
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                    notificationManager.notify(1026, builder.build())
                }

                memDB.postLastNotificationData = notificationData
            }
        }
    }

    private fun getNotificationInfo(
        notification: Notification,
        extras: Bundle
    ): Pair<String, KakaoNotification>? {
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

        val kakaoNotification = KakaoNotification(
            chatroomName,
            title,
            text,
            IconConverter.iconToString(personIcon, this),
            time,
        )

        return Pair(chatroomName, kakaoNotification)
    }

    private fun createNotificationChannel(): NotificationManager {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return notificationManager
    }
}
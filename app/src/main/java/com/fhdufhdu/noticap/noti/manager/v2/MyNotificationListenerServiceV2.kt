package com.fhdufhdu.noticap.noti.manager.v2

import android.annotation.SuppressLint
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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import com.fhdufhdu.noticap.MainActivity
import com.fhdufhdu.noticap.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.concurrent.fixedRateTimer

class MyNotificationListenerServiceV2 : NotificationListenerService() {
    companion object {
        val CHANNEL_ID = "CAPTURE"
        val ACTION_NAME = "UPDATE_NOTI_CAP"
        val DATA_NAME = "NOTI_DATA"
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras

        if (nPackageName == "com.kakao.talk") {
            val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return

            val chatroomName = notificationInfo.first

            val notificationDataManager = NotificationDataManager.getInstance()

            notificationDataManager.read(chatroomName)

            var broadcastIntent = Intent()
            broadcastIntent.action = ACTION_NAME
            sendBroadcast(broadcastIntent)

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CHATROOM_NAME", chatroomName)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 1026, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val notificationManager = createNotificationChannel()
            val unreadChatroomName = notificationDataManager.getUnreadChatroomNames()
            if (unreadChatroomName.size == 0) {
                notificationManager.cancel(1026)
            } else {
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(IconCompat.createWithResource(this, R.drawable.ic_notification))
                    .setContentTitle("새로운 카카오톡 알림")
                    .setContentText(
                        notificationDataManager.getUnreadChatroomNames().joinToString(", ")
                    )
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(1026, builder.build())
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val nPackageName = sbn.packageName
        val extras = sbn.notification.extras


        if (nPackageName == "com.kakao.talk") {
            val notificationInfo = getNotificationInfo(sbn.notification, extras) ?: return

            val chatroomName = notificationInfo.first
            val notificationData = notificationInfo.second

            val notificationDataManager = NotificationDataManager.getInstance()

            notificationDataManager.add(chatroomName, notificationData)

            var broadcastIntent = Intent()
            broadcastIntent.action = ACTION_NAME
            sendBroadcast(broadcastIntent)

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CHATROOM_NAME", chatroomName)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 1026, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            val notificationManager = createNotificationChannel()
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(IconCompat.createWithResource(this, R.drawable.ic_notification))
                .setContentTitle("새로운 카카오톡 알림")
                .setContentText(notificationDataManager.getUnreadChatroomNames().joinToString(", "))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
//                .setOngoing(true)
            notificationManager.notify(1026, builder.build())

            notificationDataManager.saveJson(this)
        }
    }

    private fun getNotificationInfo(
        notification: Notification,
        extras: Bundle
    ): Pair<String, NotificationData>? {
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
        val notificationData = NotificationData(
            title,
            text,
            subText,
            personIcon,
            time,
            pendingIntent
        )

        return Pair(chatroomName, notificationData)
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
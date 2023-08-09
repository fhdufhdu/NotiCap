package com.example.noticap.v2

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.noticap.R
import com.gun0912.tedpermission.normal.TedPermission

class MyNotiListenerService : NotificationListenerService() {
    private val TAG = "MyNotificationListenerService"
    private final val CHANNEL_ID = "NOTI_CAP"
    val KEY_TEXT_REPLY = "key_text_reply"

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras
        val extraTitle: String? = extras.getString(Notification.EXTRA_TITLE)
        val extraText: String? = extras.getString(Notification.EXTRA_TEXT)
        val extraSubText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if ((packageName == applicationContext.packageName || packageName == "com.kakao.talk") && extraTitle != null && extraText != null) {
            var chatroomName: String = extraSubText ?: extraTitle

            var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()

            var removedIds: ArrayList<Int> = kakaoTalkNotiManager.removeNoti(chatroomName)
            var notificationManager = createNotificationChannel()

            removedIds.forEach {
                notificationManager!!.cancel(it)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras
        val extraTitle: String? = extras.getString(Notification.EXTRA_TITLE)
        val extraText: String? = extras.getString(Notification.EXTRA_TEXT)
        val extraSubText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if (packageName == "com.kakao.talk" && extraTitle != null && extraText != null) {
            var chatroomName: String = extraSubText ?: extraTitle
            var sender: String = extraTitle
            var text: String = extraText

            var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
            var removedid = kakaoTalkNotiManager.addNoti(chatroomName, sender, text)
            if (removedid != null) {
                var notificationManager = createNotificationChannel()
                notificationManager!!.cancel(removedid)
            }
            sendNoti()
        }
    }

    private fun sendNoti() {
        var notificationManager = createNotificationChannel()
        var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
        var notiQueue: ArrayDeque<KakaoTalkNoti> = kakaoTalkNotiManager.notiQueue

        var smallIcon = R.drawable.ic_noti
        notiQueue.forEach {
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(it.sender).setContentText(it.text).setGroup(packageName)
                .setWhen(it.time)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)

            notificationManager?.notify(it.id, builder.build())
            if (it.chatroomName != it.sender) {
                builder = builder.setSubText(it.chatroomName)
            }

            notificationManager?.notify(it.id, builder.build())
        }

        val summaryNotification =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("새로운 카카오톡 알림")
                .setContentText("자세히 보려면 알림을 확장하세요.")
                .setSmallIcon(smallIcon)
                .setGroup(packageName)
                .setGroupSummary(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)

        notificationManager?.notify(1234, summaryNotification.build())
    }

    private fun createNotificationChannel(): NotificationManager? {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            return notificationManager
        }
        return null
    }
}
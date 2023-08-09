package com.example.noticap.v1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.noticap.R

class MyNotiListenerService : NotificationListenerService() {
    private val TAG = "MyNotificationListenerService"
    private final val CHANNEL_ID = "NOTI_CAP"

    fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras
        val extraTitle: String? = extras.getString(Notification.EXTRA_TITLE)
        val extraText: String? = extras.getString(Notification.EXTRA_TEXT)
        val extraSubText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if (
            (packageName == applicationContext.packageName || packageName == "com.kakao.talk")
            && extraTitle != null
            && extraText != null
        ) {
            Log.d(TAG, packageName)
            var chatroomName: String = extraSubText ?: extraTitle

            var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
            kakaoTalkNotiManager.removeNoti(chatroomName)

            var notiId: Int? = kakaoTalkNotiManager.getNotiId(chatroomName)
            var notificationManager = createNotificationChannel()

            if (notiId != null) {
                notificationManager!!.cancel(notiId)
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
            var maxSize =getSharedPreferences().getString("max_noti", "15").toString().toInt()
            kakaoTalkNotiManager.addNoti(chatroomName, sender, text, maxSize)

            sendNoti()
        }
    }

    private fun sendNoti() {
        var notificationManager = createNotificationChannel()
        var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
        var notiMap: HashMap<String, KakaoTalkNoti> = kakaoTalkNotiManager.notiMap

        var smallIcon = R.drawable.ic_noti

        val intent = packageManager.getLaunchIntentForPackage("com.kakao.talk")
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

        notiMap.keys.forEach {
            var kakaoTalkNoti = notiMap[it]!!

            if (kakaoTalkNoti.textList.size < 1) return@forEach

            var lastMsg = kakaoTalkNoti.textList.first()
            var fullMsg = ""

            kakaoTalkNoti.textList.forEach {subIt ->
                fullMsg += if (kakaoTalkNoti.chatroomName == subIt.sender)
                    "${subIt.time} ${subIt.text}\n"
                else "${subIt.time} [${subIt.sender}] ${subIt.text}\n"
            }
            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(kakaoTalkNoti.chatroomName)
                .setContentText(if (kakaoTalkNoti.chatroomName == lastMsg.sender)
                    "${lastMsg.time} ${lastMsg.text}"
                else "${lastMsg.time} [${lastMsg.sender}] ${lastMsg.text}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(fullMsg))
                .setGroup(packageName)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(getSharedPreferences().getBoolean("ongoing", true))
                .setContentIntent(pendingIntent)
                .setSilent(true)

            notificationManager?.notify(kakaoTalkNoti.id, builder.build())
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        Log.d(TAG, sharedPreferences.getBoolean("ongoing", true).toString())

        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("새로운 카카오톡 알림")
            .setContentText("자세히 보려면 알림을 확장하세요.")
            .setSmallIcon(smallIcon)
            .setGroup(packageName)
            .setGroupSummary(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(getSharedPreferences().getBoolean("ongoing", true))
            .setSilent(true)

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
package com.example.noticap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat

class MyNotiListenerService : NotificationListenerService() {
    private val TAG = "MyNotificationListenerService"
    private final val CHANNEL_ID = "NOTI_CAP"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras
        val extraSmallIcon: Icon = sbn.notification.smallIcon
        val extraTitle: String? = extras.getString(Notification.EXTRA_TITLE)
        val extraText: String? = extras.getString(Notification.EXTRA_TEXT)
        val extraSubText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if (
            (packageName == applicationContext.packageName || packageName == "com.kakao.talk")
            && extraTitle!= null
            && extraText!= null
        ){
            Log.d(TAG, packageName)
            var chatroomName: String = extraSubText ?: extraTitle

            var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
            kakaoTalkNotiManager.removeNoti(chatroomName)

            var notiId:Int? = kakaoTalkNotiManager.getNotiId(chatroomName)
            var notificationManager = createNotificationChannel()

            if (notiId != null){
                notificationManager!!.cancel(notiId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras
        val extraSmallIcon: Icon = sbn.notification.smallIcon
        val extraTitle: String? = extras.getString(Notification.EXTRA_TITLE)
        val extraText: String? = extras.getString(Notification.EXTRA_TEXT)
        val extraSubText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if (packageName == "com.kakao.talk" && extraTitle!= null && extraText!= null) {
            var chatroomName: String = extraSubText ?: extraTitle
            var sender: String = extraTitle
            var text: String = extraText

            var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
            kakaoTalkNotiManager.addNoti(chatroomName, sender, text)

            sendNoti(extraSmallIcon)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNoti(smallIcon: Icon){
        var notificationManager = createNotificationChannel()
        var kakaoTalkNotiManager: KakaoTalkNotiManager = KakaoTalkNotiManager.getInstance()
        var notiMap: HashMap<String, KakaoTalkNoti> = kakaoTalkNotiManager.notiMap

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
            var builder = Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(lastMsg.sender)
                .setContentText("${lastMsg.time} ${lastMsg.text}")
                .setStyle(Notification.BigTextStyle()
                    .bigText(fullMsg))
                .setGroup(packageName)

            if (kakaoTalkNoti.chatroomName != lastMsg.sender){
                builder = builder.setSubText(kakaoTalkNoti.chatroomName)
            }

            notificationManager?.notify(kakaoTalkNoti.id, builder.build())
        }

        val summaryNotification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("새로운 카카오톡 알림")
            .setContentText("자세히 보려면 알림을 확장하세요.")
            .setSmallIcon(smallIcon)
            .setGroup(packageName)
            .setGroupSummary(true)

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
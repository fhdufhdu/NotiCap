package com.fhdufhdu.noticap.noti.manager.v1;

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import com.fhdufhdu.noticap.R

class KtNotificationCaptureManagerV1 {
    private lateinit var context: Context
    private lateinit var smallIcon: IconCompat

    private var notificationIdList: Array<Boolean> = Array(100000) { false }
    private var notificationMap: HashMap<String, KtNotificationDataV1> = HashMap()

    private val CHANNEL_ID = "CAPTURE"
    private val QUIET_CHANNEL_ID = "QUIET_CAPTURE"
    private val KAKAO_TALK_PACKAGE = "com.kakao.talk"


    companion object {
        private var instance: KtNotificationCaptureManagerV1? = null

        fun getInstance(context: Context): KtNotificationCaptureManagerV1 {
            return instance ?: synchronized(this) {
                instance ?: KtNotificationCaptureManagerV1().also {
                    it.smallIcon = IconCompat.createWithResource(context, R.drawable.ic_noti)
                    it.context = context
                    instance = it
                }
            }
        }
    }

    fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    protected fun getNotificationManager(): NotificationManager {
        createNotificationChannel()
        createNotificationChannelForQuiet()
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(true)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationChannelForQuiet() {
        val name = context.getString(R.string.quiet_channel_name)
        val descriptionText = context.getString(R.string.quiet_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(QUIET_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(false)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun bundleToNotificationDataForKakao(sbn: StatusBarNotification): NotificationData {
        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras


        val title: String? = extras.getString(Notification.EXTRA_TITLE)
        val text: String? = extras.getString(Notification.EXTRA_TEXT)
        val subText: String? = extras.getString(Notification.EXTRA_SUB_TEXT)

        if (packageName != KAKAO_TALK_PACKAGE) {
            throw NotKtNotification(NotKtNotification.NOT_MATCH_KAKAO)
        } else if (title == null || text == null) {
            throw NotKtNotification(NotKtNotification.NOT_HAVE_CONTENT)
        }

        var chatroomName: String = subText ?: title
        var sender: String = title

        return NotificationData(chatroomName, sender, text)
    }

    private fun bundleToNotificationDataForNotiCap(sbn: StatusBarNotification): String {
        val packageName: String = sbn.packageName
        val extras = sbn.notification.extras

        val chatroomName: String? = extras.getString(Notification.EXTRA_TITLE)
        val text: String? = extras.getString(Notification.EXTRA_TEXT)

        if (packageName != context.packageName) {
            throw NotKtNotification(NotKtNotification.NOT_MATCH_NOTICAP)
        } else if (chatroomName == null || text == null) {
            throw NotKtNotification(NotKtNotification.NOT_HAVE_CONTENT)
        }

        return chatroomName
    }

    fun add(sbn: StatusBarNotification) {
        val nd = bundleToNotificationDataForKakao(sbn)
        val ktChannelId = sbn.notification.channelId
        val isQuiet = ktChannelId.contains("quiet")

        if (notificationMap[nd.chatroomName] == null) {
            var id: Int = 0
            do {
                id = (Math.random() * 100000).toInt()
                var existId = notificationIdList[id]
            } while (existId)
            notificationIdList[id] = true
            notificationMap[nd.chatroomName] =
                KtNotificationDataV1(id, nd.chatroomName, isQuiet)
        }
        var maxSize = getSharedPreferences().getString("max_noti", "15").toString().toInt()

        var ktNotificationData = notificationMap.getValue(nd.chatroomName)
        ktNotificationData.addText(nd.sender, nd.text, maxSize)
        ktNotificationData.isQuiet = isQuiet

        sendNotification(ktNotificationData)
    }

    fun remove(sbn: StatusBarNotification): Int? {
        var chatroomName: String = bundleToNotificationDataForNotiCap(sbn)

        var ktNotificationData = notificationMap[chatroomName]
        ktNotificationData?.textList?.clear()

        return ktNotificationData?.id
    }

    private fun sendNotification(ktNotificationData: KtNotificationDataV1) {
        var compName =
            ComponentName(KAKAO_TALK_PACKAGE, "com.kakao.talk.activity.SplashActivity");
        var intent = Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.component = compName;

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager: NotificationManager = getNotificationManager()

        val channelId: String = QUIET_CHANNEL_ID

        if (ktNotificationData.textList.size < 1) return

        var lastMsg = ktNotificationData.textList.first()
        var fullMsg = ""

        ktNotificationData.textList.forEach {
            fullMsg += if (ktNotificationData.chatroomName == it.sender)
                "${it.timeString} ${it.text}\n"
            else "${it.timeString} [${it.sender}] ${it.text}\n"
        }

        var builder = NotificationCompat.Builder(context, channelId)
            .setWhen(lastMsg.time)
            .setSmallIcon(smallIcon)
            .setContentTitle(ktNotificationData.chatroomName)
            .setContentText(
                if (ktNotificationData.chatroomName == lastMsg.sender)
                    "${lastMsg.timeString} ${lastMsg.text}"
                else "${lastMsg.timeString} [${lastMsg.sender}] ${lastMsg.text}"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullMsg)
            )
            .setGroup(context.packageName)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(getSharedPreferences().getBoolean("ongoing", true))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(ktNotificationData.id, builder.build())

        val summaryNotification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("새로운 카카오톡 알림")
            .setContentText("자세히 보려면 알림을 확장하세요.")
            .setSmallIcon(smallIcon)
            .setGroup(context.packageName)
            .setGroupSummary(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(getSharedPreferences().getBoolean("ongoing", true))

        notificationManager.notify(1234, summaryNotification.build())
    }

    fun clearNotification(id: Int) {
        getNotificationManager().cancel(id)
    }

    class NotificationData constructor(
        var chatroomName: String,
        var sender: String,
        var text: String
    ) {
    }

    class NotKtNotification constructor(val type: Int) : RuntimeException() {
        companion object {
            const val NOT_MATCH_KAKAO: Int = 1
            const val NOT_MATCH_NOTICAP: Int = 2
            const val NOT_HAVE_CONTENT: Int = 3
        }
    }
}

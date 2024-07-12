package com.fhdufhdu.noticap.notification.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDao
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDatabase
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.ui.main.MainActivity

class KakaoNotificationSender(private val context: Context) {
    private val notificationManager: NotificationManager
    private val kakaoNotificationDao: KakaoNotificationDao =
        KakaoNotificationDatabase.getInstance(context).kakaoNotificationDao()
    private val memDB: MemDB = MemDB.getInstance()

    companion object {
        const val NOTIFICATION_ID = 1026
        const val CHANNEL_ID = "CAPTURE"
        const val ACTION_NAME = "UPDATE_NOTI_CAP"
    }

    init {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private fun sendNotification() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val unreadChats = kakaoNotificationDao.selectUnreadChats().reversed()

        if (unreadChats.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val messages = ArrayList<NotificationCompat.MessagingStyle.Message>()

        unreadChats.forEach {
            var personBuilder = Person.Builder().setName(it.sender)
            if (it.personKey != null && memDB.personMap.containsKey(it.personKey)) {
                personBuilder = personBuilder.setIcon(
                    memDB.personMap[it.personKey]?.icon
                )
            }
            messages.add(
                NotificationCompat.MessagingStyle.Message(
                    it.content,
                    it.time,
                    personBuilder.build()
                )
            )
        }

        var messageStyle = NotificationCompat.MessagingStyle("")
        messages.forEach {
            messageStyle = messageStyle.addMessage(it)
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(
                IconCompat.createWithResource(
                    context,
                    R.drawable.ic_notification
                )
            )
            .setStyle(messageStyle)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun addAndSendNotification(kakaoNotificationEntity: KakaoNotificationEntity) {
        try {
            kakaoNotificationDao.insertNotification(kakaoNotificationEntity)
            sendNotification()
        } catch (exception: Exception) {
            Log.e("중복 저장", exception.toString())
        }
    }

    fun updateReadStatusAndSendNotification(chatroomName: String) {
        kakaoNotificationDao.updateRead(chatroomName)
        sendNotification()
    }


}
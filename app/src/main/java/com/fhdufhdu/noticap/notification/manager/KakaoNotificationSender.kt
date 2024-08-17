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
import java.lang.StringBuilder

class KakaoNotificationSender(
    private val context: Context,
) {
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
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                name,
                importance,
            ).apply {
                description = descriptionText
            }

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 알림에 쓸 PendingIntent를 제작합니다.
     *
     * @param context PendingIntent 제작에 사용할 Context
     * @return MainActivity 로 이동하게 하는 PendingIntent
     */
    private fun createNotificationPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    /**
     * 읽지 않은 대화를 알림에 사용할 수 있는 객체로 변환합니다.
     *
     * @param unreadChats 읽지 않은 대화 리스트
     * @return List<NotificationCompat.MessagingStyle.Message>를 반환합니다. 이 값은 시간 기준으로 오름차순으로 정렬되어 반환됩니다.
     */
    private fun makeNotificationMessages(unreadChats: List<KakaoNotificationEntity>): List<NotificationCompat.MessagingStyle.Message> {
        val sortedUnreadChats = unreadChats.sortedBy { it.time }
        val unreadChatNotificationMessages =
            sortedUnreadChats.map {
                val name = StringBuilder(it.sender)
                if (it.sender != it.chatroomName) {
                    name.append("(")
                    name.append(it.chatroomName)
                    name.append(")")
                }
                val personBuilder =
                    Person
                        .Builder()
                        .setName(name)
                        .setIcon(memDB.getIconCompat(context, it.personKey, it.personIcon))

                return@map NotificationCompat.MessagingStyle.Message(
                    it.content,
                    it.time,
                    personBuilder.build(),
                )
            }

        return unreadChatNotificationMessages
    }

    private fun sendNotification() {
        val unreadChats = kakaoNotificationDao.selectUnreadChats()

        if (unreadChats.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        val unreadChatsNotificationMessages = makeNotificationMessages(unreadChats)

        var messageStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("me").build())
        unreadChatsNotificationMessages.forEach {
            messageStyle = messageStyle.addMessage(it)
        }

        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(
                    IconCompat.createWithResource(
                        context,
                        R.drawable.ic_notification,
                    ),
                ).setContentTitle("읽지 않은 카카오톡 알림")
                .setContentText("${unreadChatsNotificationMessages.size} 건의 카카오톡 알림을 읽지 않았습니다.")
                .setStyle(messageStyle)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createNotificationPendingIntent(context))
                .setWhen(unreadChatsNotificationMessages.last().timestamp + 1)
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

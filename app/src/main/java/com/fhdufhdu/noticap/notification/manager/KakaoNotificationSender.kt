package com.fhdufhdu.noticap.notification.manager

import android.app.Notification
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
import com.fhdufhdu.noticap.notification.room.entities.KakaoChatroomEntity
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.ui.main.detail.DetailActivity

class KakaoNotificationSender(
    private val context: Context
) {
    private val notificationManager: NotificationManager
    private val kakaoNotificationDao: KakaoNotificationDao =
        KakaoNotificationDatabase.getInstance(context).kakaoNotificationDao()
    private val memDB: MemDB = MemDB.getInstance()

    companion object {
        const val NOTIFICATION_GROUP_KEY = "NOTI_GRUOP_KEY"
        const val CHANNEL_ID = "CAPTURE"
        const val ACTION_NAME = "UPDATE_NOTI_CAP"
    }

    init {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_HIGH
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
    private fun createNotificationPendingIntent(context: Context, chatroomName: String): PendingIntent {
        val intent = Intent(context, DetailActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("CHATROOM_NAME", chatroomName)

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
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
                        .setIcon(memDB.getOrSaveIconCompat(context, it.personKey, it.personIcon))

                return@map NotificationCompat.MessagingStyle.Message(
                    it.content,
                    it.time,
                    personBuilder.build()
                )
            }

        return unreadChatNotificationMessages
    }

    private fun sendNotification(chatroomName: String) {
        val unreadChats = kakaoNotificationDao.selectUnreadChatsByChatroomName(chatroomName)
        val chatroomInfo = kakaoNotificationDao.selectOneChatroomInfo(chatroomName) ?: return
        val notificationId = chatroomInfo.id

        if (unreadChats.isEmpty()) {
            notificationManager.cancel(notificationId)
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
                        R.drawable.ic_notification
                    )
                )
                .setContentTitle(chatroomName)
                .setStyle(messageStyle)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createNotificationPendingIntent(context, chatroomName))
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setAutoCancel(true)
                .setWhen(unreadChatsNotificationMessages.last().timestamp + 1)

        val summaryNotification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(
                IconCompat.createWithResource(
                    context,
                    R.drawable.ic_notification
                )
            )
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("카카오톡 알림")
            )
            .setGroup(NOTIFICATION_GROUP_KEY) // 동일한 그룹 키 설정
            .setGroupSummary(true) // 그룹 요약 알림 표시
            .build()

        notificationManager.notify(notificationId, builder.build())
        notificationManager.notify(1234, summaryNotification)
    }

    fun addAndSendNotification(kakaoNotificationEntity: KakaoNotificationEntity) {
        try {
            kakaoNotificationDao.insertNotification(kakaoNotificationEntity)
            kakaoNotificationDao.selectOneChatroomInfo(kakaoNotificationEntity.chatroomName)
                ?: kakaoNotificationDao.insertChatroomInfo(
                    KakaoChatroomEntity(kakaoNotificationEntity.chatroomName)
                )
            sendNotification(kakaoNotificationEntity.chatroomName)
        } catch (exception: Exception) {
            Log.e("중복 저장", exception.toString())
        }
    }

    fun updateReadStatus(chatroomName: String) {
        kakaoNotificationDao.updateRead(chatroomName)
    }

    fun updateReadStatusAndSendNotification(chatroomName: String) {
        kakaoNotificationDao.updateRead(chatroomName)
        sendNotification(chatroomName)
    }
}

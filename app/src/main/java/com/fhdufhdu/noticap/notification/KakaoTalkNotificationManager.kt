package com.fhdufhdu.noticap.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.preference.PreferenceManager
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.notification.vo.Chatroom
import com.fhdufhdu.noticap.notification.vo.Conversation

private const val NOTIFICATION_GROUP_KEY = "NOTI_GRUOP_KEY"
private const val CHANNEL_ID = "CAPTURE"
private const val FOREGROUND_CHANNEL_ID = "FOREGROUND"

class KakaoTalkNotificationManager(
    private val context: Context,
) {
    private val notificationManager: NotificationManager
    private val foregroundNotificationManager: NotificationManager
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = descriptionText
            }
        val foregroundChannel =
            NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "서비스 활성화",
                NotificationManager.IMPORTANCE_MIN,
            ).apply {
                description = "서비스 활성화 알림"
            }

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        foregroundNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        foregroundNotificationManager.createNotificationChannel(foregroundChannel)
    }

    /**
     * 읽지 않은 대화를 알림에 사용할 수 있는 객체로 변환합니다.
     *
     * @param unreadChats 읽지 않은 대화 리스트
     * @return List<NotificationCompat.MessagingStyle.Message>를 반환합니다. 이 값은 시간 기준으로 오름차순으로 정렬되어 반환됩니다.
     */
    private fun makeNotificationMessages(unreadChats: List<Conversation>): List<NotificationCompat.MessagingStyle.Message> {
        val sortedUnreadChats = unreadChats.sortedBy { it.time }
        val unreadChatNotificationMessages =
            sortedUnreadChats.map {
                val name = StringBuilder(it.sender.name)
                if (it.sender.name != it.chatroom.name) {
                    name.append("(")
                    name.append(it.chatroom)
                    name.append(")")
                }
                val personBuilder =
                    Person.Builder()
                        .setName(name)
                        .setIcon(it.sender.icon)

                return@map NotificationCompat.MessagingStyle.Message(
                    it.content.text,
                    it.time.timestamp,
                    personBuilder.build(),
                )
            }

        return unreadChatNotificationMessages
    }

    fun getForegroundNotification(): Notification =
        NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.foreground_notification_title))
            .setSmallIcon(
                IconCompat.createWithResource(
                    context,
                    R.drawable.ic_noti,
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

    fun sendNotification(chatroom: Chatroom) {
        val notificationId = NotificationIdManager.computeIfAbsent(chatroom)
        val conversationList = ConversationRepository.get(chatroom)

        if (conversationList.isNullOrEmpty()) {
            notificationManager.cancel(notificationId)
            return
        }

        val unreadChatsNotificationMessages = makeNotificationMessages(conversationList)

        var messageStyle = NotificationCompat.MessagingStyle(Person.Builder().setName("me").build())
        unreadChatsNotificationMessages.forEach {
            messageStyle = messageStyle.addMessage(it)
        }

        var builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(
                    IconCompat.createWithResource(
                        context,
                        R.drawable.ic_notification,
                    ),
                )
                .setContentTitle(chatroom.name)
                .setStyle(messageStyle)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setAutoCancel(true)
                .setWhen(unreadChatsNotificationMessages.last().timestamp + 1)

        val toMoveToKakao = sharedPreferences.getBoolean("TO_MOVE_TO_KAKAO", true)
        if (toMoveToKakao)
            {
                builder = builder.setContentIntent(conversationList[0].intent)
            }

        val summaryNotification: Notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(
                    IconCompat.createWithResource(
                        context,
                        R.drawable.ic_notification,
                    ),
                )
                .setStyle(
                    NotificationCompat.InboxStyle()
                        .setSummaryText("카카오톡 알림"),
                )
                .setGroup(NOTIFICATION_GROUP_KEY) // 동일한 그룹 키 설정
                .setGroupSummary(true) // 그룹 요약 알림 표시
                .build()

        notificationManager.notify(notificationId, builder.build())
        notificationManager.notify(1234, summaryNotification)
    }
}

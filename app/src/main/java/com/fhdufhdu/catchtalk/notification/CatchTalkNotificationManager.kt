package com.fhdufhdu.catchtalk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.fhdufhdu.catchtalk.R
import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import com.fhdufhdu.catchtalk.notification.vo.Conversation
import com.fhdufhdu.catchtalk.util.SharedPreferenceManager

private const val NOTIFICATION_GROUP_KEY = "NOTI_GRUOP_KEY"
private const val CHANNEL_ID = "CATCHTALK"
private const val SILENT_CHANNEL_ID = "SILENT_CATCHTALK"

class CatchTalkNotificationManager(
    private val context: Context,
) {
    private val notificationManager: NotificationManager
    private val sharedPreferencesManager = SharedPreferenceManager(context)

    /**
     * NotificationManager 초기화
     */
    init {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.channel_description)
            }
        val quietChannel =
            NotificationChannel(
                SILENT_CHANNEL_ID,
                context.getString(R.string.quiet_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.quiet_channel_description)
            }
        val foregroundChannel =
            NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                context.getString(R.string.foreground_channel_name),
                NotificationManager.IMPORTANCE_MIN,
            ).apply {
                description = context.getString(R.string.foreground_channel_description)
                setShowBadge(false)
            }

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(listOf(channel, quietChannel, foregroundChannel))
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
                    name.append(it.chatroom.name)
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

    /**
     * Foreground 서비스 활성화를 위한 Notification 객체 생성
     */
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

        val channelId = if (chatroom.quiet || !sharedPreferencesManager.isRemoveKakaoNoti()) SILENT_CHANNEL_ID else CHANNEL_ID

        var builder =
            NotificationCompat.Builder(context, channelId)
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

        if (sharedPreferencesManager.isMoveToKakao()) {
            builder = builder.setContentIntent(conversationList[0].intent)
        }

        if (sharedPreferencesManager.isRemoveKakaoNoti()) {
            builder =
                builder.addAction(
                    R.drawable.transparent,
                    "읽음",
                    PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        Intent(context, ReadNotificationActionReceiver::class.java).apply {
                            action = ReadNotificationActionReceiver.READ_NOTIFICATION_ACTION
                            putExtra(ReadNotificationActionReceiver.NOTIFICATION_ID_KEY, notificationId)
                            putExtra(ReadNotificationActionReceiver.CHATROOM_KEY, chatroom)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
        }

        val summaryNotification: Notification =
            NotificationCompat.Builder(context, SILENT_CHANNEL_ID)
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

    companion object {
        const val FOREGROUND_CHANNEL_ID = "FOREGROUND"
    }
}

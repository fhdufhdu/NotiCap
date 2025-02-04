package com.fhdufhdu.noticap.notification.vo

import android.app.Notification
import android.os.Bundle
import com.fhdufhdu.noticap.notification.UnusableNotificationException

data class NotificationTitle(
    val title: String,
) {
    companion object {
        fun from(notificationExtras: Bundle) =
            NotificationTitle(
                notificationExtras.getString(Notification.EXTRA_TITLE)
                    ?: throw UnusableNotificationException("제목 없는 알림"),
            )
    }
}

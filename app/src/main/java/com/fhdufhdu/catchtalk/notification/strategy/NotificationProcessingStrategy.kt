package com.fhdufhdu.catchtalk.notification.strategy

import android.service.notification.StatusBarNotification

interface NotificationProcessingStrategy {
    fun onNotificationPosted(sbn: StatusBarNotification)

    fun onNotificationRemoved(sbn: StatusBarNotification)
}

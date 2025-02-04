package com.fhdufhdu.noticap.notification.vo

import android.app.Notification
import android.os.Bundle

@JvmInline
value class Content(val text: String) {
    companion object {
        fun from(notificationExtra: Bundle) = Content(notificationExtra.getString(Notification.EXTRA_TEXT) ?: "")
    }
}

package com.fhdufhdu.catchtalk.notification.vo

import android.app.Notification
import android.os.Bundle

@JvmInline
value class Content(val text: String) {
    companion object {
        fun from(notificationExtras: Bundle) = Content(notificationExtras.getString(Notification.EXTRA_TEXT) ?: "")
    }
}

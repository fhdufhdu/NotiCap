package com.fhdufhdu.catchtalk.util

import android.app.Notification

object ExtensionFunctions {
    fun Notification.isQuiet() = this.channelId.contains("quiet")
}

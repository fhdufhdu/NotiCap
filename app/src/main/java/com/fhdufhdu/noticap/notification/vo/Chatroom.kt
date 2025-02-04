package com.fhdufhdu.noticap.notification.vo

@JvmInline
value class Chatroom(val name: String) {
    companion object {
        fun from(notificationTitle: NotificationTitle) = Chatroom(notificationTitle.title)
    }

    override fun toString(): String = name
}

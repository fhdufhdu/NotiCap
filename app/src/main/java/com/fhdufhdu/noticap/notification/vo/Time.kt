package com.fhdufhdu.noticap.notification.vo

import android.app.Notification

@JvmInline
value class Time(val timestamp: Long) : Comparable<Time> {
    companion object {
        fun from(notification: Notification) = Time(notification.`when`)
    }

    override fun compareTo(other: Time): Int = this.timestamp.compareTo(other.timestamp)
}

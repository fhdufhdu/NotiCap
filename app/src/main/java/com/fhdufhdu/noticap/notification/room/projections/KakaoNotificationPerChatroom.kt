package com.fhdufhdu.noticap.notification.room.projections

import androidx.room.ColumnInfo

data class KakaoNotificationPerChatroom(
    @ColumnInfo(name = "chatroom_name")
    val chatroomName: String,
    @ColumnInfo
    val sender: String,
    @ColumnInfo
    val content: String,
    @ColumnInfo(name = "person_key")
    val personKey: String?,
    @ColumnInfo(name = "person_icon")
    val personIcon: String?,
    @ColumnInfo
    val time: Long,
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int,
    @ColumnInfo
    var unread: Boolean,
    @ColumnInfo(name = "do_run_animation")
    var doRunAnimation: Boolean,
    @ColumnInfo
    val id: Long = 0,
)

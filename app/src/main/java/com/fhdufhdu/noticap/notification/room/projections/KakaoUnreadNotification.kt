package com.fhdufhdu.noticap.notification.room.projections

import androidx.room.ColumnInfo

data class KakaoUnreadNotification(

    @ColumnInfo(name = "chatroom_name")
    val chatroomName: String,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int,
)
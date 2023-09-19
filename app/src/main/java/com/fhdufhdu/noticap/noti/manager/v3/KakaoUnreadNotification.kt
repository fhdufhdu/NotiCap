package com.fhdufhdu.noticap.noti.manager.v3

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class KakaoUnreadNotification(

    @ColumnInfo(name="chatroom_name")
    val chatroomName: String,

    @ColumnInfo(name="unread_count")
    val unreadCount: Int,
)
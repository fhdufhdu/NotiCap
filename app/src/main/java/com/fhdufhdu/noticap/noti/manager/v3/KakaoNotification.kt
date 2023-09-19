package com.fhdufhdu.noticap.noti.manager.v3

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Notification")
class KakaoNotification(

    @ColumnInfo(name = "chatroom_name")
    val chatroomName: String,

    @ColumnInfo
    val sender: String,

    @ColumnInfo
    val content: String,

    @ColumnInfo(name = "person_icon")
    val personIcon: String?,

    @ColumnInfo
    val time: Long,

    @ColumnInfo(defaultValue = "true")
    var unread: Boolean = true,

    @ColumnInfo(name = "do_run_animation", defaultValue = "true")
    var doRunAnimation: Boolean = true,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
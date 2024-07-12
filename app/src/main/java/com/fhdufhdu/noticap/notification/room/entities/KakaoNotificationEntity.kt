package com.fhdufhdu.noticap.notification.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Notification", indices = [Index(value = ["chatroom_name", "unread", "time"])])
class KakaoNotificationEntity(
    @PrimaryKey
    val id: String,

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


) {
    override fun toString(): String {
        return "KakaoNotificationEntity(id='$id', chatroomName='$chatroomName', sender='$sender', content='$content', time=$time, unread=$unread, doRunAnimation=$doRunAnimation)"
    }
}
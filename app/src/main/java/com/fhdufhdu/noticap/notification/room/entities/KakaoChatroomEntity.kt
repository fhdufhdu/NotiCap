package com.fhdufhdu.noticap.notification.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Chatroom",
    indices = [Index(value = ["chatroom_name"], unique = true)],
)
class KakaoChatroomEntity(
    @ColumnInfo(name = "chatroom_name")
    val chatroomName: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
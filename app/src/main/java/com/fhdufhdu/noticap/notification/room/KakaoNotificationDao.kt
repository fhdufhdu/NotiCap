package com.fhdufhdu.noticap.notification.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fhdufhdu.noticap.notification.room.entities.KakaoChatroomEntity
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.notification.room.projections.KakaoNotificationPerChatroom
import com.fhdufhdu.noticap.notification.room.projections.KakaoUnreadNotification

@Dao
interface KakaoNotificationDao {
    @Query(
        """
            select n2.*,
                 (select count(*)
                  from Notification
                  where chatroom_name = n1.chatroom_name and unread = 1) as unread_count
            from (select chatroom_name, max(time) as max_time
                  from Notification
                  group by chatroom_name) n1
            join Notification n2
            on n1.chatroom_name=n2.chatroom_name and n1.max_time = n2.time
            order by n1.max_time desc
            limit :pageSize
            offset :pageNumber * :pageSize
        """,
    )
    fun selectLastNotificationsPerChatroom(
        pageNumber: Int,
        pageSize: Int,
    ): List<KakaoNotificationPerChatroom>

    @Query(
        """
            select count(*)
            from (select chatroom_name
                    from Notification
                    group by chatroom_name) n1
        """,
    )
    fun count(): Int

    @Query(
        "select * from Notification where chatroom_name = :chatroomName order by time desc limit :pageSize offset :pageNumber * :pageSize",
    )
    fun selectMany(
        chatroomName: String,
        pageNumber: Int,
        pageSize: Int,
    ): List<KakaoNotificationEntity>

    @Query("select count(*) from Notification where chatroom_name = :chatroomName")
    fun count(chatroomName: String): Int

    @Query(
        "select chatroom_name, count(*) as unread_count  from Notification where unread = 1 group by chatroom_name order by max(time) desc",
    )
    fun selectUnreadChatrooms(): List<KakaoUnreadNotification>

    @Query("select * from Notification where unread = 1 order by time desc limit 20")
    fun selectUnreadChats(): List<KakaoNotificationEntity>

    @Query("select * from Notification where unread = 1 and chatroom_name = :chatroomName order by time desc limit 20")
    fun selectUnreadChatsByChatroomName(chatroomName: String): List<KakaoNotificationEntity>

    @Query("select not(count(*)) from Notification")
    fun isEmpty(): LiveData<Boolean>

    @Insert
    fun insertNotification(vararg kakaoNotifications: KakaoNotificationEntity)

    @Query("update Notification set unread = 0 where unread = 1 and chatroom_name = :chatroomName")
    fun updateRead(chatroomName: String)

    @Query("delete from Notification where chatroom_name = :chatroomName")
    fun deleteOne(chatroomName: String)

    @Query("update Notification set do_run_animation = 0 where id = :id")
    fun updateDoRunAnimation(id: String)

    @Query("select * from Chatroom where chatroom_name = :chatroomName")
    fun selectOneChatroomInfo(chatroomName: String): KakaoChatroomEntity?

    @Insert
    fun insertChatroomInfo(vararg kakaoChatroomEntity: KakaoChatroomEntity)
}

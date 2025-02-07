package com.fhdufhdu.catchtalk.notification

import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object LastChatroomRepository {
    private val map: ConcurrentMap<Chatroom, Chatroom> = ConcurrentHashMap()

    fun add(chatroom: Chatroom) {
        map[chatroom] = chatroom
    }

    fun get(chatroom: Chatroom): Chatroom? = map[chatroom]
}

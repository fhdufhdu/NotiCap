package com.fhdufhdu.catchtalk.notification

import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import com.fhdufhdu.catchtalk.notification.vo.Conversation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object ConversationRepository {
    private val map: ConcurrentMap<Chatroom, MutableList<Conversation>> = ConcurrentHashMap()

    @Synchronized
    fun add(conversation: Conversation) {
        map.computeIfAbsent(conversation.chatroom) { ArrayList() }.add(conversation)
    }

    fun clear(chatroom: Chatroom) {
        map.remove(chatroom)
    }

    fun get(chatroom: Chatroom): MutableList<Conversation>? = map[chatroom]
}

package com.fhdufhdu.catchtalk.notification

import com.fhdufhdu.catchtalk.notification.vo.Chatroom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

object NotificationIdManager {
    private val lastNumber: AtomicInteger = AtomicInteger(0)
    private val map: ConcurrentMap<Chatroom, Int> = ConcurrentHashMap()

    @Synchronized
    fun computeIfAbsent(chatroom: Chatroom): Int =
        map.computeIfAbsent(chatroom) {
            val nextNumber = lastNumber.get() + 1
            lastNumber.set(nextNumber)
            nextNumber
        }
}

package com.example.noticap.v2;

class KakaoTalkNotiManager {
    var notiIdList: Array<Boolean> = Array(100000) { false }
    var notiQueue: ArrayDeque<KakaoTalkNoti> = ArrayDeque<KakaoTalkNoti>()

    companion object {
        private var instance: KakaoTalkNotiManager? = null

        fun getInstance(): KakaoTalkNotiManager {
            return instance ?: synchronized(this) {
                instance ?: KakaoTalkNotiManager().also {
                    instance = it
                }
            }
        }
    }

    public fun addNoti(chatroomName: String, sender: String, text: String): Int? {
        var id: Int = 0
        do {
            id = (Math.random() * 100000).toInt()
            var existId = notiIdList[id]
        } while (existId)
        notiIdList[id] = true

        notiQueue.addFirst(KakaoTalkNoti(id, chatroomName, sender, text))

        if (notiQueue.size > 15) {
            val id = notiQueue.removeLast().id
            notiIdList[id] = false
            return id
        }
        return null
    }

    public fun removeNoti(chatroomName: String): ArrayList<Int> {
        var removedIds: ArrayList<Int> = ArrayList()
        var removedElems: ArrayList<KakaoTalkNoti> = ArrayList()

        notiQueue.forEach {
            if (it.chatroomName == chatroomName) {
                removedIds.add(it.id)
                removedElems.add(it)
            }
        }
        notiQueue.removeAll(removedElems)
        return removedIds
    }
}

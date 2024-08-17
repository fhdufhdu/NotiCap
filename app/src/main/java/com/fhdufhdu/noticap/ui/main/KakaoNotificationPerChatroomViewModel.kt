package com.fhdufhdu.noticap.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDao
import com.fhdufhdu.noticap.notification.room.projections.KakaoNotificationPerChatroom
import com.fhdufhdu.noticap.util.CoroutineManager

class KakaoNotificationPerChatroomViewModel(
    private val dao: KakaoNotificationDao,
) : ViewModel() {
    private val _notificationList = MutableLiveData<ArrayList<KakaoNotificationPerChatroom>>()
    val notificationList: LiveData<ArrayList<KakaoNotificationPerChatroom>>
        get() = _notificationList

    fun fetchFirstPage(pageSize: Int) {
        CoroutineManager.runSync {
            val firstPage = ArrayList(dao.selectLastNotificationsPerChatroom(0, pageSize))
            _notificationList.postValue(firstPage)
        }
    }

    fun fetchNextPage(
        pageNumber: Int,
        pageSize: Int,
    ) {
        val prevList = _notificationList.value ?: return
        CoroutineManager.runSync {
            val nextPage = dao.selectLastNotificationsPerChatroom(pageNumber, pageSize)
            prevList.addAll(nextPage)
            _notificationList.postValue(prevList)
        }
    }
}

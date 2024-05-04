package com.fhdufhdu.noticap.ui.main.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotification
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDao
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationPerChatroom
import com.fhdufhdu.noticap.util.CoroutineManager

class KakaoNotificationViewModel(private val dao: KakaoNotificationDao): ViewModel() {
    private val _notificationList = MutableLiveData<ArrayList<KakaoNotification>>()
    val notificationList: LiveData<ArrayList<KakaoNotification>>
        get() = _notificationList

    fun fetchFirstPage(chatroomName: String, pageSize: Int) {
        CoroutineManager.runSync {
            val firstPage = ArrayList(dao.selectMany(chatroomName, 0, pageSize))
            _notificationList.postValue(firstPage)
        }
    }

    fun fetchNextPage(chatroomName: String, pageNumber: Int, pageSize: Int) {
        val prevList = _notificationList.value?: return
        CoroutineManager.runSync {
            val nextPage = dao.selectMany(chatroomName, pageNumber, pageSize)
            prevList.addAll(nextPage)
            _notificationList.postValue(prevList)
        }
    }

}
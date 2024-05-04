package com.fhdufhdu.noticap.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDao
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationPerChatroom

class KakaoNotificationPerChatroomViewModelFactory(private val dao: KakaoNotificationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakaoNotificationPerChatroomViewModel::class.java)) {
            return KakaoNotificationPerChatroomViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
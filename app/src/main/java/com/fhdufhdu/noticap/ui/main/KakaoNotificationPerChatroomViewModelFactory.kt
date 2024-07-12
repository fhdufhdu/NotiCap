package com.fhdufhdu.noticap.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDao

class KakaoNotificationPerChatroomViewModelFactory(private val dao: KakaoNotificationDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakaoNotificationPerChatroomViewModel::class.java)) {
            return KakaoNotificationPerChatroomViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
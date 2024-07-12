package com.fhdufhdu.noticap.ui.main.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDao

class KakaoNotificationViewModelFactory(private val dao: KakaoNotificationDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakaoNotificationViewModel::class.java)) {
            return KakaoNotificationViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
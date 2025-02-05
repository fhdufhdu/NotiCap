package com.fhdufhdu.catchtalk.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SharedPreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isMoveToKakao(context: Context): Boolean = sharedPreferences.getBoolean("TO_MOVE_TO_KAKAO", true)
}

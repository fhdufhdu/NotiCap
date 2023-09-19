package com.fhdufhdu.noticap.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SharedPreferenceManager {
    companion object {
        val TIME_FORMAT_RELATIVE = 1
        val TIME_FORMAT_ABSOLUTE = 2

        private fun getSharedPreference(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getTimeFormatType(context: Context): Int {
            val pref = getSharedPreference(context)

            return pref.getString("TIME_FORMAT", "2")!!.toInt()
        }

        fun whetherToMoveToKakao(context: Context): Boolean {
            val pref = getSharedPreference(context)

            return pref.getBoolean("TO_MOVE_TO_KAKAO", true)

        }
    }

}
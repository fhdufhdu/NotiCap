package com.fhdufhdu.noticap

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat

class CalculateTime {
    companion object{
        @SuppressLint("SimpleDateFormat")
        fun toString(pref: SharedPreferences, millis: Long): String{
            when(pref.getString("representation_time", "2")){
                "2" -> {
                    return SimpleDateFormat("HH:mm").format(millis)
                }
                else -> {
                    val now = System.currentTimeMillis() / 1000
                    val time = millis / 1000
                    val calc = now - time

                    return if (calc < 60) {
                        "${calc}초 전"
                    } else if (calc < 60 * 60) {
                        "${(calc / 60).toInt()}분 전"
                    } else if (calc < 60 * 60 * 24) {
                        "${(calc / (60 * 60)).toInt()}시간 전"
                    } else if (calc < 60 * 60 * 24 * 365){
                        SimpleDateFormat("MM/dd").format(millis)
                    } else {
                        SimpleDateFormat("yy/MM/dd").format(millis)
                    }
                }
            }
        }
    }
}
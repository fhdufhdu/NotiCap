package com.fhdufhdu.noticap.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

class TimeCalculator {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun toString(timeFormatType: Int, millis: Long): String {
            val now = System.currentTimeMillis() / 1000
            val time = millis / 1000
            val calc = now - time
            when (timeFormatType) {
                SharedPreferenceManager.TIME_FORMAT_ABSOLUTE -> {
                    return if (calc < 60 * 60 * 24) {
                        SimpleDateFormat("a h:mm", Locale.KOREA).format(millis)
                    } else if (calc < 60 * 60 * 24 * 7) {
                        SimpleDateFormat("(E) a h:mm", Locale.KOREA).format(millis)
                    } else {
                        SimpleDateFormat("yyyy. M. d.", Locale.KOREA).format(millis)
                    }
                }

                else -> {

                    return if (calc < 60) {
                        "방금 전"
                    } else if (calc < 60 * 60) {
                        "${(calc / 60).toInt()}분 전"
                    } else if (calc < 60 * 60 * 4) {
                        "${(calc / (60 * 60)).toInt()}시간 전"
                    } else if (calc < 60 * 60 * 24) {
                        SimpleDateFormat("a h:mm", Locale.KOREA).format(millis)
                    } else if (calc < 60 * 60 * 24 * 7) {
                        SimpleDateFormat("(E) a h:mm", Locale.KOREA).format(millis)
                    } else {
                        SimpleDateFormat("yyyy. M. d.", Locale.KOREA).format(millis)
                    }
                }
            }
        }
    }
}
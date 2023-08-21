package com.fhdufhdu.noticap

import android.util.DisplayMetrics

class SizeManager {
    companion object{
        fun dpToPx(dp: Int, displayMetrics: DisplayMetrics): Int {
            val scale = displayMetrics.density
            return (dp * scale).toInt()
        }

        fun pxToDp(px: Int, displayMetrics: DisplayMetrics): Int {
            val scale = displayMetrics.density
            return (px / scale).toInt()
        }

        fun spToPx(sp: Float, displayMetrics: DisplayMetrics): Float {
            val scale = displayMetrics.scaledDensity
            return sp * scale
        }

        fun ptToPx(pt: Float): Float {
            return pt * (1.0f / 0.75f)
        }

        fun inToPx(inch: Float, displayMetrics: DisplayMetrics): Float {
            val dpi = displayMetrics.densityDpi
            return inch * dpi
        }

        fun mmToPx(mm: Float, displayMetrics: DisplayMetrics): Float {
            val inch = mm / 25.4f
            return inToPx(inch, displayMetrics)
        }
    }
}
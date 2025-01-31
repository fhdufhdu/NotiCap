package com.fhdufhdu.noticap.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Base64
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toIcon
import java.io.ByteArrayOutputStream

class IconConverter {

    companion object {
        fun stringToBitmap(encodedString: String?): Bitmap? {
            return try {
                val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            } catch (e: Exception) {
                e.message
                null
            }
        }

        /*
         * Bitmap을 String형으로 변환
         * */
        fun bitmapToString(bitmap: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos)
            val bytes = baos.toByteArray()
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }

        fun stringToIcon(encodedString: String?): Icon? {
            return try {
                val bitmap = stringToBitmap(encodedString)
                bitmap?.toIcon()
            } catch (e: Exception) {
                e.message
                null
            }
        }

        //        fun iconToString(icon:Icon, context: Context): String?{
//            return bitmapToString(icon.loadDrawable(context)!!.toBitmap())
//        }
        fun iconToString(icon: Icon?, context: Context): String? {
            if (icon != null) {
                return bitmapToString(icon.loadDrawable(context)!!.toBitmap())
            }
            return null
        }

        /*
         * Bitmap을 byte배열로 변환
         * */
        fun bitmapToByteArray(bitmap: Bitmap): ByteArray? {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            return baos.toByteArray()
        }
    }
}

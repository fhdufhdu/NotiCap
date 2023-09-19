package com.fhdufhdu.noticap.noti.manager.v3;

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fhdufhdu.noticap.noti.manager.v2.KakaoNotificationDataManager

@Database(entities = [KakaoNotification::class], version = 1, exportSchema = false)
abstract class KakaoNotificationDatabase : RoomDatabase() {
    abstract fun kakaoNotificationDao(): KakaoNotificationDao

    companion object {
        private var instance: KakaoNotificationDatabase? = null
        fun getInstance(applicationContext: Context): KakaoNotificationDatabase {
            return instance ?: synchronized(this) {
                if (instance == null){
                    instance = Room.databaseBuilder(
                        applicationContext,
                        KakaoNotificationDatabase::class.java, "database"
                    ).build()
                }
                return instance!!

            }
        }
    }
}

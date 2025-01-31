package com.fhdufhdu.noticap.notification.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fhdufhdu.noticap.notification.room.entities.KakaoChatroomEntity
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity

@Database(entities = [KakaoNotificationEntity::class, KakaoChatroomEntity::class], version = 2, autoMigrations = [AutoMigration(from = 1, to = 2)])
abstract class KakaoNotificationDatabase : RoomDatabase() {
    abstract fun kakaoNotificationDao(): KakaoNotificationDao

    companion object {
        private var instance: KakaoNotificationDatabase? = null

        fun getInstance(applicationContext: Context): KakaoNotificationDatabase {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    instance =
                        Room
                            .databaseBuilder(
                                applicationContext,
                                KakaoNotificationDatabase::class.java,
                                "database"
                            ).build()
                }
                return instance!!
            }
        }
    }
}

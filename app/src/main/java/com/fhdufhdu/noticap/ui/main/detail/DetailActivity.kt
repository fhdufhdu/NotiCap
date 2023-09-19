package com.fhdufhdu.noticap.ui.main.detail

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fhdufhdu.noticap.databinding.ActivityNotificationBinding
import com.fhdufhdu.noticap.noti.manager.v2.CustomNotificationListenerService
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDatabase


class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private var chatroomName: String = ""
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val screenOffReceiver = ScreenOffReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(CustomNotificationListenerService.ACTION_NAME)
        registerReceiver(screenOffReceiver, filter)

        val dao = KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

        chatroomName = intent.getStringExtra("CHATROOM_NAME") ?: return
        intent.extras?.clear()

        binding.rvNotification.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(applicationContext)
        binding.rvNotification.adapter = notificationAdapter

        dao.selectMany(chatroomName).observe(this) {
            notificationAdapter.update(it)
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs: SharedPreferences, key: String ->
                notificationAdapter.notifyDataSetChanged()
            }

    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    inner class ScreenOffReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_SCREEN_OFF == intent?.action) {
                finish()
            }
//            else if(CustomNotificationListenerService.ACTION_NAME == intent?.action){
//                if (notificationAdapter == null) setAdapter()
//                else updateAdapter()
//            }
        }
    }

}
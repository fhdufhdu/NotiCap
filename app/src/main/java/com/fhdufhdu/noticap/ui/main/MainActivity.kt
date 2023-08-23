package com.fhdufhdu.noticap.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.ui.setting.SettingActivity
import com.fhdufhdu.noticap.noti.manager.v2.CustomNotificationListenerService
import com.fhdufhdu.noticap.noti.manager.v2.KakaoNotificationDataManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var tvEmptyNotice: TextView
    var notificationAdapter: ChatroomNotificationAdapter? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "채팅방 목록"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                TedPermission.create()
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() {}
                        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
                    })
                    .setDeniedMessage("알림 권한을 허용하지 않으시면 앱을 사용하실 수 없습니다.")
                    .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
                    .check()

        }
        if (!isNotiPermissionGranted()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

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

        val kakaoNotificationDataManager = KakaoNotificationDataManager.getInstance()
        kakaoNotificationDataManager.loadJson(this)

        tvEmptyNotice = findViewById(R.id.tv_emtpy_notice)
        if (kakaoNotificationDataManager.notificationMap.size == 0) {
            tvEmptyNotice.visibility = TextView.VISIBLE
        }
        recyclerView = findViewById(R.id.rv_main_notification)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
    }

    private fun isNotiPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationListenerAccessGranted(
                ComponentName(
                    application,
                    CustomNotificationListenerService::class.java
                )
            )
        } else {
            return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(applicationContext.packageName)
        }
    }

    private fun setAdapter() {
        notificationAdapter = ChatroomNotificationAdapter()
        recyclerView.adapter = notificationAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        notificationAdapter?.update()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menus, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    inner class ScreenOffReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_SCREEN_OFF == intent?.action) {
                finish()
            } else if (CustomNotificationListenerService.ACTION_NAME == intent?.action) {
                if (notificationAdapter == null) setAdapter()
                else updateAdapter()
                tvEmptyNotice.visibility = TextView.INVISIBLE
            }
        }
    }
}
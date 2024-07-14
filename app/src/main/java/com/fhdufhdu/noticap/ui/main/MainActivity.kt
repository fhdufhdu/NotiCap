package com.fhdufhdu.noticap.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.databinding.ActivityMainBinding
import com.fhdufhdu.noticap.notification.manager.CustomNotificationListenerService
import com.fhdufhdu.noticap.notification.manager.KakaoNotificationSender
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDatabase
import com.fhdufhdu.noticap.notification.room.projections.KakaoNotificationPerChatroom
import com.fhdufhdu.noticap.ui.setting.SettingActivity
import com.fhdufhdu.noticap.util.CoroutineManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationAdapter: ChatroomNotificationAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var viewModel: KakaoNotificationPerChatroomViewModel
    private val NOTIFICATION_PAGE_SIZE = 20
    private var notificationPageNumber = 0


    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "채팅방 목록"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) TedPermission.create()
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {}
                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
                }).setDeniedMessage("알림 권한을 허용하지 않으시면 앱을 사용하실 수 없습니다.")
                .setPermissions(Manifest.permission.POST_NOTIFICATIONS).check()

        }
        if (!isNotiPermissionGranted()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val screenOffReceiver = ScreenOffReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(KakaoNotificationSender.ACTION_NAME)
        registerReceiver(screenOffReceiver, filter)

        val dao = KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

        binding.rvMainNotification.layoutManager = LinearLayoutManager(this)
        notificationAdapter = ChatroomNotificationAdapter(applicationContext)
        binding.rvMainNotification.adapter = notificationAdapter

        viewModel = ViewModelProvider(this, KakaoNotificationPerChatroomViewModelFactory(dao)).get(
            KakaoNotificationPerChatroomViewModel::class.java
        )
//        viewModel.fetchFirstPage(NOTIFICATION_PAGE_SIZE)

        var observer = Observer<List<KakaoNotificationPerChatroom>> {
            notificationAdapter.update(it)
        }
        viewModel.notificationList.observe(this, observer)

        dao.isEmpty().observe(this) {
            binding.tvEmtpyNotice.visibility = if (it) TextView.VISIBLE else TextView.INVISIBLE
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs: SharedPreferences, key: String ->
                notificationAdapter.notifyDataSetChanged()
            }

        binding.rvMainNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                if (lastVisibleItemPosition == itemTotalCount) {
                    val notificationCount =
                        CoroutineManager.runSync { dao.count() }

                    if (NOTIFICATION_PAGE_SIZE * (notificationPageNumber + 1) < notificationCount) {
                        notificationPageNumber++
                        viewModel.fetchNextPage(notificationPageNumber, NOTIFICATION_PAGE_SIZE)
                    }

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        viewModel.fetchFirstPage((notificationPageNumber + 1) * NOTIFICATION_PAGE_SIZE )
    }

    override fun onPause() {
        super.onPause()
    }

    private fun isNotiPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationListenerAccessGranted(
                ComponentName(
                    application, CustomNotificationListenerService::class.java
                )
            )
        } else {
            return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
                .contains(applicationContext.packageName)
        }
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
            }
        }
    }
}
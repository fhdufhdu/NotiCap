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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.databinding.ActivityNotificationBinding
import com.fhdufhdu.noticap.notification.manager.KakaoNotificationSender
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDao
import com.fhdufhdu.noticap.notification.room.KakaoNotificationDatabase
import com.fhdufhdu.noticap.notification.room.entities.KakaoNotificationEntity
import com.fhdufhdu.noticap.util.CoroutineManager


class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var dao: KakaoNotificationDao
    private lateinit var viewModel: KakaoNotificationViewModel
    private var chatroomName: String = ""
    private var NOTIFICATION_PAGE_SIZE: Int = 20
    private var notificationPageNumber = 0
    private var kakaoNotificationSender: KakaoNotificationSender? = null

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
        filter.addAction(KakaoNotificationSender.ACTION_NAME)
        registerReceiver(screenOffReceiver, filter)

        dao = KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

        chatroomName = intent.getStringExtra("CHATROOM_NAME") ?: return
        intent.extras?.clear()

        binding.rvNotification.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter()
        binding.rvNotification.adapter = notificationAdapter

        viewModel = ViewModelProvider(this, KakaoNotificationViewModelFactory(dao)).get(
            KakaoNotificationViewModel::class.java
        )
        viewModel.fetchFirstPage(chatroomName, NOTIFICATION_PAGE_SIZE)

        var observer = Observer<List<KakaoNotificationEntity>> {
            notificationAdapter.update(it)
        }
        viewModel.notificationList.observe(this, observer)

        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                if (lastVisibleItemPosition == itemTotalCount) {
                    val notificationCount =
                        CoroutineManager.runSync { dao.count(chatroomName) }

                    if ((notificationPageNumber + 1) * NOTIFICATION_PAGE_SIZE < notificationCount) {
                        notificationPageNumber++
                        viewModel.fetchNextPage(
                            chatroomName,
                            notificationPageNumber,
                            NOTIFICATION_PAGE_SIZE
                        )
                    }

                }
            }
        })

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefsListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs: SharedPreferences, key: String ->
                notificationAdapter.notifyDataSetChanged()
            }
    }

    private fun initKakaoNotificationSender() {
        kakaoNotificationSender = kakaoNotificationSender ?: KakaoNotificationSender(this)
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        initKakaoNotificationSender()
        CoroutineManager.run {
            kakaoNotificationSender?.updateReadStatusAndSendNotification(chatroomName)
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
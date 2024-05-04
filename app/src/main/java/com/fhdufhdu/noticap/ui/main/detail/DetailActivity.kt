package com.fhdufhdu.noticap.ui.main.detail

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.databinding.ActivityNotificationBinding
import com.fhdufhdu.noticap.noti.manager.v2.CustomNotificationListenerService
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotification
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDao
import com.fhdufhdu.noticap.noti.manager.v3.KakaoNotificationDatabase
import com.fhdufhdu.noticap.ui.main.KakaoNotificationPerChatroomViewModel
import com.fhdufhdu.noticap.ui.main.KakaoNotificationPerChatroomViewModelFactory
import com.fhdufhdu.noticap.ui.main.MainActivity
import com.fhdufhdu.noticap.util.CoroutineManager


class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private var chatroomName: String = ""
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener
    private var NOTIFICATION_PAGE_SIZE: Int = 20
    private var notificationPageNumber = 0
    private lateinit var dao:KakaoNotificationDao
    private lateinit var viewModel: KakaoNotificationViewModel

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

        dao = KakaoNotificationDatabase.getInstance(applicationContext).kakaoNotificationDao()

        chatroomName = intent.getStringExtra("CHATROOM_NAME") ?: return
        intent.extras?.clear()

        binding.rvNotification.layoutManager = LinearLayoutManager(this)
        notificationAdapter = NotificationAdapter(applicationContext)
        binding.rvNotification.adapter = notificationAdapter

        viewModel = ViewModelProvider(this, KakaoNotificationViewModelFactory(dao)).get(
            KakaoNotificationViewModel::class.java
        )
        viewModel.fetchFirstPage(chatroomName, NOTIFICATION_PAGE_SIZE)

        var observer = Observer<List<KakaoNotification>>{
            notificationAdapter.update(it)
        }
        viewModel.notificationList.observe(this, observer)

        val thisInstance = this
        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 1
                if (lastVisibleItemPosition == itemTotalCount) {
                    val notificationCount =
                        CoroutineManager.runSync { dao.count(chatroomName) }

                    if ((notificationPageNumber + 1) * NOTIFICATION_PAGE_SIZE < notificationCount){
                        notificationPageNumber++
                        viewModel.fetchNextPage(chatroomName, notificationPageNumber, NOTIFICATION_PAGE_SIZE)
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

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onStop() {
        super.onStop()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("CHATROOM_NAME", chatroomName)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 1028, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val notificationManager = createNotificationChannel()
        CoroutineManager.run {
            dao.updateRead(chatroomName)
            val unreadChatroomNames = dao.selectUnreadChatrooms().map {
                "${it.chatroomName}(${it.unreadCount})"
            }
            if (unreadChatroomNames.isEmpty()) {
                notificationManager.cancel(1026)
            } else {
                val builder = NotificationCompat.Builder(this,
                    com.fhdufhdu.noticap.noti.manager.v3.CustomNotificationListenerService.CHANNEL_ID
                )
                    .setSmallIcon(
                        IconCompat.createWithResource(
                            this,
                            R.drawable.ic_notification
                        )
                    )
                    .setContentTitle("새로운 카카오톡 알림")
                    .setContentText(
                        unreadChatroomNames.joinToString(", ")
                    )
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(1026, builder.build())
            }
        }
    }

    private fun createNotificationChannel(): NotificationManager {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(com.fhdufhdu.noticap.noti.manager.v3.CustomNotificationListenerService.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return notificationManager
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
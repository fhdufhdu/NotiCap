package com.fhdufhdu.noticap.ui.main.detail

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fhdufhdu.noticap.R
import com.fhdufhdu.noticap.noti.manager.v2.CustomNotificationListenerService
import com.fhdufhdu.noticap.noti.manager.v2.KakaoNotificationDataManager


class DetailActivity : AppCompatActivity() {
    var chatroomName: String = ""
    lateinit var recyclerView: RecyclerView
    var notificationAdapter: NotificationAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

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

        chatroomName = intent.getStringExtra("CHATROOM_NAME")?:return
        intent.extras?.clear()

        recyclerView = findViewById<RecyclerView>(R.id.rv_notification)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setAdapter()

    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
    }

//    override fun onPause() {
//        super.onPause()
//        if (chatroomName != ""){
//            val notificationDataManager = NotificationDataManager.getInstance()
//            notificationDataManager.read(chatroomName)
//        }
//    }

    private fun setAdapter(){
        val kakaoNotificationDataManager = KakaoNotificationDataManager.getInstance()
        val notificationDataList = kakaoNotificationDataManager.notificationMap[chatroomName]

        if (notificationDataList != null){
            notificationAdapter = NotificationAdapter(notificationDataList)
            recyclerView.adapter = notificationAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter(){
        notificationAdapter?.notifyDataSetChanged()
    }

    inner class ScreenOffReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(Intent.ACTION_SCREEN_OFF == intent?.action){
                finish()
            }
            else if(CustomNotificationListenerService.ACTION_NAME == intent?.action){
                if (notificationAdapter == null) setAdapter()
                else updateAdapter()
            }
        }
    }

}
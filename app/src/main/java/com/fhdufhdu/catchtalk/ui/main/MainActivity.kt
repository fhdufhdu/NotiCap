package com.fhdufhdu.catchtalk.ui.main

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.fhdufhdu.catchtalk.R
import com.fhdufhdu.catchtalk.databinding.ActivityMainBinding
import com.fhdufhdu.catchtalk.notification.KakaoTalkNotificationListenerService
import com.fhdufhdu.catchtalk.ui.setting.SettingFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "설정"

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingFragment(), "setting_fragment")
            .commit()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                TedPermission
                    .create()
                    .setPermissionListener(
                        object : PermissionListener {
                            override fun onPermissionGranted() {}

                            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
                        },
                    ).setDeniedMessage("알림 권한을 허용하지 않으시면 앱을 사용하실 수 없습니다.")
                    .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
                    .check()
            }
        }
        if (!isNotiPermissionGranted()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        openBatteryOptimizationSettingDialog()
    }

    override fun onStart() {
        startForegroundService(Intent(this, KakaoTalkNotificationListenerService::class.java))
        super.onStart()
    }

    private fun isNotiPermissionGranted(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationListenerAccessGranted(
            ComponentName(
                application,
                KakaoTalkNotificationListenerService::class.java,
            ),
        )
    }

    private fun openBatteryOptimizationSettingDialog() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            // 화이트 리스트 등록 안됨.
            AlertDialog.Builder(this)
                .setTitle("배터리 최적화 제외")
                .setMessage("카카오톡 알림을 제대로 받아오시려면 배터리 최적화 제외가 필요합니다.")
                .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("설정하러 가기") { dialog, _ ->
                    val intent =
                        Intent().apply {
                            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                        }
                    startActivity(intent)
                    dialog.dismiss()
                }
                .show()
        }
    }
}

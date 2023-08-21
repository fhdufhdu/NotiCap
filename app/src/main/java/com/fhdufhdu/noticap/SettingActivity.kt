package com.fhdufhdu.noticap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.title = "설정"

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingFragment(), "setting_fragment")
            .commit()

    }
}
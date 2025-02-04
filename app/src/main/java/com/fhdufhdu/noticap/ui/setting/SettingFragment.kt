package com.fhdufhdu.noticap.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.fhdufhdu.noticap.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}

package com.fhdufhdu.catchtalk.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.fhdufhdu.catchtalk.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}

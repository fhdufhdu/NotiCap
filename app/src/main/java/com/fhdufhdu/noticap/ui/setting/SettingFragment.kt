package com.fhdufhdu.noticap.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.fhdufhdu.noticap.R


class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

//        val editTextPreference =
//            preferenceManager.findPreference<EditTextPreference>("max_noti")
//        editTextPreference!!.setOnBindEditTextListener { editText ->
//            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
//        }
    }
}
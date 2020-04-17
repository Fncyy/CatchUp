package com.example.catchup.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.example.catchup.R
import com.example.catchup.adapter.CategoryAdapter
import com.example.catchup.shared.library.LibraryCreator

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val categoriesPreference = SeekBarPreference(context)
        categoriesPreference.key = getString(R.string.KEY_CATEGORIES)
        categoriesPreference.title = getString(R.string.settings_categories)
        categoriesPreference.min = 1
        categoriesPreference.max = 8
        categoriesPreference.onPreferenceChangeListener = this

        val newsPreference = SeekBarPreference(context)
        newsPreference.key = getString(R.string.KEY_NEWS)
        newsPreference.title = getString(R.string.settings_news)
        newsPreference.min = 3
        newsPreference.max = 8
        newsPreference.onPreferenceChangeListener = this

        val settingsCategory = PreferenceCategory(context)
        settingsCategory.title = getString(R.string.settings_category_main)
        screen.addPreference(settingsCategory)
        settingsCategory.addPreference(categoriesPreference)
        settingsCategory.addPreference(newsPreference)

        val warningPreference = Preference(context)
        warningPreference.title = getString(R.string.settings_warning)
        warningPreference.icon = context.getDrawable(R.drawable.baseline_warning_black_48dp)

        val warningCategory = PreferenceCategory(context)
        warningCategory.title = getString(R.string.settings_category_warning)
        screen.addPreference(warningCategory)
        warningCategory.addPreference(warningPreference)

        preferenceScreen = screen
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when (preference?.key) {
            context?.getString(R.string.KEY_CATEGORIES) -> CategoryAdapter.MAX_SELECTED_COUNT =
                newValue.toString().toInt()
            context?.getString(R.string.KEY_NEWS) -> LibraryCreator.SAVED_NEWS_COUNT =
                newValue.toString().toInt()
        }
        return true
    }
}
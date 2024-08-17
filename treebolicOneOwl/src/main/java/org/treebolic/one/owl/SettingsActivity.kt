/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.owl

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.TreebolicIface
import org.treebolic.preference.OpenEditTextPreference
import org.treebolic.preference.OpenEditTextPreference.Companion.onDisplayPreferenceDialog

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
class SettingsActivity : AppCompatCommonPreferenceActivity() {

    // F R A G M E N T S

    class ProviderPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_provider)

            // bind
            val providerPreference = checkNotNull(findPreference(Settings.PREF_PROVIDER))
            providerPreference.summaryProvider = STRING_SUMMARY_PROVIDER
        }
    }

    class DataPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_data)

            // bind (can be either EditTextPreference or ListPreference or saving to string)
            val sourcePreference = checkNotNull(findPreference(TreebolicIface.PREF_SOURCE))
            setSummaryProvider(sourcePreference)

            val basePreference = checkNotNull(findPreference(TreebolicIface.PREF_BASE))
            setSummaryProvider(basePreference)

            val imageBasePreference = checkNotNull(findPreference(TreebolicIface.PREF_IMAGEBASE))
            setSummaryProvider(imageBasePreference)

            val settingsPreference = checkNotNull(findPreference(TreebolicIface.PREF_SETTINGS))
            setSummaryProvider(settingsPreference)
        }

        private fun setSummaryProvider(preference: Preference) {
            when (preference) {
                is EditTextPreference -> {
                    preference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance())
                }

                is ListPreference -> {
                    preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance())
                }

                else -> {
                    preference.summaryProvider = STRING_SUMMARY_PROVIDER
                }
            }
        }
    }

    class DownloadPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_download)

            // override
            val preference = checkNotNull(findPreference<OpenEditTextPreference>(Settings.PREF_DOWNLOAD))
            preference.values = resources.getStringArray(R.array.pref_download_urls)

            // bind
            preference.summaryProvider = OpenEditTextPreference.SUMMARY_PROVIDER
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (!onDisplayPreferenceDialog(this, preference)) {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    companion object {

        // S U M M A R Y

        /**
         * Summary provider for string
         */
        private val STRING_SUMMARY_PROVIDER = SummaryProvider { preference: Preference ->
            val context = preference.context
            val sharedPrefs = checkNotNull(preference.sharedPreferences)
            val value = sharedPrefs.getString(preference.key, null)
            value ?: context.getString(R.string.pref_value_default)
        }
    }
}

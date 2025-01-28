/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

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
            val providerPreference: Preference = findPreference(Settings.PREF_PROVIDER)!!
            providerPreference.summaryProvider = STRING_SUMMARY_PROVIDER
        }
    }

    class DataPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_data)

            // bind (can be either EditTextPreference or ListPreference or saving to string)
            val sourcePreference: Preference = findPreference(TreebolicIface.PREF_SOURCE)!!
            setSummaryProvider(sourcePreference)

            val basePreference: Preference = findPreference(TreebolicIface.PREF_BASE)!!
            setSummaryProvider(basePreference)

            val imageBasePreference: Preference = findPreference(TreebolicIface.PREF_IMAGEBASE)!!
            setSummaryProvider(imageBasePreference)

            val restrictPreference: Preference = findPreference(Settings.PREF_TRUNCATE)!!
            setSummaryProvider(restrictPreference)

            val extraRestrictPreference: Preference = findPreference(Settings.PREF_PRUNE)!!
            setSummaryProvider(extraRestrictPreference)

            val settingsPreference: Preference = findPreference(TreebolicIface.PREF_SETTINGS)!!
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
            val preference: OpenEditTextPreference = findPreference(Settings.PREF_DOWNLOAD)!!
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
            val value = preference.sharedPreferences!!.getString(preference.key, null)
            value ?: preference.context.getString(R.string.pref_value_default)
        }
    }
}

package org.treebolic.one.sql;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.preference.OpenEditTextPreference;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
public class SettingsActivity extends AppCompatCommonPreferenceActivity
{
	// S U M M A R Y

	/**
	 * Summary provider for string
	 */
	static private final Preference.SummaryProvider<Preference> STRING_SUMMARY_PROVIDER = (preference) -> {

		final Context context = preference.getContext();
		final SharedPreferences sharedPrefs = preference.getSharedPreferences();
		final String value = sharedPrefs.getString(preference.getKey(), null);
		return value == null ? context.getString(R.string.pref_value_default) : value;
	};

	// F R A G M E N T S

	@SuppressWarnings("WeakerAccess")
	public static class ProviderPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_provider);

			// bind
			final Preference providerPreference = findPreference(Settings.PREF_PROVIDER);
			assert providerPreference != null;
			providerPreference.setSummaryProvider(STRING_SUMMARY_PROVIDER);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class DataPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_data);

			// bind (can be either EditTextPreference or ListPreference or saving to string)
			final Preference sourcePreference = findPreference(TreebolicIface.PREF_SOURCE);
			assert sourcePreference != null;
			setSummaryProvider(sourcePreference);

			final Preference basePreference = findPreference(TreebolicIface.PREF_BASE);
			assert basePreference != null;
			setSummaryProvider(basePreference);

			final Preference imageBasePreference = findPreference(TreebolicIface.PREF_IMAGEBASE);
			assert imageBasePreference != null;
			setSummaryProvider(imageBasePreference);

			final Preference restrictPreference = findPreference(Settings.PREF_TRUNCATE);
			assert restrictPreference != null;
			setSummaryProvider(restrictPreference);

			final Preference extraRestrictPreference = findPreference(Settings.PREF_PRUNE);
			assert extraRestrictPreference != null;
			setSummaryProvider(extraRestrictPreference);

			final Preference settingsPreference = findPreference(TreebolicIface.PREF_SETTINGS);
			assert settingsPreference != null;
			setSummaryProvider(settingsPreference);
		}

		private void setSummaryProvider(@NonNull final Preference preference)
		{
			if (preference instanceof EditTextPreference)
			{
				preference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
			}
			else if (preference instanceof ListPreference)
			{
				preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
			}
			else
			{
				preference.setSummaryProvider(STRING_SUMMARY_PROVIDER);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class DownloadPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_download);

			// override
			final OpenEditTextPreference preference = findPreference(Settings.PREF_DOWNLOAD);
			assert preference != null;
			preference.setValues(getResources().getStringArray(R.array.pref_download_urls));

			// bind
			preference.setSummaryProvider(OpenEditTextPreference.SUMMARY_PROVIDER);
		}

		@Override
		public void onDisplayPreferenceDialog(final Preference preference)
		{
			if (!OpenEditTextPreference.onDisplayPreferenceDialog(this, preference))
			{
				super.onDisplayPreferenceDialog(preference);
			}
		}
	}
}

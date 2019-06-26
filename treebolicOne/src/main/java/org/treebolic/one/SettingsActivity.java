package org.treebolic.one;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.preference.OpenEditTextPreference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.legacy.contrib.Header;

/**
 * A AppCompatPreferenceActivity that presents a set of application settings.
 *
 * @author Bernard Bou *
 */
public class SettingsActivity extends AppCompatCommonPreferenceActivity
{
	// E V E N T S

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		// super
		super.onCreate(savedInstanceState);

		// toolbar
		setupToolbar(R.layout.toolbar, R.id.toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// S E T U P

	private static final List<String> allowedFragments = new ArrayList<>();

	@Override
	public void onBuildHeaders(@NonNull final List<Header> target)
	{
		loadHeadersFromResource(R.xml.pref_headers, target);

		// allowed fragments
		SettingsActivity.allowedFragments.clear();
		for (Header header : target)
		{
			SettingsActivity.allowedFragments.add(header.fragment);
		}
	}

	@Override
	public boolean isValidFragment(final String fragmentName)
	{
		return SettingsActivity.allowedFragments.contains(fragmentName);
	}

	// D E T E C T I O N

	@Override
	public boolean onIsMultiPane()
	{
		return SettingsActivity.isLargeTablet(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	 */
	private static boolean isLargeTablet(final Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

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

	public static class ProviderPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_provider);

			// bind
			final Preference providerPreference = findPreference(Settings.PREF_PROVIDER);
			assert providerPreference != null;
			providerPreference.setSummaryProvider(STRING_SUMMARY_PROVIDER);
		}
	}

	public static class DataPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_data);

			// bind
			final Preference sourcePreference = findPreference(TreebolicIface.PREF_SOURCE);
			assert sourcePreference != null;
			setSummaryProvider(sourcePreference);

			// entry is optional
			final Preference sourceEntryPreference = findPreference(Settings.PREF_SOURCE_ENTRY);
			if (sourceEntryPreference != null)
			{
				setSummaryProvider(sourceEntryPreference);
			}

			final Preference basePreference = findPreference(TreebolicIface.PREF_BASE);
			assert basePreference != null;
			setSummaryProvider(basePreference);

			final Preference imageBasePreference = findPreference(TreebolicIface.PREF_IMAGEBASE);
			assert imageBasePreference != null;
			setSummaryProvider(imageBasePreference);

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

	public static class DownloadPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(@SuppressWarnings("unused") final Bundle savedInstanceState, @SuppressWarnings("unused") final String rootKey)
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

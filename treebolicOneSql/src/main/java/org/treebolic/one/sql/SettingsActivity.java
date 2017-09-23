package org.treebolic.one.sql;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7app.contrib.AppCompatPreferenceActivity;
import android.view.MenuItem;

import org.treebolic.TreebolicIface;
import org.treebolic.preference.OpenEditTextPreference;

import java.util.List;

/**
 * A AppCompatPreferenceActivity that presents a set of application settings.
 *
 * @author Bernard Bou
 */
public class SettingsActivity extends AppCompatPreferenceActivity
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
		switch (item.getItemId())
		{
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBuildHeaders(final List<Header> target)
	{
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	// S E T U P

	@Override
	protected boolean isValidFragment(final String fragmentName)
	{
		return ProviderPreferenceFragment.class.getName().equals(fragmentName) || //
				DataPreferenceFragment.class.getName().equals(fragmentName) || //
				DownloadPreferenceFragment.class.getName().equals(fragmentName);
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

	// L I S T E N E R

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	static private final Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener()
	{
		@Override
		public boolean onPreferenceChange(final Preference preference, final Object value)
		{
			// set the summary to the value's simple string representation.
			final String stringValue = value == null ? "" : value.toString();
			preference.setSummary(stringValue);
			return true;
		}
	};

	// B I N D S U M M A R Y

	/**
	 * Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below the preference
	 * title) is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact display format is dependent on the
	 * type of preference.
	 *
	 * @see #listener
	 */
	static private void bind(final Preference preference, final String value0, @SuppressWarnings("SameParameterValue") final OnPreferenceChangeListener listener0)
	{
		// set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(listener0);

		// trigger the listener immediately with the preference's current value.
		listener0.onPreferenceChange(preference, value0);
	}

	// F R A G M E N T S

	public static class ProviderPreferenceFragment extends PreferenceFragment
	{
		@SuppressWarnings({"synthetic-access"})
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// inflate
			addPreferencesFromResource(R.xml.pref_provider);

			// activity
			final AppCompatActivity activity = (AppCompatActivity) getActivity();

			// bind
			final Preference providerPreference = findPreference(Settings.PREF_PROVIDER);
			final String value = Settings.getStringPref(activity, providerPreference.getKey());
			SettingsActivity.bind(providerPreference, value, SettingsActivity.listener);
		}
	}

	public static class DataPreferenceFragment extends PreferenceFragment
	{
		@SuppressWarnings("synthetic-access")
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// inflate
			addPreferencesFromResource(R.xml.pref_data);

			// activity
			final AppCompatActivity activity = (AppCompatActivity) getActivity();

			// bind
			final Preference sourcePreference = findPreference(TreebolicIface.PREF_SOURCE);
			if (sourcePreference != null)
			{
				final String value = Settings.getStringPref(activity, sourcePreference.getKey());
				SettingsActivity.bind(sourcePreference, value, SettingsActivity.listener);
			}

			final Preference basePreference = findPreference(TreebolicIface.PREF_BASE);
			if (basePreference != null)
			{
				final String value = Settings.getStringPref(activity, basePreference.getKey());
				SettingsActivity.bind(basePreference, value, SettingsActivity.listener);
			}

			final Preference imageBasePreference = findPreference(TreebolicIface.PREF_IMAGEBASE);
			if (imageBasePreference != null)
			{
				final String value = Settings.getStringPref(activity, imageBasePreference.getKey());
				SettingsActivity.bind(imageBasePreference, value, SettingsActivity.listener);
			}

			final Preference restrictPreference = findPreference(Settings.PREF_TRUNCATE);
			if (restrictPreference != null)
			{
				final String value = Settings.getStringPref(activity, restrictPreference.getKey());
				SettingsActivity.bind(restrictPreference, value, SettingsActivity.listener);
			}

			final Preference extraRestrictPreference = findPreference(Settings.PREF_PRUNE);
			if (extraRestrictPreference != null)
			{
				final String value = Settings.getStringPref(activity, extraRestrictPreference.getKey());
				SettingsActivity.bind(extraRestrictPreference, value, SettingsActivity.listener);
			}

			final Preference settingsPreference = findPreference(TreebolicIface.PREF_SETTINGS);
			if (settingsPreference != null)
			{
				final String value = Settings.getStringPref(activity, settingsPreference.getKey());
				SettingsActivity.bind(settingsPreference, value, SettingsActivity.listener);
			}
		}
	}

	public static class DownloadPreferenceFragment extends PreferenceFragment
	{
		@SuppressWarnings("synthetic-access")
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// inflate
			addPreferencesFromResource(R.xml.pref_download);

			// override
			final OpenEditTextPreference pref = (OpenEditTextPreference) findPreference(Settings.PREF_DOWNLOAD);
			pref.setValues(getResources().getStringArray(R.array.pref_download_urls));

			// bind
			final AppCompatActivity activity = (AppCompatActivity) getActivity();
			final Preference preference = findPreference(Settings.PREF_DOWNLOAD);
			final String value = Settings.getStringPref(activity, preference.getKey());
			SettingsActivity.bind(preference, value, SettingsActivity.listener);
		}
	}
}

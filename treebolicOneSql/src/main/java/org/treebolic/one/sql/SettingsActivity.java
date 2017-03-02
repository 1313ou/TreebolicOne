package org.treebolic.one.sql;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.contrib.AppCompatPreferenceActivity;
import android.view.MenuItem;

import org.treebolic.TreebolicIface;
import org.treebolic.preference.AutoEditTextPreference;

import java.util.List;

/**
 * A AppCompatPreferenceActivity that presents a set of application settings. On handset devices, settings are presented as a single list. On tablets, settings
 * are split by category, with category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android Design: Settings</a> for design guidelines and the
 * <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity
{
	/**
	 * Determines whether to always show the simplified settings UI, where settings are presented in a single list. When false, settings are shown as a
	 * master/detail two-pane view on tablets. When true, a single pane is shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	/**
	 * Subactions
	 */
	public static final String ACTION_DATA = "org.treebolic.one.sql.prefs.DATA";
	public static final String ACTION_PROVIDER = "org.treebolic.one.sql.prefs.PROVIDER";
	public static final String ACTION_DOWNLOAD = "org.treebolic.one.sql.prefs.DOWNLOAD";

	// E V E N T S

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		// super
		super.onCreate(savedInstanceState);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);

		if (SettingsActivity.isSimplePreferences(this))
		{
			setupSimplePreferencesScreen();
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
		if (!SettingsActivity.isSimplePreferences(this))
		{
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	// S E T U P

	@Override
	protected boolean isValidFragment(final String fragmentName)
	{
		return ProviderPreferenceFragment.class.getName().equals(fragmentName) || //
				DataPreferenceFragment.class.getName().equals(fragmentName) || //
				DownloadPreferenceFragment.class.getName().equals(fragmentName);
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the device configuration dictates that a simplified, single-pane UI should be shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen()
	{
		// In the simplified UI, fragments are not used at all and we instead use the older PreferenceActivity APIs.
		final String action = getIntent().getAction();
		if (action != null)
		{
			switch (action)
			{
				case SettingsActivity.ACTION_DATA:
					addPreferencesFromResource(R.xml.pref_data);
					break;
				case SettingsActivity.ACTION_PROVIDER:
				{
					addPreferencesFromResource(R.xml.pref_provider);
					final Preference pref = findPreference(Settings.PREF_PROVIDER);
					final String key = pref.getKey();
					pref.setSummary(Settings.getStringPref(this, key));
					break;
				}
				case SettingsActivity.ACTION_DOWNLOAD:
				{
					addPreferencesFromResource(R.xml.pref_download);
					final Preference pref = findPreference(Settings.PREF_DOWNLOAD);
					final String key = pref.getKey();
					pref.setSummary(Settings.getStringPref(this, key));
					break;
				}
			}
		}
		else
		{
			// Load the legacy preferences headers
			addPreferencesFromResource(R.xml.pref_headers_legacy);
		}
	}

	// D E T E C T I O N

	@Override
	public boolean onIsMultiPane()
	{
		return SettingsActivity.isLargeTablet(this) && !SettingsActivity.isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	 */
	private static boolean isLargeTablet(final Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't
	 * have newer APIs like {@link PreferenceFragment}, or the device doesn't have an extra-large screen. In these cases, a single-pane "simplified" settings UI
	 * should be shown.
	 */
	private static boolean isSimplePreferences(final Context context)
	{
		//noinspection ConstantConditions
		return SettingsActivity.ALWAYS_SIMPLE_PREFS || !SettingsActivity.isLargeTablet(context);
	}

	// L I S T E N E R

	/**
	 * A preference value change listener that updates the preference's summary to reflect its new value.
	 */
	private final Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener()
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
	private void bind(final Preference preference, final String value0, final OnPreferenceChangeListener listener0)
	{
		// set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(listener0);

		// trigger the listener immediately with the preference's current value.
		this.listener.onPreferenceChange(preference, value0);
	}

	// F R A G M E N T S

	public static class ProviderPreferenceFragment extends PreferenceFragment
	{
		@SuppressWarnings({ "synthetic-access" })
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);

			// inflate
			addPreferencesFromResource(R.xml.pref_provider);

			// activity
			final SettingsActivity activity = (SettingsActivity) getActivity();

			// bind
			final Preference providerPreference = findPreference(Settings.PREF_PROVIDER);
			final String value = Settings.getStringPref(activity, providerPreference.getKey());
			activity.bind(providerPreference, value, activity.listener);
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
			final SettingsActivity activity = (SettingsActivity) getActivity();

			// bind
			final Preference sourcePreference = findPreference(TreebolicIface.PREF_SOURCE);
			if (sourcePreference != null)
			{
				final String value = Settings.getStringPref(activity, sourcePreference.getKey());
				activity.bind(sourcePreference, value, activity.listener);
			}

			final Preference sourceEntryPreference = findPreference(Settings.PREF_SOURCE_ENTRY);
			if (sourceEntryPreference != null)
			{
				final String value = Settings.getStringPref(activity, sourceEntryPreference.getKey());
				activity.bind(sourceEntryPreference, value, activity.listener);
			}

			final Preference basePreference = findPreference(TreebolicIface.PREF_BASE);
			if (basePreference != null)
			{
				final String value = Settings.getStringPref(activity, basePreference.getKey());
				activity.bind(basePreference, value, activity.listener);
			}

			final Preference imageBasePreference = findPreference(TreebolicIface.PREF_IMAGEBASE);
			if (imageBasePreference != null)
			{
				final String value = Settings.getStringPref(activity, imageBasePreference.getKey());
				activity.bind(imageBasePreference, value, activity.listener);
			}

			final Preference restrictPreference = findPreference(Settings.PREF_TRUNCATE);
			if (restrictPreference != null)
			{
				final String value = Settings.getStringPref(activity, restrictPreference.getKey());
				activity.bind(restrictPreference, value, activity.listener);
			}

			final Preference extraRestrictPreference = findPreference(Settings.PREF_PRUNE);
			if (extraRestrictPreference != null)
			{
				final String value = Settings.getStringPref(activity, extraRestrictPreference.getKey());
				activity.bind(extraRestrictPreference, value, activity.listener);
			}

			final Preference settingsPreference = findPreference(TreebolicIface.PREF_SETTINGS);
			if (settingsPreference != null)
			{
				final String value = Settings.getStringPref(activity, settingsPreference.getKey());
				activity.bind(settingsPreference, value, activity.listener);
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
			final AutoEditTextPreference pref = (AutoEditTextPreference) findPreference(Settings.PREF_DOWNLOAD);
			pref.setValues(getResources().getStringArray(R.array.pref_download_urls));

			// bind
			final SettingsActivity activity = (SettingsActivity) getActivity();
			final Preference preference = findPreference(Settings.PREF_DOWNLOAD);
			final String value = Settings.getStringPref(activity, preference.getKey());
			activity.bind(preference, value, activity.listener);
		}
	}
}

package org.treebolic.one.sql;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import org.treebolic.TreebolicIface;
import org.treebolic.storage.Storage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Settings
 *
 * @author Bernard Bou
 */
public class Settings
{
	/**
	 * Initialized preference name
	 */
	public static final String PREF_INITIALIZED = "pref_initialized"; //$NON-NLS-1$

	/**
	 * First preference name
	 */
	public static final String PREF_FIRSTRUN = "pref_first_run"; //$NON-NLS-1$

	/**
	 * Style preference name
	 */
	public static final String PREF_STYLE = "pref_style"; //$NON-NLS-1$

	/**
	 * Source entry preference name
	 */
	public static final String PREF_SOURCE_ENTRY = "pref_source_entry"; //$NON-NLS-1$

	/**
	 * Download preference name
	 */
	public static final String PREF_DOWNLOAD = "pref_download"; //$NON-NLS-1$

	/**
	 * Provider preference name
	 */
	public static final String PREF_PROVIDER = "pref_provider"; //$NON-NLS-1$

	/**
	 * Mimetype preference name
	 */
	public static final String PREF_MIMETYPE = "pref_mimetype"; //$NON-NLS-1$

	/**
	 * File extensions preference name
	 */
	public static final String PREF_EXTENSIONS = "pref_extensions"; //$NON-NLS-1$

	/**
	 * URL scheme preference name
	 */
	public static final String PREF_URLSCHEME = "pref_urlscheme"; //$NON-NLS-1$

	/**
	 * Truncate restrict preference name
	 */
	public static final String PREF_TRUNCATE = "pref_truncate"; //$NON-NLS-1$

	/**
	 * Prune restrict preference name
	 */
	public static final String PREF_PRUNE = "pref_prune"; //$NON-NLS-1$

	/**
	 * Provider
	 */
	public static final String PROVIDER = "treebolic.provider.sqlite.Provider"; //$NON-NLS-1$

	/**
	 * URL scheme
	 */
	// public static final String URLSCHEME = "sql:";

	/**
	 * Data
	 */
	public static final String DATA = "data.zip"; //$NON-NLS-1$

	/**
	 * Default CSS
	 */
	public static final String STYLE_DEFAULT = ".content { }\n" + // //$NON-NLS-1$
			".link {color: #FFA500;font-size: small;}\n" + // //$NON-NLS-1$
			".linking {color: #FFA500; font-size: small; }" + // //$NON-NLS-1$
			".mount {color: #CD5C5C; font-size: small;}" + // //$NON-NLS-1$
			".mounting {color: #CD5C5C; font-size: small; }" + // //$NON-NLS-1$
			".searching {color: #FF7F50; font-size: small; }"; //$NON-NLS-1$

	/**
	 * Set providers default settings from provider data
	 *
	 * @param context
	 *            context
	 */
	@SuppressLint("CommitPrefEdits")
	static public void setDefaults(final Context context)
	{
		final Resources resources = context.getResources();

		// provider
		final String provider = resources.getString(R.string.provider_class);
		final String mime = resources.getString(R.string.provider_mime);
		final String extensions = resources.getString(R.string.provider_extension);

		// data
		final String source = resources.getString(R.string.source_default);
		final String settings = resources.getString(R.string.settings);
		final String urlScheme = resources.getString(R.string.urlScheme);
		final String truncate = resources.getString(R.string.truncate_default);
		final String prune = resources.getString(R.string.prune_default);

		// storage
		final File treebolicStorage = Storage.getTreebolicStorage(context);
		final Uri uri = Uri.fromFile(treebolicStorage);
		final String treebolicBase = uri.toString() + '/';

		// preferences
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor editor = sharedPref.edit();

		editor.putString(Settings.PREF_PROVIDER, provider);
		editor.putString(Settings.PREF_MIMETYPE, mime);
		editor.putString(Settings.PREF_EXTENSIONS, extensions);
		editor.putString(Settings.PREF_URLSCHEME, urlScheme);
		editor.putString(Settings.PREF_TRUNCATE, truncate);
		editor.putString(Settings.PREF_PRUNE, prune);

		editor.putString(TreebolicIface.PREF_SOURCE, source);
		editor.putString(TreebolicIface.PREF_SETTINGS, settings);

		editor.putString(TreebolicIface.PREF_BASE, treebolicBase);
		editor.putString(TreebolicIface.PREF_IMAGEBASE, treebolicBase);

		editor.commit();
	}

	/**
	 * Put string preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	@SuppressLint("CommitPrefEdits")
	static public void putStringPref(final Context context, final String key, final String value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putString(key, value).commit();
	}

	/**
	 * Clear preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 */
	static public void clearPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().remove(key).apply();
	}

	/**
	 * Put integer preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	@SuppressLint("CommitPrefEdits")
	static public void putIntPref(final Context context, final String key, final int value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putInt(key, value).commit();
	}

	/**
	 * Get string preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @return value
	 */
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Get int preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @return value
	 */
	static public int getIntPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getInt(key, 0);
	}

	/**
	 * Get preference value as url
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @return preference value as
	 */
	static public URL getURLPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String result = sharedPref.getString(key, null);
		return Settings.makeURL(result);
	}

	// U T I L S

	/**
	 * Make URL from string
	 *
	 * @param url
	 *            url string
	 * @return url
	 */
	static public URL makeURL(final String url)
	{
		try
		{
			return new URL(url);
		}
		catch (final MalformedURLException e)
		{
			return null;
		}
	}

	/**
	 * Application settings
	 *
	 * @param context
	 *            context
	 * @param pkgName
	 *            package name
	 */
	static public void applicationSettings(final Context context, final String pkgName)
	{
		final int apiLevel = Build.VERSION.SDK_INT;
		final Intent intent = new Intent();

		if (apiLevel >= 9)
		{
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + pkgName)); //$NON-NLS-1$
		}
		else
		{
			final String appPkgName = apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName"; //$NON-NLS-1$ //$NON-NLS-2$

			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails"); //$NON-NLS-1$ //$NON-NLS-2$
			intent.putExtra(appPkgName, pkgName);
		}

		// start Activity
		context.startActivity(intent);
	}

	/**
	 * Get query file
	 *
	 * @return query file
	 */
	static public File getQuery(final Context context)
	{
		final String source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		if (source != null && !source.isEmpty())
		{
			final String base = Settings.getStringPref(context, TreebolicIface.PREF_BASE);
			//noinspection TryWithIdenticalCatches
			try
			{
				final URL url = new URL(base);
				final File basedir = new File(url.toURI());
				return new File(basedir, source);
			}
			catch (URISyntaxException e)
			{
				//
			}
			catch (MalformedURLException e)
			{
				//
			}
		}
		return null;
	}

	/**
	 * Get base URL
	 *
	 * @return base URL
	 */
	static public URL getBase(final Context context)
	{
		final String base = Settings.getStringPref(context, TreebolicIface.PREF_BASE);
		try
		{
			return new URL(base);
		}
		catch (MalformedURLException e)
		{
			//
		}
		return null;
	}
}

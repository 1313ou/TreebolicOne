/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.one.owl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;

import org.treebolic.TreebolicIface;
import org.treebolic.storage.Storage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

/**
 * Settings
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Settings
{
	/**
	 * Initialized preference name
	 */
	public static final String PREF_INITIALIZED = "pref_initialized";

	/**
	 * First preference name
	 */
	public static final String PREF_FIRSTRUN = "pref_first_run";

	/**
	 * Style preference name
	 */
	public static final String PREF_STYLE = "pref_style";

	/**
	 * Download preference name
	 */
	public static final String PREF_DOWNLOAD = "pref_download";

	/**
	 * Provider preference name
	 */
	public static final String PREF_PROVIDER = "pref_provider";

	/**
	 * Mimetype preference name
	 */
	public static final String PREF_MIMETYPE = "pref_mimetype";

	/**
	 * File extensions preference name
	 */
	public static final String PREF_EXTENSIONS = "pref_extensions";

	/**
	 * URL scheme preference name
	 */
	public static final String PREF_URLSCHEME = "pref_urlscheme";

	/**
	 * Provider
	 */
	public static final String PROVIDER = "treebolic.provider.owl.Provider2";

	/*
	  URL scheme
	 */
	// public static final String URLSCHEME = "owl:";

	/**
	 * Data
	 */
	public static final String DATA = "data.zip";

	/**
	 * Default CSS
	 */
	public static final String STYLE_DEFAULT = ".content { }\n" + //
			".link {color: #FFA500;font-size: small;}\n" + //
			".linking {color: #FFA500; font-size: small; }" + //
			".mount {color: #CD5C5C; font-size: small;}" + //
			".mounting {color: #CD5C5C; font-size: small; }" + //
			".searching {color: #FF7F50; font-size: small; }";

	/**
	 * Set providers default settings from provider data
	 *
	 * @param context context
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void setDefaults(@NonNull final Context context)
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

		editor.putString(TreebolicIface.PREF_SOURCE, source);
		editor.putString(TreebolicIface.PREF_SETTINGS, settings);

		editor.putString(TreebolicIface.PREF_BASE, treebolicBase);
		editor.putString(TreebolicIface.PREF_IMAGEBASE, treebolicBase);

		editor.commit();
	}

	/**
	 * Put string preference
	 *
	 * @param context context
	 * @param key     key
	 * @param value   value
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void putStringPref(@NonNull final Context context, @SuppressWarnings("SameParameterValue") final String key, final String value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putString(key, value).commit();
	}

	/**
	 * Clear preference
	 *
	 * @param context context
	 * @param key     key
	 */
	static public void clearPref(@NonNull final Context context, @SuppressWarnings("SameParameterValue") final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().remove(key).apply();
	}

	/**
	 * Put integer preference
	 *
	 * @param context context
	 * @param key     key
	 * @param value   value
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void putIntPref(@NonNull final Context context, final String key, final int value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putInt(key, value).commit();
	}

	/**
	 * Get string preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	@Nullable
	static public String getStringPref(@NonNull final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Get int preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	static public int getIntPref(@NonNull final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getInt(key, 0);
	}

	/**
	 * Get preference value as url
	 *
	 * @param context context
	 * @param key     key
	 * @return preference value as
	 */
	@Nullable
	static public URL getURLPref(@NonNull final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String result = sharedPref.getString(key, null);
		return Settings.makeURL(result);
	}

	// U T I L S

	/**
	 * Make URL from string
	 *
	 * @param url url string
	 * @return url
	 */
	@Nullable
	static public URL makeURL(final String url)
	{
		try
		{
			return new URL(url);
		}
		catch (@NonNull final MalformedURLException ignored)
		{
			return null;
		}
	}

	/**
	 * Application settings
	 *
	 * @param context context
	 * @param pkgName package name
	 */
	static public void applicationSettings(@NonNull final Context context, final String pkgName)
	{
		final int apiLevel = Build.VERSION.SDK_INT;
		final Intent intent = new Intent();

		if (apiLevel >= 9)
		{
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + pkgName));
		}
		else
		{
			final String appPkgName = apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName";

			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, pkgName);
		}

		// start activity
		context.startActivity(intent);
	}

	/**
	 * Get query file
	 *
	 * @return query file
	 */
	@Nullable
	static public File getQuery(@NonNull final Context context)
	{
		final String source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		if (source != null && !source.isEmpty())
		{
			final String base = Settings.getStringPref(context, TreebolicIface.PREF_BASE);
			try
			{
				final URL url = new URL(base);
				final File basedir = new File(url.toURI());
				return new File(basedir, source);
			}
			catch (URISyntaxException | MalformedURLException ignored)
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
	@Nullable
	static public URL getBase(@NonNull final Context context)
	{
		final String base = Settings.getStringPref(context, TreebolicIface.PREF_BASE);
		try
		{
			return new URL(base);
		}
		catch (MalformedURLException ignored)
		{
			//
		}
		return null;
	}
}

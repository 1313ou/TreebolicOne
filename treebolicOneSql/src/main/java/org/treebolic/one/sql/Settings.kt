/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.preference.PreferenceManager
import org.treebolic.TreebolicIface
import org.treebolic.storage.Storage.getTreebolicStorage
import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

/**
 * Settings
 *
 * @author Bernard Bou
 */
object Settings {

    /**
     * Initialized preference name
     */
    const val PREF_INITIALIZED: String = "pref_initialized"

    /**
     * First preference name
     */
    const val PREF_FIRSTRUN: String = "pref_first_run"

    /**
     * Style preference name
     */
    const val PREF_STYLE: String = "pref_style"

    /**
     * Download preference name
     */
    const val PREF_DOWNLOAD: String = "pref_download"

    /**
     * Provider preference name
     */
    const val PREF_PROVIDER: String = "pref_provider"

    /**
     * Mimetype preference name
     */
    const val PREF_MIMETYPE: String = "pref_mimetype"

    /**
     * File extensions preference name
     */
    const val PREF_EXTENSIONS: String = "pref_extensions"

    /**
     * URL scheme preference name
     */
    const val PREF_URLSCHEME: String = "pref_urlscheme"

    /**
     * Truncate restrict preference name
     */
    const val PREF_TRUNCATE: String = "pref_truncate"

    /**
     * Prune restrict preference name
     */
    const val PREF_PRUNE: String = "pref_prune"

    /**
     * Provider
     */
    const val PROVIDER: String = "treebolic.provider.sqlite.Provider"

    /**
     * Data
     */
    const val DATA: String = "data.zip"

    /**
     * Default CSS
     */
    const val STYLE_DEFAULT: String = ".content { }\n" +  //
            ".link {color: #FFA500;font-size: small;}\n" +  //
            ".linking {color: #FFA500; font-size: small; }" +  //
            ".mount {color: #CD5C5C; font-size: small;}" +  //
            ".mounting {color: #CD5C5C; font-size: small; }" +  //
            ".searching {color: #FF7F50; font-size: small; }"

    /**
     * Set providers default settings from provider data
     *
     * @param context context
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun setDefaults(context: Context) {
        val resources = context.resources

        // provider
        val provider = resources.getString(R.string.provider_class)
        val mime = resources.getString(R.string.provider_mime)
        val extensions = resources.getString(R.string.provider_extension)

        // data
        val source = resources.getString(R.string.source_default)
        val settings = resources.getString(R.string.settings)
        val urlScheme = resources.getString(R.string.urlScheme)
        val truncate = resources.getString(R.string.truncate_default)
        val prune = resources.getString(R.string.prune_default)

        // storage
        val treebolicStorage = getTreebolicStorage(context)
        val uri = Uri.fromFile(treebolicStorage)
        val treebolicBase = "$uri/"

        // preferences
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()

        editor.putString(PREF_PROVIDER, provider)
        editor.putString(PREF_MIMETYPE, mime)
        editor.putString(PREF_EXTENSIONS, extensions)
        editor.putString(PREF_URLSCHEME, urlScheme)
        editor.putString(PREF_TRUNCATE, truncate)
        editor.putString(PREF_PRUNE, prune)

        editor.putString(TreebolicIface.PREF_SOURCE, source)
        editor.putString(TreebolicIface.PREF_SETTINGS, settings)

        editor.putString(TreebolicIface.PREF_BASE, treebolicBase)
        editor.putString(TreebolicIface.PREF_IMAGEBASE, treebolicBase)

        editor.commit()
    }

    /**
     * Put string preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun putStringPref(context: Context, key: String?, value: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putString(key, value).commit()
    }

    /**
     * Clear preference
     *
     * @param context context
     * @param key     key
     */
    @JvmStatic
    fun clearPref(context: Context, key: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().remove(key).apply()
    }

    /**
     * Put integer preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun putIntPref(context: Context, key: String?, value: Int) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putInt(key, value).commit()
    }

    /**
     * Get string preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    fun getStringPref(context: Context, key: String?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString(key, null)
    }

    /**
     * Get int preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    fun getIntPref(context: Context, key: String?): Int {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getInt(key, 0)
    }

    /**
     * Get preference value as url
     *
     * @param context context
     * @param key     key
     * @return preference value as
     */
    @JvmStatic
    fun getURLPref(context: Context, key: String?): URL? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val result = sharedPref.getString(key, null)
        return makeURL(result)
    }

    // U T I L S

    /**
     * Make URL from string
     *
     * @param url url string
     * @return url
     */
    private fun makeURL(url: String?): URL? {
        return try {
            URL(url)
        } catch (ignored: MalformedURLException) {
            null
        }
    }

    /**
     * Application settings
     *
     * @param context context
     * @param pkgName package name
     */
    fun applicationSettings(context: Context, pkgName: String) {
        val apiLevel = Build.VERSION.SDK_INT
        val intent = Intent()

        if (apiLevel >= 9) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$pkgName"))
        } else {
            val appPkgName = if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"

            intent.setAction(Intent.ACTION_VIEW)
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, pkgName)
        }

        // start activity
        context.startActivity(intent)
    }

    /**
     * Get query file
     *
     * @return query file
     */
    fun getQuery(context: Context): File? {
        val source = getStringPref(context, TreebolicIface.PREF_SOURCE)
        if (!source.isNullOrEmpty()) {
            val base = getStringPref(context, TreebolicIface.PREF_BASE)
            try {
                val url = URL(base)
                val basedir = File(url.toURI())
                return File(basedir, source)
            } catch (ignored: URISyntaxException) {
                //
            } catch (ignored: MalformedURLException) {
            }
        }
        return null
    }

    /**
     * Get base URL
     *
     * @return base URL
     */
    fun getBase(context: Context): URL? {
        val base = getStringPref(context, TreebolicIface.PREF_BASE)
        try {
            return URL(base)
        } catch (ignored: MalformedURLException) {
            //
        }
        return null
    }
}
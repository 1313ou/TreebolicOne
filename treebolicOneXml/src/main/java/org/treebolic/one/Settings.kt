/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.preference.PreferenceManager
import org.treebolic.TreebolicIface
import org.treebolic.one.xml.R
import org.treebolic.storage.Storage.getTreebolicStorage
import java.net.MalformedURLException
import java.net.URL
import androidx.core.net.toUri
import androidx.core.content.edit

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
     * Source entry preference name
     */
    const val PREF_SOURCE_ENTRY: String = "pref_source_entry"

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
     * Data
     */
    const val DATA: String = "data.zip"

    /**
     * Default CSS
     */
    const val STYLE_DEFAULT: String = ".content { }\n" +  
            ".link {color: #FFA500;font-size: small;}\n" +  
            ".linking {color: #FFA500; font-size: small; }" +  
            ".mount {color: #CD5C5C; font-size: small;}" +  
            ".mounting {color: #CD5C5C; font-size: small; }" +  
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
        val source = resources.getString(R.string.source)
        val settings = resources.getString(R.string.settings)

        // storage
        val treebolicStorage = getTreebolicStorage(context)
        val uri = Uri.fromFile(treebolicStorage)
        val treebolicBase = "$uri/"

        // preferences
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) {

            putString(PREF_PROVIDER, provider)
            putString(PREF_MIMETYPE, mime)
            putString(PREF_EXTENSIONS, extensions)

            putString(TreebolicIface.PREF_SOURCE, source)
            putString(TreebolicIface.PREF_SETTINGS, settings)

            putString(TreebolicIface.PREF_BASE, treebolicBase)
            putString(TreebolicIface.PREF_IMAGEBASE, treebolicBase)
        }
    }

    /**
     * Put string preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @JvmStatic
    fun putStringPref(context: Context, key: String?, value: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) { putString(key, value) }
    }

    /**
     * Put integer preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    fun putIntPref(context: Context, key: String?, value: Int) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) { putInt(key, value) }
    }

    /**
     * Get string preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
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
        } catch (_: MalformedURLException) {
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
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = "package:$pkgName".toUri()
        } else {
            val appPkgName = if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"

            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, pkgName)
        }

        // start activity
        context.startActivity(intent)
    }
}

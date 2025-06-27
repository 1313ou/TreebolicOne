/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.owl

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
    private const val PREF_MIMETYPE: String = "pref_mimetype"

    /**
     * File extensions preference name
     */
    private const val PREF_EXTENSIONS: String = "pref_extensions"

    /**
     * URL scheme preference name
     */
    const val PREF_URLSCHEME: String = "pref_urlscheme"

    /**
     * Provider
     */
    const val PROVIDER: String = "treebolic.provider.owl.Provider2"

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
        val source = resources.getString(R.string.source_default)
        val settings = resources.getString(R.string.settings)
        val urlScheme = resources.getString(R.string.urlScheme)

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
            putString(PREF_URLSCHEME, urlScheme)

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
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun putStringPref(context: Context, key: String?, value: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) { putString(key, value) }
    }

    /**
     * Clear preference
     *
     * @param context context
     * @param key     key
     */
    fun clearPref(context: Context, key: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit { remove(key) }
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
        sharedPref.edit(commit = true) { putInt(key, value) }
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
            } catch (_: URISyntaxException) {
                
            } catch (_: MalformedURLException) {
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
        } catch (_: MalformedURLException) {
            
        }
        return null
    }
}

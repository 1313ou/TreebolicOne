/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.preference.PreferenceManager

object Utils {

    /**
     * Get package class loader
     *
     * @param context current context
     * @param pkgName package name
     * @return package class loader
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getClassLoader(context: Context, pkgName: String?): ClassLoader {
        val providerContext = context.createPackageContext(pkgName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
        return providerContext.classLoader
    }

    /**
     * Get package resources
     *
     * @param context current context
     * @param pkgName package name
     * @return package resources
     */
    @Throws(PackageManager.NameNotFoundException::class)
    fun getResources(context: Context, pkgName: String?): Resources {
        val providerContext = context.createPackageContext(pkgName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
        return providerContext.resources
    }

    /**
     * Get plugin default shared preferences
     *
     * @param context current context
     * @param pkg     package name
     * @return default shared preferences
     */
    fun getPluginDefaultSharedPreferences(context: Context, pkg: String?): SharedPreferences? {
        try {
            val pluginContext = context.createPackageContext(pkg, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
            return PreferenceManager.getDefaultSharedPreferences(pluginContext)
        } catch (ignored: PackageManager.NameNotFoundException) {
            
        }
        return null
    }
}

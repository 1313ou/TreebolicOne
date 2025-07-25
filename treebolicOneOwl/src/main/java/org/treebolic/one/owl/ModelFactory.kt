/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.owl

import android.util.Log
import treebolic.IContext
import treebolic.model.Model
import treebolic.provider.IProvider
import treebolic.provider.IProviderContext
import java.net.MalformedURLException
import java.net.URL
import java.util.Properties

/**
 * Model factory
 *
 * @property provider        provider
 * @property providerContext provider context
 * @property context         context
 *
 * @author Bernard Bou
 */
class ModelFactory(

    private val provider: IProvider,
    private val providerContext: IProviderContext,
    private val context: IContext

) {

    /**
     * Make model
     *
     * @param source    source
     * @param base      base
     * @param imageBase image base
     * @param settings  settings
     * @return model
     */
    fun make(source: String?, base: String?, imageBase: String?, settings: String?): Model? {
        // provider
        provider.setContext(providerContext)
        provider.setLocator(context)
        provider.setHandle(null)

        // model
        val model = provider.makeModel(source, makeBaseURL(base), makeParameters(source, base, imageBase, settings))
        Log.d(TAG, "model=$model")
        return model
    }

    companion object {

        private const val TAG = "OneOwlModelFactory"

        /**
         * Make base URL
         *
         * @param base base
         * @return base URL
         */
        private fun makeBaseURL(base: String?): URL? {
            try {
                return URL(if (base != null && !base.endsWith("/")) "$base/" else base)
            } catch (ignored: MalformedURLException) {
                
            }
            return null
        }

        /**
         * Make parameters
         *
         * @param source    source
         * @param base      base
         * @param imageBase image base
         * @param settings  settings
         * @return parameters
         */
        private fun makeParameters(source: String?, base: String?, imageBase: String?, settings: String?): Properties {
            val parameters = Properties()
            if (source != null) {
                parameters.setProperty("source", source)
            }
            if (base != null) {
                parameters.setProperty("base", base)
            }
            if (imageBase != null) {
                parameters.setProperty("imagebase", imageBase)
            }
            if (settings != null) {
                parameters.setProperty("settings", settings)
            }
            return parameters
        }
    }
}

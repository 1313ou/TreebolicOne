/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one

import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.treebolic.TreebolicIface
import org.treebolic.one.xml.R

/**
 * Treebolic standard activity
 *
 * @author Bernard Bou
 */
class TreebolicActivity : TreebolicSourceActivity(R.menu.treebolic) {

    // Q U E R Y

    override fun query() {

        // sanity check
        if (providerName == null && source == null) {
            Toast.makeText(this, R.string.error_null_data, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // query
        widget!!.init(providerName, source)
    }

    override fun requery(newSource: String?) {
        if (newSource != null) {
            source = if (newSource.endsWith(".xml")) newSource else "$newSource.xml"
        }
        widget!!.reinit(newSource)
    }

    companion object {

        /**
         * Make Treebolic activity intent
         *
         * @param context      context
         * @param providerName providerName class
         * @param source       source
         * @param base         base
         * @param imageBase    image base
         * @param settings     settings
         * @param style        style
         * @return intent
         */
        fun makeTreebolicIntent(context: Context?, providerName: String?, source: String?, base: String?, imageBase: String?, settings: String?, style: String?): Intent {
            val intent = Intent(context, TreebolicActivity::class.java)
            intent.putExtra(TreebolicIface.ARG_PROVIDER, providerName)
            intent.putExtra(TreebolicIface.ARG_SOURCE, source)
            intent.putExtra(TreebolicIface.ARG_BASE, base)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase)
            intent.putExtra(TreebolicIface.ARG_SETTINGS, settings)
            intent.putExtra(TreebolicIface.ARG_STYLE, style)
            return intent
        }
    }
}

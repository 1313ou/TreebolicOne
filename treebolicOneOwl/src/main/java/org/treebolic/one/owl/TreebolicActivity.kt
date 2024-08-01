/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.owl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import org.treebolic.TreebolicIface

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

    override fun requery(source: String) {
        if (this.source != null) {
            val fields0 = source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val fields = this.source!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            this.source = fields[0] + ',' + (if (fields0.size > 1) fields0[1] else source) + ','
            Log.d(TAG, "New source: " + source + " saved:" + this.source)
        }
        restoring = true
        widget!!.reinit(source)
    }

    companion object {

        private const val TAG = "TreebolicA"

        // I N T E N T

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
         * @param urlScheme    URL scheme
         * @param more         more data in bundle
         * @return intent
         */
        fun makeTreebolicIntent(context: Context?, providerName: String?, source: String?, base: String?, imageBase: String?, settings: String?, style: String?, urlScheme: String?, more: Bundle?): Intent {
            val intent = Intent(context, TreebolicActivity::class.java)
            intent.putExtra(TreebolicIface.ARG_PROVIDER, providerName)
            intent.putExtra(TreebolicIface.ARG_SOURCE, source)
            intent.putExtra(TreebolicIface.ARG_BASE, base)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase)
            intent.putExtra(TreebolicIface.ARG_SETTINGS, settings)
            intent.putExtra(TreebolicIface.ARG_STYLE, style)
            intent.putExtra(TreebolicIface.ARG_URLSCHEME, urlScheme)
            intent.putExtra(TreebolicIface.ARG_MORE, more)
            return intent
        }
    }
}

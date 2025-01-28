/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.treebolic.TreebolicIface
import org.treebolic.one.sql.Settings.clearPref
import org.treebolic.one.sql.Settings.putStringPref
import java.util.Properties

abstract class TreebolicSourceActivity(menuId0: Int) : TreebolicBasicActivity(menuId0) {

    /**
     * Parameter : source (interpreted by provider)
     */
    @JvmField
    protected var source: String? = null

    /**
     * Parameter : data provider
     */
    @JvmField
    protected var providerName: String? = null

    /**
     * Restoring
     */
    @JvmField
    protected var restoring: Boolean = false

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restoring status
        restoring = savedInstanceState != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_save_narrowing) {
            saveWhere()
            return true
        } else if (itemId == R.id.action_clear_narrowing) {
            clearWhere()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)

        // restore
        source = savedInstanceState.getString(TreebolicIface.ARG_SOURCE)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        // save
        outState.putString(TreebolicIface.ARG_SOURCE, source)

        // always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState)
    }

    // T R E E B O L I C    C O N T E X T

    override fun makeParameters(): Properties? {
        val parameters = super.makeParameters()

        if (source != null) {
            parameters!!.setProperty("source", source)
            parameters.setProperty("doc", source)
        }
        if (providerName != null) {
            parameters!!.setProperty("provider", providerName)
        }
        return parameters
    }

    // U N M A R S H A L

    /**
     * Unmarshal parameters from intent
     *
     * @param intent intent
     */
    override fun unmarshalArgs(intent: Intent) {
        val params = intent.extras!!
        providerName = params.getString(TreebolicIface.ARG_PROVIDER)
        if (!restoring) {
            source = params.getString(TreebolicIface.ARG_SOURCE)
        }

        // super
        super.unmarshalArgs(intent)
    }

    // S A V E / C L E A R    W H E R E

    /**
     * Save truncate
     */
    private fun saveWhere() {
        if (source != null) {
            val fields = source!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (fields.size > 1) {
                var where = fields[1]
                if (where.startsWith("where:")) {
                    where = where.substring(6)
                    putStringPref(this, Settings.PREF_TRUNCATE, where)
                }
            }
        }
    }

    /**
     * Clear truncate
     */
    private fun clearWhere() {
        clearPref(this, Settings.PREF_TRUNCATE)
    }
}

/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.owl

import android.content.Intent
import android.os.Bundle
import org.treebolic.TreebolicIface
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
        this.restoring = savedInstanceState != null
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)

        // restore
        this.source = savedInstanceState.getString(TreebolicIface.ARG_SOURCE)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // save
        savedInstanceState.putString(TreebolicIface.ARG_SOURCE, this.source)

        // always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }

    // T R E E B O L I C C O N T E X T

    override fun makeParameters(): Properties? {
        val parameters = super.makeParameters()

        if (this.source != null) {
            parameters!!.setProperty("source", this.source)
            parameters.setProperty("doc", this.source)
        }
        if (this.providerName != null) {
            parameters!!.setProperty("provider", this.providerName)
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
        val params = checkNotNull(intent.extras)
        this.providerName = params.getString(TreebolicIface.ARG_PROVIDER)
        if (!this.restoring) {
            this.source = params.getString(TreebolicIface.ARG_SOURCE)
        }

        // super
        super.unmarshalArgs(intent)
    }
}

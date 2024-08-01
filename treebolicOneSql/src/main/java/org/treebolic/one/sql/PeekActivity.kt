/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.one.sql.Settings.getBase
import org.treebolic.one.sql.Settings.getStringPref
import treebolic.model.Model
import treebolic.model.ModelDump
import treebolic.provider.IProviderContext
import treebolic.provider.sqlite.Provider

class PeekActivity : AppCompatCommonActivity() {

    private var text: String? = null

    private lateinit var textView: TextView

    private var providerContext: IProviderContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_peek)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        textView = findViewById(R.id.peek)
        providerContext = object : IProviderContext {
            override fun message(text: String) {
                textView.text = text
                this@PeekActivity.text = text
            }

            override fun progress(text: String, arg1: Boolean) {
                textView.text = text
                this@PeekActivity.text = text
            }

            override fun warn(text: String) {
                textView.text = text
                this@PeekActivity.text = text
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // create provider
        val provider = Provider()
        provider.setContext(this.providerContext)
        provider.setLocator(null)
        provider.setHandle(null)

        // query provider
        val base = getBase(this)
        val source = getStringPref(this, TreebolicIface.PREF_SOURCE)
        val model = if (source == null) null else provider.makeModel(source, base, null)

        // display
        val text = "\n${text}\n${modelToString(model)}\n"
        textView.text = text
    }

    companion object {

        private fun modelToString(model: Model?): String {
            if (model == null) {
                return "<null>"
            }
            return ModelDump.toString(model)
        }
    }
}

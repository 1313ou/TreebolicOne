/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.guide.AboutActivity
import org.treebolic.guide.HelpActivity
import org.treebolic.guide.Tip
import org.treebolic.one.sql.Settings.getURLPref
import org.treebolic.search.ColorUtils.getActionBarForegroundColorFromTheme
import org.treebolic.search.ColorUtils.tint
import org.treebolic.search.SearchSettings
import treebolic.IContext
import treebolic.Widget
import treebolic.glue.component.Container
import treebolic.glue.component.Surface
import treebolic.glue.component.Utils
import java.net.MalformedURLException
import java.net.URL
import java.util.Properties

/**
 * Treebolic basic activity
 *
 * @property menuId menu id
 *
 * @author Bernard Bou
 */
abstract class TreebolicBasicActivity protected constructor(
    private val menuId: Int
) : AppCompatCommonActivity(), IContext {

    /**
     * Parameter : Document base
     */
    private var base: String? = null

    override fun getBase(): URL? {
        if (base != null) {
            try {
                return URL(base)
            } catch (_: MalformedURLException) {
                
            }
        }
        return getURLPref(this, TreebolicIface.PREF_BASE)
    }

    /**
     * Parameter : Image base
     */
    private var imagesBase: String? = null

    override fun getImagesBase(): URL? {
        if (imagesBase != null) {
            try {
                return URL(imagesBase)
            } catch (_: MalformedURLException) {
                
            }
        }
        return getURLPref(this, TreebolicIface.PREF_IMAGEBASE)
    }

    /**
     * Parameter : Settings
     */
    private var settings: String? = null

    /**
     * Parameter : CSS style for WebViews
     */
    private var cssStyle: String? = null

    override fun getStyle(): String {
        return if (cssStyle != null) cssStyle!! else Settings.STYLE_DEFAULT
    }

    /**
     * Parameter : Returned URL urlScheme that is handled
     */
    private var urlScheme: String? = null

    /**
     * Parameter : more parameters
     */
    private var more: Bundle? = null

    /**
     * Derived parameter : parameters
     */
    private var parameters: Properties? = null

    override fun getParameters(): Properties {
        return parameters!!
    }

    // components

    /**
     * Treebolic widget
     */
    @JvmField
    protected var widget: Widget? = null

    /**
     * Search view on action bar
     */
    protected var searchView: SearchView? = null

    // input

    /**
     * Input
     */
    private val input: String? = null

    override fun getInput(): String? {
        return input
    }

    // parent

    /**
     * Parent (client) activity
     */
    private var parentActivityIntentArg: Intent? = null

    override fun getParentActivityIntent(): Intent? {
        if (parentActivityIntentArg != null) {
            return parentActivityIntentArg
        }
        return super.getParentActivityIntent()
    }

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup
        val value = TypedValue()
        resources.getValue(R.dimen.splitter_position_percent, value, true)
        val splitterPositionPercent = value.float
        Container.splitterPositionPercent = splitterPositionPercent

        // widget
        widget = Widget(this, this)

        // content view
        setContentView(R.layout.activity_treebolic)
        val container = findViewById<ViewGroup>(R.id.container)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        val view: View = widget as View
        container.addView(view, params)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // action bar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.elevation = 0f
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }

    override fun onNewIntent(intent: Intent) {
        // an activity will always be paused before receiving a new intent, so you can count on onResume() being called after this method
        super.onNewIntent(intent)

        // getIntent() still returns the original Intent, use setIntent(Intent) to update it to this new Intent.
        setIntent(intent)
    }

    override fun onResume() {
        Log.d(TAG, "Activity resumed")

        // super
        super.onResume()

        // retrieve arguments
        unmarshalArgs(intent)

        // make parameters
        parameters = makeParameters()

        // query
        query()

        // first run
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val hasRun = prefs.getBoolean(Settings.PREF_FIRSTRUN, false)
        if (!hasRun) {
            prefs.edit {

                // flag as 'has run'
                putBoolean(Settings.PREF_FIRSTRUN, true)
            }

            // tips
            Tip.show(supportFragmentManager)
        }
    }

    override fun onPause() {
        Log.d(TAG, "Activity paused, terminating surface drawing thread")

        // terminate thread
        val surface: Surface? = widget?.view
        surface?.thread?.terminate()

        // super
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate
        menuInflater.inflate(menuId, menu)

        // search view
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.expandActionView()
        searchView = searchMenuItem.actionView as SearchView?

        // search view width
        val screenWidth = Utils.screenWidth(this)
        searchView!!.maxWidth = screenWidth / 2

        // search view listener
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                searchView!!.setQuery("", false)
                handleQueryChanged(query, true)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                handleQueryChanged(query, false)
                return true
            }
        })

        // icon tint
        val iconTint = getActionBarForegroundColorFromTheme(this)
        tint(iconTint, menu, R.id.action_search_run, R.id.action_search_reset, R.id.action_search_settings)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_finish -> {
                finish()
                return true
            }

            R.id.action_kill -> {
                Process.killProcess(Process.myPid())
                return true
            }

            R.id.action_tips -> {
                Tip.show(supportFragmentManager)
                return true
            }

            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                return true
            }

            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }

            R.id.action_search_run -> {
                handleSearchRun()
                return true
            }

            R.id.action_search_reset -> {
                handleSearchReset()
                return true
            }

            R.id.action_search_settings -> {
                SearchSettings.show(supportFragmentManager)
                return true
            }

            else -> return false
        }
    }

    // T R E E B O L I C   M O D E L

    /**
     * Unmarshal model and parameters from intent
     *
     * @param intent intent
     */
    protected open fun unmarshalArgs(intent: Intent) {
        // retrieve arguments
        val params = intent.extras!!
        params.classLoader = classLoader

        // retrieve arguments
        base = params.getString(TreebolicIface.ARG_BASE)
        imagesBase = params.getString(TreebolicIface.ARG_IMAGEBASE)
        settings = params.getString(TreebolicIface.ARG_SETTINGS)
        cssStyle = params.getString(TreebolicIface.ARG_STYLE)
        urlScheme = params.getString(TreebolicIface.ARG_URLSCHEME)
        @Suppress("DEPRECATION")
        parentActivityIntentArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY, Intent::class.java) else
            params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY)
        @Suppress("DEPRECATION")
        more = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            params.getParcelable(TreebolicIface.ARG_MORE, Bundle::class.java) else
            params.getParcelable(TreebolicIface.ARG_MORE)
    }

    // T R E E B O L I C   C O N T E X T

    override fun linkTo(url: String, target: String?): Boolean {
        // if url is handled by client, return query to client, which will handle it by initiating another query
        if (urlScheme != null && url.startsWith(urlScheme!!)) {
            val source2 = url.substring(urlScheme!!.length)
            requery(source2)
            return true
        }

        // standard handling
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = url.toUri()
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimetype == null) {
                intent.data = uri
            } else {
                intent.setDataAndType(uri, mimetype)
            }
            startActivity(intent)
            return true
        } catch (_: Exception) {
            Toast.makeText(this, R.string.error_link, Toast.LENGTH_LONG).show()
        }
        return false
    }

    override fun warn(message: String) {
        // toast(message, Toast.LENGTH_LONG)
        snackbar(message, Snackbar.LENGTH_LONG)
    }

    override fun status(message: String) {
        // toast(message, Toast.LENGTH_SHORT)
        snackbar(message, Snackbar.LENGTH_SHORT)
    }

    // Q U E R Y

    /**
     * Initial query
     */
    protected abstract fun query()

    /**
     * Requery (linkTo, or searchView)
     *
     * @param newSource new source
     */
    protected abstract fun requery(newSource: String?)

    /**
     * Search pending flag
     */
    private var searchPending = false

    /**
     * SearchView query change listener
     *
     * @param query  new query
     * @param submit whether submit was changed
     */
    protected fun handleQueryChanged(query: String, submit: Boolean) {
        // clear keyboard out of the way
        if (submit) {
            closeKeyboard()
        }

        // reset current search if any
        resetSearch()

        if (submit /*|| query.length() > SEARCH_TRIGGER_LEVEL*/) {
            // query applies to source: search is a requery
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL) // label, content, link, id
            if (SearchSettings.SCOPE_SOURCE == scope) {
                Log.d(TAG, "Source \"$query\"")
                requery(query)
                return
            }

            // query applies to non-source scope (label, content, ..): tree search
            val mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH) // equals, startswith, includes
            runSearch(scope, mode, query)
        }
    }

    /**
     * Tree search handler
     */
    private fun handleSearchRun() {
        // clear keyboard out of the way
        closeKeyboard()

        // new or continued search
        if (!searchPending) {
            val query = searchView!!.query.toString()
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL) // label, content, link, id
            if (SearchSettings.SCOPE_SOURCE == scope) {
                Log.d(TAG, "Source \"$query\"")
                requery(query)
                return
            }

            val mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH) // equals, startswith, includes
            runSearch(scope, mode, query)
        } else {
            continueSearch()
        }
    }

    /**
     * Tree search reset handler
     */
    private fun handleSearchReset() {
        // clear keyboard out of the way
        closeKeyboard()

        // get query
        val query = searchView!!.query.toString()

        // clear current query
        searchView!!.setQuery("", false)

        // query was already empty
        if (query.isEmpty()) {
            resetSearch()
        }

        // home
        widget!!.focus(null)
    }

    private fun closeKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // S E A R C H   I N T E R F A C E

    private fun runSearch(scope: String?, mode: String?, target: String?) {
        if (target.isNullOrEmpty()) {
            return
        }

        Log.d(TAG, "Search run$scope $mode $target")
        searchPending = true
        widget!!.search(CMD_SEARCH, scope, mode, target)
    }

    private fun continueSearch() {
        Log.d(TAG, "Search continue")
        widget!!.search(CMD_CONTINUE)
    }

    private fun resetSearch() {
        Log.d(TAG, "Search reset")
        searchPending = false
        widget!!.search(CMD_RESET)
    }

    // H E L P E R S

    /**
     * Make parameters from bundle
     *
     * @return properties
     */
    protected open fun makeParameters(): Properties? {
        val parameters = Properties()
        if (base != null) {
            parameters.setProperty("base", base)
        }
        if (imagesBase != null) {
            parameters.setProperty("imagebase", imagesBase)
        }
        if (settings != null) {
            parameters.setProperty("settings", settings)
        }

        // add properties from bundle
        if (more != null) {
            for (key in more!!.keySet()) {
                val value = more!!.getString(key)
                if (value != null) {
                    parameters.setProperty(key, value)
                }
            }
        }

        parameters.setProperty("debug", BuildConfig.DEBUG.toString())
        return parameters
    }

    /**
     * Put snackbar on UI thread
     *
     * @param message  message
     * @param duration duration
     */
    private fun snackbar(message: String, duration: Int) {
        runOnUiThread {
            val snack: Snackbar = Snackbar.make(widget as View, message, duration)
            snack.view.setBackgroundColor(ContextCompat.getColor(this@TreebolicBasicActivity, R.color.snackbar_color))
            snack.show()
        }
    }

    companion object {

        private const val TAG = "TreebolicBasicA"

        // S E A R C H

        private const val CMD_SEARCH = "SEARCH"

        private const val CMD_RESET = "RESET"

        private const val CMD_CONTINUE = "CONTINUE"
    }
}

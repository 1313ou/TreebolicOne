/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one.sql

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate.invoke
import com.bbou.rate.AppRate.rate
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.filechooser.FileChooserActivity.Companion.getFolder
import org.treebolic.filechooser.FileChooserActivity.Companion.setFolder
import org.treebolic.guide.AboutActivity
import org.treebolic.guide.HelpActivity
import org.treebolic.guide.Tip.Companion.show
import org.treebolic.storage.Deployer.cleanup
import org.treebolic.storage.Deployer.expandZipAssetFile
import org.treebolic.storage.Storage.getTreebolicStorage
import treebolic.glue.component.Dialog
import treebolic.glue.component.Statusbar
import treebolic.provider.sql.SqlProperties
import java.io.File

/**
 * Treebolic main activity (home)
 *
 * @author Bernard Bou
 */
class MainActivity : AppCompatCommonActivity(), View.OnClickListener {

    /**
     * Activity result launcher
     */
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    // L I F E C Y C L E

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // rate
        invoke(this)

        // activity result launcher
        this.activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

        // layout
        setContentView(R.layout.activity_main)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        }

        // init
        initialize()

        // fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, PlaceholderFragment()).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        updateButton()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onClick(arg0: View) {
        tryStartTreebolic(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.action_run -> {
                tryStartTreebolic(null)
                return true
            }
            R.id.action_peek -> {
                tryStartPeek()
                return true
            }
            R.id.action_reset -> {
                Log.d(TAG, "data cleanup")
                cleanup(this)
                Log.d(TAG, "settings reset")
                Settings.setDefaults(this)
                Log.d(TAG, "data reset from internal source")
                expandZipAssetFile(this, "data.zip")
            }
            R.id.action_download -> {
                val intent = Intent(this, DownloadActivity::class.java)
                intent.putExtra(org.treebolic.download.DownloadActivity.ARG_ALLOW_EXPAND_ARCHIVE, true)
                activityResultLauncher!!.launch(intent)
                return true
            }
            R.id.action_settings -> {
                tryStartTreebolicSettings()
                return true
            }
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                return true
            }
            R.id.action_tips -> {
                show(supportFragmentManager)
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
            R.id.action_others -> {
                startActivity(Intent(this, OthersActivity::class.java))
                return true
            }
            R.id.action_donate -> {
                startActivity(Intent(this, DonateActivity::class.java))
                return true
            }
            R.id.action_rate -> {
                rate(this)
                return true
            }
            R.id.action_app_settings -> {
                Settings.applicationSettings(this, applicationContext.packageName)
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
        }

        return false
    }

    /**
     * Initialize
     */
    private fun initialize() {
        // permissions
        Permissions.check(this)

        // initialize
        doOnUpgrade(Settings.PREF_INITIALIZED) {
            // default settings
            Settings.setDefaults(this)

            // deploy
            expandZipAssetFile(this, "data.zip")
        }

        // deploy
        val dir = getTreebolicStorage(this)
        if (dir.isDirectory) {
            val dirContent = dir.list()
            if (dirContent == null || dirContent.isEmpty()) {
                // deploy
                expandZipAssetFile(this, "data.zip")
            }
        }

        // base
        val base = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE)
        if (base != null) {
            Dialog.setBase(base)
            Statusbar.setBase(base)
        }
    }

    /**
     * Do on upgrade
     *
     * @param key      key holding last version
     * @param runnable what to do if upgrade
     * @return build version
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun doOnUpgrade(key: String, runnable: Runnable): Long {
        // first run of this version
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val version = try {
            prefs.getLong(key, -1)
        } catch (e: ClassCastException) {
            prefs.getInt(key, -1).toLong()
        }
        var build: Long = 0
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
                this.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)) else  //
                this.packageManager.getPackageInfo(packageName, 0)

            @Suppress("DEPRECATION")
            build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) //
                packageInfo.longVersionCode else  //
                packageInfo.versionCode.toLong()
        } catch (ignored: PackageManager.NameNotFoundException) {
            //
        }
        if (version < build) {
            val edit = prefs.edit()

            // do job
            runnable.run()

            // flag as 'has run'
            edit.putLong(key, build).apply()
        }
        return build
    }

    // S P E C I F I C R E T U R N S
    /*
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent returnIntent)
	{
		// handle selection of input by other activity which returns selected input
		if (requestCode == REQUEST_DOWNLOAD_CODE)
		{
		}
		super.onActivityResult(requestCode, resultCode, returnIntent);
	}
	*/
    // F R A G M E N T
    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_main, container, false)
        }
    }

    private val folder: String
        /**
         * Get initial folder
         *
         * @return initial folder
         */
        get() {
            val folder = getFolder(this, PREF_CURRENTFOLDER)
            if (folder != null) {
                return folder.path
            }
            return getTreebolicStorage(this).absolutePath
        }

    /**
     * Set folder to parent of given uri
     *
     * @param fileUri uri
     */
    private fun setFolder(fileUri: Uri) {
        val path = fileUri.path
        if (path != null) {
            val parentPath = File(path).parent
            setFolder(this, PREF_CURRENTFOLDER, parentPath)
        }
    }

    /**
     * Update button visibility
     */
    private fun updateButton() {
        val button = findViewById<ImageButton>(R.id.treebolicButton)
        button.visibility = if (sourceSet()) View.VISIBLE else View.INVISIBLE
        button.setOnClickListener(this)
    }

    /**
     * Whether source is set
     *
     * @return true if source is set
     */
    private fun sourceSet(): Boolean {
        val query = Settings.getQuery(this)
        if (query != null) {
            Log.d(TAG, "query=$query")
            return query.exists()
        }
        return false
    }

    // R E Q U E S T S ( S T A R T A C T I V I T Y )
    /**
     * Try to start Treebolic activity from source
     *
     * @param source0 source
     */
    private fun tryStartTreebolic(source0: String?) {
        val source = source0 ?: Settings.getStringPref(this, TreebolicIface.PREF_SOURCE)
        if (source.isNullOrEmpty()) {
            Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show()
            return
        }

        val base = Settings.getStringPref(this, TreebolicIface.PREF_BASE)
        val imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE)
        val settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS)
        var truncate = Settings.getStringPref(this, Settings.PREF_TRUNCATE)
        var prune = Settings.getStringPref(this, Settings.PREF_PRUNE)
        val urlScheme = Settings.getStringPref(this, Settings.PREF_URLSCHEME)
        val provider = Settings.PROVIDER
        val more = Bundle()
        if (!truncate.isNullOrEmpty()) {
            truncate = truncate.trim { it <= ' ' }
            more.putString(SqlProperties.TRUNCATE_NODES, truncate)
            more.putString(SqlProperties.TRUNCATE_TREEEDGES, truncate)
        }
        if (!prune.isNullOrEmpty()) {
            prune = prune.trim { it <= ' ' }
            more.putString(SqlProperties.PRUNE_NODES, prune)
            more.putString(SqlProperties.PRUNE_TREEEDGES, prune)
        }

        val intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, null, urlScheme, more)
        Log.d(TAG, "Start treebolic from provider:$provider source:$source base:$base more:$more")
        startActivity(intent)
    }

    /**
     * Try to start Treebolic peek
     */
    private fun tryStartPeek() {
        val intent = Intent(this, PeekActivity::class.java)
        Log.d(TAG, "Start peek")
        startActivity(intent)
    }

    /**
     * Try to start Treebolic settings activity
     */
    private fun tryStartTreebolicSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    companion object {

        private const val TAG = "OneSQLMainA"

        // F O L D E R P R E F E R E N C E
        private const val PREF_CURRENTFOLDER = "org.treebolic.one.sql.folder"
    }
}

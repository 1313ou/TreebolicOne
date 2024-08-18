/*
 * Copyright (c) 2023. Bernard Bou
 */
package org.treebolic.one

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
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
import org.treebolic.filechooser.EntryChooser.Companion.choose
import org.treebolic.filechooser.FileChooserActivity
import org.treebolic.filechooser.FileChooserActivity.Companion.getFolder
import org.treebolic.filechooser.FileChooserActivity.Companion.setFolder
import org.treebolic.guide.AboutActivity
import org.treebolic.guide.HelpActivity
import org.treebolic.guide.Tip.Companion.show
import org.treebolic.one.xml.R
import org.treebolic.storage.Deployer.copyAssetFile
import org.treebolic.storage.Deployer.expandZipAssetFile
import org.treebolic.storage.Storage.getTreebolicStorage
import java.io.File
import java.io.IOException

/**
 * Treebolic main activity (home)
 *
 * @author Bernard Bou
 */
open class MainActivity : AppCompatCommonActivity(), View.OnClickListener {

    /**
     * Activity file result launcher
     */
    private var activityFileResultLauncher: ActivityResultLauncher<Intent>? = null

    /**
     * Activity bundle result launcher
     */
    private var activityBundleResultLauncher: ActivityResultLauncher<Intent>? = null

    /**
     * Activity download result launcher
     */
    private var activityDownloadResultLauncher: ActivityResultLauncher<Intent>? = null

    // L I F E C Y C L E

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // rate
        invoke(this)

        // init
        initialize()

        // activity file result launcher
        this.activityFileResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                // handle selection of input by other activity which returns selected input
                val returnIntent = result.data
                if (returnIntent != null) {
                    val fileUri = returnIntent.data
                    if (fileUri != null) {
                        Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show()
                        setFolder(fileUri)
                        tryStartTreebolic(fileUri)
                    }
                }
            }
        }

        // activity bundle result launcher
        activityBundleResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            if (success) {
                // handle selection of input by other activity which returns selected input
                val returnIntent = result.data
                if (returnIntent != null) {
                    val fileUri = returnIntent.data
                    if (fileUri != null) {
                        setFolder(fileUri)
                        tryStartTreebolicBundle(fileUri)
                    }
                }
            }
        }

        // activity download result launcher
        this.activityDownloadResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

        // layout
        setContentView(R.layout.activity_main)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        }

        // fragment
        if (savedInstanceState == null) {
            supportFragmentManager //
                .beginTransaction() //
                .add(R.id.container, makeMainFragment()) //
                .commit()
        }
    }

    protected open fun makeMainFragment(): Fragment {
        return MainFragment()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.action_run -> {
                tryStartTreebolic(null as String?)
                return true
            }
            R.id.action_run_source -> {
                requestTreebolicSource()
                return true
            }
            R.id.action_run_bundle -> {
                requestTreebolicBundle()
                return true
            }
            R.id.action_builtin_data -> {
                val archiveUri = copyAssetFile(this, Settings.DATA)
                if (archiveUri != null) {
                    tryStartTreebolicBundle(archiveUri)
                }
                return true
            }
            R.id.action_reset -> {
                Settings.setDefaults(this)
                expandZipAssetFile(this, "data.zip")
            }
            R.id.action_download -> {
                val intent = Intent(this, DownloadActivity::class.java)
                intent.putExtra(org.treebolic.download.DownloadActivity.ARG_ALLOW_EXPAND_ARCHIVE, true)
                activityDownloadResultLauncher!!.launch(intent)
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
    }

    /**
     * Do on upgrade
     *
     * @param key      key holding last version
     * @param runnable what to do if upgrade
     * @return build version
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun doOnUpgrade(@Suppress("SameParameterValue") key: String, runnable: Runnable): Long {

        // first run of this version
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val version = try {
            prefs.getLong(key, -1)
        } catch (e: ClassCastException) {
            prefs.getInt(key, -1).toLong()
        }
        var build: Long = 0 //BuildConfig.VERSION_CODE;
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

    // F R A G M E N T

    open class MainFragment : PlaceholderFragment(R.layout.fragment_main)

    /**
     * A placeholder fragment containing a simple view.
     */
    abstract class PlaceholderFragment
    /**
     * Constructor
     */(@param:LayoutRes private val layoutId: Int) : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(this.layoutId, container, false)
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

    override fun onClick(arg0: View) {
        tryStartTreebolic(null as String?)
    }

    /**
     * Update button visibility
     */
    protected open fun updateButton() {
        val button = findViewById<ImageButton>(R.id.treebolicButton)
        if (button != null) {
            val sourceSet = sourceSet(this)
            Log.d(TAG, "treebolicButton $sourceSet")
            button.visibility = if (sourceSet) View.VISIBLE else View.INVISIBLE
            button.setOnClickListener(this)
        }
    }

    // R E Q U E S T S ( S T A R T A C T I V I T Y F O R R E S U L T )

    /**
     * Request Treebolic source
     */
    private fun requestTreebolicSource() {
        val base = Settings.getStringPref(this, TreebolicIface.PREF_BASE)
        val mimeType = Settings.getStringPref(this, Settings.PREF_MIMETYPE)
        val extensions = Settings.getStringPref(this, Settings.PREF_EXTENSIONS)
        val extensionsArray = extensions?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()

        val intent = Intent(this, FileChooserActivity::class.java)
        intent.setType(mimeType)
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, base)
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, extensionsArray)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activityFileResultLauncher!!.launch(intent)
    }

    /**
     * Request Treebolic bundle
     */
    private fun requestTreebolicBundle() {
        val base = Settings.getStringPref(this, TreebolicIface.PREF_BASE)
        val mimeType = "application/zip"
        val extensionsArray = arrayOf("zip", "jar")

        val intent = Intent(this, FileChooserActivity::class.java)
        intent.setType(mimeType)
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, base)
        intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, extensionsArray)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        activityBundleResultLauncher!!.launch(intent)
    }

    // R E Q U E S T S ( S T A R T A C T I V I T Y )

    /**
     * Try to start Treebolic activity from source
     *
     * @param source0 source
     */
    private fun tryStartTreebolic(source0: String?) {
        var source = source0 ?: Settings.getStringPref(this, TreebolicIface.PREF_SOURCE)
        if (source.isNullOrEmpty()) {
            Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show()
            return
        }

        var base = Settings.getStringPref(this, TreebolicIface.PREF_BASE)
        var imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE)
        val provider = Settings.getStringPref(this, Settings.PREF_PROVIDER)
        val settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS)

        // zip case
        if (source.endsWith(".zip")) {
            val entry = Settings.getStringPref(this, Settings.PREF_SOURCE_ENTRY)
            if (entry.isNullOrEmpty()) {
                Toast.makeText(this, R.string.error_null_zipentry, Toast.LENGTH_SHORT).show()
                return
            }
            base = "jar:$base/$source!/"
            imageBase = base
            source = entry
        }

        val intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, null)
        Log.d(TAG, "Start treebolic from provider:$provider source:$source")
        startActivity(intent)
    }

    /**
     * Try to start Treebolic activity from XML source file
     *
     * @param fileUri XML file uri
     */
    private fun tryStartTreebolic(fileUri: Uri) {
        val source = fileUri.toString()
        if (source.isEmpty()) {
            Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show()
            return
        }

        val provider = Settings.getStringPref(this, Settings.PREF_PROVIDER)
        val base = Settings.getStringPref(this, TreebolicIface.PREF_BASE)
        val imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE)
        val settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS)
        val style = Settings.getStringPref(this, Settings.PREF_STYLE)

        val intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, style)
        Log.d(TAG, "Start treebolic from uri $fileUri")
        startActivity(intent)
    }

    /**
     * Try to start Treebolic activity from zipped bundle file
     *
     * @param archiveUri archive uri
     */
    private fun tryStartTreebolicBundle(archiveUri: Uri) {
        try {
            val path = archiveUri.path
            if (path != null) {
                // choose bundle entry
                choose(this, File(path)) { zipEntry: String -> tryStartTreebolicBundle(archiveUri, zipEntry) }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Failed to start treebolic from bundle uri $archiveUri", e)
        }
    }

    /**
     * Try to start Treebolic activity from zip file
     *
     * @param archiveUri archive file uri
     * @param zipEntry   archive entry
     */
    private fun tryStartTreebolicBundle(archiveUri: Uri, zipEntry: String) {
        Log.d(TAG, "Start treebolic from bundle uri $archiveUri and zipentry $zipEntry")
        val source = zipEntry // alternatively: "jar:" + fileUri.toString() + "!/" + zipEntry;

        val provider = Settings.getStringPref(this, Settings.PREF_PROVIDER)
        val base = "jar:$archiveUri!/"
        val settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS)
        val style = Settings.getStringPref(this, Settings.PREF_STYLE)

        val intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, base, settings, style)
        Log.d(TAG, "Start treebolic from bundle uri $archiveUri")
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

        private const val TAG = "OneMainA"

        // F O L D E R P R E F E R E N C E

        private const val PREF_CURRENTFOLDER = "org.treebolic.one.folder"

        /**
         * Whether source is set
         *
         * @return true if source is set
         */
        private fun sourceSet(context: Context): Boolean {
            val source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE)
            val base = Settings.getStringPref(context, TreebolicIface.PREF_BASE)
            if (!source.isNullOrEmpty()) {
                var baseFile: File? = null
                if (base != null) {
                    val baseUri = Uri.parse(base)
                    val path = baseUri.path
                    if (path != null) {
                        baseFile = File(path)
                    }
                }
                val file = File(baseFile, source)
                Log.d(TAG, "file=$file")
                return file.exists()
            }
            return false
        }
    }
}

package org.treebolic.one;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bbou.donate.DonateActivity;
import com.bbou.others.OthersActivity;
import com.bbou.rate.AppRate;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.filechooser.EntryChooser;
import org.treebolic.filechooser.FileChooserActivity;
import org.treebolic.guide.AboutActivity;
import org.treebolic.guide.HelpActivity;
import org.treebolic.guide.Tip;
import org.treebolic.storage.Storage;

import java.io.File;
import java.io.IOException;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

/**
 * Treebolic main activity (home)
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatCommonActivity implements OnClickListener
{
	/**
	 * Log tag
	 */
	private static final String TAG = "OneMainA";

	/**
	 * File request code
	 */
	private static final int REQUEST_FILE_CODE = 1;

	/**
	 * Bundle request code
	 */
	private static final int REQUEST_BUNDLE_CODE = 2;

	/**
	 * Download request
	 */
	private static final int REQUEST_DOWNLOAD_CODE = 10;

	// L I F E C Y C L E O V E R R I D E S

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// rate
		AppRate.invoke(this);

		// init
		initialize();

		// layout
		setContentView(R.layout.activity_main);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}

		// fragment
		if (savedInstanceState == null)
		{
			getSupportFragmentManager()//
					.beginTransaction()//
					.add(R.id.container, makeMainFragment())//
					.commit();
		}
	}

	@NonNull
	@SuppressWarnings({"WeakerAccess"})
	protected Fragment makeMainFragment()
	{
		return new MainFragment();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		updateButton();
	}

	@SuppressWarnings("SameReturnValue")
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.action_run)
		{
			tryStartTreebolic((String) null);
			return true;
		}
		else if (itemId == R.id.action_run_source)
		{
			requestTreebolicSource();
			return true;
		}
		else if (itemId == R.id.action_run_bundle)
		{
			requestTreebolicBundle();
			return true;
		}
		else if (itemId == R.id.action_builtin_data)
		{
			final Uri archiveUri = Storage.copyAssetFile(this, Settings.DATA);
			if (archiveUri != null)
			{
				tryStartTreebolicBundle(archiveUri);
			}
			return true;
		}
		//		else if (itemId == R.id.action_reset)
		//		{
		//			Settings.setDefaults(this);
		//			Storage.expandZipAssetFile(this, "data.zip");
		//		}
		else if (itemId == R.id.action_download)
		{
			final Intent intent = new Intent(this, DownloadActivity.class);
			intent.putExtra(org.treebolic.download.DownloadActivity.ARG_ALLOW_EXPAND_ARCHIVE, true);
			startActivityForResult(intent, MainActivity.REQUEST_DOWNLOAD_CODE);
			return true;
		}
		else if (itemId == R.id.action_settings)
		{
			tryStartTreebolicSettings();
			return true;
		}
		else if (itemId == R.id.action_help)
		{
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		}
		else if (itemId == R.id.action_tips)
		{
			Tip.show(getSupportFragmentManager());
			return true;
		}
		else if (itemId == R.id.action_about)
		{
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		else if (itemId == R.id.action_others)
		{
			startActivity(new Intent(this, OthersActivity.class));
			return true;
		}
		else if (itemId == R.id.action_donate)
		{
			startActivity(new Intent(this, DonateActivity.class));
			return true;
		}
		else if (itemId == R.id.action_rate)
		{
			AppRate.rate(this);
			return true;
		}
		else if (itemId == R.id.action_app_settings)
		{
			Settings.applicationSettings(this, getApplicationContext().getPackageName());
			return true;
		}
		else if (itemId == R.id.action_finish)
		{
			finish();
			return true;
		}
		else if (itemId == R.id.action_kill)
		{
			Process.killProcess(Process.myPid());
			return true;
		}

		return false;
	}

	/**
	 * Initialize
	 */
	private void initialize()
	{
		// permissions
		Permissions.check(this);

		// initialize
		doOnUpgrade(Settings.PREF_INITIALIZED, () -> {

			// default settings
			Settings.setDefaults(this);

			// deploy
			Storage.expandZipAssetFile(this, "data.zip");
		});

		// deploy
		final File dir = Storage.getTreebolicStorage(this);
		if (dir.isDirectory())
		{
			final String[] dirContent = dir.list();
			if (dirContent == null || dirContent.length == 0)
			{
				// deploy
				Storage.expandZipAssetFile(this, "data.zip");
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
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	@SuppressWarnings({"UnusedReturnValue"})
	private long doOnUpgrade(@SuppressWarnings("SameParameterValue") @NonNull final String key, @NonNull final Runnable runnable)
	{
		// first run of this version
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long version;
		try
		{
			version = prefs.getLong(key, -1);
		}
		catch (ClassCastException e)
		{
			version = prefs.getInt(key, -1);
		}
		long build = 0; //BuildConfig.VERSION_CODE;
		try
		{
			final PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
			{
				build = packageInfo.getLongVersionCode();
			}
			else
			{
				build = packageInfo.versionCode;
			}
		}
		catch (PackageManager.NameNotFoundException ignored)
		{
			//
		}
		if (version < build)
		{
			final SharedPreferences.Editor edit = prefs.edit();

			// do job
			runnable.run();

			// flag as 'has run'
			edit.putLong(key, build).apply();
		}
		return build;
	}

	// S P E C I F I C R E T U R N S

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent returnIntent)
	{
		// handle selection of input by other activity which returns selected input
		switch (requestCode)
		{
			case REQUEST_FILE_CODE:
			case REQUEST_BUNDLE_CODE:
				if (resultCode == AppCompatActivity.RESULT_OK && returnIntent != null)
				{
					final Uri fileUri = returnIntent.getData();
					if (fileUri == null)
					{
						break;
					}

					Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
					switch (requestCode)
					{
						case REQUEST_FILE_CODE:
							setFolder(fileUri);
							tryStartTreebolic(fileUri);
							break;
						case REQUEST_BUNDLE_CODE:
							setFolder(fileUri);
							tryStartTreebolicBundle(fileUri);
							break;
						default:
							break;
					}
				}
				break;
			case REQUEST_DOWNLOAD_CODE:
			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, returnIntent);
	}

	// F R A G M E N T

	public static class MainFragment extends PlaceholderFragment
	{
		public MainFragment()
		{
			super(R.layout.fragment_main);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	@SuppressWarnings("WeakerAccess")
	public static abstract class PlaceholderFragment extends Fragment
	{
		private final int layoutId;

		/**
		 * Constructor
		 */
		@SuppressWarnings("WeakerAccess")
		public PlaceholderFragment(@LayoutRes int layoutId0)
		{
			this.layoutId = layoutId0;
		}

		@Override
		public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(this.layoutId, container, false);
		}
	}

	// T E S T

	// F O L D E R P R E F E R E N C E

	private static final String PREF_CURRENTFOLDER = "org.treebolic.one.folder";

	/**
	 * Get initial folder
	 *
	 * @return initial folder
	 */
	private String getFolder()
	{
		final File folder = FileChooserActivity.getFolder(this, MainActivity.PREF_CURRENTFOLDER);
		if (folder != null)
		{
			return folder.getPath();
		}
		return Storage.getTreebolicStorage(this).getAbsolutePath();
	}

	/**
	 * Set folder to parent of given uri
	 *
	 * @param fileUri uri
	 */
	private void setFolder(@NonNull final Uri fileUri)
	{
		String path = fileUri.getPath();
		if (path != null)
		{
			final String parentPath = new File(path).getParent();
			FileChooserActivity.setFolder(this, MainActivity.PREF_CURRENTFOLDER, parentPath);
		}
	}

	@Override
	public void onClick(final View arg0)
	{
		tryStartTreebolic((String) null);
	}

	/**
	 * Update button visibility
	 */
	@SuppressWarnings("WeakerAccess")
	protected void updateButton()
	{
		final ImageButton button = findViewById(R.id.treebolicButton);
		if (button != null)
		{
			boolean sourceSet = sourceSet(this);
			Log.d(TAG, "treebolicButton" + ' ' + sourceSet);
			button.setVisibility(sourceSet ? View.VISIBLE : View.INVISIBLE);
		}
	}

	/**
	 * Whether source is set
	 *
	 * @return true if source is set
	 */
	static private boolean sourceSet(final Context context)
	{
		final String source = Settings.getStringPref(context, TreebolicIface.PREF_SOURCE);
		final String base = Settings.getStringPref(context, TreebolicIface.PREF_BASE);
		if (source != null && !source.isEmpty())
		{
			File baseFile = null;
			if (base != null)
			{
				Uri baseUri = Uri.parse(base);
				String path = baseUri.getPath();
				if (path != null)
				{
					baseFile = new File(path);
				}
			}
			final File file = new File(baseFile, source);
			Log.d(MainActivity.TAG, "file=" + file);
			return file.exists();
		}
		return false;
	}

	// R E Q U E S T S ( S T A R T A C T I V I T Y F O R R E S U L T )

	/**
	 * Request Treebolic source
	 */
	private void requestTreebolicSource()
	{
		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		final String mimeType = Settings.getStringPref(this, Settings.PREF_MIMETYPE);
		final String extensions = Settings.getStringPref(this, Settings.PREF_EXTENSIONS);
		final String[] extensionsArray = extensions == null ? null : extensions.split(",");

		final Intent intent = new Intent(this, org.treebolic.filechooser.FileChooserActivity.class);
		intent.setType(mimeType);
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, base);
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, extensionsArray);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, MainActivity.REQUEST_FILE_CODE);
	}

	/**
	 * Request Treebolic bundle
	 */
	private void requestTreebolicBundle()
	{
		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		final String mimeType = "application/zip";
		final String[] extensionsArray = new String[]{"zip", "jar"};

		final Intent intent = new Intent(this, org.treebolic.filechooser.FileChooserActivity.class);
		intent.setType(mimeType);
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_INITIAL_DIR, base);
		intent.putExtra(FileChooserActivity.ARG_FILECHOOSER_EXTENSION_FILTER, extensionsArray);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, MainActivity.REQUEST_BUNDLE_CODE);
	}

	// R E Q U E S T S ( S T A R T A C T I V I T Y )

	/**
	 * Try to start Treebolic activity from source
	 *
	 * @param source0 source
	 */
	private void tryStartTreebolic(@Nullable final String source0)
	{
		String source = source0 != null ? source0 : Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		if (source == null || source.isEmpty())
		{
			Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show();
			return;
		}

		String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		String imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE);
		final String provider = Settings.getStringPref(this, Settings.PREF_PROVIDER);
		final String settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS);

		// zip case
		if (source.endsWith(".zip"))
		{
			String entry = Settings.getStringPref(this, Settings.PREF_SOURCE_ENTRY);
			if (entry == null || entry.isEmpty())
			{
				Toast.makeText(this, R.string.error_null_zipentry, Toast.LENGTH_SHORT).show();
				return;
			}
			base = "jar:" + base + '/' + source + "!/";
			imageBase = base;
			source = entry;
		}

		final Intent intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, null);
		Log.d(MainActivity.TAG, "Start treebolic from provider:" + provider + " source:" + source);
		startActivity(intent);
	}

	/**
	 * Try to start Treebolic activity from XML source file
	 *
	 * @param fileUri XML file uri
	 */
	private void tryStartTreebolic(@NonNull final Uri fileUri)
	{
		final String source = fileUri.toString();
		if (source.isEmpty())
		{
			Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show();
			return;
		}

		final String provider = Settings.getStringPref(this, Settings.PREF_PROVIDER);
		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		final String imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE);
		final String settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS);
		final String style = Settings.getStringPref(this, Settings.PREF_STYLE);

		final Intent intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, style);
		Log.d(MainActivity.TAG, "Start treebolic from uri " + fileUri);
		startActivity(intent);
	}

	/**
	 * Try to start Treebolic activity from zipped bundle file
	 *
	 * @param archiveUri archive uri
	 */
	private void tryStartTreebolicBundle(@NonNull final Uri archiveUri)
	{
		try
		{
			String path = archiveUri.getPath();
			if (path != null)
			{
				// choose bundle entry
				EntryChooser.choose(this, new File(path), zipEntry -> tryStartTreebolicBundle(archiveUri, zipEntry));
			}
		}
		catch (@NonNull final IOException e)
		{
			Log.d(MainActivity.TAG, "Failed to start treebolic from bundle uri " + archiveUri, e);
		}
	}

	/**
	 * Try to start Treebolic activity from zip file
	 *
	 * @param archiveUri archive file uri
	 * @param zipEntry   archive entry
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	private void tryStartTreebolicBundle(@NonNull final Uri archiveUri, final String zipEntry)
	{
		Log.d(MainActivity.TAG, "Start treebolic from bundle uri " + archiveUri + " and zipentry " + zipEntry);
		final String source = zipEntry; // alternatively: "jar:" + fileUri.toString() + "!/" + zipEntry;
		if (source == null)
		{
			Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show();
			return;
		}

		final String provider = Settings.getStringPref(this, Settings.PREF_PROVIDER);
		final String base = "jar:" + archiveUri.toString() + "!/";
		final String imageBase = base;
		final String settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS);
		final String style = Settings.getStringPref(this, Settings.PREF_STYLE);

		final Intent intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, style);
		Log.d(MainActivity.TAG, "Start treebolic from bundle uri " + archiveUri);
		startActivity(intent);
	}

	/**
	 * Try to start Treebolic settings activity
	 */
	private void tryStartTreebolicSettings()
	{
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}

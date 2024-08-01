/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.one.owl;

import android.annotation.SuppressLint;
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
import org.treebolic.filechooser.FileChooserActivity;
import org.treebolic.guide.AboutActivity;
import org.treebolic.guide.HelpActivity;
import org.treebolic.guide.Tip;
import org.treebolic.storage.Deployer;
import org.treebolic.storage.Storage;

import java.io.File;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import treebolic.glue.component.Dialog;
import treebolic.glue.component.Statusbar;

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
	private static final String TAG = "OneOwlMainA";

	/**
	 * Activity result launcher
	 */
	protected ActivityResultLauncher<Intent> activityResultLauncher;

	// L I F E C Y C L E O V E R R I D E S

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// rate
		AppRate.invoke(this);

		// activity result launcher
		this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

			//			boolean success = result.getResultCode() == Activity.RESULT_OK;
			//			if (success)
			//			{
			//			}
		});

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

		// init
		initialize();

		// fragment
		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
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
	public void onClick(final View arg0)
	{
		tryStartTreebolic(null);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.action_run)
		{
			tryStartTreebolic(null);
			return true;
		}
		else if (itemId == R.id.action_reset)
		{
			Log.d(MainActivity.TAG, "data cleanup");
			Deployer.cleanup(this);
			Log.d(MainActivity.TAG, "settings reset");
			Settings.setDefaults(this);
			Log.d(MainActivity.TAG, "data reset from internal source");
			Deployer.expandZipAssetFile(this, "data.zip");
		}
		else if (itemId == R.id.action_download)
		{
			final Intent intent = new Intent(this, DownloadActivity.class);
			intent.putExtra(org.treebolic.download.DownloadActivity.ARG_ALLOW_EXPAND_ARCHIVE, true);
			this.activityResultLauncher.launch(intent);
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
			Deployer.expandZipAssetFile(this, "data.zip");
		});

		// deploy
		final File dir = Storage.getTreebolicStorage(this);
		if (dir.isDirectory())
		{
			final String[] dirContent = dir.list();
			if (dirContent == null || dirContent.length == 0)
			{
				// deploy
				Deployer.expandZipAssetFile(this, "data.zip");
			}
		}

		// base
		String base = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE);
		if (base != null)
		{
			Dialog.setBase(base);
			Statusbar.setBase(base);
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
		long build = 0;
		try
		{
			final PackageInfo packageInfo = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU ? //
					this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0)) : //
					this.getPackageManager().getPackageInfo(getPackageName(), 0);

			build = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? //
					packageInfo.getLongVersionCode() : //
					packageInfo.versionCode;
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
	public static class PlaceholderFragment extends Fragment
	{
		/**
		 * Constructor
		 */
		public PlaceholderFragment()
		{
			//
		}

		@Override
		public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(R.layout.fragment_main, container, false);
		}
	}

	// T E S T

	// F O L D E R P R E F E R E N C E

	private static final String PREF_CURRENTFOLDER = "org.treebolic.one.owl.folder";

	/**
	 * Get initial folder
	 *
	 * @return initial folder
	 */
	@NonNull
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

	/**
	 * Update button visibility
	 */
	@SuppressWarnings("WeakerAccess")
	protected void updateButton()
	{
		final ImageButton button = findViewById(R.id.treebolicButton);
		button.setVisibility(sourceSet() ? View.VISIBLE : View.INVISIBLE);
		button.setOnClickListener(this);
	}

	/**
	 * Whether source is set
	 *
	 * @return true if source is set
	 */
	private boolean sourceSet()
	{
		final File query = Settings.getQuery(this);
		if (query != null)
		{
			Log.d(MainActivity.TAG, "query=" + query);
			return query.exists();
		}
		return false;
	}

	// R E Q U E S T S ( S T A R T A C T I V I T Y )

	/**
	 * Try to start Treebolic activity from source
	 *
	 * @param source0 source
	 */
	private void tryStartTreebolic(@Nullable @SuppressWarnings("SameParameterValue") final String source0)
	{
		String source = source0 != null ? source0 : Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		if (source == null || source.isEmpty())
		{
			Toast.makeText(this, R.string.error_null_source, Toast.LENGTH_SHORT).show();
			return;
		}

		final String base = Settings.getStringPref(this, TreebolicIface.PREF_BASE);
		final String imageBase = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE);
		final String settings = Settings.getStringPref(this, TreebolicIface.PREF_SETTINGS);
		final String urlScheme = Settings.getStringPref(this, Settings.PREF_URLSCHEME);
		final String provider = Settings.PROVIDER;
		final Bundle more = new Bundle();

		final Intent intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, null, urlScheme, more);
		Log.d(MainActivity.TAG, "Start treebolic from provider:" + provider + " source:" + source + " base:" + base + " more:" + more);
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

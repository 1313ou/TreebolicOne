package org.treebolic.one.sql;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
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
import org.treebolic.storage.Storage;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import treebolic.glue.component.Statusbar;
import treebolic.glue.component.WebDialog;

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
	private static final String TAG = "OneSQLMainA";

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
		else if (itemId == R.id.action_peek)
		{
			tryStartPeek();
			return true;
		}
		else if (itemId == R.id.action_reset)
		{
			Log.d(MainActivity.TAG, "data cleanup");
			Storage.cleanup(this);
			Log.d(MainActivity.TAG, "settings reset");
			Settings.setDefaults(this);
			Log.d(MainActivity.TAG, "data reset from internal source");
			Storage.expandZipAssetFile(this, "data.zip");
		}
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
			if (dir.list().length == 0)
			{
				// deploy
				Storage.expandZipAssetFile(this, "data.zip");
			}
		}

		// base
		String base = Settings.getStringPref(this, TreebolicIface.PREF_IMAGEBASE);
		if (base != null)
		{
			WebDialog.setBase(base);
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

	private static final String PREF_CURRENTFOLDER = "org.treebolic.one.sql.folder";

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
		final String path = new File(fileUri.getPath()).getParent();
		FileChooserActivity.setFolder(this, MainActivity.PREF_CURRENTFOLDER, path);
	}

	/**
	 * Update button visibility
	 */
	@SuppressWarnings("WeakerAccess")
	protected void updateButton()
	{
		final ImageButton button = findViewById(R.id.treebolicButton);
		button.setVisibility(sourceSet() ? View.VISIBLE : View.INVISIBLE);
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
		String truncate = Settings.getStringPref(this, Settings.PREF_TRUNCATE);
		String prune = Settings.getStringPref(this, Settings.PREF_PRUNE);
		final String urlScheme = Settings.getStringPref(this, Settings.PREF_URLSCHEME);
		final String provider = Settings.PROVIDER;
		final Bundle more = new Bundle();
		if (truncate != null && !truncate.isEmpty())
		{
			truncate = truncate.trim();
			more.putString(treebolic.provider.sqlx.SqlProperties.TRUNCATE_NODES, truncate);
			more.putString(treebolic.provider.sqlx.SqlProperties.TRUNCATE_TREEEDGES, truncate);
		}
		if (prune != null && !prune.isEmpty())
		{
			prune = prune.trim();
			more.putString(treebolic.provider.sqlx.SqlProperties.PRUNE_NODES, prune);
			more.putString(treebolic.provider.sqlx.SqlProperties.PRUNE_TREEEDGES, prune);
		}

		final Intent intent = TreebolicActivity.makeTreebolicIntent(this, provider, source, base, imageBase, settings, null, urlScheme, more);
		Log.d(MainActivity.TAG, "Start treebolic from provider:" + provider + " source:" + source + " base:" + base + " more:" + more);
		startActivity(intent);
	}

	/**
	 * Try to start Treebolic peek
	 */
	private void tryStartPeek()
	{
		final Intent intent = new Intent(this, PeekActivity.class);
		Log.d(MainActivity.TAG, "Start peek");
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

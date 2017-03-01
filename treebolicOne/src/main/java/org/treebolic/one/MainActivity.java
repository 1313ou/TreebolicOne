package org.treebolic.one;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.treebolic.TreebolicIface;
import org.treebolic.filechooser.EntryChooser;
import org.treebolic.filechooser.FileChooserActivity;
import org.treebolic.guide.AboutActivity;
import org.treebolic.guide.HelpActivity;
import org.treebolic.guide.Tip;
import org.treebolic.storage.Storage;

import java.io.File;
import java.io.IOException;

/**
 * Treebolic main activity (home)
 *
 * @author Bernard Bou
 */
public class MainActivity extends AppCompatActivity implements OnClickListener
{
	/**
	 * Log tag
	 */
	private static final String TAG = "One MainActivity";

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

	/**
	 * Provider adapter
	 */
	private SimpleAdapter adapter;

	// L I F E C Y C L E O V E R R I D E S

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// init
		initialize();

		// fragment
		if (savedInstanceState == null)
		{
			getSupportFragmentManager()//
					.beginTransaction()//
					.add(R.id.container, makeMainFragment())//
					.commit();
		}
	}

	@SuppressWarnings("static-method")
	protected Fragment makeMainFragment()
	{
		return new MainFragment();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		updateButton(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		// inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.action_treebolic)
		{
			tryStartTreebolic((String) null);
			return true;
		}
		else if (itemId == R.id.action_treebolic_source)
		{
			requestTreebolicSource();
			return true;
		}
		else if (itemId == R.id.action_treebolic_bundle)
		{
			requestTreebolicBundle();
			return true;
		}
		else if (itemId == R.id.action_builtin_data)
		{
			final Uri archiveUri = Storage.copyAssetFile(this, Settings.DATA);
			tryStartTreebolicBundle(archiveUri);
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
		else if (itemId == R.id.action_tips)
		{
			Tip.show(getSupportFragmentManager());
			return true;
		}
		else if (itemId == R.id.action_help)
		{
			HelpActivity.start(this);
			return true;
		}
		else if (itemId == R.id.action_about)
		{
			AboutActivity.start(this);
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
		else if (itemId == R.id.action_app_settings)
		{
			Settings.applicationSettings(this, getApplicationContext().getPackageName());
			return true;
		}

		return false;
	}

	/**
	 * Initialize
	 */
	@SuppressLint("CommitPrefEdits")
	private void initialize()
	{
		// version of this code
		int verCode = -1;
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			verCode = pInfo.versionCode;
		}
		catch (NameNotFoundException e)
		{
			//
		}

		// initialize
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final int savedVerCode = sharedPref.getInt(Settings.PREF_INITIALIZED, -1);
		if (savedVerCode == -1 || verCode == -1 || savedVerCode < verCode)
		{
			// default settings
			Settings.setDefaults(this);

			// deploy
			Storage.expandZipAssetFile(this, "data.zip");

			// flag as initialized
			sharedPref.edit().putInt(Settings.PREF_INITIALIZED, verCode).commit();
		}
	}

	// S P E C I F I C R E T U R N S

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent returnIntent)
	{
		// handle selection of input by other activity which returns selected input
		switch (requestCode)
		{
			case REQUEST_FILE_CODE:
			case REQUEST_BUNDLE_CODE:
				if (resultCode == AppCompatActivity.RESULT_OK)
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
				break;
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
	public static abstract class PlaceholderFragment extends Fragment
	{
		private final int layoutId;

		/**
		 * Constructor
		 */
		protected PlaceholderFragment(int layoutId0)
		{
			this.layoutId = layoutId0;
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
		{
			return inflater.inflate(this.layoutId, container, false);
		}
	}

	// T E S T

	// F O L D E R P R E F E R E N C E

	static final String PREF_CURRENTFOLDER = "org.treebolic.one.folder";

	/**
	 * Get initial folder
	 *
	 * @return initial folder
	 */
	private String getFolder()
	{
		final File thisFolder = FileChooserActivity.getFolder(this, MainActivity.PREF_CURRENTFOLDER);
		if (thisFolder != null)
		{
			return thisFolder.getPath();
		}
		return Storage.getTreebolicStorage(this).getAbsolutePath();
	}

	/**
	 * Set folder to parent of given uri
	 *
	 * @param fileUri uri
	 */
	private void setFolder(final Uri fileUri)
	{
		final String path = new File(fileUri.getPath()).getParent();
		FileChooserActivity.setFolder(this, MainActivity.PREF_CURRENTFOLDER, path);
	}

	@Override
	public void onClick(final View arg0)
	{
		tryStartTreebolic((String) null);
	}

	/**
	 * Update button visibility
	 */
	static protected void updateButton(final FragmentActivity activity)
	{
		final Button button = (Button) activity.findViewById(R.id.treebolicButton);
		if (button != null)
		{
			boolean sourceSet = sourceSet(activity);
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
			final File baseFile = base == null ? null : new File(Uri.parse(base).getPath());
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
	private void tryStartTreebolic(final String source0)
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
	private void tryStartTreebolic(final Uri fileUri)
	{
		final String source = fileUri.toString();
		if (source == null || source.isEmpty())
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
	private void tryStartTreebolicBundle(final Uri archiveUri)
	{
		try
		{
			// choose bundle entry
			EntryChooser.choose(this, new File(archiveUri.getPath()), new EntryChooser.Callback()
			{
				@SuppressWarnings("synthetic-access")
				@Override
				public void onSelect(final String zipEntry)
				{
					tryStartTreebolicBundle(archiveUri, zipEntry);
				}
			});
		}
		catch (final IOException e)
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
	private void tryStartTreebolicBundle(final Uri archiveUri, final String zipEntry)
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
	protected void tryStartTreebolicSettings()
	{
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}

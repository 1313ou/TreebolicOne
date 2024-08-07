/*
 * Copyright (c) 2023. Bernard Bou
 */

package org.treebolic.one.owl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.guide.AboutActivity;
import org.treebolic.guide.HelpActivity;
import org.treebolic.guide.Tip;
import org.treebolic.search.ColorUtils;
import org.treebolic.search.SearchSettings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import treebolic.IContext;
import treebolic.Widget;
import treebolic.glue.component.TreebolicThread;
import treebolic.view.View;

/**
 * Treebolic basic activity
 *
 * @author Bernard Bou
 */
abstract public class TreebolicBasicActivity extends AppCompatCommonActivity implements IContext
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TreebolicBasicA";

	/**
	 * Parameter : Document base
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String base;

	/**
	 * Parameter : Image base
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String imageBase;

	/**
	 * Parameter : Settings
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String settings;

	/**
	 * Parameter : CSS style for WebViews
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String style;

	/**
	 * Parameter : Returned URL urlScheme that is handled
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected String urlScheme;

	/**
	 * Parameter : more parameters
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected Bundle more;

	/**
	 * Derived parameter : parameters
	 */
	@SuppressWarnings("WeakerAccess")
	protected Properties parameters;

	// components

	/**
	 * Treebolic widget
	 */
	@SuppressWarnings("WeakerAccess")
	protected Widget widget;

	/**
	 * Search view on action bar
	 */
	@SuppressWarnings("WeakerAccess")
	protected SearchView searchView;

	/**
	 * Input
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected final String input;

	// parent

	/**
	 * Parent (client) activity
	 */
	@Nullable
	@SuppressWarnings("WeakerAccess")
	protected Intent parentActivity;

	// menu

	/**
	 * Menu id
	 */
	private final int menuId;

	// C O N S T R U C T O R

	protected TreebolicBasicActivity(final int menuId0)
	{
		this.menuId = menuId0;
		this.input = null;
	}

	// L I F E C Y C L E

	@SuppressWarnings("WeakerAccess")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// widget
		this.widget = new Widget(this, this);

		// content view
		setContentView(R.layout.activity_treebolic);
		final ViewGroup container = findViewById(R.id.container);
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.F);
		container.addView(this.widget, params);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);

		// action bar
		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setElevation(0);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}
	}

	@Override
	protected void onNewIntent(final Intent intent)
	{
		// an activity will always be paused before receiving a new intent, so you can count on onResume() being called after this method
		super.onNewIntent(intent);

		// getIntent() still returns the original Intent, use setIntent(Intent) to update it to this new Intent.
		setIntent(intent);
	}

	@Override
	protected void onResume()
	{
		Log.d(TreebolicBasicActivity.TAG, "Activity resumed");

		// super
		super.onResume();

		// retrieve arguments
		unmarshalArgs(getIntent());

		// make parameters
		this.parameters = makeParameters();

		// query
		query();

		// first run
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean hasRun = prefs.getBoolean(Settings.PREF_FIRSTRUN, false);
		if (!hasRun)
		{
			final SharedPreferences.Editor edit = prefs.edit();

			// flag as 'has run'
			edit.putBoolean(Settings.PREF_FIRSTRUN, true).apply();

			// tips
			Tip.show(getSupportFragmentManager());
		}
	}

	@Override
	protected void onPause()
	{
		Log.d(TreebolicBasicActivity.TAG, "Activity paused, terminating surface drawing thread");

		// terminate thread
		final View view = this.widget.getView();
		if (view != null)
		{
			final TreebolicThread thread = view.getThread();
			if (thread != null)
			{
				thread.terminate();
			}
		}

		// super
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull final Menu menu)
	{
		// inflate
		getMenuInflater().inflate(this.menuId, menu);

		// search view
		final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
		searchMenuItem.expandActionView();
		this.searchView = (SearchView) searchMenuItem.getActionView();

		// search view width
		int screenWidth = treebolic.glue.component.Utils.screenWidth(this);
		this.searchView.setMaxWidth(screenWidth / 2);

		// search view listener
		this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(@NonNull final String query)
			{
				TreebolicBasicActivity.this.searchView.clearFocus();
				TreebolicBasicActivity.this.searchView.setQuery("", false);
				handleQueryChanged(query, true);
				return true;
			}

			@Override
			public boolean onQueryTextChange(@NonNull final String query)
			{
				handleQueryChanged(query, false);
				return true;
			}
		});

		// icon tint
		final int iconTint = ColorUtils.getActionBarForegroundColorFromTheme(this);
		ColorUtils.tint(iconTint, menu, R.id.action_search_run, R.id.action_search_reset, R.id.action_search_settings);

		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("WeakerAccess")
	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.action_settings)
		{
			final Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
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
		else if (itemId == R.id.action_tips)
		{
			Tip.show(getSupportFragmentManager());
			return true;
		}
		else if (itemId == R.id.action_help)
		{
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		}
		else if (itemId == R.id.action_about)
		{
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		else if (itemId == R.id.action_search_run)
		{
			handleSearchRun();
			return true;
		}
		else if (itemId == R.id.action_search_reset)
		{
			handleSearchReset();
			return true;
		}
		else if (itemId == R.id.action_search_settings)
		{
			SearchSettings.show(getSupportFragmentManager());
			return true;
		}
		return false;
	}

	@Nullable
	@Override
	public Intent getParentActivityIntent()
	{
		if (this.parentActivity != null)
		{
			return this.parentActivity;
		}
		return super.getParentActivityIntent();
	}

	// T R E E B O L I C M O D E L

	/**
	 * Unmarshal model and parameters from intent
	 *
	 * @param intent intent
	 */
	@SuppressWarnings("WeakerAccess")
	protected void unmarshalArgs(@NonNull final Intent intent)
	{
		// retrieve arguments
		final Bundle params = intent.getExtras();
		assert params != null;
		params.setClassLoader(getClassLoader());

		// retrieve arguments
		this.base = params.getString(TreebolicIface.ARG_BASE);
		this.imageBase = params.getString(TreebolicIface.ARG_IMAGEBASE);
		this.settings = params.getString(TreebolicIface.ARG_SETTINGS);
		this.style = params.getString(TreebolicIface.ARG_STYLE);
		this.urlScheme = params.getString(TreebolicIface.ARG_URLSCHEME);
		this.more = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				params.getParcelable(TreebolicIface.ARG_MORE, Bundle.class) : //
				params.getParcelable(TreebolicIface.ARG_MORE);
		this.parentActivity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? //
				params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY, Intent.class) : //
				params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY);
	}

	// T R E E B O L I C C O N T E X T

	@Nullable
	@Override
	public URL getBase()
	{
		if (this.base != null)
		{
			try
			{
				return new URL(this.base);
			}
			catch (@NonNull final MalformedURLException ignored)
			{
				//
			}
		}
		return Settings.getURLPref(this, TreebolicIface.PREF_BASE);
	}

	@Nullable
	@Override
	public URL getImagesBase()
	{
		if (this.imageBase != null)
		{
			try
			{
				return new URL(this.imageBase);
			}
			catch (@NonNull final MalformedURLException ignored)
			{
				//
			}
		}
		return Settings.getURLPref(this, TreebolicIface.PREF_IMAGEBASE);
	}

	@Override
	public Properties getParameters()
	{
		return this.parameters;
	}

	@Nullable
	@Override
	public String getStyle()
	{
		return this.style != null ? this.style : //
				Settings.STYLE_DEFAULT;
	}

	@Override
	public boolean linkTo(@NonNull final String url, final String target)
	{
		// if url is handled by client, return query to client, which will handle it by initiating another query
		if (this.urlScheme != null && url.startsWith(this.urlScheme))
		{
			final String source2 = url.substring(this.urlScheme.length());
			requery(source2);
			return true;
		}

		// standard handling
		try
		{
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			final Uri uri = Uri.parse(url);
			final String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
			final String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (mimetype == null)
			{
				intent.setData(uri);
			}
			else
			{
				intent.setDataAndType(uri, mimetype);
			}
			startActivity(intent);
			return true;
		}
		catch (@NonNull final Exception ignored)
		{
			Toast.makeText(this, R.string.error_link, Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Nullable
	@Override
	public String getInput()
	{
		return this.input;
	}

	@Override
	public void warn(@NonNull final String message)
	{
		// toast(message, Toast.LENGTH_LONG);
		snackbar(message, Snackbar.LENGTH_LONG);
	}

	@Override
	public void status(@NonNull final String message)
	{
		// toast(message, Toast.LENGTH_SHORT);
		snackbar(message, Snackbar.LENGTH_SHORT);
	}

	// Q U E R Y

	/**
	 * Initial query
	 */
	abstract protected void query();

	/**
	 * Requery (linkTo, or searchView)
	 *
	 * @param source source
	 */
	abstract protected void requery(final String source);

	// S E A R C H

	static private final String CMD_SEARCH = "SEARCH";

	static private final String CMD_RESET = "RESET";

	static private final String CMD_CONTINUE = "CONTINUE";

	//static public int SEARCH_TRIGGER_LEVEL = Integer.MAX_VALUE;

	/**
	 * Search pending flag
	 */
	private boolean searchPending = false;

	/**
	 * SearchView query change listener
	 *
	 * @param query  new query
	 * @param submit whether submit was changed
	 */
	@SuppressWarnings("WeakerAccess")
	protected void handleQueryChanged(@NonNull final String query, boolean submit)
	{
		// clear keyboard out of the way
		if (submit)
		{
			closeKeyboard();
		}

		// reset current search if any
		resetSearch();

		if (submit /*|| query.length() > SEARCH_TRIGGER_LEVEL*/)
		{
			// query applies to source: search is a requery
			final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL); // label, content, link, id
			if (SearchSettings.SCOPE_SOURCE.equals(scope))
			{
				Log.d(TAG, "Source" + ' ' + '"' + query + '"');
				//if (submit)
				//{
				requery(query);
				//}
				return;
			}

			// query applies to non-source scope (label, content, ..): tree search
			final String mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH); // equals, startswith, includes
			runSearch(scope, mode, query);
		}
	}

	/**
	 * Tree search handler
	 */
	@SuppressWarnings("WeakerAccess")
	protected void handleSearchRun()
	{
		// clear keyboard out of the way
		closeKeyboard();

		// new or continued search
		if (!this.searchPending)
		{
			final String query = this.searchView.getQuery().toString();
			final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			final String scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL); // label, content, link, id
			if (SearchSettings.SCOPE_SOURCE.equals(scope))
			{
				Log.d(TAG, "Source" + ' ' + '"' + query + '"');
				requery(query);
				return;
			}

			final String mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH); // equals, startswith, includes
			runSearch(scope, mode, query);
		}
		else
		{
			continueSearch();
		}
	}

	/**
	 * Tree search reset handler
	 */
	@SuppressWarnings("WeakerAccess")
	protected void handleSearchReset()
	{
		// clear keyboard out of the way
		closeKeyboard();

		// get query
		String query = TreebolicBasicActivity.this.searchView.getQuery().toString();

		// clear current query
		TreebolicBasicActivity.this.searchView.setQuery("", false);

		// query was already empty
		if (query.isEmpty())
		{
			resetSearch();
		}

		// home
		this.widget.focus(null);
	}

	private void closeKeyboard()
	{
		final android.view.View view = this.getCurrentFocus();
		if (view != null)
		{
			final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			assert imm != null;
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	// S E A R C H   I N T E R F A C E

	@SuppressWarnings("WeakerAccess")
	protected void runSearch(String scope, String mode, @Nullable String target)
	{
		if (target == null || target.isEmpty())
		{
			return;
		}

		Log.d(TAG, "Search run" + scope + ' ' + mode + ' ' + target);
		this.searchPending = true;
		this.widget.search(CMD_SEARCH, scope, mode, target);
	}

	@SuppressWarnings("WeakerAccess")
	protected void continueSearch()
	{
		Log.d(TAG, "Search continue");
		this.widget.search(CMD_CONTINUE);
	}

	@SuppressWarnings("WeakerAccess")
	protected void resetSearch()
	{
		Log.d(TAG, "Search reset");
		this.searchPending = false;
		this.widget.search(CMD_RESET);
	}

	// H E L P E R S

	/**
	 * Make parameters from bundle
	 *
	 * @return properties
	 */
	@SuppressWarnings("WeakerAccess")
	protected Properties makeParameters()
	{
		final Properties parameters = new Properties();
		if (this.base != null)
		{
			parameters.setProperty("base", this.base);
		}
		if (this.imageBase != null)
		{
			parameters.setProperty("imagebase", this.imageBase);
		}
		if (this.settings != null)
		{
			parameters.setProperty("settings", this.settings);
		}

		// add properties from bundle
		if (this.more != null)
		{
			for (String key : this.more.keySet())
			{
				final String value = this.more.getString(key);
				if (value != null)
				{
					parameters.setProperty(key, value);
				}
			}
		}

		parameters.setProperty("debug", Boolean.toString(BuildConfig.DEBUG));
		return parameters;
	}

	// --Commented out by Inspection START (9/23/17 5:08 PM):
	//	/**
	//	 * Put toast on UI thread
	//	 *
	//	 * @param message  message
	//	 * @param duration duration
	//	 */
	//	private void toast(final String message, final int duration)
	//	{
	//		runOnUiThread(new Runnable()
	//		{
	//			@Override
	//			public void run()
	//			{
	//				Toast.makeText(TreebolicBasicActivity.this, message, duration).show();
	//			}
	//		});
	//	}
	// --Commented out by Inspection STOP (9/23/17 5:08 PM)

	/**
	 * Put snackbar on UI thread
	 *
	 * @param message  message
	 * @param duration duration
	 */
	private void snackbar(@NonNull final String message, final int duration)
	{
		runOnUiThread(() -> {
			Snackbar.make(TreebolicBasicActivity.this.widget, message, duration).show();
			final Snackbar snack = Snackbar.make(TreebolicBasicActivity.this.widget, message, duration);
			final android.view.View view = snack.getView();
			view.setBackgroundColor(ContextCompat.getColor(TreebolicBasicActivity.this, R.color.snackbar_color));
			snack.show();
		});
	}
}

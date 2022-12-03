package org.treebolic.one.owl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.treebolic.TreebolicIface;

import androidx.annotation.NonNull;

/**
 * Treebolic standard activity
 *
 * @author Bernard Bou
 */
public class TreebolicActivity extends TreebolicSourceActivity
{
	/**
	 * Log tag
	 */
	private static final String TAG = "TreebolicA";

	// C O N S T R U C T O R

	public TreebolicActivity()
	{
		super(R.menu.treebolic);
	}

	// Q U E R Y

	@Override
	protected void query()
	{
		// sanity check
		if (this.providerName == null && this.source == null)
		{
			Toast.makeText(this, R.string.error_null_data, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// query
		this.widget.init(this.providerName, this.source);
	}

	@Override
	protected void requery(@NonNull String source0)
	{
		if (this.source != null)
		{
			final String[] fields0 = source0.split(",");
			final String[] fields = this.source.split(",");
			this.source = fields[0] + ',' + (fields0.length > 1 ? fields0[1] : source0) + ',';
			Log.d(TAG, "New source: " + source0 + " saved:" + this.source);
		}

		this.restoring = true;
		this.widget.reinit(source0);
	}

	// I N T E N T

	/**
	 * Make Treebolic activity intent
	 *
	 * @param context      context
	 * @param providerName providerName class
	 * @param source       source
	 * @param base         base
	 * @param imageBase    image base
	 * @param settings     settings
	 * @param style        style
	 * @param urlScheme    URL scheme
	 * @param more         more data in bundle
	 * @return intent
	 */
	@NonNull
	static public Intent makeTreebolicIntent(final Context context, final String providerName, final String source, final String base, final String imageBase, final String settings, @SuppressWarnings("SameParameterValue") final String style, final String urlScheme, final Bundle more)
	{
		final Intent intent = new Intent(context, TreebolicActivity.class);
		intent.putExtra(TreebolicIface.ARG_PROVIDER, providerName);
		intent.putExtra(TreebolicIface.ARG_SOURCE, source);
		intent.putExtra(TreebolicIface.ARG_BASE, base);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase);
		intent.putExtra(TreebolicIface.ARG_SETTINGS, settings);
		intent.putExtra(TreebolicIface.ARG_STYLE, style);
		intent.putExtra(TreebolicIface.ARG_URLSCHEME, urlScheme);
		intent.putExtra(TreebolicIface.ARG_MORE, more);
		return intent;
	}
}

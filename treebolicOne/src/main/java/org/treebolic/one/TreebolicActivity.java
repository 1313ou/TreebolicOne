package org.treebolic.one;

import android.content.Context;
import android.content.Intent;
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
	// --Commented out by Inspection START (9/23/17 5:08 PM):
	//	/**
	//	 * Log tag
	//	 */
	//	private static final String TAG = "TreebolicA";
	// --Commented out by Inspection STOP (9/23/17 5:08 PM)

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
		this.source = source0.endsWith(".xml") ? source0 : source0 + ".xml";
		this.widget.reinit(this.source);
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
	 * @return intent
	 */
	@NonNull
	static public Intent makeTreebolicIntent(final Context context, final String providerName, final String source, final String base, final String imageBase, final String settings, final String style)
	{
		final Intent intent = new Intent(context, TreebolicActivity.class);
		intent.putExtra(TreebolicIface.ARG_PROVIDER, providerName);
		intent.putExtra(TreebolicIface.ARG_SOURCE, source);
		intent.putExtra(TreebolicIface.ARG_BASE, base);
		intent.putExtra(TreebolicIface.ARG_IMAGEBASE, imageBase);
		intent.putExtra(TreebolicIface.ARG_SETTINGS, settings);
		intent.putExtra(TreebolicIface.ARG_STYLE, style);
		return intent;
	}
}

package org.treebolic.one;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.treebolic.TreebolicIface;
import org.treebolic.one.R;

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
	private static final String TAG = "Treebolic Activity"; //$NON-NLS-1$

	// C O N S T R U C T O R

	public TreebolicActivity()
	{
		super(R.menu.treebolic);
	}

	// Q U E R Y

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.treebolic.one.TreebolicBasicActivity#query()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.treebolic.one.TreebolicBasicActivity#requery(java.lang.String)
	 */
	@Override
	protected void requery(String source0)
	{
		this.source = source0.endsWith(".xml") ? source0 : source0 + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$
		this.widget.reinit(this.source);
	}

	// I N T E N T

	/**
	 * Make Treebolic activity intent
	 *
	 * @param context
	 *            context
	 * @param providerName
	 *            providerName class
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @param style
	 *            style
	 * @return intent
	 */
	static public Intent makeTreebolicIntent(final Context context, final String providerName, final String source, final String base, final String imageBase,
			final String settings, final String style)
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

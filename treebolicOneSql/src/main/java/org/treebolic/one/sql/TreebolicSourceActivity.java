package org.treebolic.one.sql;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.treebolic.TreebolicIface;

import java.util.Properties;

public abstract class TreebolicSourceActivity extends TreebolicBasicActivity
{
	/**
	 * Parameter : source (interpreted by provider)
	 */
	@SuppressWarnings("WeakerAccess")
	protected String source;

	/**
	 * Parameter : data provider
	 */
	@SuppressWarnings("WeakerAccess")
	protected String providerName;

	/**
	 * Restoring
	 */
	@SuppressWarnings("WeakerAccess")
	protected boolean restoring;

	// C O N S T R U C T O R

	public TreebolicSourceActivity(int menuId0)
	{
		super(menuId0);
	}

	// L I F E C Y C L E

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// restoring status
		this.restoring = savedInstanceState != null;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		int itemId = item.getItemId();
		if (itemId == R.id.action_save_narrowing)
		{
			saveWhere();
			return true;
		}
		else if (itemId == R.id.action_clear_narrowing)
		{
			clearWhere();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRestoreInstanceState(final Bundle savedInstanceState)
	{
		// always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// restore
		this.source = savedInstanceState.getString(TreebolicIface.ARG_SOURCE);
	}

	@Override
	public void onSaveInstanceState(final Bundle savedInstanceState)
	{
		// save
		savedInstanceState.putString(TreebolicIface.ARG_SOURCE, this.source);

		// always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	// T R E E B O L I C C O N T E X T

	@Override
	protected Properties makeParameters()
	{
		final Properties theseParameters = super.makeParameters();

		if (this.source != null)
		{
			theseParameters.setProperty("source", this.source);
			theseParameters.setProperty("doc", this.source);
		}
		if (this.providerName != null)
		{
			theseParameters.setProperty("provider", this.providerName);
		}
		return theseParameters;
	}

	// U N M A R S H A L

	/**
	 * Unmarshal parameters from intent
	 *
	 * @param intent intent
	 */
	@Override
	protected void unmarshalArgs(final Intent intent)
	{
		final Bundle params = intent.getExtras();
		assert params != null;
		this.providerName = params.getString(TreebolicIface.ARG_PROVIDER);
		if (!this.restoring)
		{
			this.source = params.getString(TreebolicIface.ARG_SOURCE);
		}

		// super
		super.unmarshalArgs(intent);
	}

	// S A V E / C L E A R W H E R E

	/**
	 * Save truncate
	 */
	private void saveWhere()
	{
		// System.out.println("source " + this.source);
		String[] fields = this.source.split(",");
		if (fields.length > 1)
		{
			String where = fields[1];
			if (where.startsWith("where:"))
			{
				where = where.substring(6);
				// System.out.println("narrowing:" + restrict);
				Settings.putStringPref(this, Settings.PREF_TRUNCATE, where);
			}
		}
	}

	/**
	 * Clear truncate
	 */
	private void clearWhere()
	{
		// System.out.println("clearing narrowing");
		Settings.clearPref(this, Settings.PREF_TRUNCATE);
	}
}

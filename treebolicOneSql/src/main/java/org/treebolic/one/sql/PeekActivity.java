package org.treebolic.one.sql;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.treebolic.TreebolicIface;

import java.net.URL;
import java.util.Properties;

import treebolic.IContext;
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.provider.IProviderContext;
import treebolic.provider.sqlite.Provider;

public class PeekActivity extends AppCompatActivity
{
	private TextView textView;

	private IProviderContext providerContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_peek);

		// toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		this.textView = (TextView) findViewById(R.id.peek);
		this.providerContext = new IProviderContext()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void message(String text)
			{
				PeekActivity.this.textView.setText(text);
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void progress(String text, boolean arg1)
			{
				PeekActivity.this.textView.setText(text);
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void warn(String text)
			{
				PeekActivity.this.textView.setText(text);
			}
		};
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// create provider
		final Provider provider = new Provider();
		provider.setup(this.providerContext);
		provider.setup((IContext) null);

		// query provider
		final URL base = Settings.getBase(this);
		final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		final Properties properties = null;
		final Model model = provider.makeModel(source, base, properties);

		// display
		final String text = modelToString(model);
		this.textView.setText(this.textView.getText().toString() + '\n' + text);
	}

	private static String modelToString(final Model model)
	{
		if (model == null)
		{
			return "<null>";
		}
		return ModelDump.toString(model);
	}
}

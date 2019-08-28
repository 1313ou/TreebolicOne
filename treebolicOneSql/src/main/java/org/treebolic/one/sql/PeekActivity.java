package org.treebolic.one.sql;

import android.os.Bundle;
import android.widget.TextView;

import org.treebolic.AppCompatCommonActivity;
import org.treebolic.TreebolicIface;

import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import treebolic.model.Model;
import treebolic.model.ModelDump;
import treebolic.provider.IProviderContext;
import treebolic.provider.sqlite.Provider;

public class PeekActivity extends AppCompatCommonActivity
{
	private String text;

	private TextView textView;

	private IProviderContext providerContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.activity_peek);

		// toolbar
		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// set up the action bar
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null)
		{
			actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
		}

		this.textView = findViewById(R.id.peek);
		this.providerContext = new IProviderContext()
		{
			@Override
			public void message(String text)
			{
				PeekActivity.this.textView.setText(text);
				PeekActivity.this.text = text;
			}

			@Override
			public void progress(String text, boolean arg1)
			{
				PeekActivity.this.textView.setText(text);
				PeekActivity.this.text = text;
			}

			@Override
			public void warn(String text)
			{
				PeekActivity.this.textView.setText(text);
				PeekActivity.this.text = text;
			}
		};
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// create provider
		final Provider provider = new Provider();
		provider.setContext(this.providerContext);
		provider.setLocator(null);
		provider.setHandle(null);

		// query provider
		final URL base = Settings.getBase(this);
		final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
		final Model model = source == null ? null : provider.makeModel(source, base, null);

		// display
		final String text = this.text + '\n' + modelToString(model);
		this.textView.setText(text);
	}

	@NonNull
	private static String modelToString(@Nullable final Model model)
	{
		if (model == null)
		{
			return "<null>";
		}
		return ModelDump.toString(model);
	}
}

package org.treebolic.one;

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.IContext;
import treebolic.model.Model;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;

/**
 * Model factory
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class ModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "OneModelFactory";

	/**
	 * Provider
	 */
	private final IProvider provider;

	/**
	 * Provider context
	 */
	private final IProviderContext providerContext;

	/**
	 * Locator Context
	 */
	private final IContext locatorContext;

	/**
	 * Application context
	 */
	private final Context applicationContext;

	/**
	 * Constructor
	 *
	 * @param provider0           provider
	 * @param providerContext0    provider context
	 * @param locatorContext0     locator context0
	 * @param applicationContext0 application context0
	 */
	public ModelFactory(final IProvider provider0, final IProviderContext providerContext0, final IContext locatorContext0, final Context applicationContext0)
	{
		this.provider = provider0;
		this.providerContext = providerContext0;
		this.locatorContext = locatorContext0;
		this.applicationContext = applicationContext0;
	}

	/**
	 * Make model
	 *
	 * @param source    source
	 * @param base      base
	 * @param imageBase image base
	 * @param settings  settings
	 * @return model
	 */
	@Nullable
	public Model make(final String source, final String base, final String imageBase, final String settings)
	{
		// provider
		this.provider.setContext(this.providerContext);
		this.provider.setLocator(this.locatorContext);
		this.provider.setHandle(this.applicationContext);

		// model
		final Model model = this.provider.makeModel(source, ModelFactory.makeBaseURL(base), ModelFactory.makeParameters(source, base, imageBase, settings));
		Log.d(ModelFactory.TAG, "model=" + model);
		return model;
	}

	/**
	 * Make base URL
	 *
	 * @param base base
	 * @return base URL
	 */
	@Nullable
	private static URL makeBaseURL(@Nullable final String base)
	{
		try
		{
			return new URL(base != null && !base.endsWith("/") ? base + "/" : base);
		}
		catch (@NonNull final MalformedURLException ignored)
		{
			//
		}
		return null;
	}

	/**
	 * Make parameters
	 *
	 * @param source    source
	 * @param base      base
	 * @param imageBase image base
	 * @param settings  settings
	 * @return parameters
	 */
	@NonNull
	private static Properties makeParameters(@Nullable final String source, @Nullable final String base, @Nullable final String imageBase, @Nullable final String settings)
	{
		final Properties parameters = new Properties();
		if (source != null)
		{
			parameters.setProperty("source", source);
		}
		if (base != null)
		{
			parameters.setProperty("base", base);
		}
		if (imageBase != null)
		{
			parameters.setProperty("imagebase", imageBase);
		}
		if (settings != null)
		{
			parameters.setProperty("settings", settings);
		}

		return parameters;
	}
}

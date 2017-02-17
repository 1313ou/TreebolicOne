package org.treebolic.one;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import android.util.Log;
import treebolic.IContext;
import treebolic.model.Model;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;

/**
 * Model factory
 *
 * @author Bernard Bou
 */
public class ModelFactory
{
	/**
	 * Log tag
	 */
	static private final String TAG = "Model factory"; //$NON-NLS-1$

	/**
	 * Provider
	 */
	final IProvider provider;

	/**
	 * Provider context
	 */
	final IProviderContext providerContext;

	/**
	 * Context
	 */
	final IContext context;

	/**
	 * Constructor
	 *
	 * @param provider0
	 *            provider
	 * @param providerContext0
	 *            provider context
	 * @param context0
	 *            context0
	 */
	public ModelFactory(final IProvider provider0, final IProviderContext providerContext0, final IContext context0)
	{
		this.provider = provider0;
		this.providerContext = providerContext0;
		this.context = context0;
	}

	/**
	 * Make model
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return model
	 * @throws IOException
	 */
	public Model make(final String source, final String base, final String imageBase, final String settings) throws IOException
	{
		// provider
		this.provider.setup(this.providerContext);
		this.provider.setup(this.context);

		// model
		final Model model = this.provider.makeModel(source, ModelFactory.makeBaseURL(base), ModelFactory.makeParameters(source, base, imageBase, settings));
		Log.d(ModelFactory.TAG, "model=" + model); //$NON-NLS-1$
		return model;
	}

	/**
	 * Make base URL
	 *
	 * @param base
	 *            base
	 * @return base URL
	 */
	private static URL makeBaseURL(final String base)
	{
		try
		{
			return new URL(base != null && !base.endsWith("/") ? base + "/" : base); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (final MalformedURLException e)
		{
			//
		}
		return null;
	}

	/**
	 * Make parameters
	 *
	 * @param source
	 *            source
	 * @param base
	 *            base
	 * @param imageBase
	 *            image base
	 * @param settings
	 *            settings
	 * @return parameters
	 */
	private static Properties makeParameters(final String source, final String base, final String imageBase, final String settings)
	{
		final Properties theseParameters = new Properties();
		if (source != null)
		{
			theseParameters.setProperty("source", source); //$NON-NLS-1$
		}
		if (base != null)
		{
			theseParameters.setProperty("base", base); //$NON-NLS-1$
		}
		if (imageBase != null)
		{
			theseParameters.setProperty("imagebase", imageBase); //$NON-NLS-1$
		}
		if (settings != null)
		{
			theseParameters.setProperty("settings", settings); //$NON-NLS-1$
		}

		return theseParameters;
	}
}

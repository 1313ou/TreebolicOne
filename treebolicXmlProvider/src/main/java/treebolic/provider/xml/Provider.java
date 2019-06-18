package treebolic.provider.xml;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import treebolic.ILocator;
import treebolic.model.Model;
import treebolic.model.Tree;
import treebolic.provider.IProvider;
import treebolic.provider.IProviderContext;
import treebolic.provider.ProviderUtils;

public class Provider implements IProvider
{
	// D A T A

	/**
	 * Context used to query for Url
	 */
	@SuppressWarnings("WeakerAccess")
	protected IProviderContext context;

	/**
	 * Url
	 */
	@SuppressWarnings("WeakerAccess")
	protected URL url;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 */
	public Provider()
	{
		this.url = null;
	}

	// M A K E

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setContext(treebolic.provider.IProviderContext)
	 */
	@Override
	public void setContext(final IProviderContext context)
	{
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setLocator(treebolic.ILocator)
	 */
	@SuppressWarnings("EmptyMethod")
	@Override
	public void setLocator(final ILocator locator)
	{
		// do not need
	}

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setHandle(java.lang.Object)
	 */
	@SuppressWarnings("EmptyMethod")
	@Override
	public void setHandle(final Object handle)
	{
		// do not need
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.IProvider#makeTree(java.lang.String, java.net.URL, java.util.Properties, boolean)
	 */
	@Override
	public Tree makeTree(final String source, final URL base, final Properties parameters, final boolean checkRecursion)
	{
		final URL url = ProviderUtils.makeURL(source, base, parameters, this.context);
		if (url == null)
		{
			return null;
		}

		// direct recursion prevention
		if (checkRecursion && url.equals(this.url))
		{
			this.context.message("Recursion: " + url.toString()); //$NON-NLS-1$
			return null;
		}

		this.url = url;
		this.context.progress("Loading ..." + url.toString(), false); //$NON-NLS-1$
		final Tree tree = makeTree(url, base, parameters);
		if (tree != null)
		{
			this.context.progress("Loaded ..." + url.toString(), false); //$NON-NLS-1$
		}
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.IProvider#makeModel(java.lang.String, java.net.URL, java.util.Properties)
	 */
	@Override
	public Model makeModel(final String source, final URL base, final Properties parameters)
	{
		final URL url = ProviderUtils.makeURL(source, base, parameters, this.context);
		if (url == null)
		{
			return null;
		}

		this.url = url;
		this.context.progress("Loading ..." + url.toString(), false); //$NON-NLS-1$
		final Model model = makeModel(url, base, parameters);
		if (model != null)
		{
			this.context.progress("Loaded ..." + url.toString(), false); //$NON-NLS-1$
		}
		return model;
	}

	// P A R S E

	/**
	 * Make model from url
	 *
	 * @param url url
	 * @return model
	 */
	@SuppressWarnings("WeakerAccess")
	protected Model makeModel(final URL url, final URL base, final Properties parameters)
	{
		final Document document = makeDocument(url);
		if (document == null)
		{
			return null;
		}
		return new DocumentAdapter(this, base, parameters).makeModel(document);
	}

	/**
	 * Make tree from url
	 *
	 * @param url url
	 * @return tree
	 */
	@SuppressWarnings("WeakerAccess")
	protected Tree makeTree(final URL url, final URL base, final Properties parameters)
	{
		final Document document = makeDocument(url);
		if (document == null)
		{
			return null;
		}
		return new DocumentAdapter(this, base, parameters).makeTree(document);
	}

	/**
	 * Make DOM document from its Url
	 *
	 * @param url document url
	 * @return DOM document
	 */
	@SuppressWarnings("WeakerAccess")
	protected Document makeDocument(final URL url)
	{
		try
		{
			return new Parser().makeDocument(url, (publicId, systemId) -> {
				if (systemId.contains("Treebolic.dtd")) //$NON-NLS-1$
				{
					return new InputSource(new StringReader("")); //$NON-NLS-1$
				}
				else
				{
					return null;
				}
			});
		}
		catch (final IOException e)
		{
			this.context.warn("DOM parser IO: " + e.toString()); //$NON-NLS-1$
		}
		catch (final SAXException e)
		{
			this.context.warn("DOM parser SAX: " + e.toString()); //$NON-NLS-1$
		}
		catch (final ParserConfigurationException e)
		{
			this.context.warn("DOM parser CONFIG: " + e.toString()); //$NON-NLS-1$
		}
		return null;
	}
}

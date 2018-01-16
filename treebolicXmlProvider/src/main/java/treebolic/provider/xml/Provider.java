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
	protected IProviderContext theContext;

	/**
	 * Url
	 */
	@SuppressWarnings("WeakerAccess")
	protected URL theUrl;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 */
	public Provider()
	{
		this.theUrl = null;
	}

	// M A K E

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setContext(treebolic.provider.IProviderContext)
	 */
	@Override
	public void setContext(final IProviderContext thisContext)
	{
		this.theContext = thisContext;
	}

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setLocator(treebolic.ILocator)
	 */
	@Override
	public void setLocator(final ILocator thisLocator)
	{
		// do not need
	}

	/* (non-Javadoc)
	 * @see treebolic.provider.IProvider#setHandle(java.lang.Object)
	 */
	@Override
	public void setHandle(final Object thisHandle)
	{
		// do not need
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.IProvider#makeTree(java.lang.String, java.net.URL, java.util.Properties, boolean)
	 */
	@Override
	public Tree makeTree(final String thisSource, final URL thisBase, final Properties theseParameters, final boolean checkRecursion)
	{
		final URL thisUrl = ProviderUtils.makeURL(thisSource, thisBase, theseParameters, this.theContext);
		if (thisUrl == null)
			return null;

		// direct recursion prevention
		if (checkRecursion && thisUrl.equals(this.theUrl))
		{
			this.theContext.message("Recursion: " + thisUrl.toString()); //$NON-NLS-1$
			return null;
		}

		this.theUrl = thisUrl;
		this.theContext.progress("Loading ..." + thisUrl.toString(), false); //$NON-NLS-1$
		final Tree thisTree = makeTree(thisUrl, thisBase, theseParameters);
		if (thisTree != null)
		{
			this.theContext.progress("Loaded ..." + thisUrl.toString(), false); //$NON-NLS-1$
		}
		return thisTree;
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.IProvider#makeModel(java.lang.String, java.net.URL, java.util.Properties)
	 */
	@Override
	public Model makeModel(final String thisSource, final URL thisBase, final Properties theseParameters)
	{
		final URL thisUrl = ProviderUtils.makeURL(thisSource, thisBase, theseParameters, this.theContext);
		if (thisUrl == null)
			return null;

		this.theUrl = thisUrl;
		this.theContext.progress("Loading ..." + thisUrl.toString(), false); //$NON-NLS-1$
		final Model thisModel = makeModel(thisUrl, thisBase, theseParameters);
		if (thisModel != null)
		{
			this.theContext.progress("Loaded ..." + thisUrl.toString(), false); //$NON-NLS-1$
		}
		return thisModel;
	}

	// P A R S E

	/**
	 * Make model from url
	 *
	 * @param thisUrl
	 *        url
	 * @return model
	 */
	@SuppressWarnings("WeakerAccess")
	protected Model makeModel(final URL thisUrl, final URL thisBase, final Properties theseParameters)
	{
		final Document thisDocument = makeDocument(thisUrl);
		if (thisDocument == null)
			return null;
		return new DocumentAdapter(this, thisBase, theseParameters).makeModel(thisDocument);
	}

	/**
	 * Make tree from url
	 *
	 * @param thisUrl
	 *        url
	 * @return tree
	 */
	@SuppressWarnings("WeakerAccess")
	protected Tree makeTree(final URL thisUrl, final URL thisBase, final Properties theseParameters)
	{
		final Document thisDocument = makeDocument(thisUrl);
		if (thisDocument == null)
			return null;
		return new DocumentAdapter(this, thisBase, theseParameters).makeTree(thisDocument);
	}

	/**
	 * Make DOM document from its Url
	 *
	 * @param thisUrl
	 *        document url
	 * @return DOM document
	 */
	@SuppressWarnings("WeakerAccess")
	protected Document makeDocument(final URL thisUrl)
	{
		try
		{
			return new Parser().makeDocument(thisUrl, (publicId, systemId) ->
			{
				if (systemId.contains("Treebolic.dtd")) //$NON-NLS-1$
					return new InputSource(new StringReader("")); //$NON-NLS-1$
				else
					return null;
			});
		}
		catch (final IOException e)
		{
			this.theContext.warn("DOM parser IO: " + e.toString()); //$NON-NLS-1$
		}
		catch (final SAXException e)
		{
			this.theContext.warn("DOM parser SAX: " + e.toString()); //$NON-NLS-1$
		}
		catch (final ParserConfigurationException e)
		{
			this.theContext.warn("DOM parser CONFIG: " + e.toString()); //$NON-NLS-1$
		}
		return null;
	}
}

package treebolic.provider.xml;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

/**
 * DOM Parser
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Parser
{
	/**
	 * Validate XML
	 */
	@SuppressWarnings("WeakerAccess")
	protected final boolean validate;

	/**
	 * Constructor
	 */
	public Parser()
	{
		this(false);
	}

	/**
	 * Constructor
	 */
	@SuppressWarnings("WeakerAccess")
	public Parser(@SuppressWarnings("SameParameterValue") final boolean thisValidateFlag)
	{
		this.validate = thisValidateFlag;
	}

	/**
	 * Make document
	 *
	 * @param thisUrl      in data url
	 * @param thisResolver entity resolver
	 * @return DOM document
	 * @throws ParserConfigurationException parser configuration exception
	 * @throws IOException                  io exception
	 * @throws SAXException                 sax parser exception
	 */
	public Document makeDocument(final URL thisUrl, final EntityResolver thisResolver) throws ParserConfigurationException, SAXException, IOException
	{
		final ParseErrorLogger thisHandler = new ParseErrorLogger();
		try
		{
			final DocumentBuilder thisBuilder = makeDocumentBuilder();
			thisBuilder.setErrorHandler(thisHandler);
			if (thisResolver != null)
			{
				thisBuilder.setEntityResolver(thisResolver);
			}
			return thisBuilder.parse(thisUrl.openStream());
		}
		finally
		{
			thisHandler.terminate();
		}
	}

	/**
	 * Make Document builder
	 *
	 * @return document builder
	 * @throws ParserConfigurationException parser configuration exception
	 */
	private DocumentBuilder makeDocumentBuilder() throws ParserConfigurationException
	{
		final DocumentBuilderFactory thisFactory = DocumentBuilderFactory.newInstance();
		thisFactory.setCoalescing(true);
		thisFactory.setIgnoringComments(true);
		thisFactory.setNamespaceAware(false);
		thisFactory.setIgnoringElementContentWhitespace(true);
		try
		{
			thisFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", this.validate); //$NON-NLS-1$
		}
		catch (final Exception e)
		{
			//
		}
		thisFactory.setValidating(this.validate);
		return thisFactory.newDocumentBuilder();
	}

	/**
	 * Document
	 *
	 * @param thisIn       in data url
	 * @param thisXslt     xslt url
	 * @param thisResolver entity resolver, null if none
	 * @return DOM document
	 */
	public Document makeDocument(final URL thisIn, final URL thisXslt, final EntityResolver thisResolver)
	{
		try
		{
			// xsl
			final Source thisXslSource = new StreamSource(thisXslt.openStream());

			// in
			Source thisSource;
			if (thisResolver == null)
			{
				thisSource = new StreamSource(thisIn.openStream());
			}
			else
			{
				final XMLReader thisReader = XMLReaderFactory.createXMLReader();
				thisReader.setEntityResolver(thisResolver);
				thisSource = new SAXSource(thisReader, new InputSource(thisIn.openStream()));
			}

			// out
			final DOMResult thisResult = new DOMResult();

			// transform
			final TransformerFactory thisFactory = TransformerFactory.newInstance();
			final Transformer thisTransformer = thisFactory.newTransformer(thisXslSource);
			thisTransformer.setParameter("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
			thisTransformer.transform(thisSource, thisResult);

			return (Document) thisResult.getNode();
		}
		catch (final Exception e)
		{
			System.err.println("Dom parser: " + e.getMessage()); //$NON-NLS-1$
		}
		return null;
	}
}

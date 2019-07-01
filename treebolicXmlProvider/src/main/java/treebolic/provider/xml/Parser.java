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
	public Parser(@SuppressWarnings("SameParameterValue") final boolean validate)
	{
		this.validate = validate;
	}

	/**
	 * Make document
	 *
	 * @param url      in data url
	 * @param resolver entity resolver
	 * @return DOM document
	 * @throws ParserConfigurationException parser configuration exception
	 * @throws IOException                  io exception
	 * @throws SAXException                 sax parser exception
	 */
	public Document makeDocument(final URL url, final EntityResolver resolver) throws ParserConfigurationException, SAXException, IOException
	{
		final ParseErrorLogger handler = new ParseErrorLogger();
		try
		{
			final DocumentBuilder builder = makeDocumentBuilder();
			builder.setErrorHandler(handler);
			if (resolver != null)
			{
				builder.setEntityResolver(resolver);
			}
			return builder.parse(url.openStream());
		}
		finally
		{
			handler.terminate();
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
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(false);
		factory.setIgnoringElementContentWhitespace(true);
		try
		{
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", this.validate);
		}
		catch (final Exception ignored)
		{
			//
		}
		factory.setValidating(this.validate);
		return factory.newDocumentBuilder();
	}

	/**
	 * Document
	 *
	 * @param url      in data url
	 * @param xslt     xslt url
	 * @param resolver entity resolver, null if none
	 * @return DOM document
	 */
	public Document makeDocument(final URL url, final URL xslt, final EntityResolver resolver)
	{
		try
		{
			// xsl
			final Source xslSource = new StreamSource(xslt.openStream());

			// in
			Source source;
			if (resolver == null)
			{
				source = new StreamSource(url.openStream());
			}
			else
			{
				final XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setEntityResolver(resolver);
				source = new SAXSource(reader, new InputSource(url.openStream()));
			}

			// out
			final DOMResult result = new DOMResult();

			// transform
			final TransformerFactory factory = TransformerFactory.newInstance();
			final Transformer transformer = factory.newTransformer(xslSource);
			transformer.setParameter("http://xml.org/sax/features/validation", false);
			transformer.transform(source, result);

			return (Document) result.getNode();
		}
		catch (final Exception e)
		{
			System.err.println("Dom parser: " + e.getMessage());
		}
		return null;
	}
}

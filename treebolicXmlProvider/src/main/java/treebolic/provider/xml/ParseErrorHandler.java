package treebolic.provider.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Parse error handler
 *
 * @author Bernard Bou
 */
class ParseErrorHandler implements ErrorHandler
{
	/**
	 * Warning count
	 */
	protected int theWarnings;

	/**
	 * Error count
	 */
	protected int theErrors;

	/**
	 * Fatal error count
	 */
	protected int theFatalErrors;

	/**
	 * Constructor
	 */
	protected ParseErrorHandler()
	{
		this.theWarnings = 0;
		this.theErrors = 0;
		this.theFatalErrors = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(final SAXParseException e) throws SAXParseException
	{
		this.theFatalErrors++;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(final SAXParseException e) throws SAXParseException
	{
		this.theWarnings++;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(final SAXParseException e) throws SAXParseException
	{
		this.theFatalErrors++;
	}
}

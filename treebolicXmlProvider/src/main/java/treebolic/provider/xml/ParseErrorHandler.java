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
	@SuppressWarnings("WeakerAccess")
	protected int theWarnings;

	/**
	 * Error count
	 */
	@SuppressWarnings("WeakerAccess")
	protected final int theErrors;

	/**
	 * Fatal error count
	 */
	@SuppressWarnings("WeakerAccess")
	protected int theFatalErrors;

	/**
	 * Constructor
	 */
	@SuppressWarnings("WeakerAccess")
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

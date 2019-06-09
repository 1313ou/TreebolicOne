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
	protected int warnings;

	/**
	 * Error count
	 */
	@SuppressWarnings("WeakerAccess")
	protected final int errors;

	/**
	 * Fatal error count
	 */
	@SuppressWarnings("WeakerAccess")
	protected int fatalErrors;

	/**
	 * Constructor
	 */
	@SuppressWarnings("WeakerAccess")
	protected ParseErrorHandler()
	{
		this.warnings = 0;
		this.errors = 0;
		this.fatalErrors = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(final SAXParseException e) throws SAXParseException
	{
		this.fatalErrors++;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(final SAXParseException e) throws SAXParseException
	{
		this.warnings++;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(final SAXParseException e) throws SAXParseException
	{
		this.fatalErrors++;
	}
}

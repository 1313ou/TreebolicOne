package treebolic.provider.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.xml.sax.SAXParseException;

/**
 * Parse error logger
 *
 * @author Bernard Bou
 */
public class ParseErrorLogger extends ParseErrorHandler
{
	/**
	 * Printer
	 */
	private PrintWriter theWriter;

	/**
	 * Output stream
	 */
	private OutputStream theOutputStream;

	/**
	 * Constructor
	 */
	public ParseErrorLogger()
	{
		// do nothing
	}

	/**
	 * Log
	 *
	 * @param thisLevel
	 *        level
	 * @param e
	 *        exception
	 */
	private void log(final String thisLevel, final SAXParseException e)
	{
		if (this.theOutputStream == null)
		{
			this.theOutputStream = System.err;
		}
		if (this.theWriter == null)
		{
			this.theWriter = new PrintWriter(this.theOutputStream, true);
		}

		this.theWriter.println(thisLevel);
		this.theWriter.println(" uri: " + e.getSystemId() + "(" + e.getLineNumber() + "," + e.getColumnNumber() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.theWriter.println(" message: " + e.getMessage()); //$NON-NLS-1$
	}

	/**
	 * Terminate
	 */
	public void terminate()
	{
		try
		{
			if (this.theWriter != null && (this.theFatalErrors != 0 || this.theErrors != 0 || this.theWarnings != 0))
			{
				this.theWriter.println("Fatal Errors:" + this.theFatalErrors); //$NON-NLS-1$
				this.theWriter.println("Errors:" + this.theErrors); //$NON-NLS-1$
				this.theWriter.println("Warnings:" + this.theWarnings); //$NON-NLS-1$
			}
			if (this.theOutputStream != null)
			{
				this.theOutputStream.close();
			}
		}
		catch (final IOException ole)
		{
			// do nothing
		}
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.xml.dom.ParseErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(final SAXParseException e) throws SAXParseException
	{
		super.error(e);
		log("Recoverable Error", e); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.xml.dom.ParseErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(final SAXParseException e) throws SAXParseException
	{
		super.warning(e);
		log("Warning", e); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.xml.dom.ParseErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(final SAXParseException e) throws SAXParseException
	{
		super.fatalError(e);
		log("FATAL ERROR", e); //$NON-NLS-1$
	}
}

package treebolic.provider.xml;

import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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
	private PrintWriter writer;

	/**
	 * Output stream
	 */
	private OutputStream outputStream;

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
	 * @param level level
	 * @param e     exception
	 */
	private void log(final String level, final SAXParseException e)
	{
		if (this.outputStream == null)
		{
			this.outputStream = System.err;
		}
		if (this.writer == null)
		{
			this.writer = new PrintWriter(this.outputStream, true);
		}

		this.writer.println(level);
		this.writer.println(" uri: " + e.getSystemId() + "(" + e.getLineNumber() + "," + e.getColumnNumber() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.writer.println(" message: " + e.getMessage()); //$NON-NLS-1$
	}

	/**
	 * Terminate
	 */
	public void terminate()
	{
		try
		{
			if (this.writer != null && (this.fatalErrors != 0 || this.errors != 0 || this.warnings != 0))
			{
				this.writer.println("Fatal Errors:" + this.fatalErrors); //$NON-NLS-1$
				this.writer.println("Errors:" + this.errors); //$NON-NLS-1$
				this.writer.println("Warnings:" + this.warnings); //$NON-NLS-1$
			}
			if (this.outputStream != null)
			{
				this.outputStream.close();
			}
		}
		catch (final IOException ignored)
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

/*
  Title : Treebolic SQL provider
  Description : Treebolic SQL provider
  Version : 3.x
  Copyright : (c) 2001-2014
  Terms of use : see license agreement at http://treebolic.sourceforge.net/en/license.htm
  Author : Bernard Bou
 */
package treebolic.provider.sqlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * SQL properties
 *
 * @author Bernard Bou
 */
public class SqlProperties extends treebolic.provider.sqlx.SqlProperties
{
	/**
	 * Load properties
	 */
	static public Properties load(final File thisPath)
	{
		try
		{
			return load(thisPath.toURI().toURL());
		}
		catch (MalformedURLException e)
		{
			//
		}
		return null;
	}

	/**
	 * Load properties
	 */
	static public Properties load(final URL thisUrl)
	{
		InputStream thisInputStream = null;
		try
		{
			final Properties theseProperties = new Properties();
			thisInputStream = thisUrl.openStream();
			theseProperties.load(thisInputStream);
			return theseProperties;
		}
		catch (final IOException e)
		{
			System.err.println("Sqlite load: Cannot load <" + thisUrl.toString() + ">");
			return null;
		}
		finally
		{
			if (thisInputStream != null)
				try
				{
					thisInputStream.close();
				}
				catch (IOException e)
				{
					//
				}
		}
	}

	/**
	 * Save properties
	 */
	static void save(final Properties theseProperties, final String thisPropertyFile)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(thisPropertyFile);
			theseProperties.store(fos, "TREEBOLIC-SQLITE");
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
					//
				}
		}
	}

	/**
	 * Make default property
	 */
	static public String toString(final Properties theseProperties)
	{
		final StringBuilder thisBuilder = new StringBuilder();
		for (final Enumeration<?> thisEnum = theseProperties.propertyNames(); thisEnum.hasMoreElements();)
		{
			final String thisName = (String) thisEnum.nextElement();
			final String thisValue = theseProperties.getProperty(thisName);
			thisBuilder.append(thisName);
			thisBuilder.append("=");
			thisBuilder.append(thisValue);
			thisBuilder.append("\n");
		}
		return thisBuilder.toString();
	}
}

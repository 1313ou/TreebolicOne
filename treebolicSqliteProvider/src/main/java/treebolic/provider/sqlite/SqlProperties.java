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
@SuppressWarnings("WeakerAccess")
public class SqlProperties extends treebolic.provider.sqlx.SqlProperties
{
	/**
	 * Load properties
	 */
	static public Properties load(final File path)
	{
		try
		{
			return load(path.toURI().toURL());
		}
		catch (MalformedURLException ignored)
		{
			//
		}
		return null;
	}

	/**
	 * Load properties
	 */
	@SuppressWarnings("WeakerAccess")
	static public Properties load(final URL url)
	{
		InputStream inputStream = null;
		try
		{
			final Properties properties = new Properties();
			inputStream = url.openStream();
			properties.load(inputStream);
			return properties;
		}
		catch (final IOException ignored)
		{
			System.err.println("Sqlite load: Cannot load <" + url.toString() + ">");
			return null;
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException ignored)
				{
					//
				}
			}
		}
	}

	/**
	 * Save properties
	 */
	static void save(final Properties properties, final String propertyFile)
	{
		try (FileOutputStream fos = new FileOutputStream(propertyFile))
		{
			properties.store(fos, "TREEBOLIC-SQLITE");
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Make default property
	 */
	static public String toString(final Properties properties)
	{
		final StringBuilder sb = new StringBuilder();
		for (final Enumeration<?> names = properties.propertyNames(); names.hasMoreElements(); )
		{
			final String name = (String) names.nextElement();
			final String value = properties.getProperty(name);
			sb.append(name);
			sb.append("=");
			sb.append(value);
			sb.append("\n");
		}
		return sb.toString();
	}
}

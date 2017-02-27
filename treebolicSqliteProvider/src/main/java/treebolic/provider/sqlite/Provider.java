/**
 * Title : Treebolic SQL provider
 * Description : Treebolic SQL provider
 * Version : 3.x
 * Copyright : (c) 2001-2014
 * Terms of use : see license agreement at http://treebolic.sourceforge.net/en/license.htm
 * Author : Bernard Bou
 *
 */
package treebolic.provider.sqlite;

import android.annotation.SuppressLint;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Properties;

import treebolic.provider.sqlx.AbstractProvider;

/**
 * Provider for SQL
 *
 * @author Bernard Bou
 */
public class Provider extends AbstractProvider<Provider.AndroidDatabase, Provider.AndroidCursor>
{
	static class AndroidCursor implements AbstractProvider.Cursor
	{
		private final android.database.Cursor theCursor;

		/**
		 * Constructor
		 * 
		 * @param thisCursor
		 *            android cursor
		 */
		public AndroidCursor(android.database.Cursor thisCursor)
		{
			this.theCursor = thisCursor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#close()
		 */
		@Override
		public void close()
		{
			if (this.theCursor != null)
				try
				{
					this.theCursor.close();
				}
				catch (SQLException thisException)
				{
					//
					thisException.printStackTrace();
				}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#moveToNext()
		 */
		@Override
		public boolean moveToNext() throws Exception
		{
			return this.theCursor.moveToNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getPosition()
		 */
		@Override
		public int getPosition() throws SQLException
		{
			return this.theCursor.getPosition();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getColumnIndex(java.lang.String)
		 */
		@Override
		public int getColumnIndex(String thisColumnName) throws Exception
		{
			return this.theCursor.getColumnIndex(thisColumnName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#isNull(int)
		 */
		@Override
		public boolean isNull(int thisColumnIndex) throws Exception
		{
			return this.theCursor.isNull(thisColumnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getString(int)
		 */
		@Override
		public String getString(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getString(thisColumnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getInt(int)
		 */
		@SuppressWarnings("boxing")
		@Override
		public Integer getInt(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getInt(thisColumnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getFloat(int)
		 */
		@SuppressWarnings("boxing")
		@Override
		public Float getFloat(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getFloat(thisColumnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Cursor#getDouble(int)
		 */
		@SuppressWarnings("boxing")
		@Override
		public Double getDouble(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getDouble(thisColumnIndex);
		}
	}

	static class AndroidDatabase implements AbstractProvider.Database<AndroidCursor>
	{
		private SQLiteDatabase theDB;

		public AndroidDatabase(final String thisDatabasePath)
		{
			// path
			System.out.println("Sqlite path: " + thisDatabasePath);

			try
			{
				// connect
				this.theDB = SQLiteDatabase.openDatabase(thisDatabasePath, null, SQLiteDatabase.OPEN_READONLY);

			}
			catch (final SQLException thisException)
			{
				this.theDB = null;
				System.err.println("Sqlite exception : " + thisException.getMessage());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Database#close()
		 */
		@Override
		public void close()
		{
			if (this.theDB != null)
				try
				{
					this.theDB.close();
				}
				catch (SQLException thisException)
				{
					thisException.printStackTrace();
				}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see treebolic.provider.sqlx.AbstractProvider.Database#query(java.lang.String)
		 */
		@SuppressWarnings("resource")
		@Override
		public AndroidCursor query(String thisSql) throws SQLException
		{
			@SuppressLint("Recycle")
			final android.database.Cursor thisCursor = this.theDB.rawQuery(thisSql, null);
			return new AndroidCursor(thisCursor);
		}
	}

	@Override
	protected AndroidDatabase openDatabase(Properties theseProperties)
	{
		final String thisDatabasePath = makeDatabasePath(theseProperties);
		return new AndroidDatabase(thisDatabasePath);
	}
}

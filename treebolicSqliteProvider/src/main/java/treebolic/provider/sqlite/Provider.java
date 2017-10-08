/*
  Title : Treebolic SQL provider
  Description : Treebolic SQL provider
  Version : 3.x
  Copyright : (c) 2001-2014
  Terms of use : see license agreement at http://treebolic.sourceforge.net/en/license.htm
  Author : Bernard Bou
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
		 * @param thisCursor android cursor
		 */
		public AndroidCursor(android.database.Cursor thisCursor)
		{
			this.theCursor = thisCursor;
		}

		@Override
		public void close()
		{
			if (this.theCursor != null)
			{
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
		}

		@Override
		public boolean moveToNext() throws Exception
		{
			return this.theCursor.moveToNext();
		}

		@Override
		public int getPosition() throws SQLException
		{
			return this.theCursor.getPosition();
		}

		@Override
		public int getColumnIndex(String thisColumnName) throws Exception
		{
			return this.theCursor.getColumnIndex(thisColumnName);
		}

		@Override
		public boolean isNull(int thisColumnIndex) throws Exception
		{
			return this.theCursor.isNull(thisColumnIndex);
		}

		@Override
		public String getString(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getString(thisColumnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Integer getInt(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getInt(thisColumnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Float getFloat(int thisColumnIndex) throws Exception
		{
			return this.theCursor.getFloat(thisColumnIndex);
		}

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

		@Override
		public void close()
		{
			if (this.theDB != null)
			{
				try
				{
					this.theDB.close();
				}
				catch (SQLException thisException)
				{
					thisException.printStackTrace();
				}
			}
		}

		@SuppressWarnings("resource")
		@Override
		public AndroidCursor query(String thisSql) throws SQLException
		{
			@SuppressLint("Recycle") final android.database.Cursor thisCursor = this.theDB.rawQuery(thisSql, null);
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

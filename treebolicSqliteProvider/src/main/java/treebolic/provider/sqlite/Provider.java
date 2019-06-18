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
		private final android.database.Cursor cursor;

		/**
		 * Constructor
		 *
		 * @param cursor android cursor
		 */
		@SuppressWarnings("WeakerAccess")
		public AndroidCursor(android.database.Cursor cursor)
		{
			this.cursor = cursor;
		}

		@Override
		public void close()
		{
			if (this.cursor != null)
			{
				try
				{
					this.cursor.close();
				}
				catch (SQLException e)
				{
					//
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("RedundantThrows")
		@Override
		public boolean moveToNext() throws Exception
		{
			return this.cursor.moveToNext();
		}

		@Override
		public int getPosition() throws SQLException
		{
			return this.cursor.getPosition();
		}

		@Override
		public int getColumnIndex(String columnName)
		{
			return this.cursor.getColumnIndex(columnName);
		}

		@Override
		public boolean isNull(int columnIndex)
		{
			return this.cursor.isNull(columnIndex);
		}

		@Override
		public String getString(int columnIndex)
		{
			return this.cursor.getString(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Integer getInt(int columnIndex)
		{
			return this.cursor.getInt(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Float getFloat(int columnIndex)
		{
			return this.cursor.getFloat(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Double getDouble(int columnIndex)
		{
			return this.cursor.getDouble(columnIndex);
		}
	}

	static class AndroidDatabase implements AbstractProvider.Database<AndroidCursor>
	{
		private SQLiteDatabase db;

		@SuppressWarnings("WeakerAccess")
		public AndroidDatabase(final String databasePath)
		{
			// path
			System.out.println("Sqlite path: " + databasePath);

			try
			{
				// connect
				this.db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);

			}
			catch (final SQLException e)
			{
				this.db = null;
				System.err.println("Sqlite exception : " + e.getMessage());
			}
		}

		@Override
		public void close()
		{
			if (this.db != null)
			{
				try
				{
					this.db.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public AndroidCursor query(String sql) throws SQLException
		{
			@SuppressLint("Recycle") final android.database.Cursor cursor = this.db.rawQuery(sql, null);
			return new AndroidCursor(cursor);
		}
	}

	@Override
	protected AndroidDatabase openDatabase(Properties properties)
	{
		final String database = makeDatabasePath(properties);
		return new AndroidDatabase(database);
	}
}

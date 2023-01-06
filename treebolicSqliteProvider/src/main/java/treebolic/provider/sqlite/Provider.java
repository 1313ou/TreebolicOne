/*
 * Copyright (c) 2023. Bernard Bou
 */

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import treebolic.provider.sql.AbstractProvider;

/**
 * Provider for SQL
 *
 * @author Bernard Bou
 */
public class Provider extends AbstractProvider<Provider.AndroidDatabase, Provider.AndroidCursor, SQLException>
{
	static class AndroidCursor implements AbstractProvider.Cursor<SQLException>
	{
		@Nullable
		private final android.database.Cursor cursor;

		/**
		 * Constructor
		 *
		 * @param cursor android cursor
		 */
		@SuppressWarnings("WeakerAccess")
		public AndroidCursor(@Nullable final android.database.Cursor cursor)
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
		public boolean moveToNext() throws SQLException
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.moveToNext();
		}

		@Override
		public int getPosition() throws SQLException
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getPosition();
		}

		@Override
		public int getColumnIndex(@NonNull final String columnName)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getColumnIndex(columnName);
		}

		@Override
		public boolean isNull(final int columnIndex)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.isNull(columnIndex);
		}

		@Override
		public String getString(final int columnIndex)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getString(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Integer getInt(final int columnIndex)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getInt(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Float getFloat(final int columnIndex)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getFloat(columnIndex);
		}

		@SuppressWarnings("boxing")
		@Override
		public Double getDouble(final int columnIndex)
		{
			if (this.cursor == null)
			{
				throw new SQLException("Null database");
			}
			return this.cursor.getDouble(columnIndex);
		}
	}

	static class AndroidDatabase implements AbstractProvider.Database<AndroidCursor, SQLException>
	{
		@Nullable
		private SQLiteDatabase db;

		@SuppressWarnings("WeakerAccess")
		public AndroidDatabase(@NonNull final String databasePath)
		{
			// path
			System.out.println("Sqlite path: " + databasePath);

			try
			{
				// connect
				this.db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);

			}
			catch (@NonNull final SQLException e)
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

		@Nullable
		@Override
		public AndroidCursor query(@NonNull final String sql) throws SQLException
		{
			if (this.db == null)
			{
				return null;
			}

			@SuppressLint("Recycle") final android.database.Cursor cursor = this.db.rawQuery(sql, null);
			return new AndroidCursor(cursor);
		}
	}

	@NonNull
	@Override
	protected AndroidDatabase openDatabase(@NonNull final Properties properties)
	{
		final String database = makeDatabasePath(properties);
		return new AndroidDatabase(database);
	}
}

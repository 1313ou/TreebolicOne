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
package treebolic.provider.sqlite

import android.annotation.SuppressLint
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import treebolic.provider.sql.AbstractProvider
import treebolic.provider.sqlite.Provider.AndroidCursor
import treebolic.provider.sqlite.Provider.AndroidDatabase
import java.util.Properties

/**
 * Provider for SQL
 *
 * @author Bernard Bou
 */
class Provider : AbstractProvider<AndroidDatabase, AndroidCursor?, SQLException?>() {

    class AndroidCursor
    /**
     * Constructor
     *
     * @param cursor android cursor
     */(private val cursor: android.database.Cursor?) : Cursor<SQLException?> {

        override fun close() {
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }

        @Throws(SQLException::class)
        override fun moveToNext(): Boolean {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.moveToNext()
        }

        @Throws(SQLException::class)
        override fun getPosition(): Int {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.position
        }

        override fun getColumnIndex(columnName: String): Int {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.getColumnIndex(columnName)
        }

        override fun isNull(columnIndex: Int): Boolean {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.isNull(columnIndex)
        }

        override fun getString(columnIndex: Int): String? {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.getString(columnIndex)
        }

        override fun getInt(columnIndex: Int): Int {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.getInt(columnIndex)
        }

        override fun getFloat(columnIndex: Int): Float {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.getFloat(columnIndex)
        }

        override fun getDouble(columnIndex: Int): Double {
            if (cursor == null) {
                throw SQLException("Null database")
            }
            return cursor.getDouble(columnIndex)
        }
    }

    class AndroidDatabase(databasePath: String) : Database<AndroidCursor?, SQLException?> {

        private var db: SQLiteDatabase? = null

        init {
            // path
            println("Sqlite path: $databasePath")

            try {
                // connect
                db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS or SQLiteDatabase.OPEN_READONLY)
            } catch (e: SQLException) {
                db = null
                System.err.println("Sqlite exception : " + e.message)
            }
        }

        override fun close() {
            if (db != null) {
                try {
                    db!!.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }

        @Throws(SQLException::class)
        override fun query(sql: String): AndroidCursor? {
            if (db == null) {
                return null
            }
            val cursor = db!!.rawQuery(sql, null)
            return AndroidCursor(cursor)
        }
    }

    override fun openDatabase(properties: Properties): AndroidDatabase {
        val database = makeDatabasePath(properties)
        return AndroidDatabase(database)
    }
}

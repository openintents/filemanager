package org.openintents.filemanager.search;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Used by the standard search Service, in order to provide results asynchronously to the SearchableActivity.
 * @author George Venios
 *
 */
public class SearchResultsProvider extends ContentProvider {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "filemanager_search.db";

	private static final String TABLE_NAME = "search_results";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "NAME";
	public static final String COLUMN_PATH = "PATH";

	private static final String DATABASE_CREATE = String.format(
			"CREATE TABLE %s (%s integer primary key autoincrement, "
					+ "%s text not null, %s text not null);", TABLE_NAME,
			COLUMN_ID, COLUMN_NAME, COLUMN_PATH);

	public static final String SEARCH_MIMETYPE = "vnd.android.cursor.item/vnd.openintents.search_result";
	public static final String PROVIDER_NAME = "org.openintents.filemanager.search";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME);

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		db = dbHelper.getWritableDatabase();
		
		return (dbHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(TABLE_NAME);

		if (sortOrder == null || sortOrder == "")
			sortOrder = COLUMN_ID;

		Cursor c = sqlBuilder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = db.insert(TABLE_NAME, "", values);
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// We only write, read and delete the db. Not implemented for now.
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = db.delete(TABLE_NAME, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return SEARCH_MIMETYPE;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

}
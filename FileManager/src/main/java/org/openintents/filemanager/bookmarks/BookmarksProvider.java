package org.openintents.filemanager.bookmarks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class BookmarksProvider extends ContentProvider implements BaseColumns {
    public static final String TB_NAME = "bookmarks";
    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String CHECKED = "checked"; //Only because of multiple choice delete dialog
    public static final String PROVIDER_NAME = "org.openintents.filemanager.bookmarks";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + PROVIDER_NAME);
    public static final String BOOKMARK_MIMETYPE =
            "vnd.android.cursor.item/vnd.openintents.bookmark";
    public static final String BOOKMARKS_MIMETYPE =
            "vnd.android.cursor.dir/vnd.openintents.bookmark";

    private static final int BOOKMARKS = 1;
    private static final int BOOKMARK_ID = 2;
    private static final UriMatcher uriMatcher;
    private static final String DATABASE_CREATE =
            String.format("CREATE TABLE %s (%s integer primary key autoincrement, "
                            + "%s text not null, %s text not null, %s integer default 0);",
                    TB_NAME, _ID, NAME, PATH, CHECKED);
    private static final String DATABASE_NAME = "org.openintents.filemanager";
    private static final int DATABASE_VERSION = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, null, BOOKMARKS);
        uriMatcher.addURI(PROVIDER_NAME, "#", BOOKMARK_ID);
    }

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        int count;
        switch (uriMatcher.match(arg0)) {
            case BOOKMARKS:
                count = db.delete(TB_NAME, arg1, arg2);
                break;
            case BOOKMARK_ID:
                String id = arg0.getPathSegments().get(0);
                count = db.delete(TB_NAME, _ID + " = " + id
                                + (!TextUtils.isEmpty(arg1) ? " AND (" + arg1 + ')' : ""),
                        arg2);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKMARKS:
                return BOOKMARKS_MIMETYPE;
            case BOOKMARK_ID:
                return BOOKMARK_MIMETYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(TB_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        db = dbHelper.getWritableDatabase();
        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(TB_NAME);
        if (uriMatcher.match(uri) == BOOKMARK_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(0));
        }

        if (sortOrder == null || sortOrder == "")
            sortOrder = _ID;

        Cursor c = sqlBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case BOOKMARKS:
                count = db.update(
                        TB_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case BOOKMARK_ID:
                count = db.update(
                        TB_NAME,
                        values,
                        _ID + " = " + uri.getPathSegments().get(0)
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        /*
         * !!!
         * When changing database version, you MUST change this method.
         * Currently, it would delete all users' bookmarks
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
            onCreate(db);
        }
    }

}

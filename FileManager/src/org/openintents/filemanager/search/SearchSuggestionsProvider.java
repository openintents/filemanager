package org.openintents.filemanager.search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Not that good, but it's a working implementation at least. We REALLY need asynchronous suggestion refreshing.
 *
 * @author George Venios
 */
public class SearchSuggestionsProvider extends ContentProvider {
    public static final String SEARCH_SUGGEST_MIMETYPE = "vnd.android.cursor.item/vnd.openintents.search_suggestion";
    public static final String PROVIDER_NAME = "org.openintents.filemanager.search.suggest";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);

    private static final long MAX_NANOS = 2000000;
    private static final int MAX_SUGGESTIONS = 7;

    private ArrayList<ContentValues> mSuggestions = new ArrayList<>();
    private SearchCore searcher;

    @Override
    /**
     * Always clears all suggestions. Parameters other than uri are ignored.
     */
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = mSuggestions.size();
        mSuggestions.clear();
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return SEARCH_SUGGEST_MIMETYPE;
    }

    @Override
    @NonNull
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id = mSuggestions.size() + 1;
        values.put(BaseColumns._ID, id);
        mSuggestions.add(values);

        Uri _uri = ContentUris.withAppendedId(CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(_uri, null);

        return _uri;
    }

    @Override
    public boolean onCreate() {
        searcher = new SearchCore(getContext());
        searcher.setMaxResults(MAX_SUGGESTIONS);
        searcher.setURI(CONTENT_URI);
        return true;
    }

    @Override
    /**
     * NOT a cheap call. Actual search happens here.
     */
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        searcher.setQuery(uri.getLastPathSegment().toLowerCase());

        searcher.dropPreviousResults();

        searcher.startClock(MAX_NANOS);
        searcher.search(Environment.getExternalStorageDirectory());

        MatrixCursor cursor = new MatrixCursor(new String[]{
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                BaseColumns._ID});
        for (ContentValues val : mSuggestions)
            cursor.newRow().add(val.get(SearchManager.SUGGEST_COLUMN_ICON_1))
                    .add(val.get(SearchManager.SUGGEST_COLUMN_TEXT_1))
                    .add(val.get(SearchManager.SUGGEST_COLUMN_TEXT_2))
                    .add(val.get(SearchManager.SUGGEST_COLUMN_INTENT_DATA))
                    .add(val.get(BaseColumns._ID));
        return cursor;
    }

    @Override
    /**
     * We don't care about updating. Unimplemented.
     */
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

}
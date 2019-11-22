package org.openintents.filemanager.search;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.HomeIconHelper;
import org.openintents.filemanager.util.UIUtils;
import org.openintents.intents.FileManagerIntents;

/**
 * The activity that handles queries and shows search results.
 * Also handles search-suggestion triggered intents.
 *
 * @author George Venios
 */
public class SearchableActivity extends ListActivity {
    private LocalBroadcastManager lbm;
    private Cursor searchResults;

    /**
     * Clear the recents' history.
     */
    public static void clearSearchRecents(Context c) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(c,
                RecentsSuggestionsProvider.AUTHORITY,
                RecentsSuggestionsProvider.MODE);
        suggestions.clearHistory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.setThemeFor(this);

        // Presentation settings
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        HomeIconHelper.activity_actionbar_setDisplayHomeAsUpEnabled(this);

        lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        // Handle the search request.
        handleIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                HomeIconHelper.showHome(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Get the query.
            String query = intent.getStringExtra(SearchManager.QUERY);
            setTitle(query);

            // Get the current path, which allows us to refine the search.
            String path = null;
            if (intent.getBundleExtra(SearchManager.APP_DATA) != null)
                path = intent.getBundleExtra(SearchManager.APP_DATA).getString(
                        FileManagerIntents.EXTRA_SEARCH_INIT_PATH);

            // Add query to recents.
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                    this, RecentsSuggestionsProvider.AUTHORITY,
                    RecentsSuggestionsProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            // Register broadcast receivers
            lbm.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setProgressBarIndeterminateVisibility(false);
                }
            }, new IntentFilter(FileManagerIntents.ACTION_SEARCH_FINISHED));

            lbm.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setProgressBarIndeterminateVisibility(true);
                }
            }, new IntentFilter(FileManagerIntents.ACTION_SEARCH_STARTED));

            // Set the list adapter.
            searchResults = getSearchResults();
            setListAdapter(new SearchListAdapter(this, searchResults));

            // Start the search service.
            Intent in = new Intent(this, SearchService.class);
            in.putExtra(FileManagerIntents.EXTRA_SEARCH_INIT_PATH, path);
            in.putExtra(FileManagerIntents.EXTRA_SEARCH_QUERY, query);
            startService(in);
        } // We're here because of a clicked suggestion
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            browse(intent.getData());
        } else
            // Intent contents error.
            setTitle(R.string.query_error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, SearchService.class));
    }

    private Cursor getSearchResults() {
        return getContentResolver().query(SearchResultsProvider.CONTENT_URI, null, null, null, SearchResultsProvider.COLUMN_ID + " ASC");
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = new CursorWrapper(searchResults);
        c.moveToPosition(position);
        String path = c.getString(c.getColumnIndex(SearchResultsProvider.COLUMN_PATH));

        browse(Uri.parse(path));
    }

    private void browse(Uri path) {
        Intent intent = new Intent(this, FileManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(path);

        startActivity(intent);
        finish();
    }
}
package org.openintents.filemanager.search;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.openintents.intents.FileManagerIntents;

import java.io.File;

/**
 * Service that asynchronously executes file searches.
 *
 * @author George Venios.
 */
public class SearchService extends IntentService {
    /**
     * Used to inform the SearchableActivity of search start and end.
     */
    private LocalBroadcastManager lbm;
    private SearchCore searcher;

    public SearchService() {
        super("SearchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lbm = LocalBroadcastManager.getInstance(getApplicationContext());

        searcher = new SearchCore(this);
        searcher.setURI(SearchResultsProvider.CONTENT_URI);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // The search query
        searcher.setQuery(intent.getStringExtra(FileManagerIntents.EXTRA_SEARCH_QUERY));

        // Set initial path. To be searched first!
        String path = intent
                .getStringExtra(FileManagerIntents.EXTRA_SEARCH_INIT_PATH);
        File root;
        if (path != null)
            root = new File(path);
        else
            root = new File("/");

        // Search started, let Receivers know.
        lbm.sendBroadcast(new Intent(FileManagerIntents.ACTION_SEARCH_STARTED));

        // Search in current path.
        searcher.dropPreviousResults();
        searcher.setRoot(root);
        searcher.search(root);

        // Search is over, let Receivers know.
        lbm.sendBroadcast(new Intent(FileManagerIntents.ACTION_SEARCH_FINISHED));
    }
}
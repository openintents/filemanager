package org.openintents.filemanager.search;

import java.io.File;
import java.io.FilenameFilter;

import org.openintents.intents.FileManagerIntents;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Service that asynchronously executes file searches.
 * 
 * @author George Venios.
 * 
 */
public class SearchService extends IntentService {
	private LocalBroadcastManager lbm;
	private String query;
	private FilenameFilter filter;

	public SearchService() {
		super("SearchService");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		lbm = LocalBroadcastManager.getInstance(getApplicationContext());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// The search query
		query = intent.getStringExtra(FileManagerIntents.EXTRA_SEARCH_QUERY);

		// Set initial path. To be searched first!
		String path = intent
				.getStringExtra(FileManagerIntents.EXTRA_SEARCH_INIT_PATH);
		File root = null;
		if (path != null)
			root = new File(path);
		else
			root = new File("/");

		// The actual search filter.
		filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(query);
			}
		};

		// Search started, let Receivers know.
		lbm.sendBroadcast(new Intent(FileManagerIntents.ACTION_SEARCH_STARTED));

		// Search in current path.
		dropPreviousResults();
		search(root);

		// Search is over, let Receivers know.
		lbm.sendBroadcast(new Intent(FileManagerIntents.ACTION_SEARCH_FINISHED));
	}

	/**
	 * Core search function. Recursively searches filenames from root to the leaves.
	 * 
	 * @param root
	 */
	private void search(File root) {
		// Results in root pass
		for (File f : root.listFiles(filter)) {
			insertResult(f);

			// Let receivers know about results' update. Not actually needed since we're using a content provider and it's automatically refreshing itself.
			lbm.sendBroadcast(new Intent(
					FileManagerIntents.ACTION_SEARCH_RESULTS_UPDATED));
		}

		// Recursion pass
		for (File f : root.listFiles()) {
			if (f.isDirectory() && f.canRead())
				search(f);
		}
	}

	private void insertResult(File f) {
		ContentValues values = new ContentValues();
		values.put(SearchResultsProvider.COLUMN_NAME, f.getName());
		values.put(SearchResultsProvider.COLUMN_PATH, f.getAbsolutePath());
		getContentResolver().insert(SearchResultsProvider.CONTENT_URI, values);
	}

	private int dropPreviousResults() {
		return getContentResolver().delete(SearchResultsProvider.CONTENT_URI, null,
				null);
	}
}
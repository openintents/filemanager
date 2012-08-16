package org.openintents.filemanager.search;

import java.io.File;
import java.io.FilenameFilter;

import org.openintents.filemanager.R;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

/**
 * Provides the search core, used by every search subsystem that provides results.
 * 
 * @author George Venios
 * 
 */
public class SearchCore {
	private String mQuery;
	private Uri mContentURI;
	private Context mContext;
	/** See {@link #setRoot(File)} */
	private File root = Environment.getExternalStorageDirectory();

	private int mResultCount = 0;
	private int mMaxResults = -1;
	
	private long mMaxNanos = -1;
	private long mStart;
	
	public SearchCore(Context context) {
		mContext = context;
	}

	private FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return mQuery == null ? false : filename.toLowerCase().contains(mQuery.toLowerCase());
		}
	};

	public void setQuery(String q) {
		mQuery = q;
	}

	public String getQuery() {
		return mQuery;
	}

	/**
	 * Set the first directory to recursively search.
	 * 
	 * @param root
	 *            The directory to search first.
	 */
	public void setRoot(File root) {
		this.root = root;
	}

	/**
	 * Set the content URI, of which the results are. Used for operations on the correct search content providers.
	 * 
	 * @param URI
	 *            The URI.
	 */
	public void setURI(Uri URI) {
		mContentURI = URI;
	}

	/**
	 * Set the maximum number of results to get before search ends.
	 * 
	 * @param i
	 *            Zero or less will be ignored. The desired number of results.
	 */
	public void setMaxResults(int i) {
		mMaxResults = i;
	}
	
	/**
	 * Call this to start the search stopwatch. First call of this should be right before the call to {@link #search(File)}.
	 * @param maxNanos The search duration in nanos.
	 */
	public void startClock(long maxNanos){
		mMaxNanos = maxNanos;
		mStart = System.nanoTime();
	}

	private void insertResult(File f) {
		mResultCount++;

		ContentValues values = new ContentValues();

		if (mContentURI == SearchResultsProvider.CONTENT_URI) {
			values.put(SearchResultsProvider.COLUMN_NAME, f.getName());
			values.put(SearchResultsProvider.COLUMN_PATH, f.getAbsolutePath());
		} else if (mContentURI == SearchSuggestionsProvider.CONTENT_URI) {
			values.put(SearchManager.SUGGEST_COLUMN_ICON_1,
					f.isDirectory() ? R.drawable.ic_launcher_folder
							: R.drawable.ic_launcher_file);
			values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, f.getName());
			values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, f.getAbsolutePath());
			values.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA,
					f.getAbsolutePath());
		}

		mContext.getContentResolver().insert(mContentURI, values);
	}

	/**
	 * Reset the results of the previous queries.
	 * 
	 * @return The previous result count.
	 */
	public int dropPreviousResults() {
		mResultCount = 0;
		return mContext.getContentResolver().delete(mContentURI, null, null);
	}

	/**
	 * Core search function. Recursively searches files from root of external storage to the leaves. Prioritizes {@link #root}'s subtree.
	 * 
	 * @param dir
	 *            The starting dir for the search. Callers outside of this class are highly encouraged to use the same as {@link #root}.
	 */
	public void search(File dir) {
		// Results in root pass
		for (File f : dir.listFiles(filter)) {
			insertResult(f);
			
			// Break search on result count and search time conditions.
			if ((mMaxResults > 0 && mResultCount >= mMaxResults) || (mMaxNanos > 0 && System.nanoTime()-mStart > mMaxNanos)) {
				return;
			}
		}

		// Recursion pass
		for (File f : dir.listFiles()) {
			// Prevent us from re-searching the root directory, or trying to search invalid Files.
			if (f.isDirectory() && f.canRead() && !isChildOf(f, root))
				search(f);
		}

		// If we're on the parent of the recursion, and we're done searching, start searching the rest of the FS.
		if (dir.equals(root) && !root.equals(Environment.getExternalStorageDirectory())) {
			search(Environment.getExternalStorageDirectory());
		}
	}

	/**
	 * @param f1
	 * @param f2
	 * @return If f1 is child of f2. Also true if f1 equals f2.
	 */
	private boolean isChildOf(File f1, File f2) {
		return f2.getAbsolutePath().startsWith(f1.getAbsolutePath());
	}
}
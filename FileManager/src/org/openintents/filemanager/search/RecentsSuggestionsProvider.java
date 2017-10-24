package org.openintents.filemanager.search;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Simple recents suggestion provider. Not currently used, as we use a custom one. To use just point the searchable.xml to this provider. It's way faster, but disables access of the QuickSearchBox to OIFM.
 *
 * @author George Venios
 */
public class RecentsSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "org.openintents.filemanager.search.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentsSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
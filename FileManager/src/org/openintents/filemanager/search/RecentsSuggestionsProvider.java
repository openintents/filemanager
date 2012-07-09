package org.openintents.filemanager.search;

import android.content.SearchRecentSuggestionsProvider;

public class RecentsSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "org.openintents.filemanager.search.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentsSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
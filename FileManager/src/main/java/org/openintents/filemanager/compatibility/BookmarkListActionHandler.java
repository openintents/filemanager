package org.openintents.filemanager.compatibility;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import org.openintents.filemanager.R;
import org.openintents.filemanager.bookmarks.BookmarkListAdapter;
import org.openintents.filemanager.bookmarks.BookmarksProvider;

public class BookmarkListActionHandler {
    private BookmarkListActionHandler() {
    }

    /**
     * Offers a centralized bookmark action execution component.
     *
     * @param item The MenuItem selected.
     * @param list The list to act upon.
     */
    public static void handleItemSelection(MenuItem item, ListView list) {

        // Single selection
        if (ListViewMethodHelper.listView_getCheckedItemCount(list) == 1) {

            // Get id of selected bookmark.
            long id = -1;
            if (item.getMenuInfo() instanceof AdapterContextMenuInfo)
                id = list.getAdapter().getItemId(((AdapterContextMenuInfo) item.getMenuInfo()).position);
            id = ListViewMethodHelper.listView_getCheckedItemIds(list)[0];

            // Handle selection
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    list.getContext().getContentResolver().delete(BookmarksProvider.CONTENT_URI, BookmarksProvider._ID + "=?", new String[]{Long.toString(id)});
                    break;
            }
            // Multiple selection
        } else {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    long[] ids = ListViewMethodHelper.listView_getCheckedItemIds(list);
                    for (int i = 0; i < ids.length; i++) {
                        list.getContext().getContentResolver().delete(BookmarksProvider.CONTENT_URI, BookmarksProvider._ID + "=?", new String[]{Long.toString(ids[i])});
                    }
                    break;
            }
        }

        ((BookmarkListAdapter) list.getAdapter()).notifyDataSetChanged();
    }
}
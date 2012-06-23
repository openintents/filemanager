package org.openintents.filemanager.compatibility;

import org.openintents.filemanager.R;
import org.openintents.filemanager.bookmarks.BookmarkListAdapter;
import org.openintents.filemanager.bookmarks.BookmarksProvider;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BookmarkListActionHandler {
	/**
	 * Offers a centralized bookmark action execution component.
	 * 
	 * @param item
	 *            The MenuItem selected.
	 * @param list
	 *            The list to act upon.
	 *            
	 * @param pos The selected item's position.
	 */
	public static void handleItemSelection(MenuItem item, ListView list) {
		
		// Single selection
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB
				|| ListViewMethodHelper.listView_getCheckedItemCount(list) == 1) {

			// Get id of selected bookmark.
			long id = -1;
			if(item.getMenuInfo() instanceof AdapterContextMenuInfo)
				id = list.getAdapter().getItemId(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			if (VERSION.SDK_INT > VERSION_CODES.HONEYCOMB)
				id = ListViewMethodHelper.listView_getCheckedItemIds(list)[0];
			
			// Handle selection
			switch (item.getItemId()) {
			case R.id.menu_delete:
				list.getContext().getContentResolver().delete(BookmarksProvider.CONTENT_URI, BookmarksProvider._ID + "=?", new String[] {""+id});
				break;
			}
			// Multiple selection
		} else {
			switch (item.getItemId()) {
			case R.id.menu_delete:
				long[] ids = ListViewMethodHelper.listView_getCheckedItemIds(list);
				for(int i=0; i<ids.length; i++){
					list.getContext().getContentResolver().delete(BookmarksProvider.CONTENT_URI, BookmarksProvider._ID + "=?", new String[] {""+ids[i]});
				}
				break;
			}
		}
		
		((BookmarkListAdapter)list.getAdapter()).notifyDataSetChanged();
	}
}
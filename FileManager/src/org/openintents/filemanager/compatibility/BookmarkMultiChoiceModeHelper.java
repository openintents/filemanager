package org.openintents.filemanager.compatibility;

import org.openintents.filemanager.R;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

public class BookmarkMultiChoiceModeHelper {

	public static void listView_setMultiChoiceModeListener(final ListView list, final Context context) {
		list.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onPrepareActionMode(android.view.ActionMode mode,
					Menu menu) {
				
				mode.getMenuInflater().inflate(R.menu.bookmarks, menu);
				
				return true;
			}

			@Override
			public void onDestroyActionMode(android.view.ActionMode mode) {
			}

			@Override
			public boolean onCreateActionMode(android.view.ActionMode mode,
					Menu menu) {
				return true;
			}

			@Override
			public boolean onActionItemClicked(android.view.ActionMode mode,
					MenuItem item) {
				BookmarkListActionHandler.handleItemSelection(item, list);
				mode.finish();
				return true;
			}

			@Override
			public void onItemCheckedStateChanged(android.view.ActionMode mode,
					int position, long id, boolean checked) {
				mode.setTitle(list.getCheckedItemCount() + " " + context.getResources().getString(R.string.selected));
			}
		});
	}
}
package org.openintents.filemanager.compatibility;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.PathBar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

/**
 * This class helps wrap some of the platform specific logic of MultiChoiceMode of Honeycomb and up, 
 * while keeping the app compliant with API levels that do not ignore {@link VerifyError}s  and crash the app.
 * 
 * @author George Venios
 * 
 */
public class FileMultiChoiceModeHelper {
	private ListView list;
	private PathBar pathbar;
	private FileManagerActivity activity;

	public void setListView(ListView list) {
		this.list = list;
		list.setMultiChoiceModeListener(listener);
	}

	public void setPathBar(PathBar p) {
		pathbar = p;
	}

	public void setContext(FileManagerActivity a) {
		activity = a;
	}

	public MultiChoiceModeListener listener = new MultiChoiceModeListener() {

		@Override
		public boolean onPrepareActionMode(android.view.ActionMode mode,
				Menu menu) {
			menu.clear();
			MenuInflater inflater = mode.getMenuInflater();

			switch (list.getCheckedItemCount()) {
			// Single selection
			case 1:
				MenuUtils.fillContextMenu((FileHolder) list.getAdapter().getItem(getSelectedPosition()), menu, mode.getMenuInflater(), list.getContext());
				break;
			// Multiple selection
			default:
				inflater.inflate(R.menu.multiselect, menu);
				break;
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(android.view.ActionMode mode) {
			pathbar.setEnabled(true);
		}

		@Override
		public boolean onCreateActionMode(android.view.ActionMode mode,
				Menu menu) {
			pathbar.setEnabled(false);
			return true;
		}

		@Override
		public boolean onActionItemClicked(android.view.ActionMode mode,
				MenuItem item) {
			boolean res = false;
			switch (list.getCheckedItemCount()) {
			// Single selection
			case 1:
				res = activity.handleSingleSelectionAction(item,
						(FileHolder) list.getAdapter().getItem(getSelectedPosition()));
				break;
			// Multiple selection
			default:
// TODO				res = activity.handleMultipleSelectionAction(item);
				break;
				
			}
			mode.finish();

			return res;
		}

		@Override
		public void onItemCheckedStateChanged(android.view.ActionMode mode,
				int position, long id, boolean checked) {
			mode.setTitle(list.getCheckedItemCount() + " "
					+ activity.getResources().getString(R.string.selected));

			// Force actions' refresh
			mode.invalidate();
		}
	};

	/**
	 * This is error free since FileHolderListAdapter uses stableIds and getItemId(int) returns the int passed (the position of the item).
	 * @return 
	 */
	private int getSelectedPosition() {
		return (int) list.getCheckedItemIds()[0];
	}

	public void finish() {
//		list.setc
	}
}
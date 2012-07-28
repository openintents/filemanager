package org.openintents.filemanager.compatibility;

import java.util.ArrayList;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
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
	private SimpleFileListFragment fragment;

	public void setListView(ListView list) {
		this.list = list;
		list.setMultiChoiceModeListener(listener);
	}

	public void setPathBar(PathBar p) {
		pathbar = p;
	}

	public void setContext(SimpleFileListFragment f) {
		fragment = f;
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
				MenuUtils.fillMultiselectionMenu(menu, mode.getMenuInflater());
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
				res = MenuUtils.handleSingleSelectionAction(fragment, item,
						(FileHolder) list.getAdapter().getItem(getSelectedPosition()), fragment.getActivity());
				break;
			// Multiple selection
			default:
				res = MenuUtils.handleMultipleSelectionAction(fragment, item, getCheckedItems(), fragment.getActivity());
				break;
			}
			mode.finish();

			return res;
		}

		@Override
		public void onItemCheckedStateChanged(android.view.ActionMode mode,
				int position, long id, boolean checked) {
			mode.setTitle(list.getCheckedItemCount() + " "
					+ fragment.getActivity().getResources().getString(R.string.selected));

			// Force actions' refresh
			mode.invalidate();
		}
	};

	/**
	 * This is error free only when FileHolderListAdapter uses stableIds and getItemId(int) returns the int passed (the position of the item).
	 * @return 
	 */
	private int getSelectedPosition() {
		return (int) list.getCheckedItemIds()[0];
	}
	
	/**
	 * @return A {@link FileHolder} list with the currently selected items.
	 */
	private ArrayList<FileHolder> getCheckedItems(){
		ArrayList<FileHolder> items = new ArrayList<FileHolder>();
		
		for(long pos : list.getCheckedItemIds()) {
			items.add((FileHolder) list.getAdapter().getItem((int) pos));
		}
		
		return items;
	}
}
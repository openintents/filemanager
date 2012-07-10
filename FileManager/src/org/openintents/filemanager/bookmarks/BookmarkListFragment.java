package org.openintents.filemanager.bookmarks;

import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.BookmarkListActionHandler;
import org.openintents.filemanager.compatibility.BookmarkMultiChoiceModeHelper;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * @author George Venios
 */
public class BookmarkListFragment extends ListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new BookmarkListAdapter(getActivity()));
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		setEmptyText(getString(R.string.bookmark_empty));
		
		// Handle item selection.
		if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			registerForContextMenu(getListView());
		} else {
			BookmarkMultiChoiceModeHelper.listView_setMultiChoiceModeListener(getListView(), getActivity());
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		BookmarkListActionHandler.handleItemSelection(item, getListView());
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inf = new MenuInflater(getActivity());
		inf.inflate(R.menu.bookmarks, menu);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String path = ((BookmarkListAdapter.Bookmark) getListAdapter().getItem(position)).path;
		((BookmarkListActivity) getActivity()).onListItemClick(path);
	}
}
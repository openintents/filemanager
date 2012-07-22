package org.openintents.filemanager.lists;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.compatibility.FileMultiChoiceModeHelper;
import org.openintents.filemanager.files.FileHolder;

import android.os.Build;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * A file list fragment that supports context menu and CAB selection.
 * @author George Venios
 */
public class SelectableFileListFragment extends FileListFragment {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if(VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			registerForContextMenu(getListView());
		}
		else{
			FileMultiChoiceModeHelper multiChoiceModeHelper = new FileMultiChoiceModeHelper();
			multiChoiceModeHelper.setListView(getListView());
			//	TODO	multiChoiceModeHelper.setPathBar(mPathBar);
			// TODO decouple.
			multiChoiceModeHelper.setContext((FileManagerActivity) getActivity());
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = new MenuInflater(getActivity());
		
		// Obtain context menu info
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return;
		}
		
		((FileManagerActivity) getActivity()).fillContextMenu(getListView(), menu, inflater, info.position);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		FileHolder fh = (FileHolder) mAdapter.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
		return ((FileManagerActivity) getActivity()).handleSingleSelectionAction(item, fh);
	}
}
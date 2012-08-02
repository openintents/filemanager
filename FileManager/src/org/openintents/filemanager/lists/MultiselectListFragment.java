package org.openintents.filemanager.lists;

import java.util.ArrayList;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.LegacyActionContainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older than Honeycomb.
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
	private static final String INSTANCE_STATE_LIST_SELECTION = "list_selection";
	
	private LegacyActionContainer mLegacyActionContainer;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLongArray(INSTANCE_STATE_LIST_SELECTION, getListView().getCheckItemIds());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_legacy_multiselect, null);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter.setItemLayout(R.layout.item_filelist_multiselect);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		super.onViewCreated(view, savedInstanceState);
		
		// Init members
		mLegacyActionContainer =  (LegacyActionContainer) view.findViewById(R.id.action_container);
		mLegacyActionContainer.setMenuResource(R.menu.multiselect);
		mLegacyActionContainer.setOnActionSelectedListener(new LegacyActionContainer.OnActionSelectedListener() {
			@Override
			public void actionSelected(MenuItem item) {
				ArrayList<FileHolder> fItems = new ArrayList<FileHolder>();
				
				for(long i : getListView().getCheckItemIds()){
					fItems.add((FileHolder) mAdapter.getItem((int) i));
				}
				
				MenuUtils.handleMultipleSelectionAction(MultiselectListFragment.this, item, fItems, getActivity());
			}
		});
		
		restoreSelection(savedInstanceState);
	}
	
	private void restoreSelection(Bundle state){
		if(state == null)
			return;
		
		long[] positions = state.getLongArray(INSTANCE_STATE_LIST_SELECTION);
		ListView list = getListView();
		
		for(long i : positions){
			list.setItemChecked((int) i, true);
		}
		
		list.invalidate();
	}
}
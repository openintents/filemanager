package org.openintents.filemanager.lists;

import java.util.ArrayList;

import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.MenuUtils;
import org.openintents.filemanager.view.LegacyActionContainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Dedicated file list fragment, used for multiple selection on platforms older than Honeycomb.
 * @author George Venios
 */
public class MultiselectListFragment extends FileListFragment {
	private static final String INSTANCE_STATE_LIST_SELECTION = "list_selection";
	
	private LegacyActionContainer mLegacyActionContainer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_legacy_multiselect, null);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter.setItemLayout(R.layout.item_filelist_multiselect);
		
		setHasOptionsMenu(true);
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
				if(getListView().getCheckItemIds().length == 0){
					Toast.makeText(getActivity(), R.string.no_selection, Toast.LENGTH_SHORT).show();
					return;
				}
				
				ArrayList<FileHolder> fItems = new ArrayList<FileHolder>();
				
				for(long i : getListView().getCheckItemIds()){
					fItems.add((FileHolder) mAdapter.getItem((int) i));
				}
				
				MenuUtils.handleMultipleSelectionAction(MultiselectListFragment.this, item, fItems, getActivity());
			}
		});
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.options_multiselect, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ListView list = getListView();
		
		switch(item.getItemId()){
		case R.id.check_all:
			for(int i = 0; i < mAdapter.getCount(); i++){
				list.setItemChecked(i, true);
			}
			return true;
		case R.id.uncheck_all:
			for(int i = 0; i < mAdapter.getCount(); i++){
				list.setItemChecked(i, false);
			}
			return true;
		default:
			return false;
		}
	}
}
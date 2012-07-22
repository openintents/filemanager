package org.openintents.filemanager.lists;

import java.io.File;

import org.openintents.filemanager.FileManagerActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.compatibility.FileMultiChoiceModeHelper;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.view.PathBar;
import org.openintents.filemanager.view.PathBar.OnDirectoryChangedListener;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A file list fragment that supports context menu and CAB selection. Check {@link #setPathBar(PathBar)} for info on list item click behavior.
 * @author George Venios
 */
public class SimpleFileListFragment extends FileListFragment {
	private PathBar mPathBar;

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
	
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
         FileHolder item = (FileHolder) mAdapter.getItem(position);
         
         if(item.getFile().isDirectory() && mPathBar != null)
        	 mPathBar.cd(item.getFile());
         else
        	 open(item);
	}
    
    /**
	 * If the {@link PathBar} this {@link Fragment} holds is null, clicks on the list directly open the file. 
	 * Else, clicks pass through the set {@link PathBar} and we then change this directory of this fragment.
	 * 
	 * @param pathBar Can be null.
	 */
    public void setPathBar(PathBar pathBar){
    	mPathBar = pathBar;
    	
    	mPathBar.setOnDirectoryChangedListener(new OnDirectoryChangedListener() {
			
			@Override
			public void directoryChanged(File newCurrentDir) {
				open(new FileHolder(newCurrentDir, getActivity()));
			}
		});
    }
    
    /**
     * Point this Fragment to show the contents of the passed file.
     * @param f If same as current, does nothing.
     */
    public void open(FileHolder f){
    	// Avoid unnecessary attempts to load.
    	if(!f.getFile().exists() || f.getFile().getAbsolutePath().equals(mPath))
    		return;
    	
    	if(f.getFile().isDirectory()){
    		mScanner.setCurrentDirectory(f.getFile());
    		mScanner.restart();
    		
    		mPath = f.getFile().getAbsolutePath();
    	}
    	else if(f.getFile().isFile()){
    		openFile(f);
    	}
    }

    /**
     * Attempts to open a file for viewing.
     * @param fileholder The holder of the file to open.
     */
	private void openFile(FileHolder fileholder) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

		Uri data = FileUtils.getUri(fileholder.getFile());
		String type = fileholder.getMimeType();
		intent.setDataAndType(data, type);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(), R.string.application_not_available,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Reloads the current directory's contents.
	 */
	public void refresh(){
		mScanner.restart();
	}
}
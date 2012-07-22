package org.openintents.filemanager.lists;

import java.io.File;
import java.util.ArrayList;

import org.openintents.filemanager.FileHolderListAdapter;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.DirectoryContents;
import org.openintents.filemanager.files.DirectoryScanner;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.intents.FileManagerIntents;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ViewFlipper;

/**
 * A {@link ListFragment} that displays the contents of a directory. Clicks do nothing.
 * @author George Venios
 */
public abstract class FileListFragment extends ListFragment {
	protected FileHolderListAdapter mAdapter;
	protected DirectoryScanner mScanner;
	protected ArrayList<FileHolder> mFiles = new ArrayList<FileHolder>();
	protected String mPath;
	
	private ViewFlipper mFlipper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get arguments
		mPath = getArguments().getString(FileManagerIntents.EXTRA_DIR_PATH);
		renewScanner();
		mAdapter = new FileHolderListAdapter(mFiles, getActivity());
		
		setListAdapter(mAdapter);
		mScanner.start();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// Set list properties
		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mAdapter.setScrolling(false);
					mAdapter.notifyDataSetChanged();
				} else
					mAdapter.setScrolling(true);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		getListView().requestFocus();
		getListView().requestFocusFromTouch();
		
		// Init flipper
		mFlipper = (ViewFlipper) view.findViewById(R.id.flipper);
	}
	
	@Override
	public void onDestroy() {
		mScanner.cancel();
		super.onDestroy();
	}

	/**
	 * Reloads the current directory's contents.
	 */
	protected void refresh() {
		showLoadingIndicator(true);
		mFiles.clear();
		mAdapter.notifyDataSetChanged();
		
		renewScanner().start();
	}
	
	/**
	 * Whether to show the loading indicator or not.
	 */
	private void showLoadingIndicator(boolean show){
		mFlipper.setDisplayedChild(show ? 0 : 1);
	}

	/**
	 * Recreates the {@link #mScanner} using the previously set arguments.
	 * @return {@link #mScanner} for convenience.
	 */
	protected DirectoryScanner renewScanner() {
		String filetypeFilter = getArguments().getString(FileManagerIntents.EXTRA_FILTER_FILETYPE);
		String mimetypeFilter = getArguments().getString(FileManagerIntents.EXTRA_FILTER_MIMETYPE);
		boolean writeableOnly = getArguments().getBoolean(FileManagerIntents.EXTRA_WRITEABLE_ONLY);
		boolean directoriesOnly = getArguments().getBoolean(FileManagerIntents.EXTRA_DIRECTORIES_ONLY);
		
		mScanner = new DirectoryScanner(new File(mPath), getActivity(),
				new FileListMessageHandler(),
				MimeTypes.newInstance(getActivity()),
				filetypeFilter == null ? "" : filetypeFilter,
				mimetypeFilter == null ? "" : mimetypeFilter, writeableOnly,
				directoriesOnly);
		return mScanner;
	}
	
	private class FileListMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case DirectoryScanner.MESSAGE_SHOW_DIRECTORY_CONTENTS:
					DirectoryContents c = (DirectoryContents) msg.obj;
					mFiles.clear();
					mFiles.addAll(c.listSdCard);
					mFiles.addAll(c.listDir);
					mFiles.addAll(c.listFile);
					
					mAdapter.notifyDataSetChanged();
					showLoadingIndicator(false);
					break;
				case DirectoryScanner.MESSAGE_SET_PROGRESS:
// TODO, idk					((FileManagerActivity) getActivity()).setProgress(msg.arg1, msg.arg2);
					break;
			}
		}
	}
}

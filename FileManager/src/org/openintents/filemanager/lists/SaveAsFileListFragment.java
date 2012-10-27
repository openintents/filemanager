package org.openintents.filemanager.lists;

import java.io.File;

import org.openintents.filemanager.FileManagerProvider;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.view.SaveAsBar;
import org.openintents.filemanager.view.SaveAsBar.onSaveRequestedListener;
import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SaveAsFileListFragment extends PickFileListFragment {
	private SaveAsBar mSaveBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_saveas, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSaveBar = (SaveAsBar) view.findViewById(R.id.saveAsBar);
		mSaveBar.setOnSaveRequestedListener(new onSaveRequestedListener() {
			@Override
			public void saveRequested(String filename) {
				if(filename.trim().length() == 0) {
					Toast.makeText(getActivity(), R.string.choose_filename, Toast.LENGTH_SHORT).show();
					return;
				}

				// Pick logic has come here in this class as filelist clicks had to be overriden.
				File selection = new File(getPath() + (getPath().endsWith("/") ? "" : "/") + filename);
				
				Intent intent = new Intent();
				PreferenceActivity.setDefaultPickFilePath(getActivity(), selection.getParent() != null ?  selection.getParent() : "/");
				
				if(getArguments().getBoolean(FileManagerIntents.EXTRA_IS_GET_CONTENT_INITIATED, false))
					intent.setData(Uri.parse(FileManagerProvider.FILE_PROVIDER_PREFIX + selection));
				else
					intent.setData(Uri.fromFile(selection));
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
			}
		});
	}
	
	/**
	 * "Folder" picking is only enabled if we pass a {@link FileManagerIntents#EXTRA_DIRECTORIES_ONLY} which is irrelevant for the uses of this class. We still perform the isFile() check though.
	 */
	@Override
	protected void pickFileOrFolder(File selection, boolean getContentInitiated) {
		if(selection.isFile())
			mSaveBar.setText(selection.getName());
	}
}
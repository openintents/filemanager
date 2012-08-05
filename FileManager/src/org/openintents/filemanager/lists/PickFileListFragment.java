package org.openintents.filemanager.lists;

import org.openintents.filemanager.FileManagerProvider;
import org.openintents.filemanager.PreferenceActivity;
import org.openintents.filemanager.R;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PickFileListFragment extends SimpleFileListFragment{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionsEnabled(false);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filelist_pick, null);
	}
	
	@Override
	protected void openFile(FileHolder fileholder) {
		// GET_CONTENT or PICK_FILE request. If it was PICK_DIRECTORY, we wouldn't have shown files, and therefore we would have never reached this call.
		Intent intent = new Intent();
		PreferenceActivity.setDefaultPickFilePath(getActivity(), fileholder.getFile().getParent() != null ? 
				fileholder.getFile().getParent() : "/");
		
		if(getArguments().getBoolean(FileManagerIntents.EXTRA_IS_GET_CONTENT_INITIATED, false))
			intent.setData(Uri.parse(FileManagerProvider.FILE_PROVIDER_PREFIX + fileholder.getFile()));
		else
			intent.setData(Uri.fromFile(fileholder.getFile()));
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
		return;
	}
}
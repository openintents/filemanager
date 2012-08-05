package org.openintents.filemanager.lists;

import java.io.File;

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
import android.widget.Button;

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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		if(getArguments().getBoolean(FileManagerIntents.EXTRA_DIRECTORIES_ONLY)){
			Button pickButton = (Button) view.findViewById(R.id.button);
			pickButton.setVisibility(View.VISIBLE);
			pickButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickFileOrFolder(new File(getPath()), false);
				}
			});
		}
	}
	
	@Override
	protected void openFile(FileHolder fileholder) {
		// GET_CONTENT or PICK_FILE request. If it was PICK_DIRECTORY, we wouldn't have shown files, and therefore we would have never reached this call.
		
		pickFileOrFolder(fileholder.getFile(), getArguments().getBoolean(FileManagerIntents.EXTRA_IS_GET_CONTENT_INITIATED, false));

	}
	
	/**
	 * Act upon picking. 
	 * @param selection A {@link File} representing the user's selection.
	 * @param getContentInitiated Whether the fragment was called through a GET_CONTENT intent on the IntentFilterActivity. We have to know this so that result is correctly formatted.
	 */
	private void pickFileOrFolder(File selection, boolean getContentInitiated){
		Intent intent = new Intent();
		PreferenceActivity.setDefaultPickFilePath(getActivity(), selection.getParent() != null ?  selection.getParent() : "/");
		
		if(getContentInitiated)
			intent.setData(Uri.parse(FileManagerProvider.FILE_PROVIDER_PREFIX + selection));
		else
			intent.setData(Uri.fromFile(selection));
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}
}
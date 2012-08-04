package org.openintents.filemanager.lists;

import org.openintents.filemanager.R;

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
	
//	private void pickFileOrDirectory() {
//		File file = null;
//		if (mState == STATE_PICK_FILE) {
//			String filename = mEditFilename.getText().toString();
//			file = FileUtils.getFile(mPathBar.getCurrentDirectory().getAbsolutePath(), filename);
//		} else if (mState == STATE_PICK_DIRECTORY) {
//			file = mPathBar.getCurrentDirectory();
//		}
//		
//		PreferenceActivity.setDefaultPickFilePath(this, mPathBar.getCurrentDirectory().getAbsolutePath());
//    	 
//    	Intent intent = getIntent();
//    	intent.setData(FileUtils.getUri(file));
//    	setResult(RESULT_OK, intent);
//    	finish();
//     }
}
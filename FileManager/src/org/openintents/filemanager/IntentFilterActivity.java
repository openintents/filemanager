package org.openintents.filemanager;

import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.filemanager.lists.MultiselectListFragment;
import org.openintents.filemanager.lists.PickFileListFragment;
import org.openintents.intents.FileManagerIntents;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

public class IntentFilterActivity extends FragmentActivity {
	private FileListFragment mFragment;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Intent intent = getIntent();

		// Initialize arguments
		Bundle args = intent.getExtras();
		if(args == null)
			args = new Bundle();
		// Add a path if path is not specified in this activity's call
		if(!args.containsKey(FileManagerIntents.EXTRA_DIR_PATH))
			args.putString(FileManagerIntents.EXTRA_DIR_PATH, PreferenceActivity.getDefaultPickFilePath(this));
		// Add a mimetype filter if it was specified through the type of the intent.
		if(!args.containsKey(FileManagerIntents.EXTRA_FILTER_MIMETYPE) && intent.getType() != null)
			args.putString(FileManagerIntents.EXTRA_FILTER_MIMETYPE, intent.getType());
		
		// Multiselect
		if(intent.getAction().equals(FileManagerIntents.ACTION_MULTI_SELECT)){
			String tag = "MultiSelectListFragment";
			mFragment = (MultiselectListFragment) getSupportFragmentManager().findFragmentByTag(tag);
			
			// Only add if it doesn't exist
			if(mFragment == null){
				mFragment = new MultiselectListFragment();
				// Pass extras through to the list fragment. This helps centralize the path resolving, etc.
				mFragment.setArguments(args);
				
				setTitle(R.string.multiselect_title);
				
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, mFragment, tag).commit();
			}
		}
		// Item pickers
		else if(intent.getAction().equals(FileManagerIntents.ACTION_PICK_DIRECTORY) || intent.getAction().equals(FileManagerIntents.ACTION_PICK_FILE) || intent.getAction().equals(Intent.ACTION_GET_CONTENT)){
			String tag = "PickFileListFragment";
			mFragment = (PickFileListFragment) getSupportFragmentManager().findFragmentByTag(tag);
			
			// Only add if it doesn't exist
			if(mFragment == null){
				mFragment = new PickFileListFragment();
				
				// Pass extras through to the list fragment. This helps centralize the path resolving, etc.
				args.putBoolean(FileManagerIntents.EXTRA_IS_GET_CONTENT_INITIATED, intent.getAction().equals(Intent.ACTION_GET_CONTENT));
				args.putBoolean(FileManagerIntents.EXTRA_DIRECTORIES_ONLY, intent.getAction().equals(FileManagerIntents.ACTION_PICK_DIRECTORY));
				
				setTitle(R.string.pick_title);
				
				mFragment.setArguments(args);
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, mFragment, tag).commit();
			}
		}
	}
	
	// The following methods should properly handle back button presses on every API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Only check fragment back-ability if we're on the filepicker fragment.
		if(mFragment instanceof PickFileListFragment)
			if (VERSION.SDK_INT > VERSION_CODES.DONUT) {
				if (keyCode == KeyEvent.KEYCODE_BACK && ((PickFileListFragment) mFragment).pressBack())
					return true;
			}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Only check fragment back-ability if we're on the filepicker fragment.
		if(mFragment instanceof PickFileListFragment)
			if (VERSION.SDK_INT <= VERSION_CODES.DONUT) {
				if (keyCode == KeyEvent.KEYCODE_BACK && ((PickFileListFragment) mFragment).pressBack())
					return true;
			}

		return super.onKeyDown(keyCode, event);
	}
}
package org.openintents.filemanager;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import java.io.File;

import org.openintents.filemanager.lists.FileListFragment;
import org.openintents.filemanager.lists.MultiselectListFragment;
import org.openintents.filemanager.lists.PickFileListFragment;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.UIUtils;
import org.openintents.intents.FileManagerIntents;

public class IntentFilterActivity extends FragmentActivity {
	private FileListFragment mFragment;

	@Override
	protected void onCreate(Bundle savedInstance) {
		UIUtils.setThemeFor(this);
		
		super.onCreate(savedInstance);
		
		Intent intent = getIntent();

		// Initialize arguments
		Bundle extras = intent.getExtras();
		if (extras == null)
			extras = new Bundle();
		// Add a path if path is not specified in this activity's call
		if (!extras.containsKey(FileManagerIntents.EXTRA_DIR_PATH)) {
			// Set a default path so that we launch a proper list.
			File defaultFile = new File(
					PreferenceActivity.getDefaultPickFilePath(this));
			if (!defaultFile.exists()) {
				PreferenceActivity.setDefaultPickFilePath(this, Environment
						.getExternalStorageDirectory().getAbsolutePath());
				defaultFile = new File(
						PreferenceActivity.getDefaultPickFilePath(this));
			}
			extras.putString(FileManagerIntents.EXTRA_DIR_PATH,
					defaultFile.getAbsolutePath());
		}

		// Add a path if a path has been specified in this activity's call.
		File data = FileUtils.getFile(getIntent().getData());
		if (data != null) {			
			File dir = FileUtils.getPathWithoutFilename(data);		
			if (dir != null) {
				extras.putString(FileManagerIntents.EXTRA_DIR_PATH,
						data.getAbsolutePath());
			}
			if (dir != data){
				// data is a file
				extras.putString(FileManagerIntents.EXTRA_FILENAME, data.getName());
			}
		}

		// Add a mimetype filter if it was specified through the type of the
		// intent.
		if (!extras.containsKey(FileManagerIntents.EXTRA_FILTER_MIMETYPE)
				&& intent.getType() != null)
			extras.putString(FileManagerIntents.EXTRA_FILTER_MIMETYPE,
					intent.getType());

		// Actually fill the ui
		chooseListType(intent, extras);
	}

	private void chooseListType(Intent intent, Bundle extras) {
		// Multiselect
		if (FileManagerIntents.ACTION_MULTI_SELECT.equals(intent.getAction())) {
			String tag = "MultiSelectListFragment";
			mFragment = (MultiselectListFragment) getSupportFragmentManager()
					.findFragmentByTag(tag);

			// Only add if it doesn't exist
			if (mFragment == null) {
				mFragment = new MultiselectListFragment();
				// Pass extras through to the list fragment. This helps
				// centralize the path resolving, etc.
				mFragment.setArguments(extras);

				setTitle(R.string.multiselect_title);

				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, mFragment, tag).commit();
			}
		}
		// Item pickers
		else if ( FileManagerIntents.ACTION_PICK_DIRECTORY.equals(intent.getAction())
				|| FileManagerIntents.ACTION_PICK_FILE.equals(intent.getAction())
				|| Intent.ACTION_GET_CONTENT.equals(intent.getAction())){
			if (intent.hasExtra(FileManagerIntents.EXTRA_TITLE))
				setTitle(intent.getStringExtra(FileManagerIntents.EXTRA_TITLE));
			else
				setTitle(R.string.pick_title);

			mFragment = (PickFileListFragment) getSupportFragmentManager()
					.findFragmentByTag(PickFileListFragment.class.getName());

			// Only add if it doesn't exist
			if (mFragment == null) {
				mFragment = new PickFileListFragment();

				// Pass extras through to the list fragment. This helps
				// centralize the path resolving, etc.
				extras.putBoolean(
						FileManagerIntents.EXTRA_IS_GET_CONTENT_INITIATED,
						intent.getAction().equals(Intent.ACTION_GET_CONTENT));
				extras.putBoolean(
						FileManagerIntents.EXTRA_DIRECTORIES_ONLY,
						intent.getAction().equals(
								FileManagerIntents.ACTION_PICK_DIRECTORY));

				mFragment.setArguments(extras);
				getSupportFragmentManager()
						.beginTransaction()
						.add(android.R.id.content, mFragment,
								PickFileListFragment.class.getName()).commit();
			}
		}
	}

	// The following methods should properly handle back button presses on every
	// API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// Only check fragment back-ability if we're on the filepicker fragment.
		if (mFragment instanceof PickFileListFragment)
			if (VERSION.SDK_INT > VERSION_CODES.DONUT) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& ((PickFileListFragment) mFragment).pressBack())
					return true;
			}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Only check fragment back-ability if we're on the filepicker fragment.
		if (mFragment instanceof PickFileListFragment)
			if (VERSION.SDK_INT <= VERSION_CODES.DONUT) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& ((PickFileListFragment) mFragment).pressBack())
					return true;
			}

		return super.onKeyDown(keyCode, event);
	}
}

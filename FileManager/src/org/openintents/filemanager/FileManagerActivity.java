/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.filemanager;

import java.io.File;

import org.openintents.filemanager.bookmarks.BookmarkListActivity;
import org.openintents.filemanager.compatibility.HomeIconHelper;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.UIUtils;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FileManagerActivity extends DistributionLibraryFragmentActivity {

	private static final String FRAGMENT_TAG = "ListFragment";

	protected static final int REQUEST_CODE_BOOKMARKS = 1;

	private SimpleFileListFragment mFragment;

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getData() != null)
			mFragment.openInformingPathBar(new FileHolder(FileUtils
					.getFile(intent.getData()), this));
	}

	/**
	 * Either open the file and finish, or navigate to the designated directory.
	 * This gives FileManagerActivity the flexibility to actually handle file
	 * scheme data of any type.
	 * 
	 * @return The folder to navigate to, if applicable. Null otherwise.
	 */
	private File resolveIntentData() {
		File data = FileUtils.getFile(getIntent().getData());
		if (data == null)
			return null;

		if (data.isFile()) {
			FileUtils.openFile(new FileHolder(data, this), this);
			finish();
			return null;
		} else
			return FileUtils.getFile(getIntent().getData());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		UIUtils.setThemeFor(this);

		super.onCreate(icicle);

		mDistribution.setFirst(MENU_DISTRIBUTION_START,
				DIALOG_DISTRIBUTION_START);

		// Check whether EULA has been accepted
		// or information about new version can be presented.
		if (mDistribution.showEulaOrNewVersion()) {
			return;
		}

		// Enable home button.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			HomeIconHelper.activity_actionbar_setHomeButtonEnabled(this);

		// Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager()
				.findFragmentByTag(FRAGMENT_TAG);
		if (mFragment == null) {
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if (data == null)
				args.putString(
						FileManagerIntents.EXTRA_DIR_PATH,
						Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED) ? Environment
								.getExternalStorageDirectory()
								.getAbsolutePath() : "/");
			else
				args.putString(FileManagerIntents.EXTRA_DIR_PATH,
						data.toString());
			mFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mFragment, FRAGMENT_TAG)
					.commit();
		} else {
			// If we didn't rotate and data wasn't null.
			if (icicle == null && data != null)
				mFragment.openInformingPathBar(new FileHolder(new File(data
						.toString()), this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main, menu);

		mDistribution.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Generate any additional actions that can be performed on the
		// overall list. This allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);

		// Workaround to add icons:
		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
				menu);
		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, FileManagerActivity.class), null,
				intent, 0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_search) {
			onSearchRequested();
			return true;
		} else if (item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.menu_bookmarks) {
			startActivityForResult(new Intent(FileManagerActivity.this,
					BookmarkListActivity.class), REQUEST_CODE_BOOKMARKS);
			return true;
		} else if (item.getItemId() == android.R.id.home) {
			mFragment.browseToHome();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	// The following methods should properly handle back button presses on every
	// API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT > VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mFragment.pressBack())
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT <= VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mFragment.pressBack())
				return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * This is called after the file manager finished.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_CODE_BOOKMARKS:
			if (resultCode == RESULT_OK && data != null) {
				//mFragment.openInformingPathBar(new FileHolder(new File(data
					//	.getStringExtra(BookmarkListActivity.KEY_RESULT_PATH)),
						//this));
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}

	}

	/**
	 * We override this, so that we get informed about the opening of the search
	 * dialog and start scanning silently.
	 */
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(FileManagerIntents.EXTRA_SEARCH_INIT_PATH,
				mFragment.getPath());
		startSearch(null, false, appData, false);

		return true;
	}
}

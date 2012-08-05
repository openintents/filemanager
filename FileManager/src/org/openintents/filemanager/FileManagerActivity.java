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
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.annotation.SuppressLint;
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

@SuppressLint("NewApi")
public class FileManagerActivity extends DistributionLibraryFragmentActivity {
	private static final String FRAGMENT_TAG = "ListFragment";
    
	protected static final int REQUEST_CODE_MOVE = 1;
	protected static final int REQUEST_CODE_COPY = 2;
    private static final int REQUEST_CODE_MULTI_SELECT = 3;
    protected static final int REQUEST_CODE_EXTRACT = 4;
    protected static final int REQUEST_CODE_BOOKMARKS = 5;
	
	private static final int COPY_BUFFER_SIZE = 32 * 1024;
	
	private SimpleFileListFragment mFragment;
	
	@Override
	protected void onNewIntent(Intent intent) {
		mFragment.open(new FileHolder(FileUtils.getFile(intent.getData()), this));
	}
	
	/**
	 * Either open the file and finish, or navigate to the designated directory. This gives FileManagerActivity the flexibility to actually handle file scheme data of any type.
	 * @return The folder to navigate to, if applicable. Null otherwise.
	 */
	private File resolveIntentData(){
		File data = FileUtils.getFile(getIntent().getData());
		if(data == null)
			return null;
		
		if(data.isFile()){
			FileUtils.openFile(new FileHolder(data, this), this);
			finish();
			return null;
		}
		else
			return FileUtils.getFile(getIntent().getData());
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mDistribution.setFirst(MENU_DISTRIBUTION_START,
				DIALOG_DISTRIBUTION_START);

		// Check whether EULA has been accepted
		// or information about new version can be presented.
		if (mDistribution.showEulaOrNewVersion()) {
			return;
		}

		// Enable home button.
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			HomeIconHelper.activity_actionbar_setHomeButtonEnabled(this);
		
		// Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if(mFragment == null){
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if(data == null)
				args.putString(FileManagerIntents.EXTRA_DIR_PATH, Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/");
			else
				args.putString(FileManagerIntents.EXTRA_DIR_PATH, data.toString());
			mFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mFragment, FRAGMENT_TAG).commit();
		}
	}

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.main, menu);
 		
 		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
 			menu.removeItem(R.id.menu_multiselect);
        }
 		
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
		switch (item.getItemId()) {
			
		case R.id.menu_search:
			onSearchRequested();
			return true;
			
		case R.id.menu_multiselect:
            promptMultiSelect();
			return true;
			
		case R.id.menu_settings:
			Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
			return true;
		
		case R.id.menu_bookmarks:
			startActivityForResult(new Intent(FileManagerActivity.this, BookmarkListActivity.class), REQUEST_CODE_BOOKMARKS);
			return true;
			
		case android.R.id.home:
			mFragment.browseToHome();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}
	
	/**
	 * Starts activity for multi select.
	 */
	private void promptMultiSelect() {
        Intent intent = new Intent(FileManagerIntents.ACTION_MULTI_SELECT);
        intent.putExtra(FileManagerIntents.EXTRA_DIR_PATH, mFragment.getPath());
        startActivity(intent);
    }
	
	
//	TODO
//	private boolean copy(File oldFile, File newFile) {
//		try {
//			FileInputStream input = new FileInputStream(oldFile);
//			FileOutputStream output = new FileOutputStream(newFile);
//		
//			byte[] buffer = new byte[COPY_BUFFER_SIZE];
//			
//			while (true) {
//				int bytes = input.read(buffer);
//				
//				if (bytes <= 0) {
//					break;
//				}
//				
//				output.write(buffer, 0, bytes);
//			}
//			
//			output.close();
//			input.close();
//			
//		} catch (Exception e) {
//		    return false;
//		}
//		return true;
//	}
	
	// The following methods should properly handle back button presses on every API Level.
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
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_CODE_MOVE:
//			if (resultCode == RESULT_OK && data != null) {
//				// obtain the filename
//				File movefrom = mContextFile;
//				File moveto = FileUtils.getFile(data.getData());
//				if (moveto != null) {
// TODO					if (getSelectedItemCount() == 1) {
//					    // Move single file.
//                        moveto = FileUtils.getFile(moveto, movefrom.getName());
//						int toast = 0;
//						if (movefrom.renameTo(moveto)) {
//							// Move was successful.
//							showDirectory(null);
//				            if (moveto.isDirectory()) {
//								toast = R.string.folder_moved;
//							} else {
//								toast = R.string.file_moved;
//							}
//						} else {
//							if (moveto.isDirectory()) {
//								toast = R.string.error_moving_folder;
//							} else {
//								toast = R.string.error_moving_file;
//							}
//						}
//						Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
//					} else {
//    					// Move multiple files.
//                        int toastMessage = 0;
//						ArrayList<File> files = (ArrayList<File>) data.getSerializableExtra("checked_files");
//                        for (File f: files) {
//					        File newPath = FileUtils.getFile(moveto, f.getName());
//                            if (!f.renameTo(newPath)) {
//                            	showDirectory(null);
//                                toastMessage = moveto.isDirectory()?R.string.error_moving_folder : R.string.error_moving_file;
//                                break;
//                            }
//					    }
//
//                        if (toastMessage == 0) {
//                            // Move was successful.
//                        	showDirectory(null);
//                            toastMessage = R.string.file_moved;
//                        }
//
//                        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
//
//                        Intent intent = getIntent();
//                        setResult(RESULT_OK, intent);
//                    }
//						
//				}				
//				
//			}
			break;
        
        case REQUEST_CODE_EXTRACT:
//            if (resultCode == RESULT_OK && data != null) {
//                new ExtractManager(this).extract(mContextFile, data.getData().getPath());
//            }
            break;

		case REQUEST_CODE_COPY:
//			if (resultCode == RESULT_OK && data != null) {
//				// obtain the filename
//				File copyfrom = mContextFile;
//				File copyto = FileUtils.getFile(data.getData());
//				if (copyto != null) {
// TODO                    if (getSelectedItemCount() == 1) {
//                        // Copy single file.
//                        copyto = createUniqueCopyName(this, copyto, copyfrom.getName());
//                        
//                        if (copyto != null) {
//                            int toast = 0;
//                            if (copy(copyfrom, copyto)) {
//                                toast = R.string.file_copied;
//                                showDirectory(null);
//                            } else {
//                                toast = R.string.error_copying_file;
//                            }
//                            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        // Copy multiple files.
//						ArrayList<File> files = (ArrayList<File>) data.getSerializableExtra("checked_files");
//                        int toastMessage = 0;
//
//                        for (File f: files) {
//                            File newPath = createUniqueCopyName(this, copyto, f.getName());
//                            if (!copy(f, newPath)) {
//                                toastMessage = R.string.error_copying_file;
//                                break;
//                            }
//                        }
//
//                        if (toastMessage == 0) {
//                            // Copy was successful.
//                            toastMessage = R.string.file_copied;
//                            showDirectory(null);
//                        }
//
//                        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
//
//                        Intent intent = getIntent();
//                        setResult(RESULT_OK, intent);
//                    }
//				}				
//			}
			break; 
        case REQUEST_CODE_BOOKMARKS:
            if (resultCode == RESULT_OK && data != null) {
            	mFragment.open(new FileHolder(new File(data.getStringExtra(BookmarkListActivity.KEY_RESULT_PATH)), this));
            }
            break;
        }
		
	}
	
	/**
	 * We override this, so that we get informed about the opening of the search dialog and start scanning silently.
	 */
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(FileManagerIntents.EXTRA_SEARCH_INIT_PATH, mFragment.getPath());
		startSearch(null, false, appData, false);
		
		return true;
	}
}

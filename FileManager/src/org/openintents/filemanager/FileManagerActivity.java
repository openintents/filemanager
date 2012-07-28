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

/*
 * Based on AndDev.org's file browser V 2.0.
 */

package org.openintents.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.bookmarks.BookmarkListActivity;
import org.openintents.filemanager.compatibility.HomeIconHelper;
import org.openintents.filemanager.files.FileHolder;
import org.openintents.filemanager.lists.SimpleFileListFragment;
import org.openintents.filemanager.util.CompressManager;
import org.openintents.filemanager.util.ExtractManager;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.filemanager.view.LegacyActionContainer;
import org.openintents.filemanager.view.PathBar;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FileManagerActivity extends DistributionLibraryFragmentActivity { 
	private static final String TAG = "FileManagerActivity";
	private static final String FRAGMENT_TAG = "ListFragment";

    private static final String DIALOG_EXISTS_ACTION_RENAME = "action_rename";
    private static final String DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP = "action_multi_compress_zip";

	private static final Character FILE_EXTENSION_SEPARATOR = '.';
	
//	TODO kept as a reference for the fragments to be made
//	private static final int STATE_BROWSE = 1;
//	private static final int STATE_PICK_FILE = 2;
//	private static final int STATE_PICK_DIRECTORY = 3;
//	private static final int STATE_MULTI_SELECT = 4;
    
	protected static final int REQUEST_CODE_MOVE = 1;
	protected static final int REQUEST_CODE_COPY = 2;
    private static final int REQUEST_CODE_MULTI_SELECT = 3;
    protected static final int REQUEST_CODE_EXTRACT = 4;
    protected static final int REQUEST_CODE_BOOKMARKS = 5;

	private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
	
// TODO remove	private static final int DIALOG_NEW_FOLDER = 1;
	private static final int DIALOG_MULTI_DELETE = 4;
	private static final int DIALOG_FILTER = 5;
    private static final int DIALOG_COMPRESSING = 8;
    private static final int DIALOG_WARNING_EXISTS = 9;
    private static final int DIALOG_CHANGE_FILE_EXTENSION = 10;
    private static final int DIALOG_MULTI_COMPRESS_ZIP = 11;

	private static final int DIALOG_DISTRIBUTION_START = 100; // MUST BE LAST

	private static final int COPY_BUFFER_SIZE = 32 * 1024;
	
	private static final String BUNDLE_CURRENT_DIRECTORY = "current_directory";
	private static final String BUNDLE_CONTEXT_FILE = "context_file";
	private static final String BUNDLE_CONTEXT_TEXT = "context_text";
	private static final String BUNDLE_SHOW_DIRECTORY_INPUT = "show_directory_input";
	private static final String BUNDLE_DIRECTORY_ENTRIES = "directory_entries";
	
//
//     /** Dir separate for sorting */
//     List<FileHolder> mListDir = new ArrayList<FileHolder>();
//     
//     /** Files separate for sorting */
//     List<FileHolder> mListFile = new ArrayList<FileHolder>();
//     
//     /** SD card separate for sorting */
//     List<FileHolder> mListSdCard = new ArrayList<FileHolder>();
     
     private MimeTypes mMimeTypes;

     private String mContextText;
     private File mContextFile = new File("");
     private Drawable mContextIcon;

     private PathBar mPathBar;
     
     /**
      * @since 2011-02-11
      */
     private LegacyActionContainer mLegacyActionContainer;


    private FileHolder[] mDirectoryEntries;

    /**
     * use it field to pass params to onCreateDialog method
     */
    private String mDialogArgument;

    /**
     * to show warning dialog to user if he want to change file extension
     */
    private String mOldFileName;
    private String mNewFileName;

    /**
     * use this field to set behaviour in DIALOG_WARNING_EXISTS
     */
    private String mDialogExistsAction = "";
// TODO
//	@Override
//	protected void onNewIntent(Intent intent) { 
//		File file = FileUtils.getFile(intent.getData());
//		if(file != null)
//			if (!file.isDirectory()) {
//				mEditFilename.setText(file.getName());
//				browseTo(file);
//			}
//			else
//				mPathBar.cd(file);
//	}
	
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
		setContentView(R.layout.browse);

		// Enable home button.
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			HomeIconHelper.activity_actionbar_setHomeButtonEnabled(this);
		
		// Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// Init members
		mMimeTypes = MimeTypes.newInstance(this);
		mPathBar = (PathBar) findViewById(R.id.pathbar);
		mLegacyActionContainer =  (LegacyActionContainer) findViewById(R.id.action_multiselect);
		mLegacyActionContainer.setFileManagerActivity(this);
		mPathBar.setInitialDirectory(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment
				.getExternalStorageDirectory().getAbsolutePath() : "/");
		
		// Add fragment only if it hasn't already been added.
		if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null){
			SimpleFileListFragment frag = new SimpleFileListFragment();
			Bundle args = new Bundle();
			args.putString(FileManagerIntents.EXTRA_DIR_PATH, mPathBar.getInitialDirectory().getAbsolutePath());
			frag.setArguments(args);
			frag.setPathBar(mPathBar);
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frag, FRAGMENT_TAG).commit();
		}
		
//		// Default state
//		mState = STATE_BROWSE;
//
//		Intent intent = getIntent();
//		String action = intent.getAction();
//
//		if (action != null) {
//			if (action.equals(FileManagerIntents.ACTION_PICK_FILE)) {
//				mState = STATE_PICK_FILE;
//				mFilterFiletype = intent.getStringExtra(FileManagerIntents.EXTRA_FILTER_FILETYPE);
//				if (mFilterFiletype == null)
//					mFilterFiletype = "";
//				mFilterMimetype = intent.getType();
//				if (mFilterMimetype == null)
//					mFilterMimetype = "";
//			} else if (action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY)) {
//				mState = STATE_PICK_DIRECTORY;
//// TODO send this to the fragment!				intent.getBooleanExtra(
////						FileManagerIntents.EXTRA_WRITEABLE_ONLY, false);
//			} else if (action.equals(FileManagerIntents.ACTION_MULTI_SELECT)) {
//				mState = STATE_MULTI_SELECT;
//
//				// Remove buttons
//				mPathBar.setVisibility(View.GONE);
//				mActionNormal.setVisibility(View.GONE);
//				mLegacyActionContainer.setVisibility(View.VISIBLE);
//				mLegacyActionContainer.setMenuResource(R.menu.multiselect);
//			}
//		}

		// Set current directory and file based on intent data.
// TODO logic
//		File file = new File(intent.getData().getPath());
//		if (file != null) {
//			File dir = FileUtils.getPathWithoutFilename(file);
//			if (dir.isDirectory()) {
//				mPathBar.setInitialDirectory(dir);
//			}
//			if (!file.isDirectory()) {
//				mEditFilename.setText(file.getName());
//			}
//		} else {
//			if (mState == STATE_PICK_FILE || mState == STATE_PICK_DIRECTORY
//					|| action.equals(Intent.ACTION_GET_CONTENT)) {
//				String path = PreferenceActivity.getDefaultPickFilePath(this);
//				if (path != null) {
//					File dir = new File(path);
//					if (dir.exists() && dir.isDirectory()) {
//						mPathBar.setInitialDirectory(dir);
//					}
//				}
//			}
//		}
//		
//		// If we've gotten here through the shortcut, override everything
//		if(intent.getStringExtra(FileManagerIntents.EXTRA_SHORTCUT_TARGET) != null) {
//			file = new File(intent.getStringExtra(FileManagerIntents.EXTRA_SHORTCUT_TARGET));
//			if (!file.isDirectory()) {
//				mEditFilename.setText(file.getName());
//				browseTo(file);
//				finish();
//			}
//			else
//				mPathBar.setInitialDirectory(file);
//		}
		
//		FAIL TODO deleteeee
//		String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
//		if (title != null) {
//			setTitle(title);
//		}
// TODO pick
//		String buttontext = intent
//				.getStringExtra(FileManagerIntents.EXTRA_BUTTON_TEXT);
//		if (buttontext != null) {
//			mButtonPick.setText(buttontext);
//		}

// TODO restore-related 
//		// Reset mRestored flag.
//		if (icicle != null) {
//			mPathBar.setInitialDirectory(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
//			mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
//			mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);
//
//			if (icicle.getBoolean(BUNDLE_SHOW_DIRECTORY_INPUT))
//				mPathBar.switchToManualInput();
//			else
//				mPathBar.switchToStandardInput();
//
//			// had to bypass direct casting as it was causing a rather unexplainable crash
//			Parcelable tmpDirectoryEntries[] = icicle
//					.getParcelableArray(BUNDLE_DIRECTORY_ENTRIES);
//			mDirectoryEntries = new FileHolder[tmpDirectoryEntries.length];
//			for (int i = 0; i < tmpDirectoryEntries.length; i++) {
//				mDirectoryEntries[i] = (FileHolder) tmpDirectoryEntries[i];
//			}
//		}
	}

 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		
 		// remember file name
 		outState.putString(BUNDLE_CURRENT_DIRECTORY, mPathBar.getCurrentDirectory().getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_FILE, mContextFile.getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_TEXT, mContextText);
 		outState.putBoolean(BUNDLE_SHOW_DIRECTORY_INPUT, mPathBar.getMode()==PathBar.Mode.MANUAL_INPUT);
 		outState.putParcelableArray(BUNDLE_DIRECTORY_ENTRIES, mDirectoryEntries);
 	}
      
	/**
	 * Browse to some location by clicking on a list item.
	 * 
	 * @param aDirectory
	 */
	private void browseTo(final File aDirectory) {
		// If we can safely browse to aDirectory cd(aDirectory) will do it, its listener will refresh the list, and we'll quickly exit this method.
		if (!mPathBar.cd(aDirectory)) {
// TODO move to frag			if (mState == STATE_BROWSE || mState == STATE_PICK_DIRECTORY) {
				// Lets start an intent to View the file that was clicked...
				openFile(aDirectory);
//			}
// TODO no, really what the heck?
//			else if (mState == STATE_PICK_FILE) {
//				// Pick the file
//				mEditFilename.setText(aDirectory.getName());
//			}
		}
	}

     private void openFile(File aFile) { 
    	 if (!aFile.exists()) {
    		 Toast.makeText(this, R.string.error_file_does_not_exists, Toast.LENGTH_SHORT).show();
    		 return;
    	 }
    	 
          Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

          Uri data = FileUtils.getUri(aFile);
          String type = mMimeTypes.getMimeType(aFile.getName());
          intent.setDataAndType(data, type);

     	 // Were we in GET_CONTENT mode?
     	 Intent originalIntent = getIntent();
     	 
     	 if (originalIntent != null && originalIntent.getAction() != null && originalIntent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
    		 // In that case, we should probably just return the requested data.
     		 PreferenceActivity.setDefaultPickFilePath(this,
     				 FileUtils.getPathWithoutFilename(aFile).getAbsolutePath());
     		 intent.setData(Uri.parse(FileManagerProvider.FILE_PROVIDER_PREFIX + aFile));
     		 setResult(RESULT_OK, intent);
     		 finish();
    		 return;
    	 }

          try {
        	  startActivity(intent); 
          } catch (ActivityNotFoundException e) {
        	  Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
          };
     } 

// moved to SimpleListFragment    /**
//      * Changes the list's contents to show the children of the passed directory.
//      * @param dir The directory that will be displayed. Pass null to refresh the currently displayed dir.
//      */
//     public void showDirectory(File dir) {
//    	 if(dir==null)
//    		 dir = mPathBar.getCurrentDirectory();
//    	 
//    	 
//    	 getFragmentManager().beginTransaction().replace(R, arg1);
//    	 
////    	 setProgressBarIndeterminateVisibility(true);
//     } 
     
//     private void selectInList(File selectFile) {
//    	 String filename = selectFile.getName();
//    	 IconifiedTextListAdapter la = (IconifiedTextListAdapter) getListAdapter();
//    	 int count = la.getCount();
//    	 for (int i = 0; i < count; i++) {
//    		 IconifiedText it = (IconifiedText) la.getItem(i);
//    		 if (it.getText().equals(filename)) {
//    			 getListView().setSelection(i);
//    			 break;
//    		 }
//    	 }
//     }

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
			mPathBar.cd(mPathBar.getInitialDirectory());
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		LayoutInflater inflater;
		View view;
		switch (id) {
		case DIALOG_MULTI_DELETE:

			return new AlertDialog.Builder(this)
					.setTitle(
							getString(R.string.really_delete_multiselect))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.ok,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// Cancel should not do anything.
								}

							}).create();

// TODO        case DIALOG_FILTER:
//			inflater = LayoutInflater.from(this);
//			view = inflater.inflate(R.layout.dialog_new_folder, null);
//			((TextView)view.findViewById(R.id.foldernametext)).setText(R.string.extension);
//			final EditText et3 = (EditText) view
//					.findViewById(R.id.foldername);
//			et3.setText("");
//			return new AlertDialog.Builder(this)
//            	.setIcon(android.R.drawable.ic_dialog_alert)
//            	.setTitle(R.string.menu_filter).setView(view).setPositiveButton(
//					android.R.string.ok, new OnClickListener() {
//						
//						public void onClick(DialogInterface dialog, int which) {
//							mFilterFiletype = et3.getText().toString().trim();
//							showDirectory(null);
//						}
//						
//					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
//						
//						public void onClick(DialogInterface dialog, int which) {
//							// Cancel should not do anything.
//						}
//						
//					}).create();

        case DIALOG_MULTI_COMPRESS_ZIP:
            inflater = LayoutInflater.from(this);
            view = inflater.inflate(R.layout.dialog_text_input, null);
            final EditText editText1 = (EditText) view.findViewById(R.id.foldername);
          //accept "return" key
			TextView.OnEditorActionListener returnListener4 = new TextView.OnEditorActionListener(){
				public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
					   if (actionId == EditorInfo.IME_NULL  
					      && event.getAction() == KeyEvent.ACTION_DOWN) { 
						   if (new File(mPathBar.getCurrentDirectory()+File.separator+editText1.getText().toString()).exists()){
                               mDialogArgument = editText1.getText().toString();
                               mDialogExistsAction = DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP;
                               showDialog(DIALOG_WARNING_EXISTS);
                           } else {
                               compressMultiFile(editText1.getText().toString(), null);
                           } //match this behavior to your OK button
						   dismissDialog(DIALOG_MULTI_COMPRESS_ZIP);
					   }
					   return true;
					}

			};
			editText1.setOnEditorActionListener(returnListener4);
			//end of code regarding "return key"
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_compress).setView(view).setPositiveButton(
                            android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (new File(mPathBar.getCurrentDirectory()+File.separator+editText1.getText().toString()).exists()){
                                mDialogArgument = editText1.getText().toString();
                                mDialogExistsAction = DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP;
                                showDialog(DIALOG_WARNING_EXISTS);
                            } else {
                                compressMultiFile(editText1.getText().toString(), null);
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel should not do anything.
                        }
                    }).create();
// deprecated spaghetti. I'll pass.
//        case DIALOG_WARNING_EXISTS:
//            return new AlertDialog.Builder(this).setTitle(getString(R.string.warning_overwrite, mDialogArgument))
//                    .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
//                            android.R.string.ok, new OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP)){
//                                compressMultiFile(mDialogArgument, null);
//                            } else if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_RENAME)){
//                                File newFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), mNewFileName);
//                                rename(FileUtils.getFile(mPathBar.getCurrentDirectory(), mOldFileName), newFile);
//                            } else {
//                                new File(mContextFile.getParent()+File.separator+mDialogArgument).delete();
//                                new CompressManager(FileManagerActivity.this).compress(mContextFile, mDialogArgument);
//                            }
//                            mDialogExistsAction = "";
//                        }
//                    }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_RENAME)){
//                                mContextText = mOldFileName;
//                                showDialog(DIALOG_RENAME);
//                            } else if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP)){
//                                showDialog(DIALOG_MULTI_COMPRESS_ZIP);
//                            } else {
//                                showDialog(DIALOG_COMPRESSING);
//                            }
//                            mDialogExistsAction = "";
//                        }
//                    }).create();
//
//            case DIALOG_CHANGE_FILE_EXTENSION:
//                return new AlertDialog.Builder(this).setTitle(getString(R.string.change_file_extension))
//                        .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
//                                android.R.string.ok, new OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                File newFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), mNewFileName);
//                                if (newFile.exists()){
//                                    mDialogExistsAction = DIALOG_EXISTS_ACTION_RENAME;
//                                    showDialog(DIALOG_WARNING_EXISTS);
//                                } else {
//                                    rename(FileUtils.getFile(mPathBar.getCurrentDirectory(), mOldFileName), newFile);
//                                }
//                            }
//                        }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                mContextText = mOldFileName;
//                                showDialog(DIALOG_RENAME);
//                            }
//                        }).create();
		}
		return super.onCreateDialog(id);
			
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		switch (id) {

		case DIALOG_MULTI_DELETE:
//	TODO        final ArrayList<File> files = getSelectedItemsFiles();
//
//			dialog.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					deleteMultiFile(files);
//					
//					Intent intent = getIntent();
//					setResult(RESULT_OK, intent);
//					dialog.dismiss();
//				}
//			});
			
            break;
            
        case DIALOG_COMPRESSING:
// TODO           TextView textView = (TextView) dialog.findViewById(R.id.foldernametext);
//            textView.setText(R.string.compress_into_archive);
//            final EditText editText = (EditText) dialog.findViewById(R.id.foldername);
//            String archiveName = "";
//            if (mContextFile.isDirectory()){
//                archiveName = mContextFile.getName()+".zip";
//            } else {
//                String extension = FileUtils.getExtension(mContextFile.getName());
//                archiveName = mContextFile.getName().replaceAll(extension, "")+".zip";
//            }
//            editText.setText(archiveName);
//            editText.setSelection(0, archiveName.length()-4);
//            break;

        case DIALOG_MULTI_COMPRESS_ZIP:
//  TODO          textView = (TextView) dialog.findViewById(R.id.foldernametext);
//            textView.setText(R.string.compress_into_archive);
//            final EditText editText1 = (EditText) dialog.findViewById(R.id.foldername);
//            archiveName = mPathBar.getCurrentDirectory().getName()+".zip";
//            editText1.setText(archiveName);
//            editText1.setSelection(0, archiveName.length()-4);
//            break;

        case DIALOG_WARNING_EXISTS:
            dialog.setTitle(getString(R.string.warning_overwrite, mDialogArgument));
        }
	}

	
	/**
	 * Starts activity for multi select.
	 */
	private void promptMultiSelect() {
        Intent intent = new Intent(FileManagerIntents.ACTION_MULTI_SELECT);
        
        intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
        
        intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.multiselect_title));
        //intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));

        startActivityForResult(intent, REQUEST_CODE_MULTI_SELECT);
    }

    /**
     * 
     * @param out The name of the produced file.
     * @param listener A listener to be notified on compression completion.
     */
    private void compressMultiFile(String out, CompressManager.OnCompressFinishedListener listener) {
//  TODO       new CompressManager(FileManagerActivity.this).setOnCompressFinishedListener(listener).compress(getSelectedItemsFiles(), out);
    }

	/*! Recursively delete a directory and all of its children.
	 *  @params toastOnError If set to true, this function will toast if an error occurs.
	 *  @returns true if successful, false otherwise.
	 */
	private boolean recursiveDelete(File file, boolean toastOnError) {
		// Recursively delete all contents.
		File[] files = file.listFiles();
		
		if (files == null) {
			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
			return false;
		}
		
		for (int x=0; x<files.length; x++) {
			File childFile = files[x];
			if (childFile.isDirectory()) {
				if (!recursiveDelete(childFile, toastOnError)) {
					return false;
				}
			} else {
				if (!childFile.delete()) {
					Toast.makeText(this, getString(R.string.error_deleting_child_file, childFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
					return false;
				}
			}
		}
		
		if (!file.delete()) {
			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

	/*@ RETURNS: A file name that is guaranteed to not exist yet.
	 * 
	 * PARAMS:
	 *   context - Application context.
	 *   path - The path that the file is supposed to be in.
	 *   fileName - Desired file name. This name will be modified to
	 *     create a unique file if necessary.
	 * 
	 */
	private File createUniqueCopyName(Context context, File path, String fileName) {
		// Does that file exist?
		File file = FileUtils.getFile(path, fileName);
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		// Split file's name and extension to fix internationalization issue #307
		int fromIndex = fileName.lastIndexOf(FILE_EXTENSION_SEPARATOR);
		String extension = "";
		if (fromIndex > 0) {
			extension = fileName.substring(fromIndex);
			fileName = fileName.substring(0, fromIndex);
		}
		
		// Try a simple "copy of".
		file = FileUtils.getFile(path, context.getString(R.string.copied_file_name, fileName).concat(extension));
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		int copyIndex = 2;
		
		// Well, we gotta find a unique name at some point.
		while (copyIndex < 500) {
			file = FileUtils.getFile(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName).concat(extension));
			
			if (!file.exists()) {
				// Nope - we can take that.
				return file;
			}

			copyIndex++;
		}
	
		// I GIVE UP.
		return null;
	}
	
	private boolean copy(File oldFile, File newFile) {
		try {
			FileInputStream input = new FileInputStream(oldFile);
			FileOutputStream output = new FileOutputStream(newFile);
		
			byte[] buffer = new byte[COPY_BUFFER_SIZE];
			
			while (true) {
				int bytes = input.read(buffer);
				
				if (bytes <= 0) {
					break;
				}
				
				output.write(buffer, 0, bytes);
			}
			
			output.close();
			input.close();
			
		} catch (Exception e) {
		    return false;
		}
		return true;
	}
	
	// The following methods should properly handle back button presses on every API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT > VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mPathBar.pressBack())
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (VERSION.SDK_INT <= VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mPathBar.pressBack())
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
			if (resultCode == RESULT_OK && data != null) {
				// obtain the filename
				File movefrom = mContextFile;
				File moveto = FileUtils.getFile(data.getData());
				if (moveto != null) {
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
						
				}				
				
			}
			break;
        
        case REQUEST_CODE_EXTRACT:
            if (resultCode == RESULT_OK && data != null) {
                new ExtractManager(this).extract(mContextFile, data.getData().getPath());
            }
            break;

		case REQUEST_CODE_COPY:
			if (resultCode == RESULT_OK && data != null) {
				// obtain the filename
				File copyfrom = mContextFile;
				File copyto = FileUtils.getFile(data.getData());
				if (copyto != null) {
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
				}				
			}
			break;

        case REQUEST_CODE_MULTI_SELECT:
            if (resultCode == RESULT_OK && data != null) {
// TODO used to refresh the list			showDirectory(null);
            }
            break;
        case REQUEST_CODE_BOOKMARKS:
            if (resultCode == RESULT_OK && data != null) {
            	browseTo(new File(data.getStringExtra(BookmarkListActivity.KEY_RESULT_PATH)));
            }
            break;
        }
		
	}

	
//	
//	/**
//	 * API level agnostic way of checking if there are selected items.
//	 * @return True if at least one item is selected, false otherwise.
//	 */
//	private boolean hasSelectedItems() {
//		return getSelectedItemCount() > 0;
//	}
//	
//	/**
//	 * API level agnostic way of getting the selected item count.
//	 * @return Count of selected items.
//	 */
//	private int getSelectedItemCount() {
//		int res = 0;
//		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
//			for (FileHolder holder : mDirectoryEntries) {
//				// TODO resolve
////				if (!holder.isSelected()) {
////					continue;
////				}
//				res++;
//			}
//			return res;
//		} else
//			return ListViewMethodHelper.listView_getCheckedItemCount(getListView());
//	}

//	/**
//	 * API level agnostic way of getting a list of the selected {@link File}s.
//	 * 
//	 * @return A list of {@link File}s, representing the selected items.
//	 */
//	private ArrayList<File> getSelectedItemsFiles() {
//		ArrayList<File> files = new ArrayList<File>();
//
//        // If we use the old scheme.
//		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
//			for (FileHolder holder : mDirectoryEntries) {
//// TODO resolve multiselection!
////				if (!holder.isSelected()) {
////					continue;
////				}
//
//				files.add(holder.getFile());
//			}
//	    // Else we use the CAB and list.
//		} else {
//			// This is actually the array of positions. Check the adapter's implementation for more info.
//			long[] ids = ListViewMethodHelper.listView_getCheckedItemIds(getListView());
//			
//			for (int i = 0; i < ids.length; i++) {
//				files.add(mDirectoryEntries[(int) ids[i]].getFile());
//			}
//		}
//		return files;
//	}
	
	/**
	 * We override this, so that we get informed about the opening of the search dialog and start scanning silently.
	 */
	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(FileManagerIntents.EXTRA_SEARCH_INIT_PATH, mPathBar.getCurrentDirectory().getAbsolutePath());
		startSearch(null, false, appData, false);
		
		return true;
	}
}

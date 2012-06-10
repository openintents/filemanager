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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openintents.distribution.DistributionLibraryListActivity;
import org.openintents.filemanager.util.CompressManager;
import org.openintents.filemanager.util.ExtractManager;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.filemanager.view.LegacyActionContainer;
import org.openintents.filemanager.view.PathBar;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v2.os.Build;
import android.support.v2.view.MenuCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FileManagerActivity extends DistributionLibraryListActivity implements OnSharedPreferenceChangeListener { 
	private static final String TAG = "FileManagerActivity";

	private static final String NOMEDIA_FILE = ".nomedia";

    private static final String DIALOG_EXISTS_ACTION_RENAME = "action_rename";
    private static final String DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP = "action_multi_compress_zip";

    /**
	 * @since 2011-03-23
	 */
	private static final Character FILE_EXTENSION_SEPARATOR = '.';
	
	private int mState;
	
	private static final int STATE_BROWSE = 1;
	private static final int STATE_PICK_FILE = 2;
	private static final int STATE_PICK_DIRECTORY = 3;
	private static final int STATE_MULTI_SELECT = 4;
    
	protected static final int REQUEST_CODE_MOVE = 1;
	protected static final int REQUEST_CODE_COPY = 2;
    protected static final int REQUEST_CODE_EXTRACT = 4;

    /**
     * @since 2011-02-11
     */
    private static final int REQUEST_CODE_MULTI_SELECT = 3;

	private static final int MENU_PREFERENCES = Menu.FIRST + 3;
	private static final int MENU_NEW_FOLDER = Menu.FIRST + 4;
	private static final int MENU_DELETE = Menu.FIRST + 5;
	private static final int MENU_RENAME = Menu.FIRST + 6;
	private static final int MENU_SEND = Menu.FIRST + 7;
	private static final int MENU_OPEN = Menu.FIRST + 8;
	private static final int MENU_MOVE = Menu.FIRST + 9;
	private static final int MENU_COPY = Menu.FIRST + 10;
	/**
     * @since 2011-09-29
     */
    private static final int MENU_MORE = Menu.FIRST + 11;
	private static final int MENU_INCLUDE_IN_MEDIA_SCAN = Menu.FIRST + 12;
	private static final int MENU_EXCLUDE_FROM_MEDIA_SCAN = Menu.FIRST + 13;
	private static final int MENU_SETTINGS = Menu.FIRST + 14;
	private static final int MENU_MULTI_SELECT = Menu.FIRST + 15;
	private static final int MENU_FILTER = Menu.FIRST + 16;
	private static final int MENU_DETAILS = Menu.FIRST + 17;
	private static final int MENU_BOOKMARKS = Menu.FIRST + 18;
	private static final int MENU_BOOKMARK = Menu.FIRST + 19;
	private static final int MENU_COMPRESS = Menu.FIRST + 20;
	private static final int MENU_EXTRACT = Menu.FIRST + 21;
	private static final int MENU_REFRESH = Menu.FIRST + 22;
	private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
	
	private static final int DIALOG_NEW_FOLDER = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_RENAME = 3;

	/**
     * @since 2011-02-12
     */
	private static final int DIALOG_MULTI_DELETE = 4;
	private static final int DIALOG_FILTER = 5;
	private static final int DIALOG_DETAILS = 6;
	
	private static final int DIALOG_BOOKMARKS = 7;
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
	
	/** Shows whether activity state has been restored (e.g. from a rotation). */
	private static boolean mRestored = false;
	
	/** Contains directories and files together */
     private ArrayList<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();

     /** Dir separate for sorting */
     List<IconifiedText> mListDir = new ArrayList<IconifiedText>();
     
     /** Files separate for sorting */
     List<IconifiedText> mListFile = new ArrayList<IconifiedText>();
     
     /** SD card separate for sorting */
     List<IconifiedText> mListSdCard = new ArrayList<IconifiedText>();
     
     // There's a ".nomedia" file here
     private boolean mNoMedia;
     
     private String mSdCardPath = "";
     
     private MimeTypes mMimeTypes;
     /** Files shown are filtered using this extension */
     private String mFilterFiletype = "";
     /** Files shown are filtered using this mimetype */
     private String mFilterMimetype = null;

     private String mContextText;
     private File mContextFile = new File("");
     private Drawable mContextIcon;
          
     private EditText mEditFilename;
     private Button mButtonPick;
          
     private boolean fileDeleted = false;
     private int positionAtDelete;
     private boolean deletedFileIsDirectory = false;

     private PathBar mPathBar;
     
     /**
      * @since 2011-02-11
      */
     private LinearLayout mActionNormal;
     private LegacyActionContainer mLegacyActionContainer;

     private TextView mEmptyText;
     private ProgressBar mProgressBar;
     
     private DirectoryScanner mDirectoryScanner;
     
     private MenuItem mExcludeMediaScanMenuItem;
     private MenuItem mIncludeMediaScanMenuItem;
     
     private Handler currentHandler;

	private boolean mWritableOnly;

    private IconifiedText[] mDirectoryEntries;

 	 static final public int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500;	// List of contents is ready, obj = DirectoryContents
     static final public int MESSAGE_SET_PROGRESS = 501;	// Set progress bar, arg1 = current value, arg2 = max value
     static final public int MESSAGE_ICON_CHANGED = 502;	// View needs to be redrawn, obj = IconifiedText

     private boolean mSelected = false;

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
     * use this filed to set behaviour in DIALOG_WARNING_EXISTS
     */
    private String mDialogExistsAction = "";

	private Drawable mIconChecked;
	private Drawable mIconUnchecked;

	private ThumbnailLoader mThumbnailLoader;

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

		currentHandler = new Handler() {
			public void handleMessage(Message msg) {
				FileManagerActivity.this.handleMessage(msg);
			}
		};

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.filelist);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		mEmptyText = (TextView) findViewById(R.id.empty_text);
		mProgressBar = (ProgressBar) findViewById(R.id.scan_progress);

		getListView().setOnCreateContextMenuListener(this);
		getListView().setEmptyView(findViewById(R.id.empty));
		getListView().setTextFilterEnabled(true);
		getListView().requestFocus();
		getListView().requestFocusFromTouch();

		mPathBar = (PathBar) findViewById(R.id.pathbar);
		mActionNormal = (LinearLayout) findViewById(R.id.action_normal);
		mLegacyActionContainer =  (LegacyActionContainer) findViewById(R.id.action_multiselect);
		mEditFilename = (EditText) findViewById(R.id.filename);

		mButtonPick = (Button) findViewById(R.id.button_pick);
		mButtonPick.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				pickFileOrDirectory();
			}
		});
		mPathBar.setOnDirectoryChangedListener(new PathBar.OnDirectoryChangedListener() {

			@Override
			public void directoryChanged(File newCurrentDir) {
				showDirectoryChildren(newCurrentDir);
			}
		});
		mLegacyActionContainer.setFileManagerActivity(this);

		// Create map of extensions:
		getMimeTypes();

		mState = STATE_BROWSE;

		Intent intent = getIntent();
		String action = intent.getAction();

		resetSdCardPath();
		if (!TextUtils.isEmpty(mSdCardPath))
			mPathBar.setInitialDirectory(mSdCardPath);
		else
			mPathBar.setInitialDirectory("/");

		// Default state
		mState = STATE_BROWSE;
		mWritableOnly = false;

		if (action != null) {

			if (action.equals(FileManagerIntents.ACTION_PICK_FILE)) {
				mState = STATE_PICK_FILE;
				mFilterFiletype = intent.getStringExtra("FILE_EXTENSION");
				if (mFilterFiletype == null)
					mFilterFiletype = "";
				mFilterMimetype = intent.getType();
				if (mFilterMimetype == null)
					mFilterMimetype = "";
			} else if (action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY)) {
				mState = STATE_PICK_DIRECTORY;
				mWritableOnly = intent.getBooleanExtra(
						FileManagerIntents.EXTRA_WRITEABLE_ONLY, false);

				// Remove edit text and make button fill whole line
				mEditFilename.setVisibility(View.GONE);
				mButtonPick.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
			} else if (action.equals(FileManagerIntents.ACTION_MULTI_SELECT)) {
				mState = STATE_MULTI_SELECT;

				// Remove buttons
				mPathBar.setVisibility(View.GONE);
				mActionNormal.setVisibility(View.GONE);
				mLegacyActionContainer.setVisibility(View.VISIBLE);
				
				mLegacyActionContainer.setMenuResource(R.menu.multiselect);
				
//
//				// Multi select action: move
//				mButtonMove = (Button) findViewById(R.id.button_move);
//				mButtonMove.setOnClickListener(new View.OnClickListener() {
//
//					public void onClick(View arg0) {
//						if (checkSelection()) {
//							promptDestinationAndMoveFile();
//						}
//					}
//				});
//
//				// Multi select action: copy
//				mButtonCopy = (Button) findViewById(R.id.button_copy);
//				mButtonCopy.setOnClickListener(new View.OnClickListener() {
//
//					public void onClick(View arg0) {
//						if (checkSelection()) {
//							promptDestinationAndCopyFile();
//						}
//					}
//				});
//
//				// Multi select action: delete
//				mButtonDelete = (Button) findViewById(R.id.button_delete);
//				mButtonDelete.setOnClickListener(new View.OnClickListener() {
//
//					public void onClick(View arg0) {
//						if (checkSelection()) {
//							showDialog(DIALOG_MULTI_DELETE);
//						}
//					}
//				});
//
//				// Multi select action: delete
//				mButtonCompress = (Button) findViewById(R.id.button_compress_zip);
//				mButtonCompress.setOnClickListener(new View.OnClickListener() {
//
//					public void onClick(View arg0) {
//						if (checkSelection()) {
//							showDialog(DIALOG_MULTI_COMPRESS_ZIP);
//						}
//					}
//				});
//
//				// Cache the checked and unchecked icons
//				mIconChecked = getResources().getDrawable(
//						R.drawable.ic_button_checked);
//				mIconUnchecked = getResources().getDrawable(
//						R.drawable.ic_button_unchecked);
//
//				mCheckIconSelect = (ImageView) findViewById(R.id.check_icon_select);
//				mCheckIconSelect.setOnClickListener(new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						mSelected = !mSelected;
//
//						if (mSelected) {
//							mCheckIconSelect.setImageDrawable(mIconChecked);
//						} else {
//							mCheckIconSelect.setImageDrawable(mIconUnchecked);
//						}
//
//						toggleSelection(mSelected);
//					}
//				});

			}

		}

		if (mState == STATE_BROWSE) {
			// Remove edit text and button.
			mEditFilename.setVisibility(View.GONE);
			mButtonPick.setVisibility(View.GONE);
		}

		if (mState != STATE_MULTI_SELECT) {
			// Remove multiselect action buttons
			mLegacyActionContainer.setVisibility(View.GONE);
		}

		// Set current directory and file based on intent data.
		File file = FileUtils.getFile(intent.getData());
		if (file != null) {
			File dir = FileUtils.getPathWithoutFilename(file);
			if (dir.isDirectory()) {
				mPathBar.setInitialDirectory(dir);
			}
			if (!file.isDirectory()) {
				mEditFilename.setText(file.getName());
			}
		} else {
			if (mState == STATE_PICK_FILE || mState == STATE_PICK_DIRECTORY
					|| action.equals(Intent.ACTION_GET_CONTENT)) {
				String path = PreferenceActivity.getDefaultPickFilePath(this);
				if (path != null) {
					File dir = new File(path);
					if (dir.exists() && dir.isDirectory()) {
						mPathBar.setInitialDirectory(dir);
					}
				}
			}
		}

		String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
		if (title != null) {
			setTitle(title);
		}

		String buttontext = intent
				.getStringExtra(FileManagerIntents.EXTRA_BUTTON_TEXT);
		if (buttontext != null) {
			mButtonPick.setText(buttontext);
		}

		// Reset mRestored flag.
		mRestored = false;
		if (icicle != null) {
			mPathBar.setInitialDirectory(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
			mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
			mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);

			if (icicle.getBoolean(BUNDLE_SHOW_DIRECTORY_INPUT))
				mPathBar.switchToManualInput();
			else
				mPathBar.switchToStandardInput();

			// had to bypass direct casting as it was causing a rather unexplainable crash
			Parcelable tmpDirectoryEntries[] = icicle
					.getParcelableArray(BUNDLE_DIRECTORY_ENTRIES);
			mDirectoryEntries = new IconifiedText[tmpDirectoryEntries.length];
			for (int i = 0; i < tmpDirectoryEntries.length; i++) {
				mDirectoryEntries[i] = (IconifiedText) tmpDirectoryEntries[i];
			}
			mRestored = true;
		}

		getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
				if (adapter != null) {
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_IDLE:
						adapter.toggleScrolling(false);
						adapter.notifyDataSetChanged();
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						adapter.toggleScrolling(true);
						break;
					case OnScrollListener.SCROLL_STATE_FLING:
						adapter.toggleScrolling(true);
						break;
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// Not used
			}
		});
	}
     
     public void onDestroy() {
    	 super.onDestroy();
    	 
    	 // Stop the scanner.
    	 DirectoryScanner scanner = mDirectoryScanner;
    	 
    	 if (scanner != null) {
    		 scanner.cancel = true;
    	 }
    	 
    	 mDirectoryScanner = null;
    	 
    	 ThumbnailLoader loader = mThumbnailLoader;
    	 
    	 if (loader != null) {
    		 loader.cancel();
    		 mThumbnailLoader = null;
    	 }
    	 
    	 ListView lv;
    	 if((lv = getListView()) != null){
    		 lv.setAdapter(null);
    	 }
     }
     
     private void handleMessage(Message message) {
//    	 Log.v(TAG, "Received message " + message.what);
    	 
    	 switch (message.what) {
    	 case MESSAGE_SHOW_DIRECTORY_CONTENTS:
    		 showDirectoryContents((DirectoryContents) message.obj);
    		 break;
    		 
    	 case MESSAGE_SET_PROGRESS:
    		 setProgress(message.arg1, message.arg2);
    		 break;
    	 }
     }
     
     private void setProgress(int progress, int maxProgress) {
    	 mProgressBar.setMax(maxProgress);
    	 mProgressBar.setProgress(progress);
    	 mProgressBar.setVisibility(View.VISIBLE);
     }
     
     private void showDirectoryContents(DirectoryContents contents) {
    	 mDirectoryScanner = null;
    	 
    	 mListSdCard = contents.listSdCard;
    	 mListDir = contents.listDir;
    	 mListFile = contents.listFile;
    	 mNoMedia = contents.noMedia;

    	 if(!mRestored){
        	 directoryEntries.ensureCapacity(mListSdCard.size() + mListDir.size() + mListFile.size());

	         addAllElements(directoryEntries, mListSdCard);
	         addAllElements(directoryEntries, mListDir);
	         addAllElements(directoryEntries, mListFile);

    		 mDirectoryEntries = directoryEntries.toArray(new IconifiedText[0]);
    	 }
    	 else {
    		 directoryEntries.clear();
    		 directoryEntries.ensureCapacity(mDirectoryEntries.length);
    		 for(int i = 0; i < mDirectoryEntries.length; i++){
    			 directoryEntries.add(mDirectoryEntries[i]);
    		 }
    		 
    		 // Once mRestore flag has been used, we should toggle it so that further refreshes don't take it into account
    		 mRestored = false;
    	 }

         IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this); 
         itla.setListItems(directoryEntries, getListView().hasTextFilter(), mPathBar.getCurrentDirectory(), mMimeTypes);          
         setListAdapter(itla); 
	     getListView().setTextFilterEnabled(true);
	     
	     if(fileDeleted){
	    	 getListView().setSelection(positionAtDelete);
	     }

         setProgressBarIndeterminateVisibility(false);

    	 mProgressBar.setVisibility(View.GONE);
    	 mEmptyText.setVisibility(View.VISIBLE);
    	 
    	 toggleCheckBoxVisibility(mState == STATE_MULTI_SELECT);
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

	private void pickFileOrDirectory() {
		File file = null;
		if (mState == STATE_PICK_FILE) {
			String filename = mEditFilename.getText().toString();
			file = FileUtils.getFile(mPathBar.getCurrentDirectory().getAbsolutePath(), filename);
		} else if (mState == STATE_PICK_DIRECTORY) {
			file = mPathBar.getCurrentDirectory();
		}
		
		PreferenceActivity.setDefaultPickFilePath(this, mPathBar.getCurrentDirectory().getAbsolutePath());
    	 
    	Intent intent = getIntent();
    	intent.setData(FileUtils.getUri(file));
    	setResult(RESULT_OK, intent);
    	finish();
     }
     
	/**
	 * 
	 */
     private void getMimeTypes() {
    	 MimeTypeParser mtp = null;
		try {
			mtp = new MimeTypeParser(this, this.getPackageName());
		} catch (NameNotFoundException e) {
			//Should never happen
		}

    	 XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

    	 try {
    		 mMimeTypes = mtp.fromXmlResource(in);
    	 } catch (XmlPullParserException e) {
    		 Log
    		 .e(
    				 TAG,
    				 "PreselectedChannelsActivity: XmlPullParserException",
    				 e);
    		 throw new RuntimeException(
    		 "PreselectedChannelsActivity: XmlPullParserException");
    	 } catch (IOException e) {
    		 Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
    		 throw new RuntimeException(
    		 "PreselectedChannelsActivity: IOException");
    	 }
     } 
      
	/**
	 * Browse to some location by clicking on a list item.
	 * 
	 * @param aDirectory
	 */
	private void browseTo(final File aDirectory) {
		// If we can safely browse to aDirectory cd(aDirectory) will do it, its listener will refresh the list, and we'll quickly exit this method.
		if (!mPathBar.cd(aDirectory)) {
			if (mState == STATE_BROWSE || mState == STATE_PICK_DIRECTORY) {
				// Lets start an intent to View the file that was clicked...
				openFile(aDirectory);
			} else if (mState == STATE_PICK_FILE) {
				// Pick the file
				mEditFilename.setText(aDirectory.getName());
			}
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

     /**
      * Changes the list's contents to show the children of the passed directory.
      * @param dir The directory that will be displayed. Pass null to refresh the currently displayed dir.
      */
     public void showDirectoryChildren(File dir) {
    	 if(dir==null)
    		 dir = mPathBar.getCurrentDirectory();
    	  	 
    	 boolean directoriesOnly = mState == STATE_PICK_DIRECTORY;
    	 
    	  // Cancel an existing scanner, if applicable.
    	  DirectoryScanner scanner = mDirectoryScanner;
    	  
    	  if (scanner != null) {
    		  scanner.cancel = true;
    	  }
    	  
    	  ThumbnailLoader loader = mThumbnailLoader;
    	  
    	  if (loader != null) {
    		  loader.cancel();
    		  mThumbnailLoader = null;
    	  }
    	  
    	  directoryEntries.clear(); 
          mListDir.clear();
          mListFile.clear();
          mListSdCard.clear();
          
          setProgressBarIndeterminateVisibility(true);
          
          // Don't show the "folder empty" text since we're scanning.
          mEmptyText.setVisibility(View.GONE);
          
          // Also DON'T show the progress bar - it's kind of lame to show that
          // for less than a second.
          mProgressBar.setVisibility(View.GONE);
          setListAdapter(null); 
          
		  mDirectoryScanner = new DirectoryScanner(dir, this, currentHandler, mMimeTypes, mFilterFiletype, mFilterMimetype, mSdCardPath, mWritableOnly, directoriesOnly);
		  mDirectoryScanner.start();
		  
		  
           
          // Add the "." == "current directory" 
          /*directoryEntries.add(new IconifiedText( 
                    getString(R.string.current_dir), 
                    getResources().getDrawable(R.drawable.ic_launcher_folder)));        */
          // and the ".." == 'Up one level' 
          /*
          if(currentDirectory.getParent() != null) 
               directoryEntries.add(new IconifiedText( 
                         getString(R.string.up_one_level), 
                         getResources().getDrawable(R.drawable.ic_launcher_folder_open))); 
          */
     } 
     
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
     
     private void addAllElements(List<IconifiedText> addTo, List<IconifiedText> addFrom) {
    	 int size = addFrom.size();
    	 for (int i = 0; i < size; i++) {
    		 addTo.add(addFrom.get(i));
    	 }
     }
     
     @Override 
     protected void onListItemClick(ListView l, View v, int position, long id) { 
          super.onListItemClick(l, v, position, id); 
          
          IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
          
          if (adapter == null) {
        	  return;
          }
          
          IconifiedText text = (IconifiedText) adapter.getItem(position);

          if (mState == STATE_MULTI_SELECT) {
        	  text.setSelected(!text.isSelected());
        	  adapter.notifyDataSetChanged();
        	  return;
          }

		String file = text.getText();
		String curdir = mPathBar.getCurrentDirectory().getAbsolutePath();
		File clickedFile = FileUtils.getFile(curdir, file);
		if (clickedFile != null) {
			browseTo(clickedFile);
		}
	}

     
     /**
      * Renews the value of {@link #mSdCardPath}.
      */
     private void resetSdCardPath() {
    	 mSdCardPath = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath();
     }
     

 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);

 		int icon = android.R.drawable.ic_menu_add;
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			icon = R.drawable.ic_menu_add_folder;
 		}
 		MenuItem item = menu.add(0, MENU_NEW_FOLDER, 0, R.string.menu_new_folder).setIcon(
 				icon).setShortcut('0', 'f');
 		MenuCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 		if (mState == STATE_BROWSE) {
 		// Multi select option menu.
 	        menu.add(0, MENU_MULTI_SELECT, 0, R.string.menu_multi_select).setIcon(
 	                R.drawable.ic_menu_multiselect).setShortcut('1', 'm');
        }
			
		mIncludeMediaScanMenuItem = menu.add(0, MENU_INCLUDE_IN_MEDIA_SCAN, 0, R.string.menu_include_in_media_scan).setShortcut('2', 's')
				.setIcon(android.R.drawable.ic_menu_gallery);
		mExcludeMediaScanMenuItem = menu.add(0, MENU_EXCLUDE_FROM_MEDIA_SCAN, 0, R.string.menu_exclude_from_media_scan).setShortcut('2', 's')
				.setIcon(android.R.drawable.ic_menu_gallery);
		
		menu.add(0, MENU_BOOKMARKS, 0, R.string.bookmarks).setIcon(
				R.drawable.ic_menu_star);
		

		menu.add(0, MENU_SETTINGS, 0, R.string.settings).setIcon(
				android.R.drawable.ic_menu_preferences).setShortcut('9', 'p');
		
		/* We don't want to allow the user to override a filter set
		 * by an application.
		 */
		if(mState != STATE_PICK_FILE) {
			menu.add(0, MENU_FILTER, 0, R.string.menu_filter).setIcon(
					android.R.drawable.ic_menu_search);
		}
		
		menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh).setIcon(
				android.R.drawable.ic_menu_rotate);

 		mDistribution.onCreateOptionsMenu(menu);
 		return true;
 	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		mIncludeMediaScanMenuItem.setVisible(false);
		mExcludeMediaScanMenuItem.setVisible(false);
		
		boolean showMediaScanMenuItem = PreferenceActivity.getMediaScanFromPreference(this);
		
 		// We only know about ".nomedia" once we have the results list back.
 		if (showMediaScanMenuItem && mListDir != null) {
			if (mNoMedia) {
				mIncludeMediaScanMenuItem.setVisible(true);
			} else {
				mExcludeMediaScanMenuItem.setVisible(true);
 			}
 		}

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
				new ComponentName(this, FileManagerActivity.class), null, intent,
				0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_NEW_FOLDER:
			showDialog(DIALOG_NEW_FOLDER);
			return true;
			
		case MENU_MULTI_SELECT:
            promptMultiSelect();
			return true;
			
		case MENU_INCLUDE_IN_MEDIA_SCAN:
			includeInMediaScan();
			return true;

		case MENU_EXCLUDE_FROM_MEDIA_SCAN:
			excludeFromMediaScan();
			return true;
			
		case MENU_SETTINGS:
			showSettings();
			return true;
			
		case MENU_FILTER:
			showDialog(DIALOG_FILTER);
			return true;
		
		case MENU_BOOKMARKS:
			showDialog(DIALOG_BOOKMARKS);
			return true;
			
		case MENU_REFRESH:
			showDirectoryChildren(null);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

    private void showSettings() {
		Intent intent = new Intent(this, PreferenceActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
/*
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
*/
        IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
        
        if (adapter == null) {
      	  return;
        }
        
        IconifiedText it = (IconifiedText) adapter.getItem(info.position);
		menu.setHeaderTitle(it.getText());
		menu.setHeaderIcon(it.getIcon());
		File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), it.getText());

		
		if (!file.isDirectory()) {
			if (mState == STATE_PICK_FILE) {
				// Show "open" menu
				menu.add(0, MENU_OPEN, 0, R.string.menu_open);
			}
			menu.add(0, MENU_SEND, 0, R.string.menu_send);
		}
		menu.add(0, MENU_MOVE, 0, R.string.menu_move);
		
		if (!file.isDirectory()) {
			menu.add(0, MENU_COPY, 0, R.string.menu_copy);
		}
		
		menu.add(0, MENU_RENAME, 0, R.string.menu_rename);
		menu.add(0, MENU_DELETE, 0, R.string.menu_delete);

		//if (!file.isDirectory()) {
	        Uri data = Uri.fromFile(file);
	        Intent intent = new Intent(null, data);
	        String type = mMimeTypes.getMimeType(file.getName());
	
	        intent.setDataAndType(data, type);
	        intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
	        //intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
	
	        Log.v(TAG, "Data=" + data);
	        Log.v(TAG, "Type=" + type);
			
	        if (type != null) {
	        	// Add additional options for the MIME type of the selected file.
				menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
						new ComponentName(this, FileManagerActivity.class), null, intent, 0, null);
	        }
		//}

        if (FileUtils.checkIfZipArchive(file)){
            menu.add(0, MENU_EXTRACT, 0, R.string.menu_extract);
        } else {
            menu.add(0, MENU_COMPRESS, 0, R.string.menu_compress);
        }
	    menu.add(0, MENU_DETAILS, 0, R.string.menu_details);
	    menu.add(0, MENU_BOOKMARK, 0, R.string.menu_bookmark);
        menu.add(0, MENU_MORE, 0, R.string.menu_more);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		
		// Remember current selection
        IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
        
        if (adapter == null) {
      	  return false;
        }
        
        IconifiedText ic = (IconifiedText) adapter.getItem(menuInfo.position);
		mContextText = ic.getText();
		mContextIcon = ic.getIcon();
		mContextFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), ic.getText());
		
		switch (item.getItemId()) {
		case MENU_OPEN:
            openFile(mContextFile); 
			return true;
			
		case MENU_MOVE:
			promptDestinationAndMoveFile();
			return true;
			
		case MENU_COPY:
			promptDestinationAndCopyFile();
			return true;
			
		case MENU_DELETE:
			showDialog(DIALOG_DELETE);
			return true;

		case MENU_RENAME:
			showDialog(DIALOG_RENAME);
			return true;
			
		case MENU_SEND:
			sendFile(mContextFile);
			return true;
		
		case MENU_DETAILS:
			showDialog(DIALOG_DETAILS);
			return true;

        case MENU_COMPRESS:
            showDialog(DIALOG_COMPRESSING);
            return true;

        case MENU_EXTRACT:
            promptDestinationAndExtract();            
            return true;
			
		case MENU_BOOKMARK:
			String path = mContextFile.getAbsolutePath();
			Cursor query = managedQuery(BookmarksProvider.CONTENT_URI,
										new String[]{BookmarksProvider._ID},
										BookmarksProvider.PATH + "=?",
										new String[]{path},
										null);
			if(!query.moveToFirst()){
				ContentValues values = new ContentValues();
				values.put(BookmarksProvider.NAME, mContextFile.getName());
				values.put(BookmarksProvider.PATH, path);
				getContentResolver().insert(BookmarksProvider.CONTENT_URI, values);
				Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(this, R.string.bookmark_already_exists, Toast.LENGTH_SHORT).show();
			}
			return true;

		case MENU_MORE:
			if (!PreferenceActivity.getShowAllWarning(FileManagerActivity.this)) {
				showMoreCommandsDialog();
				return true;
			}

			showWarningDialog();

			return true;
		}

		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_NEW_FOLDER:
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.dialog_new_folder, null);
			final EditText et = (EditText) view
					.findViewById(R.id.foldername);
			et.setText("");
			//accept "return" key
			TextView.OnEditorActionListener returnListener = new TextView.OnEditorActionListener(){
				public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
					   if (actionId == EditorInfo.IME_NULL  
					      && event.getAction() == KeyEvent.ACTION_DOWN) { 
						   createNewFolder(et.getText().toString()); //match this behavior to your OK button
						   dismissDialog(DIALOG_NEW_FOLDER);
					   }
					   return true;
					}

			};
			et.setOnEditorActionListener(returnListener);
			//end of code regarding "return key"

			return new AlertDialog.Builder(this)
            	.setIcon(android.R.drawable.ic_dialog_alert)
            	.setTitle(R.string.create_new_folder).setView(view).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							createNewFolder(et.getText().toString());
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();
		

		case DIALOG_DELETE:
			return new AlertDialog.Builder(this).setTitle(getString(R.string.really_delete, mContextText))
            	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							deleteFileOrFolder(mContextFile);
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();

		case DIALOG_RENAME:
			inflater = LayoutInflater.from(this);
			view = inflater.inflate(R.layout.dialog_new_folder, null);
			final EditText et2 = (EditText) view
				.findViewById(R.id.foldername);
			//accept "return" key
			TextView.OnEditorActionListener returnListener2 = new TextView.OnEditorActionListener(){
				public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
					   if (actionId == EditorInfo.IME_NULL  
					      && event.getAction() == KeyEvent.ACTION_DOWN) { 
						   renameFileOrFolder(mContextFile, et2.getText().toString()); //match this behavior to your OK button
						   dismissDialog(DIALOG_RENAME);
					   }
					   return true;
					}

			};
			et2.setOnEditorActionListener(returnListener2);
			//end of code regarding "return key"
			return new AlertDialog.Builder(this)
            	.setTitle(R.string.menu_rename).setView(view).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							
							renameFileOrFolder(mContextFile, et2.getText().toString());
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();

        case DIALOG_MULTI_DELETE:
            String contentText = null;
            int count = 0;
            for (IconifiedText it : mDirectoryEntries) {
                if (!it.isSelected()) {
                    continue;
                }

                contentText = it.getText();
                count++;
            }
            String string;
            if (count == 1) {
                 string = getString(R.string.really_delete, contentText);
            } else {
                string = getString(R.string.really_delete_multiselect, count);
            }
            return new AlertDialog.Builder(this).setTitle(string)
                .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                    android.R.string.ok, new OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMultiFile();
    
                            Intent intent = getIntent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                        
                    }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel should not do anything.
                        }
                    
                    }).create();

        case DIALOG_FILTER:
			inflater = LayoutInflater.from(this);
			view = inflater.inflate(R.layout.dialog_new_folder, null);
			((TextView)view.findViewById(R.id.foldernametext)).setText(R.string.extension);
			final EditText et3 = (EditText) view
					.findViewById(R.id.foldername);
			et3.setText("");
			return new AlertDialog.Builder(this)
            	.setIcon(android.R.drawable.ic_dialog_alert)
            	.setTitle(R.string.menu_filter).setView(view).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							mFilterFiletype = et3.getText().toString().trim();
							showDirectoryChildren(null);
						}
						
					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// Cancel should not do anything.
						}
						
					}).create();
			

        case DIALOG_DETAILS:
        	inflater = LayoutInflater.from(this);
        	view =  inflater.inflate(R.layout.dialog_details, null);
        	        	
        	return new AlertDialog.Builder(this).setTitle(mContextText).
        			setIcon(mContextIcon).setView(view).create();
        	
        case DIALOG_BOOKMARKS:
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        	final Cursor bookmarksCursor = getBookmarks();
        	
        	builder.setTitle(R.string.bookmarks);
        	
        	builder.setCursor(bookmarksCursor, new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int item) {
	        	    	if (bookmarksCursor.moveToPosition(item)) {
	        	    		String path = bookmarksCursor.getString(
		        	    			bookmarksCursor.getColumnIndex(BookmarksProvider.PATH));
		        	    	File file = new File(path);
		        	    	if (file != null) {
			        	    	browseTo(file);
		        	    	}
	        	    	} else{
	        	    		Toast.makeText(FileManagerActivity.this, R.string.bookmark_not_found,
	        	    				Toast.LENGTH_SHORT).show();
	        	    	}
	        	    }
	        	}, BookmarksProvider.NAME);
        	
        	return builder.create();

        case DIALOG_COMPRESSING:
            inflater = LayoutInflater.from(this);
            view = inflater.inflate(R.layout.dialog_new_folder, null);
            final EditText editText = (EditText) view.findViewById(R.id.foldername);
          //accept "return" key
			TextView.OnEditorActionListener returnListener3 = new TextView.OnEditorActionListener(){
				public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
					   if (actionId == EditorInfo.IME_NULL  
					      && event.getAction() == KeyEvent.ACTION_DOWN) { 
						   if (new File(mContextFile.getParent()+File.separator+editText.getText().toString()).exists()){
                               mDialogArgument = editText.getText().toString();
                               showDialog(DIALOG_WARNING_EXISTS);
                           } else {
                               new CompressManager(FileManagerActivity.this).compress(mContextFile, editText.getText().toString());
                           } //match this behavior to your OK button
						   dismissDialog(DIALOG_COMPRESSING);
					   }
					   return true;
					}

			};
			editText.setOnEditorActionListener(returnListener3);
			//end of code regarding "return key"
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_compress).setView(view).setPositiveButton(
                            android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (new File(mContextFile.getParent()+File.separator+editText.getText().toString()).exists()){
                                mDialogArgument = editText.getText().toString();
                                showDialog(DIALOG_WARNING_EXISTS);
                            } else {
                                new CompressManager(FileManagerActivity.this).compress(mContextFile, editText.getText().toString());
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel should not do anything.
                        }
                    }).create();

        case DIALOG_MULTI_COMPRESS_ZIP:
            inflater = LayoutInflater.from(this);
            view = inflater.inflate(R.layout.dialog_new_folder, null);
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
        
        case DIALOG_WARNING_EXISTS:
            return new AlertDialog.Builder(this).setTitle(getString(R.string.warning_overwrite, mDialogArgument))
                    .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                            android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP)){
                                compressMultiFile(mDialogArgument, null);
                            } else if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_RENAME)){
                                File newFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), mNewFileName);
                                rename(FileUtils.getFile(mPathBar.getCurrentDirectory(), mOldFileName), newFile);
                            } else {
                                new File(mContextFile.getParent()+File.separator+mDialogArgument).delete();
                                new CompressManager(FileManagerActivity.this).compress(mContextFile, mDialogArgument);
                            }
                            mDialogExistsAction = "";
                        }
                    }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_RENAME)){
                                mContextText = mOldFileName;
                                showDialog(DIALOG_RENAME);
                            } else if (mDialogExistsAction.equals(DIALOG_EXISTS_ACTION_MULTI_COMPRESS_ZIP)){
                                showDialog(DIALOG_MULTI_COMPRESS_ZIP);
                            } else {
                                showDialog(DIALOG_COMPRESSING);
                            }
                            mDialogExistsAction = "";
                        }
                    }).create();

            case DIALOG_CHANGE_FILE_EXTENSION:
                return new AlertDialog.Builder(this).setTitle(getString(R.string.change_file_extension))
                        .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                                android.R.string.ok, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File newFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), mNewFileName);
                                if (newFile.exists()){
                                    mDialogExistsAction = DIALOG_EXISTS_ACTION_RENAME;
                                    showDialog(DIALOG_WARNING_EXISTS);
                                } else {
                                    rename(FileUtils.getFile(mPathBar.getCurrentDirectory(), mOldFileName), newFile);
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mContextText = mOldFileName;
                                showDialog(DIALOG_RENAME);
                            }
                        }).create();
		}
		return super.onCreateDialog(id);
			
	}
	
	private Cursor getBookmarks(){
		return managedQuery(BookmarksProvider.CONTENT_URI,
					new String[] {
						BookmarksProvider._ID,
						BookmarksProvider.NAME,
						BookmarksProvider.PATH,
					}, null, null, null);
	}


	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		switch (id) {
		case DIALOG_NEW_FOLDER:
			EditText et = (EditText) dialog.findViewById(R.id.foldername);
			et.setText("");
			break;

		case DIALOG_DELETE:
			((AlertDialog) dialog).setTitle(getString(R.string.really_delete, mContextText));
			break;
			
		case DIALOG_RENAME:
			et = (EditText) dialog.findViewById(R.id.foldername);
			et.setText(mContextText);
			TextView tv = (TextView) dialog.findViewById(R.id.foldernametext);
			if (mContextFile.isDirectory()) {
				tv.setText(R.string.file_name);
			} else {
				tv.setText(R.string.file_name);
			}
            et.setSelection(0, mContextText.lastIndexOf(".") == -1 ? mContextText.length() : mContextText.lastIndexOf("."));
			((AlertDialog) dialog).setIcon(mContextIcon);
			break;

		case DIALOG_MULTI_DELETE:
            break;
            
		case DIALOG_DETAILS:
			final TextView type = ((TextView)dialog.findViewById(R.id.details_type_value));
        	type.setText((mContextFile.isDirectory() ? R.string.details_type_folder :
        				(mContextFile.isFile() ? R.string.details_type_file :
        					R.string.details_type_other)));
        	
        	final TextView size = ((TextView)dialog.findViewById(R.id.details_size_value));
        	size.setText(FileUtils.formatSize(this, mContextFile.length()));
        	
        	// Creates a background thread that obtains the size of a directory and updates
        	// the TextView accordingly.
        	if(mContextFile.isDirectory()){
        		final AsyncTask<File, Long, Long> folderSizeTask = new AsyncTask<File, Long, Long>(){
        			
        			protected long totalSize = 0L;
        			
    				@Override
    				protected Long doInBackground(File... file) {
    					sizeOf(file[0]);
    					return totalSize;
    				}
            		
    				@Override
    				protected void onProgressUpdate(Long... updatedSize){
    					size.setText(FileUtils.formatSize(size.getContext(), updatedSize[0]));
    				}
    				
    				@Override
    				protected void onPostExecute(Long result){
    					size.setText(FileUtils.formatSize(size.getContext(), result));
    				}
    				
    				private void sizeOf(File file){
    					if(file.isFile()){
    						totalSize += file.length();
    						publishProgress(totalSize);
    					} else {
    						File[] files = file.listFiles();
    						
    						if(files != null && files.length != 0){
        						for(File subFile : files){
        							sizeOf(subFile);
        						}
    						}
    					}
    				}
            	}.execute(mContextFile);
            	
            	((AlertDialog) dialog).setOnCancelListener(new OnCancelListener(){
    				@Override
    				public void onCancel(DialogInterface dialog) {
    					folderSizeTask.cancel(true);
    				}
            	});
        	}
        	
        	String perms = (mContextFile.canRead() ? "R" : "-") +
        			(mContextFile.canWrite() ? "W" : "-") +
        			(FileUtils.canExecute(mContextFile) ? "X" : "-");
        	
        	final TextView permissions = ((TextView)dialog.findViewById(R.id.details_permissions_value));
        	permissions.setText(perms);
        	
        	final TextView hidden = ((TextView)dialog.findViewById(R.id.details_hidden_value));
        	hidden.setText(mContextFile.isHidden() ? R.string.details_yes : R.string.details_no);
        	
        	final TextView lastmodified = ((TextView)dialog.findViewById(R.id.details_lastmodified_value));
        	lastmodified.setText(FileUtils.formatDate(this, mContextFile.lastModified()));
        	((AlertDialog) dialog).setIcon(mContextIcon);
        	((AlertDialog) dialog).setTitle(mContextText);
			break;

        case DIALOG_COMPRESSING:
            TextView textView = (TextView) dialog.findViewById(R.id.foldernametext);
            textView.setText(R.string.compress_into_archive);
            final EditText editText = (EditText) dialog.findViewById(R.id.foldername);
            String archiveName = "";
            if (mContextFile.isDirectory()){
                archiveName = mContextFile.getName()+".zip";
            } else {
                String extension = FileUtils.getExtension(mContextFile.getName());
                archiveName = mContextFile.getName().replaceAll(extension, "")+".zip";
            }
            editText.setText(archiveName);
            editText.setSelection(0, archiveName.length()-4);
            break;

        case DIALOG_MULTI_COMPRESS_ZIP:
            textView = (TextView) dialog.findViewById(R.id.foldernametext);
            textView.setText(R.string.compress_into_archive);
            final EditText editText1 = (EditText) dialog.findViewById(R.id.foldername);
            archiveName = mPathBar.getCurrentDirectory().getName()+".zip";
            editText1.setText(archiveName);
            editText1.setSelection(0, archiveName.length()-4);
            break;

        case DIALOG_WARNING_EXISTS:
            dialog.setTitle(getString(R.string.warning_overwrite, mDialogArgument));
        }
	}
	
	/**
	 * @since 2011-09-30
	 */
	private void showWarningDialog() {
		LayoutInflater li = LayoutInflater.from(this);
		View warningView = li.inflate(R.layout.dialog_warning, null);
		final CheckBox showWarningAgain = (CheckBox)warningView.findViewById(R.id.showagaincheckbox);
		
		showWarningAgain.setChecked(PreferenceActivity.getShowAllWarning(FileManagerActivity.this));
		
		new AlertDialog.Builder(this).setView(warningView).setTitle(getString(R.string.title_warning_some_may_not_work))
				.setMessage(getString(R.string.warning_some_may_not_work, mContextText))
		    	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							PreferenceActivity.setShowAllWarning(FileManagerActivity.this, showWarningAgain.isChecked());

							showMoreCommandsDialog();
						}
						
					}).create()
				.show();
	}

	/**
	 * @since 2011-09-30
	 */
	private void showMoreCommandsDialog() {
		final Uri data = Uri.fromFile(mContextFile);
		final Intent intent = new Intent(null, data);
		String type = mMimeTypes.getMimeType(mContextFile.getName());

		intent.setDataAndType(data, type);

		Log.v(TAG, "Data=" + data);
		Log.v(TAG, "Type=" + type);

		if (type != null) {
			// Add additional options for the MIME type of the selected file.
			PackageManager pm = getPackageManager();
			final List<ResolveInfo> lri = pm.queryIntentActivityOptions(
					new ComponentName(this, FileManagerActivity.class),
					null, intent, 0);
			final int N = lri != null ? lri.size() : 0;

			// Create name list for menu item.
			final List<CharSequence> items = new ArrayList<CharSequence>();
			/* Some of the options don't go to the list hence we have to remove them
			 * to keep the lri correspond with the menu items. In the addition, we have
			 * to remove them after the first iteration, otherwise the iteration breaks.
			 */
			List<ResolveInfo> toRemove = new ArrayList<ResolveInfo>();
			for (int i = 0; i < N; i++) {
				final ResolveInfo ri = lri.get(i);
				Intent rintent = new Intent(intent);
				rintent.setComponent(
						new ComponentName(
								ri.activityInfo.applicationInfo.packageName,
								ri.activityInfo.name));
				ActivityInfo info = rintent.resolveActivityInfo(pm, 0);
				String permission = info.permission;
				if(info.exported && (permission == null 
						|| checkCallingPermission(permission) == PackageManager.PERMISSION_GRANTED))
					items.add(ri.loadLabel(pm));
				else
					toRemove.add(ri);
			}

			for(ResolveInfo ri : toRemove){
				lri.remove(ri);
			}

			new AlertDialog.Builder(this)
					.setTitle(mContextText)
					.setIcon(mContextIcon)
					.setItems(items.toArray(new CharSequence[0]),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int item) {
									final ResolveInfo ri = lri.get(item);
									Intent rintent = new Intent(intent)
											.setComponent(new ComponentName(
													ri.activityInfo.applicationInfo.packageName,
													ri.activityInfo.name));
									startActivity(rintent);
								}
							}).create()
						.show();
		}
	}

	private void includeInMediaScan() {
		// Delete the .nomedia file.
		File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), NOMEDIA_FILE);
		if (file.delete()) {
			Toast.makeText(this, getString(R.string.media_scan_included), Toast.LENGTH_LONG).show();
			mNoMedia = false;
		} else {
			// That didn't work.
			Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
		}
	}

	private void excludeFromMediaScan() {
		// Create the .nomedia file.
		File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), NOMEDIA_FILE);
		try {
			if (file.createNewFile()) {
				mNoMedia = true;
				Toast.makeText(this, getString(R.string.media_scan_excluded), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, getString(R.string.error_media_scan), Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			// That didn't work.
			Toast.makeText(this, getString(R.string.error_generic) + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean checkSelection() {
        for (IconifiedText it : mDirectoryEntries) {
            if (!it.isSelected()) {
                continue;
            }

            return true;
        }

        Toast.makeText(this, R.string.error_selection, Toast.LENGTH_SHORT).show();

        return false;
   }

   private void toggleSelection(boolean selected) {
	   for(IconifiedText it : mDirectoryEntries){
		   it.setSelected(selected);
	   }
	   
	   ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
   }

	private void toggleCheckBoxVisibility(boolean visible) {
		for(IconifiedText it : mDirectoryEntries){
			it.setCheckIconVisible(visible);
		}
		
		((BaseAdapter) getListAdapter()).notifyDataSetChanged();
	}

   private void promptDestinationAndMoveFile() {

		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

		intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));

		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.move_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));
		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);

		startActivityForResult(intent, REQUEST_CODE_MOVE);
	}

    private void promptDestinationAndExtract() {
        Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
        intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
        intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.extract_title));
        intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.extract_button));
        intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
        startActivityForResult(intent, REQUEST_CODE_EXTRACT);
    }
	
	private void promptDestinationAndCopyFile() {
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
		
		intent.setData(FileUtils.getUri(mPathBar.getCurrentDirectory()));
		
		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.copy_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.copy_button));
		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
		
		startActivityForResult(intent, REQUEST_CODE_COPY);
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

    private void createNewFolder(String foldername) {
		if (!TextUtils.isEmpty(foldername)) {
			File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), foldername);
			if (file.mkdirs()) {
				
				// Change into new directory:
				browseTo(file);
			} else {
				Toast.makeText(this, R.string.error_creating_new_folder, Toast.LENGTH_SHORT).show();
			}
		}
	}

    /**
     * 
     * @param out The name of the produced file.
     * @param listener A listener to be notified on compression completion.
     */
    private void compressMultiFile(String out, CompressManager.OnCompressFinishedListener listener) {
        List<File> files = new ArrayList<File>();
        for (IconifiedText it : mDirectoryEntries) {
            if (!it.isSelected()) {
                continue;
            }

            File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), it.getText());
            files.add(file);
        }
        new CompressManager(FileManagerActivity.this).setOnCompressFinishedListener(listener).compress(files, out);
    }

	/*! Recursively delete a directory and all of its children.
	 *  @params toastOnError If set to true, this function will toast if an error occurs.
	 *  @returns true if successful, false otherwise.
	 */
//	private boolean recursiveDelete(File file, boolean toastOnError) {
//		// Recursively delete all contents.
//		File[] files = file.listFiles();
//		
//		if (files == null) {
//			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG);
//			return false;
//		}
//		
//		for (int x=0; x<files.length; x++) {
//			File childFile = files[x];
//			if (childFile.isDirectory()) {
//				if (!recursiveDelete(childFile, toastOnError)) {
//					return false;
//				}
//			} else {
//				if (!childFile.delete()) {
//					Toast.makeText(this, getString(R.string.error_deleting_child_file, childFile.getAbsolutePath()), Toast.LENGTH_LONG);
//					return false;
//				}
//			}
//		}
//		
//		if (!file.delete()) {
//			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG);
//			return false;
//		}
//		
//		return true;
//	}
	
	private class RecursiveDeleteTask extends AsyncTask<Object, Void, Integer> {

		private FileManagerActivity activity = FileManagerActivity.this;
		private static final int success = 0;
		private static final int err_deleting_folder = 1;
		private static final int err_deleting_child_file = 2;
		private static final int err_deleting_file = 3;

		private File errorFile;

		/**
		 * Recursively delete a file or directory and all of its children.
		 * 
		 * @returns 0 if successful, error value otherwise.
		 */
		private int recursiveDelete(File file) {
			if (file.isDirectory() && file.listFiles() != null)
				for (File childFile : file.listFiles()) {
					if (childFile.isDirectory()) {
						int result = recursiveDelete(childFile);
						if (result > 0) {
							return result;
						}
					} else {
						if (!childFile.delete()) {
							errorFile = childFile;
							return err_deleting_child_file;
						}
					}
				}

			if (!file.delete()) {
				errorFile = file;
				return file.isFile() ? err_deleting_file : err_deleting_folder;
			}

			return success;
		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(activity, R.string.deleting_files, Toast.LENGTH_SHORT).show();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Integer doInBackground(Object... params) {
			Object files = params[0];
			
			if (files instanceof List<?>) {
				for (File file: (List<File>)files) {
					int result = recursiveDelete(file);
					if (result != success) return result;
				}
				return success;
			} else
				return recursiveDelete((File)files);

		}

		@Override
		protected void onPostExecute(Integer result) {
			switch (result) {
			case success:
				activity.showDirectoryChildren(null);
				if(deletedFileIsDirectory){
					Toast.makeText(activity, R.string.folder_deleted,Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(activity, R.string.file_deleted,Toast.LENGTH_SHORT).show();
				}
				break;
			case err_deleting_folder:
				Toast.makeText(activity,getString(R.string.error_deleting_folder,
						errorFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
				break;
			case err_deleting_child_file:
				Toast.makeText(activity,getString(R.string.error_deleting_child_file,
						errorFile.getAbsolutePath()),Toast.LENGTH_SHORT).show();
				break;
			case err_deleting_file:
				Toast.makeText(activity,getString(R.string.error_deleting_file,
						errorFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
				break;
			}
		}

	}

	private void deleteFileOrFolder(File file) {
		fileDeleted = true;
		positionAtDelete = getListView().getFirstVisiblePosition();
		deletedFileIsDirectory = file.isDirectory();
		new RecursiveDeleteTask().execute(file);
//		if (file.isDirectory()) {
//			if (recursiveDelete(file, true)) {
//				refreshList();
//				Toast.makeText(this, R.string.folder_deleted, Toast.LENGTH_SHORT).show();
//			}
//		} else {
//			if (file.delete()) {
//				// Delete was successful.
//				refreshList();
//				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
//			} else {
//				Toast.makeText(this, R.string.error_deleting_file, Toast.LENGTH_SHORT).show();
//			}
//		}
	}
	
    private void deleteMultiFile() {
//        int toast = 0;
        LinkedList<File> files = new LinkedList<File>();
        for (IconifiedText it : mDirectoryEntries) {
            if (!it.isSelected()) {
                continue;
            }

            File file = FileUtils.getFile(mPathBar.getCurrentDirectory(), it.getText());
            files.add(file);
//            if (file.isDirectory()) {
//                if (!recursiveDelete(file, true)) {
//                    break;
//                }
//            } else {
//                if (!file.delete()) {
//                    toast = R.string.error_deleting_file;
//                    break;
//                }
//            }
        }

        new RecursiveDeleteTask().execute(files);
        
//        if (toast == 0) {
//            // Delete was successful.
//            refreshList();
//            toast = R.string.file_deleted;
//        }
//
//        Toast.makeText(FileManagerActivity.this, toast, Toast.LENGTH_SHORT).show();
    }
    
    private void renameFileOrFolder(File file, String newFileName) {
        mOldFileName = file.getName();
        mNewFileName = newFileName;
        mDialogArgument = mNewFileName;
		if (newFileName != null && newFileName.length() > 0){
			if (!file.isDirectory() && !FileUtils.getExtension(newFileName).equals(FileUtils.getExtension(file.getName()))){
                showDialog(DIALOG_CHANGE_FILE_EXTENSION);
                return;
			}
		}
		File newFile = FileUtils.getFile(mPathBar.getCurrentDirectory(), newFileName);
        if (newFile.exists()){
            mDialogExistsAction = DIALOG_EXISTS_ACTION_RENAME;
            showDialog(DIALOG_WARNING_EXISTS);
        } else {
            rename(file, newFile);
        }
	}

	/**
	 * @param oldFile
	 * @param newFile
	 */
	private void rename(File oldFile, File newFile) {
		int toast = 0;
		if (oldFile.renameTo(newFile)) {
			// Rename was successful.
			showDirectoryChildren(null);
			if (newFile.isDirectory()) {
				toast = R.string.folder_renamed;
			} else {
				toast = R.string.file_renamed;
			}
		} else {
			if (newFile.isDirectory()) {
				toast = R.string.error_renaming_folder;
			} else {
				toast = R.string.error_renaming_file;
			}
		}
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
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
	
	private void sendFile(File file) {

		String filename = file.getName();
		String content = "hh";
		
		Log.i(TAG, "Title to send: " + filename);
		Log.i(TAG, "Content to send: " + content);

		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType(mMimeTypes.getMimeType(file.getName()));
		i.putExtra(Intent.EXTRA_SUBJECT, filename);
		//i.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri(file));
		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + FileManagerProvider.AUTHORITY + file.getAbsolutePath()));

		i = Intent.createChooser(i, getString(R.string.menu_send));
		
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.send_not_available,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Email client not installed");
		}
	}
	
	// The following functions should properly handle back button presses on every API Level.
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mPathBar.pressBack())
				return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.DONUT) {
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
					if (mState != STATE_MULTI_SELECT) {
					    // Move single file.
                        moveto = FileUtils.getFile(moveto, movefrom.getName());
						int toast = 0;
						if (movefrom.renameTo(moveto)) {
							// Move was successful.
							showDirectoryChildren(null);
				            if (moveto.isDirectory()) {
								toast = R.string.folder_moved;
							} else {
								toast = R.string.file_moved;
							}
						} else {
							if (moveto.isDirectory()) {
								toast = R.string.error_moving_folder;
							} else {
								toast = R.string.error_moving_file;
							}
						}
						Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
					} else {
    					// Move multi file.
                        int toast = 0;
                        for (IconifiedText it : mDirectoryEntries) {
                            if (!it.isSelected()) {
                                continue;
                            }

                            movefrom = FileUtils.getFile(mPathBar.getCurrentDirectory(), it.getText());
					        File newPath = FileUtils.getFile(moveto, movefrom.getName());
                            if (!movefrom.renameTo(newPath)) {
                            	showDirectoryChildren(null);
                                if (moveto.isDirectory()) {
                                    toast = R.string.error_moving_folder;
                                } else {
                                    toast = R.string.error_moving_file;
                                }
                                break;
                            }
					    }

                        if (toast == 0) {
                            // Move was successful.
                        	showDirectoryChildren(null);
                            toast = R.string.file_moved;
                        }

                        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
						
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
                    if (mState != STATE_MULTI_SELECT) {
                        // Copy single file.
                        copyto = createUniqueCopyName(this, copyto, copyfrom.getName());
                        
                        if (copyto != null) {
                            int toast = 0;
                            if (copy(copyfrom, copyto)) {
                                toast = R.string.file_copied;
                                showDirectoryChildren(null);
                            } else {
                                toast = R.string.error_copying_file;
                            }
                            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Copy multi file.
                        int toast = 0;
                        for (IconifiedText it : mDirectoryEntries) {
                            if (!it.isSelected()) {
                                continue;
                            }

                            copyfrom = FileUtils.getFile(mPathBar.getCurrentDirectory(), it.getText());
                            File newPath = createUniqueCopyName(this, copyto, copyfrom.getName());
                            if (copyto != null) {
                                if (!copy(copyfrom, newPath)) {
                                    toast = R.string.error_copying_file;
                                    break;
                                }
                            }
                        }

                        if (toast == 0) {
                            // Copy was successful.
                            toast = R.string.file_copied;
                            showDirectoryChildren(null);
                        }

                        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
				}				
			}
			break;

        case REQUEST_CODE_MULTI_SELECT:
            if (resultCode == RESULT_OK && data != null) {
            	showDirectoryChildren(null);
            }
            break;
        }
		
	}
	
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
	    if (//When the user chooses to show/hide hidden files, update the list
    		//to correspond with the user's choice
    		PreferenceActivity.PREFS_DISPLAYHIDDENFILES.equals(key)
    		//When the user changes the sortBy settings, update the list
    		|| PreferenceActivity.PREFS_SORTBY.equals(key)
    		|| PreferenceActivity.PREFS_ASCENDING.equals(key)){
	    	
	    	showDirectoryChildren(null);
	    }
	}

	/**
	 * Performs copying if this activity was launched as multiselection instance.
	 */
	public void actionCopy(){
        if (checkSelection()) {
            promptDestinationAndCopyFile();
        }
	}

	/**
	 * Performs moving if this activity was launched as multiselection instance.
	 */
	public void actionMove(){
        if (checkSelection()) {
            promptDestinationAndMoveFile();
        }
	}
	
	/**
	 * Performs deletion if this activity was launched as multiselection instance.
	 */
	public void actionDelete(){
	    if (checkSelection()) {
	        showDialog(DIALOG_MULTI_DELETE);
	    }
	}

	/**
	 * Performs compression if this activity was launched as multiselection instance.
	 */
	public void actionCompress(){
		compressMultiFile(mPathBar.getCurrentDirectory().getName()+".zip", null);
	}

	/**
	 * Performs send if this activity was launched as multiselection instance.
	 */
	public void actionSend(){
		final String sendFileName = mPathBar.getCurrentDirectory().getName()+"-attachment.zip";
		compressMultiFile(sendFileName, new CompressManager.OnCompressFinishedListener(){
			@Override
			public void compressFinished() {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_SEND);
				i.setType("application/zip");
				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mPathBar.getCurrentDirectory().getAbsolutePath() + "/" + sendFileName)));
				startActivity(Intent.createChooser(i, getString(R.string.send_chooser_title)));
			}
        });
	}
}

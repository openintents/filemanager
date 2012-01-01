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

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PreferenceActivity extends android.preference.PreferenceActivity
                                implements OnSharedPreferenceChangeListener {

	public static final String PREFS_MEDIASCAN = "mediascan";
	/**
	 * @since 2011-09-30
	 */
	public static final String PREFS_SHOWALLWARNING = "showallwarning";
	
	
	public static final String PREFS_DISPLAYHIDDENFILES = "displayhiddenfiles";
 	
	public static final String PREFS_SORTBY = "sortby";
	
	public static final String PREFS_ASCENDING = "ascending";
	
	public static final String PREFS_DEFAULTPICKFILEPATH = "defaultpickfilepath";
	
	private static final int DIALOG_DELETE_BOOKMARKS = 1;
	
	private Cursor deleteBookmarksCursor;
	private List<Uri> bookmarksToDelete = new LinkedList<Uri>();
	
	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.preferences);
		
		Preference editBookmarks = findPreference("editbookmarks");
		editBookmarks.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference pref){
		    	showDialog(DIALOG_DELETE_BOOKMARKS);
		        return false;
		    }
		});
		
		/* Register the onSharedPreferenceChanged listener to update the SortBy ListPreference summary */
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		/* Set the onSharedPreferenceChanged listener summary to its initial value */
		changeListPreferenceSummaryToCurrentValue((ListPreference)findPreference("sortby"));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	static boolean getMediaScanFromPreference(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean(PREFS_MEDIASCAN, false);
	}

	/**
	 * @since 2011-09-30
	 */
	static void setShowAllWarning(Context context, boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREFS_SHOWALLWARNING, enabled);
		editor.commit();
	}

	/**
	 * @since 2011-09-30
	 */
	static boolean getShowAllWarning(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_SHOWALLWARNING, true);
	}
	

	
	static void setDisplayHiddenFiles(Context context, boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREFS_DISPLAYHIDDENFILES, enabled);
		editor.commit();
	}


	static boolean getDisplayHiddenFiles(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_DISPLAYHIDDENFILES, true);
	}
	
	static void setDefaultPickFilePath(Context context, String path) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFS_DEFAULTPICKFILEPATH, path);
		editor.commit();
	}


	static String getDefaultPickFilePath(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREFS_DEFAULTPICKFILEPATH, null);
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("sortby")){
			changeListPreferenceSummaryToCurrentValue((ListPreference)findPreference(key));
		}
	}
	
	private void changeListPreferenceSummaryToCurrentValue(ListPreference listPref){
		listPref.setSummary(listPref.getEntry());
	}
	

	static int getSortBy(Context context) {
		/* entryValues must be a string-array while we need integers */
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
								 .getString(PREFS_SORTBY, "1"));
	}
	
	static boolean getAscending(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_ASCENDING, true);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DELETE_BOOKMARKS:
			deleteBookmarksCursor = getBookmarksCursor();
			AlertDialog dialog = 
				new AlertDialog.Builder(this)
					.setTitle(R.string.bookmarks_select_to_delete)
	        		.setMultiChoiceItems(deleteBookmarksCursor,
        				BookmarksProvider.CHECKED, BookmarksProvider.NAME,
        				new DialogInterface.OnMultiChoiceClickListener() {
			        	    public void onClick(DialogInterface dialog, int item, boolean checked) {
			        	    	if (deleteBookmarksCursor.moveToPosition(item)) {
		        	    			Uri deleteUri = ContentUris.withAppendedId(
					        	    					BookmarksProvider.CONTENT_URI,
					        	    					deleteBookmarksCursor.getInt(
				        	    							deleteBookmarksCursor.getColumnIndex(
			        	    									BookmarksProvider._ID)));
		        	    			if(checked)
		        	    				bookmarksToDelete.add(deleteUri);
		        	    			else
		        	    				bookmarksToDelete.remove(deleteUri);
		        	    			

	        	    				((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE)
    									.setEnabled((bookmarksToDelete.size() > 0) ? true : false);
		        	    				
		        	    			ContentValues checkedValues = new ContentValues();
		        	    			checkedValues.put(BookmarksProvider.CHECKED, checked ? 1 : 0);
				        	    	getContentResolver().update(deleteUri, checkedValues, null, null);
				        	    	//Have to use the deprecated requery()
				        	    	//(see http://code.google.com/p/android/issues/detail?id=2998)
				        	    	deleteBookmarksCursor.requery();
			        	    	}
			        	    	((AlertDialog)dialog).getListView().invalidate();
			        	    }
		        	})
		        	.setPositiveButton(R.string.bookmarks_delete, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for(Uri uri : bookmarksToDelete){
			        	    	getContentResolver().delete(uri, null, null);
							}
		        	    	Toast.makeText(PreferenceActivity.this,
		        	    			R.string.bookmarks_deleted, Toast.LENGTH_SHORT).show();
		        			restartBookmarksChecked();
						}
					})
		        	.setNegativeButton(R.string.bookmarks_cancel, new DialogInterface.OnClickListener() {
		        	    public void onClick(DialogInterface dialog, int item) {
		        	    	restartBookmarksChecked();
		        	    }
		        	}).create();
			// TODO: need to fix
			/*	Commenting this out for now.  Need another way to do this or check for SDK > 7.
			 *  With this in, Android 1.5 crashes upon launch.
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				}
			});*/
			return dialog;
		}
		return super.onCreateDialog(id);
	}
	
	private void restartBookmarksChecked(){
		ContentValues checkedValues = new ContentValues();
		checkedValues.put(BookmarksProvider.CHECKED, 0);
    	getContentResolver().update(BookmarksProvider.CONTENT_URI, checkedValues, null, null);
    	deleteBookmarksCursor.requery();
    	bookmarksToDelete.clear();
	}
	
	private Cursor getBookmarksCursor(){
		return managedQuery(BookmarksProvider.CONTENT_URI,
				new String[] {
					BookmarksProvider._ID,
					BookmarksProvider.NAME,
					BookmarksProvider.PATH,
					BookmarksProvider.CHECKED
				}, null, null, null);
	}
}

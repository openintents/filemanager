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

import org.openintents.filemanager.compatibility.HomeIconHelper;
import org.openintents.filemanager.search.SearchableActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;
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
		
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			HomeIconHelper.activity_actionbar_setDisplayHomeAsUpEnabled(this);
		}

		addPreferencesFromResource(R.xml.preferences);
		
		/* Register the onSharedPreferenceChanged listener to update the SortBy ListPreference summary */
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		/* Set the onSharedPreferenceChanged listener summary to its initial value */
		changeListPreferenceSummaryToCurrentValue((ListPreference)findPreference("sortby"));
		
		// Initialize search history reset confirmation dialog.
		findPreference("clear_search_button").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(PreferenceActivity.this)
				.setTitle(R.string.preference_search_title)
				.setMessage(R.string.preference_search_dialog_message)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	SearchableActivity.clearSearchRecents(PreferenceActivity.this);
				    	Toast.makeText(PreferenceActivity.this, R.string.search_history_cleared, Toast.LENGTH_SHORT).show();
				    }})
				 .setNegativeButton(android.R.string.cancel, null).show();
				
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			HomeIconHelper.showHome(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public static boolean getMediaScanFromPreference(Context context) {
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


	public static boolean getDisplayHiddenFiles(Context context) {
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
	

	public static int getSortBy(Context context) {
		/* entryValues must be a string-array while we need integers */
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
								 .getString(PREFS_SORTBY, "1"));
	}
	
	public static boolean getAscending(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_ASCENDING, true);
	}
}

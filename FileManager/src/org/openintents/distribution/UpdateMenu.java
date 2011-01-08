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

package org.openintents.distribution;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * @version 2009-10-23: support Market and aTrackDog
 * @version 2009-02-04
 * @author Peli
 *
 */
public class UpdateMenu {
	
	private static final String TAG = "UpdateMenu";
	
	/**
	 * If any of the following applications is installed,
	 * there is no need for a manual "Update" menu entry.
	 */
	public static final String[] UPDATE_CHECKER = new String[]
	    {
			"org.openintents.updatechecker", // OI Update
			"com.android.vending", // Google's Android Market
			"com.a0soft.gphone.aTrackDog" // aTrackDog
	    };
	
	/**
	 * Adds a menu item for update only if update checker is not installed.
	 * 
	 * @param context
	 * @param menu
	 * @param groupId
	 * @param itemId
	 * @param order
	 * @param titleRes
	 * @return
	 */
	public static MenuItem addUpdateMenu(Context context, Menu menu, int groupId,
			int itemId, int order, int titleRes) {
		PackageInfo pi = null;
		
		// Test for existence of all known update checker applications.
		for (int i = 0; i < UPDATE_CHECKER.length; i++) {
			try {
				pi = context.getPackageManager().getPackageInfo(
						UPDATE_CHECKER[i], 0);
			} catch (NameNotFoundException e) {
				// ignore
			}
			if (pi != null) {
				// At least one kind of update checker exists,
				// so there is no need to add a menu item.
				return null;
			}
		}
		
		// If we reach this point, we add a menu item for manual update.
		return menu.add(groupId, itemId, order, titleRes).setIcon(
				android.R.drawable.ic_menu_info_details).setShortcut('9',
				'u');
	}
	

	/**
	 * Shows dialog box with option to upgrade.
	 * 
	 * @param context
	 */
	public static void showUpdateBox(final Context context) {
		String version = null;
		try {
			version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		final Intent intent  = new Intent(Intent.ACTION_VIEW);
		final Intent intent2  = new Intent(Intent.ACTION_VIEW);
		new Builder(context).setMessage(context.getString(RD.string.update_box_text, version))
		.setPositiveButton(RD.string.update_check_now, new OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
				intent.setData(Uri.parse(context.getString(RD.string.update_app_url)));
				intent2.setData(Uri.parse(context.getString(RD.string.update_app_developer_url)));
				GetFromMarketDialog.startSaveActivity(context, intent, intent2);
			}
			
		}).setNegativeButton(RD.string.update_get_updater, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				intent.setData(Uri.parse(context.getString(RD.string.update_checker_url)));
				intent2.setData(Uri.parse(context.getString(RD.string.update_checker_developer_url)));
				GetFromMarketDialog.startSaveActivity(context, intent, intent2);
			}
			
		}).show();		
	}

}

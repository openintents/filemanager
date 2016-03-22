package org.openintents.filemanager.compatibility;

import org.openintents.filemanager.FileManagerActivity;

import android.app.Activity;
import android.content.Intent;

public class HomeIconHelper {
	private HomeIconHelper() {
	}

	public static void activity_actionbar_setHomeButtonEnabled(Activity act){
		act.getActionBar().setHomeButtonEnabled(true);
	}
	
	public static void activity_actionbar_setDisplayHomeAsUpEnabled(Activity act){
		if (act != null && act.getActionBar() != null){
			act.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Launch the home activity.
	 * @param act The currently displayed activity.
	 */
	public static void showHome(Activity act) {
		Intent intent = new Intent(act, FileManagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		act.startActivity(intent);
	}
}
package org.openintents.filemanager.util;

import org.openintents.filemanager.R;

import android.app.Activity;
import android.preference.PreferenceManager;

public abstract class UIUtils {

	public static void setThemeFor(Activity act) {
		if (PreferenceManager.getDefaultSharedPreferences(act).getBoolean("usedarktheme", false)) {
			act.setTheme(R.style.Theme_Dark);
		} else {
			act.setTheme(R.style.Theme_Light_DarkTitle);
		}

	}
}

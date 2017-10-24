package org.openintents.filemanager.util;

import android.app.Activity;
import android.preference.PreferenceManager;

import org.openintents.filemanager.R;

public abstract class UIUtils {

    public static void setThemeFor(Activity act) {
        if (PreferenceManager.getDefaultSharedPreferences(act).getBoolean("usedarktheme", true)) {
            act.setTheme(R.style.Theme_Dark);
        } else {
            act.setTheme(R.style.Theme_Light_DarkTitle);
        }
    }

    public static boolean shouldDialogInverseBackground(Activity act) {
        return !PreferenceManager.getDefaultSharedPreferences(act).getBoolean("usedarktheme", true);
    }
}
